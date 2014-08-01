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
package com.choicemaker.cm.core.util;



/**
 * The core set of messages used by ChoiceMaker. This class has only one
 * instance, <code>m</code>, and all of its functionality is defined by its
 * superclass, {@link MessageUtil}
 *
 * @author    rphall (refactored from MessageUtil)
 */
public class ChoiceMakerCoreMessages extends MessageUtil {
	
	public static final String RESOURCE_BUNDLE = "com.choicemaker.cm.core.util.res.ChoiceMaker";

	public static ChoiceMakerCoreMessages m = new ChoiceMakerCoreMessages(RESOURCE_BUNDLE);

	// TESTING ONLY
	ChoiceMakerCoreMessages(String name) {
		super(name);
	}

}
