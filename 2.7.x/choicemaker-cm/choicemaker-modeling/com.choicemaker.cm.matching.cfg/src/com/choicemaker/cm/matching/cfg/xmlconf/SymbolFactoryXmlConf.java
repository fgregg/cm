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

import java.util.List;

import org.jdom.Element;

import com.choicemaker.cm.core.xmlconf.XmlConfException;
import com.choicemaker.cm.matching.cfg.SimpleSymbolFactory;
import com.choicemaker.cm.matching.cfg.SymbolFactory;
import com.choicemaker.cm.matching.cfg.TokenType;

/**
 * @author ajwinkel
 *
 */
public class SymbolFactoryXmlConf {

	public static SymbolFactory readFromElement(Element e) throws XmlConfException {
		Class cls = ParserXmlConf.getClass(e, SimpleSymbolFactory.class);
		SymbolFactory factory = (SymbolFactory) ParserXmlConf.instantiate(cls);
		
		List children = e.getChildren("tokenType");
		for (int i = 0; i < children.size(); i++) {
			Element child = (Element) children.get(i);
			TokenType tt = TokenTypeXmlConf.readFromElement(child);
			factory.addVariable(tt);
		}
		
		return factory;
	}

}
