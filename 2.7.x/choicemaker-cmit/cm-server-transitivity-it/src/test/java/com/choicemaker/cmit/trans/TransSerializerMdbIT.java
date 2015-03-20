package com.choicemaker.cmit.trans;

import static com.choicemaker.cm.args.BatchProcessing.EVT_DONE;
import static com.choicemaker.cm.args.BatchProcessing.PCT_DONE;

import java.util.logging.Logger;

import javax.jms.Queue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.runner.RunWith;

import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cmit.trans.util.TransitivityDeploymentUtils;
import com.choicemaker.cmit.utils.BatchProcessingPhase;
import com.choicemaker.cmit.utils.SimplePersonSqlServerTestConfiguration;

@RunWith(Arquillian.class)
public class TransSerializerMdbIT extends
		AbstractTransitivityMdbTest<SimplePersonSqlServerTestConfiguration> {

	private static final Logger logger = Logger.getLogger(TransSerializerMdbIT.class
			.getName());

	public static final boolean TESTS_AS_EJB_MODULE = false;

	public static final String LOG_SOURCE = TransSerializerMdbIT.class
			.getSimpleName();

	/**
	 * Creates an EAR deployment.
	 */
	@Deployment
	public static EnterpriseArchive createEarArchive() {
		Class<?>[] removedClasses = { };
		return TransitivityDeploymentUtils.createEarArchive(removedClasses,
				TESTS_AS_EJB_MODULE);
	}

	public TransSerializerMdbIT() {
		super(LOG_SOURCE, logger, EVT_DONE, PCT_DONE,
				SimplePersonSqlServerTestConfiguration.class,
				BatchProcessingPhase.FINAL);
	}

	@Override
	public final Queue getResultQueue() {
		return null;
	}

	/** Stubbed implementation that does not check the working directory */
	@Override
	public boolean isWorkingDirectoryCorrectAfterProcessing(BatchJob batchJob) {
		return true;
	}

}
