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
package com.choicemaker.cm.modelmaker.stats;

/**
 *
 * @author    
 * @version   $Revision: 1.2 $ $Date: 2010/03/29 12:24:49 $
 */
public class StatPoint {
	public float differThreshold = Float.NaN;
	public float matchThreshold = Float.NaN;
	public float falseNegatives = Float.NaN;
	public float falsePositives = Float.NaN;
	public float differRecall = Float.NaN;
	public float matchRecall = Float.NaN;
	public float humanReview = Float.NaN;
	// the following are only populated for current point
	public float precision = Float.NaN;
	public float recall = Float.NaN;
	public float correlation = Float.NaN;

	public void reset() {
		differThreshold = Float.NaN;
		matchThreshold = Float.NaN;
		falseNegatives = Float.NaN;
		falsePositives = Float.NaN;
		differRecall = Float.NaN;
		matchRecall = Float.NaN;
		humanReview = Float.NaN;
		precision = Float.NaN;
		recall = Float.NaN;
		correlation = Float.NaN;
	}

	public boolean equals(Object o) {
		// Super equals Method
		//return super.equals(o);
		boolean retVal = false;
		if (this==o) {
			retVal = true;
		} else if (o instanceof StatPoint) {
			StatPoint that = (StatPoint) o;
			retVal = areEqual(this,that);
		}
			
		return retVal;
	}

	public int hashCode() {
		// Super hashCode Method
		//return super.hashCode();
		Float dt = new Float(differThreshold);
		Float mt = new Float(matchThreshold);
		Float fn = new Float(falseNegatives);
		Float fp = new Float(falsePositives);
		Float dr = new Float(differRecall);
		Float mr = new Float(matchRecall);
		Float hr = new Float(humanReview);
		Float pr = new Float(precision);
		Float re = new Float(recall);
		Float corr = new Float(correlation);
		// Simple
		return
			dt.hashCode()
			+ mt.hashCode()
			+ fn.hashCode()
			+ fp.hashCode()
			+ dr.hashCode()
			+ mr.hashCode()
			+ hr.hashCode()
			+ pr.hashCode()
			+ re.hashCode()
			+ corr.hashCode();
		// More sophiticated
		// Use bitwise xor (^) instead
		/*
		return
			dt.hashCode()
			^ mt.hashCode()
			^ fn.hashCode()
			^ fp.hashCode()
			^ dr.hashCode()
			^ mr.hashCode()
			^ hr.hashCode()
			^ pr.hashCode()
			^ re.hashCode()
			^ corr.hashCode();
		*/
	}

	private static boolean areEqual(StatPoint a, StatPoint b) {
		// Preconditions assumed
		boolean retVal = true;
		retVal = retVal && compareFloats(a.correlation,b.correlation);
		retVal = retVal && compareFloats(a.differRecall,b.differRecall);
		retVal = retVal && compareFloats(a.differThreshold,b.differThreshold);
		retVal = retVal && compareFloats(a.falseNegatives,b.falseNegatives);
		retVal = retVal && compareFloats(a.falsePositives,b.falsePositives);
		retVal = retVal && compareFloats(a.humanReview,b.humanReview);
		// The values below are not displayed
		retVal = retVal && compareFloats(a.matchRecall, b.matchRecall);
		retVal = retVal && compareFloats(a.matchThreshold, b.matchThreshold);
		retVal = retVal && compareFloats(a.precision, b.precision);
		retVal = retVal && compareFloats(a.recall, b.recall);
		return retVal;
	}

	private static boolean compareFloats(float x, float y) {
		boolean retVal = false;
		if (Float.isNaN(x) && Float.isNaN(y)) {
			retVal = true;
		} else if (x==y) {
			retVal = true;
		}
		return retVal;
	}

}
