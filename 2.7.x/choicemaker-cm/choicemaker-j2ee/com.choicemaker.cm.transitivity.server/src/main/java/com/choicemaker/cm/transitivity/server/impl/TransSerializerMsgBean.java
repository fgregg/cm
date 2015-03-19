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

import static com.choicemaker.cm.args.OperationalPropertyNames.PN_TRANSITIVITY_CACHED_GROUPS_FILE;
import static com.choicemaker.cm.args.OperationalPropertyNames.PN_TRANSITIVITY_CACHED_PAIRS_FILE;
import static com.choicemaker.cm.batch.BatchJobStatus.ABORT_REQUESTED;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.choicemaker.cm.args.AnalysisResultFormat;
import com.choicemaker.cm.args.BatchProcessingEvent;
import com.choicemaker.cm.args.IGraphProperty;
import com.choicemaker.cm.args.ProcessingEvent;
import com.choicemaker.cm.args.TransitivityParameters;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.BatchJobStatus;
import com.choicemaker.cm.batch.OperationalPropertyController;
import com.choicemaker.cm.batch.ProcessingController;
import com.choicemaker.cm.batch.ProcessingEventLog;
import com.choicemaker.cm.io.blocking.automated.offline.impl.MatchRecord2CompositeSource;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.transitivity.core.TransitivityResult;
import com.choicemaker.cm.transitivity.core.TransitivityResultCompositeSerializer;
import com.choicemaker.cm.transitivity.core.TransitivitySortType;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJobController;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityParametersController;
import com.choicemaker.cm.transitivity.server.util.ClusteringIteratorFactory;
import com.choicemaker.cm.transitivity.util.CompositeEntityIterator;
import com.choicemaker.cm.transitivity.util.CompositeEntitySource;
import com.choicemaker.cm.transitivity.util.CompositeTextSerializer;
import com.choicemaker.cm.transitivity.util.CompositeXMLSerializer;

/**
 * @version $Revision: 1.3 $ $Date: 2010/10/21 17:42:26 $
 */
@MessageDriven(
		activationConfig = {
				@ActivationConfigProperty(propertyName = "maxSession",
						propertyValue = "1"), // Singleton (JBoss only)
				@ActivationConfigProperty(
						propertyName = "destinationLookup",
						propertyValue = "java:/choicemaker/urm/jms/transSerializationQueue"),
				@ActivationConfigProperty(propertyName = "destinationType",
						propertyValue = "javax.jms.Queue") })
