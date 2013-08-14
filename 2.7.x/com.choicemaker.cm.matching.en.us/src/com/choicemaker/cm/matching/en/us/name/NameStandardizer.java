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
package com.choicemaker.cm.matching.en.us.name;

import com.choicemaker.cm.matching.cfg.ParseTreeNode;
import com.choicemaker.cm.matching.cfg.ParseTreeNodeStandardizer;
import com.choicemaker.cm.matching.cfg.ParsedData;
import com.choicemaker.cm.matching.cfg.SymbolFactory;
import com.choicemaker.cm.matching.cfg.standardizer.DefaultStandardizer;
import com.choicemaker.cm.matching.cfg.standardizer.RecursiveStandardizer;
import com.choicemaker.cm.matching.cfg.standardizer.TokenTypeStandardizer;
import com.choicemaker.cm.matching.en.us.ParsedName;
import com.choicemaker.cm.matching.gen.Sets;

/**
 * This class is horribly complicated.  AddressStandardizer is a simpler
 * example.  
 * 
 * NameStandardizer is a RecursiveStandardizer, and registers other
 * RecursiveStandardizers, and so on...
 * 
 * TODO: detail the workings on the class.
 * 
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:02 $
 * @see com.choicemaker.cm.matching.cfg.standardizer.RecursiveStandardizer
 */
public class NameStandardizer extends RecursiveStandardizer {

	/**
	 * Creates a new NameStandardizer.  Variables are retrieved from
	 * the specified SymbolFactory.
	 */
	public NameStandardizer(SymbolFactory factory) {

		ParseTreeNodeStandardizer wdpfx, wdsfx;
		wdpfx = new TokenTypeStandardizer(ParsedName.NAME_PREFIX);
		wdsfx = new TokenTypeStandardizer(ParsedName.NAME_SUFFIX);
		
		//	
		// The standardizer for FAMN (first and middle names)
		//
		RecursiveStandardizer famn = new RecursiveStandardizer();		
		RecursiveStandardizer fnOnly = new RecursiveStandardizer();
		
		RecursiveStandardizer fn = new RecursiveStandardizer();
		fn.putStandardizer(factory.getVariable("GN1"),
						   new DefaultStandardizer(ParsedName.FIRST_NAME, DefaultStandardizer.NONE));
		fn.putStandardizer(factory.getVariable("WDPFX"), wdpfx);
		fn.putStandardizer(factory.getVariable("WDSFX"), wdsfx);
		famn.putStandardizer(factory.getVariable("FN"), fn);
		fnOnly.putStandardizer(factory.getVariable("FN"), fn);
		
		RecursiveStandardizer mn = new RecursiveStandardizer();
		mn.putStandardizer(factory.getVariable("GN1"),
						   new DefaultStandardizer(ParsedName.MIDDLE_NAME, DefaultStandardizer.NONE));
		mn.putStandardizer(factory.getVariable("GN2"),
						   new DefaultStandardizer(ParsedName.MIDDLE_NAME, DefaultStandardizer.LIKE_ONLY));
		famn.putStandardizer(factory.getVariable("MN"), mn);

		BracketedStandardizer wdfnnick = new BracketedStandardizer(ParsedName.NICKNAME);
		famn.putStandardizer(factory.getVariable("WDFNQUOT"), wdfnnick);
		famn.putStandardizer(factory.getVariable("WDFNPAREN"), wdfnnick);
		
		famn.putStandardizer(factory.getVariable("WDLNPAREN"),
							 new BracketedStandardizer(ParsedName.MAIDEN_NAME));
		
		//
		// The standardizer for LN (last name)
		//
		RecursiveStandardizer ln = new RecursiveStandardizer();							 

		ln.putStandardizer(factory.getVariable("WDSFX"), wdsfx);
		
		DefaultStandardizer noSpace = new DefaultStandardizer(ParsedName.LAST_NAME, DefaultStandardizer.NONE);
		ln.putStandardizer(factory.getVariable("ONELN"), noSpace);
		ln.putStandardizer(factory.getVariable("COLLN"), noSpace);
		
		CompoundLastNameStandardizer cmpndLn = new CompoundLastNameStandardizer();
		ln.putStandardizer(factory.getVariable("HYPHLN"), cmpndLn);
		ln.putStandardizer(factory.getVariable("TWOLN"), cmpndLn);

		//
		// we only add these two standardizers to the top-level
		// standardizer.  They include things like prefixes and suffixes,
		// as well as quoted/bracketed nicknames and maiden names...
		//
		putStandardizer(factory.getVariable("FAMN"), famn);
		putStandardizer(factory.getVariable("FNONLY"), fnOnly);
		putStandardizer(factory.getVariable("MN"), mn);
		putStandardizer(factory.getVariable("LN"), ln);
		
	}

	/**
	 * joinTokens() method is overriden to return the contents of the
	 * parenthesis or quotation marks, or whatever brackets this node.
	 * 
	 * This is potentially useful in other places...  perhaps we should
	 * factor this out???
	 */
	static class BracketedStandardizer extends DefaultStandardizer {
		public BracketedStandardizer(String fieldName) {
			super(fieldName);	
		}
		public BracketedStandardizer(String fieldName, int policy) {
			super(fieldName, policy);
		}
		protected String joinTokens(ParseTreeNode tree) {
			String s = super.joinTokens(tree);
			if (s.length() > 2) {
				return s.substring(1, s.length() - 1);
			} else {
				return null;
			}
		}
	}
	
	/**
	 * Handles both "Garcia Lorca" and "Garcia-Lorca", and assigns
	 * "Garcia" to maiden name and "Lorca" to last name in both cases.
	 */
	static class CompoundLastNameStandardizer extends DefaultStandardizer {
		public CompoundLastNameStandardizer() {
			super(null);	
		}
		public void standardize(ParseTreeNode tree, ParsedData holder) {
			int numKids = tree.getNumChildren();
			if (numKids < 2) {
				throw new IllegalStateException("Internal error in NameStandardizer");	
			}
			String piece1 = joinTokens(tree.getChild(0));
			String piece2 = joinTokens(tree.getChild(numKids-1));
			if (Sets.includes("en.us.lastNameFragments", piece1)) {
				holder.put(ParsedName.LAST_NAME, piece1 + piece2);
			} else {
				holder.put(ParsedName.MAIDEN_NAME, piece1);
				holder.put(ParsedName.LAST_NAME, piece2);
			}
		}
	}

}
