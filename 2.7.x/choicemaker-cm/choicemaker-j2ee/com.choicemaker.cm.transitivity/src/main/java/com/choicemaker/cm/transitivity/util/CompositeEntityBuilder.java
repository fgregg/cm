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
package com.choicemaker.cm.transitivity.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.transitivity.core.CompositeEntity;
import com.choicemaker.cm.transitivity.core.Link;
import com.choicemaker.cm.transitivity.core.TransitivityException;

/**
 * This object takes a IMatchRecord2Source or a List of MatchRecord2
 * and creates an Iterator of CompositeEntity.
 * 
 * It goes through the matches and group those that are related into the same 
 * CompositeEntity.
 * 
 * @author pcheung
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class CompositeEntityBuilder {

	/* Map of record ID to CompositeEntity.  
	 * Many different ID's can map to the same cluster.
	 * 
	 */
	private HashMap clusterMap;
	
	private IMatchRecord2Source source;

	//List of MatchRecord2
	private List matches;
	

	/** This constructor takes in a IMatchRecord2Source from which to build
	 * CompositeEntities.
	 * 
	 * @param source
	 */

	public CompositeEntityBuilder(IMatchRecord2Source source) {
		clusterMap = new HashMap();
		this.source = source;
	}
	
	
	/** This constructor takes in a list MatchRecord2.
	 * 
	 * @param matches
	 */
	public CompositeEntityBuilder (List matches) {
		clusterMap = new HashMap();
		this.matches = matches;
	}
	
	
	
	/**
	 * This method links all the MatchRecord2 objects and returns an Iterator 
	 * of CompositeEntity.
	 * 
	 * @return Iterator - an Iterator of CompositeEntity
	 * @throws TransitivityException
	 */
	public Iterator getCompositeEntities () throws TransitivityException {
		if (source != null) {
			try {
				source.open();

				while (source.hasNext()) {
					MatchRecord2 mr = source.getNext();
					link (mr);
				}
		
				source.close();
		
			} catch (BlockingException e) {
				throw new TransitivityException (e.toString());
			}
		
			TreeSet set =  new TreeSet (clusterMap.values());
			return set.iterator();

		} else {
			int s = matches.size();
			for (int i=0; i<s; i++) {
				MatchRecord2 mr = (MatchRecord2) matches.get(i);
				link (mr);
			}
			TreeSet set =  new TreeSet (clusterMap.values());
			return set.iterator();
		}
		
	}
	
	/** This returns the first ID of MatchRecord2 with source of STAGE.
	 * 
	 * @param mr
	 */
	private RecordID getID1 (MatchRecord2 mr) {
		return new RecordID (mr.getRecordID1 (), MatchRecord2.STAGE_SOURCE);		
	}

	private RecordID getID2 (MatchRecord2 mr) {
		return new RecordID (mr.getRecordID2 (), mr.getRecord2Source());		
	}


	/* This method puts record IDs c1 and c2 of MatchRecord2 into the same cluster.
	 * 
	 * 1.	If c1 == c2 or c1 == null, or c2 == null, it throws an exception.  This 
	 * 		should never happen.  When this happens, it means that there is something 
	 * 		wrong with IMatchRecord2Source or the OABA.
	 * 
	 * 2.	Get the clusters for these two record ID's.
	 * 	A.	If these clusters are null, then create a new cluster and map to these two ids.
	 * 	B.	If one is null, link the new id to existing cluster.
	 *  C.	If neither is null, then merge them and 
	 * 
	 * @param mr - MatchRecord2 object
	 */
	private void link (MatchRecord2 mr) {
		Comparable c1 = getID1(mr);
		Comparable c2 = getID2(mr);
		
		if (c1 == null) {
			throw new IllegalArgumentException("Record id1 is null.");
		} else if (c2 == null) {
			throw new IllegalArgumentException("Record id2 is null.");
		} else if (c2.equals(c1)) {
			throw new IllegalArgumentException("Record id1 and id2 are the same. "+
			"c1 = " + c1.toString());
		}
		
		CompositeEntity ent1 = (CompositeEntity) clusterMap.get(c1);
		CompositeEntity ent2 = (CompositeEntity) clusterMap.get(c2);
		
		if (ent1 == null && ent2 == null) {
			createNew (mr);
		} else if (ent1 == null) {
			addToCluster (ent2, mr);
		} else if (ent2 == null) {
			addToCluster (ent1, mr);
		} else if (ent1 == ent2) {
			//this happens when we add an edge to an existing 
			addToCluster (ent1, mr);
		} else {
			mergeClusters (ent1, ent2, mr);
		}
		
	}


	/* This creates a new cluster and map them to the two record IDs.
	 * 
	 */
	private void createNew (MatchRecord2 mr) {
		Comparable c1 = getID1(mr);
		Comparable c2 = getID2(mr);

		UniqueSequence seq = UniqueSequence.getInstance ();
		CompositeEntity ce = new CompositeEntity (seq.getNextInteger());
		ce.addMatchRecord(mr);

		clusterMap.put(c1, ce);		
		clusterMap.put(c2, ce);		
	}


	/* This method add a MatchRecord2 to an existing cluster.
	 * 
	 */
	private void addToCluster (CompositeEntity ent, MatchRecord2 mr) {
		ent.addMatchRecord(mr);

		Comparable c1 = getID1(mr);
		Comparable c2 = getID2(mr);

		clusterMap.put(c1, ent);
		clusterMap.put(c2, ent);
	}


	/* This method merges two clusters by takes all the MatchRecords from the second one
	 * and adding them to the first one.  Finally, it also adds the input MatchRecord2.
	 * 
	 */
	private void mergeClusters (CompositeEntity ce1, CompositeEntity ce2, 
		MatchRecord2 mr) {
			
		//add the current to the first.
		ce1.addMatchRecord(mr);
		Comparable c1 = getID1(mr);
		Comparable c2 = getID2(mr);
		clusterMap.put(c1, ce1);
		clusterMap.put(c2, ce1);
		
		//add all the MatchRecord2 from the second to the first,
		List links = ce2.getAllLinks();
		for (int i=0; i<links.size(); i++) {
			Link link = (Link) links.get(i);
			
			List mrs = link.getLinkDefinition();
			for (int j=0; j<mrs.size(); j++) {
				MatchRecord2 mr2 = (MatchRecord2) mrs.get(j);
				ce1.addMatchRecord(mr2);

				//map cluster1 to the IDs
				Comparable c = getID1(mr2);
				clusterMap.put(c, ce1);
				c = getID2(mr2);
				clusterMap.put(c, ce1);
			}
		}
		
		//free up memory
		ce2 = null;
	}


}
