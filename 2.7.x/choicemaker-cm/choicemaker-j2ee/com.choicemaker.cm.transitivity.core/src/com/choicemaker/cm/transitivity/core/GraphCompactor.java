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
package com.choicemaker.cm.transitivity.core;

/**
 * This interface defines a way to compact (or merge) related nodes on a graph
 * and to return the compacted graph.
 * 
 * @author pcheung
 *
 * ChoiceMaker Technologies, Inc.
 */
public interface GraphCompactor {

	/** This method takes a graph with markings and compact those nodes and
	 * edges.
	 * 
	 * @param ce
	 * @return CompositeEntity - the compacted graph
	 */
	public CompositeEntity compact (CompositeEntity ce) throws TransitivityException;

}
