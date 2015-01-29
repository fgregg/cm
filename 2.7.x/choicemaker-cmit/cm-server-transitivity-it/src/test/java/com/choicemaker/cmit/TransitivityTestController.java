package com.choicemaker.cmit;

import java.util.List;

import javax.ejb.Local;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.args.TransitivityParameters;
import com.choicemaker.cm.core.base.Thresholds;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersEntity;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJob;
import com.choicemaker.cmit.utils.TestEntityCounts;

@Local
public interface TransitivityTestController {

	/**
	 * Synthesizes the name of a fake modelId configuration using the specified
	 * tag which may be null
	 */
	String createRandomModelConfigurationName(String tag);

	Thresholds createRandomThresholds();

	OabaParametersEntity createOabaParameters(String tag, TestEntityCounts te);

	ServerConfiguration getDefaultServerConfiguration();

//	void removeTestEntities(TestEntityCounts te);

	List<OabaParameters> findAllOabaParameters();

	List<OabaJob> findAllOabaJobs();

	List<OabaProcessing> findAllOabaProcessing();

	List<TransitivityJob> findAllTransitivityJobs();

	List<TransitivityParameters> findAllTransitivityParameters();

}