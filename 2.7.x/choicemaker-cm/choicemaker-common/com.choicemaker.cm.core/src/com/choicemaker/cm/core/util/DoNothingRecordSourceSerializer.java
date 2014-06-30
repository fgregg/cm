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
package com.choicemaker.cm.core.util;

import java.io.NotSerializableException;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import com.choicemaker.cm.core.base.AbstractRecordSourceSerializer;
import com.choicemaker.cm.core.base.ISerializableRecordSource;
import com.choicemaker.cm.core.base.RecordSource;

/**
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/24 18:07:22 $
 */
public class DoNothingRecordSourceSerializer
	extends AbstractRecordSourceSerializer {

	private static final long serialVersionUID = 1L;
	private static Logger logger =
		Logger.getLogger(DoNothingRecordSourceSerializer.class);
		
	public Properties getProperties() {
		return new Properties();
	}
	
	public void setProperties(Properties p) {
		if (p != null) {
			logger.debug("Ignoring properties: [" + p.toString() + "]");
		}
	}

	public static void logIgnored(
		Pattern uriPattern,
		Class[] handledClasses,
		Properties properties) {
		logIgnoredPattern(logger, Priority.DEBUG, uriPattern);
		logIgnoredClasses(logger, Priority.DEBUG, handledClasses);
		logIgnoredProperties(logger, Priority.DEBUG, properties);
	}

	/**
	 * @param uriPattern
	 * @param handledClasses
	 * @param properties
	 */
	public DoNothingRecordSourceSerializer(
		Pattern uriPattern,
		Class[] handledClasses,
		Properties properties) {
		super(null, (Class[]) null, null);
		logIgnored(uriPattern, handledClasses, properties);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.base.IRecordSourceSerializer#getSerializableRecordSource(java.lang.String)
	 */
	public ISerializableRecordSource getSerializableRecordSource(Properties properties)
		throws NotSerializableException {
		String msg =
			AbstractRecordSourceSerializer.msgCanNotSerializeProperties(
				properties);
		logger.error(msg);
		throw new NotSerializableException(msg);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.base.IRecordSourceSerializer#getSerializableRecordSource(com.choicemaker.cm.core.base.RecordSource)
	 */
	public ISerializableRecordSource getSerializableRecordSource(RecordSource rs)
		throws NotSerializableException {
		String msg =
			AbstractRecordSourceSerializer.msgCanNotSerializeInstance(rs);
		logger.error(msg);
		throw new NotSerializableException(msg);
	}

}
