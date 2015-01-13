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
package com.choicemaker.cm.io.blocking.automated.offline.impl;

import java.io.IOException;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.Constants;
import com.choicemaker.cm.io.blocking.automated.offline.core.EXTERNAL_DATA_FORMAT;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIdSink;

/**
 * @author pcheung
 *
 */
public class ChunkRecordIDSink extends BaseFileSink implements
		IChunkRecordIdSink {

	@Deprecated
	public ChunkRecordIDSink(String fileName, int type) {
		super(fileName, EXTERNAL_DATA_FORMAT.fromSymbol(type));
	}

	public ChunkRecordIDSink(String fileName, EXTERNAL_DATA_FORMAT type) {
		super(fileName, type);
	}

	public void writeRecordID(long recID) throws BlockingException {
		try {
			if (type == EXTERNAL_DATA_FORMAT.STRING) {
				fw.write(Long.toString(recID) + Constants.LINE_SEPARATOR);
			} else if (type == EXTERNAL_DATA_FORMAT.BINARY) {
				dos.writeLong(recID);
			}
			count++;
		} catch (IOException ex) {
			throw new BlockingException(ex.toString());
		}
	}

}
