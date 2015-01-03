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

import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Queue;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ISerializableRecordSource;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEvent;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEventLog;
import com.choicemaker.cm.io.blocking.automated.offline.impl.IDSetSource;
import com.choicemaker.cm.io.blocking.automated.offline.impl.RecordIDTranslator2;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaFileUtils;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.services.ChunkService3;
import com.choicemaker.cm.io.blocking.automated.offline.utils.Transformer;
import com.choicemaker.cm.io.blocking.automated.offline.utils.TreeTransformer;

/**
 * This bean handles the creation of chunks, including chunk data files and
 * their corresponding block files.
 *
 * In this version, a chunk has multiple tree or array files so mutiple beans
 * are process the same chunk at the same time.
 *
 * @author pcheung
 *
 */
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationLookup",
				propertyValue = "java:/choicemaker/urm/jms/chunkQueue"),
		@ActivationConfigProperty(propertyName = "destinationType",
				propertyValue = "javax.jms.Queue") })
public class Chunk2MDB extends AbstractOabaMDB {

	private static final long serialVersionUID = 271L;

	private static final Logger log = Logger.getLogger(Chunk2MDB.class
			.getName());

	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ Chunk2MDB.class.getName());

	@Resource(lookup = "java:/choicemaker/urm/jms/matchSchedulerQueue")
	private Queue matchSchedulerQueue;

	@Override
	protected void processOabaMessage(OabaJobMessage data, OabaJob oabaJob,
			OabaParameters params, OabaSettings oabaSettings,
			OabaEventLog processingLog, ServerConfiguration serverConfig,
			ImmutableProbabilityModel model) throws BlockingException {

		final int maxChunk = oabaSettings.getMaxChunkSize();
		final int numProcessors = serverConfig.getMaxChoiceMakerThreads();
		final int maxChunkFiles = serverConfig.getMaxOabaChunkFileCount();
		log.info("Maximum chunk size: " + maxChunk);
		log.info("Number of processors: " + numProcessors);
		log.info("Maximum chunk files: " + maxChunkFiles);

		RecordIDTranslator2 translator =
			new RecordIDTranslator2(OabaFileUtils.getTransIDFactory(oabaJob));
		// recover the translator
		translator.recover();
		translator.close();
		log.info("Record translator: " + translator);

		// create the os block source.
		final IBlockSinkSourceFactory osFactory =
			OabaFileUtils.getOversizedFactory(oabaJob);
		log.info("Oversized factory: " + osFactory);
		osFactory.getNextSource(); // the deduped OS file is file 2.
		final IDSetSource source2 = new IDSetSource(osFactory.getNextSource());
		log.info("Deduped oversized source: " + source2);

		// create the tree transformer.
		final TreeTransformer tTransformer =
			new TreeTransformer(translator,
					OabaFileUtils.getComparisonTreeGroupFactory(oabaJob,
							data.stageType, numProcessors));

		// create the transformer for over-sized blocks
		final Transformer transformerO =
			new Transformer(translator,
					OabaFileUtils.getComparisonArrayGroupFactoryOS(oabaJob,
							numProcessors));

		ISerializableRecordSource staging = null;
		ISerializableRecordSource master = null;
		try {
			staging = getRecordSourceController().getStageRs(params);
			master = getRecordSourceController().getMasterRs(params);
		} catch (Exception e) {
			throw new BlockingException(e.toString());
		}
		assert staging != null;

		ChunkService3 chunkService =
			new ChunkService3(OabaFileUtils.getTreeSetSource(oabaJob), source2,
					staging, master, model,
					OabaFileUtils.getChunkIDFactory(oabaJob),
					OabaFileUtils.getStageDataFactory(oabaJob, model),
					OabaFileUtils.getMasterDataFactory(oabaJob, model),
					translator.getSplitIndex(), tTransformer, transformerO,
					maxChunk, maxChunkFiles, processingLog, oabaJob);
		log.info("Chunk service: " + chunkService);
		chunkService.runService();
		log.info("Number of chunks " + chunkService.getNumChunks());
		log.info("Number of regular chunks "
				+ chunkService.getNumRegularChunks());
		log.info("Done creating chunks " + chunkService.getTimeElapsed());

		// transitivity needs the translator
		// translator.cleanUp();

		data.numChunks = chunkService.getNumChunks();
		data.numRegularChunks = chunkService.getNumRegularChunks();
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	protected Logger getJmsTrace() {
		return jmsTrace;
	}

	@Override
	protected OabaEvent getCompletionEvent() {
		return OabaEvent.DONE_CREATE_CHUNK_DATA;
	}

	@Override
	protected void notifyProcessingCompleted(OabaJobMessage data) {
		MessageBeanUtils.sendStartData(data, getJmsContext(),
				matchSchedulerQueue, getLogger());
	}

}
