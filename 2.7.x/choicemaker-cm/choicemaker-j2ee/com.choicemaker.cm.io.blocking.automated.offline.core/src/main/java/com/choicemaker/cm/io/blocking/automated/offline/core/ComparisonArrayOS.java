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
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

/**
 * This object contains a group of record IDs belonging to a block that need to
 * be compared against all the other record IDs in the group.
 * 
 * This represents an oversized block. The oversized blocks are compared as
 * follows:
 * <ul>
 * <li>STEP 1. create a random set, S, consisting of maxBlockSize ids from
 * staging.</li>
 * <li>STEP 2. the rest of the ids from staging go into set TStage.</li>
 * <li>STEP 3. master ids go into set TMaster.</li>
 * <li>STEP 4. perform round robin comparison on S.</li>
 * <li>STEP 5. for each element in TStage, compare to all in S.</li>
 * <li>STEP 6. for each element in TMaster compare to all in S.</li>
 * <li>STEP 7. for each TMaster compare with 4 random TStage.</li>
 * <li>STEP 8. for each TStage record, compare with its i+1 neighbor and 3
 * random T records</li>
 * </ul>
 * 
 * @author pcheung
 *
 */
public class ComparisonArrayOS<T extends Comparable<T>> extends
		ComparisonArray<T> {

	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(ComparisonArrayOS.class
			.getName());

	private static final int STEP_4 = 0;
	private static final int STEP_5 = 1;
	private static final int STEP_6 = 2;
	private static final int STEP_7 = 3;
	private static final int STEP_8 = 4;

	// this keeps track of in which step of the algorithm we are at.
	private int step = STEP_4;

	// This is true if in the step 8, we have already check TStage with its i+1
	// record.
	private boolean checkedNext = false;
	// the is the actual index of i+1
	private int n;

	private int maxBlockSize;
	private Random random;
	private List<T> S = null;
	private List<T> TStage = null;
	private int[] randomStage = null;

	/**
	 * This constructor takes in a ComparisonArray and an integer representing
	 * the maximum block size.
	 * 
	 * @param ca
	 * @param maxBlockSize
	 */
	public ComparisonArrayOS(ComparisonArray<T> ca, int maxBlockSize) {
		this.setStagingIDs(ca.getStagingIDs());
		this.setMasterIDs(ca.getMasterIDs());
		this.setStagingIDType(ca.getStagingIDsType());
		this.setMasterIDType(ca.getMasterIDsType());
		this.maxBlockSize = maxBlockSize;

		init();
	}

	/**
	 * This method builds the S, TStage, and TMaster lists.
	 *
	 */
	private void init() {
		// seed the random number generator
		int s = getStagingIDs().size() + getMasterIDs().size();
		random = new Random(s);

		log.fine("Random " + s);

		// create the S set
		int sSize;
		if (getStagingIDs().size() <= maxBlockSize) {
			S = getStagingIDs();
			sSize = getStagingIDs().size();
			TStage = new ArrayList<T>(0);
		} else {
			S = new ArrayList<T>(maxBlockSize);
			sSize = maxBlockSize;

			TStage = new ArrayList<T>(getStagingIDs().size() - sSize);

			// contains indexes of ids in set S.
			int[] sids =
				getRandomIDs(random, maxBlockSize, getStagingIDs().size());
			int current = 0;
			for (int i = 0; i < getStagingIDs().size(); i++) {
				if (current < maxBlockSize && i == sids[current]) {
					S.add(getStagingIDs().get(i));
					current++;
				} else {
					TStage.add(getStagingIDs().get(i));
				}
			}

		}

		set_s1(S.size());

		// special case of S having 1 element
		if (get_s1() == 1) {
			step = STEP_6;
			set_sID1(0);
			set_sID2(0);
		}

		// if (TStage.size() <= 4) {
		// } else {
		// randomStage = getRandomIDs (random, 4, TStage.size ());
		// }

		// debugArray (S);
		// debugArray (TStage);
	}

	/**
	 * This method returns a int array of the given size containing random ids
	 * from 0 to max.
	 * 
	 * @param size
	 *            - size of the random array
	 * @param max
	 *            - maximum number
	 * @return
	 */
	private static int[] getRandomIDs(Random random, int size, int max) {
		int[] list = new int[max];
		int[] list2 = new int[size];

		for (int i = 0; i < max; i++) {
			list[i] = i;
		}

		for (int i = 0; i < size; i++) {
			int ind = random.nextInt(max - i);
			list2[i] = list[ind];

			// remove ind for list
			for (int j = ind; j < max - i - 1; j++) {
				list[j] = list[j + 1];
			}
		}

		Arrays.sort(list2);
		return list2;
	}

	private ComparisonPair<T> readNext() {
		ComparisonPair<T> ret = null;
		try {
			if (step == STEP_4) {
				// round robin on S
				if (get_sID1() < get_s1() - 1 && get_sID2() < get_s1()) {
					ret = new ComparisonPair<T>();
					ret.setId1(S.get(get_sID1()));
					ret.setId2(S.get(get_sID2()));
					ret.isStage = true;

					log.fine("Round robin s " + ret.getId1().toString() + " "
							+ ret.getId2().toString());

					set_sID2(get_sID2() + 1);
					if (get_sID2() == get_s1()) {
						set_sID1(get_sID1() + 1);
						set_sID2(get_sID1() + 1);
						if (get_sID1() == get_s1() - 1) {
							if (TStage.size() > 0) {
								step = STEP_5;
								// getting ready for step 5
								set_sID1(0);
								set_sID2(0);
							} else {
								if (getMasterIDs().size() > 0) {
									step = STEP_6;
									// getting ready for step 6
									set_sID1(0);
									set_sID2(0);
								} else {
									step = STEP_8;
									// getting ready for step 8
									set_sID1(0);
									set_sID2(0);
								}
							}
						}
					}
				}
			} else if (step == STEP_5) {
				// for each in TStage, compare to S.
				int s2 = TStage.size();
				if (get_sID1() < s2 && get_sID2() < get_s1()) {
					ret = new ComparisonPair<T>();
					ret.setId1(TStage.get(get_sID1()));
					ret.setId2(S.get(get_sID2()));
					ret.isStage = true;

					log.fine("TStage with S " + ret.getId1().toString() + " "
							+ ret.getId2().toString());

					set_sID2(get_sID2() + 1);
					if (get_sID2() == get_s1()) {
						set_sID1(get_sID1() + 1);
						set_sID2(0);
						if (get_sID1() == s2) {
							if (getMasterIDs().size() > 0) {
								step = STEP_6;
								// getting ready for step 6
								set_sID1(0);
								set_sID2(0);
							} else {
								step = STEP_8;
								// getting ready for step 8
								set_sID1(0);
								set_sID2(0);
							}
						}
					}
				}
			} else if (step == STEP_6) {
				// for each in Tmaster, compare to S.
				int s2 = getMasterIDs().size();
				if (get_sID1() < s2 && get_sID2() < get_s1()) {
					ret = new ComparisonPair<T>();
					ret.setId1(S.get(get_sID2()));
					ret.setId2(getMasterIDs().get(get_sID1()));
					ret.isStage = false;

					log.fine("TMaster with S " + ret.getId1().toString() + " "
							+ ret.getId2().toString());

					set_sID2(get_sID2() + 1);
					if (get_sID2() == get_s1()) {
						set_sID1(get_sID1() + 1);
						set_sID2(0);
						if (get_sID1() == s2) {
							step = STEP_7;
							// getting ready for step 7
							set_sID1(0);
							set_sID2(0);
						}
					}
				}

			} else if (step == STEP_7) {
				// for each in Tmaster, compare to 4 random in TStage.
				int s1 = TStage.size();
				int s2 = getMasterIDs().size();
				if (s1 <= 4) {
					// compare with all TStage
					if (get_sID1() < s2 && get_sID2() < s1) {
						ret = new ComparisonPair<T>();
						ret.setId1(TStage.get(get_sID2()));
						ret.setId2(getMasterIDs().get(get_sID1()));
						ret.isStage = false;

						log.fine("TMaster random " + ret.getId1().toString()
								+ " " + ret.getId2().toString());

						set_sID2(get_sID2() + 1);
						if (get_sID2() == s1) {
							set_sID1(get_sID1() + 1);
							set_sID2(0);
							if (get_sID1() == s2) {
								step = STEP_8;
								// getting ready for step 8
								set_sID1(0);
								set_sID2(0);
							}
						}
					}
				} else {
					// compare with 4 random from TStage
					if (randomStage == null)
						randomStage = getRandomIDs(random, 4, s1);

					if (get_sID1() < s2 && get_sID2() < 4) {
						ret = new ComparisonPair<T>();
						ret.setId1(TStage.get(randomStage[get_sID2()]));
						ret.setId2(getMasterIDs().get(get_sID1()));
						ret.isStage = false;

						log.fine("TMaster random " + ret.getId1().toString()
								+ " " + ret.getId2().toString());

						set_sID2(get_sID2() + 1);
						if (get_sID2() == 4) {
							set_sID1(get_sID1() + 1);
							set_sID2(0);
							randomStage = null;
							if (get_sID1() == s2) {
								step = STEP_8;
								// getting ready for step 8
								set_sID1(0);
								set_sID2(0);
								set_mID1(0);
							}
						}
					}
				}
			} else if (step == STEP_8) {
				// for each in TStage, compare to i+1 and 3 random
				int s1 = TStage.size();
				int s2 = getMasterIDs().size();

				if (get_sID1() < s1 - 1 && get_mID1() < 3) {
					// if (!checkedNext && s1 > 1) {
					if (!checkedNext) {
						n = get_sID1() + 1;
						if (get_sID1() == s1 - 1)
							n = 0;

						ret = new ComparisonPair<T>();
						ret.setId1(TStage.get(get_sID1()));
						ret.setId2(TStage.get(n));
						ret.isStage = true;

						log.fine("TStage random i+1 " + ret.getId1().toString()
								+ " " + ret.getId2().toString());

						checkedNext = true;

						if (s1 + s2 <= 4) {
							set_sID1(get_sID1() + 1);
							checkedNext = false;
						}

					} else {
						if (s1 + s2 > 4) {
							if (randomStage == null)
								randomStage = getRandomIDs(random, 5, s1 + s2);

							if (get_sID1() == randomStage[get_sID2()]
									|| n == randomStage[get_sID2()])
								set_sID2(get_sID2() + 1);

							ret = new ComparisonPair<T>();
							ret.setId1(TStage.get(get_sID1()));

							if (randomStage[get_sID2()] >= s1) {
								ret.setId2(getMasterIDs().get(
										randomStage[get_sID2()] - s1));
								ret.isStage = false;
							} else {
								ret.setId2(TStage.get(randomStage[get_sID2()]));
								ret.isStage = true;
							}

							log.fine("TStage random " + ret.getId1().toString()
									+ " " + ret.getId2().toString());

							set_sID2(get_sID2() + 1);
							set_mID1(get_mID1() + 1);
							if (get_mID1() == 3) {
								set_sID1(get_sID1() + 1);
								set_sID2(0);
								set_mID1(0);
								checkedNext = false;
								randomStage = null;
							}
						} // end if s1+s2>4

					}
				}
			} // end if step
		} catch (Exception x) {
			String msg =
				"ComparisonArrayOS.readNext() failed: " + x + this.dump();
			log.severe(msg);
			throw x;
		}
		return ret;
	}

	@Override
	public boolean hasNextPair() {
		if (this.get_nextPair() == null) {
			this.set_nextPair(readNext());
		}
		return this.get_nextPair() != null;
	}

	@Override
	public ComparisonPair<T> getNextPair() {
		if (this.get_nextPair() == null) {
			this.set_nextPair(readNext());
		}
		ComparisonPair<T> retVal = this.get_nextPair();
		this.set_nextPair(null);

		return retVal;
	}

	public String toString() {
		int logicalStep = step + 4;
		return "ComparisonArrayOS [logicalStep="
				+ logicalStep
				+ ", maxBlockSize="
				+ maxBlockSize
				+ ", master ID count: "
				+ (getMasterIDs() == null ? null : String
						.valueOf(getMasterIDs().size()))
				+ ", staging ID count:"
				+ (getStagingIDs() == null ? null : String
						.valueOf(getStagingIDs().size())) + "]";
	}

	public String dump() {
		int logicalStep = step + 4;
		return "ComparisonArrayOS [logicalStep=" + logicalStep
				+ ", checkedNext=" + checkedNext + ", n=" + n
				+ ", maxBlockSize=" + maxBlockSize + ", random=" + random
				+ ", S=" + S + ", TStage=" + TStage + ", randomStage="
				+ Arrays.toString(randomStage) + ", get_mID1()=" + get_mID1()
				+ ", get_mID2()=" + get_mID2() + ", get_s1()=" + get_s1()
				+ ", get_s2()=" + get_s2() + ", get_sID1()=" + get_sID1()
				+ ", get_sID2()=" + get_sID2() + ", getMasterIDs()="
				+ getMasterIDs() + ", getMasterIDsType()=" + getMasterIDsType()
				+ ", getStagingIDs()=" + getStagingIDs()
				+ ", getStagingIDsType()=" + getStagingIDsType() + ", size()="
				+ size() + "]";
	}

}
