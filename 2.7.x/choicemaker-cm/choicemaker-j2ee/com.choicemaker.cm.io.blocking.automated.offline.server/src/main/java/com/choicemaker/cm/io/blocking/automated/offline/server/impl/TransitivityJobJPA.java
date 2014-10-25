package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

public interface TransitivityJobJPA {

	// -- JPA constants
	
	/** Name of a query that finds all persistent transitivity jobs */
	public static final String QN_TRANSITIVITY_FIND_ALL =
			"transitivityFindAll";

	/** EQL statement used to find all persistent transitivity job instances */
	public static final String EQL_TRANSITIVITY_FIND_ALL =
			"Select job from TransitivityJobBean job";

	String DEFAULT_TABLE_DISCRIMINATOR = "TRANSITIVITY";

}
