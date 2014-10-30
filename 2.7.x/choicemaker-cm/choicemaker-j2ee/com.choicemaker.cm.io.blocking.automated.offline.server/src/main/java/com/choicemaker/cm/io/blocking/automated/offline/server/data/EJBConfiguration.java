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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Topic;
import javax.jms.TopicConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.rmi.PortableRemoteObject;
import javax.sql.DataSource;

import com.choicemaker.cm.io.blocking.automated.offline.core.IStatus;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.StatusLog;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.StatusLogHome;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.StatusLogWrapper;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchJobBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchJobJPA;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchParametersBean;

/**
 * This object contains method to get JMS and EJB objects from the J2EE server.
 *
 * @author pcheung
 *
 */
public class EJBConfiguration implements Serializable {

	private static final Logger log = Logger.getLogger(EJBConfiguration.class.getName());

	/* As of 2010-03-10 */
	static final long serialVersionUID = -3162430788712859929L;

	// -- Enterprise Naming Context

	// ENC prefix
	public final static String ENV_BASE = "java:comp/env/";

	// ENC Entity Bean names
	public final static String EJB_BATCH_JOB = BatchJob.DEFAULT_JNDI_COMP_NAME;
	public final static String EJB_BATCH_PARAMS = BatchParameters.DEFAULT_JNDI_COMP_NAME;
	public final static String EJB_STATUS_LOG = StatusLogHome.DEFAULT_JNDI_COMP_NAME;

	// ENC Connection Factory names
	public final static String JMS_QUEUE_FACTORY = "jms/QueueConnectionFactory";
	public final static String JMS_TOPIC_CONNECTION_FACTORY = "jms/TopicConnectionFactory";

	// ENC Topic names
	public final static String JMS_STATUS_TOPIC = "jms/statusTopic";
	public final static String JMS_TRANS_STATUS_TOPIC = "jms/transStatusTopic";

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
	
//	// Injected references
//	@PersistenceContext(unitName = "oaba")
//	private EntityManager em;

	// Cached EJB home proxies
//	private transient BatchParametersHome batchParamsHome;
	private transient StatusLogHome statusLogHome;

	// Cached connection factories
	private transient QueueConnectionFactory queueConnectionFactory;
	private transient TopicConnectionFactory topicConnectionFactory;
	
	// Cached queues
	private transient Queue startMessageQueue;
	private transient Queue blockMessageQueue;
	private transient Queue dedupMessageQueue;
//	private transient Queue reverseMessageQueue;
	private transient Queue chunkMessageQueue;
	private transient Queue matchMessageQueue;
	private transient Queue matchDedupMessageQueue;
	private transient Queue matchDedupEachMessageQueue;
	private transient Queue matchSchedulerMessageQueue;
	private transient Queue matcherMessageQueue;
//	private transient Queue matchWriterMessageQueue;
	private transient Queue updateMessageQueue;
	private transient Queue updateTransMessageQueue;
	private transient Queue singleMatchMessageQueue;
	
	private transient Queue transitivityMessageQueue;
	private transient Queue transMatchSchedulerMessageQueue;
	private transient Queue transMatcherMessageQueue;
	private transient Queue transMatchDedupMessageQueue;
	private transient Queue transMatchDedupEachMessageQueue;

	// Cached topics
	private transient Topic statusTopic;
	private transient Topic transStatusTopic;
	
	// Cached data sources
	private transient DataSource ds;

	// Cached JNDI context
	private transient Context ctx;

	// Singleton instance
	private static EJBConfiguration config = null;

	private EJBConfiguration () {
	}

	public static EJBConfiguration getInstance () {
		if (config == null) config = new EJBConfiguration ();
		return config;
	}
	
	/** Returns the initial naming context */
	public Context getInitialContext() throws NamingException {
		if (ctx == null) {
			ctx = new InitialContext();
		}
		return ctx;
	}

	public TopicConnectionFactory getTopicConnectionFactory()throws NamingException {
		if (topicConnectionFactory == null) {
			Context ctx = getInitialContext();
			topicConnectionFactory =(TopicConnectionFactory) ctx.lookup(ENV_BASE + JMS_TOPIC_CONNECTION_FACTORY);
		}
		return topicConnectionFactory;
	} // getTopicConnectionFactory()

	public Topic getStatusTopic() throws NamingException {
		if (statusTopic == null) {
			Context ctx = getInitialContext();
			statusTopic = (Topic) ctx.lookup(ENV_BASE + JMS_STATUS_TOPIC);
		}
		return statusTopic;
	} //getStatusTopic()

