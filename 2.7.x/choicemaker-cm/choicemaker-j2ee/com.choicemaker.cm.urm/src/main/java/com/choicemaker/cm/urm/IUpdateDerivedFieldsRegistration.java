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
package com.choicemaker.cm.urm;

/**
 * Defines the extension point for updateDerivedFields.
 * 
 * @see com.choicemaker.cm.core.configure.XmlConfigurablesRegistry
 * @author rphall
 * @version 1.0.0
 * @since 2.5.206
 */
public interface IUpdateDerivedFieldsRegistration {

	/**
	 * The extension point,
	 * <code>com.choicemaker.cm.urm.updateDerivedFields</code>
	 */
	public static final String UPDATE_DERIVED_FIELD_EXTENSION_POINT =
	// Avoid dependence on com.choicemaker.cm.core package
	// ChoiceMakerExtensionPoint.CM_URM_UPDATEDERIVEDFIELDS;
		"com.choicemaker.cm.urm.updateDerivedFields";

}
