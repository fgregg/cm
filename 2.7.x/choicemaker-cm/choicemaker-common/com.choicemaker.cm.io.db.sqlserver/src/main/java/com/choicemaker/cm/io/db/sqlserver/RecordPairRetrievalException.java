/*
 * Created on Oct 31, 2004
 */
package com.choicemaker.cm.io.db.sqlserver;

import java.io.IOException;

/**
 * @author Adam Winkel
 */
public class RecordPairRetrievalException extends IOException {

	private static final long serialVersionUID = 271L;

	public static final int Q_RECORD = -1;
	public static final int M_RECORD = -2;
	public static final int BOTH = -3;

	private Object qId, mId;
	private int which;

	public RecordPairRetrievalException(Object qId, Object mId, int which) {
		super(createMessage(qId, mId, which));
		
		this.qId = qId;
		this.mId = mId;
		this.which = which;
	}

	public Object getQId() {
		return qId;
	}
	
	public Object getMId() {
		return mId;	
	}
	
	public int getWhich() {
		return which;	
	}
	
	private static String createMessage(Object qId, Object mId, int which) {
		if (which == Q_RECORD) {
			return "Unable to retrieve record " + qId;
		} else if (which == M_RECORD) {
			return "Unable to retrieve record " + mId;
		} else {
			return "Unable to retrieve records " + qId + " and " + mId;	
		}	
	}

}
