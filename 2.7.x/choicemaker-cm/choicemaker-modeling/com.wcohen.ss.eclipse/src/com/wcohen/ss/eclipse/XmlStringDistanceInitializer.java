/*
 * @(#)$RCSfile: XmlStringDistanceInitializer.java,v $        $Revision: 1.1 $ $Date: 2010/03/27 19:41:45 $
 *
 * Copyright (c) 2001 ChoiceMaker Technologies, Inc.
 * 41 East 11th Street, New York, NY 10003
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * ChoiceMaker Technologies Inc. ("Confidential Information").
 */

package com.wcohen.ss.eclipse;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.xmlconf.XmlModuleInitializer;

/**
 * XML initializer for StringDistances
 *
 * @author    Rick Hall
 * @version   $Revision: 1.1 $ $Date: 2010/03/27 19:41:45 $
 * @see       com.wcohen.ss.eclipse.StringDistances
 */
public class XmlStringDistanceInitializer implements XmlModuleInitializer {
	
	public final static XmlStringDistanceInitializer instance = new XmlStringDistanceInitializer();

	private XmlStringDistanceInitializer() {
	}

	public void init(Element e) throws XmlConfException {

		List distances = e.getChildren("instance");
		Iterator iDistances = distances.iterator();
		while (iDistances.hasNext()) {
			Element c = (Element) iDistances.next();
						
			String name = c.getAttributeValue("name");
			String fileName = c.getAttributeValue("file");
			String fileFormatName = c.getAttributeValue("fileFormat");
			String fileFormatVersion = c.getAttributeValue("fileFormatVersion");
			
			try {
				File file = new File(fileName);
				file = file.getAbsoluteFile();
				URL fUrl = file.toURL();
				FileFormat fileFormat = FileFormat.getInstance(fileFormatName);
				StringDistances.addStringDistance(name,fUrl, fileFormat, fileFormatVersion);
			} catch (IOException ex) {
				throw new XmlConfException(ex.getMessage(),ex);
			}

		} // while
		
		return;
	} // init(Element)

} // XmlStringDistanceInitializer

