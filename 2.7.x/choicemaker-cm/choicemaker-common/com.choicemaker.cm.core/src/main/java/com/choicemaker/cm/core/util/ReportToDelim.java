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
package com.choicemaker.cm.core.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.choicemaker.cm.core.Constants;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.1 $ $Date: 2010/01/20 15:05:03 $
 */
public class ReportToDelim extends DefaultHandler {
	private static Logger logger = Logger.getLogger(ReportToDelim.class.getName());

	private String inputFile;
	private String outputFile;
	private Writer w;

	public static void main(String[] args) {
		new ReportToDelim(args[0], args[1]).parse();
	}

	private ReportToDelim(String inputFile, String outputFile) {
		this.inputFile = inputFile;
		this.outputFile = outputFile;
	}

	private void parse() {
		try {
			w = new FileWriter(outputFile);
			w.write("differThreshold,matchThreshold,maximumMatches,modelName,startTime,duration,queryType,numberReturnedByBlocking" + Constants.LINE_SEPARATOR);
			XMLReader reader = XMLReaderFactory.createXMLReader();
			reader.setContentHandler(this);
			FileInputStream fs = new FileInputStream(new File(inputFile).getAbsoluteFile());
			reader.parse(new InputSource(new BufferedInputStream(fs)));
		} catch (Exception ex) {
			logger.severe("Internal Error: " + ex);
		} finally {
			try {
				w.close();
			} catch (IOException e) {
			}
		}
	}
	/**
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
		throws SAXException {
		try {
			if ("qu".equals(localName)) {
				int len = atts.getLength();
				for (int i = 0; i < len; ++i) {
					if (i != 0) {
						w.write(",");
					}
					w.write(atts.getValue(i));
				}
				w.write(Constants.LINE_SEPARATOR);
			}
		} catch (IOException ex) {
			throw new SAXException(ex.toString());
		}
	}
}
