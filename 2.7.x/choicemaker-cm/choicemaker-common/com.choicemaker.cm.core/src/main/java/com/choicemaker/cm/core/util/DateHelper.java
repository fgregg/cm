/*
 * Copyright (c) 2001, 2009 ChoiceMaker Technologies, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License
 * v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     ChoiceMaker Technologies, Inc. - initial API and implementation
 */
package com.choicemaker.cm.core.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * Utilities for dealing with <code>Date</code>s.
 * 
 * 2006-04-10 (pcheung) - Removed synchronize keyword from methods to improve parallel
 * performance.
 * 
 * 2008-08-07 (rphall) - Replaced synchronized use of static DateFormat variables with
 * use of ThreadLocal variables
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/27 21:12:19 $
 */
public class DateHelper {

	private static Logger logger = Logger.getLogger(DateHelper.class.getName());

	//	protected static final DateFormat oldTwoDigitYearLocaleSpecificDateParser = DateFormat.getDateInstance(DateFormat.SHORT);
	private static final ThreadLocal
	oldTwoDigitYearLocaleSpecificDateParserThreadLocal = new ThreadLocal() {
		protected Object initialValue() {
			DateFormat oldTwoDigitYearLocaleSpecificDateParser =
				(DateFormat) DateFormat
					.getDateInstance(DateFormat.SHORT)
					.clone();
			return oldTwoDigitYearLocaleSpecificDateParser;
		}
	};

	//	protected static final DateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private static final ThreadLocal
	sqlDateFormatThreadLocal = new ThreadLocal() {
		protected Object initialValue() {
			DateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			return sqlDateFormat;
		}
	};

	//	protected static final DateFormat displayDateFormat;
	private static final ThreadLocal
	displayDateFormatThreadLocal = new ThreadLocal() {
		protected Object initialValue() {
			DateFormat df =
				(DateFormat) DateFormat
					.getDateInstance(DateFormat.SHORT)
					.clone();
			if (df instanceof SimpleDateFormat) {
				String pattern = ((SimpleDateFormat) df).toPattern();
				StringBuffer sb = new StringBuffer();

				// i may be incremented inside loop
				for (int i = 0; i < pattern.length();) {
					char c = pattern.charAt(i);
					if (c == 'y') {
						sb.append("yyyy");
						while (i < pattern.length() && pattern.charAt(i) == 'y') {
							++i;
						}
					} else {
						sb.append(c);
						++i;
					}
				}
			
				df = new SimpleDateFormat(sb.toString());
			}
			return df;
		}
	};

	//	NOTE: initialValue of dateParserThreadLocal should be null,
	//  UNLESS it has been set for some thread, BEFORE it is used by
	//  ANY thread; see setDateParser(DateParser)
	//  and parse(String) methods.
	//
	//  To implement this behaviour, there's one global value of _dateParser.
	//  Each thead-local copy of dateParserThreadLocal is initialized with this global copy.
	//
	private static DateParser _dateParser;
	private static final ThreadLocal dateParserThreadLocal = new ThreadLocal() {
		protected Object initialValue() {
			DateParser dateParser = null;
			if (_dateParser != null) {
				dateParser = (DateParser) _dateParser.clone();
			}
			return dateParser;
		}
	};

	public static void setDateParser(DateParser v) {
		_dateParser = v;
	}

	/**
	 * Returns the <code>Date</code> represented by a <code>String</code>.
	 *
	 * @param   s  The string representing the date.
	 * @return  The date represented by the string. <null> if the latter
	 *            does not denote a valid date.
	 */
	public static Date parse(String s) {
		if (s == null || s.length() == 0) {
			return null;
		}
		Date d = null;
		try {
			DateFormat sqlDateFormat = (DateFormat) sqlDateFormatThreadLocal.get();
			DateParser dateParser = (DateParser) dateParserThreadLocal.get();
			DateFormat oldTwoDigitYearLocaleSpecificDateParser =
				(DateFormat) oldTwoDigitYearLocaleSpecificDateParserThreadLocal.get();
			if (s.length() == 10 && s.charAt(4) == '-' && s.charAt(7) == '-') {
				d = sqlDateFormat.parse(s);
			} else if (dateParser != null) {
				d = dateParser.parse(s);
			} else {
				d = oldTwoDigitYearLocaleSpecificDateParser.parse(s);
			}
		} catch (Exception ex) {
			logger.error("Parsing date: " + s, ex);
		}
		return d;
	}

	public static Date parseSqlDateOrTimestamp(String s) {
		if (s == null) {
			return null;
		}
		if (s.length() > 10) {
			return parseSqlTimestamp(s);
		} else {
			return parseSqlDate(s);
		}
	}

	public static Date parseSqlTimestamp(String s) {
		if (s == null) {
			return null;
		}
		Date d = null;
		try {
			d = java.sql.Timestamp.valueOf(s);
			// Convert to Date because comparison between Date and Timestamp
			// does not work as expected.
			if (d != null) {
				d = new java.sql.Date(d.getTime());
			}
		} catch (Exception ex) {
			logger.error("Parsing date: " + s, ex);
		}
		return d;
	}

	public static Date parseSqlDate(String s) {
		if (s == null) {
			return null;
		}
		Date d = null;
		try {
			d = java.sql.Date.valueOf(s);
		} catch (Exception ex) {
			logger.error("Parsing date: " + s, ex);
		}
		return d;
	}

	public static String format(Date d) {
		if (d == null) {
			return null;
		} else {
			DateFormat sqlDateFormat = (DateFormat) sqlDateFormatThreadLocal.get();
			return sqlDateFormat.format(d);
		}
	}

	public static String formatDisplay(Date d) {
		if (d == null) {
			return null;
		} else {
			DateFormat displayDateFormat = (DateFormat) displayDateFormatThreadLocal.get();
			return displayDateFormat.format(d);
		}
	}

//	private static String[] MONTHS =
//		{ "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC" };

	public static String formatDb(Date d) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		if (day.length() == 1)
			day = "0" + day;
		String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
		if (month.length() == 1)
			month = "0" + month;
		String year = String.valueOf(cal.get(Calendar.YEAR));
		return year + "-" + month + "-" + day;
	}

}

 
