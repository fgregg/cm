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

import java.io.Serializable;


/**
 * Criteria for identifying a set of records as linked together and denoting the same physical entity.
 * Criteria includes graph property type and the flag of the query record containment. 
 * <p>  
 *
 * @author emoussikaev
 * @version Revision: 2.5  Date: Nov 1, 2005 1:18:08 PM
 * @see
 */
public class LinkCriteria implements Serializable {

	/* As of 2010-03-10 */
	static final long serialVersionUID = 4915885639786038386L;

	protected GraphProperty graphPropType;
	protected boolean mustIncludeQuery;
		
	public LinkCriteria(GraphProperty t, boolean mic) {
		super();
		this.graphPropType = t;
		this.mustIncludeQuery = mic;
	}

	public boolean isMustIncludeQuery() {
		return mustIncludeQuery;
	}


	public void setMustIncludeQuery(boolean b) {
		mustIncludeQuery = b;
	}

	public GraphProperty getGraphPropType() {
		return graphPropType;
	}

	public void setGraphPropType(GraphProperty type) {
		graphPropType = type;
	}

}
