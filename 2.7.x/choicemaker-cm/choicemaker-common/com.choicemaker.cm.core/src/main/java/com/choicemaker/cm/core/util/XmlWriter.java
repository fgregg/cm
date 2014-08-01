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
import java.util.ArrayList;
import java.util.Date;

import com.choicemaker.cm.core.Constants;

/**
 * Description
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/27 21:27:02 $
 */
public class XmlWriter extends Writer {
	private Writer w;
	private ArrayList s;
	private int len;
	private int size;
	private boolean openElement;

	// BUG 2009-09-04 rphall
	// much of the code in this class duplicates code in XmlOutput
	// this code should probably call code in XmlOutput

	public XmlWriter(Writer w, String encoding) throws IOException {
		this.w = w;
		if (encoding != null) {
			w.write("<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>" + Constants.LINE_SEPARATOR);
		}
		len = 0;
		size = 0;
		s = new ArrayList();
	}

	public XmlWriter(Writer w) throws IOException {
		this(w, null);
	}

	public void close() throws IOException {
		w.close();
	}

	public void flush() throws IOException {
		w.flush();
	}

	public void write(char[] cbuf, int off, int len) throws IOException {
		w.write(cbuf, off, len);
	}

	public void beginElement(String name) throws IOException {
		if (openElement) {
			w.write(">" + Constants.LINE_SEPARATOR);
		}
		w.write("<" + name);
		openElement = true;
		s.add(name);
		++size;
		len = size;
	}

	public void endElement() throws IOException {
		if (len == size && openElement) {
			w.write("/>" + Constants.LINE_SEPARATOR);
		} else {
			w.write("</" + s.get(size - 1) + ">" + Constants.LINE_SEPARATOR);
		}
		--size;
		s.remove(size);
		openElement = false;
	}

	public void text(String t) throws IOException {
		if (openElement) {
			w.write(">");
			openElement = false;
		}
		w.write(escapeElementEntities(t));
	}

	public void attribute(String name, String value) throws IOException {
		if (!openElement) {
			throw new IllegalStateException("No open element.");
		}
		w.write(' ' + name + "=\"" + escapeAttributeEntities(value) + "\"");
	}

	public void attribute(String name, Object value) throws IOException {
		attribute(name, value.toString());
	}

	public void attribute(String name, long value) throws IOException {
		attribute(name, String.valueOf(value));
	}

	public void attribute(String name, Date value) throws IOException {
		attribute(name, DateHelper.format(value));
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
					// BUG? 2009-09-04 rphall
					// FIXME? not consistent w/ XmlOutput.escapeAttributeEntities(String)
					if (ch < 32 || ch > 127) {
						entity = "&#" + (int)ch + ";";
					}
					// ENDBUG
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
