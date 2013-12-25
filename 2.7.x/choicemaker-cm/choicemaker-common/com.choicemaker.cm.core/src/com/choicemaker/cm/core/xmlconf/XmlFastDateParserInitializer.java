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

import org.jdom.Element;

import com.choicemaker.cm.core.util.DateHelper;
import com.choicemaker.cm.core.util.FastDateParser;

/**
 * XML initializer for collections (sets).
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.1 $ $Date: 2010/01/20 15:05:01 $
 * @see       com.choicemaker.cm.matching.gen.Colls
 */
public class XmlFastDateParserInitializer implements XmlModuleInitializer {
	public final static XmlFastDateParserInitializer instance = new XmlFastDateParserInitializer();

	private XmlFastDateParserInitializer() {
	}

	public void init(Element e) {
		int centuryTurn = 20;
		String ct = e.getChildText("centuryTurn");
		if (ct != null) {
			centuryTurn = Integer.parseInt(ct);
		}
		boolean dmy = "true".equals(e.getChildText("dmy"));
		DateHelper.setDateParser(new FastDateParser(centuryTurn, dmy));
	}
}
