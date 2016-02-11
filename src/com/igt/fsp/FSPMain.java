package com.igt.fsp;

import java.io.InputStream;

import com.igt.fsp.game.FSPGame;
import com.igt.fsp.game.FSPGameFactory;
import com.igt.fsp.singleton.DB;
import com.igt.fsp.singleton.FSPProperties;
/**
 * Main entry point

 * @author lblum@menhir.com.ar
 */

public class FSPMain {

	
	/**
	 * Main entry point
	 * 
	 * @param args command line parameters
	 */
	public static void main(String[] args) {

		FSPGame g = null;
		InputStream is;
		try {
			// Load command line arguments
			FSPProperties.loadFromCmdLine(args);
			
			System.out.println("Reading input file ...");
			is = FSPProperties.getInpXML();
			g = FSPGameFactory.getNewGame(is);
			is.close();
			try {

				System.out.println("Connecting to DB ...");
				DB.openSession();

				System.out.println("Writing to DB ...");
				g.saveToDB();
				
				System.out.println("Writing to XML ...");
				g.saveOutXML();
				DB.commit();
				
				
			} catch (Exception e) {
				try {
					DB.rollback();
				} catch (Exception e1) {
					// Nothing special
				}
				e.printStackTrace();
			} finally {
				try {
					DB.closeSession();
				} catch (Exception innerE) {
					// Nothing special
				}
			}
		} catch (Exception pE) {
			System.out.println(pE.getMessage());
		}
	}
}
