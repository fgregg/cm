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
package com.choicemaker.cm.urm.ejb;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
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
import javax.jms.TopicConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.rmi.PortableRemoteObject;
import javax.sql.DataSource;

import com.choicemaker.cm.core.ChoiceMakerExtensionPoint;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchParametersHome;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchQueryService;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchQueryServiceHome;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.TransitivityJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.TransitivityJobHome;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchJobBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.TransitivityJobBean;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityOABAService;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityOABAServiceHome;
import com.choicemaker.cm.urm.exceptions.CmRuntimeException;
import com.choicemaker.cm.urm.exceptions.ConfigException;

/**
 * @author emoussikaev
 * @version Revision: 2.5  Date: Aug 8, 2005 12:23:46 PM
 * @see
 */
public class Single implements Serializable {

	private static final Logger log = Logger.getLogger(Single.class.getName());

	/* As of 2010-03-10 */
	static final long serialVersionUID = 3897669963105467617L;

	// -- Enterprise Naming Context

	// ENC Session Bean names
	private static final String BATCH_QUERY_SERVICE =
		"java:comp/env/ejb/BatchQueryService";
	private static final String TRANSITIVITY_OABA_SERVICE =
		"java:comp/env/ejb/TransitivityOABAService";
	private static final String TRANSITIVITY_SERIALIZER =
		"java:comp/env/ejb/TransSerializer";

	// ENC Entity Bean names
	private static final String URM_JOB = "java:comp/env/ejb/UrmJob";
	private static final String TRANSITIVITY_JOB =
		"java:comp/env/ejb/TransitivityJob";
	private static final String CMS_JOB =
		"java:comp/env/ejb/UrmSerializationJob";
	private static final String URM_STEP_JOB = "java:comp/env/ejb/UrmStepJob";
	private static final String EJB_BATCH_PARAMS =
		"java:comp/env/ejb/BatchParameters";

	// ENC Connection Factory names
	public final static String ENC_JNDI_TOPIC_CONNECTION_FACTORY =
		"java:comp/env/jms/TopicConnectionFactory";
	public final static String JMS_QUEUE_FACTORY =
		"java:comp/env/jms/QueueConnectionFactory";

	// ENC Topic names
	public final static String TRANS_SERIAL_STATUS_TOPIC =
		"java:comp/env/jms/transSerialStatusTopic";

	// -- END Enterprise Naming Context

	// ChoiceMaker extension names
	public static final String DATABASE_ACCESSOR =
		ChoiceMakerExtensionPoint.CM_IO_BLOCKING_AUTOMATED_BASE_DATABASEACCESSOR;
	public static final String MATCH_CANDIDATE =
		ChoiceMakerExtensionPoint.CM_CORE_MATCHCANDIDATE;
	/* UNUSED
	public static final String BEAN_MATCH_CANDIDATE =
		"com.choicemaker.cm.core.beanMatchCandidate";
	*/

	// ChoiceMaker parameters
	public static final String CURRENT_VERSION = "2.7.1";
	public static final int DEFAULT_MAX_DB_COLLECTION_CHUNK_SIZE = 100000;

	// Cached EJB remote proxies
	private transient BatchQueryService batchQueryService = null;
	private transient TransitivityOABAService transOABAService = null;
	private transient TransSerializer transSerializer = null;

	// Cached EJB home proxies
	private transient UrmJobHome urmJobHome = null;
	private transient TransitivityJobHome transJobHome = null;
	private transient CmsJobHome cmsJobHome = null;
	private transient UrmStepJobHome urmStepJobHome = null;
	private transient BatchParametersHome batchParamsHome = null;

	// Cached connection factories
	private transient TopicConnectionFactory topicConnectionFactory;
	private transient QueueConnectionFactory queueConnectionFactory = null;

	// Cached JNDI context
	private transient InitialContext initContext = null;

	// Singleton instance
	private static Single me = null;

	private Single() {
	}

	public static Single getInst() {
		if (me == null)
			me = new Single();
		return me;
	}

	public String getVersion() {
		return CURRENT_VERSION;
	}

