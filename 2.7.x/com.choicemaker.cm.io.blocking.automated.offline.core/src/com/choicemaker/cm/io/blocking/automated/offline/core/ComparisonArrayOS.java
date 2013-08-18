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
import java.util.Arrays;
import java.util.Random;

import org.apache.log4j.Logger;

/**
 * This object contains a group of record IDs belonging to a block that need to be compared against
 * all the other record IDs in the group.
 * 
 * This represents an oversized block.
 * The oversized blocks are compared as follows:
 * 1.	create a random set, S, consisting of maxBlockSize ids from staging.
 * 2.	the rest of the ids from staging go into set T stage.
 * 3.	master ids go into set T master.
 * 4.	perform round robin comparison on S.
 * 5.	for each element in T stage, compare to all in S.
 * 6.	for each element in T master compare to all in S.
 * 7.	for each T master compare with 4 random T stage.
 * 8.	for each T stage record, compare with its i+1 neighbor and 3 random T records
 * 
 * @author pcheung
 *
 */
public class ComparisonArrayOS extends ComparisonArray {
	
	private static final Logger log = Logger.getLogger(ComparisonArrayOS.class);

	private static final int STEP_4 = 0;
	private static final int STEP_5 = 1;
	private static final int STEP_6 = 2;
	private static final int STEP_7 = 3;
	private static final int STEP_8 = 4;
	
	//this keeps track of in which step of the algorithm we are at.
	private int step = STEP_4;
	
	//This is true if in the step 8, we have already check T stage with its i+1 record.
	private boolean checkedNext = false;
	//the is the actual index of i+1
	private int n; 

	private int maxBlockSize;	
	private Random random;
	private ArrayList S = null;
	private ArrayList TStage = null;
	private int [] randomStage = null;

	/** This constructor takes in a ComparisonArray and an integer representing the maximum block size.
	 * 
	 * @param ca
	 * @param maxBlockSize
	 */
	public ComparisonArrayOS (ComparisonArray ca, int maxBlockSize) {
		this.stagingIDs = ca.stagingIDs;
		this.masterIDs = ca.masterIDs;
		this.stagingIDType = ca.stagingIDType;
		this.masterIDType = ca.masterIDType;
		this.maxBlockSize = maxBlockSize;
		
		init ();
	}
	
	
	/**
	 * This method builds the S, T stage, and T master lists.
	 *
	 */
	private void init () {
		//seed the random number generator
		int s = stagingIDs.size() + masterIDs.size();
		random = new Random (s);

		log.debug("Random " + s);

		//create the S set
		int sSize;
		if (stagingIDs.size() <= maxBlockSize) {
			S = stagingIDs;
			sSize = stagingIDs.size();
			TStage = new ArrayList (0);
		} else {
			S = new ArrayList (maxBlockSize);
			sSize = maxBlockSize;

			TStage = new ArrayList (stagingIDs.size() - sSize);

			//contains indexes of ids in set S.
			int [] sids = getRandomIDs (random, maxBlockSize, stagingIDs.size());
			int current = 0;
			for (int i=0; i< stagingIDs.size(); i++) {
				if (current < maxBlockSize && i == sids[current]) {
					S.add( stagingIDs.get(i));
					current ++;
				} else {
					TStage.add( stagingIDs.get(i));
				}
			}

		}

		s1 = S.size ();

		//special case of S having 1 element
		if (s1 == 1) {
			step = STEP_6;
			sID1 = 0;
			sID2 = 0;
		} 


//		if (TStage.size() <= 4) {
//		} else {
//			randomStage = getRandomIDs (random, 4, TStage.size ());
//		}
		
//		debugArray (S);
//		debugArray (TStage);
	}
	
