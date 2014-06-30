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

import java.io.IOException;
import java.io.StringWriter;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.base.Constants;
import com.choicemaker.cm.core.base.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.Record;

/**
 * Comment
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.2 $ $Date: 2010/03/28 09:20:46 $
 */
public class XmlSingleRecordWriter {

	protected static Logger log = Logger.getLogger(XmlSingleRecordWriter.class);

	public static String writeRecord(
		ImmutableProbabilityModel probabilityModel,
		Record record,
		boolean header) {
		StringWriter sw = new StringWriter();
		if (header) {
			sw.write(
				"<?xml version=\"1.0\" encoding=\""
					+ XmlMarkedRecordPairSink.getEncoding()
					+ "\"?>"
					+ Constants.LINE_SEPARATOR);
		}
		try {
			((XmlAccessor) probabilityModel.getAccessor())
				.getXmlRecordOutputter()
				.put(
				sw,
				record);
		} catch (IOException e) {
			String msg = "Unable to write record " + record.getId();
			log.error(msg,e);
			sw.write("<error>" + msg + "</error>");
		}

		return sw.toString();
	}
}
