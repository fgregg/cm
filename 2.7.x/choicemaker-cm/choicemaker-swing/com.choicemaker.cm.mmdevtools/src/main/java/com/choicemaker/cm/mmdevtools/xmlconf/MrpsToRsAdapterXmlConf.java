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
package com.choicemaker.cm.mmdevtools.xmlconf;

import java.io.File;

import org.jdom.Element;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.MarkedRecordPairSource;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.xmlconf.MarkedRecordPairSourceXmlConf;
import com.choicemaker.cm.core.xmlconf.RecordSourceXmlConfigurator;
import com.choicemaker.cm.mmdevtools.io.MrpsToRsAdapter;
import com.choicemaker.util.FileUtilities;

/**
 * Comment
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.2 $ $Date: 2010/03/29 13:48:43 $
 */
public class MrpsToRsAdapterXmlConf implements RecordSourceXmlConfigurator {

	public RecordSource getRecordSource(String fileName, Element e, ImmutableProbabilityModel model) throws XmlConfException {
		String mrpsFileName = e.getChildText("fileName");
		mrpsFileName = FileUtilities.getAbsoluteFile(new File(fileName).getParentFile(), mrpsFileName).getAbsolutePath();		
		MarkedRecordPairSource mrps = MarkedRecordPairSourceXmlConf.getMarkedRecordPairSource(mrpsFileName);
		mrps.setModel(model);
		return new MrpsToRsAdapter(mrps);
	}
	
	public void add(RecordSource desc) throws XmlConfException {
		throw new XmlConfException("Cannot save an MrpsToRsAdapter!");
	}

	public Object getHandler() {
		return this;
	}

	public Class getHandledType() {
		return MrpsToRsAdapter.class;
	}

}
