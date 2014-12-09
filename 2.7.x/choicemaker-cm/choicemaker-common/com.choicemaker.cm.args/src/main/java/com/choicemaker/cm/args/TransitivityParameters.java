package com.choicemaker.cm.args;

public interface TransitivityParameters extends OabaParameters {
	
	String DEFAULT_EJB_REF_NAME = "ejb/transitivityParameters";
	String DEFAULT_JNDI_COMP_NAME = "java:comp/env/" + DEFAULT_EJB_REF_NAME;

	AnalysisResultFormat getAnalysisResultFormat();
	
	IGraphProperty getGraphProperty();

}
