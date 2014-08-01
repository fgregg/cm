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

import java.io.File;
import java.io.IOException;

import com.choicemaker.cm.core.XmlConfException;

/**
 *
 * @author    Adam Winkel
 * @version   
 */
public interface ObjectMaker {
	/**
	 * Generate this ObjectMaker's objects (files or groups of files) in the
	 * specified output directory.  This method does not specify the name of the
	 * file to be created within outDir, which is up to each implementation. 
	 */
	public void generateObjects(File outDir) throws XmlConfException, IOException;
}
