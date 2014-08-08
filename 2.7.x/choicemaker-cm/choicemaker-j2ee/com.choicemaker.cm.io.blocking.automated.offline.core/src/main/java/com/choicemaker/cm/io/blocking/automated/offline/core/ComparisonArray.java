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

import java.util.Collections;
import java.util.List;

/**
 * This object contains a group of record IDs belonging to a block that need to be compared against
 * all the other record IDs in the group.
 * 
 * @author pcheung
 *
 */
public class ComparisonArray<T extends Comparable<? super T>> implements IComparisonSet<T> {
	
	private static final long serialVersionUID = 1L;
	protected List<T> stagingIDs;
	protected List<T> masterIDs;
	
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
	protected ComparisonPair<T> nextPair;
	
	
	/** This constructor takes these parameters.
	 * 
	 * @param stageIDs - a list of staging IDs
	 * @param masterIDs - a list of mater IDs
	 * @param stageType - Type of stage ID, could be Contants.TYPE_LONG, or TYPE_INTEGER or TYPE_STRING
	 * @param masterType - Type of master ID, could be Contants.TYPE_LONG, or TYPE_INTEGER or TYPE_STRING
	 */
	public ComparisonArray (List<T> stageIDs, List<T> masterIDs, int stageType, int masterType) {
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
	public T get (int i) {
		if (i < 0 || i >= size ()) throw new IllegalArgumentException ("invalid i = " + i);
		
		if (i < stagingIDs.size()) return stagingIDs.get(i);
		else return masterIDs.get(i - stagingIDs.size());
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
	public List<T> getStagingIDs () {
		return stagingIDs;
	}

	/** This method returns the master ids in this comparison group.
	 * 
	 * @return ArrayList
	 */
	public List<T> getMasterIDs () {
		return masterIDs;
	}

	
	@Override
	public int hashCode() {
		final int maxCount = 11;
		final int prime = 31;
		int result = 1;
		result = prime * result + masterIDType;
		int count = 0;
		if (masterIDs != null) {
			for (T id :masterIDs) {
				if (++count > maxCount) {
					break;
				}
				result = prime * result + (id == null ? 0 : id.hashCode());
			}
		}
		count = 0;
		if (stagingIDs != null) {
			for (T id :stagingIDs) {
				if (++count > maxCount) {
					break;
				}
				result = prime * result + (id == null ? 0 : id.hashCode());
			}
		}
		return result;
	}


	/** for testing only */ 
	@SuppressWarnings("unchecked")
	boolean equals_00(Object o) {
		boolean ret = false;

		if (o instanceof ComparisonArray) {
			@SuppressWarnings("rawtypes")
			ComparisonArray cg = (ComparisonArray) o;

			if (this.stagingIDType == cg.getStagingIDsType()
					&& this.masterIDType == cg.getMasterIDsType()) {
				if (compareArrays(stagingIDs, cg.getStagingIDs())
						&& compareArrays(masterIDs, cg.getMasterIDs()))
					ret = true;
			}
		}

		return ret;
	}	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		ComparisonArray other = (ComparisonArray) obj;
		if (masterIDType != other.masterIDType) {
			return false;
		}
		if (masterIDs == null) {
			if (other.masterIDs != null) {
				return false;
			}
		} else if (!masterIDs.equals(other.masterIDs)) {
			return false;
		}
		if (stagingIDType != other.stagingIDType) {
			return false;
		}
		if (stagingIDs == null) {
			if (other.stagingIDs != null) {
				return false;
			}
		} else if (!stagingIDs.equals(other.stagingIDs)) {
			return false;
		}
		return true;
	}


	/**
	 * This returns true if both arrays have the same comparable elements in the
	 * same order.
	 * 
	 * @param A1
	 * @param A2
	 * @return boolean
	 */
	private static <T extends Comparable<? super T>> boolean compareArrays(
			List<T> A1, List<T> A2) {
		boolean ret = false;
		int s1 = A1.size();
		int s2 = A2.size();

		if (s1 == s2) {
			Collections.sort(A1);
			Collections.sort(A2);

			int i = 0;
			boolean match = true;
			while (match && (i < s1)) {
				T c1 = A1.get(i);
				T c2 = A2.get(i);
				if (!c1.equals(c2))
					match = false;
				i++;
			}
			ret = match;
		}

		return ret;
	}	
	

	private ComparisonPair<T> readNext () {
		ComparisonPair<T> ret = null;
		if (sID1 <  s1) {
			if (sID2 < s1) {
				//compare stage with stage
				ret = new ComparisonPair<>();
				ret.id1 = stagingIDs.get(sID1);
				ret.id2 = stagingIDs.get(sID2);
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
					ret = new ComparisonPair<>();
					ret.id1 = stagingIDs.get(sID1);
					ret.id2 = masterIDs.get(mID2);
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
	public ComparisonPair<T> getNextPair() {
		if (this.nextPair == null) {
			this.nextPair = readNext();
		}
		ComparisonPair<T> retVal = this.nextPair;
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
	
	
	private StringBuffer writeArray (List<T> a) {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<a.size(); i++) {
			T c = a.get(i);
			sb.append(c.toString());
			sb.append(',');
		}
		return sb;
	}

}
