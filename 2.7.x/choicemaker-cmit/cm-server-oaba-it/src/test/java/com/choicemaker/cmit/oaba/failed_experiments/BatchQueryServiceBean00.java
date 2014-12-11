package com.choicemaker.cmit.oaba.failed_experiments;

import java.util.logging.Logger;

import javax.jms.Queue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.runner.RunWith;

import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.StartOABA;
import com.choicemaker.cmit.oaba.util.OabaDeploymentUtils;

@RunWith(Arquillian.class)
public class BatchQueryServiceBean00 extends AbstractIntermediateProcessing {

	private static final Logger logger = Logger
			.getLogger(BatchQueryServiceBean00.class.getName());

	private static final boolean TESTS_AS_EJB_MODULE = true;

	private final static String LOG_SOURCE = BatchQueryServiceBean00.class
			.getSimpleName();

	/**
	 * Creates an EAR deployment in which the OABA server JAR is missing the
	 * StartOABA message bean. This allows another class to attach to the
	 * startQueue for testing.
	 */
	@Deployment
	public static EnterpriseArchive createEarArchive() {
		Class<?>[] removedClasses = { StartOABA.class };
		return OabaDeploymentUtils.createEarArchive(removedClasses,
				TESTS_AS_EJB_MODULE);
	}

	@Override
	protected Queue getIntermediateResultQueue() {
		return getStartQueue();
	}

	@Override
	protected int getIntermediateResultEventId() {
		return OabaProcessing.EVT_INIT;
	}

	@Override
	protected int getIntermediateResultPercentComplete() {
		return OabaProcessing.PCT_INIT;
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
