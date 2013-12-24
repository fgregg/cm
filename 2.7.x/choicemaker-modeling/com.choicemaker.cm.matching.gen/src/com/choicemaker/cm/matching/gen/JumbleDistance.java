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
package com.choicemaker.cm.matching.gen;

/**
 * Computes a type of edit distance between 2 strings, based on the
 * osbervatoin taht one can raed snetneces in wihch wrods hvae jmubled
 * sepllnigs as lnog as the wodrs satrt and end wtih the crorect lteters.
 * When two words start and end with the same letters, but have different
 * interior sequences, the jumble distance is defined as the edit distance
 * (specifically EditDistance2) of the interior sequences;
 * e.g.<ul>
 * <li>Jumble distance 0: observation vs observation</li>
 * <li>Jumble distance 1: observation vs observatin</li>
 * <li>Jumble distance 1: observation vs osbervation</li>
 * <li>Jumble distance 2: observation vs osbervatoin</li>
 * <li>Jumble distance MAX_INT: observation vs observatino</li></ul>
 *
 * Jumble distance is probably most useful in detecting important
 * but subtle differences in the first names of twins, where parents
 * name their children similarly; e.g. "Belissa" versus "Jelissa" (real
 * examples from the NYC DOHMH MCI project).
 * 
 * @author Rick Hall
 * @version   $Revision: 1.1 $ $Date: 2010/03/27 22:22:06 $
 * @see EditDistance2
 */

public class JumbleDistance {

	/**
	 * Computes the jumble distance between two strings.
	 *
	 * If the result would be greater than <code>maxDistance</code>, then
	 * <code>maxDistance + 1</code> is returned. The rationale for
	 * the the <code>maxDistance</code> parameter is to improve performance
	 * by stopping the computation if the result would be bigger than
	 * interested.
	 *
	 * @param  s           The first string.
	 * @param  t           The second string.
	 * @param  maxDistance The maximum distance. Must be <code>&gt; 0</code> and
	 *           <code>&lt; Integer.MAX_VALUE</code>.
	 * @return the minimum jumble distance between <code>s</code> and <code>t</code>
	 * @throws NullPointerException if one of the Strings is <code>null</code>. 
	 */
	public static int jumbleDistance(String s, String t, int maxDistance) {

		int m = s.length();
		int n = t.length();

		int retVal = Integer.MAX_VALUE;
		if (m == 0 && n == 0) {
			retVal =0;

		} else if (m == 1 && n == 1) {
			if (s.charAt(0) == t.charAt(0)) {
				retVal = 0;
			}

		} else if (m > 1 && n > 1) {
			int p = s.length() - 1;
			int q = t.length() - 1;
			if (s.charAt(0) == t.charAt(0) && s.charAt(p) == t.charAt(q)) {
				String s_Interior = s.substring(1,p);
				String t_Interior = t.substring(1,q);
				retVal = EditDistance2.editDistance2(s_Interior,t_Interior,maxDistance);
			}

		}
		return retVal;
	}

	/**
	 * Computes the jumble distance between two strings.
	 *
	 * This is equivalent to <code>editDistance(s, t, Integer.MAX_VALUE - 1)</code>.
	 *
	 * @param  s           The first string.
	 * @param  t           The second string.
	 * @return the minimum jumble distance between <code>a</code> and <code>b</code> 
	 */
	public static int jumbleDistance(String s, String t) {
		return jumbleDistance(s, t, Integer.MAX_VALUE - 1);
	}

}

