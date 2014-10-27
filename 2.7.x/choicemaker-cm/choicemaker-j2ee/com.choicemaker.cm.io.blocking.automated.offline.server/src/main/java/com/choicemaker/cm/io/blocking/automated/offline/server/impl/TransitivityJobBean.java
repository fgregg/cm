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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
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
@NamedQuery(name = TransitivityJobJPA.QN_TRANSITIVITY_FIND_ALL_BY_PARENT_ID,
		query = TransitivityJobJPA.JPQL_TRANSITIVITY_FIND_ALL_BY_PARENT_ID)
})
@Entity
@DiscriminatorValue(value = TransitivityJobJPA.DISCRIMINATOR_VALUE)
public class TransitivityJobBean extends BatchJobBean implements
		TransitivityJob {

	private static final long serialVersionUID = 271L;

	/** Required by JPA; do not invoke directly */
	protected TransitivityJobBean() {
		super();
	}

	public TransitivityJobBean(BatchJob parent) {
		this(parent, parent.getExternalId(), parent.getTransactionId(), parent
				.getUrmId());
	}

	public TransitivityJobBean(BatchJob parent, String externalId) {
		this(parent, externalId, randomTransactionId(), parent.getUrmId());
	}

	protected TransitivityJobBean(BatchJob parent, String externalId, long tid,
			long urmid) {
		super(externalId, tid, parent.getId(), urmid);
		if (!BatchJobBean.isPersistent(parent)) {
			throw new IllegalArgumentException(
					"non-persistent parent batch job");
		}
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

