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
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;

import com.choicemaker.cm.transitivity.core.CompositeEntity;
import com.choicemaker.cm.transitivity.core.EdgeProperty;
import com.choicemaker.cm.transitivity.core.SubGraphProperty;
import com.choicemaker.cm.transitivity.core.TransitivityException;
import com.choicemaker.cm.transitivity.util.GraphAnalyzer;
import com.choicemaker.cm.transitivity.util.SimpleGraphCompactor;

/**
 * This object takes an Iterator of uncompacted graphs (CompositeEntity) and
 * returns an Iterator of compacted graphs using edges property 
 * and sub graph property specified in the constructor. 
 * 
 * @author pcheung
 *
 * ChoiceMaker Technologies, Inc.
 */
public class GenericIterator implements Iterator {

	private static final Logger log = Logger.getLogger(GenericIterator.class.getName());
	
//	private final String name;
	
	private final Iterator compositeEntities;
	
	private final EdgeProperty edge;
	
	private final SubGraphProperty subGraph;
	
	/** This constructor takes in an Iterator of CompositeEntity.
	 * 
	 * @param compositeEntities
	 */
	public GenericIterator (String name, Iterator compositeEntities, EdgeProperty ep, 
		SubGraphProperty sp) {
			
//		this.name = name;
		this.compositeEntities = compositeEntities;
		this.edge = ep;
		this.subGraph = sp;
		
		// Invariants
		if (name == null || name.trim().length() == 0) {
			throw new IllegalArgumentException("nulll or blank name");
		}
		if (compositeEntities == null || edge == null || subGraph == null) {
			throw new IllegalArgumentException("nulll constructor argument");
		}
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
		CompositeEntity ce = (CompositeEntity) compositeEntities.next();
		
		GraphAnalyzer ga = new GraphAnalyzer (ce, edge, subGraph);
		CompositeEntity ret = ce;
		try {
			ga.analyze();
			
			SimpleGraphCompactor compactor = new SimpleGraphCompactor ();
			ret = compactor.compact(ce);
			
		} catch (TransitivityException e) {
			log.error(e,e);
			throw new NoSuchElementException(e.getMessage());
		}

		return ret;
	}

}
