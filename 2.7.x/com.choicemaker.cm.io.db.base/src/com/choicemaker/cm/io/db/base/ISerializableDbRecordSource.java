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
package com.choicemaker.cm.io.db.base;

import com.choicemaker.cm.core.ISerializableRecordSource;


/**
 * A DbRecordSource that can be serialized. Implementors should define
 * a nullity constructor and a one-parameter constructor that takes
 * a Properties object. Most serializable database record sources require
 * at least 4 properties to be configured before they are used:<ul>
 * <li/>{@link #PN_DATABASE_CONFIG dbConfig}
 * <li/>{@link #PN_DATASOURCE_JNDI_NAME dsJndiName}
 * <li/>{@line #PN_MODEL_NAME modelName}
 * <li/>{@link #PN_SQL_QUERY sqlQuery}</ul>
 * The parameterized constructor for a subclass should throw
 * a IncompleteSpecificationException if the specified set of
 * properties is incomplete:<pre>
 * public SomeSubClass implements ISerializableRecordSource {
 *     public SomeSubClass() {}
 *     public SomeSubClass(Properties p)
 *         throws IncompleteSpecificationException {
 *         // ... ctor method should check properties for completeness
 *     }
 *     // ... other methods
 * }
 * </pre>
 * @author rphall
 *
 */
public interface ISerializableDbRecordSource extends ISerializableRecordSource {
	
	/**
	 * The name of the property that specifies the ChoiceMaker database configuration,
	 * <code>dbConfig</code>.
	 */
	public static final String PN_DATABASE_CONFIG = "dbConfig";
	
	/**
	 * The name of the property that specifies the data source JNDI name,
	 * <code>dsJndiName</code>.
	 */
	public static final String PN_DATASOURCE_JNDI_NAME = "dsJndiName";

	/**
	 * The name of the property that specifies a SQL SELECT query,
	 * <code>sqlQuery</code>.  This query may be used to filter the
	 * records returned from a data source.
	 */
	public static final String PN_SQL_QUERY = "sqlQuery";

	/**
	 * The name of a property that specifies a buffer size used by
	 * certain implementations of DbRecordSource,
	 * <code>bufferSize</code>.
	 */
	public static final String PN_BUFFER_SIZE = "bufferSize";

}
