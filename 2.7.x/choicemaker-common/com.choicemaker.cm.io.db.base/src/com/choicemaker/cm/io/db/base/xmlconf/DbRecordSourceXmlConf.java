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
package com.choicemaker.cm.io.db.base.xmlconf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.xmlconf.RecordSourceXmlConfigurator;
import com.choicemaker.cm.core.xmlconf.XmlConfException;
import com.choicemaker.cm.io.db.base.DbRecordSource;

/**
 * Handling of Db Marked Record Pair sources.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/24 22:40:20 $
 */
public class DbRecordSourceXmlConf implements RecordSourceXmlConfigurator {
	public static final String EXTENSION_POINT_ID = "com.choicemaker.cm.io.db.base.dbRsReader";

	public Object getHandler() {
		return this;
	}

	public Class getHandledType() {
		return DbRecordSource.class;
	}

	/**
	 * Add a Db record source to the configuration.
	 *
	 * @param   name  The name of the source.
	 * @param   probabilityModel  The name of the probability model.
	 * @param   probabilityModelConfig  The name of the configuration containing the probability model.
	 * @param   selection  The selection.
	 * @param   connectionName  The name of the connection to access this source.
	 * @param   replace  Whether an exiting probability model of the same name should be replaced.
	 *            If the value of <code>replace</code> is <code>false</code> and a model of the
	 *            same name already exists, an exception is thrown.
	 * @throws  XmlConfException  if an exception occurs.
	 */
	public void add(RecordSource s) throws XmlConfException {
		try {
			DbRecordSource src = (DbRecordSource) s;
			String fileName = src.getFileName();
			Element e = new Element("RecordSource");
			e.setAttribute("class", EXTENSION_POINT_ID);
			e.setAttribute("conf", src.getConf());
			e.addContent(new Element("selection").setText(src.getSelection()));
			e.addContent(new Element("connectionName").setText(src.getDataSourceName()));
			FileOutputStream fs = new FileOutputStream(new File(fileName).getAbsoluteFile());
			XMLOutputter o = new XMLOutputter("    ", true);
			o.setTextNormalize(true);
			o.output(new Document(e), fs);
			fs.close();
		} catch (IOException ex) {
			throw new XmlConfException("Internal error.", ex);
		}
	}

	public RecordSource getRecordSource(String fileName, Element e, IProbabilityModel model)
		throws XmlConfException {
		String conf = e.getAttributeValue("conf");
		String selection = e.getChildText("selection");
		String connectionName = e.getChildText("connectionName");
		return new DbRecordSource(fileName, connectionName, model, conf, selection);
	}
}
