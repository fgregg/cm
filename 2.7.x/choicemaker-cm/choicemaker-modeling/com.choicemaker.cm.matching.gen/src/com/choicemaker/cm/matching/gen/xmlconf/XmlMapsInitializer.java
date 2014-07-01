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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

import com.choicemaker.cm.core.xmlconf.XmlConfException;
import com.choicemaker.cm.core.xmlconf.XmlModuleInitializer;
import com.choicemaker.cm.matching.gen.Maps;

/**
 * XML initializer for mapections (sets).
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:05 $
 * @see       com.choicemaker.cm.matching.gen.Maps
 */
public class XmlMapsInitializer implements XmlModuleInitializer {
	public final static XmlMapsInitializer instance = new XmlMapsInitializer();

	private XmlMapsInitializer() {
	}

	public void init(Element e) throws XmlConfException {
		List maps = e.getChildren("fileMap");
		Iterator iMaps = maps.iterator();
		while (iMaps.hasNext()) {
			Element c = (Element) iMaps.next();
						
			String name = c.getAttributeValue("name");
			String fileName = c.getAttributeValue("file");
			String keyType = c.getAttributeValue("keyType").intern();
			String valueType = c.getAttributeValue("valueType").intern();
			
			boolean singleLine = "true".equals(c.getAttributeValue("singleLine"));
			
			try {
				Map m = null;
				if (singleLine) {
					m = Maps.readSingleLineMap(fileName, keyType, valueType);
				} else {
					m = Maps.readFileMap(fileName, keyType, valueType);
				}
				Maps.addMap(name, m);
			} catch (IOException ex) {
				throw new XmlConfException("Error reading file: " + fileName, ex);	
			}
		}
	}

}