	/** Returns the initial naming context */
	public Context getInitialContext() throws NamingException {
		if (this.initContext == null) {
			this.initContext = new InitialContext();
		}
		return this.initContext;
	}
	public BatchQueryService getBatchQueryService()
		throws ConfigException, CmRuntimeException {
		if (batchQueryService == null) {
			try {
				Context ctx = getInitialContext(); //naming ex
				Object homeRef = ctx.lookup(BATCH_QUERY_SERVICE);
				BatchQueryServiceHome batchQueryServiceHome =
					(BatchQueryServiceHome) PortableRemoteObject.narrow(
						homeRef,
						BatchQueryServiceHome.class);
				batchQueryService = batchQueryServiceHome.create();
			} catch (ClassCastException e) {
				log.severe(e.toString());
				throw new CmRuntimeException(e.toString());
			} catch (RemoteException e) {
				log.severe(e.toString());
				throw new CmRuntimeException(e.toString());
			} catch (NamingException e) {
				log.severe(e.toString());
				throw new ConfigException(e.toString());
			} catch (CreateException e) {
				log.severe(e.toString());
				throw new ConfigException(e.toString());
			}
		}
		return batchQueryService;
	}

	public TransitivityOABAService getTransitivityOABAService()
		throws ConfigException, CmRuntimeException {
		if (transOABAService == null) {
			try {
				Context ctx = getInitialContext(); //naming ex
				Object homeRef = ctx.lookup(TRANSITIVITY_OABA_SERVICE);
				TransitivityOABAServiceHome trServiceHome =
					(TransitivityOABAServiceHome) PortableRemoteObject.narrow(
						homeRef,
						TransitivityOABAServiceHome.class);
				transOABAService = trServiceHome.create();
			} catch (ClassCastException e) {
				log.severe(e.toString());
				throw new CmRuntimeException(e.toString());
			} catch (RemoteException e) {
				log.severe(e.toString());
				throw new CmRuntimeException(e.toString());
			} catch (NamingException e) {
				log.severe(e.toString());
				throw new ConfigException(e.toString());
			} catch (CreateException e) {
				log.severe(e.toString());
				throw new ConfigException(e.toString());
			}
		}
		return transOABAService;
	}

	public TransSerializer getTransSerializer()
		throws ConfigException, CmRuntimeException {
		if (transSerializer == null) {
			try {
				Context ctx = getInitialContext(); //naming ex
				Object homeRef = ctx.lookup(TRANSITIVITY_SERIALIZER);
				TransSerializerHome trHome =
					(TransSerializerHome) PortableRemoteObject.narrow(
						homeRef,
						TransSerializerHome.class);
				transSerializer = trHome.create();
			} catch (ClassCastException e) {
				log.severe(e.toString());
				throw new CmRuntimeException(e.toString());
			} catch (RemoteException e) {
				log.severe(e.toString());
				throw new CmRuntimeException(e.toString());
			} catch (NamingException e) {
				log.severe(e.toString());
				throw new ConfigException(e.toString());
			} catch (CreateException e) {
				log.severe(e.toString());
				throw new ConfigException(e.toString());
			}
		}
		return transSerializer;
	}

	public UrmJobHome getUrmJobHome()
		throws ConfigException, CmRuntimeException {
		if (urmJobHome == null) {
			try {
				Context ctx = getInitialContext();
				Object homeRef = ctx.lookup(URM_JOB);
				urmJobHome =
					(UrmJobHome) PortableRemoteObject.narrow(
						homeRef,
						UrmJobHome.class);
			} catch (ClassCastException e) {
				log.severe(e.toString());
				throw new CmRuntimeException(e.toString());
			} catch (NamingException e) {
				log.severe(e.toString());
				throw new ConfigException(e.toString());
			}
		}
		return urmJobHome;

	}

	public UrmJob createUrmJob(String externalId)
		throws ConfigException, CmRuntimeException {
		try {
			UrmJob status = getUrmJobHome().create(externalId);
			return status;
		} catch (RemoteException e) {
			log.severe(e.toString());
			throw new CmRuntimeException(e.toString());
		} catch (CreateException e) {
			log.severe(e.toString());
			throw new ConfigException(e.toString());
		}
	}

