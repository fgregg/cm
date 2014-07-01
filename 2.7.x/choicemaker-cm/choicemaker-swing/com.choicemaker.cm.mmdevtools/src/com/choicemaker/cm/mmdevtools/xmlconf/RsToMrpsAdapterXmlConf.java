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

import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.MarkedRecordPairSource;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.util.FileUtilities;
import com.choicemaker.cm.core.xmlconf.MarkedRecordPairSourceXmlConfigurator;
import com.choicemaker.cm.core.xmlconf.RecordSourceXmlConf;
import com.choicemaker.cm.core.xmlconf.XmlConfException;
import com.choicemaker.cm.mmdevtools.io.RsToMrpsAdapter;

/**
 * Comment
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.2 $ $Date: 2010/03/29 13:48:43 $
 */
public class RsToMrpsAdapterXmlConf implements MarkedRecordPairSourceXmlConfigurator {

	public MarkedRecordPairSource getMarkedRecordPairSource(String fileName, Element e, IProbabilityModel model) throws XmlConfException {
		String rsFileName = e.getChildText("fileName");
		rsFileName = FileUtilities.getAbsoluteFile(new File(fileName).getParentFile(), rsFileName).getAbsolutePath();
		RecordSource rs = RecordSourceXmlConf.getRecordSource(rsFileName);
		rs.setModel(model);
		return new RsToMrpsAdapter(rs);
	}

	public void add(MarkedRecordPairSource desc) throws XmlConfException {
		throw new XmlConfException("Cannot save an RsToMrpsAdapter!");
	}

	public Object getHandler() {
		return this;
	}

	public Class getHandledType() {
		return RsToMrpsAdapter.class;
	}

}
