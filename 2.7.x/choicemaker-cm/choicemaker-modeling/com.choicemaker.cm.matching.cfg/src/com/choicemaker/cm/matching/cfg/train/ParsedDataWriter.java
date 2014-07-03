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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.choicemaker.cm.core.util.XmlWriter;
import com.choicemaker.cm.matching.cfg.ParseTreeNode;
import com.choicemaker.cm.matching.cfg.ParsedData;

/**
 * .
 * 
 * @author Adam Winkel
 */
public class ParsedDataWriter extends XmlWriter {

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
//	private static final String EMPTY_FIELD = "emptyField";
	private static final String TOKEN = "token";
	
	// attributes of field (at depth 4)
	private static final String NAME = "name";


	private String[] rawData;
	private List tokenizations = new ArrayList();
	private List parseTrees = new ArrayList();
	private List parsedData = new ArrayList();

	public ParsedDataWriter(String f) throws IOException {
		this(new FileWriter(f));
	}

	public ParsedDataWriter(File f) throws IOException {
		this(new FileWriter(f));
	}

	public ParsedDataWriter(OutputStream os) throws IOException {
		this(new OutputStreamWriter(os));	
	}

	public ParsedDataWriter(Writer w) throws IOException {
		super(new BufferedWriter(w));
		beginElement(DATA);
	}

	public void writeAndClear() throws IOException {
		write();
		clear();
	}

	public void write() throws IOException {
		beginElement(DATUM);
		
		// the raw datum
		if (rawData != null) {
			beginElement(RAW_DATUM);
			for (int i = 0; i < rawData.length; i++) {
				beginElement(FIELD);
				text(rawData[i]);
				endElement();	
			}
			endElement();
		}
		
		// the tokenization(s)
		for (int i = 0; i < tokenizations.size(); i++) {
			List tokens = (List) tokenizations.get(i);
			beginElement(TOKENIZATION);
			for (int j = 0; j < tokens.size(); j++) {
				beginElement(TOKEN);
				text(tokens.get(j).toString());
				endElement();	
			}
			endElement();	
		}

		// the parse tree(s)
		for (int i = 0; i < parseTrees.size(); i++) {
			ParseTreeNode ptn = (ParseTreeNode) parseTrees.get(i);
			beginElement(PARSE_TREE);
			text("\n" + ptn.prettyPrint());
			endElement();
		}
		
		// the parsed datum/a
		for (int i = 0; i < parsedData.size(); i++) {
			ParsedData pd = (ParsedData) parsedData.get(i);
			beginElement(PARSED_DATUM);
			Iterator it = pd.keySet().iterator();
			while (it.hasNext()) {
				String name = (String) it.next();
				String value = pd.get(name);
				beginElement(FIELD);
				attribute(NAME, name);
				text(value);
				endElement();
			}
			endElement();
		}
		
		endElement();
	}

	public void close() throws IOException {
		endElement();
		super.close();
	}

	public void clear() {
		rawData = null;
		tokenizations.clear();
		parseTrees.clear();
		parsedData.clear();
	}
	
	//
	// Setting the data
	//
	
	public void setRawData(String s) {
		rawData = new String[] {s};	
	}
	
	public void setRawData(String[] s) {
		rawData = new String[s.length];
		System.arraycopy(s, 0, rawData, 0, s.length);	
	}
		
	public void addTokenization(List toks) {
		tokenizations.add(toks);
	}
	
	public void addAllTokenizations(List toks) {
		tokenizations.addAll(toks);	
	}

	public void addAllTokenizations(List[] toks) {
		for (int i = 0; i < toks.length; i++)
			tokenizations.add(toks[i]);
	}

	public void addParseTree(ParseTreeNode ptn) {
		parseTrees.add(ptn);	
	}
	
	public void addAllParseTrees(List ptns) {
		parseTrees.addAll(ptns);	
	}

	public void addAllParseTrees(ParseTreeNode[] ptns) {
		for (int i = 0; i < ptns.length; i++)
			parseTrees.add(ptns[i]);	
	}
	
	public void addParsedData(ParsedData pd) {
		parsedData.add(pd);	
	}
	
	public void addAllParsedData(List pd) {
		parsedData.addAll(pd);	
	}
	
	public void addAllParsedData(ParsedData[] pd) {
		for (int i = 0; i < pd.length; i++)
			parsedData.add(pd[i]);	
	}

	//
	// Additional needed writing methods
	//

	/**
	 * Surrounds the argument String with the CDATA delimieters
	 * and writes it as text
	 */
	protected void cdata(String cdata) throws IOException {
		text(CDATA_START + cdata + CDATA_END);
	}

	private static final String CDATA_START = "";
	private static final String CDATA_END = "";

}
