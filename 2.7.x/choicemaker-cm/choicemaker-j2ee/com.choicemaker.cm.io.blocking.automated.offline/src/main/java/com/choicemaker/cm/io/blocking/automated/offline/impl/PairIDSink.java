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
import com.choicemaker.cm.io.blocking.automated.offline.core.IPairIDSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.PairID;

/**
 * @author pcheung
 *
 */
public class PairIDSink extends BaseFileSink implements IPairIDSink {

	@Deprecated
	public PairIDSink(String fileName, int type) {
		super(fileName, EXTERNAL_DATA_FORMAT.fromSymbol(type));
	}

	public PairIDSink(String fileName, EXTERNAL_DATA_FORMAT type) {
		super(fileName, type);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IPairIDSink#writePair(com.choicemaker.cm.io.blocking.automated.offline.core.PairID)
	 */
	public void writePair(PairID p) throws BlockingException {
		try {
			if (type == EXTERNAL_DATA_FORMAT.BINARY) {
				dos.writeLong(p.getID1());
				dos.writeLong(p.getID2());
			} else if (type == EXTERNAL_DATA_FORMAT.STRING) {
				fw.write(Long.toString(p.getID1()));
				fw.write(Constants.EXPORT_FIELD_SEPARATOR);
				fw.write(Long.toString(p.getID2()));
				fw.write(Constants.LINE_SEPARATOR);
			}
			count ++;
		} catch (IOException ex) {
			throw new BlockingException (ex.toString());
		}
		
	}

}
