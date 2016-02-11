package com.igt.fsp.game;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import com.igt.fsp.generated.db.FspInpDivisions;
import com.igt.fsp.generated.db.FspInpDivisionsId;
import com.igt.fsp.generated.db.FspInpHeader;
import com.igt.fsp.generated.db.FspInpHeaderId;
import com.igt.fsp.generated.db.FspInpTotals;
import com.igt.fsp.generated.db.FspInpTotalsId;

/**
 * Factory for games (static model)
 * 
 * @author lblum@menhir.com.ar
 */
public class FSPGameFactory {

	public static String GAME_ELEGIDOS = "E";
	public static String GAME_ELEGIDOSTEETE = "T";
	public static String GAME_PREMIO = "P";
	
	/**
	 * Generator of FSPGames based on gameId
	 * 
	 * @param g The game read from the XML file
	 * @return A new FSPGame
	 * @throws Exception
	 */
	public static FSPGame getNewGame(com.igt.fsp.generated.xml.inp.Game g) throws Exception {
		
		FSPGame ret = null;
		
		if ( g.getGameId().equals(GAME_ELEGIDOS)){
			ret = new FSPGameElegidos(g);
		} else if (( g.getGameId().equals(GAME_ELEGIDOSTEETE))){
			ret = new FSPGameElegidosTeeTe(g);			
		} else if (( g.getGameId().equals(GAME_PREMIO))){
			ret = new FSPGamePremio(g);			
		} else {
			throw new Exception("Invalid game => ["+g.getGameId()+"]");
		}
		return ret;
	}

	/**
	 * Load a game from an XML input stream
	 * 
	 * @param inpStr the input stream
	 * @return a new game
	 * @throws Exception
	 */
	public static FSPGame getNewGame(InputStream inpStr) throws Exception{
		JAXBContext jaxbContext = JAXBContext.newInstance(com.igt.fsp.generated.xml.inp.Game.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();		
		com.igt.fsp.generated.xml.inp.Game g = (com.igt.fsp.generated.xml.inp.Game) jaxbUnmarshaller.unmarshal(inpStr);

		return getNewGame(g);
	}
		
	/**
	 * Generates a DB game (FspInpHeader) from a XML read game
	 * 
	 * @param fspGame The game read from a file
	 * @return a DB game
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static FspInpHeader loadInpFromXML(FSPGame fspGame) throws Exception{
		
		com.igt.fsp.generated.xml.inp.Game g = fspGame.getInpGame();
		FspInpHeader ret = new FspInpHeader();
		
		// The header
		char gameId = g.getGameId().charAt(0);
		long drawNR = g.getDrawNR();
		FspInpHeaderId hId = new FspInpHeaderId(gameId,drawNR);
		ret.setId(hId);
		ret.setDrawdate(g.getDrawDate().toGregorianCalendar().getTime());
		
		FspInpTotalsId tId = new FspInpTotalsId(ret,g.getVendorId().charAt(0));
		FspInpTotals tot = new FspInpTotals();
		tot.setId(tId);
		tot.setCanceledtotalamount(g.getCanceledTotalAmount());
		tot.setCanceledtotalq(g.getCanceledTotalQ());
		tot.setSalestotalamount(g.getSalesTotalAmount());
		tot.setSalestotalq(g.getSalesTotalQ());
		
		// Divisions
		Set divList = new HashSet(0);
		for (com.igt.fsp.generated.xml.inp.Game.Division d : g.getDivision() ){
			FspInpDivisionsId dId = new FspInpDivisionsId(tot,d.getDivNR());
			FspInpDivisions div = new FspInpDivisions();
			div.setId(dId);
			div.setWinnersamount(d.getWinnersAmount());
			div.setWinnersq(d.getWinnersQ());
			divList.add(div);
		}
		tot.setFspInpDivisionses(divList);
		
		ret.getFspInpTotalses().add(tot);
		
		return ret;
	}
}
