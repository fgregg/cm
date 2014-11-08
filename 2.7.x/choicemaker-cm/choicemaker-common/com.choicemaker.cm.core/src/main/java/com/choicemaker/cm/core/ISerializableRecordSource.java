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
package com.choicemaker.cm.core;

import java.util.Properties;


/**
 * A RecordSource that can be serialized. All serializable record sources
 * require at least a model name to be specified to be configured before they
 * are used. See {@link #setProperties(Properties)}. Extensions of this
 * interface define additional, type-specific property names:
 * <ul>
 * <li/>{@link com.choicemaker.cm.io.db.base.ISerializableDbRecordSource
 * Database record sources}
 * <li/>
 * {@link com.choicemaker.cm.core.io.db.base.ISerializableFileBasedRecordSource
 * File-based record sources}
 * </ul>
 * 
 * @author rphall
 *
 */
public interface ISerializableRecordSource extends SerializableRecordSource {
	
	/**
	 * The name of the property that specifies the probability model name,
	 * <code>modelName</code>.
	 */
	public static final String PN_MODEL_NAME = "modelName";
	
	/**
	 * Gets a copy of the current serializer configuration.
	 * @return non-null collection of properties
	 */
	public Properties getProperties();
	
	/**
	 * Sets (or resets) <em>all</em> the properties of a serializer.
	 * @param properties non-null collection of properties
	 * @throws IncompleteSpecificationException if the specified
	 * collection is missing a property required to fully configure
	 * the serializer
	 */
	public void  setProperties(Properties properties) throws IncompleteSpecificationException;
	
	/**
	 * Returns an XML representation of the serializer that is
	 * sufficient to completely specify the serializer configuration.
	 */
	public String toXML();

}
