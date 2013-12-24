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
package com.choicemaker.cm.module;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;

import com.choicemaker.cm.module.IModuleController.IStatusModel;

/**
 * @author rphall
 */
public interface IUserMessages extends IStatusModel {
	
	public Writer getWriter();

	public OutputStream getOutputStream();

	public PrintStream getPrintStream();

	public void postMessage(final String s);

	public void clearMessages();

	/** Displays a message to the user */
	public void postInfo(String s);

}

