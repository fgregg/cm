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

import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaOperationalPropertyNames.PN_CHUNK_FILE_COUNT;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaOperationalPropertyNames.PN_RECORD_ID_TYPE;

import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.jms.Queue;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ISerializableRecordSource;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEvent;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEventLog;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;
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
 * @author pcheung
 *
 */
@Deprecated
public class ChunkMDB extends AbstractOabaMDB {

	private static final long serialVersionUID = 271L;

	private static final Logger log = Logger
			.getLogger(ChunkMDB.class.getName());

	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ ChunkMDB.class.getName());

	@Resource(lookup = "java:/choicemaker/urm/jms/matchSchedulerQueue")
	private Queue matchSchedulerQueue;

	@Override
	protected void processOabaMessage(OabaJobMessage data, OabaJob oabaJob,
			OabaParameters params, OabaSettings oabaSettings,
			OabaEventLog processingLog, ServerConfiguration serverConfig,
			ImmutableProbabilityModel model) throws BlockingException {
		String temp = (String) model.properties().get("maxChunkSize");
		int maxChunk = Integer.parseInt(temp);

		// get the maximum number of chunk files
		temp = (String) model.properties().get("maxChunkFiles");
		int maxChunkFiles = Integer.parseInt(temp);

		RecordIDTranslator2 translator =
			new RecordIDTranslator2(OabaFileUtils.getTransIDFactory(oabaJob));
		// recover the translator
		translator.recover();
		translator.close();

		// create the os block source.
		IBlockSinkSourceFactory osFactory =
			OabaFileUtils.getOversizedFactory(oabaJob);
		osFactory.getNextSource(); // the deduped OS file is file 2.
		IDSetSource source2 = new IDSetSource(osFactory.getNextSource());

		// create the tree transformer
		final String _recordIdType =
			getPropertyController().getJobProperty(oabaJob, PN_RECORD_ID_TYPE);
		final RECORD_ID_TYPE recordIdType =
			RECORD_ID_TYPE.valueOf(_recordIdType);
		@SuppressWarnings("rawtypes")
		IComparisonTreeSinkSourceFactory ctssf =
			OabaFileUtils.getComparisonTreeFactory(oabaJob, recordIdType);
		TreeTransformer tTransformer = new TreeTransformer(translator, ctssf);

		// create the oversized block transformer
		Transformer transformerO =
			new Transformer(translator,
					OabaFileUtils.getComparisonArrayFactoryOS(oabaJob));

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
		chunkService.runService();
		log.info("Done creating chunks " + chunkService.getTimeElapsed());

		translator.cleanUp();

		final int numChunks = chunkService.getNumChunks();
		log.info("Number of chunks " + numChunks);
		getPropertyController().setJobProperty(oabaJob, PN_CHUNK_FILE_COUNT,
				String.valueOf(numChunks));
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
