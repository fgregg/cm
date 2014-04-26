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
package com.choicemaker.cm.matching.gen;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.util.DateHelper;
import com.choicemaker.cm.core.util.StringUtils;

/**
 * Utilities for dealing with <code>Date</code>s.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/27 22:24:58 $
 */
public class DateUtils extends DateHelper {

	private static Logger logger = Logger.getLogger(DateUtils.class);

	// private static Calendar calendar = Calendar.getInstance();

	/**
	 * Removes the separators from the date leaving just the
	 * numbers.  For example 8/9/62 become 8962 whereas 12/24/01
	 * become 122401.
	 *
	 * @param d the input date
	 * @return the date as number according to the following formula:
	 * year + 10000 * day + x * month, where x is 100000 if day &lt; 10 and 100000 otherwise
	 */
	public static int numbersOnly(Date d) {
		if (d == null) {
			return 0;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(d);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int year = calendar.get(Calendar.YEAR);
		month = (day < 10) ? (month * 100000) : (month * 1000000);
		day *= 10000;
		return year + month + day;
	}

	/**
	 * Returns an integer representation of <code>d</code> in YYYYMMDD format.
	 *
	 * Returns an int representation of <code>d</code> according to the following formula:
	 * <p>
	 * 	 year * 10000 + month * 100 + day
	 * </p>
	 * Thus, the result is in the format YYYYMMDD.
	 *
	 * @param d the input date
	 * @return the date as number according to the following formula:
	 */
	public static int numericFormat(Date d) {
		if (d == null) {
			return 0;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(d);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int year = calendar.get(Calendar.YEAR);
		month = month * 100;
		year *= 10000;
		return year + month + day;
	}

	/**
	 * Returns true iff the years of d1 and d2 are the same and either
	 * the month or day of one is missing a digit.
	 *
	 * In other words, in addition to the years being the same, one of the
	 * following must be true:
	 * <ul>
	 * 	<li>the months are the same and the day of one is the day of the other
	 * 	with a digit added on the left or right </li>
	 *	<li>the days are the same and the month of one is the month of the
	 * 	other with a digit added on the left or right </li>
	 * </ul>
	 * For example, the function returns true for <code>1/5/2002</code> and <code>1/25/2002</code>.
	 *
	 * @param d1 the first date
	 * @param d2 the second date
	 * @return true if d1 and d2 are the same modulo a missing number in one.
	 */
	public static boolean missingNumber(Date d1, Date d2) {
		if (d1 != null && d2 != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(d1);
			int month1 = calendar.get(Calendar.MONTH) + 1;
			int day1 = calendar.get(Calendar.DAY_OF_MONTH);
			int year1 = calendar.get(Calendar.YEAR);
			calendar.setTime(d2);
			int month2 = calendar.get(Calendar.MONTH) + 1;
			int day2 = calendar.get(Calendar.DAY_OF_MONTH);
			int year2 = calendar.get(Calendar.YEAR);
			if (year1 == year2) {
				if (month1 == month2) {
					return missingNumber(day1, day2);
				} else if (day1 == day2) {
					return missingNumber(month1, month2);
				}
			}
		}
		return false;
	}

	/**
	 * Helper method for missingNumber(Date, Date)
	 */
	private static boolean missingNumber(int a, int b) {
		int s, l;
		if (a < 10 && b >= 10) {
			s = a;
			l = b;
		} else if (a > 10 && b < 10) {
			s = b;
			l = a;
		} else {
			return false;
		}
		return s == l / 10 || s == l % 10;
	}

	/**
	 * Returns the difference between d1 and d2 in days.
	 * If d1 and d2 are different times of day, then the result will
	 * not be an integer.
	 *
	 * @param d1 the first date
	 * @param d2 the second date
	 * @return the difference between d1 and d2 in days
	 */
	public static float daysApart(Date d1, Date d2) {
		long t1 = d1.getTime();
		long t2 = d2.getTime();
		long diff = java.lang.Math.abs(t1 - t2);
		float daysDiff = diff / 86400000f; //diff in days
		return daysDiff;
	}

	/**
	 * Returns true iff d1 and d2 are more than 365 days apart.
	 *
	 * @param d1 the first date
	 * @param d2 the second date
	 * @return true iff d1 and d2 are more than 365 days apart
	 */
	public static boolean moreThanYearApart(Date d1, Date d2) {
		return (daysApart(d1, d2) > 365);
	}

	/**
	 * Returns true iff d is non-null and its day is the first of
	 * the month.
	 *
	 * @param d the input date
	 * @return true if d falls on the first of the month
	 */
	public static boolean isFirstOfMonth(Date d) {
		if (d == null) {
			return false;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(d);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		return day == 1;
	}

	/**
	 * Returns true iff d is non-null and is on January 1st.
	 *
	 * @param d the input date
	 * @return true if d falls on January 1st
	 */
	public static boolean isJanuary1(Date d) {
		return isFirstOfYear(d);
	}

	/**
	 * Returns true iff d is non-null and on January 1st.
	 *
	 * @param d the input date
	 * @return true iff d falls on January 1st
	 */
	public static boolean isFirstOfYear(Date d) {
		if (d == null) {
			return false;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(d);
		return calendar.get(Calendar.DAY_OF_MONTH) == 1
			&& calendar.get(Calendar.MONTH) == 0;
	}

	/**
	 * Returns true iff d1 and d2 have the same month and year.
	 *
	 * @param d1 the first date
	 * @param d2 the second date
	 * @return whether the month and year of d1 and d2 are the same
	 */
	public static boolean sameMonthAndYear(Date d1, Date d2) {
		boolean answer = false;
		if (d1 == null || d2 == null) {
			return false;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(d1);
		int month = calendar.get(Calendar.MONTH);
		int year = calendar.get(Calendar.YEAR);
		calendar.setTime(d2);
		answer = (month == calendar.get(Calendar.MONTH));
		answer = answer && (year == calendar.get(Calendar.YEAR));
		return answer;
	}

	/**
	 * Returns true iff d1 and d2 have the same day and month.
	 *
	 * @param d1 the first date
	 * @param d2 the second date
	 * @return whether the day and month of d1 and d2 are the same
	 */
	public static boolean sameDayAndMonth(Date d1, Date d2) {
		if (d1 == null || d2 == null) {
			return false;
		}
		boolean answer = false;
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(d1);
			int month = calendar.get(Calendar.MONTH);
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			calendar.setTime(d2);
			answer = (day == calendar.get(Calendar.DAY_OF_MONTH));
			answer = answer && (month == calendar.get(Calendar.MONTH));
		} catch (IllegalArgumentException ex) {
			logger.error("d1: " + d1.getTime() + ", d2: " + d2.getTime(), ex);
		}
		return answer;
	}

	/**
	 * Returns true iff d1 and d2 have the same day and month.
	 *
	 * @param d1 the first date
	 * @param d2 the second date
	 * @return whether dates are different in either last digit of the year or 3rh(decade) digit of the year
	 */
	public static boolean onlyDecadeOrLastYearDigitDiff(Date d1, Date d2) {
		if (d1 == null || d2 == null) {
			return false;
		}
		boolean answer = false;
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(d1);
			int month = calendar.get(Calendar.MONTH);
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			int year1 = calendar.get(Calendar.YEAR);
			calendar.setTime(d2);
			int year2 = calendar.get(Calendar.YEAR);
			answer = (day == calendar.get(Calendar.DAY_OF_MONTH));
			answer = answer && (month == calendar.get(Calendar.MONTH));
			answer = answer && (year1 / 100 == year2 / 100);
			int lt1 = year1 % 100;
			int lt2 = year2 % 100;
			answer = answer && (lt1 / 10 == lt2 / 10 || lt1 % 10 == lt2 % 10);
		} catch (IllegalArgumentException ex) {
			logger.error("d1: " + d1.getTime() + ", d2: " + d2.getTime(), ex);
		}
		return answer;
	}

	/**
	 * Returns true iff d1 and d2 have the same year and the month of d1 is the day
	 * of d2 and vice versa.  For example, returns true for '3/2/1979' and '2/3/1979',
	 * returns false for '3/2/1979' and '3/3/1979'.
	 *
	 * @param d1 the first date
	 * @param d2 the second date
	 * @return whether the year of d1 and d2 are the same and the day and month are swapped.
	 */
	public static boolean swappedDayMonth(Date d1, Date d2) {
		if (d1 != null && d2 != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(d1);
			int month1 = calendar.get(Calendar.MONTH) + 1;
			int day1 = calendar.get(Calendar.DAY_OF_MONTH);
			int year1 = calendar.get(Calendar.YEAR);
			calendar.setTime(d2);
			int month2 = calendar.get(Calendar.MONTH) + 1;
			int day2 = calendar.get(Calendar.DAY_OF_MONTH);
			int year2 = calendar.get(Calendar.YEAR);
			return month1 == day2 && month2 == day1 && year1 == year2;
		} else {
			return false;
		}
	}

	/**
	 * Returns the input date's year and month as a String in YYYYMM format.
	 *
	 * @param d the input date
	 * @return d's year and month as a YYYYMM String
	 */
	public static String yearAndMonth(Date d) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(d);
		StringBuffer buff = new StringBuffer(6);
		buff.append(StringUtils.padLeft("" + calendar.get(Calendar.YEAR), 4));
		buff.append(
			StringUtils.padLeft("" + (calendar.get(Calendar.MONTH) + 1), 2));
		return buff.toString();
	}

	/**
	 * Returns the year as an integer.
	 *
	 * @param d the input date
	 * @return the year
	 */
	public static int getYear(Date d) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(d);
		return calendar.get(Calendar.YEAR);
	}

	/**
	 * Returns the month as an integer in the range [1,12].
	 *
	 * @param d the input date
	 * @return the month in the range [1,12]
	 */
	public static int getMonth(Date d) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(d);
		return calendar.get(Calendar.MONTH) + 1;
	}

