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
package com.choicemaker.cm.io.flatfile.gui;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:58 $
 */
public class Separator {
	String desc;
	char sep;
	boolean other;
	Separator(String desc, char sep) {
		this.desc = desc;
		this.sep = sep;
	}
	Separator(String desc) {
		this.desc = desc;
		this.other = true;
	}
	public String toString() {
		return desc;
	}
	boolean isDefined(String ot) {
		return !other || ot.length() == 1;
	}
	boolean isOther() {
		return other;
	}
	char getSeparator(String ot) {
		if (other) {
			return ot.charAt(0);
		} else {
			return sep;
		}
	}

	static final Separator[] SEPARATORS =
		{
			new Separator("Comma (,)", ','),
			new Separator("Semicolon (;)", ';'),
			new Separator("Tab", '\t'),
			new Separator("Pipe (|)", '|'),
			new Separator("Other:")};
}
