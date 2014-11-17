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
package com.choicemaker.cm.io.blocking.automated.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.choicemaker.cm.io.blocking.automated.IBlockingSet;
import com.choicemaker.cm.io.blocking.automated.IBlockingValue;

public class PrintUtils {

	private PrintUtils() {}

	public static void logBlockingValue(Logger logger, String msg, IBlockingValue bv) {
		logBlockingValue(logger, Level.FINE, msg, bv);
	}

	public static void logBlockingValue(Logger logger, Level level, String msg, IBlockingValue bv) {
		// Precondition
		if (logger == null) {
			throw new IllegalArgumentException("null logger");
		}
		if (level == null) {
			throw new IllegalArgumentException("null logging level");
		}

		if (logger.isLoggable(level)) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			if (msg != null) {
				pw.print(msg + " ");
			}
			printBlockingValue(pw, bv);
			logger.log(level,sw.toString());
		}
	}

	public static void printBlockingValue(PrintWriter pw, IBlockingValue bv) {
		// Precondition
		if (pw == null) {
			throw new IllegalArgumentException("null PrintWriter");
		}

		if (bv == null) {
			pw.print("[null]");
		} else {
			pw.print(
				"[value "
					+ bv.getValue()
					+ " count "
					+ bv.getCount()
					+ " "
					+ bv.getBlockingField().getDbField().getName()
					+ " "
					+ bv.getBlockingField().getDbField().getTable().getName()
					+ " "
					+ bv.getTableSize()
					+ "]");
		}
	}

	public static void logBlockingSet(Logger logger, String msg, IBlockingSet b) {
		logBlockingSet(logger, Level.FINE, msg, b);
	}

	public static void logBlockingSet(Logger logger, Level level, String msg, IBlockingSet b) {
		// Precondition
		if (logger == null) {
			throw new IllegalArgumentException("null logger");
		}
		if (level == null) {
			throw new IllegalArgumentException("null logging level");
		}

		if (logger.isLoggable(level)) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			if (msg != null) {
				pw.print(msg + " ");
			}
			printBlockingSet(pw,b);
			logger.log(level,sw.toString());
		}
	}

	public static void printBlockingSet(
		PrintWriter pw,
		IBlockingSet b) {
		if (b == null) {
			pw.print("[null]");
		} else {
			pw.print(
				"[count: "
					+ b.getExpectedCount()
					+ " table: "
					+ b.getNumTables()
					+ " values: { ");
			IBlockingValue[] bvs = b.getBlockingValues();
			for (int j = 0; j < bvs.length; j++) {
				printBlockingValue(pw, bvs[j]);
				pw.print(" ");
			}
			pw.print("}]");
		}
	}

}

