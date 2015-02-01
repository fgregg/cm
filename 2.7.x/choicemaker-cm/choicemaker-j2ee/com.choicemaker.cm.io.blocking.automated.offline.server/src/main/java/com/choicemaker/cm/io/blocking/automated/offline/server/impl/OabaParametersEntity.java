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
package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.DV_OABA;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.JPQL_OABAPARAMETERS_FIND_ALL;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA.QN_OABAPARAMETERS_FIND_ALL;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;

import com.choicemaker.cm.args.OabaLinkageType;
import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.PersistableRecordSource;

/**
 * @author pcheung (original version)
 * @author rphall (migrated to JPA 2.0)
 *
 */
@NamedQuery(name = QN_OABAPARAMETERS_FIND_ALL,
		query = JPQL_OABAPARAMETERS_FIND_ALL)
@Entity
@DiscriminatorValue(DV_OABA)
public class OabaParametersEntity extends AbstractParametersEntity implements
		Serializable, OabaParameters {

	private static final long serialVersionUID = 271L;

	// private static final Logger logger = Logger
	// .getLogger(OabaParametersEntity.class.getName());

	public static String dump(OabaParameters p) {
		return dump(DEFAULT_DUMP_TAG, p);
	}

	public static String dump(String tag, OabaParameters p) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);

		pw.println("Blocking parameters (" + tag + ")");
		if (p == null) {
			pw.println(tag + ": null batch parameters");
		} else {
			pw.println(tag + ": DIFFER threshold: " + p.getLowThreshold());
			pw.println(tag + ": MATCH threshold: " + p.getHighThreshold());
			pw.println(tag + ": Model configuration name: "
					+ p.getModelConfigurationName());
			final OabaLinkageType task = p.getOabaLinkageType();
			pw.print(tag + ": Linkage task: " + task);
			if (task == OabaLinkageType.STAGING_DEDUPLICATION) {
				pw.println(" (deduplicating a single record source)");
				pw.println(tag + ": Staging record source: " + p.getStageRsId());
				pw.println(tag + ": Staging record source type: "
						+ p.getStageRsType());
			} else if (task == OabaLinkageType.STAGING_TO_MASTER_LINKAGE) {
				pw.println(" (linking a staging source to a master source)");
				pw.println(tag + ": Staging record source: " + p.getStageRsId());
				pw.println(tag + ": Staging record source type: "
						+ p.getStageRsType());
				pw.println(tag + ": Master record source: " + p.getMasterRsId());
				pw.println(tag + ": Master record source type: "
						+ p.getMasterRsType());
			} else if (task == OabaLinkageType.MASTER_TO_MASTER_LINKAGE) {
				pw.println(" (linking a master source to a master source)");
				pw.println(tag + ": Master record source 1: "
						+ p.getStageRsId());
				pw.println(tag + ": Master record source 1 type: "
						+ p.getStageRsType());
				pw.println(tag + ": Master record source 2: "
						+ p.getMasterRsId());
				pw.println(tag + ": Master record source 2 type: "
						+ p.getMasterRsType());
			} else {
				throw new IllegalArgumentException("unexpected task type: "
						+ task);
			}
		}
		String retVal = sw.toString();
		return retVal;
	}

	/** Required by JPA; do not invoke directly */
	protected OabaParametersEntity() {
		super();
	}

	public OabaParametersEntity(String modelConfigurationName,
			float lowThreshold, float highThreshold,
			PersistableRecordSource stageRs) {
		this(modelConfigurationName, lowThreshold, highThreshold, stageRs,
				null, OabaLinkageType.STAGING_DEDUPLICATION);
	}

	public OabaParametersEntity(OabaParameters bp) {
		this(bp.getModelConfigurationName(), bp.getLowThreshold(), bp
				.getHighThreshold(), bp.getStageRsId(), bp.getStageRsType(), bp
				.getMasterRsId(), bp.getMasterRsType(), bp.getOabaLinkageType());
	}

	public OabaParametersEntity(String modelConfigurationName,
			float lowThreshold, float highThreshold,
			PersistableRecordSource stageRs, PersistableRecordSource masterRs,
			OabaLinkageType taskType) {
		this(modelConfigurationName, lowThreshold, highThreshold, stageRs
				.getId(), stageRs.getType(), masterRs == null ? null : masterRs
				.getId(), masterRs == null ? null : masterRs.getType(),
				taskType);
	}

	/**
	 * 
	 * @param modelConfigurationName
	 *            model configuration name
	 * @param lowThreshold
	 *            differ threshold
	 * @param highThreshold
	 *            match threshold
	 * @param sId
	 *            persistence id of the staging record source
	 * @param sType
	 *            the type of the staging record source (FlatFile, XML, DB)
	 * @param mId
	 *            the persistence id of the master record. Must be null if the
	 *            <code>taskType</code> is STAGING_DEDUPLICATION; otherwise must
	 *            be non-null.
	 * @param mType
	 *            the type of the master record source. Must be null if the
	 *            <code>taskType</code> is STAGING_DEDUPLICATION; otherwise must
	 *            be non-null.
	 * @param taskType
	 *            the record matching task: duplication of a staging source;
	 *            linkage of a staging source to a master source; or linkage of
	 *            two master sources.
	 */
	public OabaParametersEntity(String modelConfigurationName,
			float lowThreshold, float highThreshold, long sId, String sType,
			Long mId, String mType, OabaLinkageType taskType) {
		super(DV_OABA, modelConfigurationName, lowThreshold, highThreshold,
				sId, sType, mId, mType, taskType, null, null);
	}

	@Override
	public String getModelConfigurationName() {
		return modelConfigName;
	}

	@Deprecated
	@Override
	public String getStageModel() {
		return getModelConfigurationName();
	}

	@Deprecated
	@Override
	public String getMasterModel() {
		return getModelConfigurationName();
	}

	@Override
	public long getStageRsId() {
		return stageRsId;
	}

	@Override
	public String getStageRsType() {
		return stageRsType;
	}

	@Override
	public Long getMasterRsId() {
		return masterRsId;
	}

	@Override
	public String getMasterRsType() {
		return masterRsType;
	}

	// -- Identity

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		if (id == NONPERSISTENT_ID) {
			result = prime * result + hashCode0();
		}
		return result;
	}

	protected int hashCode0() {
		final int prime = 31;
		int result = 1;

		result = prime * result + Float.floatToIntBits(highThreshold);
		result = prime * result + Float.floatToIntBits(lowThreshold);
		result =
			prime
					* result
					+ ((masterRsId == null) ? 0
							: (int) (masterRsId ^ (masterRsId >>> 32)));
		result =
			prime * result
					+ ((masterRsType == null) ? 0 : masterRsType.hashCode());
		result =
			prime
					* result
					+ ((modelConfigName == null) ? 0 : modelConfigName
							.hashCode());
		result = prime * result + (int) (stageRsId ^ (stageRsId >>> 32));
		result =
			prime * result
					+ ((stageRsType == null) ? 0 : stageRsType.hashCode());
		result = prime * result + ((task == null) ? 0 : task.hashCode());

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		OabaParametersEntity other = (OabaParametersEntity) obj;
		if (id != other.id) {
			return false;
		}
		if (id == NONPERSISTENT_ID) {
			return equals0(other);
		}
		return true;
	}

	protected boolean equals0(OabaParametersEntity other) {
		assert other != null;
		if (Float.floatToIntBits(highThreshold) != Float
				.floatToIntBits(other.highThreshold)) {
			return false;
		}
		if (Float.floatToIntBits(lowThreshold) != Float
				.floatToIntBits(other.lowThreshold)) {
			return false;
		}
		if (masterRsId != other.masterRsId) {
			return false;
		}
		if (masterRsType == null) {
			if (other.masterRsType != null) {
				return false;
			}
		} else if (!masterRsType.equals(other.masterRsType)) {
			return false;
		}
		if (modelConfigName == null) {
			if (other.modelConfigName != null) {
				return false;
			}
		} else if (!modelConfigName.equals(other.modelConfigName)) {
			return false;
		}
		if (stageRsId != other.stageRsId) {
			return false;
		}
		if (stageRsType == null) {
			if (other.stageRsType != null) {
				return false;
			}
		} else if (!stageRsType.equals(other.stageRsType)) {
			return false;
		}
		if (task == null) {
			if (other.task != null) {
				return false;
			}
		} else if (!task.equals(other.task)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "OabaParametersEntity [id=" + id + ", modelId="
				+ modelConfigName + ", lowThreshold=" + lowThreshold
				+ ", highThreshold=" + highThreshold + ", task=" + task + "]";
	}

}
