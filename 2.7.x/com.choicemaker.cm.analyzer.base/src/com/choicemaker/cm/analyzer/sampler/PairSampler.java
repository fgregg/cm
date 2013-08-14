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
package com.choicemaker.cm.analyzer.sampler;

import java.util.List;

import com.choicemaker.cm.core.MutableMarkedRecordPair;

/**
 * @author Adam Winkel
 */
public interface PairSampler {

	public void processPair(MutableMarkedRecordPair mrp);
	public int getNumRetained();
	public List getRetainedPairs();

}
