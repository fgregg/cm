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
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEvent;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEventLog;
import com.choicemaker.cm.io.blocking.automated.offline.impl.BlockGroup;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.services.BlockDedupService4;
import com.choicemaker.cm.io.blocking.automated.offline.services.OversizedDedupService;

/**
 * This bean handles the deduping of blocks and oversized blocks.
 *
 * @author pcheung
 *
 */
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationLookup",
				propertyValue = "java:/choicemaker/urm/jms/dedupQueue"),
		@ActivationConfigProperty(propertyName = "destinationType",
				propertyValue = "javax.jms.Queue") })
public class DedupMDB extends AbstractOabaMDB {

	private static final long serialVersionUID = 271L;

	private static final Logger log = Logger
			.getLogger(DedupMDB.class.getName());

	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ DedupMDB.class.getName());

	@Resource(lookup = "java:/choicemaker/urm/jms/chunkQueue")
	private Queue chunkQueue;

	@Override
	protected void processOabaMessage(OabaJobMessage data, OabaJob oabaJob,
			OabaParameters params, OabaSettings oabaSettings,
			OabaEventLog processingLog, ServerConfiguration serverConfig,
			ImmutableProbabilityModel model) throws BlockingException {

		// Handle regular blocking sets
		final int maxBlock = oabaSettings.getMaxBlockSize();
		final int interval = oabaSettings.getInterval();
		final BlockGroup bGroup =
			new BlockGroup(OabaFileUtils.getBlockGroupFactory(oabaJob),
					maxBlock);
		BlockDedupService4 dedupService =
			new BlockDedupService4(bGroup,
					OabaFileUtils.getBigBlocksSinkSourceFactory(oabaJob),
					OabaFileUtils.getTempBlocksSinkSourceFactory(oabaJob),
					OabaFileUtils.getSuffixTreeSink(oabaJob), maxBlock,
					processingLog, oabaJob, interval);
		dedupService.runService();
		log.info("Done block dedup " + dedupService.getTimeElapsed());
		log.info("Blocks In " + dedupService.getNumBlocksIn());
		log.info("Blocks Out " + dedupService.getNumBlocksOut());
		log.info("Tree Out " + dedupService.getNumTreesOut());

		// Handle oversized blocking sets
		final IBlockSink osSpecial =
			OabaFileUtils.getOversizedFactory(oabaJob).getNextSink();
		final IBlockSinkSourceFactory osFactory =
			OabaFileUtils.getOversizedFactory(oabaJob);
		final IBlockSource osSource = osFactory.getSource(osSpecial);
		final IBlockSink osDedup = osFactory.getNextSink();

		OversizedDedupService osDedupService =
			new OversizedDedupService(osSource, osDedup,
					OabaFileUtils.getOversizedTempFactory(oabaJob),
					processingLog, oabaJob);
		osDedupService.runService();
		log.info("Done oversized dedup " + osDedupService.getTimeElapsed());
		log.info("Num OS Before " + osDedupService.getNumBlocksIn());
		log.info("Num OS After Exact " + osDedupService.getNumAfterExact());
		log.info("Num OS Done " + osDedupService.getNumBlocksOut());
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
		return OabaEvent.DONE_DEDUP_OVERSIZED;
	}

	@Override
	protected void notifyProcessingCompleted(OabaJobMessage data) {
		MessageBeanUtils.sendStartData(data, getJmsContext(), chunkQueue,
				getLogger());
	}

}
