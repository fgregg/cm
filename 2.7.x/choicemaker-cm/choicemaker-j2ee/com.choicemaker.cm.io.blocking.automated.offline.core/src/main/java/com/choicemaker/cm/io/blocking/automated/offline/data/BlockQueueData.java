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
package com.choicemaker.cm.io.blocking.automated.offline.data;

import java.io.Serializable;
import java.util.ArrayList;

import com.choicemaker.cm.io.blocking.automated.offline.core.ComparisonArray;

/**
 * This object is produced by BlockQueueProducer and used by BlockQueueConsumer.
 * It contains a ComparisonGroup and a list of Records.
 * 
 * @deprecated
 * 
 * @author pcheung
 *
 */
public class BlockQueueData implements Serializable {

	/* As of 2010-03-10 */
	static final long serialVersionUID = -2356704154280725951L;
	
	private ComparisonArray cg;
	private ArrayList stageRecords;
	private ArrayList masterRecords;
	
	public BlockQueueData (ComparisonArray cg, ArrayList stageRecords, ArrayList masterRecords) {
		this.cg = cg;
		this.stageRecords = stageRecords;
		this.masterRecords = masterRecords;
	}


	public ComparisonArray getComparisonGroup () {
		return cg;
	}
	
	public ArrayList getStageRecords () {
		return stageRecords;
	}
	
	public ArrayList getMasterRecords () {
		return masterRecords;
	}

}
