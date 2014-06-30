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
import com.choicemaker.cm.io.blocking.automated.offline.data.IllegalComboPair;

/**
 * @author pcheung
 *
 */
public interface IIllegalComboSink extends ISink {

	/** Writes the PairID to the sink. */
	public void writeIllegalCombo (IllegalComboPair p) throws BlockingException;

}
