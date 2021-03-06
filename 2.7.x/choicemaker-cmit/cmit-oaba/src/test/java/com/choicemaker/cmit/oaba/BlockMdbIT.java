package com.choicemaker.cmit.oaba;

import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_OVERSIZED_TRIMMING;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.PCT_DONE_OVERSIZED_TRIMMING;

import java.util.logging.Logger;

import javax.jms.Queue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.runner.RunWith;

import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.DedupMDB;
import com.choicemaker.cmit.oaba.util.OabaDeploymentUtils;
import com.choicemaker.cmit.testconfigs.SimplePersonSqlServerTestConfiguration;
import com.choicemaker.cmit.utils.j2ee.BatchProcessingPhase;

@RunWith(Arquillian.class)
public class BlockMdbIT extends
		AbstractOabaMdbTest<SimplePersonSqlServerTestConfiguration> {

	private static final Logger logger = Logger.getLogger(BlockMdbIT.class
			.getName());

	private static final boolean TESTS_AS_EJB_MODULE = false;

	private final static String LOG_SOURCE = BlockMdbIT.class.getSimpleName();

	/**
	 * Creates an EAR deployment in which the OABA server JAR is missing the
	 * DedeupOABA message bean. This allows this class to
	 * attach to the deduplication and update queues as intermediate result and
	 * status queues, respectively.
	 */
	@Deployment
	public static EnterpriseArchive createEarArchive() {
		Class<?>[] removedClasses = { DedupMDB.class };
		return OabaDeploymentUtils.createEarArchive(removedClasses,
				BlockMdbIT.TESTS_AS_EJB_MODULE);
	}

	public BlockMdbIT() {
		super(LOG_SOURCE, logger, EVT_DONE_OVERSIZED_TRIMMING,
				PCT_DONE_OVERSIZED_TRIMMING,
				SimplePersonSqlServerTestConfiguration.class,
				BatchProcessingPhase.INTERMEDIATE);
	}

	@Override
	public final Queue getResultQueue() {
		return getDedupQueue();
	}

	/** Stubbed implementation that does not check the working directory */
	@Override
	public boolean isWorkingDirectoryCorrectAfterProcessing(BatchJob batchJob) {
		return true;
	}

}
