/*
 * Copyright (c) 2001, 2009 ChoiceMaker Technologies, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License
 * v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     ChoiceMaker Technologies, Inc. - initial API and implementation
 */
package com.choicemaker.cm.urm.base;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// BUG 2009-08-24 rphall
// This class isn't extensible, but it is reasonable to expect
// new types of GraphProperty will be needed. This is a perfect
// place for plugins. This class shouldn't be an enum.
//
// In essence, GraphProperty instances are names for
// transitivity analyses. A particular transitivity analysis should be a plug-in,
// and GraphProperty instances should be the registered names
// of these plugins. (But keep this class and its package decoupled
// from the transitivity package.)
/**
 * A type of the graph topology that can be used for identifing set of records 
 * connected by match or hold as linked record set.
 * <p>  
 *
 * @author emoussikaev
 * @version Revision: 2.5  Date: Nov 1, 2005 1:12:25 PM
 * @see
 */
public class GraphProperty implements Serializable {
	
	/* As of 2009-08-29 */
	static final long serialVersionUID = 5328204197234009120L;

	private String name;
	
	// BUG 2009-08-24 rphall
	// This public constructor  is incompatbile with the serialization method.
	// It allows a user to define an object that can't be deserialized.
	public GraphProperty(String value) {
		if (value == null || value.trim().length() == 0) {
			throw new IllegalArgumentException("null or blank value");
		}
		this.name = value;
	}

	// BUG 2009-08-24 rphall
	// Define some extensible method for enumerating registered GraphProperty types at runtime
	// (.e.g. based on plugin iterators of CompositeEntities.).
	public static final GraphProperty BCM_FCMH = new GraphProperty("BCM_FCMH");
	public static final GraphProperty BCM = new GraphProperty("BCM");
	public static final GraphProperty FCM = new GraphProperty("FCM");
	public static final GraphProperty CM = new GraphProperty("CM");
	
	public String getName() {
		return name;
	}

   public String toString() {
	   return getName();
   }

   public static GraphProperty valueOf(String name) {
	   name = name.intern();
	   if (BCM_FCMH.toString().intern() == name) {
		   return BCM_FCMH;
	   } else if (BCM.toString().intern() == name) {
		   return BCM;
	   } else if (FCM.toString().intern() == name) {
		   return FCM;
	   } else if (CM.toString().intern() == name) {
		   return CM;
	   } else {
		   throw new IllegalArgumentException(name + " is not a valid GraphProperty.");
	   }
   }

   private static int 	nextOrdinal = 0;
   private final int 	ordinal = nextOrdinal++;
   private static final GraphProperty[] gpVALUES = {BCM_FCMH,BCM,FCM,CM};

   public static List getValues() {
		// BUG 2009-08-24 rphall
		// Define some extensible method for enumerating registered GraphProperty types at runtime
		// (.e.g. based on plugins.)	
		List retVal = Collections.unmodifiableList(Arrays.asList(gpVALUES));
		return retVal;
   }
	
   // BUG 2009-08-24 rphall
   // This serialization method.is incompatible with the public constructor.
   // It can't deserialize user-defined GraphProperty instances.
   Object readResolve() throws ObjectStreamException {
	   return gpVALUES[ordinal];
   } 

}
