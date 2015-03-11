package com.choicemaker.cm.io.blocking.automated.offline.server.ejb;

import java.util.List;

import javax.ejb.Local;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobEntity;

@Local
public interface OabaJobController {

	BatchJob createPersistentOabaJob(String externalID, OabaParameters params,
			OabaSettings settings, ServerConfiguration sc)
			throws ServerConfigurationException;

	BatchJob save(BatchJob batchJob);

	OabaJobEntity save(OabaJobEntity job);

	BatchJob findOabaJob(long id);

	List<BatchJob> findAll();

	void delete(BatchJob batchJob);

	void detach(BatchJob oabaJob);

}