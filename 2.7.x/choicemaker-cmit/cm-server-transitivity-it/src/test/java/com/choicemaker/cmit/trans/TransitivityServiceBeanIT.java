package com.choicemaker.cmit.trans;

import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_INIT;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.PCT_INIT;

import java.util.logging.Logger;

import javax.jms.Queue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.runner.RunWith;

import com.choicemaker.cm.transitivity.server.impl.StartTransitivityMDB;
import com.choicemaker.cmit.AbstractTransitivityMdbTest;
import com.choicemaker.cmit.trans.util.TransitivityDeploymentUtils;
import com.choicemaker.cmit.utils.OabaProcessingPhase;
import com.choicemaker.cmit.utils.SimplePersonSqlServerTestConfiguration;

@RunWith(Arquillian.class)
public class TransitivityServiceBeanIT extends
		AbstractTransitivityMdbTest<SimplePersonSqlServerTestConfiguration> {

	private static final Logger logger = Logger
			.getLogger(TransitivityServiceBeanIT.class.getName());

	private static final boolean TESTS_AS_EJB_MODULE = true;

	private final static String LOG_SOURCE = TransitivityServiceBeanIT.class
			.getSimpleName();

	/**
	 * Creates an EAR deployment in which the OABA server JAR is missing the
	 * StartTransitivityMDB message bean. This allows another class to attach to
	 * the transitivityQueue for testing.
	 */
	@Deployment
	public static EnterpriseArchive createEarArchive() {
		Class<?>[] removedClasses = { StartTransitivityMDB.class };
		return TransitivityDeploymentUtils.createEarArchive(removedClasses,
				TESTS_AS_EJB_MODULE);
	}

	public TransitivityServiceBeanIT() {
		super(LOG_SOURCE, logger, EVT_INIT, PCT_INIT,
				SimplePersonSqlServerTestConfiguration.class,
				OabaProcessingPhase.INITIAL);
	}

	@Override
	public final Queue getResultQueue() {
		return getTransitivityQueue();
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
