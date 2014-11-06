/*
 * Created on Jun 2, 2004
 *
 */
package com.choicemaker.cm.io.db.sqlserver;

import com.choicemaker.cm.core.Decision;

public class MarkedRecordPairSpec {
	
	private Comparable qId, mId;
	private Decision d;
	
	public MarkedRecordPairSpec(Comparable qId, Comparable mId, Decision d) {
		this.qId = qId;
		this.mId = mId;
		this.d = d;
	}
	
	public Comparable getQId() {
		return qId;
	}
	
	public Comparable getMId() {
		return mId;
	}
	
	public Decision getDecision() {
		return d;
	}
	
}
