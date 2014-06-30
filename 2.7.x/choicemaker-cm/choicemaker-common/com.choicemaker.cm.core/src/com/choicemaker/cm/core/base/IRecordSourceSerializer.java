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
package com.choicemaker.cm.core.base;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.Properties;

/**
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/24 17:00:57 $
 */
public interface IRecordSourceSerializer extends Serializable {
	
	/**
	 * Tests whether this serializer can return a serializable record source
	 * for the specified record source
	 */
	boolean canSerialize(String url);

	/**
	 * Tests whether this serializer can return a serializable record source
	 * for the specified record source
	 */
	boolean canSerialize(RecordSource rs);

	/**
	 * Returns serializable record source for the specified records source.
	 * @param rs
	 * @return
	 * @throws NotSerializableException
	 */
	ISerializableRecordSource getSerializableRecordSource(RecordSource rs)
		throws NotSerializableException;

	/**
	 * Returns serializable record source for the specified records source.
	 * @param properties that specify a record source 
	 * @return
	 * @throws NotSerializableException
	 */
	ISerializableRecordSource getSerializableRecordSource(Properties properties)
		throws NotSerializableException;

	Properties getProperties();
	
	void setProperties(Properties p);

}
