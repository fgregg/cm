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

import com.choicemaker.cm.urm.exceptions.RecordCollectionException;

/**
 * A record collection represented by the text in a certain format. 
 * <p>  
 *
 * @author emoussikaev
 * @version Revision: 2.5  Date: Nov 1, 2005 1:35:45 PM
 * @see
 */
public class TextRefRecordCollection extends RefRecordCollection { //implements ITextRefRecordCollection {

	/** As of 2010-11-12 */
	static final long serialVersionUID = -2150279459283519005L;

	ITextFormat format;
	
	/**
	 * Constructs a <code>RefRecordCollection</code> with the specified Uniform Resource Locator and format.
	 * 
	 * @param   locator  The URL that defines the location of the "resource" that provides the set of the records
	 * @param   f		 The format.
	 *  
	 */
	public TextRefRecordCollection(String url, ITextFormat f) {
		super(url);
		this.format = f; 
	}

	/**
	 * @return
	 */
	//public TextFormatType ugetFormat() {
	//	return format;
	//}


	public ITextFormat getFormat() {
		return format;
	}
	/**
	 * @param format
	 */
	public void setFormat(ITextFormat format) {
		this.format = format;
	}
	
	public void accept(IRecordCollectionVisitor ext)throws RecordCollectionException{
		ext.visit(this);
	}
	
	public String toString() {
		return super.toString()+"|"+this.format.toString();
	}
}
