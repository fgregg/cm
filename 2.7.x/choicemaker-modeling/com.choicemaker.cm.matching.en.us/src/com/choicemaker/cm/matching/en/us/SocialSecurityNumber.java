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

import com.choicemaker.cm.core.util.StringUtils;

/**
 * Returns the first 9 digits of a SSN.
 *
 * @author    Adam Winkel
 * @author    Martin Buechi
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:02 $
 */
public class SocialSecurityNumber {
	
	private SocialSecurityNumber() { }

	private static final int LEN = 9;

	/**
	 * Returns the first nine digits of an SSN.
	 *
	 * If the input is <code>null</code>, then <code>null</code>
	 * is returned.
	 *
	 * If the input has less than nine digits, then only those digits
	 * are returned (nothing is preprended or appended).
	 *
	 * @param ssn The Social Security number.
	 * @return The first <code>LEN</code> digits of the SSN
	 */
	public static String ssn(String ssn) {
		if (ssn != null) {
			ssn = StringUtils.removeNonDigits(ssn);
			if (ssn.length() > LEN) {
				ssn = ssn.substring(0, LEN);
			}
		}
		return ssn;
	}
}
