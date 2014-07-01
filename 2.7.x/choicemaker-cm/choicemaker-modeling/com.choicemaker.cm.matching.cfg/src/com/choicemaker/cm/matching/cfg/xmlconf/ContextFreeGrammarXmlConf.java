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
package com.choicemaker.cm.matching.cfg.xmlconf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jdom.Element;

import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.util.NamedResources;
import com.choicemaker.cm.matching.cfg.ContextFreeGrammar;
import com.choicemaker.cm.matching.cfg.Rule;
import com.choicemaker.cm.matching.cfg.SymbolFactory;
import com.choicemaker.cm.matching.cfg.TokenType;
import com.choicemaker.cm.matching.cfg.Variable;
import com.choicemaker.cm.matching.cfg.tokentype.InlineTokenType;
import com.choicemaker.util.StringUtils;

/**
 * @author ajwinkel
 *
 */
public class ContextFreeGrammarXmlConf {

	public static final String ARROW = "-->";
	public static final String TT_DEF = ":=";
	public static final String COMMENT = "//";

	public static final char PIPE = '|';
	public static final char LEFT_BRACKET = '[';
	public static final char RIGHT_BRACKET = ']';

	//
	// CFG
	//

	public static ContextFreeGrammar readFromFile(String filename, SymbolFactory factory) throws IOException, ParseException {
		return readFromFile(filename, factory, false);
	}

	public static ContextFreeGrammar readFromFile(String filename, SymbolFactory factory, boolean strict) throws IOException, ParseException {
		FileInputStream fis = new FileInputStream(new File(filename).getAbsoluteFile());
		ContextFreeGrammar g = readFromStream(fis, factory, strict);
		fis.close();

		return g;
	}

	public static ContextFreeGrammar readFromStream(InputStream is, SymbolFactory factory) throws ParseException, IOException {
		return readFromStream(is, factory, false);
	}

	/**
	 * The strings "//", "-->", and ":=" are special, as are the characters '|', '[', and ']'.
	 * Thus, inline token types that use the special strings or characters must be defined in a symbol factory.
	 * Also, whitespace is not significant in the grammar.
	 */
	public static ContextFreeGrammar readFromStream(InputStream stream, SymbolFactory factory, boolean strict) throws IOException, ParseException {

		// Create more manageable input
		InputStreamReader rdr = new InputStreamReader(stream);
		BufferedReader reader = new BufferedReader(rdr);

		List rules = new ArrayList();

		int lineNum = 0;
		while (reader.ready()) {
			String line = reader.readLine();
			lineNum++;

			// ignore blank and comment-only lines.  Strip the comments from
			// ends of lines.
			int index = line.indexOf(COMMENT);
			if (index >= 0)
				line = line.substring(0, index).trim();

			// each non-empty line either defined a rule,
			if (line.length() > 0) {

				int ruleIndex = line.indexOf(ARROW);
				int ttIndex = line.indexOf(TT_DEF);
				if (ruleIndex < 0 && ttIndex < 0) {
					throw new ParseException("Line " + lineNum + " is neither a rule nor a token type definition.\n" + line, lineNum);
				} else if (ruleIndex > 0 && ttIndex > 0) {
					throw new ParseException("Line " + lineNum + " has both rule and token type definitions.\n" + line, lineNum);
				} else if (ruleIndex > 0) {
					List rulesForLine = parseRules(line, factory, strict);
					rules.addAll(rulesForLine);
				} else {  // ttIndex > 0
					// 2014-04-24 rphall: Commented out unused local variable.
					// Any side effects?
					/* TokenType tt = */
					parseInlineTokenType(line, factory, strict);
				}
			}
		}

		// close the input
		rdr.close();
		reader.close();

		if (rules.size() == 0) {
			throw new ParseException("The specified reader (" + rdr.toString() + " is empty or has no rules!", -1);
		}

		Rule startRule = (Rule) rules.get(0);
		Variable startVariable = startRule.getLhs();

		ContextFreeGrammar pcfg = new ContextFreeGrammar(startVariable, rules);

		return pcfg;
	}

	//
	// Fragments
	//

	public static void readFragmentFromFile(String filename, ContextFreeGrammar grammar, SymbolFactory factory) throws IOException, ParseException {
		readFragmentFromFile(filename, grammar, factory, false);
	}

	public static void readFragmentFromFile(String filename, ContextFreeGrammar grammar, SymbolFactory factory, boolean strict) throws IOException, ParseException {
		FileInputStream fis = new FileInputStream(new File(filename).getAbsoluteFile());
		readFragmentFromStream(fis, grammar, factory, strict);
		fis.close();
	}

	public static void readFragmentFromStream(InputStream is, ContextFreeGrammar grammar, SymbolFactory factory) throws ParseException, IOException {
		readFragmentFromStream(is, grammar, factory, false);
	}

