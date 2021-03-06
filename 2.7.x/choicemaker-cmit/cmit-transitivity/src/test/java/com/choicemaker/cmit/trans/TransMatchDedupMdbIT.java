package com.choicemaker.cmit.trans;

import static com.choicemaker.cm.transitivity.core.TransitivityProcessing.EVT_TRANSITIVITY_PAIRWISE;
import static com.choicemaker.cm.transitivity.core.TransitivityProcessing.PCT_TRANSITIVITY_PAIRWISE;

import java.util.logging.Logger;

import javax.jms.Queue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.runner.RunWith;

import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.transitivity.server.impl.TransSerializerMDB;
import com.choicemaker.cmit.testconfigs.SimplePersonSqlServerTestConfiguration;
import com.choicemaker.cmit.trans.util.TransitivityDeploymentUtils;
import com.choicemaker.cmit.utils.j2ee.BatchProcessingPhase;

@RunWith(Arquillian.class)
public class TransMatchDedupMdbIT extends
		AbstractTransitivityMdbTest<SimplePersonSqlServerTestConfiguration> {

	private static final Logger logger = Logger
			.getLogger(TransMatchDedupMdbIT.class.getName());

	public static final boolean TESTS_AS_EJB_MODULE = false;

	public static final String LOG_SOURCE = TransMatchDedupMdbIT.class
			.getSimpleName();

	/**
	 * Creates an EAR deployment.
	 */
	@Deployment
	public static EnterpriseArchive createEarArchive() {
		Class<?>[] removedClasses = { TransSerializerMDB.class };
		return TransitivityDeploymentUtils.createEarArchive(removedClasses,
				TESTS_AS_EJB_MODULE);
	}

	public TransMatchDedupMdbIT() {
		super(LOG_SOURCE, logger, EVT_TRANSITIVITY_PAIRWISE,
				PCT_TRANSITIVITY_PAIRWISE,
				SimplePersonSqlServerTestConfiguration.class,
				BatchProcessingPhase.INTERMEDIATE);
	}

	@Override
	public final Queue getResultQueue() {
		return getTransSerializationQueue();
	}

	/** Stubbed implementation that does not check the working directory */
	@Override
	public boolean isWorkingDirectoryCorrectAfterProcessing(BatchJob batchJob) {
		return true;
	}

}
