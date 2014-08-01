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

public class CustomFileFilter extends javax.swing.filechooser.FileFilter implements java.io.FileFilter {
	private String description;
	private String[] extensions;
	private boolean dirs;

	public CustomFileFilter(String description, String[] extensions, boolean dirs) {
		this.description = description;
		this.extensions = extensions;
		this.dirs = dirs;
	}

	public CustomFileFilter(String description, String extension, boolean dirs) {
		this.description = description;
		this.extensions = new String[1];
		this.extensions[0] = extension;
		this.dirs = dirs;
	}

	public boolean accept(File f) {
		if (f.isDirectory()) {
			return dirs;
		}

		String extension = getExtension(f);
		for (int i = 0; i < extensions.length; ++i) {
			String exi = extensions[i];
			if (exi == null) {
				if (extension == null || extension.length() == 0) {
					return true;
				}
			} else if (exi.equals(extension) || (exi.length() == 0 && extension == null)) {
				return true;
			}
		}
		return false;
	}

	// The description of this filter
	public String getDescription() {
		return description;
	}

	private String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}
}