	public static void readFragmentFromStream(InputStream stream, ContextFreeGrammar grammar, SymbolFactory factory, boolean strict) throws IOException, ParseException {

		// Create more manageable input
		InputStreamReader rdr = new InputStreamReader(stream);
		BufferedReader reader = new BufferedReader(rdr);

		int numRules = 0;

		int lineNum = 0;
		while (reader.ready()) {
			String line = reader.readLine();
			lineNum++;

			// ignore blank and comment-only lines.  Strip the comments from
			// ends of lines.
			int index = line.indexOf(COMMENT);
			if (index >= 0)
				line = line.substring(0, index).trim();

			// each non-empty line either defined a rule,
			if (line.length() > 0) {

				int ruleIndex = line.indexOf(ARROW);
				int ttIndex = line.indexOf(TT_DEF);
				if (ruleIndex < 0 && ttIndex < 0) {
					throw new ParseException("Line " + lineNum + " is neither a rule nor a token type definition.\n" + line, lineNum);
				} else if (ruleIndex > 0 && ttIndex > 0) {
					throw new ParseException("Line " + lineNum + " has both rule and token type definitions.\n" + line, lineNum);
				} else if (ruleIndex > 0) {
					List rulesForLine = parseRules(line, factory, strict);
					numRules += rulesForLine.size();
					grammar.addAllRules(rulesForLine);
				} else {  // ttIndex > 0
					// 2014-04-24 rphall: Commented out unused local variable.
					// Any side effects?
					/* TokenType tt = */
					parseInlineTokenType(line, factory, strict);
				}
			}
		}

		// close the input
		rdr.close();
		reader.close();

		if (numRules == 0) {
			throw new ParseException("The specified reader (" + rdr.toString() + " is empty or has no rules!", -1);
		}

	}

	//
	// XML Elements
	//

	public static ContextFreeGrammar readFromElement(Element e, SymbolFactory factory, URL pluginUrl) throws XmlConfException {

		// Base Grammar
		ContextFreeGrammar grammar = null;
		try {
			InputStream is = getInputStream(e, pluginUrl);
			grammar = readFromStream(is, factory);
		} catch (IOException ex) {
			throw new XmlConfException("Unable to retrieve grammar from resource", ex);
		} catch (ParseException ex) {
			throw new XmlConfException("Problem parsing grammar", ex);
		}

		// Fragments
		List children = e.getChildren("fragment");
		for (int i = 0; i < children.size(); i++) {
			Element child = (Element)children.get(i);

			try {
				InputStream is = getInputStream(child, pluginUrl);
				readFragmentFromStream(is, grammar, factory);
			} catch (IOException ex) {
				throw new XmlConfException("Unable to read grammar fragment", ex);
			} catch (ParseException ex) {
				throw new XmlConfException("Problem parsing grammar fragment", ex);
			}
		}

		return grammar;

	}

	private static InputStream getInputStream(Element e, URL pluginUrl) throws XmlConfException, IOException {
		String val = e.getAttributeValue("resource");
		if (val != null) {
			InputStream is = NamedResources.getNamedResource(val);
			return is;
		}

		val = e.getAttributeValue("file");
		if (val != null) {
			if (pluginUrl != null) {
				URL rUrl = new URL(pluginUrl, val);
				return rUrl.openStream();
			} else {
				InputStream is = new FileInputStream(new File(val).getAbsoluteFile());
				return is;
			}
		}

		throw new XmlConfException("Element " + e.getName() + " must define either 'resource' or 'file' attribute");
	}

	//
	// Helper methods
	//

	private static List parseRules(String line, SymbolFactory factory, boolean strict) throws ParseException {
		List rules = new ArrayList();

		int arrowIndex = line.indexOf(ARROW);
		if (arrowIndex < 0) {
			throw new ParseException("Arrow (" + ARROW + ") expected, but not found\n" + line, -1);
		}

		String lhsString = line.substring(0, arrowIndex).trim();
		Variable lhs = checkAndGetVariable(lhsString, factory, strict);

		String rhsString = line.substring(arrowIndex + ARROW.length()).trim();
		if (rhsString.length() == 0) {
			throw new ParseException("Empty RHS in rule\n" + line, -1);
		}

		String[] pipeTokens = StringUtils.split(rhsString, PIPE);
		for (int i = 0; i < pipeTokens.length; i++) {
			String pipeToken = pipeTokens[i].trim();

			if (pipeToken.length() == 0) {
				throw new ParseException("Empty RHS in rule\n" + line, -1);
			}

			double probability = 0;

			int openBracket = pipeToken.indexOf(LEFT_BRACKET);
			int closeBracket = pipeToken.indexOf(RIGHT_BRACKET);
			if (openBracket >= 0 && closeBracket > openBracket) {
				try {
					probability = Double.parseDouble(pipeToken.substring(openBracket+1, closeBracket));
					if (probability < 0 || probability > 1) {
						throw new ParseException("Illegal probability: " + probability, -1);
					}
				} catch (NumberFormatException ex) {
					throw new ParseException("Trouble parsing probability: " + ex.getMessage(), -1);
				}

				if (pipeToken.substring(closeBracket + 1).trim().length() > 0) {
					throw new ParseException("Non-space characters after the probability: '" + pipeToken + "'", -1);
				}

				pipeToken = pipeToken.substring(0, openBracket);
			}

			List rhs = new ArrayList();
			String[] spaceTokens = StringUtils.split(pipeToken);
			for (int j = 0; j < spaceTokens.length; j++) {
				String symbolToken = spaceTokens[j];
				rhs.add( checkAndGetVariable(symbolToken, factory, strict) );
			}

			if (rhs.size() == 0) {
				throw new ParseException("Empty RHS in rule\n" + line, -1);
			}

			rules.add(new Rule(lhs, rhs, probability));
		}

		return rules;
	}

