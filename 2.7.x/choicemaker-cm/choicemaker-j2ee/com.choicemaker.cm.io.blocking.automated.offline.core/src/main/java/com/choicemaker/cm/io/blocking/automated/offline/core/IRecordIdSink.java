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
package com.choicemaker.cm.io.blocking.automated.offline.core;

import com.choicemaker.cm.core.BlockingException;

/**
 * This writes the record identifiers to a file. The record ID can be an Integer
 * or Long or String.
 * 
 * @author pcheung
 *
 */
public interface IRecordIdSink extends ISink {

	/**
	 * Writes a record identifier to this sink.
	 */
	public void writeRecordID(Comparable<?> o) throws BlockingException;

	/**
	 * This tells the sink the object type of the record id. Call this method
	 * before calling the open method.
	 * 
	 * @param type
	 * @throws IllegalArgumentException
	 *             if this method is invoked with a type that is null or not
	 *             identical to the type specified in a previous invocation.
	 */
	public void setRecordIDType(RECORD_ID_TYPE type);

	/**
	 * @throws IllegalStateException
	 *             if the type of record identifier has not been
	 *             {@link #setRecordIDType(RECORD_ID_TYPE) set}
	 */
	public RECORD_ID_TYPE getRecordIdType();

}
