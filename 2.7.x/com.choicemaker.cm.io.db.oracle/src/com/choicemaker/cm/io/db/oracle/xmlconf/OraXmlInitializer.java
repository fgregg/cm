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
package com.choicemaker.cm.io.db.oracle.xmlconf;

import org.jdom.Element;

import com.choicemaker.cm.core.xmlconf.XmlModuleInitializer;

/**
 * Description
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.1 $ $Date: 2010/01/28 02:02:09 $
 */
public class OraXmlInitializer implements XmlModuleInitializer {
	public static OraXmlInitializer instance = new OraXmlInitializer();

	private OraXmlInitializer() {
	}

	public void init(Element e) {
		OraConnectionCacheXmlConf.init();
	}
}