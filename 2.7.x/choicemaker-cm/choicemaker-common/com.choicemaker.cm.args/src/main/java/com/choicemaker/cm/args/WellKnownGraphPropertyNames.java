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
	String GPN_SCM = "CM";

	/**
	 * Biconnected by MATCH relationships.
	 */
	String GPN_BCM = "BCM";

	/**
	 * Fully connected by MATCH relationships.
	 */
	String GPN_FCM = "FCM";
	
//	/**
//	 * Biconnected by MATCH relationships, fully connected by HOLD (or MATCH) relations.
//	 */
//	String GPN_BCM_FCHM = "BCM_FCHM";

	/** List of well known names */
	List<String> GPN_NAMES = Collections.unmodifiableList(Arrays
			.asList(new String[] {
					GPN_SCM, GPN_BCM, GPN_FCM }));

}