	public UrmJob findUrmJobById(long id)
		throws ConfigException, CmRuntimeException {
		try {
			UrmJobHome home = getUrmJobHome();
			return home.findByPrimaryKey(new Long(id));
		} catch (RemoteException e) {
			log.severe(e.toString());
			throw new CmRuntimeException(e.toString());
		} catch (FinderException e) {
			log.severe(e.toString());
			throw new ConfigException(e.toString());
		}
	}

	public Collection getUrmJobList()
		throws ConfigException, CmRuntimeException {
		try {
			UrmJobHome home = getUrmJobHome();
			return home.findAll();
		} catch (RemoteException e) {
			log.severe(e.toString());
			throw new CmRuntimeException(e.toString());
		} catch (FinderException e) {
			log.severe(e.toString());
			throw new ConfigException(e.toString());
		}
	}

	public CmsJobHome getCmsJobHome()
		throws ConfigException, CmRuntimeException {
		if (cmsJobHome == null) {
			try {
				Context ctx = getInitialContext();
				Object homeRef = ctx.lookup(CMS_JOB);
				cmsJobHome =
					(CmsJobHome) PortableRemoteObject.narrow(
						homeRef,
						CmsJobHome.class);
			} catch (ClassCastException e) {
				log.severe(e.toString());
				throw new CmRuntimeException(e.toString());
			} catch (NamingException e) {
				log.severe(e.toString());
				throw new ConfigException(e.toString());
			}
		}
		return cmsJobHome;
	}

	public CmsJob createCmsJob(String externalId, long transId)
		throws ConfigException, CmRuntimeException {
		try {
			CmsJob status = getCmsJobHome().create(externalId, transId);
			return status;
		} catch (RemoteException e) {
			log.severe(e.toString());
			throw new CmRuntimeException(e.toString());
		} catch (CreateException e) {
			log.severe(e.toString());
			throw new ConfigException(e.toString());
		}
	}

	public CmsJob findCmsJobById(long id)
		throws ConfigException, CmRuntimeException {
		try {
			CmsJobHome home = getCmsJobHome();
			return home.findByPrimaryKey(new Long(id));
		} catch (RemoteException e) {
			log.severe(e.toString());
			throw new CmRuntimeException(e.toString());
		} catch (FinderException e) {
			log.severe(e.toString());
			throw new ConfigException(e.toString());
		}
	}

	public UrmStepJobHome getUrmStepJobHome()
		throws ConfigException, CmRuntimeException {
		if (urmStepJobHome == null) {
			try {
				Context ctx = getInitialContext();
				Object homeRef = ctx.lookup(URM_STEP_JOB);
				urmStepJobHome =
					(UrmStepJobHome) PortableRemoteObject.narrow(
						homeRef,
						UrmStepJobHome.class);
			} catch (ClassCastException e) {
				log.severe(e.toString());
				throw new CmRuntimeException(e.toString());
			} catch (NamingException e) {
				log.severe(e.toString());
				throw new ConfigException(e.toString());
			}
		}
		return urmStepJobHome;

	}

	public UrmStepJob createUrmStepJob(Long urmJobId, Long stepIndex)
		throws ConfigException, CmRuntimeException {
		try {
			UrmStepJob status = getUrmStepJobHome().create(urmJobId, stepIndex);
			return status;
		} catch (RemoteException e) {
			log.severe(e.toString());
			throw new CmRuntimeException(e.toString());
		} catch (CreateException e) {
			log.severe(e.toString());
			throw new ConfigException(e.toString());
		}
	}

	public Collection findStepJobsByUrmId(long id)
		throws ConfigException, CmRuntimeException {
		try {
			if (urmStepJobHome == null) {
				getUrmStepJobHome();
			}
			return urmStepJobHome.findAllStepsOfUrmJob(new Long(id));
		} catch (ClassCastException e) {
			log.severe(e.toString());
			throw new CmRuntimeException(e.toString());
		} catch (RemoteException e) {
			log.severe(e.toString());
			throw new CmRuntimeException(e.toString());
		} catch (FinderException e) {
			log.severe(e.toString());
			throw new ConfigException(e.toString());
		}
	}

