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

	/**
	 * Returns true if both arrays have the same comparable elements in the
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
	private static final long serialVersionUID = 1L;

	private List<T> stagingIDs;
	private List<T> masterIDs;

	// the following 2 variables indicate the data type of the record IDs.
	private RECORD_ID_TYPE stagingIDType;
	private RECORD_ID_TYPE masterIDType;

	private int s1 = 0;
	private int s2 = 0;

	// These keep track of what pairs to compare
	private int sID1 = 0;
	private int sID2 = 1;
	private int mID1 = 0;
	private int mID2 = 0;

	private ComparisonPair<T> nextPair;

	protected ComparisonArray() {
	}

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
		this.setStagingIDs(stageIDs);
		this.setMasterIDs(masterIDs);
		this.setStagingIDType(stageType);
		this.setMasterIDType(masterType);
		this.set_s1(getStagingIDs().size());
		this.set_s2(masterIDs.size());
	}

	public boolean equals(ComparisonArray<T> cg) {
		boolean ret = false;

		if (cg != null) {
			if (this.getStagingIDsType() == cg.getStagingIDsType()
					&& this.getMasterIDsType() == cg.getMasterIDsType()) {
				if (compareArrays(getStagingIDs(), cg.getStagingIDs())
						&& compareArrays(getMasterIDs(), cg.getMasterIDs()))
					ret = true;
			}
		}

		return ret;
	}

	/**
	 * Returns the ith record id. i must be between 0 and size ()-1. If i
	 * &lt; stagingIDS.size (), return stagingIDs.get(i). if i &gt;=
	 * stagingIDS.size (), return masterIDs.get (i - stageingIDs.size()).
	 * 
	 * @param i
	 * @return Comparable
	 */
	public T get(int i) {
		if (i < 0 || i >= size())
			throw new IllegalArgumentException("invalid i = " + i);

		if (i < getStagingIDs().size())
			return getStagingIDs().get(i);
		else
			return getMasterIDs().get(i - getStagingIDs().size());
	}

	protected int get_mID1() {
		return mID1;
	}

	protected int get_mID2() {
		return mID2;
	}

	protected ComparisonPair<T> get_nextPair() {
		return nextPair;
	}

	protected int get_s1() {
		return s1;
	}

	protected int get_s2() {
		return s2;
	}

	protected int get_sID1() {
		return sID1;
	}

	protected int get_sID2() {
		return sID2;
	}

	/**
	 * Returns the master ids in this comparison group.
	 * 
	 * @return ArrayList
	 */
	public List<T> getMasterIDs() {
		return masterIDs;
	}

	/**
	 * Returns the type of master record IDs.
	 * 
	 * @return int
	 */
	public RECORD_ID_TYPE getMasterIDsType() {
		return masterIDType;
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
		if (this.get_nextPair() == null) {
			this.set_nextPair(readNext());
		}
		ComparisonPair<T> retVal = this.get_nextPair();
		this.set_nextPair(null);

		return retVal;
	}

	/**
	 * Returns the staging ids in this comparison group.
	 * 
	 * @return ArrayList
	 */
	public List<T> getStagingIDs() {
		return stagingIDs;
	}

	/**
	 * Returns the type of staging record IDs.
	 * 
	 * @return int
	 */
	public RECORD_ID_TYPE getStagingIDsType() {
		return stagingIDType;
	}

	@Override
	public int hashCode() {
		int ret = 0;
		for (int i = 0; i < getStagingIDs().size(); i++) {
			Comparable<?> c = getStagingIDs().get(i);
			ret += c.hashCode();
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
		if (this.get_nextPair() == null) {
			this.set_nextPair(readNext());
		}
		return this.get_nextPair() != null;
	}

	private ComparisonPair<T> readNext() {
		ComparisonPair<T> ret = null;
		if (get_sID1() < get_s1()) {
			if (get_sID2() < get_s1()) {
				// compare stage with stage
				ret = new ComparisonPair<T>();
				ret.setId1(getStagingIDs().get(get_sID1()));
				ret.setId2(getStagingIDs().get(get_sID2()));
				ret.isStage = true;

				set_sID2(get_sID2() + 1);
				if (get_sID2() == get_s1()) {
					if (get_s2() == 0) {
						// no master, so compare the stages
						set_sID1(get_sID1() + 1);
						set_sID2(get_sID1() + 1);
					}
				}
			} else {
				// compare with master
				if (get_mID2() < get_s2()) {
					ret = new ComparisonPair<T>();
					ret.setId1(getStagingIDs().get(get_sID1()));
					ret.setId2(getMasterIDs().get(get_mID2()));
					ret.isStage = false;

					set_mID2(get_mID2() + 1);
					if (get_mID2() == get_s2()) {
						set_sID1(get_sID1() + 1);
						set_sID2(get_sID1() + 1);
						set_mID2(0);
					}
				}
			}
		}
		return ret;
	}

	protected void set_mID1(int mID1) {
		this.mID1 = mID1;
	}

	protected void set_mID2(int mID2) {
		this.mID2 = mID2;
	}

	protected void set_nextPair(ComparisonPair<T> nextPair) {
		this.nextPair = nextPair;
	}

	protected void set_s1(int s1) {
		this.s1 = s1;
	}

	protected void set_s2(int s2) {
		this.s2 = s2;
	}

	protected void set_sID1(int sID1) {
		this.sID1 = sID1;
	}

	protected void set_sID2(int sID2) {
		this.sID2 = sID2;
	}

	protected void setMasterIDs(List<T> masterIDs) {
		this.masterIDs = masterIDs;
	}

	protected void setMasterIDType(RECORD_ID_TYPE masterIDType) {
		this.masterIDType = masterIDType;
	}

	protected void setStagingIDs(List<T> stagingIDs) {
		this.stagingIDs = stagingIDs;
	}

	protected void setStagingIDType(RECORD_ID_TYPE stagingIDType) {
		this.stagingIDType = stagingIDType;
	}

	/**
	 * Returns the total size of the ComparisonGroup, which is the number
	 * of elements in staging and the number of elements in master.
	 * 
	 * @return int
	 */
	public int size() {
		return getStagingIDs().size() + getMasterIDs().size();
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
		sb.append(writeArray(getStagingIDs()));

		sb.append(Constants.LINE_SEPARATOR);
		sb.append("master: ");
		sb.append(writeArray(getMasterIDs()));
		sb.append(Constants.LINE_SEPARATOR);
		return sb.toString();
	}

}
