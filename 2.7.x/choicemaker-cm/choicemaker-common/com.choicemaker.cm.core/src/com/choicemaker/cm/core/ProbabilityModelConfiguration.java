package com.choicemaker.cm.core;

public interface ProbabilityModelConfiguration {

	ImmutableProbabilityModel getProbabilityModel();
	
	String getDatabaseConfigurationName();
	
	String getBlockingConfigurationName();
	
}
