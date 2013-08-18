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
package com.choicemaker.cm.reviewmaker.base;

import java.io.Serializable;

import com.choicemaker.cm.core.Decision;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.2 $ $Date: 2010/04/15 20:51:55 $
 */
public class ClientData implements Serializable {

	/* As of 2010-03-10 */
	static final long serialVersionUID = 3345271052608581164L;

	// from ServerData
	private int id;
	// Decision for each potential match
	private Decision[] decisions;

	/**
	 * Constructor for ReviewResponse.
	 */
	public ClientData(int id, Decision[] decisions){
		this.id = id;
		this.decisions = decisions;
	}
	
	/**
	 * Returns the decisions.
	 * @return Decision[]
	 */
	public Decision[] getDecisions() {
		return decisions;
	}

	/**
	 * Returns the id.
	 * @return int
	 */
	public int getId() {
		return id;
	}

}
