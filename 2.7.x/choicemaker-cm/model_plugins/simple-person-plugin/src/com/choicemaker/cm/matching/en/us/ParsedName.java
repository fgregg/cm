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
package com.choicemaker.cm.matching.en.us;

import com.choicemaker.cm.matching.cfg.ParsedData;

/**
 * ParsedName contains keys for common name fields (for use
 * with ParsedDataHolder's accessor methods) as well as
 * some convenience methods.
 *
 * TODO: toString(), getCanonicalName()...
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:02 $
 */
public class ParsedName extends ParsedData {

	private static final long serialVersionUID = 1L;

	/** The key for the first name field. */
	public static String FIRST_NAME = "FirstName";

	/** The key for the middle name field. */
	public static String MIDDLE_NAME = "MiddleName";

	/** The key for the last name field. */
	public static String LAST_NAME = "LastName";

	/** The key for the name prefix field. */
	public static String NAME_PREFIX = "NamePrefix";

	/** The key for the name suffix field. */
	public static String NAME_SUFFIX = "NameSuffix";

	/** The key for the name title field. */
	public static String NAME_TITLE  = "NameTitle";
		
	/** The key for the maiden name field. */
	public static String MAIDEN_NAME = "MaidenName";

	/** The key for the nickname field. */
	public static String NICKNAME = "Nickname";
	
	/**
	 * Creates a new ParsedName.
	 */
	public ParsedName() { 
		// nothing to do...
	}
	
	/**
	 * Returns a simple attribute/value representation of this name.
	 * 
	 * @return a String of several lines, each of which contains an attribute/value
	 * pair for this ParsedName
	 */
	public String toAttributeValueString() {
		StringBuffer sBuff = new StringBuffer();

		sBuff.append("Prefix:      ");
		if (has(NAME_PREFIX)) {
			sBuff.append(get(NAME_PREFIX));
		}
		sBuff.append('\n');

		sBuff.append("First Name:  ");
		if (has(FIRST_NAME)) {
			sBuff.append(get(FIRST_NAME));
		}
		sBuff.append('\n');
		
		sBuff.append("Middle Name: ");
		if (has(MIDDLE_NAME)) {
			sBuff.append(get(MIDDLE_NAME));	
		}
		sBuff.append('\n');

		sBuff.append("Last Name:   ");
		if (has(LAST_NAME)) {
			sBuff.append(get(LAST_NAME));	
		}
		sBuff.append('\n');

		sBuff.append("Suffix:      ");
		if (has(NAME_SUFFIX)) {
			sBuff.append(get(NAME_SUFFIX));
		}
		sBuff.append('\n');
		
		return sBuff.toString();
	}

}
