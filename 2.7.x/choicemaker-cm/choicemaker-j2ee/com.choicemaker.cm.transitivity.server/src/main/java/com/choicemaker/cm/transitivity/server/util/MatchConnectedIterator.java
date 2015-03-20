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

import static com.choicemaker.cm.args.WellKnownGraphPropertyNames.GPN_SCM;

import java.util.Iterator;

import com.choicemaker.cm.transitivity.core.MatchEdgeProperty;
import com.choicemaker.cm.transitivity.core.SimpleConnectedProperty;

/**
 * This object takes an Iterator of uncompacted graphs (CompositeEntity) and
 * returns an Iterator of compacted graphs using match edges property and
 * SimpleConnected graph property.
 * 
 * @author pcheung
 *
 *         ChoiceMaker Technologies, Inc.
 */
@SuppressWarnings({ "rawtypes" })
public class MatchConnectedIterator extends GenericIterator {

	public static final String NAME = GPN_SCM;

	/**
	 * This constructor takes in an Iterator of CompositeEntity.
	 * 
	 * @param compositeEntities
	 */
	public MatchConnectedIterator(Iterator compositeEntities) {
		super(NAME, compositeEntities, MatchEdgeProperty.getInstance(),
				new SimpleConnectedProperty());
	}

}
