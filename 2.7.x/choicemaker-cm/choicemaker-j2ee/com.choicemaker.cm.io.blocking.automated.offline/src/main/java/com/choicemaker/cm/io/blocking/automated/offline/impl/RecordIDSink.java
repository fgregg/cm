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

import static com.choicemaker.cm.io.blocking.automated.offline.core.Constants.LINE_SEPARATOR;
import static com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE.TYPE_INTEGER;
import static com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE.TYPE_LONG;
import static com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE.TYPE_STRING;

import java.io.IOException;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.EXTERNAL_DATA_FORMAT;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;

/**
 * @author pcheung
 *
 */
@SuppressWarnings({"rawtypes"})
public class RecordIDSink extends BaseFileSink implements IRecordIDSink {

	protected RECORD_ID_TYPE idType = null;

	@Deprecated
	public RecordIDSink(String fileName, int type) {
		super(fileName, EXTERNAL_DATA_FORMAT.fromSymbol(type));
	}

	public RecordIDSink(String fileName, EXTERNAL_DATA_FORMAT type) {
		super(fileName, type);
	}

	public void writeRecordID(Comparable o) throws BlockingException {
		try {
			if (type == EXTERNAL_DATA_FORMAT.BINARY) {
				
				if (count == 0) dos.writeInt(idType.getIntSymbol());
				
				if (idType == TYPE_INTEGER) {
					
					Integer I = (Integer) o;
					dos.writeInt(I.intValue());
					
				} else if (idType == TYPE_LONG) {
					
					Long L = (Long) o;
					dos.writeLong(L.longValue());
					
				} else if (idType == TYPE_STRING) {

					String S = (String) o;
					dos.writeInt(S.length());
					dos.writeChars(S);
					
				} else {
					throw new BlockingException ("Please set the recordIDType.");
				}
				
			} else if (type == EXTERNAL_DATA_FORMAT.STRING) {

				if (count == 0) fw.write (idType.getStringSymbol() + LINE_SEPARATOR);

				if (idType == TYPE_INTEGER) {
					
					Integer I = (Integer) o;
					fw.write(I.toString() + LINE_SEPARATOR);
					
				} else if (idType == TYPE_LONG) {

					Long L = (Long) o;
					fw.write(L.toString() + LINE_SEPARATOR);

				} else if (idType == TYPE_STRING) {
					
					String S = (String) o;
					fw.write(S + LINE_SEPARATOR);

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
	public void setRecordIDType(RECORD_ID_TYPE type) {
		this.idType = type;
	}

}
