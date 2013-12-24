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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.choicemaker.cm.io.blocking.automated.base.BlockingSet;
import com.choicemaker.cm.io.blocking.automated.base.BlockingValue;

public class PrintUtils {

	private PrintUtils() {}

	public static void logBlockingValue(Logger logger, String msg, BlockingValue bv) {
		logBlockingValue(logger, Level.DEBUG, msg, bv);
	}

	public static void logBlockingValue(Logger logger, Level level, String msg, BlockingValue bv) {
		// Precondition
		if (logger == null) {
			throw new IllegalArgumentException("null logger");
		}
		if (level == null) {
			throw new IllegalArgumentException("null logging level");
		}

		if (logger.isEnabledFor(level)) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			if (msg != null) {
				pw.print(msg + " ");
			}
			printBlockingValue(pw, bv);
			logger.log(level,sw.toString());
		}
	}

	public static void printBlockingValue(PrintWriter pw, BlockingValue bv) {
		// Precondition
		if (pw == null) {
			throw new IllegalArgumentException("null PrintWriter");
		}

		if (bv == null) {
			pw.print("[null]");
		} else {
			pw.print(
				"[value "
					+ bv.value
					+ " count "
					+ bv.count
					+ " "
					+ bv.blockingField.dbField.name
					+ " "
					+ bv.blockingField.dbField.table.name
					+ " "
					+ bv.tableSize
					+ "]");
		}
	}

	public static void logBlockingSet(Logger logger, String msg, BlockingSet b) {
		logBlockingSet(logger, Level.DEBUG, msg, b);
	}

	public static void logBlockingSet(Logger logger, Level level, String msg, BlockingSet b) {
		// Precondition
		if (logger == null) {
			throw new IllegalArgumentException("null logger");
		}
		if (level == null) {
			throw new IllegalArgumentException("null logging level");
		}

		if (logger.isEnabledFor(level)) {
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
		BlockingSet b) {
		if (b == null) {
			pw.print("[null]");
		} else {
			pw.print(
				"[count: "
					+ b.getExpectedCount()
					+ " table: "
					+ b.getNumTables()
					+ " values: { ");
			BlockingValue[] bvs = b.getBlockingValues();
			for (int j = 0; j < bvs.length; j++) {
				printBlockingValue(pw, bvs[j]);
				pw.print(" ");
			}
			pw.print("}]");
		}
	}

}

