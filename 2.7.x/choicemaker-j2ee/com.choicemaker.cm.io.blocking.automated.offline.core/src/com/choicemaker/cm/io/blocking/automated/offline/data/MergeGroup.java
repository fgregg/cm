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

import com.choicemaker.cm.io.blocking.automated.offline.core.IMergeGroup;

/**
 * MatchGroup is the key Object in the transitivity engine.
 * 
 * @deprecated
 * 
 * @author pcheung
 *
 */
public class MergeGroup implements IMergeGroup {

	private long [] stageIDs;
	private long [] masterIDs;
	private long [] stageHolds;
	private long [] masterHolds;
	private boolean isHold;
	
	
	public MergeGroup (long[] stageIDs, long[] masterIDs, long[] stageHolds, long [] masterHolds,
		boolean isHold) {
		this.stageIDs  = stageIDs;
		this.masterIDs = masterIDs;
		this.masterHolds = masterHolds;
		this.stageHolds = stageHolds;
		this.isHold = isHold;
	}
	


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IMergeGroup#getStagingRecords()
	 */
	public long[] getStagingRecords() {
		return stageIDs;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IMergeGroup#getMasterRecords()
	 */
	public long[] getMasterRecords() {
		return masterIDs;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IMergeGroup#isHold()
	 */
	public boolean isHold() {
		return isHold;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IMergeGroup#getStagingHolds()
	 */
	public long[] getStagingHolds() {
		return stageHolds;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IMergeGroup#getMasterHolds()
	 */
	public long[] getMasterHolds() {
		return masterHolds;
	}
	

}
