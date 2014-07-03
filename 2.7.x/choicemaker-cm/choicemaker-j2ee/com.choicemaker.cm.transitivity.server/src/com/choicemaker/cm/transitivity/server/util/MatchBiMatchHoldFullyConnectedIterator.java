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
package com.choicemaker.cm.transitivity.server.util;

import java.util.Iterator;

//import com.choicemaker.cm.transitivity.core.BiConnectedProperty;
//import com.choicemaker.cm.transitivity.core.CompositeEntity;
//import com.choicemaker.cm.transitivity.core.MatchEdgeProperty;
//import com.choicemaker.cm.transitivity.core.TransitivityException;
//import com.choicemaker.cm.transitivity.util.GraphAnalyzer;
//import com.choicemaker.cm.transitivity.util.SimpleGraphCompactor;

/**
 * This object takes an Iterator of uncompacted graphs (CompositeEntity) and
 * returns an Iterator of compacted graphs using match edges property 
 * and biconnected graph property. 
 * 
 * @author pcheung
 *
 * ChoiceMaker Technologies, Inc.
 */
public class MatchBiMatchHoldFullyConnectedIterator implements Iterator {

	public static final String NAME = "BCM_FCMH";

//	private static final Logger log = Logger.getLogger(MatchBiMatchHoldFullyConnectedIterator.class);
	
	private Iterator compositeEntities;
	
	/** This constructor takes in an Iterator of CompositeEntity.
	 * 
	 * @param compositeEntities
	 */
	public MatchBiMatchHoldFullyConnectedIterator (Iterator compositeEntities) {
		this.compositeEntities = compositeEntities;
		throw new RuntimeException("Not yet implemented");
	}


	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		compositeEntities.remove();
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		return compositeEntities.hasNext();
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public Object next() {
		throw new RuntimeException("Not yet implemented");
//		CompositeEntity ce = (CompositeEntity) compositeEntities.next();
//		MatchEdgeProperty mp = MatchEdgeProperty.getInstance();
//		BiConnectedProperty bip = new BiConnectedProperty ();
//		
//		GraphAnalyzer ga = new GraphAnalyzer (ce, mp, bip);
//		CompositeEntity ret = ce;
//		try {
//			ga.analyze();
//			
//			SimpleGraphCompactor compactor = new SimpleGraphCompactor ();
//			ret = compactor.compact(ce);
//			
//		} catch (TransitivityException e) {
//			log.error(e,e);
//		}
//
//		return ret;
	}

}
