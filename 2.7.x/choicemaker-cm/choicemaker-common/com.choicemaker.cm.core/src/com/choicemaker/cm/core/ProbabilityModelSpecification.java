package com.choicemaker.cm.core;


public interface ProbabilityModelSpecification {

	String getWeightFileName();

	String getClueFileName();

	void setAntCommand(String v);

	MachineLearner getMachineLearner();

	void setUseAnt(boolean v);

	boolean isUseAnt();

	String getAntCommand();

}