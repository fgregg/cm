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
package com.choicemaker.cm.io.composite.base.xmlconf;

import java.io.*;
import java.util.*;

import org.jdom.*;
import org.jdom.output.XMLOutputter;

import com.choicemaker.cm.core.*;
import com.choicemaker.cm.core.util.FileUtilities;
import com.choicemaker.cm.core.xmlconf.*;
import com.choicemaker.cm.io.composite.base.CompositeMarkedRecordPairSource;

/**
 * Handling of composite Marked Record Pair sources.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/28 08:56:16 $
 */
public class CompositeMarkedRecordPairSourceXmlConf implements MarkedRecordPairSourceXmlConfigurator {
	public static final String EXTENSION_POINT_ID = "com.choicemaker.cm.io.composite.base.compositeMrpsReader";

	public Object getHandler() {
		return this;
	}

	public Class getHandledType() {
		return CompositeMarkedRecordPairSource.class;
	}

	/**
	 * Add a Composite marked record pair source to the configuration.
	 *
	 * @param   s  The composite marked record pair source.
	 * @param   replace  Whether an exiting probability model of the same name should be replaced.
	 *            If the value of <code>replace</code> is <code>false</code> and a model of the
	 *            same name already exists, an exception is thrown.
	 * @throws  XmlConfException  if an exception occurs.
	 */
	public void add(MarkedRecordPairSource s) throws XmlConfException {
		try {
			CompositeMarkedRecordPairSource src = (CompositeMarkedRecordPairSource) s;
			String fileName = src.getFileName();
			File rel = new File(fileName).getAbsoluteFile().getParentFile();
			Element e = new Element("MarkedRecordPairSource");
			e.setAttribute("class", EXTENSION_POINT_ID);
			int numSources = src.getNumSources();
			for (int i = 0; i < numSources; ++i) {
				Element cons = new Element("constituent");
				MarkedRecordPairSource mrps = (MarkedRecordPairSource) src.getSource(i);
				if (src.saveAsRelative(i)) {
					cons.setAttribute("name", FileUtilities.getRelativeFile(rel, mrps.getFileName()).toString());
				} else {
					cons.setAttribute("name", mrps.getFileName());
				}
				e.addContent(cons);
			}
			FileOutputStream fs = new FileOutputStream(new File(fileName).getAbsoluteFile());
			XMLOutputter o = new XMLOutputter("    ", true);
			o.setTextNormalize(true);
			o.output(new Document(e), fs);
			fs.close();
		} catch (IOException ex) {
			throw new XmlConfException("Internal error.", ex);
		}
	}

	public MarkedRecordPairSource getMarkedRecordPairSource(String fileName, Element e, IProbabilityModel model)
		throws XmlConfException {
		CompositeMarkedRecordPairSource comp = new CompositeMarkedRecordPairSource();
		comp.setFileName(fileName);
		List cons = e.getChildren("constituent");
		Iterator i = cons.iterator();
		File rel = new File(fileName).getAbsoluteFile().getParentFile();
		while (i.hasNext()) {
			Element conEl = (Element) i.next();
			String conFileName = conEl.getAttributeValue("name");
			String absConFileName = FileUtilities.getAbsoluteFile(rel, conFileName).toString();
			boolean saveAsRel = !FileUtilities.isFileAbsolute(conFileName);
			comp.add(MarkedRecordPairSourceXmlConf.getMarkedRecordPairSource(absConFileName), saveAsRel);
		}
		return comp;
	}

	public String toString() {
		return "Composite MRPS";
	}
}
