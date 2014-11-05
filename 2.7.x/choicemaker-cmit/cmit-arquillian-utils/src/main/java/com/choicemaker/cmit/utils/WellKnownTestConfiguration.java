package com.choicemaker.cmit.utils;

import com.choicemaker.cm.core.SerialRecordSource;
import com.choicemaker.cm.core.base.ImmutableThresholds;

public interface WellKnownTestConfiguration {

	SerialRecordSource getStagingRecordSource();

	SerialRecordSource getMasterRecordSource();

	ImmutableThresholds getThresholds();

	String getModelConfigurationName();

	int getSingleRecordMatchingThreshold();

	boolean getTransitivityAnalysisFlag();

}
