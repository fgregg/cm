package com.choicemaker.cm.transitivity.server.ejb;

import java.util.List;

import javax.ejb.Local;

import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.args.TransitivityParameters;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.BatchJobController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;

@Local
public interface TransitivityJobController extends BatchJobController {

	BatchJob createPersistentTransitivityJob(String externalID,
			TransitivityParameters params, BatchJob batchJob,
			OabaSettings settings, ServerConfiguration sc)
			throws ServerConfigurationException;

	List<BatchJob> findAllByOabaJobId(long oabaJobId);

	BatchJob findTransitivityJob(long id);

	List<BatchJob> findAllTransitivityJobs();

//	TransitivityJobEntity save(TransitivityJobEntity job);

}