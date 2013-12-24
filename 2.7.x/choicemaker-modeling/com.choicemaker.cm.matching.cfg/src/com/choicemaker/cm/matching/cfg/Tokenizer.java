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

import java.util.List;

/**
 * The Tokenizer interface defines the two basic methods 
 * for tokenizing a input String or Strings.
 * 
 * Tokenization is the first step in name and address
 * parsing.  The task is to split a String into its constituent
 * pieces.
 * 
 * The Tokenizer interface provides two methods for tokenizing
 * an input argument or arguments.  The first takes a single String,
 * as names and addresses are often reported as a single field.
 * The other takes an array of Strings, as names and addresses 
 * are also sometimes entered as multiple fields, for example
 * first, middle, and last names.
 * 
 * In general, <code>tokenize(String[1])</code> and 
 * <code>tokenize(String)</code> should return equivalent Lists,
 * but this needn't be the case.
 * 
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:59 $
 */
public interface Tokenizer {
	
	/**
	 * Returns a List of Token objects, constituting a
	 * tokenization of the input String.
	 * 
	 * @param s the String to split
	 * @return a List of Token objects representing a tokenization
	 * of the input String
	 */
	public List tokenize(String s);
	
	/**
	 * Returns a List of Token objects, constituting a
	 * tokenization of the input String array.
	 * 
	 * Implementing classes may wish to insert delimeters,
	 * e.g. pound signs, between the Tokens corresponding to 
	 * each String in the array.
	 * 
	 * @param strings the String array to split
	 * @return a List of Token objects representing a tokenization
	 * of the input String
	 */
	public List tokenize(String[] strings);
	
}
