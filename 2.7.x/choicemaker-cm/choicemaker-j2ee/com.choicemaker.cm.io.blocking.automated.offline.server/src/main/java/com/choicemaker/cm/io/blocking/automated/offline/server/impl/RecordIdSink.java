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

	public RecordIdSink(String fileName) {
		super(fileName, EXTERNAL_DATA_FORMAT.STRING);
	}

	@Override
	public void writeRecordID(Comparable o) throws BlockingException {
		if (!isOpen()) {
			throw new IllegalStateException("not open");
		}
		if (getRecordIdTypeUnchecked() == null) {
			@SuppressWarnings("unchecked")
			RECORD_ID_TYPE rit = RECORD_ID_TYPE.fromInstance(o);
			this.setRecordIDType(rit);
		}
		try {
			if (count == 0)
				fw.write(getRecordIdTypeUnchecked().getStringSymbol()
						+ LINE_SEPARATOR);

			if (getRecordIdTypeUnchecked() == TYPE_INTEGER) {
				Integer I = (Integer) o;
				fw.write(I.toString() + LINE_SEPARATOR);

			} else if (getRecordIdTypeUnchecked() == TYPE_LONG) {
				Long L = (Long) o;
				fw.write(L.toString() + LINE_SEPARATOR);

			} else if (getRecordIdTypeUnchecked() == TYPE_STRING) {
				String S = (String) o;
				fw.write(S + LINE_SEPARATOR);

			} else {
				throw new BlockingException("Please set the recordIDType.");
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

		if (this.getRecordIdTypeUnchecked() == null) {
			this.idType = type;
		} else if (type != this.idType) {
			String msg =
				"Specified type (" + type + ") conflicts with existing type ("
						+ this.idType + ")";
			throw new IllegalArgumentException(msg);
		}
	}

	protected RECORD_ID_TYPE getRecordIdTypeUnchecked() {
		return this.idType;
	}

	@Override
	public RECORD_ID_TYPE getRecordIdType() {
		if (idType == null) {
			throw new IllegalStateException("A type has not been set");
		}
		return getRecordIdTypeUnchecked();
	}

}
