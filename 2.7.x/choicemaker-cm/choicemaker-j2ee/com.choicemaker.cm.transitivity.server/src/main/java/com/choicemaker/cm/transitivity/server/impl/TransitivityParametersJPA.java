package com.choicemaker.cm.transitivity.server.impl;

import com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersJPA;

public interface TransitivityParametersJPA extends AbstractParametersJPA {

	String DV_TRANS = "TRANS";

	/**
	 * Name of the query that finds all persistent Transitivity instances
	 */
	String QN_TRANSPARAMETERS_FIND_ALL = "transParametersFindAll";

	/** JPQL used to implement {@link #QN_TRANSPARAMETERS_FIND_ALL} */
	String JPQL_TRANSPARAMETERS_FIND_ALL =
		"Select p from TransitivityParametersEntity p";

}
