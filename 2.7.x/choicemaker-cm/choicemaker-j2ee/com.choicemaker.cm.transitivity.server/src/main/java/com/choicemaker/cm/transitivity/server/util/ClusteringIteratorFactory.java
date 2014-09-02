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

import java.util.logging.Logger;

/**
 * @author rphall
 * @version $Revision: 1.2 $ $Date: 2010/04/16 20:43:18 $
 */
public class ClusteringIteratorFactory {

	private static final Logger log = Logger.getLogger(GenericIterator.class.getName());
	
	private static ClusteringIteratorFactory instance = null;
	private static Object instanceSynch = new Object();
	
	private ClusteringIteratorFactory() {
	}
	
	public static ClusteringIteratorFactory getInstance() {
		if (instance == null) {
			synchronized(instanceSynch) {
				if (instance == null) {
					instance = new ClusteringIteratorFactory();
				}
			}
		}
		return instance;
	}
	
	/**
	 * Creates an iterator that finds clusters of records ("compacted" CompositeEntities).
	 * @param name name of a clustering iterator (e.g. FCM, CM, BCM)
	 * @param ceIter a group of entities that should be grouped
	 * @return
	 */	
	public Iterator createClusteringIterator(String name,Iterator ceIter) {

		if (name == null || name.trim().length() ==0) {
			throw new IllegalArgumentException("null or blank iterator name");
		}
		if (ceIter == null) {
			throw new IllegalArgumentException("null CompositeEntity iterator");
		}

		Iterator retVal = null;

		// Replace by lookup in a plugin registry (or replace factroy by dependency injection)
		if(name.equals(MatchBiconnectedIterator.NAME)) {
			retVal = new MatchBiconnectedIterator(ceIter);
		} else if ( name.equals(MatchFullyConnectedIterator.NAME) ) {
			retVal = new MatchFullyConnectedIterator(ceIter);
		} else if( name.equals(MatchBiMatchHoldFullyConnectedIterator.NAME)) {
			retVal = new MatchBiMatchHoldFullyConnectedIterator(ceIter);
		} else if(name.equals(MatchConnectedIterator.NAME)) {
			retVal = new MatchConnectedIterator(ceIter);
		} else {
			String msg = "unknown group match criterium";
			log.severe(msg);
			throw new IllegalArgumentException(msg);
		}

		return retVal;
	}

}

