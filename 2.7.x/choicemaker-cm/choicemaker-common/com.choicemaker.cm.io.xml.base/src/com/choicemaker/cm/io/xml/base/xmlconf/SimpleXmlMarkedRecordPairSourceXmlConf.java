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
package com.choicemaker.cm.io.xml.base.xmlconf;

import org.jdom.Element;

import com.choicemaker.cm.core.base.IProbabilityModel;
import com.choicemaker.cm.core.base.MarkedRecordPairSource;
import com.choicemaker.cm.core.xmlconf.XmlConfException;
import com.choicemaker.cm.io.xml.base.SimpleXmlMarkedRecordPairSource;

/**
 * @author ajwinkel
 *
 */
public class SimpleXmlMarkedRecordPairSourceXmlConf extends XmlMarkedRecordPairSourceXmlConf {

	public void add(MarkedRecordPairSource src) throws XmlConfException {
		throw new XmlConfException("Can't create a new SimpleXmlMarkedRecordPairSource!");
	}
	
	public MarkedRecordPairSource getMarkedRecordPairSource(String fileName, Element e, IProbabilityModel model) {
		return new SimpleXmlMarkedRecordPairSource(fileName, model);
	}

}
