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

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import com.choicemaker.cm.module.IModuleController.IUserInterface;

/**
 * @author rphall
 */
public class SystemConsole implements IUserInterface {

	public Writer getWriter() {
		return new OutputStreamWriter(System.out);
	}
	
	public Reader getReader() {
		return new InputStreamReader(System.in);
	}

}

