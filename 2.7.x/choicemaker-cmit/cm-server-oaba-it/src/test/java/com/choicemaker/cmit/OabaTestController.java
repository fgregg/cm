package com.choicemaker.cmit;

import javax.ejb.Local;

import com.choicemaker.cm.core.base.Thresholds;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersEntity;
import com.choicemaker.cmit.utils.TestEntityCounts;

@Local
public interface OabaTestController {

	/**
	 * Synthesizes the name of a fake modelId configuration using the specified
	 * tag which may be null
	 */
	String createRandomModelConfigurationName(String tag);

	Thresholds createRandomThresholds();

	OabaParametersEntity createBatchParameters(String tag, TestEntityCounts te);

}