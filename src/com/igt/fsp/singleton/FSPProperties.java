/**
 * Config management
 * Singleton design pattern 
 */
package com.igt.fsp.singleton;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;

/**
 * @author lblum@menhir.com.ar
 *
 */
public class FSPProperties {
	
	private static Properties mProps = null;
	private static Options optList = new Options();	
	private static CommandLine cmdLine = null;

	/**
	 * Empty private constructor
	 */
	private FSPProperties() {
	}
	
	private static final String HIB_CONFIG_XML = "/com/igt/resource/hibernate.cfg.xml";
	private static final String LOG4J_CONFIG_XML = "/com/igt/resource/log4j.properties"; 
	private static final String XML_TEST_FILE ="/com/igt/resource/xml/fspInput.xml";
	private static final String DEF_DBHOST = "localhost";
	private static final String DEF_DBPORT = "5432";
	private static final String DEF_DBDB = "fspdb";
	private static final String DEF_DBSCHEMA = null;
	private static final String DEF_DBUSER = null;
	private static final String DEF_DBPASS = null;
	// private static final String DEF_LOGLEVEL = "DEBUG";
	private static final String OUT_FILE_BASE ="consolidated_winner_info_";
	/* DB2
	private static final String DEF_DBPORT = "50001";
	private static final String DEF_DBDB = "FPS";
	*/
	
	/**
	 * Load properties from command line 
	 */
	private static void loadProps() {
		
		mProps = new Properties();
		
		// Default values for some properties
		setPropertyVal("hibernate.config", HIB_CONFIG_XML);
		setPropertyVal("hibernate.connection.password",getOptVal("dbPass", DEF_DBPASS));
		setPropertyVal("hibernate.connection.username",getOptVal("dbUser", DEF_DBUSER));
		setPropertyVal("hibernate.default_schema",getOptVal("dbSchema", DEF_DBSCHEMA));
		setPropertyVal("hibernate.connection.url",getDBUrl());
		
		// Log
		setPropertyVal("log4j.configuration",LOG4J_CONFIG_XML);
		
		//org.apache.log4j.BasicConfigurator.configure();
		
	}
	
	/**
	 * 
	 * @return Database URL
	 */
	private static String getDBUrl() {
		String ret = "jdbc:postgresql://";
		ret += getOptVal("dbHost",DEF_DBHOST) + ":";
		ret += getOptVal("dbPort",DEF_DBPORT) + "/";
		ret += getOptVal("dbDB",DEF_DBDB);
		return ret;
		
	}
	
	/**
	 * Sets a property val, posible override from an System property
	 * @param propName the Property name
	 * @param propDefVal A suitable default val
	 */
	private static void setPropertyVal(String propName, String propDefVal){
		String val = System.getProperty(propName,propDefVal);
		if ( val!= null ) 
			mProps.put(propName, val);
	}
	
	/**
	 * 
	 * @return The properties array
	 */
	public static Properties getProps() {
		if ( mProps == null )
            synchronized (FSPProperties.class) {
                // Double check
                if (mProps == null) {
                	loadProps();
                }
            }
		return mProps;
	}
	
	/**
	 * 
	 * @return The resource for Hibernate Configuration
	 */
	public static String getHibConfig() {
		return (String) getProps().get("hibernate.config");
	}
	

	/**
	 * Merge with external properties array
	 * @param p The properties to be merged with
	 * @return The mix
	 */
	public static Properties mergeProps(Properties p){
		Properties mergeP = new Properties();
		mergeP.putAll(p);
		mergeP.putAll(getProps());
		return mergeP;
	}
	
	/**
	 * Command line parsing
	 * @param args The command line arguments array
	 * @throws ParseException
	 */
	public static void loadFromCmdLine( String[] args) throws ParseException {
		
		// The options
		optList = new Options();
		
		Option o = new Option("h","dbHost",true,"host of the database (default: localhost)");
		o.setRequired(false);
		optList.addOption(o);
		
		o = new Option("o","dbPort",true,"port of the database (default: 5432)");
		o.setRequired(false);
		optList.addOption(o);
		
		o = new Option("d","dbDB",true,"name of the database (default: fspdb)");
		o.setRequired(false);
		optList.addOption(o);
		
		o = new Option("s","dbSchema",true,"name of the database schema (default: public)");
		o.setRequired(false);
		optList.addOption(o);
		
		o = new Option("u","dbUser",true,"user of the database (no default)");
		o.setRequired(false);
		optList.addOption(o);
		
		o = new Option("p","dbPass",true,"password for the dbUser (no default: no required if no user defined)");
		o.setRequired(false);
		optList.addOption(o);
		
		o = new Option("f","inputFile",true,"input XML file (no default: required)");
		o.setRequired(false);
		optList.addOption(o);
		
		// Report options
		o = new Option("g","gameId",true,"id of the game to report (posible values: E,P) (default: all games)");
		o.setRequired(false);
		optList.addOption(o);

		o = new Option("w","drawNr",true,"draw Nr (default: the last one)");
		o.setRequired(false);
		optList.addOption(o);
		
		CommandLineParser parser = new DefaultParser();
		
		try {
			cmdLine = parser.parse( optList, args );
		} catch (ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
			
			formatter.printHelp("FSPMain", optList , true);
			throw e;
		}
		
	}
	
	/**
	 * The value of some command line option (no default)
	 * @param optName The option name
	 * @return The option val or null if not defined
	 */
	public static String getOptVal(String optName){
		return getOptVal(optName,null);
	}
	
	
	public static String getReportGameId() {
		return getOptVal("gameId",null);		
	}

	
	public static Integer getReportDrawNr() {
		Integer retVal = null;
		String retStr = getOptVal("drawNr",null);
		try {
			retVal = Integer.parseInt(retStr);
		} catch (NumberFormatException e) {
			retVal = null;
		}
		return retVal;		
	}
	
	/**
	 * The value of some command line option (no default)
	 * @param optName The option name
	 * @param optDefault The default value
	 * @return The option val or default value
	 */
	public static String getOptVal(String optName,String optDefault){

		String ret = "";
		try {
			ret = cmdLine.getOptionValue(optName);
		} catch (Exception e1) {
			ret = optDefault;
		}
		return ret==null?optDefault:ret;
	}
	
	/**
	 * 
	 * @return The XML input stream
	 * @throws Exception
	 */
	public static InputStream getInpXML() throws Exception {
				
		String fName = getOptVal("inputFile",XML_TEST_FILE);
		
		
		InputStream fis = null;
		fis = new FileInputStream(fName);
		return fis;
	}
	
	/**
	 * An output file Stream
	 * @param gameId The id of the game
	 * @param drawNr The draw number
	 * @return An opened output stream
	 * @throws Exception
	 */
	public static FileOutputStream getOutXML(String gameId , long drawNr ) throws Exception {
		
		String outFName = String.format("%s%s%s_%05d.xml", getOutPath() , 
				                                       OUT_FILE_BASE ,
				                                       gameId,
				                                       drawNr);
		
		FileOutputStream os = new FileOutputStream(outFName);
		
		return os;
	}
	
	/**
	 * 
	 * @return The path for output files
	 */
	public static String getOutPath() {
		String fName = getOptVal("inputFile",XML_TEST_FILE);
		
		return FilenameUtils.getFullPath(fName);
	}

}
