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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Reads a stream and writes it to another stream or appends it to a string.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.1 $ $Date: 2010/01/20 15:05:03 $
 */
public class StreamRelayer extends Thread {
	private InputStreamReader isr;
	private OutputStream os;
	private StringBuffer b;

	/**
	 * Constructs a <code>StreamRelayer</code> that reads from an
	 * <code>InputStream</code> and writes to an <code>OutputStream</code>.
	 * The process terminates when the end of the <code>InputStream</code>
	 * is reached.
	 *
	 * @param   is  the input stream.
	 * @param   os  the output stream.
	 */
	public StreamRelayer(InputStream is, OutputStream os) {
		isr = new InputStreamReader(is);
		this.os = os;
	}

	/**
	 * Constructs a <code>StreamRelayer</code> that reads from an
	 * <code>InputStream</code> and appends to a string.
	 * 
	 * @param   is  the input string.
	 */
	public StreamRelayer(InputStream is) {
		isr = new InputStreamReader(is);
		b = new StringBuffer();
	}

	/**
	 * Returns the data read from the <code>InputStream</code>
	 * as a string.
	 *
	 * @return  the data read from the <code>InputStream</code>.
	 */
	public String getInput() {
		return new String(b);
	}

	public void run() {
		try {
			int numRead = isr.read();
			while (numRead != -1) {
				if (os != null) {
					os.write(numRead);
				} else {
					b.append((char) numRead);
				}
				numRead = isr.read();
			}
			isr.close();
		} catch (IOException ex) {
			// ignore
		}
	}
}
