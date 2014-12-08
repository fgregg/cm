/*
 * GroupMatchType.java       Revision: 2.5  Date: Sep 9, 2005 2:53:25 PM 
 *
 * Copyright (c) 2001 ChoiceMaker Technologies, Inc.
 * 48 Wall Street, 11th Floor, New York, NY 10005
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * ChoiceMaker Technologies Inc. ("Confidential Information").
 */
package com.choicemaker.cm.urm.config;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * A type of the graph topology that can be used for identifying set of records
 * connected by match or hold as linked record set.
 * <p>
 *
 * @author emoussikaev
 * @version Revision: 2.5 Date: Nov 1, 2005 1:12:25 PM
 */
public class GraphProperty implements Serializable, IGraphProperty {

	private static final long serialVersionUID = 271L;

	public static final IGraphProperty BCM_FCMH = new GraphProperty("BCM_FCMH");
	public static final IGraphProperty BCM = new GraphProperty("BCM");
	public static final IGraphProperty FCM = new GraphProperty("FCM");

	private static int nextOrdinal = 0;
	private final int ordinal = nextOrdinal++;
	private static final IGraphProperty[] VALUES = {
			FCM, BCM, BCM_FCMH };

	public static IGraphProperty valueOf(String name) {
		name = name.intern();
		if (BCM_FCMH.getName().intern() == name) {
			return BCM_FCMH;
		} else if (BCM.getName().intern() == name) {
			return BCM;
		} else if (FCM.getName().intern() == name) {
			return FCM;
		} else {
			throw new IllegalArgumentException(name
					+ " is not a valid GraphProperty.");
		}
	}

	private final String name;

	private GraphProperty(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return getName();
	}

	Object readResolve() throws ObjectStreamException {
		return VALUES[ordinal];
	}

}