	public UrmStepJob findStepJobByUrmAndIndex(long urmJobId, long stepIndex)
		throws ConfigException, CmRuntimeException {
		Collection col = Single.getInst().findStepJobsByUrmId(urmJobId);
		Iterator it = col.iterator();
		UrmStepJob usj = null;
		long si = -1;
		try {
			while (it.hasNext() && si != stepIndex) {
				usj = (UrmStepJob) it.next();
				si = usj.getStepIndex().longValue();
			}
		} catch (RemoteException e) {
			log.severe(e.toString());
			throw new CmRuntimeException(e.toString());
		}
		if (si == -1)
			throw new CmRuntimeException(
				"can't find "
					+ urmJobId
					+ " job "
					+ stepIndex
					+ " step description");
		return usj;
	}

	public void removeUrmJob(EntityManager em, long urmJobId)
		throws ConfigException, CmRuntimeException {
		if (em == null) {
			throw new IllegalArgumentException("null entity manager");
		}
		UrmJob uj = findUrmJobById(urmJobId);
		Collection col = Single.getInst().findStepJobsByUrmId(urmJobId);
		Iterator it = col.iterator();
		UrmStepJob usj = null;
		int si = -1;
		long stepJobId = -1;
		while (it.hasNext()) {
			usj = (UrmStepJob) it.next();
			try {
				si = usj.getStepIndex().intValue();
				stepJobId = usj.getStepJobId().longValue();
			} catch (RemoteException e) {
				log.severe(e.toString());
				continue;
			}
			try {
				switch (si) {
					case BatchMatchAnalyzerBean.BATCH_MATCH_STEP_INDEX :
						{
							BatchJob job = findBatchJobById(em, BatchJobBean.class, stepJobId);
							if (job != null) {
								EJBConfiguration.getInstance().deleteBatchJob(em, job);
							}
						}
						break;
					case BatchMatchAnalyzerBean.TRANS_OABA_STEP_INDEX :
						{
							BatchJob job = findBatchJobById(em, TransitivityJobBean.class, stepJobId);
							assert job instanceof TransitivityJob ;
							if (job != null) {
								EJBConfiguration.getInstance().deleteBatchJob(em, job);
							}
						}
						break;
					case BatchMatchAnalyzerBean.TRANS_SERIAL_STEP_INDEX :
						{
							CmsJob cj =
								Single.getInst().findCmsJobById(stepJobId);
							cj.remove();
						}
						break;
					default :
						log.severe(
							"invalid step job index "
								+ si
								+ " urm job "
								+ urmJobId);
				}
			} catch (Exception e1) {
				log.severe(e1.toString());
			}
		}
		it = col.iterator();
		try {
			while (it.hasNext()) {
				usj = (UrmStepJob) it.next();
				usj.remove();
			}
		} catch (Exception e) {
			log.severe(e.toString());
		}
		try {
			uj.remove();
		} catch (Exception e) {
			log.severe(e.toString());
		}
	}

	public BatchJob findBatchJobById(EntityManager em, Class c, long id) {
		BatchJob retVal =
			EJBConfiguration.getInstance().findBatchJobById(em, c, id);
		return retVal;
	}

	public void deleteBatchJob(EntityManager em, BatchJob job) {
		if (job != null) {
			EJBConfiguration.getInstance().deleteBatchJob(em, job);
		}
	}

	private BatchParametersHome getBatchParamsHome() throws NamingException {
		if (batchParamsHome == null) {
			Context ctx = getInitialContext();

			Object homeRef = ctx.lookup(EJB_BATCH_PARAMS);
			batchParamsHome =
				(BatchParametersHome) PortableRemoteObject.narrow(
					homeRef,
					BatchParametersHome.class);
		}
		return batchParamsHome;
	}

	public BatchParameters findBatchParamsById(long id)
		throws CmRuntimeException, ConfigException {
		try {
			BatchParametersHome home = getBatchParamsHome();
			return home.findByPrimaryKey(new Long(id));
		} catch (Exception e) {
			log.severe(e.toString());
			throw new CmRuntimeException(e.toString());
		}
	}

	public Collection getBatchJobList(EntityManager em) {
		if (em == null) {
			throw new IllegalArgumentException("null entity manager");
		}
		List jobs = EJBConfiguration.getInstance().findAllBatchJobs(em);
		return jobs;
	}

