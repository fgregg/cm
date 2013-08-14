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
package com.choicemaker.cm.module.console;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;

import com.choicemaker.cm.module.IUserMessages;

/**
 * @author rphall
 */
public class SystemStatusMessages implements IUserMessages {

	public Writer getWriter() {
		return new OutputStreamWriter(System.err);
	}

	public OutputStream getOutputStream() {
		return System.err;
	}

	public PrintStream getPrintStream() {
		return new PrintStream(System.err);
	}

	public void postMessage(final String s) {
		System.err.println(s);
	}

	public void clearMessages() {
	}

	/** Displays a message to the user */
	public void postInfo(String s) {
		postMessage(s);
	}

}

