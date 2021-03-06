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
package com.choicemaker.cm.io.blocking.automated.offline.core;

/**
 * This interface handles the reading of ComparisonGroups.
 * 
 * @author pcheung
 *
 */
public interface IComparisonTreeSource<T extends Comparable<T>> extends
		ISource<ComparisonTreeNode<T>> {

	/** Returns the number of ComparisonTree read so far. */
	public int getCount();

}
