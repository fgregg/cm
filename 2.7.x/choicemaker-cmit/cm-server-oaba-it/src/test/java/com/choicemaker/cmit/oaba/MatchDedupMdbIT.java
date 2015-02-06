package com.choicemaker.cmit.oaba;

import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_OABA;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.PCT_DONE_OABA;

import java.util.logging.Logger;

import javax.jms.Queue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.runner.RunWith;

import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
//import com.choicemaker.cm.io.blocking.automated.offline.server.impl.SingleRecordMatchMDB;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.UpdateStatusMDB;
import com.choicemaker.cmit.oaba.util.OabaDeploymentUtils;
import com.choicemaker.cmit.utils.OabaProcessingPhase;
import com.choicemaker.cmit.utils.SimplePersonSqlServerTestConfiguration;

@RunWith(Arquillian.class)
public class MatchDedupMdbIT extends
		AbstractOabaMdbTest<SimplePersonSqlServerTestConfiguration> {

	public static final boolean TESTS_AS_EJB_MODULE = false;

	public static final String LOG_SOURCE = MatchDedupMdbIT.class
			.getSimpleName();

	private static final Logger logger = Logger.getLogger(MatchDedupMdbIT.class
			.getName());

	/**
	 * Creates an EAR deployment in which the OABA server JAR is missing the
	 * DedeupOABA and UpdateStatusMDB message beans. This allows other classes
	 * to attach to the chunk and update queues for testing.
	 */
	@Deployment
	public static EnterpriseArchive createEarArchive() {
		Class<?>[] removedClasses = { UpdateStatusMDB.class };
		return OabaDeploymentUtils.createEarArchive(removedClasses,
				TESTS_AS_EJB_MODULE);
	}

	public MatchDedupMdbIT() {
		super(LOG_SOURCE, logger, EVT_DONE_OABA, PCT_DONE_OABA,
				SimplePersonSqlServerTestConfiguration.class,
				OabaProcessingPhase.FINAL);
	}

	@Override
	public final Queue getResultQueue() {
		return null;
	}

	/** Stubbed implementation that does not check the working directory */
	@Override
	public boolean isWorkingDirectoryCorrectAfterProcessing(OabaJob batchJob) {
		return true;
	}

}
