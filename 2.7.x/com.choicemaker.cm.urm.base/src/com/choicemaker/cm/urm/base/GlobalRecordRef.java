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
 * A record in a referenced record collection (database, file) that represented by its ID.
 * <p>  
 *
 * @author emoussikaev
 * @version Revision: 2.5  Date: Nov 1, 2005 12:13:01 PM
 * @see
 */
public class GlobalRecordRef implements ISingleRecord{
	
	/** As of 2010-11-12 */
	static final long serialVersionUID = -612416872534337942L;

	private Comparable          id;
	private RefRecordCollection recCollRef;
		
	public GlobalRecordRef(Comparable id, RefRecordCollection recColl) {
		super();
		this.id = id;
		this.recCollRef = recColl;
	}
	/**
	 * <code>getId</code>
	 * <p> 
	 * 
	 * @return
	 */
	public Comparable getId() {
		return id;
	}

	/**
	 * <code>getRecCollRef</code>
	 * <p> 
	 * 
	 * @return
	 */
	public RefRecordCollection getRecCollRef() {
		return recCollRef;
	}

	/**
	 * <code>setId</code>
	 * <p> 
	 * 
	 * @param comparable
	 */
	public void setId(Comparable comparable) {
		id = comparable;
	}

	/**
	 * <code>setRecCollRef</code>
	 * <p> 
	 * 
	 * @param collection
	 */
	public void setRecCollRef(RefRecordCollection collection) {
		recCollRef = collection;
	}

	public void accept(IRecordVisitor ext){
		ext.visit(this);
	}	
}
