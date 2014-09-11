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
package com.choicemaker.cm.persist0;

import java.util.Date;
import java.util.logging.Logger;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;

/**
 * A TransitivityJobBean tracks the progress of a (long-running) transitivity
 * process. A successful request goes through a sequence of states: NEW, QUEUED,
 * STARTED, and COMPLETED. A request may be aborted at any point, in which case
 * it goes through the ABORT_REQUESTED and the ABORT states.</p>
 *
 * A long-running process should provide some indication that it is making
 * progress. It can provide this estimate as a fraction between 0 and 100
 * (inclusive) by updating the getFractionComplete() field.</p>
 *
 * @author pcheung (original version)
 * @author rphall (migrated to JPA 2.0)
 *
 */
@NamedQuery(name = "transitivityFindAll",
		query = "Select job from TransitivityJobBean job")
@Entity
@DiscriminatorValue(value = "TRANSITIVITY")
public class TransitivityJobBean extends BatchJobBean implements
		TransitivityJob {

	private static final long serialVersionUID = 271L;

	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(TransitivityJobBean.class
			.getName());

	public static final String TABLE_DISCRIMINATOR = "TRANSITIVITY";

	public static enum NamedQuery {
		FIND_ALL("transitivityFindAll");
		public final String name;

		NamedQuery(String name) {
			this.name = name;
		}
	}

	// -- Instance data

	// -- Construction

	protected TransitivityJobBean() {
		this(null);
	}

	public TransitivityJobBean(String externalId) {
		this(externalId, randomTransactionId());
	}

	public TransitivityJobBean(String externalId, String transactionId) {
		super(externalId, transactionId, TABLE_DISCRIMINATOR);
	}

	// -- Accessors

	// -- Modifiers

	// -- Call backs

	@Override
	protected CMP_AuditEvent createStatusEvent(BatchJobStatus s, Date d) {
		TransitivityJobAuditEvent e =
			new TransitivityJobAuditEvent(this.getTransactionId(), this, d,
					batchJobStatus);
		return e;
	}

	// -- Identity

	@Override
	public String toString() {
		return "TransitivityJobBean [" + id + "/" + externalId + "/"
				+ batchJobStatus + "]";
	}

}
