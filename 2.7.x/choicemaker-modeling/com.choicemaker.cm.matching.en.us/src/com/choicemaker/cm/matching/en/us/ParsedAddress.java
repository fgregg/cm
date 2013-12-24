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
 * ParsedAddress contains keys for common address fields
 * (for use with ParsedDataHolder's accessor methods) as
 * well as some convenience methods.
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:02 $
 */
public class ParsedAddress extends ParsedData {

	/** The key for the Attention field. */
	public static final String ATTENTION = "Attention";

	/** The key for the Recipient field. */
	public static final String RECIPIENT = "Recipient";

	
	/** The key for the house number field. */
	public static final String HOUSE_NUMBER = "HouseNumber";
	
	/** The key for the pre direction field. */
	public static final String PRE_DIRECTION = "PreDirection";
	
	/** The key for the street name field. */
	public static final String STREET_NAME = "StreetName";
	
	/** The key for the street suffix field. */
	public static final String STREET_SUFFIX = "StreetSuffix";
	
	/** The key for the post direction field. */
	public static final String POST_DIRECTION = "PostDirection";
	
	/** The key for the apartment number field. */
	public static final String APARTMENT_NUMBER = "ApartmentNumber";
	
	/** The key for the apartment type field. */
	public static final String APARTMENT_TYPE = "ApartmentType";
	
	
	/** The key for the PO Box field. */
	public static final String PO_BOX = "POBox";
	
	
	/** The key for the RR field. */
	public static final String RR_NUM = "RRNum";
	
	/** The key for the RR Box field. */
	public static final String RR_BOX = "RRBox";


	/** The key for the HC Number field. */
	public static final String HC_NUM = "HCNum";

	/** The key for the HC Box field. */	
	public static final String HC_BOX = "HCBox";


	/** 
	 * The key for the Military Location Type field.  
	 * The only values for this are CMR, PSC, and UNIT.
	 */
	public static final String MIL_LOC_TYPE = "MilLocType";
	
	/** 
	 * The key for the Military Location Number field.  
	 * For example, the '3' in &quot;CMR 3 BOX 28&quot;
	 */
	public static final String MIL_LOC_NUM = "MilLocNum";

	/** 
	 * The key for the Military Box Number field.
	 * For example, the '28' in &quot;CMR 3 BOX 28&quot; 
	 */
	public static final String MIL_BOX_NUM = "MilBoxNum";
	
	
	/** The key for the city field. */	
	public static final String CITY = "City";
	
	/** The key for the state field. */
	public static final String STATE = "State";
	
	/** The key for the zip field. */
	public static final String ZIP = "Zip";
	
	/** The key for the zip code extension field. */
	public static final String PLUS_FOUR = "Plus4";

	/** The key for the country field. */
	public static final String COUNTRY = "Country";

	//
	// TODO:
	//	String format()  // which is composed of...
	//
	//  String formatAttention()
	//	String formatRecipient()
	//	String formatLine1()
	//	String formatLine2()	// sometimes null/empty, so omit
	//  String formatLastLine() // city, state, zip
	//

	/**
	 * Create a new ParsedAddress.
	 */
	public ParsedAddress() { 
		// nothing to do...
	}
	
	/**
	 * Returns the street name along with any directions given.
	 * Notably, this method does <it>not</it> return the street 
	 * suffix, even if it is present in this ParsedAddress.
	 * 
	 * @return a String containing the street name and directions, without the street suffix
	 */
	public String getStreetNameAndDirection() {
		StringBuffer s = new StringBuffer();
		
		if (has(PRE_DIRECTION)) {
			s.append(get(PRE_DIRECTION));
			s.append(' ');	
		}	
		
		s.append(get(STREET_NAME));
		
		if (has(POST_DIRECTION)) {
			s.append(' ');
			s.append(get(POST_DIRECTION));	
		}
		
		return s.toString();
	}
	
	/**
	 * Returns the street name surrounded by any street suffix 
	 * and directions given.  As opposed to <code>getStreetNameAndDirection()</code>,
	 * this method includes the street suffix if it is given.
	 * 
	 * @return a String containing the street name, directions, and suffix, if given
	 */
	public String getFullStreetName() {
		StringBuffer s = new StringBuffer();
		
		if (has(PRE_DIRECTION)) {
			s.append(get(PRE_DIRECTION));
			s.append(' ');	
		}
		
		if (has(STREET_NAME)) {
			s.append(get(STREET_NAME));
		}
		
		if (has(STREET_SUFFIX)) {
			s.append(' ');
			s.append(get(STREET_SUFFIX));
		}

		if (has(POST_DIRECTION)) {
			s.append(' ');
			s.append(get(POST_DIRECTION));	
		}
		
		return s.toString();
	}
	
	/**
	 * Returns a String containing the house number, the full street name, 
	 * and the apartment identifier.
	 * 
	 * @return a String containing the full street address, including house number,
	 * street name, and apartment specifier
	 */
	public String getStreetAddress() {
		StringBuffer s = new StringBuffer();
		
		if (has(HOUSE_NUMBER)) {
			s.append(get(HOUSE_NUMBER));
			s.append(' ');
		}
		
		s.append(getFullStreetName());
				
		if (has(APARTMENT_TYPE)) {
			s.append(' ');
			s.append(get(APARTMENT_TYPE));	
		}

		if (has(APARTMENT_NUMBER)) {
			s.append(' ');
			s.append(get(APARTMENT_NUMBER));
		}
					
		return s.toString();
	}

	/**
	 * Returns a simple attribute/value representation of this
	 * ParsedAddress.
	 * 
	 * @return a String containing several lines, each of which is
	 * an attribute/value pair for this ParsedAddress
	 */
	public String toAttributeValueString() {
		StringBuffer s = new StringBuffer();
		
		s.append("House Number:     ");
		s.append(has(HOUSE_NUMBER) ? get(HOUSE_NUMBER) : "");
		s.append("\n");
		
		s.append("Pre Direction:    ");
		s.append(has(PRE_DIRECTION) ? get(PRE_DIRECTION) : "");
		s.append("\n");
		
		s.append("Street Name:      ");
		s.append(has(STREET_NAME) ? get(STREET_NAME) : "");
		s.append("\n");
		
		s.append("Street Suffix:    ");
		s.append(has(STREET_SUFFIX) ? get(STREET_SUFFIX) : "");
		s.append("\n");
		
		s.append("Post Direction:   ");
		s.append(has(POST_DIRECTION) ? get(POST_DIRECTION) : "");
		s.append("\n");
		
		s.append("Apartment Type:   ");
		s.append(has(APARTMENT_TYPE) ? get(APARTMENT_TYPE) : "");
		s.append("\n");
		
		s.append("Apartment Number: ");
		s.append(has(APARTMENT_NUMBER) ? get(APARTMENT_NUMBER) : "");
		s.append("\n");
				
		return s.toString();
	}
	
	/**
	 * Returns a simple String representation of this ParsedAddress
	 * (actually just returns <code>getStreetAddress()</code>).
	 */
	public String toString() {
		return getStreetAddress();
	}

}