	public TransitivityJob findTransJobById(long id)
		throws ConfigException, CmRuntimeException {
		try {
			if (transJobHome == null) {
				Context ctx = getInitialContext();
				Object homeRef = ctx.lookup(TRANSITIVITY_JOB);
				transJobHome =
					(TransitivityJobHome) PortableRemoteObject.narrow(
						homeRef,
						TransitivityJobHome.class);
			}
			return transJobHome.findByPrimaryKey(new Long(id));
		} catch (ClassCastException e) {
			log.severe(e.toString());
			throw new CmRuntimeException(e.toString());
		} catch (RemoteException e) {
			log.severe(e.toString());
			throw new CmRuntimeException(e.toString());
		} catch (NamingException e) {
			log.severe(e.toString());
			throw new ConfigException(e.toString());
		} catch (FinderException e) {
			log.severe(e.toString());
			throw new ConfigException(e.toString());
		}
	}

	/** This looks up a queue factory on the EJB server.
	 * Don't not specify the prefix java:comp/env/
	 * 
	 * @param jndiQueueName - like jms/sQueue
	 * @return
	 * @throws NamingException
	 */
	private QueueConnectionFactory getMessageQueueFactory(String factoryName)
		throws NamingException {
		if (factoryName == null || factoryName.length() == 0) {
			throw new IllegalArgumentException("null or blank jndi name");
		}
		Context ctx = getInitialContext();
		QueueConnectionFactory retVal =
			(QueueConnectionFactory) ctx.lookup(factoryName);
		return retVal;
	}

	/**
	 * Returns a factory for Queue connections. Note that if a client
	 * uses the factory to create connections, it is the client's
	 * responsibility to close the connections after the client is
	 * finished with the connection.
	 */
	private QueueConnectionFactory getQueueConnectionFactory()
		throws NamingException {
		if (queueConnectionFactory == null) {
			queueConnectionFactory = getMessageQueueFactory(JMS_QUEUE_FACTORY);
		}
		return queueConnectionFactory;
	} // getQueueConnectionFactory

	private QueueSession getQueueSession()
		throws NamingException, JMSException {
		QueueConnectionFactory factory = getQueueConnectionFactory();
		QueueConnection connection = factory.createQueueConnection();
		connection.start();
		QueueSession session =
			connection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
		return session;
	}

	/** Looks up a queue on the EJB server.
	 * 
	 * @param jndiQueueName - like jms/sQueue
	 * @return
	 * @throws NamingException
	 */
	public Queue getMessageQueue(String jndiQueueName)
		throws ConfigException, CmRuntimeException {
		if (jndiQueueName == null || jndiQueueName.length() == 0) {
			throw new CmRuntimeException("null or blank jndi name");
		}
		Queue retVal;
		try {
			Context ctx = getInitialContext();
			retVal = (Queue) ctx.lookup(jndiQueueName);
		} catch (NamingException e) {
			log.severe(e.toString());
			throw new ConfigException(e.toString());
		}
		return retVal;
	}

	public void sendMessage(Queue queue, Serializable data) {
		QueueSession session = null;

		try {
			session = getQueueSession();
			ObjectMessage message = session.createObjectMessage(data);
			QueueSender sender = session.createSender(queue);

			sender.send(message);

			log.fine("Sending on queue '" + queue.getQueueName());
			log.fine("session " + session);
			log.fine("message " + message);
			log.fine("sender " + sender);
		} catch (Exception ex) {
			log.severe(ex.toString());
		} finally {
			try {
				if (session != null)
					session.close();
			} catch (JMSException ex) {
				log.severe(ex.toString());
			}
		}
	}

	public TopicConnectionFactory getTopicConnectionFactory()
		throws NamingException {
		if (topicConnectionFactory == null) {
			Context ctx = getInitialContext();
			topicConnectionFactory =
				(TopicConnectionFactory) ctx.lookup(
					ENC_JNDI_TOPIC_CONNECTION_FACTORY);
		}
		return topicConnectionFactory;
	}

	public DataSource getDataSource(String dsJndiName) throws NamingException {
		DataSource ds = (DataSource) getInitialContext().lookup(dsJndiName);
		return ds;
	}

}
