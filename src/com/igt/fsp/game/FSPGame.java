package com.igt.fsp.game;

import java.io.FileOutputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.hibernate.Query;
import org.hibernate.Session;

import com.igt.fsp.generated.db.FspInpHeader;
import com.igt.fsp.generated.db.FspOutData;
import com.igt.fsp.generated.db.FspOutDivisions;
import com.igt.fsp.reports.DateAdapter;
import com.igt.fsp.singleton.DB;
import com.igt.fsp.singleton.FSPProperties;

/**
 * Base abstract class of all games<br/>
 * 
 * @author lblum@menhir.com.ar
 *
 */
public abstract class FSPGame {
	

	/**
	 * The game read from the XML file
	 */
	protected com.igt.fsp.generated.xml.inp.Game inpGame = null;

	/**
	 * The hibernate session (shorthand for the singleton) 
	 */
	protected Session mainSession;	
	
	/**
	 * Empty constructor
	 */
	public FSPGame () {		
	}
		
	/**
	 * Contructor
	 * 
	 * @param g The game from XML input
	 */
	public FSPGame( com.igt.fsp.generated.xml.inp.Game g ) {
		setInpGame(g);
	}
	/**
	 * @return the inpGame
	 */
	public com.igt.fsp.generated.xml.inp.Game getInpGame() {
		return inpGame;
	}

	/**
	 * @param inpGame the inpGame to set
	 */
	public void setInpGame(com.igt.fsp.generated.xml.inp.Game inpGame) {
		this.inpGame = inpGame;
	}

	/**
	 * @return the mainSession
	 */
	public Session getMainSession() {
		return mainSession;
	}

	/**
	 * Saves input XML to DB (FSP_INP_* and FSP_OUT_*)
	 * 
	 * @throws Exception
	 */
	public void saveToDB() throws Exception {
		// Generate Inp record
		FspInpHeader inpHeader = FSPGameFactory.loadInpFromXML(this);
		mainSession = DB.openSession();

		// Begin Inp records
		
		// Delete old inp records		
		String delInpQryStr ="delete FspInpDivisions where gameId = :gameId and drawNr = :drawNr and vendorId = :vendorId";
		Query delInpQry = mainSession.createQuery(delInpQryStr);
		delInpQry.setParameter("gameId", inpGame.getGameId());
		delInpQry.setParameter("drawNr", inpGame.getDrawNR());
		delInpQry.setParameter("vendorId", inpGame.getVendorId());
		delInpQry.executeUpdate();
		
		// save to DB
		mainSession.saveOrUpdate(inpHeader);
		mainSession.flush();		
		
		// End Inp records
		
		// Begin Out records
		
		// Delete old out records
		String delOutQryStr ="delete FspOutDivisions where gameId = :gameId and drawNr = :drawNr";
		Query delOutQry = mainSession.createQuery(delOutQryStr);
		delOutQry.setParameter("gameId", inpGame.getGameId());
		delOutQry.setParameter("drawNr", inpGame.getDrawNR());
		delOutQry.executeUpdate();
		delOutQryStr ="delete FspOutData where gameId = :gameId and drawNr = :drawNr";
		delOutQry = mainSession.createQuery(delOutQryStr);
		delOutQry.setParameter("gameId", inpGame.getGameId());
		delOutQry.setParameter("drawNr", inpGame.getDrawNR());
		delOutQry.executeUpdate();

		// Totals and divisions
		computeTotals();
		computeDivisions();
		
		// End Out records
	}

	/**
	 * Consolidates the vendors
	 */
	protected void computeTotals() {
		String schema = FSPProperties.getOptVal("dbSchema");
		// TODO: to hql
		String insertQuery = "insert into " + schema + ".fsp_out_data \n" +
				"      ( gameId, \n" +
				"        drawNR, \n" +
				"        drawDate, \n" +
				"        salesTotalQ, \n" +
				"        salesTotalAmount, \n" +
				"        canceledTotalQ, \n" +
				"        canceledTotalAmount) \n" +
				"select h.gameId,\n" +
				"       h.drawNR,\n" +
				"       min(h.drawDate),\n" +
				"       sum(t.salesTotalQ),\n" +
				"       sum(t.salesTotalAmount),\n" +
				"       sum(t.canceledTotalQ),\n" +
				"       sum(t.canceledTotalAmount)\n" +
				"  from " + schema + ".fsp_inp_header h,\n" +
				"       " + schema + ".fsp_inp_totals t\n" +
				" where h.gameId = :gameId\n" +
				"   and h.drawNr = :drawNr\n" +
				"   and h.gameId = t.gameId\n" +
				"   and h.drawNr = t.drawNR\n" +
				" group by h.gameId,\n" +
				"          h.drawNR\n";
		Query insQ = mainSession.createSQLQuery(insertQuery);
		insQ.setParameter("gameId", inpGame.getGameId());
		insQ.setParameter("drawNr", inpGame.getDrawNR());
		insQ.executeUpdate();
	}
	