	/**
	 * Returns the day of the month.
	 *
	 * @param d the input date
	 * @return the day of the month
	 */
	public static int getDayOfMonth(Date d) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(d);
		return calendar.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * Returns the current year.
	 *
	 * @return the year of the current date
	 */
	public static int getCurrentYear() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		return calendar.get(Calendar.YEAR);
	}

	/**
	 * Returns a representation of the input date in YYYYMMDDHHMMSS format.
	 *
	 * @param d the input date
	 * @return a date time String in YYYYMMDDHHMMSS format
	 */
	public static String getDateTimeString(Date d) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(d);
		StringBuffer buff = new StringBuffer(14);
		buff.append(StringUtils.padLeft("" + calendar.get(Calendar.YEAR), 4));
		buff.append(
			StringUtils.padLeft("" + (calendar.get(Calendar.MONTH) + 1), 2));
		buff.append(
			StringUtils.padLeft("" + calendar.get(Calendar.DAY_OF_MONTH), 2));
		buff.append(
			StringUtils.padLeft("" + calendar.get(Calendar.HOUR_OF_DAY), 2));
		buff.append(StringUtils.padLeft("" + calendar.get(Calendar.MINUTE), 2));
		buff.append(StringUtils.padLeft("" + calendar.get(Calendar.SECOND), 2));
		return buff.toString();
	}

	/**
	 * Note, the range of mm is 1 thru 12, inclusive.
	 */
	public static Date getDate(int yyyy, int mm, int dd) {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(yyyy, mm - 1, dd);
		return calendar.getTime();
	}

}

