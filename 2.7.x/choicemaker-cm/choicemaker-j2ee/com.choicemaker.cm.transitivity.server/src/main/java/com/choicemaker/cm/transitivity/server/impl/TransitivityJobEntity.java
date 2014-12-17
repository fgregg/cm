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
package com.choicemaker.cm.transitivity.server.impl;

import java.io.File;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.args.TransitivityParameters;
import com.choicemaker.cm.args.TransitivitySettings;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.BatchJobRigor;
import com.choicemaker.cm.batch.impl.BatchJobEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJob;

/**
 * A TransitivityJobEntity is a type of OabaJob that tracks the progress of a
 * (long-running) transitivity analysis process. Transitivity jobs use the match
 * results of an OABA OabaJob, so a transitivity job is always associated with
 * exactly one OABA OabaJob. The id of the OABA OabaJob is tracked by the value
 * of the {@link #getBatchParentId() transaction parent id} field.
 *
 * @author pcheung (original version)
 * @author rphall (migrated to JPA 2.0)
 */
@NamedQueries({
		@NamedQuery(name = TransitivityJobJPA.QN_TRANSITIVITY_FIND_ALL,
				query = TransitivityJobJPA.JPQL_TRANSITIVITY_FIND_ALL),
		@NamedQuery(
				name = TransitivityJobJPA.QN_TRANSITIVITY_FIND_ALL_BY_PARENT_ID,
				query = TransitivityJobJPA.JPQL_TRANSITIVITY_FIND_ALL_BY_PARENT_ID) })
@Entity
@DiscriminatorValue(value = TransitivityJobJPA.DISCRIMINATOR_VALUE)
public class TransitivityJobEntity extends BatchJobEntity implements
		TransitivityJob {

	private static final long serialVersionUID = 271L;

	/** Required by JPA; do not invoke directly */
	protected TransitivityJobEntity() {
		super();
	}

	public TransitivityJobEntity(TransitivityParameters params,
			TransitivitySettings settings, ServerConfiguration sc,
			OabaJob parent, String externalId) {
		this(TransitivityJobJPA.DISCRIMINATOR_VALUE, params.getId(), settings
				.getId(), sc.getId(), externalId, randomTransactionId(), parent
				.getId(), INVALID_ID, BatchJob.DEFAULT_RIGOR);
	}

	public TransitivityJobEntity(TransitivityParameters params,
			TransitivitySettings settings, ServerConfiguration sc,
			OabaJob parent, String externalId, BatchJobRigor bjr) {
		this(TransitivityJobJPA.DISCRIMINATOR_VALUE, params.getId(), settings
				.getId(), sc.getId(), externalId, randomTransactionId(), parent
				.getId(), INVALID_ID, bjr);
	}

	public TransitivityJobEntity(TransitivityJob o) {
		this(TransitivityJobJPA.DISCRIMINATOR_VALUE, o
				.getTransitivityParametersId(), o.getTransitivitySettingsId(),
				o.getServerId(), o.getExternalId(), o.getTransactionId(), o
						.getBatchParentId(), o.getUrmId(), o.getBatchJobRigor());
		this.workingDirectory = o.getWorkingDirectory().getAbsolutePath();
	}

	protected TransitivityJobEntity(String type, long paramsId,
			long settingsId, long serverId, String externalId, long tid,
			long parentId, long urmid, BatchJobRigor bjr) {
		super(type, paramsId, settingsId, serverId, externalId, tid, parentId,
				urmid, bjr);
	}

	public TransitivityJobEntity(TransitivityParametersEntity params,
			OabaJob job, String extId) {
		throw new Error("not implemented");
	}

	public TransitivityJobEntity(TransitivityParametersEntity params,
			BatchJobEntity job) {
		throw new Error("not implemented");
	}

	public TransitivityJobEntity(TransitivityParametersEntity params,
			BatchJob job, String extId) {
		throw new Error("not implemented");
	}

	void setWorkingDirectory(File workingDir) {
		final String wd = workingDir == null ? "null" : workingDir.toString();
		if (workingDir == null || !workingDir.exists()
				|| !workingDir.isDirectory()) {
			String msg =
				"Working directory '" + wd
						+ "' is null, does not exist, or is not a directory";
			throw new IllegalArgumentException(msg);
		}
		if (!workingDir.canRead() || !workingDir.canWrite()) {
			String msg =
				"Working directory '" + wd
						+ "' is not readable or not writeable";
			throw new IllegalArgumentException(msg);
		}
		this.workingDirectory = workingDir.getAbsolutePath();
	}

	@Override
	public long getTransitivityParametersId() {
		return super.getParametersId();
	}

	@Override
	public long getTransitivitySettingsId() {
		return super.getSettingsId();
	}

} // TransitivityJobEntity

