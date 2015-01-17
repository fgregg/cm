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
package com.choicemaker.cm.io.blocking.automated.offline.result;

/**
 * This object performs set find, set union, and set path compression using an
 * int array as base.
 * 
 * It is based on ideas from
 * "Two Strategies to Speed up Connected Component Labeling Algorithms" by
 * Kesheng Wu, Ekow Otoo, Kenji Suzuki
 * 
 * 
 * @author pcheung
 *
 */
public class SetJoiner {

	/**
	 * This is the root arry. rootArray[i] is the root of element i.
	 */
	private int[] rootArray;

	/**
	 * This is the initial value. If rootArray[i] = DEFAULT, then i is not in
	 * any set.
	 */
	public static final int DEFAULT = -1;

	/**
	 * This constructor takes in the maximum size of the array of roots. It can
	 * handle up to maxSize integer elements.
	 * 
	 * @param maxSize
	 *            - maximum number of integers to find and union.
	 */
	public SetJoiner(int maxSize) {
		rootArray = new int[maxSize];

		// initial the elements to -1, because some id's might not show up.
		// we want to distinguish between 0, a possible root, and -1, not root.
		for (int i = 0; i < maxSize; i++) {
			rootArray[i] = DEFAULT;
		}
	}

	/**
	 * This method tells the objects that i and j are related. It will merge the
	 * sets containing i and j. It uses the smallest id in the set as the root.
	 * 
	 * @param i
	 *            - i is related to j
	 * @param j
	 *            - j is related to i
	 */
	public void union(int i, int j) {
		// System.out.println (i + " " + j);

		// if an element is not in any set, assign it to its own set.
		if (rootArray[i] == DEFAULT)
			rootArray[i] = i;
		if (rootArray[j] == DEFAULT)
			rootArray[j] = j;

		int rooti = findRoot(i);
		if (i != j) {
			int rootj = findRoot(j);

			// make sure rooti is the smaller one
			if (rootj < rooti)
				rooti = rootj;

			setRoot(j, rooti);
		}

		setRoot(i, rooti);
	}

	/**
	 * This method assigns each element to its ultimate root and returns the
	 * array containing all the roots.
	 * 
	 * If 1 is the root of 5 and 5 is the root of 10, then rootArray[1] == 1,
	 * rootArray[5] == 1, and rootArray[10] == 1. Note that the smallest id is
	 * always the ultimate root.
	 * 
	 * Only call this method when you are done with all the unions.
	 * 
	 * @return int [] - the root array
	 */
	public int[] flatten() {
		int size = rootArray.length;
		for (int i = 1; i < size; i++) {
			if (rootArray[i] != DEFAULT)
				rootArray[i] = rootArray[rootArray[i]];
		}

		return rootArray;
	}

	/**
	 * This method finds the root
	 * 
	 * @param i
	 *            - the member for whom you are finding the root.
	 * @return int - the ultimate root of element i.
	 */
	private int findRoot(int i) {
		int ret = i;
		while (rootArray[ret] < ret) {
			ret = rootArray[ret];
		}
		return ret;
	}

	/**
	 * This method compressed the sets by directly connecting every element from
	 * i to its root to the root argument.
	 * 
	 * @param i
	 *            - the path starting from i for which you want to directly
	 *            connect to root
	 * @param root
	 *            - the root value to which you want to set
	 */
	private void setRoot(int i, int root) {
		while (rootArray[i] < i) {
			int j = rootArray[i];
			rootArray[i] = root;
			// System.out.println ("setting root of " + i + " to " + root);
			i = j;
		}
		rootArray[i] = root;
		// System.out.println ("setting root of " + i + " to " + root);
	}

}
