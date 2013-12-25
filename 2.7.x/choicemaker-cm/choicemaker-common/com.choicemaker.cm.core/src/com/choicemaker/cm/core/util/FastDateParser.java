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

import java.util.Calendar;
import java.util.Date;

/**
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/27 21:16:34 $
 * @deprecated This class will cause silent problems in training data. Use
 * a DateFormat instance instead.
 */
public class FastDateParser implements DateParser {

	private final int centuryTurn;
	private final boolean dmy;

	private static final int IDX_BEGIN = 0;
	private static final int IDX_LENGTH = 1;
	private static final int ARRAY_LENGTH = 2;

	public FastDateParser(int centuryTurn, boolean dmy) {
		this.centuryTurn = centuryTurn;
		this.dmy = dmy;
	}
	
	public Object clone() {
		return new FastDateParser(this.centuryTurn,this.dmy);
	}

	public Date parse(String s) {

		int[] parseArgs = new int[ARRAY_LENGTH];
		// NOTE: parseArgs[IDX_BEGIN] == 0 by initialization
		parseArgs[IDX_LENGTH] = s.length();

		int month = getNum(parseArgs, s);
		int day = getNum(parseArgs, s);
		int year = getNum(parseArgs, s);

		// FIXME: Since the default value of century is 20,
		// this miserable class falls apart after 2020.
		// Worse, ChoiceMaker clients are likely to archive
		// training data beyond 2020.
		// Even worse, the resulting errors will be silent ones
		// in values assigned to training weights.
		if (year < 1000) {
			if (year < centuryTurn) {
				year += 2000;
			} else {
				year += 1900;
			}
		}

		final Calendar cal = Calendar.getInstance();
		if (dmy) { // European D/M/Y format: switch day and month
			cal.set(year, day - 1, month);
		} else {
			cal.set(year, month - 1, day);
		}

		return cal.getTime();
	}

	private int getNum(int[] parseArgs, String s) {
		// Changes value of parseArgs[IDX_BEGIN], but not parseArgs[IDX_LENGTH]
		final int len = parseArgs[IDX_LENGTH];
		int beg = parseArgs[IDX_BEGIN];

		int res = 0;
		char c;
		while (beg < len && '0' <= (c = s.charAt(beg)) && c <= '9') {
			res = 10 * res + c - '0';
			++beg;
		}
		++beg;

		parseArgs[IDX_BEGIN] = beg;
		return res;
	}

}

