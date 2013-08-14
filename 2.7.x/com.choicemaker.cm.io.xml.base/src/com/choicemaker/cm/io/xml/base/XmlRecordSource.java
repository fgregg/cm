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

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLFilterImpl;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.Sink;
import com.choicemaker.cm.core.util.ChainedIOException;
import com.choicemaker.cm.core.util.FileUtilities;
import com.choicemaker.cm.core.util.NameUtils;
import com.choicemaker.cm.core.xmlconf.XmlParserFactory;

/**
 * Description
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/28 09:18:16 $
 */
public class XmlRecordSource extends XMLFilterImpl implements RecordHandler, Runnable, RecordSource {
	private static Logger logger = Logger.getLogger(XmlRecordSource.class);
	public static String FORCED_CLOSE = "forcedClose";
	private final String READER = "org.apache.xerces.parsers.SAXParser";
	private final int BUF_SIZE = 1000;
	private String name;
	private String fileName;
	private String rawXmlFileName;
	private String xmlFileName;
	private Record[] records = new Record[BUF_SIZE];
	private int out;
	private int size;
	private int depth;
	private boolean mayHaveMore;
	private boolean readMore;
	private Thread thread;
	private ImmutableProbabilityModel model;
	private Throwable thrown;

	public XmlRecordSource() {
	}

	public XmlRecordSource(String fileName, String rawXmlFileName, ImmutableProbabilityModel model) {
		setFileName(fileName);
		setRawXmlFileName(rawXmlFileName);
		setModel(model);
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
				throw new ChainedIOException(thrown);
			}
			return size != 0;
		} catch (InterruptedException ex) {
			readMore = false;
			return false;
		}
	}

	public synchronized Record getNext() throws IOException {
		if (thrown != null) {
			throw new ChainedIOException(thrown);
		}
		try {
			while (size == 0 && mayHaveMore) {
				wait();
			}
		} catch (InterruptedException ex) {
			throw new IOException(ex.toString());
		}
		Record r = records[out];
		--size;
		out = (out + 1) % BUF_SIZE;
		this.notifyAll();
		return r;
	}

	public void run() {
		FileInputStream fs = null;
		try {
			XMLReader reader = XmlParserFactory.createXMLReader(READER);
			reader.setContentHandler(this);
			fs = new FileInputStream(new File(xmlFileName).getAbsoluteFile());
			reader.parse(new InputSource(new BufferedInputStream(fs)));
		} catch (Exception ex) {
			logger.error("", ex);
			thrown = ex;
		} finally {
			try {
				if (fs != null) {
					fs.close();
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
			if (qName == "ChoiceMakerRecords") {
				// ignore
			} else {
				throw new SAXException("Illegal ChoiceMaker XML Source format.");
			}
		} else {
			getContentHandler().startElement(uri, localName, qName, atts);
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
		if (depth == 0) {
			// ignore
		} else {
			getContentHandler().endElement(uri, localName, qName);
		}
	}

	public synchronized void handleRecord(Record r) throws SAXException {
		try {
			while (size == BUF_SIZE && readMore) {
				wait();
			}
			if (readMore) {
				records[(out + size) % BUF_SIZE] = r;
				++size;
				notifyAll();
			} else {
				notifyAll();
				throw new SAXException(FORCED_CLOSE);
			}
		} catch (InterruptedException ex) {
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
		this.xmlFileName =
			FileUtilities.getAbsoluteFile(new File(fileName).getAbsoluteFile().getParentFile(), fn).toString();
	}

	public String getRawXmlFileName() {
		return rawXmlFileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
		setName(NameUtils.getNameFromFileName(fileName));
	}

	/**
	 * Get the value of fileName.
	 * @return value of fileName.
	 */
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
		return new XmlRecordSink(getName(), getXmlFileName(), getModel());
	}
}
