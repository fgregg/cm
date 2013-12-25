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
 * A record represented by an identifier. It is assumed that the real location
 * of the record (database, file) is known from the context.
 * <p>  
 *
 * @author emoussikaev
 * @version Revision: 2.5  Date: Nov 1, 2005 12:08:29 PM
 * @see
 */
public class RecordRef implements ISingleRecord {

	/** As of 2010-11-12 */
	static final long serialVersionUID = -4347784657257638692L;

	protected Comparable	id;
	
	/**
	 * Constructs a <code>RecordRef</code> with unknown (null) identifier.
	 */
	public RecordRef() {
		super();
	}

	/**
	 * Constructs a <code>RecordRef</code> with specified identifier.
	 * <p> 
	 * 
	 * @param id
	 */
	public RecordRef(Comparable id) {
		this.id = id;
	}

	/**
	 * Returns record identifier. 
	 * <p>
	 * 
	 * @return
	 */
	public Comparable getId() {
		return id;
	}

	/**
	 * Sets record identifier.
	 * <p> 
	 * 
	 * @param id
	 */
	public void setId(Comparable id) {
		this.id = id;
	}

	public void accept(IRecordVisitor ext){
		ext.visit(this);
	}
}
