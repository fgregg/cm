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
package com.choicemaker.cm.io.blocking.automated.offline.core;

import java.util.ArrayList;
import java.util.Collections;

/**
 * This object contains a group of record IDs belonging to a block that need to be compared against
 * all the other record IDs in the group.
 * 
 * @author pcheung
 *
 */
public class ComparisonArray implements IComparisonSet {
	
	private static final long serialVersionUID = 1L;
	protected ArrayList stagingIDs;
	protected ArrayList masterIDs;
	
	//the following 2 variables indicate the data type of the record IDs.
	protected int stagingIDType = 0;
	protected int masterIDType = 0;
	
	protected int s1 = 0;
	protected int s2 = 0;

	//These keep track of what pairs to compare
	protected int sID1 = 0;
	protected int sID2 = 1;
	protected int mID1 = 0;
	protected int mID2 = 0;
	protected ComparisonPair nextPair;
	
	
	/** This constructor takes these parameters.
	 * 
	 * @param stageIDs - a list of staging IDs
	 * @param masterIDs - a list of mater IDs
	 * @param stageType - Type of stage ID, could be Contants.TYPE_LONG, or TYPE_INTEGER or TYPE_STRING
	 * @param masterType - Type of master ID, could be Contants.TYPE_LONG, or TYPE_INTEGER or TYPE_STRING
	 */
	public ComparisonArray (ArrayList stageIDs, ArrayList masterIDs, int stageType, int masterType) {
		this.stagingIDs = stageIDs;
		this.masterIDs = masterIDs;
		this.stagingIDType = stageType;
		this.masterIDType = masterType;
		this.s1 = stagingIDs.size();
		this.s2 =  masterIDs.size();
	}
	
	
	protected ComparisonArray () {
	}
	
	
	/** This returns the total size of the ComparisonGroup, which is the number of elements in
	 * staging and the number of elements in master.
	 * 
	 * @return int
	 */
	public int size () {
		return stagingIDs.size() + masterIDs.size();
	}
	
	
	/** This returns the ith record id.  i must be between 0 and size ()-1.  
	 * If i &lt; stagingIDS.size (), return stagingIDs.get(i).
	 * if i &gt;= stagingIDS.size (), return masterIDs.get (i - stageingIDs.size()).
	 * 
	 * @param i
	 * @return Comparable
	 */
	public Comparable get (int i) {
		if (i < 0 || i >= size ()) throw new IllegalArgumentException ("invalid i = " + i);
		
		if (i < stagingIDs.size()) return (Comparable) stagingIDs.get(i);
		else return (Comparable) masterIDs.get(i - stagingIDs.size());
	}
	
	
	/** This returns the type of staging record IDs.
	 * 
	 * @return int
	 */
	public int getStagingIDsType () {
		return stagingIDType;
	}
	
	
	/** This returns the type of master record IDs.
	 * 
	 * @return int
	 */
	public int getMasterIDsType () {
		return masterIDType;
	}
	
	
	/** This method returns the staging ids in this comparison group.
	 * 
	 * @return ArrayList
	 */
	public ArrayList getStagingIDs () {
		return stagingIDs;
	}

	/** This method returns the master ids in this comparison group.
	 * 
	 * @return ArrayList
	 */
	public ArrayList getMasterIDs () {
		return masterIDs;
	}


	public boolean equals (Object o) {
		boolean ret = false;

		if (o instanceof ComparisonArray) {
			ComparisonArray cg = (ComparisonArray) o;

			if (this.stagingIDType == cg.getStagingIDsType() && this.masterIDType == cg.getMasterIDsType()) {
				if (compareArrays(stagingIDs, cg.getStagingIDs()) && 
					compareArrays(masterIDs, cg.getMasterIDs()) ) ret = true;
			} 
		}
		
		return ret;
	}
	
	
	/** This returns true if both arrays have the same comparable elements in the same order.
	 * 
	 * @param A1
	 * @param A2
	 * @return boolean
	 */

	private static boolean compareArrays (ArrayList A1, ArrayList A2) {
		boolean ret = false;
		int s1 = A1.size();
		int s2 = A2.size ();
		
		if (s1 == s2) {
			Collections.sort (A1);
			Collections.sort (A2);
		
			int i = 0;
			boolean match = true;
			while (match && (i < s1)) {
				Comparable c1 = (Comparable) A1.get(i);
				Comparable c2 = (Comparable) A2.get(i);
				if (!c1.equals(c2)) match = false;
				i ++;
			}
			ret = match;
		}
		
		return ret;
	}
	
	

	protected static void debugArray (ArrayList A) {
		StringBuffer sb = new StringBuffer ();
		for (int i=0; i< A.size(); i++) {
			sb.append(A.get(i));
			sb.append(' ');
		}
	}

		
	
	public int hashCode () {
		int ret = 0;
		for (int i=0; i<stagingIDs.size(); i++) {
			Comparable c = (Comparable) stagingIDs.get(i);
			ret += c.hashCode();
		}
		return ret;
	}
	
	
	private ComparisonPair readNext () {
		ComparisonPair ret = null;
		if (sID1 <  s1) {
			if (sID2 < s1) {
				//compare stage with stage
				ret = new ComparisonPair ();
				ret.id1 = (Comparable) stagingIDs.get(sID1);
				ret.id2 = (Comparable) stagingIDs.get(sID2);
				ret.isStage = true;
				
				sID2 ++;
				if (sID2 == s1) {
					if (s2 == 0){
						//no master, so compare the stages
						sID1 ++;
						sID2 = sID1 + 1;
					}
				}
			} else {
				//compare with master
				if (mID2 < s2) {
					ret = new ComparisonPair ();
					ret.id1 = (Comparable) stagingIDs.get(sID1);
					ret.id2 = (Comparable) masterIDs.get(mID2);
					ret.isStage = false;
					
					mID2 ++;
					if (mID2 == s2) {
						sID1 ++;
						sID2 = sID1 + 1;
						mID2 = 0;
					}
				}
			}
		} 
		return ret;
	}
	


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSet#hasNextPair()
	 */
	public boolean hasNextPair() {
		if (this.nextPair == null) {
			this.nextPair = readNext();
		}
		return this.nextPair != null;
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSet#getNextPair()
	 */
	public ComparisonPair getNextPair() {
		if (this.nextPair == null) {
			this.nextPair = readNext();
		}
		ComparisonPair retVal = this.nextPair;
		this.nextPair = null;

		return retVal;
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSet#writeDebug()
	 */
	public String writeDebug() {
		StringBuffer sb = new StringBuffer();
		sb.append(Constants.LINE_SEPARATOR);
		sb.append("stage: ");
		sb.append(writeArray(stagingIDs));

		sb.append(Constants.LINE_SEPARATOR);
		sb.append("master: ");
		sb.append(writeArray(masterIDs));
		sb.append(Constants.LINE_SEPARATOR);
		return sb.toString();
	}
	
	
	private StringBuffer writeArray (ArrayList a) {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<a.size(); i++) {
			Comparable c = (Comparable) a.get(i);
			sb.append(c.toString());
			sb.append(',');
		}
		return sb;
	}

}
