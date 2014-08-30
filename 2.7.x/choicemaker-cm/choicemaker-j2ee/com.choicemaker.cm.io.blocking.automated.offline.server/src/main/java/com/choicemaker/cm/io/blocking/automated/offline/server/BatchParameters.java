package com.choicemaker.cm.io.blocking.automated.offline.server;

import com.choicemaker.cm.core.SerialRecordSource;

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

	SerialRecordSource getStageRs();

	void setStageRs(SerialRecordSource stageRs);

	SerialRecordSource getMasterRs();

	void setMasterRs(SerialRecordSource masterRs);

}