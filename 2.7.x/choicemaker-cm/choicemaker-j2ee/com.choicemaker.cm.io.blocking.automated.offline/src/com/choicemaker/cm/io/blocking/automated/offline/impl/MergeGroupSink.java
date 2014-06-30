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

import com.choicemaker.cm.core.base.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.Constants;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMergeGroup;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMergeGroupSink;

/**
 * @author pcheung
 *
 */
public class MergeGroupSink extends BaseFileSink implements IMergeGroupSink {
	
	/** This creates a new match record sink.  By default it does not append to existing file, but
	 * overwrites it.
	 * 
	 * @param fileName
	 * @param type
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public MergeGroupSink (String fileName, int type) {
		super.init(fileName, type);
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IMergeGroupSink#writeMergeGroup(com.choicemaker.cm.io.blocking.automated.offline.core.IMergeGroup)
	 */
	public void writeMergeGroup(IMergeGroup mg) throws BlockingException {
		try {
			writeArray (mg.getStagingRecords());
			writeArray (mg.getMasterRecords());
			writeArray (mg.getStagingHolds());
			writeArray (mg.getMasterHolds());
				
			if (type == Constants.STRING) {
				fw.write( mg.isHold() + Constants.LINE_SEPARATOR);
			} else if (type == Constants.BINARY) {
				dos.writeBoolean(mg.isHold());
			}
			count ++;
		} catch (IOException ex) {
			throw new BlockingException (ex.toString());
		}
	}

	private void writeArray (long [] list) throws BlockingException {
		try {
			if (type == Constants.STRING) {
				if (list != null) {
					for (int i=0; i<list.length; i++) {
						fw.write(list[i] + " ");
					}
				}
				fw.write( Constants.LINE_SEPARATOR);
			} else if (type == Constants.BINARY) {
				if (list == null) {
					dos.writeInt(0);
				} else {
					dos.writeInt(list.length);
					for (int i=0; i<list.length; i++) {
						dos.writeLong(list[i]);
					}
				}
			}			
		} catch (IOException ex) {
			throw new BlockingException (ex.toString());
		}
	}
	
	
}
