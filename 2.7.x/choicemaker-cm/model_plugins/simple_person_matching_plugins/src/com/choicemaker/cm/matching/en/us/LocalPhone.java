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

/**
 * Returns the last 7 digits of a phone number.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:02 $
 */
public class LocalPhone {
	private LocalPhone() {
	};

	private static final int len = 7;

	/**
	 * Returns the last <code>len</code> digits of a phone number.
	 * This basically amounts to striping the phone number
	 * of the area code and non-digits.
	 *
	 * If the input is <code>null</code>, then <code>null</code>
	 * is returned.
	 *
	 * If <code>phoneNumber</code> has fewer than <code>len</code> digits, leading
	 * zeros are added.
	 *
	 * Note that this function does not attempt to remove
	 * extensions. E.g., <code>localPhone("212 905 6033 x11")</code>
	 * returns <code>5603311</code>.
	 *
	 * @param phoneNumber The phone number.
	 * @return The last <code>len</code> digits of the phone number.
	 */
	public static String localPhone(String phoneNumber) {
		if (phoneNumber == null) {
			return null;
		} else {
			char[] res = new char[len];
			int pos = len;
			for (int i = phoneNumber.length() - 1; i >= 0 && pos > 0; --i) {
				char c = phoneNumber.charAt(i);
				if ('0' <= c && c <= '9') {
					res[--pos] = c;
				}
			}
			while (pos > 0) {
				res[--pos] = '0';
			}
			return new String(res);
		}
	}
}
