package com.choicemaker.cm.args;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * These are names of GraphProperty plugins that are guaranteed to be present in
 * any installation of ChoiceMaker.
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

	/** List of well known names */
	List<String> GPN_NAMES = Collections.unmodifiableList(Arrays
			.asList(new String[] {
					GPN_SCM, GPN_BCM, GPN_FCM }));

}