	public Topic getTransStatusTopic() throws NamingException {
		if (transStatusTopic == null) {
			Context ctx = getInitialContext();
			transStatusTopic = (Topic) ctx.lookup(ENV_BASE + JMS_TRANS_STATUS_TOPIC);
		}
		return transStatusTopic;
	} //gettransStatusTopic()

	/** This looks up a queue on the EJB server.
	 * Don't not specify the prefix java:comp/env/
	 *
	 * @param jndiQueueName - like jms/sQueue
	 */
	private Queue getMessageQueue(String jndiQueueName) throws NamingException {
		if (jndiQueueName == null || jndiQueueName.length() == 0) {
			throw new IllegalArgumentException("null or blank jndi name");
		}
		Context ctx = getInitialContext();
		Queue retVal = (Queue) ctx.lookup(ENV_BASE + jndiQueueName);
		return retVal;
	}

	/** This looks up a queue factory on the EJB server.
	 * Don't not specify the prefix java:comp/env/
	 */
	private QueueConnectionFactory getMessageQueueFactory (String factoryName) throws NamingException {
		if (factoryName == null || factoryName.length() == 0) {
			throw new IllegalArgumentException("null or blank jndi name");
		}
		Context ctx = getInitialContext();
		QueueConnectionFactory retVal = (QueueConnectionFactory) ctx.lookup(ENV_BASE + factoryName);
		return retVal;
	}

	/** Returns the start message queue used by the batch processor and backend */
	public Queue getStartMessageQueue() throws NamingException {
		if (startMessageQueue == null) {
			startMessageQueue = getMessageQueue( JMS_START_QUEUE);
		}
		return startMessageQueue;
	}

	/**
	 * Returns the blocking message queue
	 */
	public Queue getBlockingMessageQueue() throws NamingException {
		if (blockMessageQueue == null) {
			blockMessageQueue = getMessageQueue(JMS_BLOCKING_QUEUE);
		}
		return blockMessageQueue;
	}

	/**
	 * Returns the dedup message queue
	 */
	public Queue getDedupMessageQueue() throws NamingException {
		if (dedupMessageQueue == null) {
			dedupMessageQueue = getMessageQueue(JMS_DEDUP_QUEUE);
		}
		return dedupMessageQueue;
	}

	/**
	 * Returns the reverse id translation message queue
	 */
/*
	public Queue getReverseMessageQueue() throws NamingException {
		if (reverseMessageQueue == null) {
			reverseMessageQueue = getMessageQueue(JMS_REVERSE_QUEUE);
		}
		return reverseMessageQueue;
	}
*/

	/**
	 * Returns the create chunk message queue
	 */
	public Queue getChunkMessageQueue() throws NamingException {
		if (chunkMessageQueue == null) {
			chunkMessageQueue = getMessageQueue(JMS_CHUNK_QUEUE);
		}
		return chunkMessageQueue;
	}

	/**
	 * Returns the matching message queue
	 */
	public Queue getMatchingMessageQueue() throws NamingException {
		if (matchMessageQueue == null) {
			matchMessageQueue = getMessageQueue(JMS_MATCH_QUEUE);
		}
		return matchMessageQueue;
	}


	/**
	 * Returns the matching dedup message queue
	 */
	public Queue getMatchDedupMessageQueue() throws NamingException {
		if (matchDedupMessageQueue == null) {
			matchDedupMessageQueue = getMessageQueue(JMS_MATCH_DEDUP_QUEUE);
		}
		return matchDedupMessageQueue;
	}

	/**
	 * Returns the matching dedup each message queue
	 */
	public Queue getMatchDedupEachMessageQueue() throws NamingException {
		if (matchDedupEachMessageQueue == null) {
			matchDedupEachMessageQueue = getMessageQueue(JMS_MATCH_DEDUP_EACH_QUEUE);
		}
		return matchDedupEachMessageQueue;
	}

	/**
	 * Returns the match scheduler message queue
	 */
	public Queue getMatchSchedulerMessageQueue() throws NamingException {
		if (matchSchedulerMessageQueue == null) {
			matchSchedulerMessageQueue = getMessageQueue(JMS_MATCH_SCHEDULER_QUEUE);
		}
		return matchSchedulerMessageQueue;
	}

	/**
	 * Returns the matcher message queue
	 */
	public Queue getMatcherMessageQueue() throws NamingException {
		if (matcherMessageQueue == null) {
			matcherMessageQueue = getMessageQueue(JMS_MATCHER_QUEUE);
		}
		return matcherMessageQueue;
	}

	/**
	 * Returns the match writer message queue
	 */
/*
	public Queue getMatchWriterMessageQueue() throws NamingException {
		if (matchWriterMessageQueue == null) {
			matchWriterMessageQueue = getMessageQueue(JMS_MATCH_WRITER_QUEUE);
		}
		return matchWriterMessageQueue;
	}
*/

