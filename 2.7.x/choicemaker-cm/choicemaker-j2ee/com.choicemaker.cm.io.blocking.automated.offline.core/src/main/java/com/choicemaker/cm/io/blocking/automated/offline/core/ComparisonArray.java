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
 * This object contains a group of record IDs belonging to a block that need to
 * be compared against all the other record IDs in the group.
 * 
 * @author pcheung
 *
 */
public class ComparisonArray<T extends Comparable<T>> implements
		IComparisonSet<T> {

	private static final long serialVersionUID = 1L;
	protected List<T> stagingIDs;
	protected List<T> masterIDs;

	// the following 2 variables indicate the data type of the record IDs.
	protected RECORD_ID_TYPE stagingIDType;
	protected RECORD_ID_TYPE masterIDType;

	protected int s1 = 0;
	protected int s2 = 0;

	// These keep track of what pairs to compare
	protected int sID1 = 0;
	protected int sID2 = 1;
	protected int mID1 = 0;
	protected int mID2 = 0;
	protected ComparisonPair<T> nextPair;

	/**
	 * This constructor takes these parameters.
	 * 
	 * @param stageIDs
	 *            - a list of staging IDs
	 * @param masterIDs
	 *            - a list of mater IDs
	 * @param stageType
	 *            - Type of stage ID, could be Contants.TYPE_LONG, or
	 *            TYPE_INTEGER or TYPE_STRING
	 * @param masterType
	 *            - Type of master ID, could be Contants.TYPE_LONG, or
	 *            TYPE_INTEGER or TYPE_STRING
	 */
	public ComparisonArray(List<T> stageIDs, List<T> masterIDs,
			RECORD_ID_TYPE stageType, RECORD_ID_TYPE masterType) {
		this.stagingIDs = stageIDs;
		this.masterIDs = masterIDs;
		this.stagingIDType = stageType;
		this.masterIDType = masterType;
		this.s1 = stagingIDs.size();
		this.s2 = masterIDs.size();
	}

	protected ComparisonArray() {
	}

	/**
	 * This returns the total size of the ComparisonGroup, which is the number
	 * of elements in staging and the number of elements in master.
	 * 
	 * @return int
	 */
	public int size() {
		return stagingIDs.size() + masterIDs.size();
	}

	/**
	 * This returns the ith record id. i must be between 0 and size ()-1. If i
	 * &lt; stagingIDS.size (), return stagingIDs.get(i). if i &gt;=
	 * stagingIDS.size (), return masterIDs.get (i - stageingIDs.size()).
	 * 
	 * @param i
	 * @return Comparable
	 */
	public T get(int i) {
		if (i < 0 || i >= size())
			throw new IllegalArgumentException("invalid i = " + i);

		if (i < stagingIDs.size())
			return stagingIDs.get(i);
		else
			return masterIDs.get(i - stagingIDs.size());
	}

	/**
	 * This returns the type of staging record IDs.
	 * 
	 * @return int
	 */
	public RECORD_ID_TYPE getStagingIDsType() {
		return stagingIDType;
	}

	/**
	 * This returns the type of master record IDs.
	 * 
	 * @return int
	 */
	public RECORD_ID_TYPE getMasterIDsType() {
		return masterIDType;
	}

	/**
	 * This method returns the staging ids in this comparison group.
	 * 
	 * @return ArrayList
	 */
	public List<T> getStagingIDs() {
		return stagingIDs;
	}

	/**
	 * This method returns the master ids in this comparison group.
	 * 
	 * @return ArrayList
	 */
	public List<T> getMasterIDs() {
		return masterIDs;
	}

	public boolean equals(ComparisonArray<T> cg) {
		boolean ret = false;

		if (cg != null) {
			if (this.stagingIDType == cg.getStagingIDsType()
					&& this.masterIDType == cg.getMasterIDsType()) {
				if (compareArrays(stagingIDs, cg.getStagingIDs())
						&& compareArrays(masterIDs, cg.getMasterIDs()))
					ret = true;
			}
		}

		return ret;
	}

	/**
	 * This returns true if both arrays have the same comparable elements in the
	 * same order.
	 * 
	 * @param A1
	 * @param A2
	 * @return boolean
	 */

	private static <T extends Comparable<T>> boolean compareArrays(List<T> A1,
			List<T> A2) {
		boolean ret = false;
		int s1 = A1.size();
		int s2 = A2.size();

		if (s1 == s2) {
			Collections.sort(A1);
			Collections.sort(A2);

			int i = 0;
			boolean match = true;
			while (match && (i < s1)) {
				Comparable<?> c1 = A1.get(i);
				Comparable<?> c2 = A2.get(i);
				if (!c1.equals(c2))
					match = false;
				i++;
			}
			ret = match;
		}

		return ret;
	}

	protected static <T extends Comparable<T>> void debugArray(List<T> A) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < A.size(); i++) {
			sb.append(A.get(i));
			sb.append(' ');
		}
	}

	@Override
	public int hashCode() {
		int ret = 0;
		for (int i = 0; i < stagingIDs.size(); i++) {
			Comparable<?> c = stagingIDs.get(i);
			ret += c.hashCode();
		}
		return ret;
	}

	private ComparisonPair<T> readNext() {
		ComparisonPair<T> ret = null;
		if (sID1 < s1) {
			if (sID2 < s1) {
				// compare stage with stage
				ret = new ComparisonPair<T>();
				ret.setId1(stagingIDs.get(sID1));
				ret.setId2(stagingIDs.get(sID2));
				ret.isStage = true;

				sID2++;
				if (sID2 == s1) {
					if (s2 == 0) {
						// no master, so compare the stages
						sID1++;
						sID2 = sID1 + 1;
					}
				}
			} else {
				// compare with master
				if (mID2 < s2) {
					ret = new ComparisonPair<T>();
					ret.setId1(stagingIDs.get(sID1));
					ret.setId2(masterIDs.get(mID2));
					ret.isStage = false;

					mID2++;
					if (mID2 == s2) {
						sID1++;
						sID2 = sID1 + 1;
						mID2 = 0;
					}
				}
			}
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSet#
	 * hasNextPair()
	 */
	@Override
	public boolean hasNextPair() {
		if (this.nextPair == null) {
			this.nextPair = readNext();
		}
		return this.nextPair != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSet#
	 * getNextPair()
	 */
	@Override
	public ComparisonPair<T> getNextPair() {
		if (this.nextPair == null) {
			this.nextPair = readNext();
		}
		ComparisonPair<T> retVal = this.nextPair;
		this.nextPair = null;

		return retVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSet#
	 * writeDebug()
	 */
	@Override
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

	private StringBuffer writeArray(List<T> a) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < a.size(); i++) {
			Comparable<?> c = a.get(i);
			sb.append(c.toString());
			sb.append(',');
		}
		return sb;
	}

}
