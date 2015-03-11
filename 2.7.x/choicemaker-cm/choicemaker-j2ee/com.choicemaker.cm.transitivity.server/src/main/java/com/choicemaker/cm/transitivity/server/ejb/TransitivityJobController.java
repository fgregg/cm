package com.choicemaker.cm.transitivity.server.ejb;

import java.util.List;

import javax.ejb.Local;

import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.args.TransitivityParameters;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;

@Local
public interface TransitivityJobController {

	BatchJob createPersistentTransitivityJob(String externalID,
			TransitivityParameters params, BatchJob batchJob,
			ServerConfiguration sc) throws ServerConfigurationException;

	BatchJob save(BatchJob batchJob);

//	TransitivityJobEntity save(TransitivityJobEntity job);

	BatchJob findTransitivityJob(long id);

	List<BatchJob> findAllTransitivityJobs();

	List<BatchJob> findAllByOabaJobId(long batchJobId);

	void delete(BatchJob transitivityJob);

	void detach(BatchJob job);

}