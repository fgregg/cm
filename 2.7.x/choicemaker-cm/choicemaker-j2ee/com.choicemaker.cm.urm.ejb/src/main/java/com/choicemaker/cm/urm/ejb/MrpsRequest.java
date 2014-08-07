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

import java.rmi.RemoteException;
import java.util.Properties;

import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.MarkedRecordPairSink;
import com.choicemaker.cm.core.SerialRecordSource;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OABAConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchParameters;
import com.choicemaker.cm.io.xml.base.XmlMarkedRecordPairSink;
import com.choicemaker.cm.urm.exceptions.CmRuntimeException;
import com.choicemaker.cm.urm.exceptions.ConfigException;
import com.choicemaker.util.StringUtils;

/**
 * An MrpsRequest contains the information necessary to start
 * the back-end processing of an MRPS job.
 * @see MrpsBackend
 */
public class MrpsRequest implements IMrpsRequest {

	private static final String MRPS_SUFFIX = "mrps";
	private static final String DATA_SUFFIX = "xml";

	private final Long mrpsConvJobId;
	private final Long oabaJobId;
	private final String externalId;
	private final String mrpsFilename;
	private final MrpsRequestConfiguration configuration;

	private transient BatchParameters batchParameters = null;
	private transient IProbabilityModel stagingModel = null;
	private transient IMatchRecord2Source matchPairs = null;
	private transient SerialRecordSource rsStaging = null;
	private transient SerialRecordSource rsMaster = null;
	private transient MarkedRecordPairSink mrps = null;

	/**
	 * This constructor creates an instance with an "infinite" batchSize
	 * (e.g. Integer.MAX_VALUE).
	 */
	public MrpsRequest(
		long mrpsConvJobId,
		long oabaJobId,
		String externalId,
		String mrpsFilename) {
		this(
			mrpsConvJobId,
			oabaJobId,
			externalId,
			mrpsFilename,
			new MrpsRequestConfiguration());
	}

	/**
	 * The OABA Job associated with this request contains information about
	 * the staging and master models used during matching. The models must
	 * reference the same ChoiceMaker record schema.
	 */
	public MrpsRequest(
		long mrpsConvJobId,
		long oabaJobId,
		String externalId,
		String mrpsFilename,
		MrpsRequestConfiguration configuration) {

		this.mrpsConvJobId = new Long(mrpsConvJobId);
		this.oabaJobId = new Long(oabaJobId);
		this.externalId = externalId;
		this.mrpsFilename = mrpsFilename;
		this.configuration = configuration;

		// Postconditions
		if (!StringUtils.nonEmptyString(this.mrpsFilename)) {
			throw new IllegalArgumentException("blank or null MRPS file name");
		}
		if (this.configuration == null) {
			throw new IllegalArgumentException("null configuration");
		}
	}

	private BatchParameters getBatchParameters()
		throws CmRuntimeException, ConfigException {
		BatchParameters retVal = this.batchParameters;
		if (retVal == null) {
			this.batchParameters =
				Single.getInst().findBatchParamsById(
					this.oabaJobId.longValue());
			retVal = this.batchParameters;
		}
		// Postcondition
		if (retVal == null) {
			throw new IllegalStateException("null batch parameters");
		}
		return retVal;
	}

	private String getStagingModelName()
		throws CmRuntimeException, ConfigException, RemoteException {
		BatchParameters bp = getBatchParameters();
		String retVal = bp.getStageModel();
		// Postcondition
		if (retVal == null) {
			throw new IllegalStateException("null staging accessProvider name");
		}
		return retVal;
	}

	public IProbabilityModel getStagingModel()
		throws CmRuntimeException, ConfigException, RemoteException {
		IProbabilityModel retVal = this.stagingModel;
		if (retVal == null) {
			BatchParameters bp = getBatchParameters();
			String stagingModelName = bp.getStageModel();
			this.stagingModel = PMManager.getModelInstance(stagingModelName);
			retVal = this.stagingModel;
		}
		// Postcondition
		if (retVal == null) {
			throw new IllegalStateException("null staging accessProvider");
		}
		return retVal;
	}

