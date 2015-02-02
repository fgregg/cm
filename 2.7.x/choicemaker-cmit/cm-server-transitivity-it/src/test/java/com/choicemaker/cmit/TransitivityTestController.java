package com.choicemaker.cmit;

import javax.ejb.Local;

import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.args.TransitivityParameters;
import com.choicemaker.cm.core.base.Thresholds;
import com.choicemaker.cmit.utils.TestEntityCounts;

@Local
public interface TransitivityTestController {

	/**
	 * Synthesizes the name of a fake modelId configuration using the specified
	 * tag which may be null
	 */
	String createRandomModelConfigurationName(String tag);

	Thresholds createRandomThresholds();

	TransitivityParameters createTransitivityParameters(String tag, TestEntityCounts te);

	ServerConfiguration getDefaultServerConfiguration();

}