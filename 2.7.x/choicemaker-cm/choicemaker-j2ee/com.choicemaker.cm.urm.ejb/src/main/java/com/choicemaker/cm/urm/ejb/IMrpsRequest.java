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
package com.choicemaker.cm.urm.ejb;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Properties;

import javax.persistence.EntityManager;

import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.MarkedRecordPairSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordSourceController;
import com.choicemaker.cm.urm.exceptions.CmRuntimeException;
import com.choicemaker.cm.urm.exceptions.ConfigException;

/**
 * An MrpsRequest contains the information necessary to start the back-end
 * processing of an MRPS job.
 * 
 * @see MrpsBackend
 */
@SuppressWarnings({ "rawtypes" })
interface IMrpsRequest extends Serializable, Cloneable {

	MarkedRecordPairSink getMarkedRecordPairSink(EntityManager em)
			throws CmRuntimeException, ConfigException, RemoteException;

	String getExternalId();

	Long getMrpsConvJobId();

	Long getOabaJobId();

	IMatchRecord2Source getMatchPairs(EntityManager em)
			throws CmRuntimeException, ConfigException, RemoteException;

	PersistableRecordSource getRsMaster(EntityManager em,
			RecordSourceController prsc)
			throws CmRuntimeException, ConfigException, RemoteException;

	PersistableRecordSource getRsStage(EntityManager em,
			RecordSourceController prsc)
			throws CmRuntimeException, ConfigException, RemoteException;

	IProbabilityModel getStagingModel(EntityManager em)
			throws CmRuntimeException, ConfigException, RemoteException;

	// From the Configurable interface

	/** Returns all the properties specified by this object */
	Properties getProperties();

	/** Sets some properties for this object */
	void setProperties(Properties p);

}