	/**
	 * Returns the transitivity matching scheduler message queue
	 */
	public Queue getTransMatchSchedulerMessageQueue() throws NamingException {
		if (transMatchSchedulerMessageQueue == null) {
			transMatchSchedulerMessageQueue = getMessageQueue(JMS_TRANS_MATCH_SCHEDULER_QUEUE);
		}
		return transMatchSchedulerMessageQueue;
	}

	/**
	 * Returns the transitivity matcher message queue
	 */
	public Queue getTransMatcherMessageQueue() throws NamingException {
		if (transMatcherMessageQueue == null) {
			transMatcherMessageQueue = getMessageQueue(JMS_TRANS_MATCHER_QUEUE);
		}
		return transMatcherMessageQueue;
	}

	/**
	 * Returns the transitivity matching dedup message queue
	 */
	public Queue getTransMatchDedupMessageQueue() throws NamingException {
		if (transMatchDedupMessageQueue == null) {
			transMatchDedupMessageQueue = getMessageQueue(JMS_TRANS_MATCH_DEDUP_QUEUE);
		}
		return transMatchDedupMessageQueue;
	}

	/**
	 * Returns the matching dedup each message queue
	 */
	public Queue getTransMatchDedupEachMessageQueue() throws NamingException {
		if (transMatchDedupEachMessageQueue == null) {
			transMatchDedupEachMessageQueue = getMessageQueue(JMS_TRANS_MATCH_DEDUP_EACH_QUEUE);
		}
		return transMatchDedupEachMessageQueue;
	}

	/**
	 * Returns the start transitivity message queue
	 */
	public Queue getTransitivityMessageQueue() throws NamingException {
		if (transitivityMessageQueue == null) {
			transitivityMessageQueue = getMessageQueue(JMS_TRANSITIVITY_QUEUE);
		}
		return transitivityMessageQueue;
	}

	/**
	 * Returns the update OABA status message queue
	 */
	public Queue getUpdateMessageQueue() throws NamingException {
		if (updateMessageQueue == null) {
			updateMessageQueue = getMessageQueue(JMS_UPDATE_QUEUE);
		}
		return updateMessageQueue;
	}

	/**
	 * Returns the update Transitivity status message queue
	 */
	public Queue getUpdateTransMessageQueue() throws NamingException {
		if (updateTransMessageQueue == null) {
			updateTransMessageQueue = getMessageQueue(JMS_UPDATE_TRANSITIVITY_QUEUE);
		}
		return updateTransMessageQueue;
	}

	/**
	 * Returns the single record matching message queue
	 */
	public Queue getSingleMatchMessageQueue() throws NamingException {
		if (singleMatchMessageQueue == null) {
			singleMatchMessageQueue = getMessageQueue(JMS_SINGLE_MATCH_QUEUE);
		}
		return singleMatchMessageQueue;
	}

	/**
	 * Returns a factory for Queue connections. Note that if a client
	 * uses the factory to create connections, it is the client's
	 * responsibility to close the connections after the client is
	 * finished with the connection.
	 */
	private QueueConnectionFactory getQueueConnectionFactory() throws NamingException {
		if (queueConnectionFactory == null) {
			queueConnectionFactory = getMessageQueueFactory (JMS_QUEUE_FACTORY);
		}
		return queueConnectionFactory;
	} // getQueueConnectionFactory

	private QueueSession getQueueSession () throws NamingException, JMSException {
		QueueConnectionFactory factory = getQueueConnectionFactory();
		QueueConnection connection = factory.createQueueConnection();
		connection.start ();
		QueueSession session = connection.createQueueSession (false, QueueSession.AUTO_ACKNOWLEDGE);
//		QueueSession session = this.connection.createQueueSession(true, 0);
		return session;
	}

	public void sendMessage (Queue queue, Serializable data) {
		QueueSession session = null;

		try {
			session = getQueueSession ();
			ObjectMessage message = session.createObjectMessage(data);
			QueueSender sender = session.createSender(queue);

			sender.send(message);

			log.fine ("Sending on queue '" + queue.getQueueName()) ;
			log.fine("session " + session);
			log.fine("message " + message);
			log.fine("sender " + sender);
		} catch (Exception ex) {
			log.severe(ex.toString());
		} finally {
			try {
				if (session != null) session.close();
			} catch (JMSException ex) {
				log.severe(ex.toString());
			}
		}
	}

	/** A convenience method that creates a new BatchJob record */
	public BatchJob createBatchJob(EntityManager em, String externalId) {
		if (em == null) {
			throw new IllegalArgumentException("null entity manager");
		}
		BatchJob retVal = new BatchJobBean(externalId);
		em.persist(retVal);
		return retVal;
	}

