package com.igt.fsp.game;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;

import com.igt.fsp.generated.db.FspOutData;
import com.igt.fsp.generated.db.FspOutDivisions;
import com.igt.fsp.generated.xml.inp.Game;

/**
 * Game PREMIO
 * 
 * @author lblum@menhir.com.ar
 */
public class FSPGamePremio extends FSPGame {

	// Game Id
	
	// Game values
	private static double RESERV_PCT = 0.5361;
	private static double INCR_PCT = 0.2165;
	private static double DEF_INCR = 1000000;
	private static double MIN_JACKPOT = 10000000;

	/**
	 * Empty constructor
	 * 
	 */
	public FSPGamePremio() {
		super();
	}

	/**
	 * Contructor
	 * 
	 * @param g The game from XML input
	 */

	public FSPGamePremio(Game g) {
		super(g);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void computeDivisions() {

		super.computeDivisions();

		// Compute the acumulated vs advertised

		// 1st: I need the last one for the accum and the last jackpot.
		// The accum is in the field accumTotal of the header and
		// the last jackpot is in the winnersAmount of the 1st division
		// In the case of winnersQ os the first division > 0, there is
		// no accum, and the jackpot resets to the start value.

		
		double currSales=0   , pastSales=0;
		double currAccum=0   , pastAccum=0;
		double currJackPot=0 , pastJackPot = 0;
		double pastFixes=0   , currFixes=0;
		long currWinners=0 , pastWinners=0;
		
		// Current values
		currSales = inpGame.getSalesTotalAmount().doubleValue() - inpGame.getCanceledTotalAmount().doubleValue();
		// Loop through divisions for the first
		List<com.igt.fsp.generated.xml.inp.Game.Division> lstCurrDiv = inpGame.getDivision();
		for ( com.igt.fsp.generated.xml.inp.Game.Division currD : lstCurrDiv ){
			if ( currD.getDivNR() == 1 ){
				currWinners = currD.getWinnersQ();				
			} else {
				currFixes += currD.getWinnersAmount().doubleValue();
			}
		}
		
		// Past Values
		String hqlStr = 
				  " from FspOutData\n" 
		        + "where gameId = :gameId\n" 
				+ "  and drawNr < :drawNr\n" 
		        + "order by drawNr desc\n";
		Query q = mainSession.createQuery(hqlStr);
		q.setMaxResults(1);
		q.setParameter("gameId", inpGame.getGameId());
		q.setParameter("drawNr", inpGame.getDrawNR());
		List<FspOutData> outList = q.list();

		if (outList.size() > 0) {
			FspOutData pastD = outList.get(0);
			pastSales = pastD.getSalestotalamount().doubleValue() 
					  - pastD.getCanceledtotalamount().doubleValue();
			pastAccum = pastD.getTotalaccum().doubleValue();
			Set<FspOutDivisions> outD = (Set<FspOutDivisions>) outList.get(0)
					.getFspOutDivisionses();
			for (FspOutDivisions d : outD) {
				if (d.getId().getDivnr() == 1) {
					// The first div contains the former jackPot
					pastWinners = d.getWinnersq();
					pastJackPot = d.getWinnersamount().doubleValue();
				} else {
					pastFixes += d.getWinnersamount().doubleValue();
				}
			}
					 
		}
		
		// The computations
		currAccum = pastSales * RESERV_PCT- pastFixes;	
		currAccum += pastAccum;
		
		
		
		if ( pastJackPot == 0 || pastWinners > 0 ) {
			// First record or reset 
			currJackPot = MIN_JACKPOT;
		} else {
			if ( currAccum - pastJackPot  <= 0 ) {
				currJackPot = pastJackPot + DEF_INCR;
			} else {
				currJackPot = pastJackPot + currSales * INCR_PCT;
			}			
		}
		if ( currAccum < 0 ) {
			// No accum
			currAccum = 0;
		}
		
		
		// Round jackpot to upper 1M
		currJackPot = Math.floor(currJackPot/DEF_INCR);
		currJackPot *= DEF_INCR;
		
		// Ask user for manual confirmation.		
		currJackPot = getNewJackPot(currJackPot);
		
		// Finaly the updates
		
		// Header
		hqlStr = "update FspOutData\n" + "   set totalAccum = :totalAccum\n"
				+ "where gameId = :gameId\n" + "  and drawNr = :drawNr\n";
		q = mainSession.createQuery(hqlStr);
		q.setParameter("gameId", inpGame.getGameId());
		q.setParameter("drawNr", inpGame.getDrawNR());
		q.setParameter("totalAccum", currAccum);
		q.executeUpdate();
		
		// Out divisions
		hqlStr = "update FspOutDivisions\n"
				+ "   set winnersAmount = :jackPot\n"
				+ "where gameId = :gameId\n" 
				+ "  and drawNr = :drawNr\n"
				+ "  and divNr  = 1\n";
		q = mainSession.createQuery(hqlStr);
		q.setParameter("gameId", inpGame.getGameId());
		q.setParameter("drawNr", inpGame.getDrawNR());
		q.setParameter("jackPot", currJackPot);
		q.executeUpdate();
		
		// Bonus: Inp Divisions
		hqlStr = "update FspInpDivisions\n"
				+ "   set winnersAmount = :jackPot\n"
				+ "where gameId = :gameId\n" 
				+ "  and drawNr = :drawNr\n"
				+ "  and divNr  = 1\n";
		q = mainSession.createQuery(hqlStr);
		q.setParameter("gameId", inpGame.getGameId());
		q.setParameter("drawNr", inpGame.getDrawNR());
		q.setParameter("jackPot", currJackPot);
		q.executeUpdate();
	}
	
	/**
	 * Done, ask user for confirmation of the jackpot
	 * 
	 * @param currJackPot the confirmated jackpot
	 * @return
	 */
	protected double getNewJackPot(double currJackPot) {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String newJackPotStr;
		double ret = currJackPot;
		System.out.println("Enter the jackpot for the next draw ["
				+ String.format("%f", currJackPot) + "]");
		try {
			newJackPotStr = br.readLine();
			
			if ( newJackPotStr != null )
				if ( !newJackPotStr.equals("")  )
					ret = Double.parseDouble(newJackPotStr);
		} catch (Exception e) {
			e.printStackTrace();
			ret = getNewJackPot(currJackPot);
		}
		System.out.println("Next jackpot = " + String.format("%f", ret));
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.igt.fsp.game.FSPGame#arrangeValues(com.igt.fsp.generated.xml.out.Game)
	 */
	@Override
	protected com.igt.fsp.generated.xml.out.Game arrangeValues(com.igt.fsp.generated.xml.out.Game outGame) {
		super.arrangeValues(outGame);
		
		// This game's jackpot is stored in last game's 1st division
		BigDecimal currJackPot = new BigDecimal(MIN_JACKPOT);
		String hqlStr = 
				  " from FspOutDivisions\n" 
		        + "where gameId = :gameId\n" 
				+ "  and drawNr = :drawNr\n"
		        + "  and divNr  = 1\n"; 
		Query q = mainSession.createQuery(hqlStr);
		q.setMaxResults(1);
		q.setParameter("gameId", inpGame.getGameId());
		q.setParameter("drawNr", inpGame.getDrawNR()-1);
		List<FspOutDivisions> outList = q.list();
		if ( q.list().size() >= 1) {
			FspOutDivisions d = (FspOutDivisions)q.list().get(0);
			currJackPot = d.getWinnersamount();			
		}
			
		// The first division has the total prize
		// and must be unit prize.
				
		for ( com.igt.fsp.generated.xml.out.Game.Division outDiv : outGame.getDivision() ){
			if ( outDiv.getDivNR() == 1 )
				outGame.setJackPot(outDiv.getWinnersAmount());
				if ( outDiv.getWinnersQ() > 0 ){
					double adjVal = currJackPot.doubleValue() /
							outDiv.getWinnersQ();
					outDiv.setWinnersAmount(BigDecimal.valueOf(adjVal));
				}
		}
		
		return outGame;

	}
	
	@Override
	public String getCode() {
		return "P";
	}

	@Override
	public String getTitle() {
		return "PREMIO";
	}
	
}
