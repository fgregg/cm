/*
 * Created on Jun 2, 2004
 *
 */
package com.choicemaker.cm.io.db.sqlserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.base.MutableMarkedRecordPair;


class MarkedRecordPairSourceSpec {
	private List spec;
	public MarkedRecordPairSourceSpec() {
		this.spec = new ArrayList();
	}
	
	public void addMarkedPair(Comparable qId, Comparable mId, Decision d) {
		spec.add(new MarkedRecordPairSpec(qId, mId, d));
	}
	
	/**
	 * Returns a list of entries, one for each previous call to 
	 * addMarkedPair().  Each entry is either a pair or a RecordNotFoundException.
	 */
	public List createPairs(RecordSource rs) throws IOException {
		HashMap recordMap = new HashMap();
		try {
			rs.open();
			while (rs.hasNext()) {
				Record r = rs.getNext();
				recordMap.put(r.getId().toString(), r);
			}
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}	
		}
		
		List pairs = new ArrayList(spec.size());
		for (int i = 0; i < spec.size(); i++) {
			MarkedRecordPairSpec s = (MarkedRecordPairSpec)spec.get(i);
			Record q = (Record) recordMap.get(s.getQId());
			Record m = (Record) recordMap.get(s.getMId());

			if (q == null && m != null) {
				pairs.add(new RecordPairRetrievalException(s.getQId(), s.getMId(), RecordPairRetrievalException.Q_RECORD));
			} else if (q != null && m == null) {
				pairs.add(new RecordPairRetrievalException(s.getQId(), s.getMId(), RecordPairRetrievalException.M_RECORD));
			} else if (q == null && m == null) {
				pairs.add(new RecordPairRetrievalException(s.getQId(), s.getMId(), RecordPairRetrievalException.BOTH));
			} else {
				MutableMarkedRecordPair mrp = new MutableMarkedRecordPair();
				mrp.setQueryRecord(q);
				mrp.setMatchRecord(m);
				mrp.setMarkedDecision(s.getDecision());
				mrp.setComment("");
				mrp.setDateMarked(new Date());
				mrp.setSource("");
				mrp.setUser("");
			
				pairs.add(mrp);
			}
		}
		
		return pairs;
	}
	
}
