package com.choicemaker.cm.io.blocking.automated.offline.server.ejb;

import javax.ejb.Local;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.BatchJobController;

@Local
public interface OabaJobController extends BatchJobController {

	BatchJob createPersistentOabaJob(String externalID, OabaParameters params,
			OabaSettings settings, ServerConfiguration sc)
			throws ServerConfigurationException;

	BatchJob createPersistentOabaJob(String externalID,
			OabaParameters batchParams, OabaSettings oabaSettings,
			ServerConfiguration serverConfiguration, BatchJob urmJob)
			throws ServerConfigurationException;

	BatchJob findOabaJob(long id);

	// OabaJobEntity save(OabaJobEntity job);

}