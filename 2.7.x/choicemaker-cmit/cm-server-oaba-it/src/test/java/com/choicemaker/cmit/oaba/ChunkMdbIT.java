package com.choicemaker.cmit.oaba;

import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_CREATE_CHUNK_DATA;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.PCT_DONE_CREATE_CHUNK_DATA;

import java.util.logging.Logger;

import javax.jms.Queue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.runner.RunWith;

import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.MatchSchedulerMDB;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.UpdateStatusMDB;
import com.choicemaker.cmit.oaba.util.OabaDeploymentUtils;
import com.choicemaker.cmit.utils.BatchProcessingPhase;
import com.choicemaker.cmit.utils.SimplePersonSqlServerTestConfiguration;

@RunWith(Arquillian.class)
public class ChunkMdbIT extends
		AbstractOabaMdbTest<SimplePersonSqlServerTestConfiguration> {

	private static final Logger logger = Logger.getLogger(ChunkMdbIT.class
			.getName());

	private static final boolean TESTS_AS_EJB_MODULE = false;

	private final static String LOG_SOURCE = ChunkMdbIT.class.getSimpleName();

	/**
	 * Creates an EAR deployment in which the OABA server JAR is missing the
	 * MatchSchedulerMDB and UpdateStatusMDB message beans. This allows other
	 * classes to attach to the matchSchedulerQueue and update queues for
	 * testing.
	 */
	@Deployment
	public static EnterpriseArchive createEarArchive() {
		Class<?>[] removedClasses = {
				MatchSchedulerMDB.class, UpdateStatusMDB.class };
		return OabaDeploymentUtils.createEarArchive(removedClasses,
				TESTS_AS_EJB_MODULE);
	}

	public ChunkMdbIT() {
		super(LOG_SOURCE, logger, EVT_DONE_CREATE_CHUNK_DATA,
				PCT_DONE_CREATE_CHUNK_DATA,
				SimplePersonSqlServerTestConfiguration.class,
				BatchProcessingPhase.INTERMEDIATE);
	}

	@Override
	public final Queue getResultQueue() {
		return getMatchSchedulerQueue();
	}

	/** Stubbed implementation that does not check the working directory */
	@Override
	public boolean isWorkingDirectoryCorrectAfterProcessing(BatchJob batchJob) {
		return true;
	}

}
