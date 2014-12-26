package com.choicemaker.cmit.utils;

import com.choicemaker.cm.args.AnalysisResultFormat;
import com.choicemaker.cm.args.OabaLinkageType;
import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.core.ISerializableDbRecordSource;
import com.choicemaker.cm.core.base.ImmutableThresholds;
import com.choicemaker.e2.CMPluginRegistry;

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

	AnalysisResultFormat getTransitivityResultFormat();

	String getTransitivityGraphProperty();

	/** Post-construction method */
	void initialize(CMPluginRegistry registry);

}
