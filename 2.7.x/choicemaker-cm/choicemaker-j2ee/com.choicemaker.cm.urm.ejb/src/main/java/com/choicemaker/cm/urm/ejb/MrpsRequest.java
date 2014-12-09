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

import javax.persistence.EntityManager;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.MarkedRecordPairSink;
import com.choicemaker.cm.core.SerializableRecordSource;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.server.util.DatabaseUtils;
import com.choicemaker.cm.io.xml.base.XmlMarkedRecordPairSink;
import com.choicemaker.cm.urm.exceptions.CmRuntimeException;
import com.choicemaker.cm.urm.exceptions.ConfigException;
import com.choicemaker.util.StringUtils;

/**
 * An MrpsRequest contains the information necessary to start
 * the back-end processing of an MRPS job.
 * @see MrpsBackend
 */
@SuppressWarnings({"rawtypes"})
public class MrpsRequest implements IMrpsRequest {

	private static final long serialVersionUID = 1L;
	private static final String MRPS_SUFFIX = "mrps";
	private static final String DATA_SUFFIX = "xml";

	private final Long mrpsConvJobId;
	private final Long oabaJobId;
	private final String externalId;
	private final String mrpsFilename;
	private final MrpsRequestConfiguration configuration;

	private transient OabaParameters oabaParameters = null;
	private transient IProbabilityModel stagingModel = null;
	private transient IMatchRecord2Source matchPairs = null;
	private transient SerializableRecordSource rsStaging = null;
	private transient SerializableRecordSource rsMaster = null;
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

	private OabaParameters getBatchParameters(EntityManager em)
		throws CmRuntimeException, ConfigException {
		if (em == null) {
			throw new IllegalArgumentException("null entity manager");
		}
		OabaParameters retVal = this.oabaParameters;
		if (retVal == null) {
			this.oabaParameters =
				Single.getInst().findBatchParamsById(em,
					this.oabaJobId.longValue());
			retVal = this.oabaParameters;
		}
		// Postcondition
		if (retVal == null) {
			throw new IllegalStateException("null batch parameters");
		}
		return retVal;
	}

//	private String getStagingModelName(EntityManager em)
//		throws CmRuntimeException, ConfigException, RemoteException {
//		OabaParameters bp = getBatchParameters(em);
//		String retVal = bp.getStageModel();
//		// Postcondition
//		if (retVal == null) {
//			throw new IllegalStateException("null staging accessProvider name");
//		}
//		return retVal;
//	}

	public IProbabilityModel getStagingModel(EntityManager em)
		throws CmRuntimeException, ConfigException, RemoteException {
		IProbabilityModel retVal = this.stagingModel;
		if (retVal == null) {
			OabaParameters bp = getBatchParameters(em);
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
	public MarkedRecordPairSink getMarkedRecordPairSink(EntityManager em)
		throws CmRuntimeException, ConfigException, RemoteException {
		MarkedRecordPairSink retVal = this.mrps;
		if (retVal == null) {
			String xmlFilename = createMrpsDataFileName(this.mrpsFilename);
			this.mrps =
				new XmlMarkedRecordPairSink(
					this.mrpsFilename,
					xmlFilename,
					this.getStagingModel(em));
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
	public IMatchRecord2Source getMatchPairs(EntityManager em)
		throws CmRuntimeException, ConfigException, RemoteException {
		IMatchRecord2Source retVal = this.matchPairs;
		if (retVal == null) {
			throw new Error("not yet implemented");
//			String stagingModelName = this.getStagingModelName(em);
//			this.matchPairs =
//					OabaFileUtils.getCompositeMatchSource(this.oabaJobId.longValue());
//			retVal = this.matchPairs;
//		}
//		// Postcondition
//		if (retVal == null) {
//			throw new IllegalStateException("null match pairs");
		}
		return retVal;
	}

	/**
	 * @return
	 */
	public SerializableRecordSource getRsMaster(EntityManager em)
		throws CmRuntimeException, ConfigException, RemoteException {
		SerializableRecordSource retVal = this.rsMaster;
		if (retVal == null) {
			OabaParameters bp = getBatchParameters(em);
			this.rsMaster = DatabaseUtils.getRecordSource(bp.getMasterRs());
			retVal = this.rsMaster;
		}
		// Postcondition: master record source may be null
		return retVal;
	}

	/**
	 * @return
	 */
	public SerializableRecordSource getRsStage(EntityManager em)
		throws CmRuntimeException, ConfigException, RemoteException {
		SerializableRecordSource retVal = this.rsStaging;
		if (retVal == null) {
			OabaParameters bp = getBatchParameters(em);
			this.rsStaging = DatabaseUtils.getRecordSource(bp.getStageRs());
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

