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

import java.util.ArrayList;
import java.util.List;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.base.MatchCandidate;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecordSource;

/**
 * This object takes a MatchRecordSource and returns MatchLists.
 * 
 * @author pcheung
 *
 */
public class MatchListSource {
	
	IMatchRecordSource matchSource;
	MatchList nextList;
	long lastID = Long.MIN_VALUE;
	MatchCandidate lastCandidate = null;
	
	public MatchListSource (IMatchRecordSource mrs) {
		this.matchSource = mrs;
	}
	
	
	/** Opens the source for reading MatchList.
	 * 
	 * @throws BlockingException
	 */
	public void open () throws BlockingException {
		matchSource.open();
	}
	
	
	/** Closes the IMatchRecordSource.
	 * 
	 * @throws BlockingException
	 */
	public void close () throws BlockingException {
		matchSource.close();
	}
	
	
	/** True if there are more MatchList.
	 * 
	 * @return boolean - true is this source has more elements
	 */
	public boolean hasNext ()  throws BlockingException {
		boolean ret = false;
		boolean stop = false;
		long currentID = Long.MIN_VALUE;
		List<MatchCandidate> candidates = new ArrayList<>();
		MatchCandidate mc;
		IMatchRecord record;
		
		if (lastID != Long.MIN_VALUE) {
			currentID = lastID;
			candidates.add(lastCandidate);
			ret = true;
		} else {
			if (matchSource.hasNext()) {
				record = matchSource.getNext();
				if (record.getRecordID1() < lastID) {
					throw new BlockingException ("IMatchSource is not sorted " + record.getRecordID1() + 
						" " + lastID);
				}
				lastID = record.getRecordID1();
				currentID = lastID;

				mc = getMatchCandidate (record);
				candidates.add(mc);
			}			
		}
		
		while (!stop && matchSource.hasNext()) {
			record = matchSource.getNext();
			if (record.getRecordID1() < lastID) {
				throw new BlockingException ("IMatchSource is not sorted " + record.getRecordID1() + 
					" " + lastID);
			}

			if (lastID != record.getRecordID1()) {
				stop = true;
				lastID = record.getRecordID1();
				lastCandidate = getMatchCandidate (record);
			} else {
				mc = getMatchCandidate (record);
				candidates.add(mc);
			}
			ret = true;
		}
		
		if (!stop) {
			lastID = Long.MIN_VALUE;
		}
		
		//create the next MatchList
		MatchCandidate [] list = new MatchCandidate [candidates.size()];
		for (int i=0; i<candidates.size(); i++) {
			list[i] = (MatchCandidate) candidates.get(i);
		}
		nextList = new MatchList (currentID, list);
		
		return ret;
	}
	
	
	private MatchCandidate getMatchCandidate (IMatchRecord mr) {
		Long L = new Long (mr.getRecordID2());
		int decision = 0;
		
		if (mr.getMatchType() == IMatchRecord.DIFFER) decision = MatchCandidate.DIFFER;
		else if (mr.getMatchType() == IMatchRecord.MATCH) decision = MatchCandidate.MATCH;
		else if (mr.getMatchType() == IMatchRecord.HOLD) decision = MatchCandidate.HOLD;
		
		MatchCandidate mc = new MatchCandidate (L, mr.getProbability(), decision, null);
		return mc;
	}
	
	
	/** Returns the next MatchList
	 * 
	 * @return MatchList - the next MatchList in the list.
	 */
	public MatchList getNext () {
		return nextList;
	}
	

}
