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

import java.util.Date;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.TransitivityJob;

/**
 * A TransitivityJobBean is a type of BatchJob that tracks the progress of a
 * (long-running) transitivity analysis process. Transitivity jobs use the match
 * results of an OABA BatchJob, so a transitivity job is always associated with
 * exactly one OABA BatchJob. The id of the OABA BatchJob is tracked by the
 * value of the {@link #getBatchParentId() transaction parent id} field.
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
public class TransitivityJobBean extends BatchJobBean implements
		TransitivityJob {

	private static final long serialVersionUID = 271L;

	/** Required by JPA; do not invoke directly */
	protected TransitivityJobBean() {
		super();
	}

	public TransitivityJobBean(BatchParameters params, BatchJob parent) {
		this(params, parent, parent.getExternalId(), parent.getTransactionId(),
				parent.getUrmId());
	}

	public TransitivityJobBean(BatchParameters params, BatchJob parent,
			String externalId) {
		this(params, parent, externalId, randomTransactionId(), parent
				.getUrmId());
	}

	public TransitivityJobBean(TransitivityJobBean job, String externalId) {
		this(job.getBatchParametersId(), job.getBatchParentId(), 
				externalId, job.getTransactionId(), job
						.getUrmId());
		this.setDescription(job.getDescription());
		this.setFractionComplete(job.getFractionComplete());
		this.setStatus(job.getStatus());
		Date ts = job.getTimeStamp(job.getStatus());
		this.setTimeStamp(job.getStatus(), ts);
	}

	/**
	 * Used by public, non-copy constructors
	 * @param params must be persistent
	 * @param parent must be persistent
	 * @param externalId may be null
	 * @param tid may be invalid/non-persistent
	 * @param urmid may be invalid/non-persistent
	 */
	protected TransitivityJobBean(BatchParameters params, BatchJob parent,
			String externalId, long tid, long urmid) {
		this(params.getId(), parent.getId(), 
				externalId, tid, urmid);
		if (!BatchParametersBean.isPersistent(params)) {
			throw new IllegalArgumentException(
					"non-persistent batch parameters");
		}
		if (!BatchJobBean.isPersistent(parent)) {
			throw new IllegalArgumentException(
					"non-persistent parent batch job");
		}
	}

	/**
	 * Used by copy constructor
	 * @param paramsId may be invalid/non-persistent
	 * @param parentId may be invalid/non-persistent
	 * @param externalId may be invalid/non-persistent
	 * @param tid may be invalid/non-persistent
	 * @param urmid may be invalid/non-persistent
	 */
	protected TransitivityJobBean(long paramsId, long parentId,
			String externalId, long tid, long urmid) {
		super(TransitivityJobJPA.DISCRIMINATOR_VALUE, paramsId,
				externalId, tid, parentId, urmid);
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

} // TransitivityJobBean

