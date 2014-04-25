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
 * A collection of records located in a resource identifies by URL.
 * <p>
 *
 * @author emoussikaev
 * @version Revision: 2.5  Date: Nov 1, 2005 1:32:59 PM
 * @see
 */
public abstract class RefRecordCollection implements IRecordCollection{

	private static final long serialVersionUID =  -4421691427817051628L;
	private String url;

//	public RefRecordCollection(){}
	/**
	 * Constructs a <code>RefRecordCollection</code> with the specified Uniform Resource Locator
	 *
	 * @param   locator  The URL that defines the location of the "resource" that provides the set of the records
	 */
	public RefRecordCollection(String url) {
		super();
		this.url = url;
	}


	public String getUrl() {
		return url;
	}

	public void setUrl(String string) {
		url = string;
	}

	public String toString() {
		return this.url;
	}

}
