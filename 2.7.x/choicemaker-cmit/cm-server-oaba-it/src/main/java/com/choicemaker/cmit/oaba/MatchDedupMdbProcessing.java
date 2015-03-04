package com.choicemaker.cmit.oaba;

import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_OABA;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.PCT_DONE_OABA;

import java.util.logging.Logger;

import javax.jms.Queue;

import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cmit.utils.OabaProcessingPhase;
import com.choicemaker.cmit.utils.SimplePersonSqlServerTestConfiguration;

/**
 * This class is reused in other modules to perform a test linkage or deduplication.
 * It defers from {@link MatchDedupMdbIT} only in that it lacks an Arquillian
 * shrink-wrap method and an Arquillian <code>RunWith</code> directive.
 * 
 * @author rphall
 */
public class MatchDedupMdbProcessing extends
		AbstractOabaMdbTest<SimplePersonSqlServerTestConfiguration> {

	public static final String LOG_SOURCE = MatchDedupMdbProcessing.class
			.getSimpleName();

	private static final Logger logger = Logger.getLogger(MatchDedupMdbProcessing.class
			.getName());

	public MatchDedupMdbProcessing() {
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
