package com.choicemaker.cmit.oaba;

import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_MATCHING_DATA;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.PCT_DONE_MATCHING_DATA;

import java.util.logging.Logger;

import javax.jms.Queue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.runner.RunWith;

import com.choicemaker.cm.io.blocking.automated.offline.server.impl.MatchDedupMDB;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.UpdateStatusMDB;
import com.choicemaker.cmit.oaba.util.OabaDeploymentUtils;
import com.choicemaker.cmit.utils.AbstractOabaProcessingTest;
import com.choicemaker.cmit.utils.OabaProcessingPhase;
import com.choicemaker.cmit.utils.SimplePersonSqlServerTestConfiguration;

@RunWith(Arquillian.class)
public class MatchSchedulerIT extends
		AbstractOabaProcessingTest<SimplePersonSqlServerTestConfiguration> {

	private static final Logger logger = Logger.getLogger(MatchSchedulerIT.class
			.getName());

	private static final boolean TESTS_AS_EJB_MODULE = true;

	private final static String LOG_SOURCE = MatchSchedulerIT.class
			.getSimpleName();

	/**
	 * Creates an EAR deployment in which the OABA server JAR is missing the
	 * MatchDedupMDB and UpdateStatusMDB message beans. This allows other classes to
	 * attach to the matchDedup and update queues for testing.
	 */
	@Deployment
	public static EnterpriseArchive createEarArchive() {
		Class<?>[] removedClasses = {
				MatchDedupMDB.class, UpdateStatusMDB.class };
		return OabaDeploymentUtils.createEarArchive(removedClasses,
				TESTS_AS_EJB_MODULE);
	}

	public MatchSchedulerIT() {
		super(LOG_SOURCE, logger, EVT_DONE_MATCHING_DATA,
				PCT_DONE_MATCHING_DATA,
				SimplePersonSqlServerTestConfiguration.class,
				OabaProcessingPhase.INTERMEDIATE);
	}

	@Override
	public final Queue getResultQueue() {
		return getMatchDedupQueue();
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
