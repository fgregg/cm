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
 * Implementation of standard affine gap edit distance algorithm with the exception
 * that consecutive character swaps count as one edit rather than two.
 * 
 * This algorithm penalizes consecutive inserted or deleted characters less than 
 * regular edit distance.  The cost for the first insertion/deletion is <i>gap start cost</i>,
 * while the cost for each subsequent, consecutive insertion/deletion is <i>gap continue cost</i>
 * 
 * For example, the affine gap edit distance of &quot;INC&quot; and 
 * &quot;INCORPORATED&quot; is 1*1 + 8*0.5 = 5, which is much less than the regular edit distance
 * of 9.
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:04 $
 */
public class AffineGapEditDistance {

	/**
	 * Returns the affine gap edit distance between <code>s</code> and <code>t</code>.
	 * By default, the gap start cost is 1 and the gap continue cost is 0.5.
	 * 
	 * @param s the first String
	 * @param t the second String
	 * @return the affine gap edit distance between <code>s</code> and <code>t</code>
	 *  with gap start cost 1 and gap continue cost 0.5
	 * @see #agEditDistance(String, String, float, float)
	 */
	public static float agEditDistance(String s, String t) {
		return agEditDistance(s, t, GAP_START, GAP_CONTINUE, Integer.MAX_VALUE - 1);
	}

	/**
	 * Returns the affine gap edit distance between <code>s</code> and <code>t</code> using
	 * the specified gap start cost and gap continue cost.
	 * 
	 * @param s the first String
	 * @param t the second String
	 * @param gapStart the gap start cost
	 * @param gapContinue the gap continue cost
	 * @return the affine gap edit distance between <code>s</code> and <code>t</code>
	 *  with the specified gap start and continue costs
	 */	
	public static float agEditDistance(String s, String t, float gapStart, float gapContinue) {
		return agEditDistance(s, t, gapStart, gapContinue, Integer.MAX_VALUE - 1);
	}
	
	/**
	 * Returns the affine gap edit distance between <code>s</code> and <code>t</code> using
	 * the specified gap start cost and gap continue cost.  If the edit distance will be longer
	 * than <code>maxDistance</code>, then <code>maxDistance + 1</code> is returned.
	 * 
	 * @param s the first String
	 * @param t the second String
	 * @param gapStart the gap start cost
	 * @param gapContinue the gap continue cost
	 * @param maxDistance the distance cutoff
	 * @return the affine gap edit distance between <code>s</code> and <code>t</code>
	 *  with the specified gap start and continue costs
	 */	
	public static float agEditDistance(String s, String t, float gapStart, float gapContinue, int maxDistance) {

		if (gapStart > 1) {
			throw new IllegalArgumentException("GapStart can be no larger than 1: " + gapStart);
		} else if (gapContinue > gapStart) {
			throw new IllegalArgumentException("GapContinue (" + gapContinue + 
				") must be no bigger than gapStart (" + gapStart + ")"); 
		} else if (gapContinue < 0) {
			throw new IllegalArgumentException("Neither GapStart nor GapContinue can be negative");
		}

		int m = s.length();
		int n = t.length();
		if (m == 0) {
			return Math.min(maxDistance + 1, n);
		}
		if (n == 0) {
			return Math.min(maxDistance + 1, m);
		}

		// Initialize the 0th rows and columns of each matrix

		float[][] d  = new float[m+1][n+1];
		float[][] sg = new float[m+1][n+1];
		float[][] tg = new float[m+1][n+1];

		float val;

		val = gapStart;
		for (int i = 1; i <= m; i++) {
			d[i][0] = i;
			sg[i][0] = val; val += gapContinue;
			tg[i][0] = i;
		}
		
		val = gapStart;
		for (int j = 1; j <= n; j++) {
			d[0][j] = j;
			sg[0][j] = j;
			tg[0][j] = val; val += gapContinue;
		}
		
		// Loop

		int cost;
		char s1, s2, t1, t2;

		s1 = '\0';
		for (int i = 1; i <= m; i++) {
			s2 = s1;
			s1 = s.charAt(i - 1);
			
			t1 = '\0';
			for (int j = 1; j <= n; j++) {
				t2 = t1;
				t1 = t.charAt(j - 1);

				// the normal distance matrix
				//	Note that we don't consider insertions and deletions in d;
				//		that is done is sg and tg.
				//  However, we do consider swaps.
				cost = s1 == t1 ? 0 : 1;
				d[i][j] = cost + min(d[i-1][j-1], sg[i-1][j-1], tg[i-1][j-1]);
				if (s2 == t1 && s1 == t2) {
					d[i][j] = min(d[i][j], d[i-2][j-2] + 1);
				}
				
				// end with gap in s
				sg[i][j] = min(d[i-1][j] + gapStart, sg[i-1][j] + gapContinue);
				
				// end with gap in t
				tg[i][j] = min(d[i][j-1] + gapStart, tg[i][j-1] + gapContinue);
			}
		}

		// return
		
		float minD = min(d[m][n], sg[m][n], tg[m][n]);
		return minD < maxDistance ? minD : maxDistance + 1;
	}
	
	private static float min(float i, float j) {
		return i < j ? i : j;
	}
	
	private static float min(float i, float j, float k) {
		return i < j ? 
					i < k ? i : k :
					j < k ? j : k;
	}

	private static final float GAP_START = 1.0f;
	private static final float GAP_CONTINUE = 0.5f;

}
