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
package com.choicemaker.cm.core.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.ImmutableMarkedRecordPair;
import com.choicemaker.cm.core.MarkedRecordPairSource;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.base.MutableMarkedRecordPair;
import com.choicemaker.util.IntArrayList;

public class LinkMap {
	
	List links = new ArrayList();

	Map sourceIndices = new HashMap();
	Map targetIndices = new HashMap();
	Map pairIndices = new HashMap();
	
	public LinkMap() { }
	
	public LinkMap(MarkedRecordPairSource mrps) throws IOException {
		mrps.open();
		while (mrps.hasNext()) {
			addLink(mrps.getNextMarkedRecordPair());
		}
		mrps.close();
	}
	
	public LinkMap(List pairs) {
		for (int i = 0; i < pairs.size(); i++) {
			addLink((MutableMarkedRecordPair)pairs.get(i));	
		}
	}
					
	public void addLink(MutableMarkedRecordPair mrp) {
		int index = links.size();
		
		String key1 = mrp.getQueryRecord().getId().toString();
		String key2 = mrp.getMatchRecord().getId().toString();
		
		IntArrayList sl = (IntArrayList)sourceIndices.get(key1);
		if (sl == null) {
			sl = new IntArrayList(1);
			sourceIndices.put(key1, sl);
		}
		sl.add(index);
		
		IntArrayList tl = (IntArrayList)targetIndices.get(key2);
		if (tl == null) {
			tl = new IntArrayList(1);
			targetIndices.put(key2, tl);
		}
		tl.add(index);
		
		IntArrayList pl = (IntArrayList)pairIndices.get(key1 + "#%#" + key2);
		if (pl == null) {
			pl = new IntArrayList(1);
			pairIndices.put(key1 + "#%#" + key2, pl);
		}
		pl.add(index);
		
		links.add(mrp);
	}
	
//	private ImmutableMarkedRecordPair getLink(int index) {
//		return (ImmutableMarkedRecordPair) links.get(index);	
//	}
	
	public boolean hasLink(Record q, Record m) {
		return hasLink(q.getId().toString(), m.getId().toString());	
	}
	
	public boolean hasLink(String sourceId, String targetId) {
		String key = sourceId + "#%#" + targetId;
		return pairIndices.containsKey(key);
	}

	public List getLinks(String sourceId, String targetId) {
		List ret = new ArrayList(1);
		IntArrayList list = (IntArrayList) pairIndices.get(sourceId + "#%#" + targetId);
		for (int i = 0; i < list.size(); i++) {
			ret.add(links.get(list.get(i)));	
		}
		return ret;
	}

	/**
	 * For ALL decisions...
	 */
	public List getLinks(MutableMarkedRecordPair mrp) {
		return getLinks(mrp.getQueryRecord().getId().toString(), mrp.getMatchRecord().getId().toString());
	}

	public boolean hasLink(MutableMarkedRecordPair mrp) {
		return hasLink(mrp.getQueryRecord().getId().toString(), mrp.getMatchRecord().getId().toString(), mrp.getMarkedDecision());	
	}

	public boolean hasConflictingLink(MutableMarkedRecordPair mrp) {
		List links = getLinks(mrp);
		for (int i = 0; i < links.size(); i++) {
			ImmutableMarkedRecordPair pair = (ImmutableMarkedRecordPair) links.get(i);
			if (pair.getMarkedDecision() != mrp.getMarkedDecision()) {
				return true;
			}
		} 
		return false;
	}

	public boolean hasLink(String sourceId, String targetId, Decision d) {
		List links = getLinks(sourceId, targetId);
		for (int i = 0; i < links.size(); i++) {
			ImmutableMarkedRecordPair mrp = (ImmutableMarkedRecordPair) links.get(i);
			if (mrp.getMarkedDecision() == d) {
				return true;	
			}
		}
		return false;
	}

	public ImmutableMarkedRecordPair getSourceLink(Record r) {
		List l = getSourceLinks(r);
		if(l.size() > 0) {
			return (ImmutableMarkedRecordPair)l.get(0);
		} else {
			return null;
		}
	}

	public List getSourceLinks(Record r) {
		return getSourceLinks(r.getId().toString());	
	}
	
	public List getSourceLinks(String id) {
		IntArrayList list = (IntArrayList) sourceIndices.get(id);
		if (list != null) {
			List ret = new ArrayList(1);
			for (int i = 0; i < list.size(); i++) {
				ret.add(links.get(list.get(i)));	
			}
			return ret;
		} else {
			return new ArrayList(1);	
		}
	}
	
	public List getTargetLinks(Record r) {
		return getTargetLinks(r.getId().toString());	
	}
	
	public List getTargetLinks(String id) {
		IntArrayList list = (IntArrayList) targetIndices.get(id);
		if (list != null) {
			List ret = new ArrayList(1);
			for (int i = 0; i < list.size(); i++) {
				ret.add(links.get(list.get(i)));	
			}
			return ret;
		} else {
			return new ArrayList(1);	
		}
	}
	
	public int getNumSourceLinks(String id) {
		return getSourceLinks(id).size();
	}
	
	public int getNumTargetLinks(String id) {
		return getTargetLinks(id).size();
	}
	
	public Collection getSourceIds() {
		return sourceIndices.keySet();
	}
	
	public Collection getTargetIds() {
		return targetIndices.keySet();
	}

	public List getLinks() {
		return new ArrayList(links);
	}
	
	public List filterLinkedSourceRecords(List records) {
		List ret = new ArrayList(records.size());
		for (int i = 0; i < records.size(); i++) {
			Record r = (Record) records.get(i);
			if (isLinkedSourceRecord(r)) {
				ret.add(r);	
			}
		}
		return ret;
	}

	public List filterUnlinkedSourceRecords(List records) {
		List ret = new ArrayList(records.size());
		for (int i = 0; i < records.size(); i++) {
			Record r = (Record) records.get(i);
			if (!isLinkedSourceRecord(r)) {
				ret.add(r);	
			}
		}
		return ret;
	}

	public List filterLinkedTargetRecords(List records) {
		List ret = new ArrayList(records.size());
		for (int i = 0; i < records.size(); i++) {
			Record r = (Record) records.get(i);
			if (isLinkedTargetRecord(r)) {
				ret.add(r);	
			}
		}
		return ret;
	}

	public List filterUnlinkedTargetRecords(List records) {
		List ret = new ArrayList(records.size());
		for (int i = 0; i < records.size(); i++) {
			Record r = (Record) records.get(i);
			if (!isLinkedTargetRecord(r)) {
				ret.add(r);	
			}
		}
		return ret;
	}

	private boolean isLinkedSourceRecord(Record r) {
		if (getNumSourceLinks(r.getId().toString()) > 0) {
			List links = getSourceLinks(r);
			for (int j = 0; j < links.size(); j++) {
				ImmutableMarkedRecordPair mrp = (ImmutableMarkedRecordPair) links.get(j);
				if (mrp.getMarkedDecision() != Decision.DIFFER) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	private boolean isLinkedTargetRecord(Record r) {
		if (getNumTargetLinks(r.getId().toString()) > 0) {
			List links = getTargetLinks(r);
			for (int j = 0; j < links.size(); j++) {
				ImmutableMarkedRecordPair mrp = (ImmutableMarkedRecordPair) links.get(j);
				if (mrp.getMarkedDecision() != Decision.DIFFER) {
					return true;
				}
			}
		}
		
		return false;
	}
}
