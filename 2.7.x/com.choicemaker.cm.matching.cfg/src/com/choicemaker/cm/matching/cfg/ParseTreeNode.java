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
package com.choicemaker.cm.matching.cfg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * A node in a parse tree for a List of Tokens, parsed according to
 * a grammar.  Usually, parse trees are passed around by their root nodes.
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:59 $
 */
public class ParseTreeNode {

	private Rule rule;
	
	private List children = new ArrayList();
	private int numChildren;
	
	private double probability;

	/**
	 * Creates a new ParseTreeNode with the specified Rule that has no subtrees.
	 * 
	 * @throws IllegalArgumentException if the Rule is not a "leaf rule".
	 */
	public ParseTreeNode(Rule rule) {
		this.rule = rule;
		
		numChildren = 0;
		
		this.probability = rule.getProbability();
		
		if (! (rule.getLhs() instanceof TokenType)) {
			throw new IllegalArgumentException("LHS must be a token type if no children provided.");	
		} else if (! (rule.getRhsSize() == 1 && rule.getRhsSymbol(0) instanceof Token)) {
			throw new IllegalArgumentException("RHS must be a single token if no children provided.");
		}
	}
	
	/**
	 * Creates a new ParseTreeNode with the specified Rule and List of subtrees.
	 */
	public ParseTreeNode(Rule rule, List children) {
		this.rule = rule;
		
		this.children.addAll(children);
		numChildren = children.size();
		
		this.probability = rule.getProbability();
		for (int i = 0; i < children.size(); i++) {
			this.probability *=	((ParseTreeNode)children.get(i)).getProbability();
		}
	}

	/**
	 * Returns true iff this node has no children.  Presumably, this means
	 * that this node's rule is a "leaf rule" of the form
	 * 
	 * TokenType --&gt; Token
	 */
	public boolean isLeaf() {
		return numChildren == 0;
	}
	
	/**
	 * Returns the Rule for this node.
	 */
	public Rule getRule() {
		return rule;
	}
	
	/**
	 * Returns the number of subtrees for this node.
	 */
	public int getNumChildren() {
		return numChildren;
	}
		
	/**
	 * Returns the subtree of this parse tree at the specified index.
	 */
	public ParseTreeNode getChild(int index) {
		return (ParseTreeNode) children.get(index);
	}
	
	/**
	 * Returns a List of the subtrees of this ParseTreeNode, one for each Variable
	 * on the right-hand side of this node's Rule.
	 */
	public List getChildren() {
		return new ArrayList(children);	
	}

	/**
	 * Returns the probability of this parse tree (or subtree).
	 */
	public double getProbability() {
		return probability;	
	}

	/**
	 * Returns a non-pretty-printed String representation of this ParseTreeNode.
	 */
	public String toString() {
		String s  = "";
		
		s += "(";
		s += rule.getLhs();
		
		if (numChildren == 0) {
			for (int i = 0; i < rule.getRhsSize(); i++) {
				s += " ";
				s += rule.getRhsSymbol(i);
			}
		} else {
			for (int i = 0; i < rule.getRhsSize(); i++) {
				s += " ";
				s += getChild(i).toString();
			}	
		}
		
		s += ")";
		
		return s;
	}

	/**
	 * Returns a pretty-printed String representing this ParseTreeNode.
	 */
	public String prettyPrint() {
		return prettyPrint(0, true);  // isFirstChild has no effect, as indent is 0 anyway...
	}
	
