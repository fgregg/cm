package com.choicemaker.cmit.trans;

import static com.choicemaker.cm.transitivity.core.TransitivityProcessing.*;

import java.util.logging.Logger;

import javax.jms.Queue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.runner.RunWith;

import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.transitivity.server.impl.TransMatchSchedulerMDB;
import com.choicemaker.cm.transitivity.server.impl.TransMatchSchedulerSingleton;
import com.choicemaker.cmit.testconfigs.SimplePersonSqlServerTestConfiguration;
import com.choicemaker.cmit.trans.util.TransitivityDeploymentUtils;
import com.choicemaker.cmit.utils.BatchProcessingPhase;

@RunWith(Arquillian.class)
public class StartTransitivityMdbIT extends
		AbstractTransitivityMdbTest<SimplePersonSqlServerTestConfiguration> {

	private static final Logger logger = Logger
			.getLogger(StartTransitivityMdbIT.class.getName());

	private static final boolean TESTS_AS_EJB_MODULE = false;

	private final static String LOG_SOURCE = StartTransitivityMdbIT.class
			.getSimpleName();

	/**
	 * Creates an EAR deployment in which the OABA server JAR is missing the
	 * TransMatch* message beans. This
	 * allows other classes to attach to the block, singleRecordMatch and
	 * updateStatus queues for testing.
	 */
	@Deployment
	public static EnterpriseArchive createEarArchive() {
		Class<?>[] removedClasses =
			new Class<?>[] {
					TransMatchSchedulerSingleton.class,
					TransMatchSchedulerMDB.class };
		return TransitivityDeploymentUtils.createEarArchive(removedClasses,
				TESTS_AS_EJB_MODULE);
	}

	public StartTransitivityMdbIT() {
		super(LOG_SOURCE, logger, EVT_DONE_CREATE_CHUNK_DATA,
				PCT_DONE_CREATE_CHUNK_DATA,
				SimplePersonSqlServerTestConfiguration.class,
				BatchProcessingPhase.INTERMEDIATE);
	}

	@Override
	public final Queue getResultQueue() {
		return getTransMatchSchedulerQueue();
	}

	/** Stubbed implementation that does not check the working directory */
	@Override
	public boolean isWorkingDirectoryCorrectAfterProcessing(BatchJob batchJob) {
		return true;
	}

}
