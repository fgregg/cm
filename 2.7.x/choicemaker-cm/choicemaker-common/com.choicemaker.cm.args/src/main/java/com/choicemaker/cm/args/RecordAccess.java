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
 * Parameters used to access records in a database. These parameters are
 * specific to a particular ChoiceMaker model; in particular, they depend on the
 * model schema, which describes the layout of logical records in memory and
 * physical records in a database. These parameters may also be specific to a
 * particular database type, or they may apply to broad categories of database
 * types that conform to some standard. This interface defines several
 * well-known database types, but others may be defined elsewhere.
 * 
 * @author rphall
 *
 */
public interface RecordAccess extends Serializable {

	String DB_TYPE_FLATFILE = "FLATFILE";
	String DB_TYPE_XML = "XML";
	String DB_TYPE_ORACLE = "ORACLE";
	String DB_TYPE_SQLSERVER = "SQLSERVER";
	String DB_TYPE_POSTGRES = "POSTGRES";
	String DB_TYPE_JDBC = "JDBC";

	String getModelName();

	String getDatabaseType();

	String getDatabaseAccessorName();

	String getDatabaseConfigurationName();

	String getBlockingConfigurationName();

}
