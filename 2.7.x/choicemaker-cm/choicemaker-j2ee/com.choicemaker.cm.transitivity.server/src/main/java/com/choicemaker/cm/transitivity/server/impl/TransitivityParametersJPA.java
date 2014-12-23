package com.choicemaker.cm.transitivity.server.impl;

import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersJPA;

public interface TransitivityParametersJPA extends OabaParametersJPA {

	/** Hides {@link OabaParametersJPA#DISCRIMINATOR_VALUE} */
	String DISCRIMINATOR_VALUE = "TRANSITIVITY";

	public static final String QN_TRANSPARAMS_FIND_ALL = "transParametersFindAll";

	/** JPQL used to implement {@link #QN_TRANSPARAMS_FIND_ALL} */
	String JPQL_TRANSPARAMS_FIND_ALL =
			"Select params from TransitivityParametersEntity params";

}
