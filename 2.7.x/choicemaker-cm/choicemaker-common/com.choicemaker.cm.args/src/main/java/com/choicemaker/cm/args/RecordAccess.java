/*
 * Copyright (c) 2015 ChoiceMaker LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License
 * v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ChoiceMaker LLC - initial API and implementation
 */
package com.choicemaker.cm.args;

import java.io.Serializable;

/**
 * Parameters used to retrieve records from a database. These parameters are
 * specific to a particular ChoiceMaker model; in particular, they depend on the
 * model schema, which describes the layout of logical records in memory and
 * physical records in a database. These parameters may also be specific to a
 * particular type of database type. This interface defines several well-known
 * database types, but others may be defined elsewhere.
 * <p>
 * The <code>modelName</code> and <code>databaseType</code> properties should be
 * treated as documentation, rather than as references or foreign keys to other
 * ChoiceMaker types. Implementations of this interface are not required to
 * enforce consistency between the model name and the blocking, query or
 * reference configurations. Similarly, implementations are not required to
 * enforce consistency between the database type and the database accessor
 * class.
 * </p>
 *
 * @author rphall
 *
 */
public interface RecordAccess extends PersistentObject, Serializable {

	String DB_TYPE_FLATFILE = "FLATFILE";
	String DB_TYPE_XML = "XML";
	String DB_TYPE_ORACLE = "ORACLE";
	String DB_TYPE_SQLSERVER = "SQLSERVER";
	String DB_TYPE_POSTGRES = "POSTGRES";
	String DB_TYPE_JDBC = "JDBC";

	/** The name of a ChoiceMaker matching model */
	String getModelName();

	/**
	 * Returns the name of a blocking configuration used by this record
	 * accessor. The blocking configuration must be defined by the model used by
	 * this instance, but this constraint may not be enforced by implementations
	 * of this class.
	 */
	String getBlockingConfiguration();

	/**
	 * Returns the type of record source used to retrieve query records.
	 * @see PersistableRecordSource#getType()
	 */
	String getQueryType();

	/**
	 * Returns the class name of the database accessor used by this record
	 * accessor to retrieve query records.
	 */
	String getQueryAccessor();

	/**
	 * Returns the name of a database configuration used by this record accessor
	 * to retrieve query records. The database configuration must be defined by
	 * the model used by this instance, but this constraint may not be enforced
	 * by implementations of this class.
	 */
	String getQueryConfiguration();

	/**
	 * Returns the type of record source used to retrieve reference records.
	 * @see PersistableRecordSource#getType()
	 */
	String getReferenceType();

	/**
	 * Returns the class name of the database accessor used by this record
	 * accessor to retrieve reference records.
	 */
	String getReferenceAccessor();

	/**
	 * Returns the name of a database configuration used by this record accessor
	 * to retrieve reference records. The database configuration must be defined
	 * by the model used by this instance, but this constraint may not be
	 * enforced by implementations of this class.
	 */
	String getReferenceConfiguration();

}
