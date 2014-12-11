package com.choicemaker.demo.persist0;

import com.choicemaker.cm.core.ISerializableRecordSource;

public interface BatchParameters {

	// Inclusive
	float MIN_THRESHOLD = 0.0f;

	float MID_THRESHOLD = 0.5f;

	// Inclusive
	float MAX_THRESHOLD = 1.0f;

	long getId();

	String getStageModel();

	void setStageModel(String stageModel);

	String getMasterModel();

	void setMasterModel(String masterModel);

	int getMaxSingle();

	void setMaxSingle(int maxSingle);

	float getLowThreshold();

	void setLowThreshold(float lowThreshold);

	float getHighThreshold();

	void setHighThreshold(float highThreshold);

	ISerializableRecordSource getStageRs();

	void setStageRs(ISerializableRecordSource stageRs);

	ISerializableRecordSource getMasterRs();

	void setMasterRs(ISerializableRecordSource masterRs);

}