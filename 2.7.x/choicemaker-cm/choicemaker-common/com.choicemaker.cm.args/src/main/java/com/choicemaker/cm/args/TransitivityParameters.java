package com.choicemaker.cm.args;

import static com.choicemaker.cm.args.WellKnownGraphPropertyNames.GPN_SCM;

public interface TransitivityParameters {

	String DEFAULT_EJB_REF_NAME = "ejb/transitivityParameters";
	String DEFAULT_JNDI_COMP_NAME = "java:comp/env/" + DEFAULT_EJB_REF_NAME;

	AnalysisResultFormat DEFAULT_RESULT_FORMAT =
		AnalysisResultFormat.SORT_BY_HOLD_GROUP;

	/** The name of the default graph property (simply connected by matches) */
	String DEFAULT_GRAPH_PROPERTY_NAME = GPN_SCM;

	long NONPERSISTENT_ID = 0;

	long getId();

	String getModelConfigurationName();

	OabaLinkageType getOabaLinkageType();

	float getLowThreshold();

	float getHighThreshold();

	/** The staging record source (and its id) is never null */
	long getStageRsId();

	/** The staging record source (and its type) is never null */
	String getStageRsType();

	/** The master record source (and its id) may be null */
	Long getMasterRsId();

	/** The master record source (and its type) may be null */
	String getMasterRsType();

	AnalysisResultFormat getAnalysisResultFormat();

	IGraphProperty getGraphProperty();

	OabaParameters asOabaParameters();

}