@SuppressWarnings({ "rawtypes" })
public class TransSerializerMsgBean implements MessageListener, Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger
			.getLogger(TransSerializerMsgBean.class.getName());

	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ TransSerializerMsgBean.class.getName());

	public static final int DEFAULT_MAX_RECORD_COUNT = 100000000;

	private static Map<AnalysisResultFormat, TransitivityResultCompositeSerializer> formatSerializer =
		new HashMap<>();
	static {
		formatSerializer.put(AnalysisResultFormat.SORT_BY_HOLD_GROUP,
				new CompositeTextSerializer(
						TransitivitySortType.SORT_BY_HOLD_MERGE_ID));
		formatSerializer.put(AnalysisResultFormat.SORT_BY_RECORD_ID,
				new CompositeTextSerializer(TransitivitySortType.SORT_BY_ID));
		formatSerializer.put(AnalysisResultFormat.XML,
				new CompositeXMLSerializer());
	}

	public static TransitivityResultCompositeSerializer getTransitivityResultSerializer(
			AnalysisResultFormat format) /* throws ConfigException */{
		TransitivityResultCompositeSerializer retVal =
			formatSerializer.get(format);
		if (retVal == null) {
			String msg = "No serializer for analysis format '" + format + "'";
			throw new IllegalStateException(msg);
		}
		return retVal;
	}

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	@EJB
	private TransitivityJobController jobController;

	@EJB
	private OabaSettingsController oabaSettingsController;

	@EJB
	private TransitivityParametersController paramsController;

	@EJB
	private ProcessingController processingController;

	@EJB
	private ServerConfigurationController serverController;

	@EJB
	private OperationalPropertyController propController;

	@Override
	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;

		log.fine("MatchDedupMDB In onMessage");

		BatchJob batchJob = null;
		try {
			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				Object o = msg.getObject();

				if (o instanceof OabaJobMessage) {
					OabaJobMessage data = (OabaJobMessage) o;
					long jobId = data.jobID;
					batchJob = jobController.findTransitivityJob(jobId);
					_onMessage(batchJob);
				} else {
					log.warning("wrong message body: " + o.getClass().getName());
				}

			} else {
				log.warning("wrong type: " + inMessage.getClass().getName());
			}

		} catch (Exception e) {
			log.severe(e.toString());
			if (batchJob != null) {
				batchJob.markAsFailed();
			}
		}
		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
	}

	public void _onMessage(BatchJob batchJob) {
		if (batchJob == null) {
			throw new IllegalArgumentException("null batch job");
		}

		final long jobId = batchJob.getId();
		final TransitivityParameters params =
			this.paramsController.findTransitivityParametersByBatchJobId(jobId);
		final ProcessingEventLog processingEntry =
			processingController.getProcessingLog(batchJob);
		final IGraphProperty graph = params.getGraphProperty();
		final AnalysisResultFormat format = params.getAnalysisResultFormat();
		final String modelConfigId = params.getModelConfigurationName();

		try {
			if (log.isLoggable(Level.FINE)) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				pw.println("Transitivity serialization jobId: " + jobId);
				pw.println("Trans serialization groupProperty: '"
						+ graph.getName() + "'");
				pw.println("Trans serialization resultFormat: '" + format + "'");
				String s = sw.toString();
				log.fine(s);
			}

			final BatchJobStatus jobStatus = batchJob.getStatus();
			if (jobStatus == ABORT_REQUESTED) {
				batchJob.markAsAborted();
				log.fine("Transitivity serialization job marked as aborted: "
						+ batchJob);
				return;
			}

			final String cachedPairsFileName =
				propController.getJobProperty(batchJob,
						PN_TRANSITIVITY_CACHED_PAIRS_FILE);
			log.info("Cached transitivity pairs file: " + cachedPairsFileName);

			String analysisResultFileName =
				TransitivityFileUtils.getGroupResultFileName(batchJob);
			propController.setJobProperty(batchJob,
					PN_TRANSITIVITY_CACHED_GROUPS_FILE, analysisResultFileName);

			MatchRecord2CompositeSource mrs =
				new MatchRecord2CompositeSource(cachedPairsFileName);

			// TODO: replace by extension point
			CompositeEntitySource ces = new CompositeEntitySource(mrs);
			CompositeEntityIterator ceIter = new CompositeEntityIterator(ces);
			String name = graph.getName();
			ClusteringIteratorFactory f =
				ClusteringIteratorFactory.getInstance();
			Iterator clusteringIterator;
			try {
				clusteringIterator = f.createClusteringIterator(name, ceIter);
			} catch (Exception x) {
				log.severe("Unable to create clustering iterator: " + x);
				batchJob.markAsFailed();
				return;
			}

			TransitivityResult tr =
				new TransitivityResult(modelConfigId, params.getLowThreshold(),
						params.getHighThreshold(), clusteringIterator);

			log.fine("serialize to " + format + "format");

			TransitivityResultCompositeSerializer sr =
				getTransitivityResultSerializer(format);
			sr.serialize(tr, analysisResultFileName, DEFAULT_MAX_RECORD_COUNT);

			final Date now = new Date();
			final String info = null;
			sendToUpdateStatus(batchJob, BatchProcessingEvent.DONE, now, info);
			processingEntry
					.setCurrentProcessingEvent(BatchProcessingEvent.DONE);

		} catch (Exception e) {
			log.severe(e.toString());
			if (batchJob != null) {
				batchJob.markAsFailed();
			}
		}
		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
	}

	protected void sendToUpdateStatus(BatchJob job, ProcessingEvent event,
			Date timestamp, String info) {
		processingController.updateStatusWithNotification(job, event,
				timestamp, info);
	}

}
