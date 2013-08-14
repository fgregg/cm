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
package com.choicemaker.cm.matching.cfg.train;

import java.io.IOException;

/**
 * .
 * 
 * @author Adam Winkel
 */
public class ParsedDataFilter {

	public static final int PARSED = -1;
	public static final int UNPARSED = -2;
	public static final int ALL = -3;

	public static void filterRawData(ParsedDataReader reader) throws IOException {
		filterRawData(reader, UNPARSED);
	}
	
	public static void filterRawData(ParsedDataReader reader, int which) throws IOException {
		while (reader.next()) {
			String[] raw = reader.getRawData();
			if (raw == null) {
				continue;	
			}
			if (which == ALL) {
				System.out.println(join(raw, " | "));
			} else {
				boolean parsed = reader.getNumParseTrees() + reader.getNumParsedData() > 0;
				if (parsed && which == PARSED) {
					System.out.println(join(raw, " | "));
				} else if (!parsed && which == UNPARSED) {
					System.out.println(join(raw, " | "));
				}
			}
		}
	}
	
	private static String join(String[] pieces, String with) {
		if (pieces.length == 0) {
			return "";	
		} else {
			StringBuffer buff = new StringBuffer();
			buff.append(pieces[0]);
			for (int i = 1; i < pieces.length; i++) {
				buff.append(with);
				buff.append(pieces[i]);
			}
			return buff.toString();
		}
	}

}
