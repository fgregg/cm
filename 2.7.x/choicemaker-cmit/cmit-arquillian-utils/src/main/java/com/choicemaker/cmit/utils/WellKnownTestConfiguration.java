package com.choicemaker.cmit.utils;

import com.choicemaker.cm.args.OabaLinkageType;
import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.core.ISerializableDbRecordSource;
import com.choicemaker.cm.core.base.ImmutableThresholds;

public interface WellKnownTestConfiguration {

	PersistableRecordSource getStagingRecordSource();

	ISerializableDbRecordSource getSerializableStagingRecordSource();

	PersistableRecordSource getMasterRecordSource();

	OabaLinkageType getOabaTask();

	ISerializableDbRecordSource getSerializableMasterRecordSource();

	ImmutableThresholds getThresholds();

	String getModelConfigurationName();

	int getSingleRecordMatchingThreshold();

	boolean getTransitivityAnalysisFlag();

}
