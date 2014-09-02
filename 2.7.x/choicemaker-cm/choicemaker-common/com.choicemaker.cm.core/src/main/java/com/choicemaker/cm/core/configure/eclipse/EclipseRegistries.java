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
package com.choicemaker.cm.core.configure.eclipse;

import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;

import com.choicemaker.util.Precondition;

/**
 * A collection of eclipse-based registries.
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/24 18:04:31 $
 */
public class EclipseRegistries {

	private static final Logger logger =
		Logger.getLogger(EclipseRegistries.class.getName());

	private static final Object _mapSynch = new Object();
	private static Map _instances = new Hashtable();

	public static EclipseRegistry getInstance(String uniqueExtensionPointId) {
		Precondition.assertNonNullArgument(
			"null unique extension point ID",
			uniqueExtensionPointId);
		EclipseRegistry retVal =
			(EclipseRegistry) _instances.get(
				uniqueExtensionPointId);
		if (retVal == null) {
			synchronized (_mapSynch) {
				retVal =
					(EclipseRegistry) _instances.get(
						uniqueExtensionPointId);
				if (retVal == null) {
					retVal =
						new EclipseRegistry(uniqueExtensionPointId);
					_instances.put(uniqueExtensionPointId, retVal);
					String msg =
						"Added Eclipse-based registry for extension point '"
							+ uniqueExtensionPointId
							+ "'";
					logger.fine(msg);
				}
			}
		}
		return retVal;
	}

	private EclipseRegistries() {
	}
	
}
