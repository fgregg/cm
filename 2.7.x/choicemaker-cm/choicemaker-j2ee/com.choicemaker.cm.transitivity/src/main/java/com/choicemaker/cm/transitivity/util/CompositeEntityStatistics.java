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
package com.choicemaker.cm.transitivity.util;

import java.util.Iterator;
import java.util.TreeMap;

import com.choicemaker.cm.transitivity.core.BiConnectedProperty;
import com.choicemaker.cm.transitivity.core.CompositeEntity;
import com.choicemaker.cm.transitivity.core.FullyConnectedProperty;
import com.choicemaker.cm.transitivity.core.MatchEdgeProperty;
import com.choicemaker.cm.transitivity.core.MatchHoldEdgeProperty;
import com.choicemaker.cm.transitivity.core.TransitivityException;

/** This object categorizes the CompositeEntities into different property categories.
 * 
 * @author pcheung
 *
 * ChoiceMaker Technologies, Inc.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class CompositeEntityStatistics {

	private TreeMap buckets;
	
	private int count;
	private int maxSize;	
	
	
	public CompositeEntityStatistics () {
		buckets = new TreeMap ();
	}
	
	/**
	 * Include this cluster in the statistics.
	 *
	 */
	public void includeCluster (CompositeEntity ce) throws TransitivityException {
		count ++;
		int s = ce.getChildren().size(); 
		if (s > maxSize) maxSize = s;
		StatisticsBucket bucket = getBucket2 (s);
		bucket._count ++;
		
		MatchHoldEdgeProperty mhp = MatchHoldEdgeProperty.getInstance ();
		MatchEdgeProperty mp = MatchEdgeProperty.getInstance ();
		
		FullyConnectedProperty fcp = new FullyConnectedProperty ();
		BiConnectedProperty bcp = new BiConnectedProperty ();
		
		CompositeEntity ce2 = GraphFilter.getInstance().filter(ce, mhp);
		boolean isFull_MH = fcp.hasProperty(ce2);
		boolean isBi_MH = bcp.hasProperty(ce2);
		
		ce2 = GraphFilter.getInstance().filter(ce, mp);
		boolean isFull_M = false;
		boolean isBi_M = false;
		if (ce2.getChildren().size() == ce.getChildren().size()) {
			//don't count those that had nodes dropped off.
			isFull_M = fcp.hasProperty(ce2);
			isBi_M = bcp.hasProperty(ce2);
		}


/*
		FullyConnectedMatchHoldProperty fcmh = 
			FullyConnectedMatchHoldProperty.getInstance ();
		boolean isFull_MH = ce.hasProperty(fcmh);
		
		BiConnectedMatchHoldProperty bcmh = 
			BiConnectedMatchHoldProperty.getInstance ();
		boolean isBi_MH = ce.hasProperty(bcmh);
		
		FullyConnectedMatchProperty fcm = 
			FullyConnectedMatchProperty.getInstance ();
		boolean isFull_M = ce.hasProperty(fcm);
		
		BiConnectedMatchProperty bcm = 
			BiConnectedMatchProperty.getInstance ();
		boolean isBi_M = ce.hasProperty(bcm);
*/
		
		if (isFull_MH) bucket._full_MH ++;
		if (isBi_MH) bucket._bi_MH ++;
		if (isFull_M) bucket._full_M ++;
		if (isBi_M) bucket._bi_M ++;
		if (isFull_MH && isBi_M) bucket._full_MH_bi_M ++;
	}


	/**
	 * This method write the statistics to System.out.
	 *
	 */
	public void writeStatistics () {
		Iterator it = buckets.values().iterator();
		while (it.hasNext()) {
			StatisticsBucket bucket = (StatisticsBucket) it.next();
			System.out.println ("size from " + bucket._lowerLimit + " to " + bucket._upperLimit 
				+ " count: " + bucket._count + " Full: " + bucket._full_MH +
				" Full_M: " + bucket._full_M + " Bi: " + bucket._bi_MH + 
				" Bi_M: " + bucket._bi_M +
				" Full_MH_Bi_M: " +  bucket._full_MH_bi_M);
		}
		System.out.println ("number of clusters: " + count + " maxSize: " + maxSize);
	}
	

	private StatisticsBucket getBucket2 (int s) throws TransitivityException {
		Integer I = new Integer(s);
		StatisticsBucket b = (StatisticsBucket) buckets.get(I);
		
		if (b == null) {
			b = new StatisticsBucket ();
			b._lowerLimit = s;
			b._upperLimit = s;
			buckets.put(I, b);
		} 
		return b;
	}
	
	
	private class StatisticsBucket {
	
		private int _lowerLimit;
		private int _upperLimit;
		private int _count;
		private int _full_M;
		private int _full_MH;
		private int _bi_M;
		private int _bi_MH;
		private int _full_MH_bi_M;
	
	}

	
}
