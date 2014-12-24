package com.choicemaker.cmit.oaba;

import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_INIT;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.PCT_INIT;

import java.util.logging.Logger;

import javax.jms.Queue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.runner.RunWith;

import com.choicemaker.cm.io.blocking.automated.offline.server.impl.StartOabaMDB;
import com.choicemaker.cmit.oaba.util.OabaDeploymentUtils;
import com.choicemaker.cmit.utils.AbstractOabaProcessingTest;
import com.choicemaker.cmit.utils.OabaProcessingPhase;
import com.choicemaker.cmit.utils.SimplePersonSqlServerTestConfiguration;

@RunWith(Arquillian.class)
public class BatchQueryServiceBeanIT extends
		AbstractOabaProcessingTest<SimplePersonSqlServerTestConfiguration> {

	private static final Logger logger = Logger
			.getLogger(BatchQueryServiceBeanIT.class.getName());

	private static final boolean TESTS_AS_EJB_MODULE = true;

	private final static String LOG_SOURCE = BatchQueryServiceBeanIT.class
			.getSimpleName();

	/**
	 * Creates an EAR deployment in which the OABA server JAR is missing the
	 * StartOabaMDB message bean. This allows another class to attach to the
	 * startQueue for testing.
	 */
	@Deployment
	public static EnterpriseArchive createEarArchive() {
		Class<?>[] removedClasses = { StartOabaMDB.class };
		return OabaDeploymentUtils.createEarArchive(removedClasses,
				TESTS_AS_EJB_MODULE);
	}

	public BatchQueryServiceBeanIT() {
		super(LOG_SOURCE, logger, EVT_INIT, PCT_INIT,
				SimplePersonSqlServerTestConfiguration.class,
				OabaProcessingPhase.INITIAL);
	}

	@Override
	public final Queue getResultQueue() {
		return getStartQueue();
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