	/**
	 * If the variable is in the symbol factory, returns it.
	 * Else if strict is true, throws a ParseException
	 * Else if the name is an illegal variable name, throws a ParseException.
	 * Else, creates a new variable, adds it to the SymbolFactory, and returns the variable.
	 */
	private static Variable checkAndGetVariable(String variable, SymbolFactory factory, boolean strict) throws ParseException {
		if (factory.hasVariable(variable)) {
			return factory.getVariable(variable);
		} else if (strict) {
			throw new ParseException("Variable '" + variable + "' not found in SymbolFactory, and strict flag set.", -1);
		} else if (!isLegalVariableName(variable)) {
			throw new ParseException("'" + variable + "' is not a legal variable name.", -1);
		} else {
			Variable v = new Variable(variable);
			factory.addVariable(v);
			return v;
		}
	}

	/**
	 * Parses things of the form:
	 *
	 * TT_XXXX := A | B | C
	 * TT_YYYY := A [.5] | B [.25] | C [.25]
	 */
	private static TokenType parseInlineTokenType(String line, SymbolFactory factory, boolean strict) throws ParseException {

		int ttDefIndex = line.indexOf(TT_DEF);
		if (ttDefIndex < 0) {
			throw new ParseException("Token type def (:=) expected, but not found\n" + line, -1);
		}

		String lhsString = line.substring(0, ttDefIndex).trim();
		if (!isLegalVariableName(lhsString)) {
			throw new ParseException("'" + lhsString + "' is not a legal token type name.", -1);
		} else if (factory.hasVariable(lhsString)) {
			throw new ParseException("SymbolFactory already has a variable named '" + lhsString + "'.\n" + line, -1);
		}

		String rhsString = line.substring(ttDefIndex + TT_DEF.length()).trim();
		if (rhsString.length() == 0) {
			throw new ParseException("Empty RHS in token type definition\n" + line, -1);
		}

		String[] pipeTokens = StringUtils.split(rhsString, PIPE);
		String[] tokens = new String[pipeTokens.length];
		double[] probabilities = new double[pipeTokens.length];
		Arrays.fill(probabilities, tokens.length > 0 ? 1.0 / tokens.length : 1);

		for (int i = 0; i < pipeTokens.length; i++) {
			String pipeToken = pipeTokens[i].trim();

			int openBracket = pipeToken.indexOf(LEFT_BRACKET);
			int closeBracket = pipeToken.indexOf(RIGHT_BRACKET);
			if (openBracket >= 0 && closeBracket > openBracket) {
				try {
					probabilities[i] = Double.parseDouble(pipeToken.substring(openBracket+1, closeBracket));
					if (probabilities[i] < 0 || probabilities[i] > 1) {
						throw new ParseException("Illegal probability: " + probabilities[i], -1);
					}
				} catch (NumberFormatException ex) {
					throw new ParseException("Trouble parsing probability: " + ex.getMessage(), -1);
				}

				if (pipeToken.substring(closeBracket + 1).trim().length() > 0) {
					throw new ParseException("Non-space characters after the probability: '" + pipeToken + "'", -1);
				}

				pipeToken = pipeToken.substring(0, openBracket).trim();
			}

			if (pipeToken.length() == 0) {
				throw new ParseException("Empty RHS in token type definition\n" + line, -1);
			}

			tokens[i] = pipeToken;
		}

		TokenType tt = new InlineTokenType(lhsString, tokens, probabilities);
		factory.addVariable(tt);

		return tt;
	}

	/**
	 * Legal variable names are any non-zero length combination of letters, digits, and the underscore character.
	 */
	private static boolean isLegalVariableName(String variable) {
		return variable.length() > 0 && !StringUtils.containsNonWordChars(variable);
	}

}
