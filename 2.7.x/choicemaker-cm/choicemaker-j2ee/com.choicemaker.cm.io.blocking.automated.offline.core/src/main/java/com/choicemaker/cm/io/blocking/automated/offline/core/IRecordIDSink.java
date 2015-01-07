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
 * This writes the record ID to a file.  The record ID can be an Integer or Long or String.
 * 
 * @author pcheung
 *
 */
public interface IRecordIDSink extends ISink {
	
	/** This method writes the record ID object to the sink.
	 * 
	 * @param o
	 */
	public void writeRecordID (Comparable<?> o) throws BlockingException;
	
	
	/** This tells the sink the object type of the record id.  Call this method before calling the open
	 * method.
	 * 
	 * @param type
	 * @throws BlockingException
	 */
	public void setRecordIDType (RECORD_ID_TYPE type);

}
