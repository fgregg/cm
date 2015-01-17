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
package com.choicemaker.cm.io.blocking.automated.offline.filter;

import java.io.Serializable;

import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;

/**
 * Checks if a MatchRecord2 pair satisfies a filter constraint
 * 
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/28 15:45:19 $
 * @see com.choicemaker.cm.analyzer.filter.Filter
 */
public interface IMatchRecord2Filter<T extends Comparable<T>> extends
		Serializable {

	/**
	 * Checks if a pair satisfies a filter constraint
	 */
	boolean satisfy(MatchRecord2<T> pair);

}
