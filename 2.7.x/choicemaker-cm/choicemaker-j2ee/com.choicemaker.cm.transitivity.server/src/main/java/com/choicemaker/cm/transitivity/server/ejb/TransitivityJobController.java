package com.choicemaker.cm.transitivity.server.ejb;

import java.util.List;

import javax.ejb.Local;

import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.args.TransitivityParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;
import com.choicemaker.cm.transitivity.server.impl.TransitivityJobEntity;

@Local
public interface TransitivityJobController {

	TransitivityJob createPersistentTransitivityJob(String externalID,
			TransitivityParameters params, OabaJob oabaJob,
			ServerConfiguration sc) throws ServerConfigurationException;

	TransitivityJob save(TransitivityJob batchJob);

	TransitivityJob findTransitivityJob(long id);

	List<TransitivityJob> findAllTransitivityJobs();

	List<TransitivityJobEntity> findAllByOabaJobId(long batchJobId);

	void delete(TransitivityJob transitivityJob);

	void detach(TransitivityJob job);

}