	protected String prettyPrint(int indent, boolean isFirstChild) {
		StringBuffer sBuff = new StringBuffer();
		
		if (!isFirstChild) {
			for (int i = 0; i < indent; i++)
				sBuff.append(' ');
		}
		
		sBuff.append('(');
		sBuff.append(getRule().getLhs().toString());
		sBuff.append(' ');
		
		if (isFirstChild) {
			indent += sBuff.length();
		} else {
			indent = sBuff.length();	
		}
		
		if (numChildren == 0) {
			sBuff.append(getRule().getRhsSymbol(0).toString());
		} else {
			sBuff.append(getChild(0).prettyPrint(indent, true));
			for (int i = 1; i < numChildren; i++) {
				sBuff.append('\n');
				sBuff.append(getChild(i).prettyPrint(indent, false));	
			}
		}
		
		sBuff.append(')');
		
		return sBuff.toString();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof ParseTreeNode)) {
			return false;	
		}	
		
		ParseTreeNode node = (ParseTreeNode) obj;
		
		//if (!rule.equals(node.getRule())) {
		//	System.out.print("Different rule:       ");
		//	System.out.print(rule + "       ");
		//	System.out.println(node.rule);
		//}
		
		//System.out.println(rule + "     " + node.rule);
				
		//if (!children.equals(node.getChildren())) {
		//	System.out.println("Different children!");
		//}
				
		return rule.equals(node.getRule()) && children.equals(node.getChildren());
	}

	public int hashCode() {
		return rule.hashCode() + children.hashCode();
	}

	/**
	 * Reads a set of parse trees from the specified file and returns a List containing their roots.
	 */
	public static List readFromFile(String filename, SymbolFactory factory, ContextFreeGrammar cfg) throws IOException, ParseException {
		FileInputStream fis = new FileInputStream(new File(filename).getAbsoluteFile());
		List list = readFromStream(fis, factory, cfg);
		fis.close();
		
		return list;
	}

	/**
	 * Reads a set of parse trees from the specified InputStream and returns a List containing
	 * their roots.
	 */
	public static List readFromStream(InputStream stream, SymbolFactory factory, ContextFreeGrammar cfg) throws IOException, ParseException {
				
		InputStreamReader rdr = new InputStreamReader(stream);
		BufferedReader reader = new BufferedReader(rdr);
		
		String s = "";
		
		String line;
		while ((line = reader.readLine()) != null) {
			int index = line.indexOf("//");
			if (index >= 0)
				line = line.substring(0, index).trim();	
			
			if (line.length() > 0)
				s += line + " ";
		}
		
		rdr.close();
		reader.close();
		
		List list = new ArrayList();
		
		int index = 0;
		int len = s.length();
		while (index < len) {
			char c = s.charAt(index);
			if (c == '(') {
				int start = index;
				int parenCount = 1;
				while (++index < len && parenCount > 0)
					parenCount += s.charAt(index) == ')' ? -1 : s.charAt(index) == '(' ? 1 : 0;
		
				if (parenCount > 0) {
					throw new ParseException("Unmatched parens.", index-1);	
				}

				try {
					String treeString = s.substring(start, index);
					ParseTreeNode root = parseFromString(treeString, factory, cfg);
					list.add(root);
				} catch (ParseException ex) {
					throw new ParseException(ex.getMessage(), start);	
				}
			} else if (c == ')') {
				throw new ParseException("Unmatched parens.", index);
			} else {
				index++;
			}
		}
		
		return list;
	}

	/**
	 * Parses and returns a single parse tree from the argument String.  If the
	 * input String is the concatenation of two or more parse trees, this method
	 * will throw a ParseException.
	 */
	public static ParseTreeNode parseFromString(String s, SymbolFactory factory, ContextFreeGrammar grammar) throws ParseException {
		// trim off whitespace and make sure the String is not empty.
		s = s.trim();
		int len = s.length();
		if (len == 0) {
			throw new ParseException("Empty parseTree!", 0);	
		}

		// Strip off the open and close parens.
		int openIndex = s.indexOf('(');
		if (openIndex != 0) {
			throw new ParseException("ParseTree must begin with an open paren", 0);	
		}

		int closeIndex = s.lastIndexOf(')');
		if (closeIndex != len-1) {
			throw new ParseException("ParseTree must end with a close paren", len-1);
		}
		
		int innerOpenIndex = s.indexOf('(', openIndex+1);
		int innerCloseIndex = s.lastIndexOf(')', closeIndex-1);
		
		if (innerOpenIndex < 0 && innerCloseIndex < 0) { // a terminal rule

			StringTokenizer tokens = new StringTokenizer(s.substring(openIndex+1, closeIndex));
			
			if (tokens.countTokens() < 2) {
				throw new ParseException("Must have token type and token in terminal rule.", -1);	
			}
			
			String lhsString = tokens.nextToken();
			String tokenString = tokens.nextToken();
			while (tokens.hasMoreTokens())
				tokenString += " " + tokens.nextToken();
			
			TokenType lhs = (TokenType) factory.getVariable(lhsString);
			
			Token token = new Token(tokenString);
			
			Rule rule = new Rule(lhs, token);
			return new ParseTreeNode(rule);
			
		} else { // recursively parse nested trees.
	
			// find the beginning of the name of the top-most rule.
			int index = openIndex;
			while (++index < closeIndex && Character.isWhitespace(s.charAt(index)))
				;
			
			int lhsStart = index;
			
			// find the end of the name of the top-most rule.
			while (++index < closeIndex && !Character.isWhitespace(s.charAt(index)))
				;
				
			String lhsString = s.substring(lhsStart, index);
			
			Variable lhs = factory.getVariable(lhsString);
			
			List rhs = new ArrayList();
			List subTrees = new ArrayList();
			
			// now find all matched groups of parens (child rules) and recurse into them.
			while (index < closeIndex) {
				char c = s.charAt(index);
				
				if (c == '(') {
					
					int start = index;
					int parenCount = 1;
					while (++index < closeIndex && parenCount > 0) {
						c = s.charAt(index);
						parenCount += c == ')' ? -1 : c == '(' ? 1 : 0;
					}
					
					ParseTreeNode child = parseFromString(s.substring(start, index), factory, grammar);
					rhs.add(child.getRule().getLhs());
					subTrees.add(child);

				} else if (c == ')') {
					throw new ParseException("Unmatched close paren.", index);
				} else if (Character.isWhitespace(c)) {
					index++;
				} else {
					throw new ParseException("Illegal character encountered: " + c, index);	
				}
			}

			//Rule rule = new Rule(lhs, rhs);
			Rule rule = grammar.getRule(lhs, rhs);
			return new ParseTreeNode(rule, subTrees);		
		}
		
	}

}
