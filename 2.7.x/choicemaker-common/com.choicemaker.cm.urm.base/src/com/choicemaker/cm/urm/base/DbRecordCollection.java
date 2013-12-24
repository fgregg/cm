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
 * A record collection in a database. 
 * <p>  
 *
 * @author emoussikaev
 * @version Revision: 2.5  Date: Nov 1, 2005 1:36:59 PM
 * @see
 */
public class DbRecordCollection extends RefRecordCollection {
	
	/** As of 2010-11-12 */
	static final long serialVersionUID = -6426979517126949280L;

	public static final int DEFAULT_REC_COLLETION_BUFFER_SIZE = 100000;
	private Integer    bufferSize; 				

	private String name;
	
	/**
	 * Constructs a <code>DbRecordCollection</code> using the data source JNDI name, user name, password and the query that provides a list of records ids.
	 * The record collection will consist of the records with the specified IDs.
	 * 
	 * @param   dbConfig  
	 * @param 	idsQuery The query that provides a list of records ids.
	 * @param	maxSize
 
	 */		
	public DbRecordCollection(String url, String name, int bufferSize) throws RecordCollectionException{
		super(url);
		this.name = name;
		if(bufferSize >0)
			this.bufferSize = new Integer(bufferSize);
		else
			throw new RecordCollectionException("invalid buffer size "+ bufferSize );
	}


	/**
	 * Constructs a <code>DbRecordCollection</code> using the data source JNDI name, user name, password and the query that provides a list of records ids.
	 * The record collection will consist of the records with the specified IDs.
	 * 
	 * @param   dbConfig  
	 * @param 	idsQuery The query that provides a list of records ids.
	 * @param	maxSize
 
	 */		
	public DbRecordCollection(String url,String name) {
		super(url);
		this.name = name;
		this.bufferSize = new Integer(DEFAULT_REC_COLLETION_BUFFER_SIZE);
	}
	public String getName() {
		return name;
	}


	public void setName(String n) {
		name = n;
	}
	
	public void accept(IRecordCollectionVisitor ext)throws RecordCollectionException{
		ext.visit(this);
	}

	/**
	 * @return
	 */
	public Integer getBufferSize() {
		return bufferSize;
	}

	/**
	 * @param long1
	 */
	public void setBufferSize(Integer sz) {
		if(sz.intValue() >0)
			bufferSize = sz;
	}

	public String toString() {
		return super.toString()+"|"+this.name;
	}
}


///** URL
// * Constructs a <code>DbRecordSource</code> using the data source JNDI name and the query that provides a list of record ids.
// * The record collection will consist of the records with the specified IDs.
// * 
// * @param   dataSourceName  The JNDI name of the data source.
// * @param 	idsQuery The query that provides a list of records ids. 
// */	
//public DbRecordSource(String DriverClass, String idsQuery) {
//	this.dataSourceName = dataSourceName;
//	this.idsQuery = idsQuery;
//}
