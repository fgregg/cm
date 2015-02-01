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

	OabaJob createPersistentOabaJob(String externalID, OabaParameters params,
			OabaSettings settings, ServerConfiguration sc)
			throws ServerConfigurationException;

	OabaJob save(OabaJob batchJob);

	OabaJobEntity save(OabaJobEntity job);

	OabaJob findOabaJob(long id);

	List<OabaJob> findAll();

	void delete(OabaJob oabaJob);

	void detach(BatchJob oabaJob);

}