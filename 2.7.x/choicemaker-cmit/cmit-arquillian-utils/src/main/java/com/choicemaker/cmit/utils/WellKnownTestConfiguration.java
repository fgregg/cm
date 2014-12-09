package com.choicemaker.cmit.utils;

import com.choicemaker.cm.args.OabaTaskType;
import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.core.ISerializableDbRecordSource;
import com.choicemaker.cm.core.base.ImmutableThresholds;

public interface WellKnownTestConfiguration {

	PersistableRecordSource getStagingRecordSource();

	ISerializableDbRecordSource getSerializableStagingRecordSource();

	PersistableRecordSource getMasterRecordSource();

	OabaTaskType getOabaTask();

	ISerializableDbRecordSource getSerializableMasterRecordSource();

	ImmutableThresholds getThresholds();

	String getModelConfigurationName();

	int getSingleRecordMatchingThreshold();

	boolean getTransitivityAnalysisFlag();

}
