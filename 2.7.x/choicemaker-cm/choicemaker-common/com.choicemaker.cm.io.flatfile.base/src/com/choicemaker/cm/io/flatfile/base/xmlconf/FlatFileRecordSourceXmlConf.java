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
package com.choicemaker.cm.io.flatfile.base.xmlconf;

import java.io.*;

import org.jdom.*;
import org.jdom.output.XMLOutputter;

import com.choicemaker.cm.core.base.*;
import com.choicemaker.cm.core.xmlconf.*;
import com.choicemaker.cm.io.flatfile.base.FlatFileRecordSource;

/**
 * Handling of XML Marked Record Pair sources.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/28 09:10:40 $
 */
public class FlatFileRecordSourceXmlConf implements RecordSourceXmlConfigurator {
	public static final String EXTENSION_POINT_ID = "com.choicemaker.cm.io.flatfile.base.flatfileRsReader";

	public Object getHandler() {
		return this;
	}

	public Class getHandledType() {
		return FlatFileRecordSource.class;
	}

	/**
	 * Add a FlatFile marked record pair source to the configuration.
	 *
	 * @param   replace  Whether an exiting probability model of the same name should be replaced.
	 *            If the value of <code>replace</code> is <code>false</code> and a model of the
	 *            same name already exists, an exception is thrown.
	 * @throws  XmlConfException  if an exception occurs.
	 */
	public void add(RecordSource s) throws XmlConfException {
		try {
			FlatFileRecordSource src = (FlatFileRecordSource) s;
			String fileName = src.getFileName();
			Element e = new Element("RecordSource");
			e.setAttribute("class", EXTENSION_POINT_ID);
			e.addContent(new Element("fileNamePrefix").setText(String.valueOf(src.getRawFileNamePrefix())));
			e.addContent(new Element("fileNameSuffix").setText(String.valueOf(src.getFileNameSuffix())));
			e.addContent(new Element("multiFile").setText(String.valueOf(src.isMultiFile())));
			e.addContent(new Element("singleLine").setText(String.valueOf(src.isSingleLine())));
			e.addContent(new Element("fixedLength").setText(String.valueOf(src.isFixedLength())));
			e.addContent(new Element("separatorChar").setText(String.valueOf(src.getSeparator())));
			e.addContent(new Element("tagged").setText(String.valueOf(src.isTagged())));
			FileOutputStream fs = new FileOutputStream(new File(fileName).getAbsoluteFile());
			XMLOutputter o = new XMLOutputter("    ", false);
			o.output(new Document(e), fs);
			fs.close();
		} catch (IOException ex) {
			throw new XmlConfException("Internal error.", ex);
		}
	}

	public RecordSource getRecordSource(String fileName, Element e, IProbabilityModel model)
		throws XmlConfException {
		String fileNamePrefix = e.getChildText("fileNamePrefix");
		String fileNameSuffix = e.getChildText("fileNameSuffix");
		boolean multiFile = Boolean.valueOf(e.getChildText("multiFile")).booleanValue();
		boolean singleLine = Boolean.valueOf(e.getChildText("singleLine")).booleanValue();
		boolean fixedLength = Boolean.valueOf(e.getChildText("fixedLength")).booleanValue();
		char sep = e.getChildText("separatorChar").charAt(0);
		boolean tagged = Boolean.valueOf(e.getChildText("tagged")).booleanValue();
		return new FlatFileRecordSource(
			fileName,
			fileNamePrefix,
			fileNameSuffix,
			multiFile,
			singleLine,
			fixedLength,
			sep,
			tagged,
			model);
	}
}
