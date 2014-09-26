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

import java.net.URL;

import org.jdom.input.SAXBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Comment
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.1 $ $Date: 2010/01/20 15:05:01 $
 */
public class XmlParserFactory {

	public static SAXBuilder createSAXBuilder(boolean validate) {
		ClassLoader oldCl = setClassLoader();
		SAXBuilder builder = new SAXBuilder(validate);
		restoreClassLoader(oldCl);

		return builder;
	}

	public static XMLReader createXMLReader() throws SAXException {
		ClassLoader oldCl = setClassLoader();
//		XMLReader reader = XMLReaderFactory.createXMLReader(className);
		XMLReader reader = XMLReaderFactory.createXMLReader();
		restoreClassLoader(oldCl);

		return reader;
	}

	public static XMLReader createXMLReader(String /* ignored */className) throws SAXException {
		return createXMLReader();
	}

	public static ClassLoader setClassLoader() {
		ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(XmlParserFactory.class.getClassLoader());
		return oldCl;
	}

	public static void restoreClassLoader(ClassLoader oldClassLoader) {
		if (oldClassLoader == null) {
			throw new IllegalArgumentException();
		}
		Thread.currentThread().setContextClassLoader(oldClassLoader);
	}

	public static boolean connected() {
		try {
			URL url = new URL("http://www.choicemaker.com/");
			// 2014-04-24 rphall: Commented out unused local variable.
			/* Object o = */
			url.getContent();
			return true;
		} catch (Exception ex) {
			return false;
		}
	}
}
