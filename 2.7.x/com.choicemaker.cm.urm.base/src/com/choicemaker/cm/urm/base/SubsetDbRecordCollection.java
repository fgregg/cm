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
 * A subset of <code>DbRecordCollection</code> in a database. The IDs of the records included into the subset are defined by a SQL query. 
 * <p>  
 *
 * @author emoussikaev
 * @version Revision: 2.5  Date: Nov 1, 2005 1:39:49 PM
 * @see
 */
public class SubsetDbRecordCollection extends DbRecordCollection {//implements ISubsetDbRecordCollection {

	/** As of 2010-11-12 */
	static final long serialVersionUID = 6883681680224428705L;

	protected String idsQuery;

	/**
	 * Constructs a <code>DbRecordCollection</code> using the data source JNDI name, user name, password and the query that provides a list of records ids.
	 * The record collection will consist of the records with the specified IDs.
	 * 
	 * @param   dbConfig  
	 * @param 	idsQuery The query that provides a list of records ids.
	 * @param	maxSize
 
	 */		
	public SubsetDbRecordCollection(String url,String name, int bufferSize, String idsQuery)throws RecordCollectionException {
		super(url,name,bufferSize);
		this.idsQuery = idsQuery;
	}

	/**
	 * Constructs a <code>DbRecordCollection</code> using the data source JNDI name, user name, password and the query that provides a list of records ids.
	 * The record collection will consist of the records with the specified IDs.
	 * 
	 * @param   dbConfig  
	 * @param 	idsQuery The query that provides a list of records ids.
	 * @param	maxSize
 
	 */		
	public SubsetDbRecordCollection(String url,String name,String idsQuery) {
		super(url,name);
		this.idsQuery = idsQuery;
	}

	
	/**
	 * @param string
	 */
	public void setIdsQuery(String string) {
		idsQuery = string;
	}


	/**
	 * @return
	 */
	public String getIdsQuery() {
		return idsQuery;
	}

	public void accept(IRecordCollectionVisitor ext)throws RecordCollectionException{
		ext.visit(this);
	}

	public String toString() {
		return super.toString()+"|"+this.getIdsQuery();
	}
}


