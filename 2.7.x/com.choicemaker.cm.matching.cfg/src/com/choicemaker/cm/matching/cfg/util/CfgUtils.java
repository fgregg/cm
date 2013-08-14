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
package com.choicemaker.cm.matching.cfg.util;

import java.util.*;

import com.choicemaker.cm.matching.cfg.*;

/**
 * .
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:01 $
 */
public final class CfgUtils {

	/**
	 * Sorts parse trees (the root nodes of parse trees, to be precise)
	 * in decreasing order by probability.
	 * 
	 * @param parseTrees a list of ParseTreeNodes
	 * @throws ClassCastException if an element of the list is not an
	 * instance of ParseTreeNode
	 */
	public static void sortParseTrees(List parseTrees) {
		Collections.sort(parseTrees, REVERSE_PARSE_TREE_COMPARATOR);
	}
	
	public static void sortParseTrees(ParseTreeNode[] parseTrees) {
		Arrays.sort(parseTrees, REVERSE_PARSE_TREE_COMPARATOR);	
	}
	
	/**
	 * Sorts ParsedAddresses in decreasing order by probability.
	 * 
	 * @param parsedDataHolders the ParsedDataHolders to sort
	 * @throws ClassCastException if an element of the list is not an
	 * instance of ParsedDataHolder
	 */
	public static void sortParsedDataHolders(List parsedDataHolders) {
		Collections.sort(parsedDataHolders, REVERSE_PARSED_DATA_HOLDER_COMPARATOR);	
	}

	public static void sortParsedDataHolders(ParsedData[] parsedData) {
		Arrays.sort(parsedData, REVERSE_PARSED_DATA_HOLDER_COMPARATOR);
	}

	/**
	 * Comparator to sort ParseTreeNodes in decreasing order by probability.
	 */	
	private static class ReverseParseTreeNodeComparator implements Comparator {
		public int compare(Object obj1, Object obj2) {
			double p1 = ((ParseTreeNode)obj1).getProbability();
			double p2 = ((ParseTreeNode)obj2).getProbability();	
			return p1 < p2 ? 1 : p1 > p2 ? -1 : 0;
		}		
	}
	
	/**
	 * Comparator to sort ParsedAddresses in decreasing order by probability.
	 */
	private static class ReverseParsedDataHolderComparator implements Comparator {
		public int compare(Object obj1, Object obj2) {
			double p1 = ((ParsedData)obj1).getProbability();
			double p2 = ((ParsedData)obj2).getProbability();
			return p1 < p2 ? 1 : p1 > p2 ? -1 : 0;
		}	
	}
	
	private static final Comparator REVERSE_PARSE_TREE_COMPARATOR =
		new ReverseParseTreeNodeComparator();
	
	private static final Comparator REVERSE_PARSED_DATA_HOLDER_COMPARATOR = 
		new ReverseParsedDataHolderComparator();

}
