package com.igt.fsp.reports;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;

import com.igt.fsp.singleton.DB;
import com.igt.fsp.singleton.FSPProperties;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

/**
 * Emit reports
 * 
 * @author lblum
 *
 */
public class FSPReport {

	private static final String COD_ELEGIDOS = "E";
	private static final String COD_PREMIO = "P";
	private static final String COD_ELEGIDOSTEETE = "T";
	private static final String STR_ELEGIDOS = "ELEGIDOS";
	private static final String STR_PREMIO = "PREMIO";
	private static final String STR_ELEGIDOSTEETE = "ELEGIDOS TEETE";

	private static final String RPT_FILENAME = "consolidated.jasper";
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		try {
			// Load command line arguments
			FSPProperties.loadFromCmdLine(args);
			
			// Connect to DB
			DB.openSession();
			
			String gameId = FSPProperties.getReportGameId();
			Integer drawNr = FSPProperties.getReportDrawNr();
			
			ArrayList<Object[]> reps = new ArrayList<Object[]>();
			
			if ( gameId == null ) {
				reps.add(new Object[]{COD_ELEGIDOS, null , STR_ELEGIDOS});
				reps.add(new Object[]{COD_ELEGIDOSTEETE, null , STR_ELEGIDOSTEETE});
				reps.add(new Object[]{COD_PREMIO, null , STR_PREMIO});
			} else {
				reps.add(new Object[]{gameId,drawNr,gameId.equals(COD_ELEGIDOS)?STR_ELEGIDOS:STR_PREMIO});
			}
			
			for (Object[] o : reps) {
				try {
					drawNr = o[1] == null ? getLastNr((String)(o[0])):(Integer)o[1];
					System.out.println("Printing " + ((String)o[2]).toLowerCase() +" report ...");
					printReport((String)o[0], drawNr ,(String)o[2]);
				} catch(Exception e) {
					// Nothing to do
				}
				
			}
			
		} catch (Exception pE) {
			pE.printStackTrace();
		}
	}
	
	
	/**
	 * Return the last game save to th DB
	 * @return the last game (empty throws Exception)
	 * @throws Exception
	 */
	protected static Integer getLastNr (String gameId) throws Exception {
		int ret = -1;
		
		Session mainSession = DB.openSession();
		String hqlStr = "select max(id.drawnr) from FspOutData where gameId=:gameId";
		Query q = mainSession.createQuery(hqlStr);
		q.setParameter("gameId", gameId);
		
		@SuppressWarnings("unchecked")
		List<Object> outList = q.list();
		if ( outList.size() < 1 ){
			throw new Exception("No data found");
		}
		
		ret = (Integer) outList.get(0);
		
		return ret;
	}
	
	/**
	 * The report printing method
	 * 
	 * @throws Exception
	 */
	protected static void printReport(String gameId , int drawNr , String gameName ) throws Exception {
		
		// Open the report file
		InputStream fis = null;
		fis = FSPReport.class.getResourceAsStream(RPT_FILENAME);
		JasperReport jasRpt = (JasperReport)JRLoader.loadObject(fis); 
				
		// Create a map of parameters to pass to the report.
		Map<String,Object> parameters = new HashMap<String,Object>();
		parameters.put("gameId", gameId);
		parameters.put("drawNr", new Integer(drawNr));
		parameters.put("gameName", gameName);
		
		// Connect to database
		Connection conn = DB.getJDBCConnection();
		
		// Fill the report with data
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasRpt,parameters,conn);
		
		// Save to file system
		String outFName = String.format("./%s%04d-winner_summary.pdf", gameName.toLowerCase(),drawNr);		
		OutputStream os = new FileOutputStream(new File(outFName));
		
		JasperExportManager.exportReportToPdfStream(jasperPrint, os);
		os.flush();
		os.close();

	}
	
	protected class RepOpt {
		private String gameId;
		private Integer drawNr;
		private String gameName;
		/**
		 * @return the gameId
		 */
		public String getGameId() {
			return gameId;
		}
		/**
		 * @return the drawNr
		 */
		public Integer getDrawNr() {
			return drawNr;
		}
		
		/**
		 * @param gameId
		 * @param drawNr
		 */
		public RepOpt(String gameId, Integer drawNr , String gameName) {
			super();
			this.gameId = gameId;
			this.drawNr = drawNr;
			this.gameName = gameName;
		}
		
	}

}
