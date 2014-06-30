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

import com.choicemaker.cm.core.base.BlockingException;

/**
 * This interface handles the writing of ComparisonGroups.
 * 
 * @author pcheung
 *
 */
public interface IComparisonTreeSink extends ISink {

	/** Writes the ComparisonTreeNode to the sink. 
	 * tree.getRecordId should not be -1.
	 * 
	 * @param tree
	 * @throws BlockingException
	 */
	public void writeComparisonTree (ComparisonTreeNode tree) throws BlockingException;


}
