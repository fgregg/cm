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
 * A group of matching together and denoting the same physical entity. 
 * <p>  
 *
 * @author emoussikaev
 * @version Revision: 2.5  Date: Nov 1, 2005 1:23:03 PM
 * @see
 */
public class LinkedRecordSet extends CompositeRecord {

	/** As of 2010-11-12 */
	static final long serialVersionUID = -8988092145857498700L;

	LinkCriteria 	   criteria;
	
	public LinkedRecordSet(Comparable id, IRecord[] r, LinkCriteria c) {
		super(id,r);
		this.criteria = c;
	}
	
	
	public LinkCriteria getCriteria() {
		return criteria;
	}
	
	public void accept(IRecordVisitor ext){
		ext.visit(this);
	}

	/**
	 * @param criteria
	 */
	public void setCriteria(LinkCriteria criteria) {
		this.criteria = criteria;
	}

}
