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
import com.choicemaker.cm.core.util.IntArrayList;

/**
 * This interface handles writting out record_id, value_id pairs.
 * 
 * @author pcheung
 *
 */
public interface IRecValSink extends ISink{
	
	/** Writes a block to the sink. */
	public void writeRecordValue (long recID, IntArrayList values) throws BlockingException;
	
}
