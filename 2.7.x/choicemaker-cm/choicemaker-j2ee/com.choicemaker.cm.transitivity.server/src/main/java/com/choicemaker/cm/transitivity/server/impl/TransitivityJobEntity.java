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

import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.args.TransitivityParameters;
import com.choicemaker.cm.args.TransitivitySettings;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.impl.BatchJobEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersEntity;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJob;

/**
 * A TransitivityJobEntity is a type of OabaJob that tracks the progress of a
 * (long-running) transitivity analysis process. Transitivity jobs use the match
 * results of an OABA OabaJob, so a transitivity job is always associated with
 * exactly one OABA OabaJob. The id of the OABA OabaJob is tracked by the
 * value of the {@link #getBatchParentId() transaction parent id} field.
 *
 * @author pcheung (original version)
 * @author rphall (migrated to JPA 2.0)
 */
// @NamedQueries({
//		@NamedQuery(name = TransitivityJobJPA.QN_TRANSITIVITY_FIND_ALL,
//				query = TransitivityJobJPA.JPQL_TRANSITIVITY_FIND_ALL),
//		@NamedQuery(
//				name = TransitivityJobJPA.QN_TRANSITIVITY_FIND_ALL_BY_PARENT_ID,
//				query = TransitivityJobJPA.JPQL_TRANSITIVITY_FIND_ALL_BY_PARENT_ID) })
//@Entity
//@DiscriminatorValue(value = TransitivityJobJPA.DISCRIMINATOR_VALUE)
public class TransitivityJobEntity extends OabaJobEntity implements
		TransitivityJob {

	private static final long serialVersionUID = 271L;

	/** Required by JPA; do not invoke directly */
	protected TransitivityJobEntity() {
		super();
	}

	public TransitivityJobEntity(TransitivityParameters params,
			TransitivitySettings settings, ServerConfiguration sc, OabaJob parent, String externalId) {
		this(TransitivityJobJPA.DISCRIMINATOR_VALUE, params.getId(), settings
				.getId(), sc.getId(), externalId, randomTransactionId(), parent.getId(),
				INVALID_ID);
	}
	
	public TransitivityJobEntity(TransitivityJob o) {
		this(TransitivityJobJPA.DISCRIMINATOR_VALUE, o.getParametersId(), o
				.getSettingsId(), o.getServerId(), o.getExternalId(), o.getTransactionId(), o
				.getBatchParentId(), o.getUrmId());
		this.workingDirectory = o.getWorkingDirectory().getAbsolutePath();
	}

	protected TransitivityJobEntity(String type, long paramsId, long settingsId,
			long serverId, 
			String externalId, long tid, long parentId, long urmid) {
		super(type, paramsId, settingsId, serverId, externalId, tid, parentId, urmid);
	}
	
	public TransitivityJobEntity(OabaParametersEntity params, OabaJob job,
			String extId) {
		throw new Error("not implemented");
	}

	public TransitivityJobEntity(OabaParametersEntity params, BatchJobEntity job) {
		throw new Error("not implemented");
	}

	public TransitivityJobEntity(OabaParametersEntity params, BatchJob job,
			String extId) {
		throw new Error("not implemented");
	}

	@Override
	public String getModel() {
		throw new Error("not implemented");
	}

	@Override
	public float getDiffer() {
		throw new Error("not implemented");
	}

	@Override
	public float getMatch() {
		throw new Error("not implemented");
	}

	@Override
	public void setModel(String stageModelName) {
		throw new Error("not implemented");
	}

	@Override
	public void setMatch(float high) {
		throw new Error("not implemented");
	}

	@Override
	public void setDiffer(float low) {
		throw new Error("not implemented");
	}

} // TransitivityJobEntity

