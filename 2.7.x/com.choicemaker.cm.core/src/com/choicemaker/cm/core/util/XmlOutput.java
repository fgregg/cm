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

import java.io.IOException;
import java.io.Writer;
import java.util.Date;

/**
 * Description
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.1 $ $Date: 2010/01/20 15:05:03 $
 */

public class XmlOutput {
	public static void writeAttribute(Writer w, String name, String value) throws IOException {
		if (value != null) {
			w.write(' ' + name + "=\"" + escapeAttributeEntities(value) + "\"");
		}
	}

	public static void writeAttribute(Writer w, String name, char value) throws IOException {
		if (value >= 32) {
			writeAttribute(w, name, String.valueOf(value));
		} else if (value == 0) {
			writeAttribute(w, name, "");
		}
	}

	public static void writeAttribute(Writer w, String name, Date value) throws IOException {
		if (value != null) {
			writeAttribute(w, name, DateHelper.format(value));
		}
	}

	public static void writeAttribute(Writer w, String name, long value) throws IOException {
		writeAttribute(w, name, String.valueOf(value));
	}

	public static void writeOpenBeginElement(Writer w, String name) throws IOException {
		w.write("<" + name);
	}

	public static void writeClosedBeginElement(Writer w, String name) throws IOException {
		w.write("<" + name + ">");
	}

	public static void writeEndElement(Writer w, String name) throws IOException {
		w.write("</" + name + ">");
	}

	public static void writeOpenEndTag(Writer w) throws IOException {
		w.write(">");
	}

	public static void writeClosedEndTag(Writer w) throws IOException {
		w.write("/>");
	}

	/**
	 * <p>
	 * This will take the pre-defined entities in XML 1.0 and
	 * convert their character representation to the appropriate
	 * entity reference, suitable for XML attributes.  It does
	 * no converstion for ' because it's not necessary as the outputter
	 * writes attributes surrounded by double-quotes.
	 * </p>
	 *
	 * @param str <code>String</code> input to escape.
	 * @return <code>String</code> with escaped content.
	 */
	public static String escapeAttributeEntities(String str) {
		StringBuffer buffer;
		char ch;
		String entity;

		buffer = null;
		for (int i = 0; i < str.length(); i++) {
			ch = str.charAt(i);
			switch (ch) {
				case '<' :
					entity = "&lt;";
					break;
				case '>' :
					entity = "&gt;";
					break;
					/*
					                case '\'' :
					                    entity = "&apos;";
					                    break;
					*/
				case '\"' :
					entity = "&quot;";
					break;
				case '&' :
					entity = "&amp;";
					break;
				default :
					entity = null;
					if (ch < 32) {
						if (ch == 0x9 || ch == 0xA || ch == 0xD) {
							entity = "&#" + (int) ch + ";";
						} else {
							entity = "";
						}
					} else if (ch > 127) {
						if (ch <= 0xD7FF || ch >= 0xE000 && ch <= 0xFFFD || ch >= 0x10000 && ch <= 0x10FFFF) {
							entity = "&#" + (int) ch + ";";
						} else {
							entity = "";
						}
					}
					break;
			}
			if (buffer == null) {
				if (entity != null) {
					// An entity occurred, so we'll have to use StringBuffer
					// (allocate room for it plus a few more entities).
					buffer = new StringBuffer(str.length() + 20);
					// Copy previous skipped characters and fall through
					// to pickup current character
					buffer.append(str.substring(0, i));
					buffer.append(entity);
				}
			} else {
				if (entity == null) {
					buffer.append(ch);
				} else {
					buffer.append(entity);
				}
			}
		}

		// If there were any entities, return the escaped characters
		// that we put in the StringBuffer. Otherwise, just return
		// the unmodified input string.
		return (buffer == null) ? str : buffer.toString();
	}

	/**
	 * <p>
	 * This will take the three pre-defined entities in XML 1.0
	 * (used specifically in XML elements) and convert their character
	 * representation to the appropriate entity reference, suitable for
	 * XML element content.
	 * </p>
	 *
	 * @param st <code>String</code> input to escape.
	 * @return <code>String</code> with escaped content.
	 */
	public static String escapeElementEntities(String str) {
		StringBuffer buffer;
		char ch;
		String entity;

		buffer = null;
		for (int i = 0; i < str.length(); i++) {
			ch = str.charAt(i);
			switch (ch) {
				case '<' :
					entity = "&lt;";
					break;
				case '>' :
					entity = "&gt;";
					break;
				case '&' :
					entity = "&amp;";
					break;
				default :
					entity = null;
					break;
			}
			if (buffer == null) {
				if (entity != null) {
					// An entity occurred, so we'll have to use StringBuffer
					// (allocate room for it plus a few more entities).
					buffer = new StringBuffer(str.length() + 20);
					// Copy previous skipped characters and fall through
					// to pickup current character
					buffer.append(str.substring(0, i));
					buffer.append(entity);
				}
			} else {
				if (entity == null) {
					buffer.append(ch);
				} else {
					buffer.append(entity);
				}
			}
		}

		// If there were any entities, return the escaped characters
		// that we put in the StringBuffer. Otherwise, just return
		// the unmodified input string.
		return (buffer == null) ? str : buffer.toString();
	}
}
