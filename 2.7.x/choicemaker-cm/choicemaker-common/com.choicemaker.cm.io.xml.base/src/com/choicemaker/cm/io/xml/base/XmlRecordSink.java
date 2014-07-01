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
package com.choicemaker.cm.io.xml.base;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.choicemaker.cm.core.Constants;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordSink;

/**
 * Description
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/28 09:18:16 $
 */
public class XmlRecordSink implements RecordSink {
	private String name;
	private String fileName;
	private ImmutableProbabilityModel model;
	private XmlRecordOutputter recordOutputter;
	private FileOutputStream outFile;
	private Writer w;

	public XmlRecordSink() {
	}

	public XmlRecordSink(String name, String fileName, ImmutableProbabilityModel model) {
		this.name = name;
		this.fileName = fileName;
		setModel(model);
	}

	public void open() throws IOException {
		recordOutputter = ((XmlAccessor) model.getAccessor()).getXmlRecordOutputter();
		outFile = new FileOutputStream(new File(fileName).getAbsoluteFile());
		w = new OutputStreamWriter(new BufferedOutputStream(outFile));
		w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + Constants.LINE_SEPARATOR);
		w.write("<ChoiceMakerRecords>" + Constants.LINE_SEPARATOR);
	}

	public void close() throws IOException {
		w.write("</ChoiceMakerRecords>");
		w.flush();
		outFile.close();
	}

	/**
	 * @see com.choicemaker.cm.core.base.Sink#flush()
	 */
	public void flush() throws IOException {
		w.flush();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public void put(Record r) throws IOException {
		putRecord(r);
	}

	public void putRecord(Record r) throws IOException {
		recordOutputter.put(w, r);
	}

	public void setModel(ImmutableProbabilityModel model) {
		this.model = model;
	}

	public ImmutableProbabilityModel getModel() {
		return model;
	}
}
