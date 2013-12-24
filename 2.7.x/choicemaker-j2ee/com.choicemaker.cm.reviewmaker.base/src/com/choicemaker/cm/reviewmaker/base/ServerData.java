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

import com.choicemaker.cm.core.Record;

/**
 * .
 *
 * @author   Arturo Falck
 * @version  $Revision: 1.2 $ $Date: 2010/04/15 20:51:34 $
 */
public class ServerData implements Serializable {

	/* As of 2010-03-10 */
	static final long serialVersionUID = 1967837495006336991L;

	public static final int DATA = 0;
	public static final int LOCKED = 1;
	public static final int NO_REVIEW = 2;
	public static final int ERROR = 3;
	
	private int type;
	private int id;
	private Record baseRecord;
	private MatchRecord[] potentialMatches;
	
	public ServerData(int id, Record baseRecord, MatchRecord[] potentialMatches){
		this.type = DATA;
		this.id = id;
		this.baseRecord = baseRecord;
		this.potentialMatches = potentialMatches;
	}
	
	public ServerData(int type) {
		this.type = type;
		this.id = -1;
	}
	
	/**
	 * Returns the baseRecord.
	 * @return Record
	 */
	public Record getBaseRecord() {
		return baseRecord;
	}

	/**
	 * Returns the potentialMatches.
	 * @return Record[]
	 */
	public MatchRecord[] getPotentiallyMatchingRecords() {
		return potentialMatches;
	}

	/**
	 * Returns the id.
	 * @return int
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return
	 */
	public int getType() {
		return type;
	}

}
