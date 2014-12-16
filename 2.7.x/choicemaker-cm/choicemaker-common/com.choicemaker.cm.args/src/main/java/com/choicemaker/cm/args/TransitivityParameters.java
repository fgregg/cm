package com.choicemaker.cm.args;

import static com.choicemaker.cm.args.WellKnownGraphPropertyNames.GPN_SCM;

public interface TransitivityParameters extends OabaParameters {

	String DEFAULT_EJB_REF_NAME = "ejb/transitivityParameters";
	String DEFAULT_JNDI_COMP_NAME = "java:comp/env/" + DEFAULT_EJB_REF_NAME;

	AnalysisResultFormat DEFAULT_RESULT_FORMAT =
		AnalysisResultFormat.SORT_BY_HOLD_GROUP;

	/** The name of the default graph property (simply connected by matches) */
	String DEFAULT_GRAPH_PROPERTY_NAME = GPN_SCM;

	AnalysisResultFormat getAnalysisResultFormat();

	IGraphProperty getGraphProperty();

}
