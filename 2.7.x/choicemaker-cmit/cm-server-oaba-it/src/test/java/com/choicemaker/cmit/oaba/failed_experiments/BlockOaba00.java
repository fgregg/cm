package com.choicemaker.cmit.oaba.failed_experiments;

import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_OVERSIZED_TRIMMING;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.PCT_DONE_OVERSIZED_TRIMMING;

import java.util.logging.Logger;

import javax.jms.Queue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.runner.RunWith;

import com.choicemaker.cm.io.blocking.automated.offline.server.impl.DedupOABA;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.UpdateStatus;
import com.choicemaker.cmit.oaba.util.OabaDeploymentUtils;
//import com.choicemaker.cm.io.blocking.automated.offline.server.impl.SingleRecordMatch;

@RunWith(Arquillian.class)
public class BlockOaba00 extends AbstractIntermediateProcessing {

	private static final Logger logger = Logger.getLogger(BlockOaba00.class
			.getName());

	private static final boolean TESTS_AS_EJB_MODULE = true;

	private final static String LOG_SOURCE = BlockOaba00.class.getSimpleName();

	/**
	 * Creates an EAR deployment in which the OABA server JAR is missing the
	 * DedeupOABA and UpdateStatus message beans. This allows this class to
	 * attach to the dedup and update queues as intermediate result and status
	 * queues, respectively.
	 */
	@Deployment
	public static EnterpriseArchive createEarArchive() {
		Class<?>[] removedClasses = {
				DedupOABA.class, UpdateStatus.class };
		return OabaDeploymentUtils.createEarArchive(removedClasses,
				BlockOaba00.TESTS_AS_EJB_MODULE);
	}

	@Override
	protected Queue getIntermediateResultQueue() {
		return getDedupQueue();
	}

	@Override
	protected int getIntermediateResultEventId() {
		return EVT_DONE_OVERSIZED_TRIMMING;
	}

	@Override
	protected int getIntermediateResultPercentComplete() {
		return PCT_DONE_OVERSIZED_TRIMMING;
	}

	@Override
	protected String getSourceName() {
		return LOG_SOURCE;
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

}
