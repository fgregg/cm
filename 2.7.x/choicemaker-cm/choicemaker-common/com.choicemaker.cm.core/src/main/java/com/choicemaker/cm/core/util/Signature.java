/*
 * Copyright (c) 2009, 2010 Rick Hall and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License
 * v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Rick Hall - initial API and implementation
 */
package com.choicemaker.cm.core.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

import com.choicemaker.cm.core.ClueSet;
import com.choicemaker.cm.core.Descriptor;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.Evaluator;
import com.choicemaker.util.Base64;
import com.choicemaker.util.Precondition;

/**
 * @author rphall
 * @version $Revision: 1.1.2.4 $ $Date: 2010/03/12 18:23:09 $
 */
public class Signature {

	// Don't use directly, use getMessageDigest()
	private static MessageDigest _digest = null;

	// Don't use directly, use getDigestLength()
	private static int _digestLength = -1;

	/**
	 * The byte encoding used to calculate signatures of Strings.
	 * <code>char</code> values are converted to <code>byte</code> values using
	 * this character set.
	 * 
	 * @see #calculateSignature(String,String)
	 */
	// public static final String CHARSET_NAME = ModelingConstants.CHARSET_NAME;
	public static final String CHARSET_NAME = "UTF8"; //$NON-NLS-1$

	/**
	 * The hash algorithm used to calculate signatures of Strings.
	 * 
	 * @see #calculateSignature(String,String)
	 */
	public static final String HASH_ALGORITHM = "SHA-1"; //$NON-NLS-1$

	private static final Logger log = Logger.getLogger(Signature.class
			.getName());
	
	public static String calculateClueSetSignature(ClueSet cs) {
		Precondition.assertNonNullArgument("null ClueSet", cs);
		String retVal = Signature.calculateSignature(cs.getClass());
		return retVal;
	}

	public static String calculateEvaulatorSignature(Evaluator e) {
		Precondition.assertNonNullArgument("null evaluator", e);
		String retVal = Signature.calculateSignature(e.getClass());
		return retVal;
	}

	public static String calculateModelSignature(ImmutableProbabilityModel ipm) {
		Precondition.assertNonNullArgument("null model", ipm);

		String s1 = calculateEvaulatorSignature(ipm.getEvaluator());
		String s2 = calculateClueSetSignature(ipm.getClueSet());
		String s3 =
			calculateRecordLayoutSignature(ipm.getAccessor().getDescriptor());
		String retVal = calculateModelSignature(s1, s2, s3);
		return retVal;
	}

	public static String calculateModelSignature(String evaluatorSignature,
			String cluesetSignature, String recordLayoutSignature) {
		Precondition.assertNonEmptyString(evaluatorSignature);
		Precondition.assertNonEmptyString(evaluatorSignature);
		Precondition.assertNonEmptyString(evaluatorSignature);

		String s =
			evaluatorSignature + cluesetSignature + recordLayoutSignature;
		String retVal = Signature.calculateSignature(s);
		return retVal;
	}

	public static String calculateRecordLayoutSignature(Descriptor d) {
		Precondition.assertNonNullArgument("null descriptor", d);
		String retVal = Signature.calculateSignature(d.getHandledClass());
		Descriptor[] children = d.getChildren();
		if (children != null && children.length > 0) {
			for (int i = 0; i < children.length; i++) {
				retVal += calculateRecordLayoutSignature(children[i]);
			}
		}
		retVal = calculateSignature(retVal);
		return retVal;
	}

	public static String calculateSignature(Class c) {
		Precondition.assertNonNullArgument("null class", c);
		String retVal;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(c);
			byte[] toDigest = baos.toByteArray();
			byte[] sig_bytes = getMessageDigest().digest(toDigest);
			retVal = Base64.encodeBytes(sig_bytes, false);
		} catch (IOException x) {
			String msg = "Unexpected IO exception: " + x.getMessage();
			log.severe(msg + x);
			throw new IllegalStateException(msg);
		}

		return retVal;
	}

/**
	 * Computes a Base64-encoded SHA-1 hash.
	 * <p>
	 * The input string is trimmed (or converted to an empty String if null).
	 * The String is then converted to a byte array using a
	 * {@link #CHARSET_NAME default encoding}. The byte array is hashed
	 * using a {@link #HASH_ALGORITHM default algorithm, and the resulting
	 * bytes are converted to ASCII character values using Base64 encoding.
	 * @return a String value about 60 charactes (or fewer) in length
	 * @throws IllegalStateException if the {@link #HASH_ALGORITHM default algorithm}
	 * is not available, or if the {@link #CHARSET_NAME default encoding}
	 * is not supported.
	 */
	public static String calculateSignature(String s)
			throws IllegalStateException {

		s = s == null ? "" : s.trim();

		String retVal = null;
		try {
			byte[] toDigest = s.getBytes(Signature.CHARSET_NAME);
			byte[] sig_bytes = getMessageDigest().digest(toDigest);
			retVal = Base64.encodeBytes(sig_bytes, false);
		} catch (UnsupportedEncodingException x) {
			String msg =
				"default hash algorithm is not available: " + x.getMessage();
			log.severe(msg + x);
			throw new IllegalStateException(msg);
		}

		return retVal;
	}

	/** Returns the expected length of a signature, before Base64 encoding */
	public static int getDigestLength() {
		if (_digestLength == -1) {
			_digestLength = getMessageDigest().getDigestLength();
		}
		int retVal = _digestLength;
		return retVal;
	}

	static MessageDigest getMessageDigest() {
		if (_digest == null) {
			try {
				_digest = MessageDigest.getInstance(Signature.HASH_ALGORITHM);
			} catch (NoSuchAlgorithmException x) {
				String msg =
					"default hash algorithm is not available: "
							+ x.getMessage();
				log.severe(msg + x);
				throw new IllegalStateException(msg);
			}
		}
		return _digest;
	}

	private Signature() {
	}

}
