/**
 * DB connection sigleton
 */
package com.igt.fsp.singleton;

import java.sql.Connection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.w3c.dom.Document;

/**
 * @author lblum@menhir.com.ar
 *
 */
public class DB {
	private static volatile SessionFactory sessionFactory = null;
	private static volatile Session session = null;
	private static volatile Transaction transac = null;

	/**
	 * Empty private contructor
	 */
	private DB() {
	}

	/**
	 * Loads configuration from resource
	 * 
	 * @param resourcePath The XML for hibernate
	 * @return The XML Document
	 * @throws Exception
	 */
	private static Document parseConfiguration(String resourcePath)
			throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// Validate DTD only on development environments
		factory.setValidating(false); // Don't validate DTD
		
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document ret =builder.parse(builder.getClass().getResourceAsStream(
				resourcePath));
		return ret;
	}

	/**
	 * The sigleton
	 * 
	 * @return A Session factory based on XML configuration and command line parameters
	 * @throws HibernateException
	 * @throws Exception
	 */
	private static SessionFactory getSessionFactory() throws HibernateException, Exception {
		if (sessionFactory == null) {
			synchronized (SessionFactory.class) {
				// Double check
				if (sessionFactory == null) {
					Configuration cfg = new Configuration()
							.configure(parseConfiguration(FSPProperties.getHibConfig()));
					// Merge with global config
					cfg.setProperties(FSPProperties.mergeProps(cfg
							.getProperties()));
					sessionFactory = cfg.buildSessionFactory();
				}
			}
		}
		return sessionFactory;
	}

	/**
	 * Connection to the database
	 * @return An open Session, and a transaction
	 * @throws Exception
	 */
	public static Session openSession() throws Exception {
		session = getSessionFactory().openSession();
		transac = session.beginTransaction();

		return session;
	}

	/**
	 * Closes the open session
	 */
	public static void closeSession() {
		if ( session != null )
			session.close();
		session = null;
		transac = null;
	}

	/**
	 * Commit transaction
	 */
	public static void commit() {
		transac.commit();
	}

	/**
	 * Rollback transaction
	 */
	public static void rollback() {
		transac.rollback();
	}

	/**
	 * Begin transaction
	 */
	public static void beginTran() {
		transac.begin();
	}
	
	/**
	 * Get the JDBC connection (for reports)
	 * @return the JDBC connection associated with the Hibernate session
	 * @throws Exception 
	 * @throws HibernateException 
	 */
	@SuppressWarnings("deprecation")
	public static Connection getJDBCConnection() throws HibernateException, Exception {
		return openSession().connection();
	}

}
