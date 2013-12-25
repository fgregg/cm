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
package com.choicemaker.cm.transitivity.util;

import java.io.File;
import java.io.IOException;

/**
 * @author pcheung
 *
 */
public class FileUtils {

	/** This returns true is the writer file size is greater than the maxFileSize. 
	 * 
	 * @return
	 */
	public static boolean isFull (String fileBase, String fileExt,
		int currentFile, int maxFileSize) throws IOException {
		
		File f = new File (FileUtils.getFileName (fileBase, fileExt, currentFile));
		
		if (f.length() >= maxFileSize) {
			return true;
		} else {
			return false;
		} 
	}
	
	
	public static String getFileName (String fileBase, String fileExt, int num) {
		return fileBase + "_" + num + "." + fileExt;
	}
	

}
