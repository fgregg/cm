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

import static com.choicemaker.cm.batch.impl.BatchJobJPA.DISCRIMINATOR_COLUMN;
import static com.choicemaker.cm.batch.impl.BatchJobJPA.TABLE_NAME;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobJPA.DISCRIMINATOR_VALUE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobJPA.JPQL_OABAJOB_FIND_ALL;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobJPA.QN_OABAJOB_FIND_ALL;

import java.io.File;
import java.io.Serializable;

//import javax.jms.ObjectMessage;
//import javax.jms.Topic;
//import javax.jms.TopicConnection;
//import javax.jms.TopicPublisher;
//import javax.jms.TopicSession;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.batch.impl.BatchJobEntity;
import com.choicemaker.cm.core.IControl;
//import com.choicemaker.cm.io.blocking.automated.offline.server.data.BatchJobStatus;
//import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;

/**
 * This class tracks the progress of a (long-running) offline matching process.
 * It also serves as the base class of other types of long-running jobs.
 * <p>
 * A successful batch job goes through a sequence of states: NEW, QUEUED,
 * STARTED, and COMPLETED. If processing fails in one of these stages, the job
 * state is marked as FAILED. A request may be aborted at any point, in which
 * case it goes through the ABORT_REQUESTED and the ABORT states.
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
@Table(/* schema = "CHOICEMAKER", */name = TABLE_NAME)
@DiscriminatorColumn(name = DISCRIMINATOR_COLUMN,
		discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue(DISCRIMINATOR_VALUE)
public class OabaJobEntity extends BatchJobEntity implements IControl,
		Serializable, OabaJob {

	private static final long serialVersionUID = 271L;

	// -- Instance data

	// -- Construction

	/** Required by JPA; do not invoke directly */
	protected OabaJobEntity() {
		super();
	}

	/**
	 * Creates an isolated OabaJob entity without any of the required ancillary
	 * objects. The preferred  method for creating an OabaJob entity is via
	 * the {@link OabaJobControllerBean}
	 * @param params non-null
	 * @param settings non-null
	 * @param externalId optional; may be null
	 */
	public OabaJobEntity(OabaParameters params, OabaSettings settings,
			ServerConfiguration serverConfig, String externalId) {
		this(OabaJobJPA.DISCRIMINATOR_VALUE, params.getId(), settings.getId(), serverConfig.getId(),
				externalId, randomTransactionId(), INVALID_ID,
				INVALID_ID);
	}

	public OabaJobEntity(OabaJob o) {
		this(OabaJobJPA.DISCRIMINATOR_VALUE, o.getParametersId(), o
				.getSettingsId(), o.getServerId(), o.getExternalId(), o.getTransactionId(), o
				.getBatchParentId(), o.getUrmId());
		File owd = o.getWorkingDirectory();
		this.workingDirectory = owd == null ? null : owd.getAbsolutePath();
	}

	protected OabaJobEntity(String type, long paramsid, long settingsId, long serverId,
			String externalId, long tid, long bpid, long urmid) {
		super(type, paramsid, settingsId, serverId, externalId, tid, bpid, urmid);
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

} // OabaJobEntity

