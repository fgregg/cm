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
 * A record connection between two records at least one of which is a composite record.
 * <p>
 *
 * @author emoussikaev
 * @version Revision: 2.5  Date: Nov 1, 2005 4:55:52 PM
 * @see
 */
public class CompositeConnection extends RecordConnection {

	/** As of 2010-11-12 */
	private static final long serialVersionUID = 1468508143179500532L;

	protected RecordConnection[]	inwardConnections;

	public CompositeConnection() {
		super();
	}

	public CompositeConnection(MatchScore score, int i1, int i2, RecordConnection[]	inwardLinks) {
		super(score, i1, i2);
		this.inwardConnections = inwardLinks;
	}

	public RecordConnection[] getInwardConnections() {
		return inwardConnections;
	}

	public void setInwardConnections(RecordConnection[] links) {
		inwardConnections = links;
	}
}
