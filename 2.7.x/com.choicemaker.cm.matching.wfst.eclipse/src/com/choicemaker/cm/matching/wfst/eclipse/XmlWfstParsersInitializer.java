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
package com.choicemaker.cm.matching.wfst.eclipse;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.jdom.Element;

import com.choicemaker.cm.core.xmlconf.*;

/**
 * XML initializer for WfstParsers
 *
 * @author    Rick Hall
 * @version   $Revision: 1.2 $ $Date: 2010/03/27 22:30:22 $
 * @see       com.choicemaker.cm.matching.wfst.eclipse.WfstParsers
 */
public class XmlWfstParsersInitializer implements XmlModuleInitializer {
	
	public final static XmlWfstParsersInitializer instance = new XmlWfstParsersInitializer();

	private XmlWfstParsersInitializer() {
	}

	public void init(Element e) throws XmlConfException {

		List parsers = e.getChildren("instance");
		Iterator iParsers = parsers.iterator();
		while (iParsers.hasNext()) {
			Element c = (Element) iParsers.next();
						
			String name = c.getAttributeValue("name");
			String filterFileName = c.getAttributeValue("filterFile");
			String grammarFileName = c.getAttributeValue("grammarFile");
			boolean lazy = true;
			if (c.getAttribute("lazy") != null) {
				lazy = Boolean.getBoolean(c.getAttributeValue("lazy"));
			}
			
			try {
				File filterFile = new File(filterFileName);
				filterFile = filterFile.getAbsoluteFile();
				URL fUrl = filterFile.toURL();
				File grammarFile = new File(grammarFileName);
				grammarFile = grammarFile.getAbsoluteFile();
				URL gUrl = grammarFile.toURL();
				WfstParsers.addParser(name,fUrl, gUrl, lazy);
			} catch (MalformedURLException ex) {
				throw new XmlConfException(ex.getMessage(),ex);
			} catch (IOException ex) {
				throw new XmlConfException(ex.getMessage(),ex);
			}

		} // while
		
		return;
	} // init(Element)

} // XmlWfstParsersInitializer