	/**
	 * Consolidates the divisions
	 */
	protected void computeDivisions() {
		String schema = FSPProperties.getOptVal("dbSchema");
		// TODO: to hql
		String insertQuery = 
				"insert into " + schema + ".fsp_out_divisions \n" +
				"      ( gameId,\n" +
				"        drawNr,\n" +
				"        divNr, \n" +
				"        winnersQ, \n" +
				"        winnersAmount) \n" +
				"select gameId,\n" +
				"       drawNr,\n" +
				"       divNr,\n" +
				"       sum(winnersQ),\n" +
				"       sum(winnersAmount)\n" +
				"  from " + schema + ".fsp_inp_divisions\n" +
				" where gameId = :gameId\n" +
				"   and drawNr = :drawNr\n" +
				" group by gameId,\n" +
				"          drawNr,\n" +
				"          divNr\n" ;

		Query insQ = mainSession.createSQLQuery(insertQuery);
		insQ.setParameter("gameId", inpGame.getGameId());
		insQ.setParameter("drawNr", inpGame.getDrawNR());
		insQ.executeUpdate();		
	}
	
	/**
	 * Saves the XML file generated from the FSP_OUT_* records, conforming to fspOutput.xsd 
	 * @throws Exception
	 */
	public void saveOutXML() throws Exception {
		// XML from DB
		String hqlStr = 
				  " from FspOutData\n" 
		        + "where gameId = :gameId\n" 
				+ "  and drawNr = :drawNr\n";
		Query q = mainSession.createQuery(hqlStr);
		q.setParameter("gameId", inpGame.getGameId());
		q.setParameter("drawNr", inpGame.getDrawNR());
		@SuppressWarnings("unchecked")
		List<FspOutData> outList = q.list();

		if (outList.size() == 0) 
			return;
		
		FspOutData outData = outList.get(0);
		com.igt.fsp.generated.xml.out.Game outGame = new com.igt.fsp.generated.xml.out.Game();

		// Attr. copy
		String gameId = "";
		gameId += outData.getId().getGameid();
		outGame.setGameId(gameId);
		outGame.setDrawNR(outData.getId().getDrawnr());
		outGame.setDrawDate(DateAdapter.dateToXmlGregorianCalendar(outData.getDrawdate()));
		outGame.setSalesTotalQ((int)outData.getSalestotalq());
		outGame.setSalesTotalAmount(outData.getSalestotalamount());
		outGame.setCanceledTotalQ((int)outData.getCanceledtotalq());
		outGame.setCanceledTotalAmount(outData.getCanceledtotalamount());
		
		// Div copy
		@SuppressWarnings("unchecked")
		Set<FspOutDivisions> outD = (Set<FspOutDivisions>) outData.getFspOutDivisionses();
		for (FspOutDivisions d : outD) {
			com.igt.fsp.generated.xml.out.Game.Division outDiv = new com.igt.fsp.generated.xml.out.Game.Division();
			outDiv.setDivNR(d.getId().getDivnr());
			outDiv.setWinnersQ((int)d.getWinnersq());
			outDiv.setWinnersAmount(d.getWinnersamount());
			outGame.getDivision().add(outDiv);
		}
	
		outGame = arrangeValues(outGame);
		
		JAXBContext jaxbContext = JAXBContext.newInstance(com.igt.fsp.generated.xml.out.Game.class);
		
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
 
		// output pretty printed
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, "fspOutput.xsd");
 
		StringWriter sw = new StringWriter();
		jaxbMarshaller.marshal(outGame, sw);
		String outXML = sw.toString();
		
		FileOutputStream os = FSPProperties.getOutXML(gameId,outData.getId().getDrawnr());
		os.write(outXML.getBytes(), 0, outXML.getBytes().length);
		os.close();
		
	}
	
	/**
	 * Last chance to prepare values for XML output
	 */
	protected com.igt.fsp.generated.xml.out.Game arrangeValues(com.igt.fsp.generated.xml.out.Game outGame) {
		// In the default case: nothing
		return outGame;
	}

	/**
	 * Just for debug
	 */
	@Override
	public String toString() {
		return inpGame.toString(); 
	}
	
	/**
	 * The one char Game Code
	 * @return the gameCode
	 */
	public abstract String getCode();
	
	/**
	 * Descriptive name of the game
	 * @return
	 */
	public abstract String getTitle();
	
}
