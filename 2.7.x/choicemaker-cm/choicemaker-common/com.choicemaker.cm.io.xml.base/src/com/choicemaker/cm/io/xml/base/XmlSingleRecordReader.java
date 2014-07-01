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

import java.io.IOException;
import java.io.StringReader;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.xmlconf.XmlParserFactory;

/**
 * Comment
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.2 $ $Date: 2010/03/28 09:18:16 $
 */
public class XmlSingleRecordReader implements RecordHandler {
	private final String READER = "org.apache.xerces.parsers.SAXParser";
	private String profile;
	private IProbabilityModel model;
	private Record record;
	
	public static Record getRecord(IProbabilityModel model, String profile) throws SAXException {
		XmlSingleRecordReader r = new XmlSingleRecordReader(model, profile);
		return r.getRecord();
	}
	
	private XmlSingleRecordReader(IProbabilityModel model, String profile) {
		this.profile = profile;
		this.model = model;
	}
	
	private Record getRecord() throws SAXException {
		XMLReader reader = XmlParserFactory.createXMLReader(READER);
		XmlReader handler = ((XmlAccessor) model.getAccessor()).getXmlReader();
		reader.setContentHandler(handler);
		handler.open(this);
		try {
			reader.parse(new InputSource(new StringReader(profile)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return record;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.xml.RecordHandler#handleRecord(com.choicemaker.cm.core.Record)
	 */
	public void handleRecord(Record r) throws SAXException {
		record = r;
	}
	
	
}
