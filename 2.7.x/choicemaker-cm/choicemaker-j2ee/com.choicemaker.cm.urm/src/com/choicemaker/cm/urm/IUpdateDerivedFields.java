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
package com.choicemaker.cm.urm;

import java.io.IOException;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import javax.sql.DataSource;

import com.choicemaker.cm.urm.base.DbRecordCollection;

/**
 * An interface implemented by model-specific code to update
 * values of derived fields that are stored in some persistent storage
 * accessible via JDBC connections (i.e. a database).</p>
 * <p>
 * Note that derived fields that are used in online blocking
 * must be persistent.</p>
 * 
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/24 23:58:33 $
 */
public interface IUpdateDerivedFields {
	
	/**
	 * The property name used to register an implementation of this interface,
	 * i.e. <code>com.choicemaker.cm.urm.updateDerivedFields</code>.
	 * <p>
	 * Implementations are an optional property of individual model
	 * configurations. Currently, registrations are defined in the
	 * productionProbabilityModels section of a project configuration
	 * file (e.g. project.xml).</p>
	 * <p>
	 * If a model configuration does not define an implementation of this
	 * interface, then a default, DoNothing implementation is invoked
	 * where needed.
	 * @see CmServerAdmin#updateAllDerivedFields(String,DbRecordCollection)
	 * @see CmServerAdmin#updateDerivedFields(String,DbRecordCollection)
	 */
	String PN_MODEL_CONFIGURATION_UPDATOR_DELEGATE = "com.choicemaker.cm.urm.updateDerivedFields";

	/**
	 * Updates derived fields for records in which raw fields
	 * have changed but for which derived fields have not
	 * been recalculated.
	 * <p>
	 * This method should be implemented by installations
	 * that do not automatically update dirty derived fields
	 * via database triggers and stored procedures. A standard
	 * optimization is to mark records that have changed
	 * with a dirty flag, so that derived fields of unchanged records
	 * are not recalculated.</p>
	 * <p>
	 * Installations that use database triggers and stored procedures
	 * to update derived fields should implement a NOP method
	 * that returns zero.</p>
	 * 
	 * @param dataSource Provides access to a JDBC connection that should
	 * be used to update derived fields in the persistent storage. The data source is
	 * guaranteed to be non-null, but there is no guarantee that it is properly
	 * configured. For example, because of a configuration error, it may provide
	 * access to a database that is missing tables expected by the update method.
	 *
	 * @return the number of records for which derived fields were updated
	 * 
	 * @throws	SQLException If a SQL error prevents
	 * 			ChoiceMaker from fulfilling the request.
	 * @throws	IOException  If a communication problem occurs.
	 */
	int updateDirtyDerivedFields(DataSource dataSource)
		throws SQLException, IOException;

	/**
	 * Updates derived fields for all records, regardless of whether they
	 * have changed.</p>
	 * <p>
	 * Installations that use database triggers and stored procedures
	 * to update derived fields may implement a NOP method
	 * that returns zero. Otherwise they should trigger the stored
	 * procedures to recalculate all derived fields on all records and
	 * then return a non-zero count of the records that were updated.</p>
	 *
	 * @param dataSource Provides access to a JDBC connection that should
	 * be used to update derived fields in the persistent storage. The data source is
	 * guaranteed to be non-null, but there is no guarantee that it is properly
	 * configured. For example, because of a configuration error, it may provide
	 * access to a database that is missing tables expected by the update method.
	 *
	 * @return the number of records for which derived fields were updated
	 * 
	 * @throws	SQLException If a SQL error prevents
	 * 			ChoiceMaker from fulfilling the request.
	 * @throws	IOException  If a communication problem occurs.
	 */
	int updateAllDerivedFields(DataSource dataSource)
		throws SQLException, IOException;

	/**
	 * A method that is called by the URM in order to update persistent fields
	 * of the record with the specified id.</p>
	 * <p>
	 * Installations that use database triggers and stored procedures
	 * to update derived fields may implement a NOP method
	 * that returns zero. Otherwise they should trigger the stored
	 * procedures to recalculate all derived fields on the specified record and
	 * then return a count of 1 (one).</p>
	 *
	 * @see com.choicemaker.cm.core.Profile#getRecord()
	 * @see com.choicemaker.cm.core.Record#getId()
	 * 
	 * @param dataSource Provides access to a JDBC connection that should
	 * be used to update derived fields in the persistent storage. The data source is
	 * guaranteed to be non-null, but there is no guarantee that it is properly
	 * configured. For example, because of a configuration error, it may provide
	 * access to a database that is missing tables expected by the update method.
	 * 
	 * @param id the key that identifies the record that should be updated.
	 * 
	 * @return the number of records for which derived fields were updated (0 or 1 expected)
	 * 
	 * @throws  NoSuchElementException if the specified record does not exist
	 * @throws	SQLException If a SQL error prevents
	 * 			ChoiceMaker from fulfilling the request.
	 * @throws	IOException  If a communication problem occurs.
	 */
	int updateDerivedFields(DataSource dataSource, Comparable id)
		throws SQLException, IOException;

}
