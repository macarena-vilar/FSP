package com.igt.fsp.reports;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Simply date formatter for the JAXB Marshaller

 * @author lblum@menhir.com.ar
 *
 */
public class DateAdapter extends XmlAdapter<String, XMLGregorianCalendar> {


	@Override
	public String marshal(XMLGregorianCalendar v) throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd");
		return dateFormat.format(xmlGregorianCalendarToDate(v));
	}

	@Override
	public XMLGregorianCalendar unmarshal(String v) throws Exception {
		// No need, never unmarshall
		return null;
	}

	/**
	 * Conversion from XMLGregorianCalendar to java.util.Date
	 * @param c The Calendar
	 * @return A java.util.Date equivalent to the Calendar
	 */
	public static Date xmlGregorianCalendarToDate ( XMLGregorianCalendar c ){
        if(c == null) {
            return null;
        }
        return c.toGregorianCalendar().getTime();
	}
	
	/**
	 * Conversion from java.util.Date to XMLGregorianCalendar
	 * @param d The Date
	 * @return A Calendar equivalent to the java.util.Date 
	 */
	public static XMLGregorianCalendar dateToXmlGregorianCalendar (Date d) throws DatatypeConfigurationException{
		// Ugly ...
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(d);
		XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);

		return xmlDate;
	}

}
