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
package com.choicemaker.cm.module;

import java.util.Properties;


/**
 * @author rphall
 *
 */
public interface IPropertyControl extends IModuleController.IConfigurationModel {
	// -- Intrinsic (i.e. non-GUI) Properties and property listeners

	/** Get a copy of all currently set properties */
	Properties getProperties();

	/** Get a property value for this controller */
	String getProperty(String name);

	/**
	 * Get a property value for this controller, or return the specified default value
	 */
	String getProperty(String name, String strDefault);

	/** Set or change the specified property of this controller */
	void setProperty(String name, String value);

	/** Remove a property value of this controller */
	void removeProperty(String name);

	/** Add a listener to the intrinsic properties of this controller */
	void addPropertyListener(IPropertyListener l);

	/** Remove a listener to the intrinsic properties of this controller */
	void removePropertyListener(IPropertyListener l);

}
