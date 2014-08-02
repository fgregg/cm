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
package com.choicemaker.cm.matching.en.us.address;

import com.choicemaker.cm.matching.cfg.ParseTreeNode;
import com.choicemaker.cm.matching.cfg.ParsedData;
import com.choicemaker.cm.matching.cfg.SymbolFactory;
import com.choicemaker.cm.matching.cfg.Token;
import com.choicemaker.cm.matching.cfg.TokenType;
import com.choicemaker.cm.matching.cfg.standardizer.DefaultStandardizer;
import com.choicemaker.cm.matching.cfg.standardizer.RecursiveStandardizer;
import com.choicemaker.cm.matching.cfg.standardizer.TokenTypeStandardizer;
import com.choicemaker.cm.matching.cfg.tokentype.OrdinalTokenType;
import com.choicemaker.cm.matching.en.us.ParsedAddress;
import com.choicemaker.util.StringUtils;

/**
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:02 $
 */
public class AddressStandardizer extends RecursiveStandardizer {

	private static final String TT_WD = "WD";
	private static final String TT_NUM = "NUM";
	private static final String TT_ORD = "ORD";
	private static final String TT_CARD = "CARD";

	/**
	 * Creates a new AddressStandardizer, retreiving Variables from
	 * the specified SymbolFactory.
	 */
	public AddressStandardizer(SymbolFactory factory) {

		putStandardizer(factory.getVariable("HN"),
							new HouseNumberNodeStandardizer(ParsedAddress.HOUSE_NUMBER));

		putStandardizer(factory.getVariable("DIR"),
							new DirectionNodeStandardizer());

		putStandardizer(factory.getVariable("SN"),
							new StreetNameNodeStandardizer(ParsedAddress.STREET_NAME));

		putStandardizer(factory.getVariable("SS"),
							new TokenTypeStandardizer(ParsedAddress.STREET_SUFFIX));

		putStandardizer(factory.getVariable("AT"),
							new TokenTypeStandardizer(ParsedAddress.APARTMENT_TYPE));

		putStandardizer(factory.getVariable("APT"),
							new DefaultStandardizer(ParsedAddress.APARTMENT_NUMBER, DefaultStandardizer.NONE));

		putStandardizer(factory.getVariable("V_POBNUM"),
							new DefaultStandardizer(ParsedAddress.PO_BOX, DefaultStandardizer.NONE));

		putStandardizer(factory.getVariable("V_RRNUM"),
							new DefaultStandardizer(ParsedAddress.RR_NUM, DefaultStandardizer.NONE));

		putStandardizer(factory.getVariable("V_RRBOX"),
							new DefaultStandardizer(ParsedAddress.RR_BOX, DefaultStandardizer.NONE));

		putStandardizer(factory.getVariable("CO_ADDR"),
							new CareOfStandardizer(ParsedAddress.ATTENTION));

	}

	/**
	 * DefaultStandardizer that removes everything but digits and letters from
	 * its node's joined Tokens.
	 */
	private static class HouseNumberNodeStandardizer extends DefaultStandardizer {
		public HouseNumberNodeStandardizer(String fieldName) {
			super(fieldName);
		}

		public void standardize(ParseTreeNode node, ParsedData addr) {
			String value = StringUtils.removeNonDigitsLetters(joinTokens(node));
			addr.put(fieldName, value);
		}
	}

	/**
	 * DefaultStandardizer that puts a direction either in
	 * PRE_DIRECTION, or POST_DIRECTION, depending on whether or not
	 * STREET_NAME is NULL.
	 */
	private static class DirectionNodeStandardizer extends DefaultStandardizer {
		public DirectionNodeStandardizer() {
			super(null);
		}

		public void standardize(ParseTreeNode node, ParsedData addr) {
			TokenType type = (TokenType)node.getRule().getLhs();
			Token tok = (Token)node.getRule().getRhsSymbol(0);
			String value = type.getStandardToken(tok);

			if (addr.has(ParsedAddress.STREET_NAME)) {
				addr.put(ParsedAddress.POST_DIRECTION, value);
			} else {
				addr.put(ParsedAddress.PRE_DIRECTION, value);
			}
		}
	}

	/**
	 * Able to handle
	 * 	- totally numeric street names
	 *  - totally word street names
	 *  - mixed, like brooklyn, e.g. "BEACH 54TH"
	 *
	 * The strategy:
	 * 		- a word is a word.
	 * 		- ordinals, numbers, and cardinals, all get transformed to
	 * 			number + ordinal extension
	 * 		- ignore everything else.
	 */
	private static class StreetNameNodeStandardizer extends DefaultStandardizer {

		public StreetNameNodeStandardizer(String fieldName) {
			super(fieldName);
		}

		public void standardize(ParseTreeNode node, ParsedData addr) {
			String sn = joinStreetTokens(node);
			addr.put(fieldName, sn);
		}

		private String joinStreetTokens(ParseTreeNode node) {
			String sn = null;

			int numKids = node.getNumChildren();
			for (int i = 0; i < numKids; i++) {
				ParseTreeNode child = node.getChild(i);

				TokenType type = (TokenType) child.getRule().getLhs();
				String typeName = type.toString();
				Token tok = (Token)child.getRule().getRhsSymbol(0);
				String value = null;

				if (TT_WD.equals(typeName)) {
					value = type.getStandardToken(tok);
				} else if (TT_ORD.equals(typeName)) {
					value = type.getStandardToken(tok);
				} else if (TT_NUM.equals(typeName) || TT_CARD.equals(typeName)) {
					value = type.getStandardToken(tok);
					value = OrdinalTokenType.numberToOrdinal(value);
				} // ignore everything else...

				if (value != null) {
					if (sn == null) {
						sn = value;
					} else {
						sn += " " + value;
					}
				}
			}

			return sn;
		}
	}

	/**
	 * Note: the only
	 */
	private static class CareOfStandardizer extends DefaultStandardizer {
		public CareOfStandardizer(String fieldName) {
			super(fieldName);
		}

		public void standardize(ParseTreeNode node, ParsedData addr) {
			String co = joinCoTokens(node);
			addr.put(fieldName, co);
		}

		private String joinCoTokens(ParseTreeNode node) {
			int numKids = node.getNumChildren();
			String[] words = new String[numKids - 1];
			for (int i = 1; i < numKids; i++) {
				ParseTreeNode child = node.getChild(i);

				TokenType type = (TokenType) child.getRule().getLhs();
				// 2014-04-24 rphall: Commented out unused local variable.
//				String typeName = type.toString();
				Token tok = (Token)child.getRule().getRhsSymbol(0);
				String value = type.getStandardToken(tok);
				words[i-1] = value;
			}

			if (words.length == 1 || words[1] == null) {
				return words[0];
			} else if (words[1].length() == 1) {
				return words[1] + " " + words[0];
			} else {
				return words[0] + " " + words[1];
			}
		}
	}

}
