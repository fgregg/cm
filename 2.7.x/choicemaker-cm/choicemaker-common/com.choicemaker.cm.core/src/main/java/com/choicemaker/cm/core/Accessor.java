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

import java.io.Serializable;


/**
 * Provides access to the clue set and other generated features.
 * <p>
 * This interface is implemented by the generated accessor class, which
 * also implements the translator accessor interfaces of the configured
 * I/O components.
 * <p>
 * Access to the <code>ClueSet</code> goes through this interface for two reasons.
 * First, the implementing class can decide whether to create a new
 * <code>ClueSet</code> for each call or always return the same singleton. The
 * former is useful if a <code>ClueSet</code> caches data in fields and is not
 * thread safe.
 * <p>
 * Second, this design simplifies the reloading of modified generated
 * classes into the same virtual machine during training.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/27 21:29:07 $
 * @see       ProbabilityModel
 */
public interface Accessor extends Serializable {
	/**
	 * Returns an instance of the associated <code>ClueSet</code>.
	 *
	 * A returned instance may only be used by one single thread.
	 *
	 * @return   An instance of the associated <code>ClueSet</code>.
	 */
	ClueSet getClueSet();

	/**
	 * Returns an instance of the associated <code>Descriptor</code>.
	 *
	 * @return   an instance of the associated <code>Descriptor</code> 
	 */
	Descriptor getDescriptor();

	/**
	 * Returns the name of the schema file used for creating this accessor.
	 * Used checking whether file has changed since last compilation.
	 *
	 * @return   The name of the schema file used for creating this accessor.
	 */
	String getSchemaFileName();

	/**
	 * The creation (compilation) date in seconds since 1/1/1970.
	 * 
	 * @see      java.util.Date#getTime
	 * @return   The creation (compilation) date in seconds since 1/1/1970.
	 */
	long getCreationDate();

	/**
	 * The number of record types in the schema.
	 *
	 * @return   The number of record types in the schema.
	 */
	int getNumRecordTypes();

	/**
	 * Returns the name of the schema used for generating this accessor.
	 * E.g., for Sample.schema, this returns Sample.
	 *
	 * @return   The name of the schema.
	 */
	String getSchemaName();

	/**
	 * Returns the name of the clue set used for generating this accessor.
	 *
	 * @return   The name of the clue set.
	 */
	String getClueSetName();
	
	Object toHolder(Record r);
	
	
	/**
	 * This returns Elmer's IRecordHolder for the record.
	 * @param r
	 * @return
	 */
	Object toRecordHolder(Record r);
	
	Record toImpl(Object o);
}
