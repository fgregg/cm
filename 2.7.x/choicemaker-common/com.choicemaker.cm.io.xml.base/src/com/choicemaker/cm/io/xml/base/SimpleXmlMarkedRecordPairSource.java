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
package com.choicemaker.cm.io.xml.base;

import com.choicemaker.cm.core.IProbabilityModel;

/**
 * @author ajwinkel
 *
 */
public class SimpleXmlMarkedRecordPairSource extends XmlMarkedRecordPairSource {

	public SimpleXmlMarkedRecordPairSource() { }

	public SimpleXmlMarkedRecordPairSource(String xmlFileName, IProbabilityModel model) {
		setFileName(xmlFileName);
		setModel(model);
	}

	public void setFileName(String fn) {
		super.setFileName(fn);
		super.setRawXmlFileName(fn);	
	}

	public void setRawXmlFileName(String fn) {
		setFileName(fn);	
	}

}
