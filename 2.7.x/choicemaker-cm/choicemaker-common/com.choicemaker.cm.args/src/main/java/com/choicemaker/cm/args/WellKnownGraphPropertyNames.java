package com.choicemaker.cm.args;

/**
 * These are names of GraphProperty plugins that are guaranteed to be present
 * in any installation of ChoiceMaker.
 */
public interface WellKnownGraphPropertyNames {
	
	/**
	 * Simply connected by MATCH relationships.
	 */
	String GPN_SCM = "SimplyConnectedMatches";

	/**
	 * Biconnected by MATCH relationships.
	 */
	String GPN_BCM = "BiconnectedMatches";

	/**
	 * Fully connected by MATCH relationships.
	 */
	String GPN_FCM = "FullyConnectedMatches";

}
