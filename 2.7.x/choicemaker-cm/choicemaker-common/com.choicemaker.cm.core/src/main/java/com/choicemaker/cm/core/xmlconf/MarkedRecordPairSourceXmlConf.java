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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.choicemaker.cm.core.ChoiceMakerExtensionPoint;
import com.choicemaker.cm.core.MarkedRecordPairSource;
import com.choicemaker.cm.core.XmlConfException;

/**
 * XML configuration for marked record pairs. Each actual source type
 * has its own XML configurator in the respective io.type package.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.1 $ $Date: 2010/01/20 15:05:01 $
 */
public class MarkedRecordPairSourceXmlConf {
	public static final String EXTENSION_POINT = ChoiceMakerExtensionPoint.CM_CORE_MRPSREADER;
	public static final String EXTENSION_POINT_2 = ChoiceMakerExtensionPoint.CM_CORE_FILEMRPSREADER;

	public static HashMap fileMrpsReaders;

	public static void add(MarkedRecordPairSource src) throws XmlConfException {
		((MarkedRecordPairSourceXmlConfigurator)ExtensionPointMapper.getInstance(EXTENSION_POINT, src.getClass())).add(src);
	}

	public static MarkedRecordPairSource getMarkedRecordPairSource(String fileName) throws XmlConfException {

		try {
			// the extension way
			String fn = new File(fileName).getName();
			int dotIndex = fn.lastIndexOf(".");
			String extension = "";
			if (dotIndex > -1) {
				extension = fn.substring(dotIndex + 1);
			}

			if (extension.length() > 0) {
				if (fileMrpsReaders == null) {
					initFileMrpsReaders();
				}
				Class cls = (Class) fileMrpsReaders.get(extension);
				if (cls != null) {
					MarkedRecordPairSourceXmlConfigurator c = (MarkedRecordPairSourceXmlConfigurator) cls.newInstance();
					return c.getMarkedRecordPairSource(fileName, null, null);
				}
			}

			// the new way
			SAXBuilder builder = XmlParserFactory.createSAXBuilder(false);
			Document document = builder.build(fileName);
			Element e = document.getRootElement();
			String cls = e.getAttributeValue("class");
			MarkedRecordPairSourceXmlConfigurator c = (MarkedRecordPairSourceXmlConfigurator)ExtensionPointMapper.getInstance(EXTENSION_POINT, cls);
			return c.getMarkedRecordPairSource(fileName, e, null);
		} catch (Exception ex) {
			throw new XmlConfException("Internal error.", ex);
		}
	}

	public static void initFileMrpsReaders() {
		fileMrpsReaders = new HashMap();

		IExtensionPoint pt = Platform.getPluginRegistry().getExtensionPoint(EXTENSION_POINT_2);
		IExtension[] extensions = pt.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement[] elems = extensions[i].getConfigurationElements();
			String fileExtension = elems[0].getAttribute("extension");

			try {
				Object obj = elems[0].createExecutableExtension("class");
				fileMrpsReaders.put(fileExtension, obj.getClass());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public static String[] getMrpsExtensions() {
		if (fileMrpsReaders == null) {
			initFileMrpsReaders();
		}

		List extensions = new ArrayList();
		extensions.add("mrps");

		Set keys = fileMrpsReaders.keySet();
		if (keys.contains("mrps")) {
			keys.remove("mrps");
		}

		extensions.addAll(keys);

		String[] strings = new String[extensions.size()];
		for (int i = 0; i < strings.length; i++) {
			strings[i] = (String) extensions.get(i);
		}

		return strings;
	}
}
