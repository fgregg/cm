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

import com.choicemaker.util.StringUtils;

/**
 * Utilities for dealing with US Zip Codes.
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:02 $
 */
public final class ZipCode {

	private ZipCode() { }

	/**
	 * Returns true if <code>zip</code> is a valid zip code.
	 * This means it has either five or nine digits.
	 *
	 * @param zip the input zip code
	 * @return true if zip is a valid zip code
	 */
	public static boolean isValidZip(String zip) {
		String s = cleanZip(zip);
		if (s == null) {
			return false;
		} else {
			return s.length() == 5 || s.length() == 9;
		}
	}

	/**
	 * Returns the 5-digit zip code from the (possibly) non-standard
	 * zip code <code>zip</code>.  If the 5-digit zip code cannot
	 * be extracted, returns null.
	 *
	 * @param zip the input zip code
	 * @return the 5-digit zip code from the input zip
	 */
	public static String get5DigitZip(String zip) {
		String s = cleanZip(zip);
		if (s != null && s.length() > 4) {
			return zip.substring(0,5);
		}
		return null;
	}

	/**
	 * Returns the plus4 zip code extension from the input
	 * or null if the input zip is invalid or doesn't have a
	 * plus4 extension.
	 *
	 * @param zip the input zip code
	 * @return the plus4 extension for <code>zip</code>
	 */
	public static String getPlus4(String zip) {
		String s = cleanZip(zip);
		if (s != null && s.length() == 9) {
			return s.substring(5, 9);
		}
		return null;
	}

	private static String cleanZip(String zip) {
		if (zip != null) {
			return StringUtils.removeNonDigits(zip);
		} else {
			return null;
		}
	}

	/**
	 * Returns the state code for the specified zip code, or null
	 * if the input zip is invalid or a the zip doesn't correspond
	 * to a state.
	 *
	 * @param zip the input zip code
	 * @return the state code for the input zip
	 */
	public static String getStateCodeFromZipCode(String zip) {
		if (zip == null || zip.length() == 0) {
			return null;
		}

		int z = -1;
		try {
			z = Integer.parseInt(zip);
		} catch (NumberFormatException ex) {
			System.err.println("Unable to get state code from zip: " + zip);
			return null;
		}

		for (int i = 0; i < LENGTH; i++) {
			if (z >= MIN[i] && z <= MAX[i]) {
				return STATES[i];
			}
		}

		return null;
	}

	// Data for getStateCodeFromZipCode()

	private static final int[] MIN = {
		601,
		1001,
		2801,
		3031,
		3901,
		5001,
		5501,
		5601,
		6001,
		6390,
		6401,
		7001,
		10001,
		15001,
		19701,
		20001,
		20101,
		20201,
		20601,
		22001,
		24701,
		27006,
		29001,
		30001,
		32004,
		35004,
		37010,
		38601,
		39901,
		40003,
		43001,
		46001,
		48001,
		50001,
		53001,
		55001,
		57001,
		58001,
		59001,
		60001,
		63001,
		66002,
		68001,
		68119,
		68122,
		70001,
		71233,
		71234,
		71601,
		73001,
		73301,
		73401,
		75001,
		75502,
		75503,
		80001,
		82001,
		83201,
		84001,
		85001,
		87001,
		88510,
		88901,
		90001,
		96701,
		97001,
		98001,
		99501
	};

	private static final int[] MAX = {
		988,
		2791,
		2940,
		3897,
		4992,
		5495,
		5544,
		5907,
		6389,
		6390,
		6928,
		8989,
		14975,
		19640,
		19980,
		20099,
		20199,
		20599,
		21930,
		24658,
		26886,
		28909,
		29948,
		31999,
		34997,
		36925,
		38589,
		39776,
		39901,
		42788,
		45999,
		47997,
		49971,
		52809,
		54990,
		56763,
		57799,
		58856,
		59937,
		62999,
		65899,
		67954,
		68118,
		68120,
		69367,
		71232,
		71233, // Debatable whether this is MS or LA.  It is surrounded by LA zips, and the USPS couldn't locate it.
		71497,
		72959,
		73199,
		73301,
		74966,
		75501,
		75502, // Debatable whether this is AR or TX.  It is surrounded by TX, and the USPS couldn't locate it.
		79999,
		81658,
		83128,
		83876,
		84784,
		86556,
		88441,
		88589,
		89883,
		96162,
		96898,
		97920,
		99403,
		99950
	};

	private static final String[] STATES = {
		"PR",
		"MA",
		"RI",
		"NH",
		"ME",
		"VT",
		"MA",
		"VT",
		"CT",
		"NY",
		"CT",
		"NJ",
		"NY",
		"PA",
		"DE",
		"DC",
		"VA",
		"DC",
		"MD",
		"VA",
		"WV",
		"NC",
		"SC",
		"GA",
		"FL",
		"AL",
		"TN",
		"MS",
		"GA",
		"KY",
		"OH",
		"IN",
		"MI",
		"IA",
		"WI",
		"MN",
		"SD",
		"ND",
		"MT",
		"IL",
		"MO",
		"KS",
		"NE",
		"IA",
		"NE",
		"LA",
		"MS",
		"LA",
		"AR",
		"OK",
		"TX",
		"OK",
		"TX",
		"AR",
		"TX",
		"CO",
		"WY",
		"ID",
		"UT",
		"AZ",
		"NM",
		"TX",
		"NV",
		"CA",
		"HI",
		"OR",
		"WA",
		"AK",
	};

	private static final int LENGTH = MIN.length;

}
