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
package com.choicemaker.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilities for combining hash codes to form a new hash code.
 * <p>
 * See <a href="http://www.ibm.com/developerworks/java/library/j-jtp05273.html">
 * Hashing_It_Out</a> by Brian Goetz.
 * 
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/24 18:06:02 $
 */
public class HashUtils {

	public static int hashCode(int h1, int h2) {
		// see Hashing_It_Out by Brian Goetz
		// http://www.ibm.com/developerworks/java/library/j-jtp05273.html
		//
		// hashCode = 31*hashCode + (obj==null ? 0 : obj.hashCode());
		return (31 * h1) + h2;
	}

	public static int hashCode(int h1, long value) {
		return (31 * h1) + (int) (value ^ (value >>> 32));
	}

	public static int hashCode(int hashCode, Object o) {
		return (31 * hashCode) + (o == null ? 0 : o.hashCode());
	}

	/** @see Long#hashCode() */
	public static int hashCode(long value) {
		return (int) (value ^ (value >>> 32));
	}

	public static int hashCode(Object o) {
		return o == null ? 0 : o.hashCode();
	}

	public static final String DEFAULT_HASH_ALGORITHM = "SHA1";
	public static final String DEFAULT_ENCODING = "UTF-8";

	private HashUtils() {
	}

	/**
	 * Returns a base-64 encoded String representation of the SHA1 hash of the
	 * specified String encoded as UTF-8 bytes.
	 */
	public static String toBase64SHA1Hash(String s, boolean breakLines) {
		String retVal = null;
		try {
			MessageDigest md =
				MessageDigest.getInstance(DEFAULT_HASH_ALGORITHM);
			byte[] raw = md.digest(s.getBytes(DEFAULT_ENCODING));
			retVal = Base64.encodeBytes(raw, breakLines);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("No " + DEFAULT_HASH_ALGORITHM
					+ " algorithm: " + e.toString());
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("No " + DEFAULT_ENCODING
					+ " encoding: " + e.toString());
		}
		return retVal;
	}

}
