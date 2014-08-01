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
 * A collection of record holders (records presented as objects at runtime). 
 * <p>  
 *
 * @author emoussikaev
 * @version Revision: 2.5  Date: Nov 1, 2005 1:41:15 PM
 * @see
 */
public class ValueRecordCollection implements IRecordCollection {

	/** As of 2010-11-12 */
	static final long serialVersionUID = 5214701323326777466L;

	protected IRecordHolder[]	records; 

	/**
	 * Constructs an empty collection
	 */
	public ValueRecordCollection(){
		super();
	}
	
	/**
	 * Constructs a collection containing the specified records
	 * 
	 * @param   records  array of single records
	 */	
	public ValueRecordCollection(IRecordHolder[]	records){
		super();
		this.records = records;
	}

	/**
	 * <code>getRecords</code>
	 * <p> 
	 * 
	 * @return
	 */
	public IRecordHolder[] getRecords() {
		return records;
	}

	/**
	 * <code>setRecords</code>
	 * <p> 
	 * 
	 * @param holders
	 */
	public void setRecords(IRecordHolder[] holders) {
		records = holders;
	}

	public void accept(IRecordCollectionVisitor ext) throws RecordCollectionException{
		ext.visit(this);
	}

}
