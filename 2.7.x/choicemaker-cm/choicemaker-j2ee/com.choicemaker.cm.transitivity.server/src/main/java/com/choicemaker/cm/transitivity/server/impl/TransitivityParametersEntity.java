package com.choicemaker.cm.transitivity.server.impl;

import com.choicemaker.cm.args.AnalysisResultFormat;
import com.choicemaker.cm.args.IGraphProperty;
import com.choicemaker.cm.args.OabaTaskType;
import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.args.TransitivityParameters;
import com.choicemaker.cm.core.SerializableRecordSource;

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
	public PersistableRecordSource getStageRs() {
		throw new Error("not yet implemented");
	}

	@Override
	public PersistableRecordSource getMasterRs() {
		throw new Error("not yet implemented");
	}

	@Override
	public OabaTaskType getOabaTaskType() {
		// TODO Auto-generated method stub
		throw new Error("not yet implemented");
	}

	@Override
	public AnalysisResultFormat getAnalysisResultFormat() {
		// TODO Auto-generated method stub
		throw new Error("not yet implemented");
	}

	@Override
	public IGraphProperty getGraphProperty() {
		// TODO Auto-generated method stub
		throw new Error("not yet implemented");
	}

	@Override
	public long getStageRsId() {
		// TODO Auto-generated method stub
		throw new Error("not yet implemented");
	}

	@Override
	public String getStageRsType() {
		// TODO Auto-generated method stub
		throw new Error("not yet implemented");
	}

	@Override
	public long getMasterRsId() {
		// TODO Auto-generated method stub
		throw new Error("not yet implemented");
	}

	@Override
	public String getMasterRsType() {
		// TODO Auto-generated method stub
		throw new Error("not yet implemented");
	}

}
