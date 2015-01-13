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

import com.choicemaker.cm.core.BlockingException;

/**
 * This interface handles writing recordID to a chunk data file.
 * 
 * @author pcheung
 *
 */
public interface IChunkRecordIdSink extends ISink {

	/** Writes a recordID to the sink. */
	public void writeRecordID (long recID) throws BlockingException;
	
}
