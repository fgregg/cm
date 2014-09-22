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
package com.choicemaker.cm.core;

/**
 * Methods for getting and setting accessors from the ProbabilityModel class.
 * This interface facilitates testing by allowing stubs and mock objects to be
 * used instead of project-specific models.
 * 
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/24 17:12:11 $
 */
public interface AccessProvider {

	/**
	 * Sets the translator accessors used by a matching model.
	 * 
	 * @param newAcc
	 *            A dynamic proxy that implements translator-specific methods.
	 * @throws ModelConfigurationException
	 */
	public abstract void setAccessor(Accessor newAcc)
			throws ModelConfigurationException;

	/**
	 * Returns translator accessors used by a matching model.
	 * 
	 * @return A dynamic proxy that implements translator-specific methods.
	 */
	public abstract Accessor getAccessor();

	/**
	 * Returns the name of the dynamic-proxy class that implements translator
	 * accessors for a matching model.
	 *
	 * Note: this is not the same as getAccessor().getClass().getName() because
	 * getAccessor() returns a dynamic proxy, so the class name is something
	 * like $Proxy0.
	 * 
	 * @return The name of the dynamic-proxy class.
	 */
	public abstract String getAccessorClassName();

}
