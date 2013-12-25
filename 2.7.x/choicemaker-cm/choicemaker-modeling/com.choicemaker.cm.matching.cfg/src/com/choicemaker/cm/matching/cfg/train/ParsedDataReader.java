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

import java.io.*;
import java.util.*;

import java.text.ParseException;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import com.choicemaker.cm.core.xmlconf.XmlParserFactory;
import com.choicemaker.cm.matching.cfg.*;

/**
 * 
 * @author Adam Winkel
 */
public class ParsedDataReader extends DefaultHandler implements Runnable {
	
	private static String READER = "org.apache.xerces.parsers.SAXParser";
	
	// Parsing variables
	
	private Reader reader;
	private boolean mayHaveMore;
	private boolean readMore;
	
	// For reading ParseTrees
	
	private SymbolFactory symbolFactory;
	private ContextFreeGrammar grammar;

	// Vars for serving parsed data to the caller
	
	private LinkedList queue;	
	private final int MAX_QUEUE = 1000;

	private String[] rawData;
	private List[] tokenizations;
	private ParseTreeNode[] parseTrees;
	private ParsedData[] parsedData;

	//
	// Constructors
	//
	
	public ParsedDataReader(String fn, SymbolFactory sf, ContextFreeGrammar cfg) throws FileNotFoundException {
		this(new FileInputStream(fn), sf, cfg);	
	}
	
	public ParsedDataReader(File f, SymbolFactory sf, ContextFreeGrammar cfg) throws FileNotFoundException {
		this(new FileInputStream(f), sf, cfg);	
	}
	
	public ParsedDataReader(InputStream is, SymbolFactory sf, ContextFreeGrammar cfg) {
		this(new InputStreamReader(is), sf, cfg);
	}
	
	public ParsedDataReader(Reader rdr, SymbolFactory sf, ContextFreeGrammar cfg) {
		reader = rdr;

		symbolFactory = sf;
		grammar = cfg;
		
		queue = new LinkedList();
		depth = 0;
		mayHaveMore = true;
		readMore = true;
		
		Thread t = new Thread(this);
		t.start();
	}
	
	public void run() {
		try {
			XMLReader xmlReader = XmlParserFactory.createXMLReader(READER);
			xmlReader.setContentHandler(this);
			xmlReader.parse(new InputSource(new BufferedReader(reader)));
		} catch (SAXException ex) {
			ex.printStackTrace();
			ex.getMessage();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (Exception e) { }
		}
		synchronized (this) {
			mayHaveMore = false;
			notifyAll();
		}
	}
	
	public synchronized void close() {
		mayHaveMore = false;
		readMore = false;
		notifyAll(); // make sure that thread ends
	}
	
	public synchronized boolean next() throws IOException {
		try {
			while (queue.size() == 0 && mayHaveMore) {
				wait();
			}
			
			DatumHolder serving = null;
			if (!queue.isEmpty())
				serving = (DatumHolder) queue.removeFirst();
					
			return explodeDatumHolder(serving);
		} catch (InterruptedException ex) {
			readMore = false;
			return false;
		}
	}
	
	private boolean explodeDatumHolder(DatumHolder d) {
		if (d == null) {
			rawData = null;
			tokenizations = new List[0];
			parseTrees = new ParseTreeNode[0];
			parsedData = new ParsedData[0];
			return false;	
		} else {
			// raw data
			rawData = d.rawData;
			
			// tokenizations
			tokenizations = new List[d.tokenizations.size()];
			for (int i = 0; i < tokenizations.length; i++)
				tokenizations[i] = (List) d.tokenizations.get(i);
			
			// parse trees
			parseTrees = new ParseTreeNode[d.parseTrees.size()];
			for (int i = 0; i < parseTrees.length; i++) {
				parseTrees[i] = (ParseTreeNode) d.parseTrees.get(i);
			}
			
			// parsed data
			parsedData = new ParsedData[d.parsedData.size()];
			for (int i = 0; i < parsedData.length; i++)
				parsedData[i] = (ParsedData) d.parsedData.get(i);	
			
			return true;	
		}
	}
	
	//
	// Accessors
	//
	
