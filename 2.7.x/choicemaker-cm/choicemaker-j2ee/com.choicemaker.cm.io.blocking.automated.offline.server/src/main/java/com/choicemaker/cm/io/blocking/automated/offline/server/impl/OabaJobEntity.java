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

import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobJPA.DISCRIMINATOR_VALUE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobJPA.JPQL_OABAJOB_FIND_ALL;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobJPA.QN_OABAJOB_FIND_ALL;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.BatchJobRigor;
import com.choicemaker.cm.batch.impl.BatchJobEntity;
import com.choicemaker.cm.core.IControl;

/**
 * This class tracks the progress of a (long-running) offline matching process.
 * It also serves as the base class of other types of long-running jobs.
 * <p>
 * A successful batch job goes through a sequence of states: NEW, QUEUED,
 * PROCESSING, and COMPLETED. If processing fails in one of these stages, the
 * job state is marked as FAILED. A request may be aborted at any point, in
 * which case it goes through the ABORT_REQUESTED and the ABORT states.
 * </p>
 * <p>
 * A long-running process should provide some indication that it is making
 * progress. This class provides this estimate as a fraction between 0 and 100
 * (inclusive) by updating the {@link #setFractionComplete(int) fraction
 * complete} field.
 * </p>
 *
 * @author pcheung (original version)
 * @author rphall (migrated to JPA 2.0)
 *
 */
@NamedQuery(name = QN_OABAJOB_FIND_ALL, query = JPQL_OABAJOB_FIND_ALL)
@Entity
@DiscriminatorValue(DISCRIMINATOR_VALUE)
public class OabaJobEntity extends BatchJobEntity implements IControl {

	private static final long serialVersionUID = 271L;

	/** Required by JPA; do not invoke directly */
	protected OabaJobEntity() {
		super();
	}

	public static String dump(BatchJob job) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println("BatchJob id: " + job.getId());
		pw.println("BatchJob extId: " + job.getExternalId());
		// FIXME pw.println("BatchJob description: " + job.getDescription());
		pw.println("BatchJob rigor: " + job.getBatchJobRigor());
		String retVal = sw.toString();
		return retVal;
	}

	// -- Constructors

	/**
	 * Creates an isolated BatchJob entity with {@link BatchJob#DEFAULT_RIGOR
	 * default rigor} but without any required, ancillary objects. The preferred
	 * method for creating an BatchJob entity is via the
	 * {@link OabaJobControllerBean}
	 *
	 * @param params
	 *            non-null
	 * @param settings
	 *            non-null
	 * @param externalId
	 *            optional; may be null
	 */
	public OabaJobEntity(OabaParameters params, OabaSettings settings,
			ServerConfiguration serverConfig, String externalId) {
		this(OabaJobJPA.DISCRIMINATOR_VALUE, params.getId(), settings.getId(),
				serverConfig.getId(), externalId, randomTransactionId(),
				NONPERSISTENT_ID, NONPERSISTENT_ID, DEFAULT_RIGOR);
	}

	/**
	 * Creates an isolated BatchJob entity without any required, ancillary
	 * objects. The preferred method for creating an BatchJob entity is via the
	 * {@link OabaJobControllerBean}
	 * 
	 * @param params
	 *            non-null
	 * @param settings
	 *            non-null
	 * @param externalId
	 *            optional; may be null
	 * @param bjr
	 *            required; must not be null
	 */
	public OabaJobEntity(OabaParameters params, OabaSettings settings,
			ServerConfiguration serverConfig, String externalId,
			BatchJobRigor bjr) {
		this(OabaJobJPA.DISCRIMINATOR_VALUE, params.getId(), settings.getId(),
				serverConfig.getId(), externalId, randomTransactionId(),
				NONPERSISTENT_ID, NONPERSISTENT_ID, bjr);
	}

	public OabaJobEntity(BatchJob o) {
		this(OabaJobJPA.DISCRIMINATOR_VALUE, o.getParametersId(), o
				.getSettingsId(), o.getServerId(), o.getExternalId(), o
				.getTransactionId(), o.getBatchParentId(), o.getUrmId(), o
				.getBatchJobRigor());
		File owd = o.getWorkingDirectory();
		this.workingDirectory = owd == null ? null : owd.getAbsolutePath();
	}

	protected OabaJobEntity(String type, long paramsid, long settingsId,
			long serverId, String externalId, long tid, long bpid, long urmid,
			BatchJobRigor bjr) {
		super(type, paramsid, settingsId, serverId, externalId, tid, bpid,
				urmid, bjr);
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

}
