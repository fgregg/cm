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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * .
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.2 $ $Date: 2010/03/27 21:24:22 $
 */
public class PrefixTree {

	TreeMap map = new TreeMap();

	PrefixTreeNode root;

	public PrefixTree() {
		root = new PrefixTreeNode();	
	}

	public PrefixTree(Collection strings) {
		this();
		addAll(strings);
	}
	
	public PrefixTree(String[] strings) {
		this();
		addAll(strings);	
	}
	
	public void addAll(Collection strings) {
		Iterator it = strings.iterator();	
		while (it.hasNext()) {
			add((String)it.next());
		}
	}
	
	public void addAll(String[] strings) {
		for (int i = 0; i < strings.length; i++) {
			add(strings[i]);	
		}	
	}
	
	public void add(String s) {
		if (s == null || s.length() == 0) {
			throw new IllegalArgumentException("Cannot add null or zero-length String to prefix tree.");
		}
		root.add(s);
	}

	public boolean contains(String s) {
		return root.contains(s);	
	}
	
	public String getLongestPrefix(String s) {
		return root.getLongestPrefix(s);
	}
	
	public String getShortestPrefix(String s) {
		return root.getShortestPrefix(s);
	}

	public List getAllPrefixes(String s) {
		return root.getAllPrefixes(s);
	}

	public static class PrefixTreeNode {
		public static int count = 0;
		public static int kidsArrayCount = 0;
		
		protected char thisChar;
		protected boolean isPrefix;

		Character c;

		protected Map kids;
		
		public PrefixTreeNode() {
			this('\0');
		}

		public PrefixTreeNode(char c) {
			count++;
			thisChar = c;
			isPrefix = false;
		}

		public void add(String s) {
			add(s, 0);	
		}

		protected void add(String s, int index) {
			if (index == s.length()) {
				isPrefix = true;
				return;
			} else if (index >= s.length()) {
				throw new IllegalStateException();	
			}
			
			char next = s.charAt(index);
			Character nextCharacter = new Character(next);
			
			PrefixTreeNode child = null;
			
			if (kids != null) {
				child = (PrefixTreeNode) kids.get(nextCharacter);
			}
			
			if (child == null) {
				child = new PrefixTreeNode(next);
				if (kids == null) {
					kids = new TreeMap();	
				}
				kids.put(nextCharacter, child);
			}
		
			child.add(s, index+1);
		}
		
		public boolean contains(String s) {
			return contains(s, 0);	
		}
		
		protected boolean contains(String s, int index) {
			if (index >= s.length()) {
				return isPrefix ? true : false;
			} else if (kids == null) {
				return false;
			}
			
			char next = s.charAt(index);
			Character nextCharacter = new Character(next);
			
			PrefixTreeNode child = (PrefixTreeNode) kids.get(nextCharacter);
			if (child != null) {
				return child.contains(s, index+1);	
			}
			
			return false;
		}
		
		public String getLongestPrefix(String s) {
			int length = getLongestPrefixLength(s, 0);
			if (length > 0) {
				return s.substring(0, length);
			}
			return null;
		}
		
		protected int getLongestPrefixLength(String s, int index) {
			if (index >= s.length() || kids == null) {
				return isPrefix ? index : -1;
			}
						
			char next = s.charAt(index);
			Character nextCharacter = new Character(next);
						
			PrefixTreeNode child = (PrefixTreeNode) kids.get(nextCharacter);
			if (child != null) {
				int childLongest = child.getLongestPrefixLength(s, index+1);
				if (childLongest > 0) {
					return childLongest;	
				} else if (isPrefix) {
					return index;
				}
			} else if (isPrefix) {
				return index;	
			}
			
			return -1;
		}
		
		public String getShortestPrefix(String s) {
			int length = getShortestPrefixLength(s, 0);
			if (length > 0) {
				return s.substring(0, length);
			}
			return null;
		}
		
		protected int getShortestPrefixLength(String s, int index) {	
			if (isPrefix) {
				return index;	
			} else if (index >= s.length() || kids == null) {
				return -1;
			}
			
			char next = s.charAt(index);
			Character nextCharacter = new Character(next);
			
			PrefixTreeNode child = (PrefixTreeNode) kids.get(nextCharacter);
			if (child != null) {
				return child.getShortestPrefixLength(s, index+1);	
			}
			
			return -1;
		}
		
		public List getAllPrefixes(String s) {
			List list = new ArrayList();
			getAllPrefixes(s, 0, list);
			return list;
		}
		
		protected void getAllPrefixes(String s, int index, List prefixes) {
			if (isPrefix) {
				prefixes.add(s.substring(0, index));	
			}
			
			if (index >= s.length() || kids == null) {
				return;
			}
			
			char next = s.charAt(index);
			Character nextCharacter = new Character(next);
				
			PrefixTreeNode child = (PrefixTreeNode) kids.get(nextCharacter);
			if (child != null) {
				child.getAllPrefixes(s, index+1, prefixes);	
			}
		}
	}

}
