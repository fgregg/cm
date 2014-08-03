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
package com.choicemaker.cm.urm.ejb;

import com.choicemaker.cm.core.configure.eclipse.EclipseRegistries;
import com.choicemaker.cm.core.configure.eclipse.EclipseRegistry;
import com.choicemaker.cm.urm.IUpdateDerivedFieldsRegistration;

/**
 * This class defines an instance of an eclipse-based registry
 * for derived-field updators.
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/25 00:17:22 $
 */
public class DefaultUpdateDerivedFieldsRegistry
	extends EclipseRegistry
	implements IUpdateDerivedFieldsRegistration {

	public static EclipseRegistry getInstance() {
		return EclipseRegistries.getInstance(
			UPDATE_DERIVED_FIELD_EXTENSION_POINT);
	}

	// This constructor is never used
	private DefaultUpdateDerivedFieldsRegistry() {
		super(UPDATE_DERIVED_FIELD_EXTENSION_POINT);
	}

}