	public synchronized boolean hasRawData() {
		return rawData != null;
	}
	
	public synchronized String[] getRawData() {
		return rawData;
	}
	
	public synchronized int getNumTokenizations() {
		return tokenizations.length;
	}
	
	public synchronized List getTokenization(int i) {
		return tokenizations[i];
	}
	
	public synchronized List[] getTokenizations() {
		return tokenizations;
	}
	
	public synchronized int getNumParseTrees() {
		return parseTrees.length;
	}
	
	/**
	 * Returns the first parse tree, or null if there are none.
	 */
	public synchronized ParseTreeNode getParseTree() {
		if (parseTrees.length > 0) {
			return parseTrees[0];	
		} else {
			return null;	
		}
	}
	
	public synchronized ParseTreeNode getParseTree(int i) {
		return parseTrees[i];
	}
	
	public synchronized ParseTreeNode[] getParseTrees() {
		return parseTrees;
	}
	
	public synchronized int getNumParsedData() {
		return parsedData.length;
	}
	
	public synchronized ParsedData getParsedData() {
		if (parsedData.length > 0) {
			return parsedData[0];
		} else {
			return null;	
		}
	}
	
	public synchronized ParsedData getParsedData(int i) {
		return parsedData[i];
	}
	
	public synchronized ParsedData[] getAllParsedData() {
		return parsedData;
	}
	
	//
	// Sax Handler code
	//

	// depth 1
	private static final String DATA = "data";
	
	// depth 2
	private static final String DATUM = "datum";
	
	// depth 3
	private static final String RAW_DATUM = "raw";
	private static final String TOKENIZATION = "tokenization";
	private static final String PARSE_TREE = "parseTree";
	private static final String PARSED_DATUM = "parsedDatum";

	// depth 4
	private static final String FIELD = "field";
	private static final String TOKEN = "token";
	
	// attributes of field (at depth 4)
	private static final String NAME = "name";

	// Instance vars.

	private DatumHolder building;
	private int depth;
	
	private String level3;
	private String level4;
	
	private String fieldName;
	private String fieldValue;

	private List rawDatum;
	private List tokens;
	private String parseTreeText;
	private ParsedData parsedDatum;

	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		qName = qName.intern();
		depth++;
		
