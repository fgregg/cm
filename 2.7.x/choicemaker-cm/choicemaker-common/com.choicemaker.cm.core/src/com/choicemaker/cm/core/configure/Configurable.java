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
package com.choicemaker.cm.core.configure;

import java.util.Properties;

/**
 * An object that can be configured via properties.
 * @author rphall
 */
public interface Configurable {

	/**
	 * Checks if the specified value is allowed for the
	 * specified property name.
	 */
	boolean isAllowedPropertyValue(String propertyName, String propertyValue);

	/**
	 * Returns the set property names allowed by this type.
	 * Since it makes no sense for a Configurable type to
	 * support <em>no</em> properties, an empty return
	 * value has a special meaning. It indicates that this
	 * type does not check property names when either the
	 * {@link #setProperty(String,String) setProperty} or the
	 * {@link #setProperties(Properties) setProperties} method
	 * is invoked.
	 * @return a non-null, but possibly empty array of Strings.
	 * An empty array indicates this type does not check
	 * whether a property name is allowed when a property value
	 * is set.
	 */
	String[] getAllowedPropertyNames();

	/**
	 * Returns the minimum subset of allowed property names that is
	 * required to completely specify an instance of this type.
	 * <p>
	 * <strong>Implementation note:</strong>
	 * If no properties are required (i.e. the array returned by
	 * this method is blank), then consider whether two instances
	 * of this type should always test equal. See the <code>java.lang</code>
	 * package documentation for what this implies about the
	 * implementation of the <code>equals</code>,
	 * <code>hashCode</code> and <code>compareTo</code>
	 * methods.
	 * @return a non-null (but possibly empty) array of non-null,
	 * non-blank and trimmed property names. The returned array
	 * should be a logical subset of getAllowedPropertyNames().
	 * If getAllowedPropertyNames() returns a empty array, then
	 * this method is free to return any array of names; otherwise
	 * this method should return an array whose elvery element
	 * is an element of getAllowedPropertyNames().
	 */
	String[] getRequiredPropertyNames();

	/** Returns all the properties specified by this object */
	Properties getProperties();

	/**
	 * Sets some properties for this object
	 * @param p a non-null collection of properties
	 * @throws InvalidPropertyNameException if the specified
	 * property name is not allowed for this object.
	 * @throws InvalidPropertyValueException if the specified
	 * property value is not allowed for this object.
	 */
	void setProperties(Properties p)
		throws InvalidPropertyNameException, InvalidPropertyValueException;

	/**
	 * Resets all the properties for this object
	 * @throws InvalidPropertyNameException if the specified
	 * property name is not allowed for this object.
	 * @throws InvalidPropertyValueException if the specified
	 * property value is not allowed for this object.
	 * @throws IncompleteSpecificationException if some required
	 * property is not specified for this object
	 */
	void setAllProperties(Properties p)
		throws
			IncompleteSpecificationException,
			InvalidPropertyNameException,
			InvalidPropertyValueException;

	/**
	 * Gets a particular property of this object.
	 * @param propertyName a non-null, non-blank String
	 * @return possibly null if no such propertyName exists
	 */
	String getProperty(String propertyName);

	/**
	 * Sets a particular property of this object
	 * @param propertyName a non-null, non-blank String
	 * @param propertyValue a non-null (but possibly blank) String
	 * @throws InvalidPropertyNameException if the specified
	 * property name is not allowed for this object.
	 * @throws InvalidPropertyValueException if the specified
	 * property value is not allowed for this object.
	 */
	void setProperty(String propertyName, String propertyValue)
		throws InvalidPropertyNameException, InvalidPropertyValueException;

	/**
	 * Removes a property from this object. If no such
	 * property exists, this method has no effect.
	 * @param propertyName a non-null, non-blank String
	 * @throws IncompleteSpecificationException if the specified
	 * property is required to be set for this object.
	 */
	void removeProperty(String propertyName)
		throws IncompleteSpecificationException;

}
