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
import java.util.Locale;

import com.choicemaker.cm.core.Constants;
import com.choicemaker.cm.core.ImmutableMarkedRecordPair;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.ImmutableRecordPair;
import com.choicemaker.cm.core.MarkedRecordPairSink;
import com.choicemaker.cm.core.util.DateHelper;
import com.choicemaker.cm.core.util.XmlOutput;
import com.choicemaker.util.FileUtilities;

/**
 * Description
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.3 $ $Date: 2010/03/28 09:18:16 $
 */
public class XmlMarkedRecordPairSink implements MarkedRecordPairSink {
	private String name;
	private String xmlFileName;
	private String rawXmlFileName;
	private ImmutableProbabilityModel model;
	private XmlRecordOutputter recordOutputter;
	private FileOutputStream outFile;
	private Writer writer;

	private static String encoding;
	static {
		if (Locale.getDefault().getLanguage().equals("ja")) {
			encoding = "Shift_JIS";
		} else {
			encoding = "UTF-8";
		}
	}
	
	public static String getEncoding() {
		return encoding;
	}

	public XmlMarkedRecordPairSink(String name, String rawXmlFileName, ImmutableProbabilityModel model) {
		this.name = name;
		setRawXmlFileName(rawXmlFileName);
		setModel(model);
	}
	
	public void setRawXmlFileName(String fn) {
		this.rawXmlFileName = fn;
		this.xmlFileName = FileUtilities.getAbsoluteFile(new File(name).getParentFile(), fn).toString();
	}

	public String getRawXmlFileName() {
		return rawXmlFileName;
	}

	protected void startRootEntity() throws IOException {
		getWriter().write("<ChoiceMakerMarkedRecordPairs>" + Constants.LINE_SEPARATOR);
	}

	public void open() throws IOException {
		recordOutputter = ((XmlAccessor) model.getAccessor()).getXmlRecordOutputter();
		outFile = new FileOutputStream(new File(xmlFileName).getAbsoluteFile());
		setWriter(new OutputStreamWriter(new BufferedOutputStream(outFile)));
		getWriter().write("<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>" + Constants.LINE_SEPARATOR);
		startRootEntity();
		// BUG: getWriter().write(">" + Constants.LINE_SEPARATOR);
		getWriter().flush();
	}

	protected void finishRootEntity() throws IOException {
		getWriter().write("</ChoiceMakerMarkedRecordPairs>");
	}

	public void close() throws IOException {
		finishRootEntity();
		getWriter().flush();
		outFile.close();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getXmlFileName() {
		return xmlFileName;
	}

	public void setXmlFileName(String xmlFileName) {
		this.xmlFileName = xmlFileName;
	}

	private void setWriter(Writer writer) {
		this.writer = writer;
	}

	protected Writer getWriter() {
		return writer;
	}

	public void put(ImmutableRecordPair r) throws IOException {
		putMarkedRecordPair((ImmutableMarkedRecordPair) r);
	}

	protected void startRecordPairEntity() throws IOException {
		getWriter().write("<MarkedRecordPair");
	}
	
	protected void finishRecordPairEntity() throws IOException {
		getWriter().write("</MarkedRecordPair>" + Constants.LINE_SEPARATOR);
	}

	public void putMarkedRecordPair(ImmutableMarkedRecordPair r) throws IOException {
		startRecordPairEntity();
		XmlOutput.writeAttribute(getWriter(), "decision", r.getMarkedDecision().toString());
		XmlOutput.writeAttribute(getWriter(), "date", DateHelper.format(r.getDateMarked()));
		XmlOutput.writeAttribute(getWriter(), "user", r.getUser());
		XmlOutput.writeAttribute(getWriter(), "src", r.getSource());
		XmlOutput.writeAttribute(getWriter(), "comment", r.getComment());
		putAdditionalAttributes(r);
		getWriter().write(">" + Constants.LINE_SEPARATOR);
		recordOutputter.put(getWriter(), r.getQueryRecord());
		recordOutputter.put(getWriter(), r.getMatchRecord());
		finishRecordPairEntity();
	}

	/**
	 * Callback for subclasses
	 * @param mrp Non-null marked record pair
	 */
	protected void putAdditionalAttributes(ImmutableMarkedRecordPair mrp)  throws IOException {
	}

	public void setModel(ImmutableProbabilityModel model) {
		this.model = model;
	}

	public ImmutableProbabilityModel getModel() {
		return model;
	}

	public void flush() throws IOException {
		getWriter().flush();
	}

}
