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
package com.choicemaker.cm.io.blocking.automated.offline.result;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.SortedSet;

import com.choicemaker.cm.core.util.EquivalenceClass;
import com.choicemaker.cm.io.blocking.automated.offline.core.EXTERNAL_DATA_FORMAT;

/**
 * @author pcheung
 *
 */
@SuppressWarnings({ "rawtypes" })
public class EquivalenceClassSink {

	DataOutputStream dos;
	FileWriter fw;
	String fileName;

	EXTERNAL_DATA_FORMAT type;
	int count; // this counts oversized blocks written.

	@Deprecated
	public EquivalenceClassSink(String fileName, int type) {
		this.type = EXTERNAL_DATA_FORMAT.fromSymbol(type);
		this.fileName = fileName;
	}

	public EquivalenceClassSink(String fileName, EXTERNAL_DATA_FORMAT type) {
		this.type = type;
		this.fileName = fileName;
	}

	public void open() throws FileNotFoundException, IOException {
		if (type == EXTERNAL_DATA_FORMAT.STRING)
			fw = new FileWriter(fileName, false);
		else if (type == EXTERNAL_DATA_FORMAT.BINARY)
			dos = new DataOutputStream(new FileOutputStream(fileName, false));
	}

	public void close() throws IOException {
		if (type == EXTERNAL_DATA_FORMAT.STRING)
			fw.close();
		else if (type == EXTERNAL_DATA_FORMAT.BINARY)
			dos.close();
	}

	public int getCount() {
		return count;
	}

	/** Gets the file name or other pertinent information if it is not a file. */
	public String getInfo() {
		return fileName;
	}

	public void writeEquivalenceClass(EquivalenceClass ec) throws IOException {
		if (type == EXTERNAL_DATA_FORMAT.BINARY) {

			SortedSet set = ec.getMemberIds();
			Iterator it = set.iterator();

			// write the size
			dos.writeLong(set.size());

			// write the records.
			while (it.hasNext()) {
				Long L = (Long) it.next();
				dos.writeLong(L.longValue());
			}

		} else if (type == EXTERNAL_DATA_FORMAT.STRING) {

			SortedSet set = ec.getMemberIds();
			Iterator it = set.iterator();

			while (it.hasNext()) {
				Long L = (Long) it.next();
				fw.write(L.toString());
				fw.write(" ");
			}

			fw.write("\r\n");
		}
	}

}
