package com.choicemaker.cmit.oaba;

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
import com.choicemaker.cmit.utils.OabaProcessingPhase;
import com.choicemaker.cmit.utils.SimplePersonSqlServerTestConfiguration;

@RunWith(Arquillian.class)
public class BlockOabaIT extends
		AbstractOabaProcessingTest<SimplePersonSqlServerTestConfiguration> {

	private static final Logger logger = Logger.getLogger(BlockOabaIT.class
			.getName());

	private static final boolean TESTS_AS_EJB_MODULE = true;

	private final static String LOG_SOURCE = BlockOabaIT.class
			.getSimpleName();

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
				BlockOabaIT.TESTS_AS_EJB_MODULE);
	}

	public BlockOabaIT() {
		super(LOG_SOURCE, logger, EVT_DONE_OVERSIZED_TRIMMING,
				PCT_DONE_OVERSIZED_TRIMMING,
				SimplePersonSqlServerTestConfiguration.class,
				OabaProcessingPhase.INTERMEDIATE);
	}

	@Override
	public final Queue getResultQueue() {
		return getDedupQueue();
	}

	/** Stubbed implementation that does not check the working directory */
	@Override
	public boolean isWorkingDirectoryCorrectAfterLinkageProcessing() {
		return true;
	}

	/** Stubbed implementation that does not check the working directory */
	@Override
	public boolean isWorkingDirectoryCorrectAfterDeduplicationProcessing() {
		return true;
	}

}
