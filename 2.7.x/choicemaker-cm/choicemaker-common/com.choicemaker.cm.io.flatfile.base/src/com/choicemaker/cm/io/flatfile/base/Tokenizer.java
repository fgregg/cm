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
package com.choicemaker.cm.io.flatfile.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;

import com.choicemaker.cm.core.util.DateHelper;

public class Tokenizer {
	private boolean fixedWidth;
	private int tagWidth;
	private char separator;
	private boolean tagged;
	private BufferedReader reader;
	private String line;
	private int lineLength;
	public String tag;
	public int pos;
	private char[] buf = new char[8192];
	private boolean wn;

	public Tokenizer(BufferedReader reader, char separator) {
		this.reader = reader;
		this.separator = separator;
	}

	public Tokenizer(BufferedReader reader) {
		this.reader = reader;
		this.fixedWidth = true;
	}

	public Tokenizer(BufferedReader reader, int tagWidth) {
		this(reader);
		this.tagged = true;
		this.tagWidth = tagWidth;
	}

	public Tokenizer(BufferedReader reader, boolean fixedWidth, char separator, boolean tagged, int tagWidth) {
		this.reader = reader;
		this.fixedWidth = fixedWidth;
		this.separator = separator;
		this.tagged = tagged;
		this.tagWidth = tagWidth;
	}

	public boolean readLine() throws IOException {
		line = reader.readLine();
		pos = 0;
		if (line != null) {
			lineLength = line.length();
			if (tagged) {
				tag = nextTrimedString(tagWidth).intern();
			}
			return true;
		} else {
			tag = null;
			return false;
		}
	}

	public boolean lineRead() {
		return line != null;
	}

	public boolean ready() throws IOException {
		return reader.ready();
	}

	public boolean wasNull() {
		return wn;
	}
	
	public void skip(int num) {
		if(!fixedWidth && line != null) {
			while(pos < lineLength) {
				// <BUGFIX author="rphall date="2005-10-25"
				//		summary="throw an exception if not a separator">
				// <BUG author="rphall" date="2005-10-25">
				// If line.charAt(pos) is not the separator,
				// then this code hangs in an endless loop
				//if(line.charAt(pos) == separator && --num == 0) {
				//	break;
				// </BUG>
				if(line.charAt(pos) == separator) {
					if (--num == 0) {
						break;
					}
				} else {
					String msg = "Algorithm error: Tokenizer.skip(int): "
						+ "pos == '" + pos + "', "
						+ "line.charAt(pos) == '" + line.charAt(pos) + "'";
					throw new RuntimeException(msg);
				}
			}
			++pos;
		}
	}

	public String nextInternedString(int width) {
		String s = nextString(width);
		return s != null ? s.intern() : null;
	}
	
	public String getInernedString(int start, int width) {
		String s = getString(start, width);
		return s != null ? s.intern() : null;
	}

	public String nextInternedTrimedString(int width) {
		String s = nextString(width);
		return s != null ? s.trim().intern() : null;
	}

	public String getInernedTrimedString(int start, int width) {
		String s = getString(start, width);
		return s != null ? s.trim().intern() : null;
	}

	public String nextTrimedString(int width) {
		String s = nextString(width);
		return s != null ? s.trim() : null;
	}
	
	public String getTrimedString(int start, int width) {
		String s = getString(start, width);
		return s != null ? s.trim() : null;
	}

	public String nextString(int width) {
		if (line == null)
			return null;
		if (fixedWidth) {
			if (pos >= lineLength) {
				wn = true;
				return null;
			} else {
				pos += width;
				wn = false;
				return line.substring(pos - width, pos);
			}
		} else {
			if (pos >= lineLength) {
				wn = true;
				return null;
			} else {
				int bpos = 0;
				char c;
				while (pos < lineLength && (c = line.charAt(pos)) != separator) {
					buf[bpos++] = c;
					++pos;
				}
				++pos;
				wn = false;
				return new String(buf, 0, bpos);
			}
		}
	}

	public String getString(int start, int width) {
		if(fixedWidth) {
			if(start < lineLength) {
				wn = false;
				if(start + width <= lineLength) {
					return line.substring(start, start + width);
				} else {
					return line.substring(start);
				}
			} else {
				wn = true;
				return null;
			}
		} else {
			return nextString(width);
		}
	}

	public char nextChar(int width, char nullRepresentation) {
		String s = nextString(width);
		if(s != null && s.length() > 0) {
			char c = s.charAt(0);
			if(c == nullRepresentation) {
				return '\0';
			} else {
				return c;
			}
		} else {
			return '\0';
		}
	}
	
	public char getChar(int start, int width, char nullRepresentation) {
		if(start < lineLength) {
			char c = line.charAt(start);
			if(c == nullRepresentation) {
				return '\0';
			} else {
				return c;
			}
		} else {
			return '\0';
		}			
	}

	public int nextInt(int width) {
		String s = nextTrimedString(width);
		return s != null && s.length() > 0 ? Integer.parseInt(s) : 0;
	}

	public int getInt(int start, int width) {
		String s = getTrimedString(start, width);
		return s != null && s.length() > 0 ? Integer.parseInt(s) : 0;
	}

	public long nextLong(int width) {
		String s = nextTrimedString(width);
		return s != null && s.length() > 0 ? Long.parseLong(s) : 0L;
	}

	public long getLong(int start, int width) {
		String s = getTrimedString(start, width);
		return s != null && s.length() > 0 ? Long.parseLong(s) : 0L;
	}

	public Date nextDate(int width) {
		String s = nextTrimedString(width);
		return s != null && s.length() > 0 ? DateHelper.parse(s) : null;
	}
	
	public Date getDate(int start, int width) {
		String s = getTrimedString(start, width);
		return s != null && s.length() > 0 ? DateHelper.parse(s) : null;
	}
}
