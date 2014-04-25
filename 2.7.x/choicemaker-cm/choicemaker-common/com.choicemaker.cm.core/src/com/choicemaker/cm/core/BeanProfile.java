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
package com.choicemaker.cm.core;

/**
 * Profile that represents a query record as a Java bean using the
 * generated holder classes.
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.2 $ $Date: 2010/03/24 20:59:50 $
 */
public class BeanProfile implements Profile {
	private static final long serialVersionUID = 1L;
	private Object profile;

	/**
	 * Constructs a <code>BeanProfile</code> without an inner Java Bean representation of the query.
	 */	
	public BeanProfile() { }
	
	/**
	 * Constructs a <code>BeanProfile</code> with the specified Java bean representaion of the query record.
	 * Note that <code>profile</code> must be constructed using the generated holder classes.
	 * 
	 * This constructor does not clone the passed argument. 
	 * 
	 * @param   profile  The Java bean representation of the query record.
	 */
	public BeanProfile(Object profile) {
		this.profile = profile;
	}
	
	/**
	 * Returns the Java bean representation of the query record.
	 * 
	 * @return  The Java bean representation of the query record.
	 */
	public Object getProfile() {
		return profile;
	}
	
	/**
	 * Sets the Java bean representation of the query.
	 * 
	 * @param profile  the Java bean representation of the query record
	 */
	public void setProfile(Object profile) {
		this.profile = profile;
	}
	
	public String toString() {
		return "beanProfile";
	}
	
	public Record getRecord(IProbabilityModel model) {
		return model.getAccessor().toImpl(getProfile());
	}
}
