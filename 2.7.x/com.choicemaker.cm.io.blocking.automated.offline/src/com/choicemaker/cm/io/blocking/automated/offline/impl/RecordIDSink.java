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
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDSink;

/**
 * @author pcheung
 *
 */
public class RecordIDSink extends BaseFileSink implements IRecordIDSink {

	protected int idType = 0;

	public RecordIDSink (String fileName, int type) {
		init (fileName, type);
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDSink#writeRecordID(java.lang.Object)
	 */
	public void writeRecordID(Comparable o) throws BlockingException {
		try {
			if (type == Constants.BINARY) {
				
				if (count == 0) dos.writeInt(idType);
				
				if (idType == Constants.TYPE_INTEGER) {
					
					Integer I = (Integer) o;
					dos.writeInt(I.intValue());
					
				} else if (idType == Constants.TYPE_LONG) {
					
					Long L = (Long) o;
					dos.writeLong(L.longValue());
					
				} else if (idType == Constants.TYPE_STRING) {

					String S = (String) o;
					dos.writeInt(S.length());
					dos.writeChars(S);
					
				} else {
					throw new BlockingException ("Please set the recordIDType.");
				}
				
			} else if (type == Constants.STRING) {

				if (count == 0) fw.write (Integer.toString(idType) + Constants.LINE_SEPARATOR);

				if (idType == Constants.TYPE_INTEGER) {
					
					Integer I = (Integer) o;
					fw.write(I.toString() + Constants.LINE_SEPARATOR);
					
				} else if (idType == Constants.TYPE_LONG) {

					Long L = (Long) o;
					fw.write(L.toString() + Constants.LINE_SEPARATOR);

				} else if (idType == Constants.TYPE_STRING) {
					
					String S = (String) o;
					fw.write(S + Constants.LINE_SEPARATOR);

				} else {
					throw new BlockingException ("Please set the recordIDType.");
				}

			}
			
			count ++;
		
		} catch (IOException ex) {
			throw new BlockingException (ex.toString());
		}
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDSink#setRecordIDType(int)
	 */
	public void setRecordIDType(int type) {
		this.idType = type;
	}

}
