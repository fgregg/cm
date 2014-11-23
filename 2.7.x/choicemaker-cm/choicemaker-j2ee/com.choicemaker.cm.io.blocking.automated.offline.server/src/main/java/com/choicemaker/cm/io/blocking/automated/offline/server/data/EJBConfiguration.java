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
package com.choicemaker.cm.io.blocking.automated.offline.server.data;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.ejb.CreateException;
//import javax.jms.Topic;
//import javax.jms.TopicConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.sql.DataSource;

import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersEntity;

/**
 * This object contains method to get JMS and EJB objects from the J2EE server.
 *
 * @author pcheung
 * @author rphall (migration to EJB3)
 *
 */
public class EJBConfiguration implements Serializable {

//	private static final Logger log = Logger.getLogger(OabaProcessingControllerBean.class.getName());

	private static final long serialVersionUID = 271;

	// -- Enterprise Naming Context

	// ENC prefix
	public final static String ENV_BASE = "java:comp/env/";

	// ENC Entity Bean names
	public final static String EJB_BATCH_JOB = OabaJob.DEFAULT_JNDI_COMP_NAME;
	public final static String EJB_BATCH_PARAMS = OabaParameters.DEFAULT_JNDI_COMP_NAME;
	public final static String EJB_STATUS_LOG = OabaJobProcessing.DEFAULT_JNDI_COMP_NAME;

	// ENC Connection Factory names
	public final static String JMS_QUEUE_FACTORY = "jms/QueueConnectionFactory";
//	public final static String JMS_TOPIC_CONNECTION_FACTORY = "jms/TopicConnectionFactory";

	// ENC Topic names
//	public final static String JMS_STATUS_TOPIC = "jms/statusTopic";
//	public final static String JMS_TRANS_STATUS_TOPIC = "jms/transStatusTopic";

	// ENC Queue names
	public final static String JMS_START_QUEUE = "jms/startQueue";
	public final static String JMS_BLOCKING_QUEUE = "jms/blockQueue";
	public final static String JMS_DEDUP_QUEUE = "jms/dedupQueue";
	public final static String JMS_CHUNK_QUEUE = "jms/chunkQueue";
	public final static String JMS_MATCH_QUEUE = "jms/matchQueue";
	public final static String JMS_MATCH_DEDUP_QUEUE = "jms/matchDedupQueue";
	public final static String JMS_UPDATE_QUEUE = "jms/updateQueue";
	public final static String JMS_UPDATE_TRANSITIVITY_QUEUE = "jms/updateTransQueue";
	public final static String JMS_SINGLE_MATCH_QUEUE = "jms/singleMatchQueue";
	public final static String JMS_TRANSITIVITY_QUEUE = "jms/transitivityQueue";
	public final static String JMS_TRANS_MATCH_SCHEDULER_QUEUE = "jms/transMatchSchedulerQueue";
	public final static String JMS_TRANS_MATCHER_QUEUE = "jms/transMatcherQueue";
	public final static String JMS_TRANS_MATCH_DEDUP_QUEUE = "jms/transMatchDedupQueue";
	public final static String JMS_TRANS_MATCH_DEDUP_EACH_QUEUE = "jms/transMatchDedupEachQueue";

	//new parallelization code
	public final static String JMS_MATCH_SCHEDULER_QUEUE = "jms/matchSchedulerQueue";
	public final static String JMS_MATCHER_QUEUE = "jms/matcherQueue";
	public final static String JMS_MATCH_WRITER_QUEUE = "jms/matchWriterQueue";

	//match dedup parallelization
	public final static String JMS_MATCH_DEDUP_EACH_QUEUE = "jms/matchDedupEachQueue";

	// ENC DataSource names
	public final static String DATA_SOURCE = "jdbc/OABADS";

	// -- END Enterprise Naming Context
	
	// Cached data sources
	private transient DataSource ds;

	// Singleton instance
	private static EJBConfiguration config = null;

	private EJBConfiguration () {
	}

	public static EJBConfiguration getInstance () {
		if (config == null) config = new EJBConfiguration ();
		return config;
	}
	
	public Connection getConnection () throws RemoteException, CreateException, NamingException, SQLException {
		getDataSource ();
		Connection retVal = ds.getConnection();
		return retVal;
	}

	public OabaParameters findBatchParamsByJobId(EntityManager em, long jobId) {
		if (em == null) {
			throw new IllegalArgumentException("null entity manager");
		}
		OabaJob job = em.find(OabaJobEntity.class, jobId);
		long paramsId = job.getParametersId();
		OabaParameters retVal = em.find(OabaParametersEntity.class, paramsId);
		return retVal;
	}

	public DataSource getDataSource () throws RemoteException, CreateException, NamingException, SQLException {
		if (this.ds == null) {
			Context ctx = new InitialContext();
			this.ds = (DataSource) ctx.lookup (ENV_BASE + DATA_SOURCE);
		}
		return this.ds;
	}

}

