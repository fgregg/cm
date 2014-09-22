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
package com.choicemaker.cm.core.xmlconf;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.choicemaker.cm.core.ChoiceMakerExtensionPoint;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.e2.E2Exception;

/**
 * XML configuration for record sources. Each actual source type has its own XML
 * configurator in the respective io.type package.
 *
 * @author Martin Buechi
 * @version $Revision: 1.1 $ $Date: 2010/01/20 15:05:01 $
 */
public class RecordSourceXmlConf {
	public static final String EXTENSION_POINT =
		ChoiceMakerExtensionPoint.CM_CORE_RSREADER;

	public static void add(RecordSource src) throws XmlConfException {
		try {
			((RecordSourceXmlConfigurator) ExtensionPointMapper.getInstance(
					EXTENSION_POINT, src.getClass())).add(src);
		} catch (E2Exception e) {
			throw new XmlConfException(e.toString(),e);
		}
	}

	public static RecordSource getRecordSource(String fileName)
			throws XmlConfException {
		try {
			SAXBuilder builder = XmlParserFactory.createSAXBuilder(false);
			Document document = builder.build(fileName);
			Element e = document.getRootElement();
			String cls = e.getAttributeValue("class");
			RecordSourceXmlConfigurator c =
				(RecordSourceXmlConfigurator) ExtensionPointMapper.getInstance(
						EXTENSION_POINT, cls);
			return c.getRecordSource(fileName, e, null);
		} catch (Exception ex) {
			throw new XmlConfException(ex.getMessage(), ex);
		}
	}
}
