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
package com.choicemaker.cm.matching.gen.xmlconf;

import java.io.*;
import java.util.*;

import org.jdom.Element;

import com.choicemaker.cm.core.xmlconf.*;
import com.choicemaker.cm.matching.gen.Sets;

/**
 * XML initializer for collections (sets).
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:05 $
 * @see       com.choicemaker.cm.matching.gen.Colls
 */
public class XmlSetsInitializer implements XmlModuleInitializer {
	public final static XmlSetsInitializer instance = new XmlSetsInitializer();

	private XmlSetsInitializer() { }

	public void init(Element e) throws XmlConfException {
		List colls = e.getChildren();
		Iterator iColls = colls.iterator();
		while (iColls.hasNext()) {
			Element c = (Element) iColls.next();
			if (!c.getName().equals("fileSet")) {
				throw new XmlConfException("Only file sets are currently supported.");
			}
			String name = c.getAttributeValue("name");
			String fileName = c.getAttributeValue("file");
			try {
				Set s = Sets.readFileSet(fileName);
				Sets.addCollection(name, s);
			} catch (IOException ex) {
				throw new XmlConfException("Error reading file: " + fileName, ex);	
			}
		}
	}

}
