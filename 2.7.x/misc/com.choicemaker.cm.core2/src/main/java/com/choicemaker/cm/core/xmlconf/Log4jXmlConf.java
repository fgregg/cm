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

import java.util.Iterator;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.xml.DOMConfigurator;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.output.DOMOutputter;

import com.choicemaker.cm.core.XmlConfException;

/**
 * XML configurator for Log4j.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/27 21:28:10 $
 */
public class Log4jXmlConf {

	private static final Logger logger = Logger.getLogger(Log4jXmlConf.class);

	public static void configIfPresent(String name) {
		try {
			config(name);
		} catch (XmlConfException ex) {
			//logger.info("Caught XmlConfException", ex);
			logger.info("log config " + name + " was not found.");
		}
	}

	/**
	 * Configures log4j.
	 *
	 * @param   name  The name of the configuration.
	 * @throws  XmlConfException  if there is a problem with the configuration file.
	 */
	public static void config(String name) throws XmlConfException {
		if (name != null) {
			org.jdom.Element l = XmlConfigurator.getInstance().getCore().getChild("logging");
			if (l != null) {
				Iterator i = l.getChildren("log4jconf").iterator();
				while (i.hasNext()) { // contains break
					org.jdom.Element e = (org.jdom.Element) i.next();
					if (name.equals(e.getAttributeValue("name"))) {
						String init = e.getAttributeValue("init");
						if (init == null || !"false".equals(init)) {
							config(e.getChild("configuration", Namespace.getNamespace("http://jakarta.apache.org/log4j/")));
						}
						return;
					}
				}
			}

			throw new XmlConfException("No such log configuration: " + name);
		} else {
			Logger root = Logger.getRootLogger();
			root.addAppender(new NoAppender());
			//root.setLevel((Level) Level.OFF);
		}
	}

	private static class NoAppender extends AppenderSkeleton {
		NoAppender() {
			setThreshold(Priority.FATAL);
		}
		public void close() {
		}
		public boolean requiresLayout() {
			return false;
		}
		public void append(LoggingEvent event) {
		}
	}

	public static void config(org.jdom.Element e) throws XmlConfException {
		DOMOutputter domout = new DOMOutputter();
		org.w3c.dom.Element domElement = null;
		System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
		ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(Log4jXmlConf.class.getClassLoader());
		try {
			org.jdom.Element det = ((org.jdom.Element) e.clone()).detach();
			domElement = domout.output(new org.jdom.Document(det)).getDocumentElement();
		} catch (JDOMException ex) {
			throw new XmlConfException("Internal error.", ex);
		} finally {
			Thread.currentThread().setContextClassLoader(oldCl);
		}
		config(domElement);
	}

	public static void config(org.w3c.dom.Element domElement) {
		DOMConfigurator.configure(domElement);
	}
}
