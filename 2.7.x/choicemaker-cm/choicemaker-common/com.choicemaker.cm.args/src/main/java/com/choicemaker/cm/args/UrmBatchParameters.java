package com.choicemaker.cm.args;

import java.io.Serializable;

public interface UrmBatchParameters extends Serializable {

	OabaParameters getOabaParameters();
	
	OabaSettings getOabaSettings();
	
	TransitivityParameters getTransitivitySettings();
	
	ServerConfiguration getServerConfiguration();

}
