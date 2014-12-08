package com.choicemaker.cm.args;

public interface UrmBatchParameters {

	OabaParameters getOabaParameters();
	
	OabaSettings getOabaSettings();
	
	TransitivityParameters getTransitivitySettings();
	
	ServerConfiguration getServerConfiguration();

}