	/** This method returns a int array of the given size containing random ids from 0 to max.
	 * 
	 * @param size - size of the random array
	 * @param max - maximum number
	 * @return
	 */
	private static int [] getRandomIDs (Random random, int size, int max) {
		int [] list = new int [max];
		int [] list2 = new int [size];
		
		for (int i=0; i<max; i++) {
			list[i] = i;
		}
		
		for (int i=0; i< size; i++) {
			int ind = random.nextInt(max - i);
			list2[i] = list[ind];
			
			//remove ind for list
			for (int j=ind; j < max - i - 1; j ++) {
				list[j] = list[j+1];
			}
		}
		
		Arrays.sort( list2);
		return list2;
	}
	
	
	private ComparisonPair readNext () {
		ComparisonPair ret = null;
		if (step == STEP_4) {
			//round robin on S
			if (sID1 < s1 - 1 && sID2 < s1) {
				ret = new ComparisonPair ();
				ret.id1 = (Comparable) S.get(sID1);
				ret.id2 = (Comparable) S.get(sID2);
				ret.isStage = true;

				log.debug ("Round robin s " + ret.id1.toString() + " " + ret.id2.toString());

				sID2 ++;
				if (sID2 == s1) {
					sID1 ++;
					sID2 = sID1 + 1;
					if (sID1 == s1 - 1) {
						if (TStage.size() > 0) {
							step = STEP_5;
							//getting ready for step 5
							sID1 = 0;
							sID2 = 0;
						} else {
							if (masterIDs.size() > 0) {
								step = STEP_6;
								//getting ready for step 6
								sID1 = 0;
								sID2 = 0;
							} else {
								step = STEP_8;
								//getting ready for step 8
								sID1 = 0;
								sID2 = 0;
							}
						}
					}
				}
			}
		} else if (step == STEP_5) {
			//for each in Tstage, compare to S.
			int s2 = TStage.size();
			if (sID1 < s2 && sID2 < s1) {
				ret = new ComparisonPair ();
				ret.id1 = (Comparable) TStage.get(sID1);
				ret.id2 = (Comparable) S.get(sID2);
				ret.isStage = true;
					
				log.debug ("TStage with S " + ret.id1.toString() + " " + ret.id2.toString());

				sID2++;
				if (sID2 == s1) {
					sID1 ++;
					sID2 = 0;
					if (sID1 == s2) {
						if (masterIDs.size() > 0) {
							step = STEP_6;
							//getting ready for step 6
							sID1 = 0;
							sID2 = 0;
						} else {
							step = STEP_8;
							//getting ready for step 8
							sID1 = 0;
							sID2 = 0;
						}
					}
				}
			}
		} else if (step == STEP_6) {
			//for each in Tmaster, compare to S.
			int s2 = masterIDs.size();
			if (sID1 < s2 && sID2 < s1) {
				ret = new ComparisonPair ();
				ret.id1 = (Comparable) S.get(sID2);
				ret.id2 = (Comparable) masterIDs.get(sID1);
				ret.isStage = false;

				log.debug ("TMaster with S " + ret.id1.toString() + " " + ret.id2.toString());

				sID2++;
				if (sID2 == s1) {
					sID1 ++;
					sID2 = 0;
					if (sID1 == s2) {
						step = STEP_7;
						//getting ready for step 7
						sID1 = 0;
						sID2 = 0;
					}
				}
			}
			
		} else if (step == STEP_7) {
			//for each in Tmaster, compare to 4 random in Tstage.
			int s1 = TStage.size();
			int s2 = masterIDs.size();
			if (s1 <= 4) {
				//compare with all TStage
				if (sID1 < s2 && sID2 < s1) {
					ret = new ComparisonPair ();
					ret.id1 = (Comparable) TStage.get(sID2);
					ret.id2 = (Comparable) masterIDs.get(sID1);
					ret.isStage = false;
					
					log.debug ("TMaster random " + ret.id1.toString() + " " + ret.id2.toString());

					sID2++;
					if (sID2 == s1) {
						sID1 ++;
						sID2 = 0;
						if (sID1 == s2) {
							step = STEP_8;
							//getting ready for step 8
							sID1 = 0;
							sID2 = 0;
						}
					}
				}
			} else {
				//compare with 4 random from TStage
				if (randomStage == null) randomStage = getRandomIDs (random, 4, s1);
				
				if (sID1 < s2 && sID2 < 4) {
					ret = new ComparisonPair ();
					ret.id1 = (Comparable) TStage.get(randomStage[sID2]);
					ret.id2 = (Comparable) masterIDs.get(sID1);
					ret.isStage = false;
					
					log.debug ("TMaster random " + ret.id1.toString() + " " + ret.id2.toString());

					sID2 ++;
					if (sID2 == 4) {
						sID1 ++;
						sID2 = 0;
						randomStage = null;
						if (sID1 == s2) {
							step = STEP_8;
							//getting ready for step 8
							sID1 = 0;
							sID2 = 0;
							mID1 = 0;
						}
					}
				}
			}
		}else if (step == STEP_8) {
			//for each in Tstage, compare to i+1 and 3 random
			int s1 = TStage.size();
			int s2 = masterIDs.size();
			
			if (sID1 < s1 - 1 && mID1 < 3) {
//				if (!checkedNext && s1 > 1) {
				if (!checkedNext) {
					n = sID1 + 1;
					if (sID1 ==  s1 - 1) n = 0;
					
					ret = new ComparisonPair ();
					ret.id1 = (Comparable) TStage.get(sID1);
					ret.id2 = (Comparable) TStage.get(n);
					ret.isStage = true;
					
					log.debug ("TStage random i+1 " + ret.id1.toString() + " " + ret.id2.toString());

					checkedNext = true;
					
					if (s1+s2 <=4) {
						sID1 ++;
						checkedNext = false;
					}
					
				} else {
					if (s1 + s2 > 4) {
						if (randomStage == null) randomStage = getRandomIDs (random, 5, s1 + s2);
					
						if (sID1 == randomStage[sID2] || n == randomStage[sID2]) sID2 ++;
						
						ret = new ComparisonPair ();
						ret.id1 = (Comparable) TStage.get(sID1);
						
						if (randomStage[sID2] >= s1) {
							ret.id2 = (Comparable) masterIDs.get(randomStage[sID2] - s1);
							ret.isStage = false;
						} else {
							ret.id2 = (Comparable) TStage.get(randomStage[sID2]);
							ret.isStage = true;
						}
						
						log.debug ("TStage random " + ret.id1.toString() + " " + ret.id2.toString());

						sID2 ++;
						mID1 ++;
						if (mID1 == 3) {
							sID1 ++;
							sID2 = 0;
							mID1 = 0;
							checkedNext = false;
							randomStage = null;
						}
					} //end if s1+s2>4

				}
			}
		} //end if step
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

}
