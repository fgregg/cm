package com.choicemaker.cmit.utils.j2ee;

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

	ImmutableThresholds getThresholds();

	String getBlockingConfiguration();

	PersistableRecordSource getQueryRecordSource();
	
	boolean isQueryRsDeduplicated();

	String getQueryDatabaseConfiguration();

	PersistableRecordSource getReferenceRecordSource();

	String getReferenceDatabaseConfiguration();

	boolean getTransitivityAnalysisFlag();

	String getTransitivityGraphProperty();

	AnalysisResultFormat getTransitivityResultFormat();

	/** Post-construction method */
	void initialize(OabaLinkageType type, CMPluginRegistry registry);

}
