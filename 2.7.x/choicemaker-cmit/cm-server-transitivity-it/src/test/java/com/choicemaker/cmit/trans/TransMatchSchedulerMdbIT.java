package com.choicemaker.cmit.trans;

import static com.choicemaker.cm.transitivity.core.TransitivityProcessing.EVT_DONE_MATCHING_DATA;
import static com.choicemaker.cm.transitivity.core.TransitivityProcessing.PCT_DONE_MATCHING_DATA;

import java.util.logging.Logger;

import javax.jms.Queue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.runner.RunWith;

import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.transitivity.server.impl.TransMatchDedupMDB;
import com.choicemaker.cmit.testconfigs.SimplePersonSqlServerTestConfiguration;
import com.choicemaker.cmit.trans.util.TransitivityDeploymentUtils;
import com.choicemaker.cmit.utils.BatchProcessingPhase;

@RunWith(Arquillian.class)
public class TransMatchSchedulerMdbIT extends
		AbstractTransitivityMdbTest<SimplePersonSqlServerTestConfiguration> {

	private static final Logger logger = Logger
			.getLogger(TransMatchSchedulerMdbIT.class.getName());

	private static final boolean TESTS_AS_EJB_MODULE = false;

	private final static String LOG_SOURCE = TransMatchSchedulerMdbIT.class
			.getSimpleName();

	/**
	 * Creates an EAR deployment in which the transitivity server JAR is missing
	 * the MatchDedupMDB message bean. This allows other classes to attach to
	 * the matchDedup and update queues for testing.
	 */
	@Deployment
	public static EnterpriseArchive createEarArchive() {
		Class<?>[] removedClasses = { TransMatchDedupMDB.class };
		return TransitivityDeploymentUtils.createEarArchive(removedClasses,
				TESTS_AS_EJB_MODULE);
	}

	public TransMatchSchedulerMdbIT() {
		super(LOG_SOURCE, logger, EVT_DONE_MATCHING_DATA,
				PCT_DONE_MATCHING_DATA,
				SimplePersonSqlServerTestConfiguration.class,
				BatchProcessingPhase.INTERMEDIATE);
	}

	@Override
	public final Queue getResultQueue() {
		return getTransMatchDedupQueue();
	}

	/** Stubbed implementation that does not check the working directory */
	@Override
	public boolean isWorkingDirectoryCorrectAfterProcessing(BatchJob batchJob) {
		return true;
	}

}
