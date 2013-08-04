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
 * A set of records connected by the match or hold relationship. Field <code>connections</code> provides
 * evaluation of the matching between those records.
 * <p>  
 *
 * @author emoussikaev
 * @version Revision: 2.5  Date: Nov 1, 2005 5:06:52 PM
 * @see
 */
public class ConnectedRecordSet extends CompositeRecord {

	/** As of 2010-11-12 */
	static final long serialVersionUID = 1604807022706941623L;

	private IRecordConnection[]		connections;

	public ConnectedRecordSet(Comparable id, IRecord[] r, IRecordConnection[] connections) {
		super(id,r);
		this.connections = connections;
	}
	/**
	 * @return
	 */
	public IRecordConnection[] getConnections() {
		return connections;
	}

	/**
	 * @param links
	 */
	public void setConnections(RecordConnection[] links) {
		this.connections = links;
	}

	public void accept(IRecordVisitor ext){
		ext.visit(this);
	}
}
