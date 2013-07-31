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

import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Comment
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.1 $ $Date: 2010/01/20 15:05:03 $
 */
public class RemoveBelow32 {
	private static final int SIZE = 8192;
	static byte[] buf = new byte[SIZE];
	
	public static void main(String[] args) throws Exception {
		FileInputStream is = new FileInputStream(args[0]);
		FileOutputStream os = new FileOutputStream(args[1]);
		int available = is.available();
		int read;
		while((read = is.read(buf)) != -1) {
			for (int i = 0; i < read; i++) {
				byte b = buf[i];
				if(b < 32 && b != 10 && b != 13) {
					buf[i] = 32;
				}
			}
			os.write(buf, 0, read);
		}
		is.close();
		os.close();
	}
}
