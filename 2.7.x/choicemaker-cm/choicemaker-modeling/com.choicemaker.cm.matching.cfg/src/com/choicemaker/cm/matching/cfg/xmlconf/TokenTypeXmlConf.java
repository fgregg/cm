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
package com.choicemaker.cm.matching.cfg.xmlconf;

import org.jdom.Element;

import com.choicemaker.cm.core.xmlconf.XmlConfException;
import com.choicemaker.cm.matching.cfg.TokenType;

/**
 * @author ajwinkel
 *
 */
public class TokenTypeXmlConf {

	public static TokenType readFromElement(Element e) throws XmlConfException {
		String name = e.getAttributeValue("name");
		if (name == null) {
			throw new XmlConfException("Element " + e.getName() + " does not define a 'name' attribute");
		}
		
		Class cls = ParserXmlConf.getClass(e, null);
		if (cls == null) {
			throw new XmlConfException("Element " + e.getName() + " does not define a 'class' attribute");
		}
		
		TokenType tt = (TokenType) ParserXmlConf.instantiate(cls, new Class[] {String.class}, new Object[] {name});
		
		ParserXmlConf.invoke(tt, e.getChildren());
		
		return tt;
	}

}
