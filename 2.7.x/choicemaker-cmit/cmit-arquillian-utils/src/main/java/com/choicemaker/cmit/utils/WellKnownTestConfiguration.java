package com.choicemaker.cmit.utils;

import com.choicemaker.cm.core.SerializableRecordSource;
import com.choicemaker.cm.core.base.ImmutableThresholds;

public interface WellKnownTestConfiguration {

	SerializableRecordSource getStagingRecordSource();

	SerializableRecordSource getMasterRecordSource();

	ImmutableThresholds getThresholds();

	String getModelConfigurationName();

	int getSingleRecordMatchingThreshold();

	boolean getTransitivityAnalysisFlag();

}
