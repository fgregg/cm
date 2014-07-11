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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLFilterImpl;

import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.ImmutableRecordPair;
import com.choicemaker.cm.core.MarkedRecordPairSource;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.Sink;
import com.choicemaker.cm.core.base.MutableMarkedRecordPair;
import com.choicemaker.cm.core.util.DateHelper;
import com.choicemaker.cm.core.util.NameUtils;
import com.choicemaker.cm.core.xmlconf.XmlParserFactory;
import com.choicemaker.util.FileUtilities;

/**
 * Description
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/28 09:18:16 $
 */
public class XmlMarkedRecordPairSource
	extends XMLFilterImpl
	implements RecordHandler, Runnable, MarkedRecordPairSource {
	private static Logger logger = Logger.getLogger(XmlMarkedRecordPairSource.class);
	private final String READER = "org.apache.xerces.parsers.SAXParser";
	private final int BUF_SIZE = 1000;
	private String name;

	// If the inputStream is null, the xmlFileName must not be null
	private InputStream inputStream;
	private String xmlFileName;

	private String rawXmlFileName;
	private String fileName;
	private boolean report;
	private Date curDate;
	private Record curQ;
	private Decision curDecision;
	private int loc;
	private static final int QR = 0;
	private static final int MR = 1;
	private static final int MA = 2;
	private static final int OUTSIDE = 3;
	private MutableMarkedRecordPair[] pairs = new MutableMarkedRecordPair[BUF_SIZE];
	private MutableMarkedRecordPair cur;
	private int out;
	private int size;
	private int depth;
	private boolean mayHaveMore;
	private boolean readMore;
	private Thread thread;
	private ImmutableProbabilityModel model;
	private volatile Throwable thrown;

	public XmlMarkedRecordPairSource() {
	}

	public XmlMarkedRecordPairSource(String fileName, String rawXmlFileName, ImmutableProbabilityModel model) {
		setFileName(fileName);
		//this.xmlFileName = xmlFileName;
		setRawXmlFileName(rawXmlFileName);
		setModel(model);
	}

	public XmlMarkedRecordPairSource(InputStream is, String fileName, String rawXmlFileName, ImmutableProbabilityModel model) {
		this.inputStream = is;
		setFileName(fileName);
		//this.xmlFileName = xmlFileName;
		setRawXmlFileName(rawXmlFileName);
		setModel(model);
	}

	public boolean isReport() {
		return report;
	}

	public void setReport(boolean v) {
		this.report = v;
	}

	public void open() {
		thrown = null;
		DefaultHandler handler = ((XmlAccessor) model.getAccessor()).getXmlReader();
		setContentHandler(handler);
		((XmlReader) handler).open(this);
		out = 0;
		size = 0;
		depth = 0;
		mayHaveMore = true;
		readMore = true;
		thread = new Thread(this);
		thread.start();
	}

	public synchronized boolean hasNext() throws IOException {
		try {
			while (size == 0 && mayHaveMore) {
				wait();
			}
			if (thrown != null) {
				throw new IOException(thrown);
			}
			return size != 0;
		} catch (InterruptedException ex) {
			readMore = false;
			return false;
		}
	}

	public synchronized MutableMarkedRecordPair getNextMarkedRecordPair() throws IOException {
		if (thrown != null) {
			throw new IOException(thrown);
		}
		try {
			while (size == 0 && mayHaveMore) {
				wait();
			}
		} catch (InterruptedException ex) {
			throw new IOException(ex.toString());
		}
		MutableMarkedRecordPair r = pairs[out];
		--size;
		out = (out + 1) % BUF_SIZE;
		this.notifyAll();
		return r;
	}

	public ImmutableRecordPair getNext() throws IOException {
		return getNextMarkedRecordPair();
	}

	public void run() {
		InputStream is = this.inputStream;
		try {
			XMLReader reader = XmlParserFactory.createXMLReader(READER);
			reader.setContentHandler(this);
			if (is == null) {
				is = new FileInputStream(new File(xmlFileName).getAbsoluteFile());
			}
			reader.parse(new InputSource(new BufferedInputStream(is)));
		} catch (SAXException ex) {
			if (!XmlRecordSource.FORCED_CLOSE.equals(ex.toString())) {
				if (!(report
					&& (ex.toString().equals(
						"org.xml.sax.SAXParseException: The element type \"Report\" must be terminated by the matching end-tag \"</Report>\"."))
						|| ex.toString().startsWith("org.xml.sax.SAXParseException: End of entity not allowed; an end tag is missing.") ||
						ex.toString().startsWith("org.xml.sax.SAXParseException: XML document structures must start and end within the same entity."))) {
					logger.error("", ex);
					thrown = ex;
				}
			}
		} catch (Exception ex) {
			logger.error("Error reading file referenced by " + fileName, ex);
			thrown = ex;
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (Exception e) {
			}
		}
		synchronized (this) {
			mayHaveMore = false;
			this.notifyAll();
		}
	}

	/**
	 * Filter a start element event.
	 *
	 * @param uri The element's Namespace URI, or the empty string.
	 * @param localName The element's local name, or the empty string.
	 * @param qName The element's qualified (prefixed) name, or the empty
	 *        string.
	 * @param atts The element's attributes.
	 * @exception org.xml.sax.SAXException The client may throw
	 *            an exception during processing.
	 * @see org.xml.sax.ContentHandler#startElement
	 */
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		qName = qName.intern();
		++depth;
		if (depth == 1) {
			if (qName == "ChoiceMakerMarkedRecordPairs") {
				// ignore
			} else if (qName == "Report") {
				report = true;
				loc = OUTSIDE;
			} else {
				throw new SAXException("Illegal ChoiceMaker XML Source format.");
			}
		} else if (report) {
			if (depth == 2) {
				if (qName == "qu") {
					String st = atts.getValue("st");
					if(st != null && st.charAt(4) == '-') {
						curDate = DateHelper.parseSqlTimestamp(st);
					} else {
						curDate = new Date(Long.parseLong(st));
					}
				} else {
					throw new SAXException("Illegal ChoiceMaker XML Source format.");
				}
			} else if (depth == 3) {
				if (qName == "qr") {
					loc = QR;
				} else if (qName == "ma") {
					loc = MA;
					curDecision = Decision.valueOf(atts.getValue("de"));
				} else {
					loc = OUTSIDE;
				}
			} else if (depth == 4 && loc == MA) {
				if (qName == "mr") {
					loc = MR;
				}
			} else if (loc == QR || loc == MR) {
				getContentHandler().startElement(uri, localName, qName, atts);
			}
		} else { // !report
			if (depth == 2) {
				if (qName == "MarkedRecordPair") {
					cur = new MutableMarkedRecordPair();
					cur.setMarkedDecision(Decision.valueOf(atts.getValue("decision")));
					cur.setDateMarked(DateHelper.parse(atts.getValue("date")));
					cur.setUser(atts.getValue("user"));
					cur.setSource(atts.getValue("src"));
					cur.setComment(atts.getValue("comment"));
				} else {
					throw new SAXException("Illegal ChoiceMaker XML Source format.");
				}
			} else {
				getContentHandler().startElement(uri, localName, qName, atts);
			}
		}
	}

	/**
	 * Filter an end element event.
	 *
	 * @param uri The element's Namespace URI, or the empty string.
	 * @param localName The element's local name, or the empty string.
	 * @param qName The element's qualified (prefixed) name, or the empty
	 *        string.
	 * @exception org.xml.sax.SAXException The client may throw
	 *            an exception during processing.
	 * @see org.xml.sax.ContentHandler#endElement
	 */
	public void endElement(String uri, String localName, String qName) throws SAXException {
		qName = qName.intern();
		--depth;
		if (report) {
			if (depth == 0 || depth == 1) {
				// ignore
			} else if (depth == 2) {
				loc = OUTSIDE;
			} else if (depth == 3 && loc == MR) {
				loc = MA;
			} else if (loc == QR || loc == MR) {
				getContentHandler().endElement(uri, localName, qName);
			}
		} else {
			if (depth > 1) {
				getContentHandler().endElement(uri, localName, qName);
			}
		}
	}

	private void putPair(MutableMarkedRecordPair pair) throws SAXException {
		synchronized (this) {
			try {
				while (size == BUF_SIZE && readMore) {
					wait();
				}
				if (readMore) {
					pairs[(out + size) % BUF_SIZE] = pair;
					++size;
					notifyAll();
				} else {
					notifyAll();
					throw new SAXException(XmlRecordSource.FORCED_CLOSE);
				}
			} catch (InterruptedException ex) {
			}
		}
	}

	public void handleRecord(Record r) throws SAXException {
		if (report) {
			if (loc == QR) {
				curQ = r;
			} else {
				putPair(new MutableMarkedRecordPair(curQ, r, curDecision, curDate, "", "Report", ""));
			}
		} else { // !report
			if (cur.getQueryRecord() == null) {
				cur.setQueryRecord(r);
			} else {
				cur.setMatchRecord(r);
				putPair(cur);
			}
		}
	}

	public synchronized void close() {
		readMore = false;
		mayHaveMore = false;
		this.notifyAll(); // make sure that thread ends
	}

	/**
	 * Get the value of name.
	 * @return value of name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the value of name.
	 * @param v  Value to assign to name.
	 */
	public void setName(String v) {
		this.name = v;
	}

	/**
	 * Get the value of xmlFileName.
	 * @return value of xmlFileName.
	 */
	public String getXmlFileName() {
		return xmlFileName;
	}

	/**
	 * Set the value of xmlFileName.
	 * @param v  Value to assign to xmlFileName.
	 */
	public void setRawXmlFileName(String fn) {
		this.rawXmlFileName = fn;
		this.xmlFileName = FileUtilities.getAbsoluteFile(new File(fileName).getParentFile(), fn).toString();
	}

	public String getRawXmlFileName() {
		return rawXmlFileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
		setName(NameUtils.getNameFromFileName(fileName));
	}

	public String getFileName() {
		return fileName;
	}

	public void setModel(ImmutableProbabilityModel model) {
		this.model = model;
	}

	public ImmutableProbabilityModel getModel() {
		return model;
	}

	public String toString() {
		return name;
	}

	public boolean hasSink() {
		return true;
	}

	public Sink getSink() {
		return new XmlMarkedRecordPairSink(fileName, xmlFileName, model);
	}
}
