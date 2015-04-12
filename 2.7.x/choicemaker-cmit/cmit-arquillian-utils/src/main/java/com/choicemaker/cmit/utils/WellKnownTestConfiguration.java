package com.choicemaker.cmit.utils;

import com.choicemaker.cm.args.AnalysisResultFormat;
import com.choicemaker.cm.args.OabaLinkageType;
import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.core.ISerializableDbRecordSource;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.ImmutableThresholds;
import com.choicemaker.e2.CMPluginRegistry;

public interface WellKnownTestConfiguration {

	ImmutableProbabilityModel getModel();

	String getModelConfigurationName();

	OabaLinkageType getOabaTask();

	ISerializableDbRecordSource getSerializableMasterRecordSource();

	ISerializableDbRecordSource getSerializableStagingRecordSource();

	int getSingleRecordMatchingThreshold();

	PersistableRecordSource getQueryRecordSource();
	
	boolean isQueryRsDeduplicated();

	String getQueryDatabaseConfiguration();

	String getQueryBlockingConfiguration();

	PersistableRecordSource getReferenceRecordSource();

	String getReferenceDatabaseConfiguration();

	String getReferenceBlockingConfiguration();

	ImmutableThresholds getThresholds();

	boolean getTransitivityAnalysisFlag();

	String getTransitivityGraphProperty();

	AnalysisResultFormat getTransitivityResultFormat();

	/** Post-construction method */
	void initialize(OabaLinkageType type, CMPluginRegistry registry);

}
