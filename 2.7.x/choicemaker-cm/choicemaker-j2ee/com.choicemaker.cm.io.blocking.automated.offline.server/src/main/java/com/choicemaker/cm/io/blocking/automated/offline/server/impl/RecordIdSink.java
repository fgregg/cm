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
package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import static com.choicemaker.cm.io.blocking.automated.offline.core.Constants.LINE_SEPARATOR;
import static com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE.TYPE_INTEGER;
import static com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE.TYPE_LONG;
import static com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE.TYPE_STRING;

import java.io.IOException;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.EXTERNAL_DATA_FORMAT;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIdSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;
import com.choicemaker.cm.io.blocking.automated.offline.impl.BaseFileSink;

/**
 * @author pcheung
 *
 */
@SuppressWarnings({ "rawtypes" })
public class RecordIdSink extends BaseFileSink implements IRecordIdSink {

	protected RECORD_ID_TYPE idType = null;

	public RecordIdSink(String fileName, EXTERNAL_DATA_FORMAT type) {
		super(fileName, type);
	}

	@Override
	public void writeRecordID(Comparable o) throws BlockingException {
		try {
			if (type == EXTERNAL_DATA_FORMAT.BINARY) {

				if (count == 0)
					dos.writeInt(idType.getIntSymbol());

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
					throw new BlockingException("Please set the recordIDType.");
				}

			} else if (type == EXTERNAL_DATA_FORMAT.STRING) {

				if (count == 0)
					fw.write(idType.getStringSymbol() + LINE_SEPARATOR);

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
					throw new BlockingException("Please set the recordIDType.");
				}

			}

			count++;

		} catch (IOException ex) {
			throw new BlockingException(ex.toString());
		}
	}

	@Override
	public void setRecordIDType(RECORD_ID_TYPE type) {
		if (type == null) {
			throw new IllegalArgumentException("null type");
		}

		if (this.idType == null) {
			this.idType = type;
		} else if (type != this.idType) {
			String msg = "Specified type (" + type + ") conflicts with existing type (" + this.idType + ")";
			throw new IllegalArgumentException(msg);
		}
	}

	@Override
	public RECORD_ID_TYPE getRecordIdType() {
		if (this.idType == null) {
			throw new IllegalStateException("A type has not been set");
		}
		return this.idType;
	}

}
