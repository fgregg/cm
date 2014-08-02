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

import com.choicemaker.cm.matching.cfg.Parser;

/**
 * .
 * 
 * @author Adam Winkel
 */
public class ParsedDataCreator {

	public static final int NONE = -1;
	public static final int BEST = -2;
	public static final int ALL = -3;

	protected RawDataReader rawDataReader;
	protected Parser parser;
	protected ParsedDataWriter parsedDataWriter;

	protected int rawDataPolicy;
	protected int tokenizationPolicy;
	protected int parseTreePolicy;
	protected int parsedDataPolicy;

	public ParsedDataCreator(RawDataReader reader, Parser parser, ParsedDataWriter writer) {
		this(reader, parser, writer, ALL, NONE, ALL, NONE);
	}
	
	public ParsedDataCreator(RawDataReader reader, Parser parser, ParsedDataWriter writer,
			int raw, int tokenizations, int trees, int parsed) {
		this.rawDataReader = reader;
		this.parser = parser;
		this.parsedDataWriter = writer;
		
		setRawDataPolicy(raw);
		setTokenizationPolicy(tokenizations);
		setParseTreePolicy(trees);
		setParsedDataPolicy(parsed);
	}
	
	public void createData() throws IOException {
		parsedDataWriter.clear();
		
		while (rawDataReader.hasNext()) {
			String[] raw = rawDataReader.next();
			handleRawData(raw);
		}
		
		rawDataReader.close();
		parsedDataWriter.close();
	}
	
	private void handleRawData(String[] raw) throws IOException {
		parsedDataWriter.clear();
		
		// raw data
		if (rawDataPolicy != NONE) {
			parsedDataWriter.setRawData(raw);
		}
		
		// tokenizations
		if (tokenizationPolicy != NONE) {
			parsedDataWriter.addAllTokenizations( parser.getAllTokenizations(raw) );
		}
		
		// parse trees
		if (parseTreePolicy == BEST) {
			parsedDataWriter.addParseTree( parser.getBestParseTree(raw) );
		} else if (parseTreePolicy == ALL) {
			parsedDataWriter.addAllParseTrees( parser.getAllParseTrees(raw) );
		}

		// parsed data
		if (parsedDataPolicy == BEST) {
			parsedDataWriter.addParsedData( parser.getBestParse(raw) );	
		} else if (parsedDataPolicy == ALL) {
			parsedDataWriter.addAllParsedData( parser.getAllParses(raw) );
		}

		parsedDataWriter.write();
	}
	
	public void setRawDataPolicy(int p) {
		rawDataPolicy = p;
	}

	public void setTokenizationPolicy(int p) {
		tokenizationPolicy = p;
	}

	public void setParseTreePolicy(int p) {
		parseTreePolicy = p;
	}

	public void setParsedDataPolicy(int p) {
		parsedDataPolicy = p;	
	}

}