	public StatusLog createStatusLog(long id) throws RemoteException, CreateException, NamingException{
		Context ctx = getInitialContext();
		Object homeRef = ctx.lookup(EJB_STATUS_LOG);
		StatusLogHome statusLogHome = (StatusLogHome) PortableRemoteObject.narrow(homeRef,StatusLogHome.class);
		StatusLog retVal = statusLogHome.create (id);
		return retVal;
	}

//	public BatchParameters createBatchParameters (long id)
//		throws RemoteException, CreateException, NamingException, SQLException {
//		BatchParametersHome batchParamHome = getBatchParamsHome();
//		BatchParameters retVal = batchParamHome.create (id);
//    return retVal;
//	}

	public Connection getConnection () throws RemoteException, CreateException, NamingException, SQLException {
		getDataSource ();
		Connection retVal = ds.getConnection();
		return retVal;
	}

	/** This returns a BatchJob (or a sub-class) for the given job id */
	public BatchJob findBatchJobById(EntityManager em, Class<? extends BatchJob> c, long id) {
		if (em == null) {
			throw new IllegalArgumentException("null entity manager");
		}
		if (c == null) {
			throw new IllegalArgumentException("null class");
		}
		BatchJob retVal = em.find(c, id);
		return retVal;
	}
	
	public List<? extends BatchJob> findAllBatchJobs(EntityManager em) {
		if (em == null) {
			throw new IllegalArgumentException("null entity manager");
		}
		Query query = em.createNamedQuery(BatchJobJPA.QN_BATCHJOB_FIND_ALL);
		@SuppressWarnings("unchecked")
		List<BatchJobBean> entries = query.getResultList();
		if (entries == null) {
			entries = new ArrayList<BatchJobBean>();
		}
		return entries;
	}
	
	public void deleteBatchJob(EntityManager em, BatchJob job) {
		if (job == null) {
			log.finest("Ignoring null batch job");
		} else if (!BatchJobBean.isPersistent(job)) {
			log.fine("Ignoring non-persistent batch job: " + job.getId());
		} else {
			Class<? extends BatchJob> c = job.getClass();
			BatchJob dbEntry = em.find(c, job.getId());
			if (dbEntry == null) {
				// The batch job isn't in the database
				log.warning("Unable to find batch job: " + job.getId());
			}  else {
				job = em.merge(job);
				em.remove(job);
			}
		}
	}
	
	public BatchParameters findBatchParamsById(EntityManager em, long id) {
		if (em == null) {
			throw new IllegalArgumentException("null entity manager");
		}
		BatchParameters retVal = em.find(BatchParametersBean.class, id);
		return retVal;
	}

	public StatusLog findStatusLogById(long id) throws RemoteException, FinderException, NamingException {
		StatusLogHome home = getStatusLogHome();
		StatusLog retVal = home.findByPrimaryKey(new Long(id));
		return retVal;
	}

	public DataSource getDataSource () throws RemoteException, CreateException, NamingException, SQLException {
		if (this.ds == null) {
			Context ctx = getInitialContext();
			this.ds = (DataSource) ctx.lookup (ENV_BASE + DATA_SOURCE);
		}
		return this.ds;
	}

//	private BatchParametersHome getBatchParamsHome() throws NamingException {
//		if (this.batchParamsHome == null) {
//			Context ctx = getInitialContext();
//			Object homeRef = ctx.lookup(EJB_BATCH_PARAMS);
//			this.batchParamsHome =
//				(BatchParametersHome) PortableRemoteObject.narrow(
//					homeRef, BatchParametersHome.class);
//		}
//		return this.batchParamsHome;
//	}

	private StatusLogHome getStatusLogHome() throws NamingException {
		if (this.statusLogHome == null) {
			Context ctx = getInitialContext();
			Object homeRef = ctx.lookup(EJB_STATUS_LOG);
			this.statusLogHome = (StatusLogHome) PortableRemoteObject.narrow(
				homeRef, StatusLogHome.class);
		}
		return this.statusLogHome;
	}

	public IStatus getStatusLog (StartData data) throws RemoteException, FinderException, NamingException {
		StatusLog statusLog = findStatusLogById (data.jobID);
		StatusLogWrapper retVal = new StatusLogWrapper (statusLog);
		return retVal;
	}

	public IStatus getStatusLog (long jobID) throws RemoteException, FinderException, NamingException {
		StatusLog statusLog = findStatusLogById (jobID);
		StatusLogWrapper retVal = new StatusLogWrapper (statusLog);
		return retVal;
	}

	public void createNewStatusLog (long id) throws RemoteException, CreateException, NamingException {
		StatusLog statusLog = createStatusLog(id);
		statusLog.setStatusId(new Integer (0));
	}

} // EJBConfiguration

