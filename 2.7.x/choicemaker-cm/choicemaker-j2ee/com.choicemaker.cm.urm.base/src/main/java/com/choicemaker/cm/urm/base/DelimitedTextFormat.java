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
package com.choicemaker.cm.urm.base;

/**
 * Delimited text format.
 * <p>  
 *
 * @author emoussikaev
 * @version Revision: 2.5  Date: Nov 1, 2005 12:16:24 PM
 * @see
 */
public class DelimitedTextFormat implements ITextFormat {

	/** As of 2010-11-12 */
	static final long serialVersionUID = -5156541888862439396L;

	private char separator;
	
	public DelimitedTextFormat(char separator){
		this.separator = separator;
	}
	public void accept(ITextFormatVisitor ext) {
		ext.visit(this);
	}
	/**
	 * @return
	 */
	public char getSeparator() {
		return separator;
	}

	/**
	 * @param c
	 */
	public void setSeparator(char c) {
		separator = c;
	}

}