	private static String createMrpsDataFileName(String mrpsFileName) {
		String s = mrpsFileName.trim().toLowerCase();
		int index = s.lastIndexOf(MRPS_SUFFIX);
		if (index != -1) {
			String retVal = s.substring(0, index);
			retVal = retVal + DATA_SUFFIX;
			return retVal;
		} else if (-1 != s.lastIndexOf(DATA_SUFFIX)) {
			return mrpsFileName;
		} else
			throw new IllegalStateException(
				"invalid file mrps name '" + mrpsFileName + "'");

	}

	/**
	 * @return a non-null instance of MarkedRecordPairSink
	 */
	public MarkedRecordPairSink getMarkedRecordPairSink()
		throws CmRuntimeException, ConfigException, RemoteException {
		MarkedRecordPairSink retVal = this.mrps;
		if (retVal == null) {
			String xmlFilename = createMrpsDataFileName(this.mrpsFilename);
			this.mrps =
				new XmlMarkedRecordPairSink(
					this.mrpsFilename,
					xmlFilename,
					this.getStagingModel());
			retVal = this.mrps;
		}
		// Postcondition
		if (retVal == null) {
			throw new IllegalStateException("null marked record pair sink");
		}
		return retVal;
	}

	/**
	 * @return
	 */
	public String getExternalId() {
		return externalId;
	}

	/**
	 * @return
	 */
	public Long getMrpsConvJobId() {
		return mrpsConvJobId;
	}

	/**
	 * @return
	 */
	public Long getOabaJobId() {
		return this.oabaJobId;
	}

	/**
	 * @return a non-null instance of IMatchRecord2Source
	 */
	public IMatchRecord2Source getMatchPairs()
		throws CmRuntimeException, ConfigException, RemoteException {
		IMatchRecord2Source retVal = this.matchPairs;
		if (retVal == null) {
			BatchParameters bp = getBatchParameters();
			String stagingModelName = this.getStagingModelName();
			OABAConfiguration config =
				new OABAConfiguration(
					stagingModelName,
					this.oabaJobId.longValue());
			this.matchPairs =
				config.getCompositeMatchSource(this.oabaJobId.longValue());
			retVal = this.matchPairs;
		}
		// Postcondition
		if (retVal == null) {
			throw new IllegalStateException("null match pairs");
		}
		return retVal;
	}

	/**
	 * @return
	 */
	public SerialRecordSource getRsMaster()
		throws CmRuntimeException, ConfigException, RemoteException {
		SerialRecordSource retVal = this.rsMaster;
		if (retVal == null) {
			BatchParameters bp = getBatchParameters();
			this.rsMaster = bp.getMasterRs();
			retVal = this.rsMaster;
		}
		// Postcondition: master record source may be null
		return retVal;
	}

	/**
	 * @return
	 */
	public SerialRecordSource getRsStage()
		throws CmRuntimeException, ConfigException, RemoteException {
		SerialRecordSource retVal = this.rsStaging;
		if (retVal == null) {
			BatchParameters bp = getBatchParameters();
			this.rsStaging = bp.getStageRs();
			retVal = this.rsStaging;
		}
		// Postcondition -- staging record source may be null?
		if (retVal == null) {
			throw new IllegalStateException("null staging record source");
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.configure.Configureable#getVersion()
	 */
	public String getVersion() {
		return this.configuration.getVersion();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.configure.Configureable#getProperties()
	 */
	public Properties getProperties() {
		return this.configuration.getProperties();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.configure.Configureable#setProperties(java.util.Properties)
	 */
	public void setProperties(Properties p) {
		this.configuration.setProperties(p);
	}

} // MrpsRequest

