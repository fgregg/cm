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
package com.choicemaker.cm.matching.gen;

import java.util.StringTokenizer;

import com.choicemaker.util.StringUtils;

// import javax.mail.internet.AddressException;
// import javax.mail.internet.InternetAddress;

/**
 * The Email class contains static methods for standardizing email addresses
 * and extracting the constituent pieces of an address, according to the RFC822
 * standard.
 *
 * @author   Adam Winkel
 * @author   Rick Hall (removed dependence on javax.mail)
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:04 $
 * @deprecated
 */
public final class Email {

	private Email() { }
	
	/**
	 * Returns an equivalent email address without the personal name, or null, 
	 * if there is a problem parsing the address.
	 * 
	 * @param address a raw email address 
	 * @return the cleaned address string without the personal name
	 */
	public static String getAddress(String address) {
		/*
		InternetAddress addr = init(address);
		if (addr != null) {
			return addr.getAddress();	
		} else {
			return null;	
		}
		*/

		String retVal = null;

		if (StringUtils.nonEmptyString(address)) {

			// Remove angle brackets
			address = address.replace('<',' ');
			address = address.replace('>',' ');
			address = address.trim();

			// Look for a chunk of text containing an "@" symbol
			StringTokenizer tokens = new StringTokenizer(address);
			while (tokens.hasMoreTokens()) {
				String s = tokens.nextToken();
				int index = s.indexOf('@');
				if (index>-1) {
					retVal=s;
					break;
				} // if
			} // while

		} // if
		
		return retVal;
	} // getAddress(String)
	
	/**
	 * Returns the user name for the specified email address, that is, everything
	 * <it>before</it> the '@', or <code>null</code> if there is a problem parsing the
	 * address, or the address is null.
	 * 
	 * @param address a raw email address
	 * @return the user name
	 */
	public static String getUser(String address) {
		String[] split = split(address);
		return split[0];
	}
	
	/**
	 * Returns the domain name for the specified email address, that is, everything
	 * <it>after</it> the '@', or <code>null</code> if there is a problem parsing the
	 * address, or the address is null.
	 * 
	 * @param address a raw email address
	 * @return the domain name
	 */
	public static String getDomain(String address) {
		String[] split = split(address);
		return split[1];
	}
	
	/**
	 * Returns the personal name for the specified email address;
	 * that is, everything before the opening angle bracket in
	 * <code>John Doe &lt;jdoe@somedomain.com&gt;</code>.
	 * Returns <code>null</code> if there is a problem parsing the
	 * address, or the address is null.
	 * 
	 * @param address a raw email address
	 * @return the personal name
	 */
	public static String getPersonal(String address) {

		/*
		InternetAddress addr = init(address);
		if (addr != null) {
			return addr.getPersonal();
		} else {
			return null;
		}
		*/

		String retVal = null;
		
		if (StringUtils.nonEmptyString(address)) {
			int index = address.indexOf('<');
			if (index > -1) {
				retVal = address.substring(0,index);
			}
		}
		
		return retVal;		
	} // getPersonal(String)
	
	private static String[] split(String address) {
		String[] split = new String[2];

		String sAddr = getAddress(address);
		if (sAddr != null) {
			StringTokenizer tokens = new StringTokenizer(sAddr, "@");
			if (tokens.hasMoreTokens()) {
				split[0] = tokens.nextToken();	
			}
			if (tokens.hasMoreTokens()) {
				split[1] = tokens.nextToken();	
			}
		}
		
		return split;
	}
	
}
