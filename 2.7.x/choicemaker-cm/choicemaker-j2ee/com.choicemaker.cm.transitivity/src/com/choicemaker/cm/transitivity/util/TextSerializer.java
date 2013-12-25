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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.choicemaker.cm.transitivity.core.CompositeEntity;
import com.choicemaker.cm.transitivity.core.Entity;
import com.choicemaker.cm.transitivity.core.INode;
import com.choicemaker.cm.transitivity.core.TransitivityResult;

/**
 * This object takes a TransitivityResult and a Writer and outputs the clusters as
 * RECORD_ID, MATCH_GROUP_ID, HOLD_GROUP_ID.
 * 
 * @author pcheung
 *
 * ChoiceMaker Technologies, Inc.
 */
public class TextSerializer {
	
	private static final Logger log = Logger.getLogger(TextSerializer.class);

	public static final int SORT_BY_ID = 1;
	public static final int SORT_BY_HOLD_MERGE_ID = 2;
	
	protected static final char DELIMITER = '|';
	protected static final String NEW_LINE = System.getProperty("line.separator");
	
	//counter variables to assign hold and merge group ids.
	private int holdCount = 0;
	private int matchCount = 0;
	
	//This list is created from all the nodes in TransitivityResult.
	//This is memory intensive.
	protected List records;

	protected TransitivityResult result;
	
	protected Writer writer;
	
	protected int sortType;


	/**
	 * Default constructor for sub classes.
	 *
	 */
	protected TextSerializer () {
	}


	/** This constructor takes a TransitivityResult and a Writer.
	 * 
	 * @param result - the object to serialize
	 * @param writer - the output writer
	 */
	public TextSerializer (TransitivityResult result, Writer writer, int sortType) {
		this.result = result;
		this.writer = writer;
		this.records = new ArrayList ();
		this.sortType = sortType;
	}


	/** This method serializes the result to the writer.
	 * 
	 *
	 */
	public void serialize () throws IOException {
		//first get all the record IDs from the clusters.
		Iterator it = result.getNodes();
		while (it.hasNext()) {
			CompositeEntity ce = (CompositeEntity) it.next();
			
			getCompositeEntity (ce);
		}
		
		//second, sort them accordingly
		Object [] recs = handleSort ();
		
		//free memory
		records = null;
		
		//third, write them out.
		writeRecords (recs);
		
		writer.flush();
		writer.close();
		
		//free up memory
		recs = null;
	}
	
	
	/** This method gets the record ID and assign to it match group and hold group ids.
	 * 
	 * @param ce
	 * @throws IOException
	 */
	protected void getCompositeEntity (CompositeEntity ce) {
		holdCount ++;
		
		StringBuffer sb = new StringBuffer ();
		
		List children = ce.getChildren();
		int s = children.size();
		for (int i=0; i<s; i++) {
			INode node = (INode) children.get(i);
			if (node instanceof Entity) {
				Record r = new Record ();
				r.id = node.getNodeId();
				r.mergeGroupId = 0;
				r.holdGroupId = holdCount;
				records.add(r);
			} else if (node instanceof CompositeEntity) {
				matchCount ++;
				List children2 = node.getChildren();
				int s2 = children2.size();
				for (int j=0; j<s2; j++) {
					INode node2 = (INode) children2.get(j);
					if (node2 instanceof Entity) {
						Record r = new Record ();
						r.id = node2.getNodeId();
						r.mergeGroupId = matchCount;
						r.holdGroupId = holdCount;
						records.add(r);						
					} else {
						throw new IllegalArgumentException ("Does not support CompositeEntity within a CompositeEntity.");
					}
				}
				
			} else {
				throw new IllegalArgumentException ("Unknown node: " + node.toString());
			}
		}
	}
	
	
	protected Object [] handleSort () {
		Object [] recs = records.toArray();

		Comparator sort = null;
		if (sortType == SORT_BY_ID) sort = new SortByID();
		else if (sortType == SORT_BY_HOLD_MERGE_ID) sort = new SortByHoldMergeID ();
		else throw new IllegalArgumentException ("sortType " + sortType + " is not supported.");

		Arrays.sort (recs, sort);
		return recs;
	}


	private void writeRecords (Object [] recs) throws IOException {
		int s = recs.length;
		for (int i=0; i<s; i++) {
			Record r = (Record) recs[i];
			writer.write(printRecord(r));
		}
	}


	protected String printRecord (Record r) {
		StringBuffer sb = new StringBuffer ();
		sb.append (r.id.toString());
		sb.append (DELIMITER);
		sb.append (r.mergeGroupId);
		sb.append (DELIMITER);
		sb.append (r.holdGroupId);
		sb.append (NEW_LINE);
		return sb.toString();
	}




	/** This private inner class holds the record id, merge group id, and hold group id.
	 * 
	 * @author pcheung
	 *
	 * ChoiceMaker Technologies, Inc.
	 */
	protected class Record implements Comparable{
		protected Comparable id;
		protected int mergeGroupId;
		protected int holdGroupId;
		
		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(Object o) {
			Record r = (Record) o;
			return this.id.compareTo(r.id);
		}
	}
	
	
	/** This private inner class sorts the records by id.
	 * 
	 * @author pcheung
	 *
	 * ChoiceMaker Technologies, Inc.
	 */
	private class SortByID implements Comparator {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			Record r1 = (Record) o1;
			Record r2 = (Record) o2;

			return r1.id.compareTo(r2.id);
		}
	}


	/** This private inner class sorts the records by hold group, merge group, and id.
	 * 
	 * @author pcheung
	 *
	 * ChoiceMaker Technologies, Inc.
	 */
	private class SortByHoldMergeID implements Comparator {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			Record r1 = (Record) o1;
			Record r2 = (Record) o2;

			if (r1.holdGroupId < r2.holdGroupId) return -1;
			else if (r1.holdGroupId > r2.holdGroupId) return 1;
			else {
				if (r1.mergeGroupId < r2.mergeGroupId) return -1;
				else if (r1.mergeGroupId > r2.mergeGroupId) return 1;
				else {
					return r1.id.compareTo(r2.id);
				}
			}
		}
	}

}
