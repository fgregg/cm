package com.choicemaker.cm.transitivity.server.impl;

import com.choicemaker.cm.core.SerializableRecordSource;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityParameters;

public class TransitivityParametersEntity implements TransitivityParameters {

	public static boolean isPersistent(TransitivityParameters params) {
		throw new Error("not yet implemented");
	}

	public TransitivityParametersEntity() {
		throw new Error("not yet implemented");
	}

	public TransitivityParametersEntity(TransitivityParameters transParams) {
		throw new Error("not yet implemented");
	}

	public TransitivityParametersEntity(
			String createRandomModelConfigurationName, float differThreshold,
			float matchThreshold, SerializableRecordSource stage,
			SerializableRecordSource master) {
		throw new Error("not yet implemented");
	}

	@Override
	public long getId() {
		throw new Error("not yet implemented");
	}

	@Override
	public String getModelConfigurationName() {
		throw new Error("not yet implemented");
	}

	@Override
	public String getStageModel() {
		throw new Error("not yet implemented");
	}

	@Override
	public String getMasterModel() {
		throw new Error("not yet implemented");
	}

	@Override
	public float getLowThreshold() {
		throw new Error("not yet implemented");
	}

	@Override
	public float getHighThreshold() {
		throw new Error("not yet implemented");
	}

	@Override
	public SerializableRecordSource getStageRs() {
		throw new Error("not yet implemented");
	}

	@Override
	public SerializableRecordSource getMasterRs() {
		throw new Error("not yet implemented");
	}

}