		if (depth == 1) {
			if (qName != DATA) {
				throw new SAXException("Depth 1 element is not '" + DATA + "': " + qName);	
			}
		} else if (depth == 2) {
			if (qName != DATUM) {
				throw new SAXException("Depth 2 element is not '" + DATUM + "': " + qName);
			}
			building = new DatumHolder();
		} else if (depth == 3) {
			if (qName == RAW_DATUM) {
				if (building.rawData != null) {
					throw new SAXException("A " + DATUM + " element can have at most one " + RAW_DATUM + " child.");	
				}
				rawDatum = new ArrayList();
			} else if (qName == TOKENIZATION) {
				tokens = new ArrayList();
			} else if (qName == PARSE_TREE) {
				parseTreeText = "";
			} else if (qName == PARSED_DATUM) {
				parsedDatum = new ParsedData();
			} else {
				throw new SAXException("Unknown depth 3 element: " + qName);
			}
			
			level3 = qName;
		} else if (depth == 4) {
			if (level3 == RAW_DATUM) {
				if (qName == FIELD) {
					fieldValue = "";
				} else {
					throw new SAXException("Unknown depth 4 element: " + qName);					
				}
			} else if (level3 == TOKENIZATION) {
				if (qName == TOKEN) {
					// DO NOTHING
				} else {
					throw new SAXException("Unknown depth 4 element: " + qName);
				}
			} else if (level3 == PARSED_DATUM) {
				if (qName == FIELD) {
					fieldName = atts.getValue(NAME);
					if (fieldName == null || fieldName.trim().length() == 0) {
						throw new SAXException("field.name must be non-empty");
					}
					fieldValue = "";
				} else {
					throw new SAXException("Unknown depth 4 element: " + qName);					
				}				
			} else {
				throw new SAXException("Unknown depth 4 element: " + qName);
			}
			
			level4 = qName;
		} else {
			throw new SAXException("Too deep: " + depth + ", element: " + qName);
		}
		
	}
	
	public void characters(char[] ch, int start, int length) throws SAXException {
				
		if (depth < 3) {
			ensureWhitespace(ch, start, length);
		} else if (depth == 3) {
			if (level3 == PARSE_TREE) {
				parseTreeText += new String(ch, start, length);
			} else {
				ensureWhitespace(ch, start, length);
			}			
		} else if (depth == 4) {  // field values only.
			if (level4 == FIELD) {
				fieldValue += new String(ch, start, length);
			} else if (level4 == TOKEN) {
				String tokenText = new String(ch, start, length);
				tokens.add(tokenText);
			} else {
				ensureWhitespace(ch, start, length);
			}
		}
	}
	
	public void endElement(String uri, String localName, String qName) throws SAXException {
		qName = qName.intern();
				
		if (depth == 1) {
			// DO NOTHING
		} else if (depth == 2) {
			if (building.hasSomething()) {
				addDatumHolder();
			} else {
				throw new SAXException("Empty " + DATUM);	
			}
			building = null;
		} else if (depth == 3) {

			// TODO: check that the parsed datum
			// has at least one field.
			
			if (qName == RAW_DATUM) {
				if (rawDatum.size() == 0) {
					throw new SAXException("Empty " + RAW_DATUM);
				}

				// add the raw strings
				building.rawData = new String[rawDatum.size()];
				for (int i = 0; i < building.rawData.length; i++) {
					building.rawData[i] = (String) rawDatum.get(i);	
				}

				rawDatum = null;
			} else if (qName == TOKENIZATION) {
				if (tokens.size() == 0) {
					throw new SAXException("Empty " + TOKENIZATION);	
				}

				// add the tokenization
				building.tokenizations.add(tokens);
				tokens = null;
			} else if (qName == PARSE_TREE) {
				try {
					ParseTreeNode ptn = 
						ParseTreeNode.parseFromString(parseTreeText, symbolFactory, grammar);
					building.parseTrees.add(ptn);
				} catch (ParseException ex) {
					System.err.println(parseTreeText);
					throw new SAXException(ex);	
				}
				parseTreeText = null;
			} else if (qName == PARSED_DATUM) {
				if (parsedDatum.size() == 0) {
					throw new SAXException("Empty " + PARSED_DATUM);
				}
				
				// add the parsedDatum
				building.parsedData.add(parsedDatum);
				parsedDatum = null;
			}

			level3 = null;
		} else if (depth == 4) {
			if (level3 == RAW_DATUM) {
				rawDatum.add(fieldValue);
				fieldValue = null;
			} else if (level3 == TOKENIZATION) {
				// DO NOTHING
			} else if (level3 == PARSED_DATUM) {
				parsedDatum.put(fieldName, fieldValue);
				fieldName = null;
				fieldValue = null;
			}
			
			level4 = null;
		} else {
			throw new SAXException("Too deep: " + depth);	
		}

		depth--;
	}

	public void ensureWhitespace(char[] ch, int start, int length) throws SAXException {
		for (int i = start + length - 1; i >= start; i--) {
			if (!Character.isWhitespace(ch[i])) {
				throw new SAXException("Non whitespace in illegal place: " + new String(ch, start, length));	
			}	
		}	
	}

	public synchronized void addDatumHolder() throws SAXException {
		try {
			while (queue.size() == MAX_QUEUE && readMore) {
				wait();
			}
			if (readMore) {
				queue.addLast(building);
				building = null;
				notifyAll();
			} else {
				notifyAll();
				throw new SAXException("");
			}
		} catch (InterruptedException ex) {
			// DO NOTHING
		}
	}
	
	//
	// Inner class
	//
		
	private class DatumHolder {
		String[] rawData = null;
		List tokenizations = new ArrayList();
		List parseTrees = new ArrayList();
		List parsedData = new ArrayList();
		
		public boolean hasSomething() {
			return rawData != null || tokenizations.size() > 0 ||
				parseTrees.size() > 0 || parsedData.size() > 0;
		}
	}
	
}
