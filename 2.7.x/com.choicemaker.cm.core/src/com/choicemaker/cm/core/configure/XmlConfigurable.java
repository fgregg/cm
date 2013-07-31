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

import java.io.Serializable;
import java.net.URL;

/**
 * An interface that indicates a type whose instances
 * may be completely specified by some set of required properties.
 * The interface also declares an operation for creating a
 * standardized XML representation of instances.
 * <p>
 * Implementations of this interface should consider subclassing
 * AbstractXmlSpecification because it provides a template
 * for some best practices concerning serialization and versioning.
 * @author rphall
 * @version 1.0.0
 * @since 2.5.206
 */
public interface XmlConfigurable
	extends Configurable, Serializable, Cloneable {

	/** XML attribute <code>class<code> */
	String ATTRIBUTE_CONFIGURABLE_CLASS = "class"; //$NON-NLS-1$

	/** XML attribute <code>isSerializable<code> */
	String ATTRIBUTE_CONFIGURABLE_SERIALIZABLE = "isSerializable"; //$NON-NLS-1$

	/** XML attribute serializable default is <code>true<code> */
	String ATTRIBUTE_CONFIGURABLE_SERIALIZABLE_DEFAULT = "true"; //$NON-NLS-1$

	/** XML attribute <code>name<code> */
	String ATTRIBUTE_PROPERTY_NAME = "name"; //$NON-NLS-1$

	/** XML attribute <code>value<code> */
	String ATTRIBUTE_PROPERTY_VALUE = "value"; //$NON-NLS-1$

	/** XML element <code>configurable<code> */
	String ELEMENT_CONFIGURABLE = "configurable"; //$NON-NLS-1$

	/** XML element <code>properties<code> */
	String ELEMENT_PROPERTIES = "properties"; //$NON-NLS-1$

	/** XML element <code>property<code> */
	String ELEMENT_PROPERTY = "property"; //$NON-NLS-1$

	/**
	 * URL for a default schema used to convert XmlConfigurable objects
	 * to and from XML Strings
	 */
	URL XML_CONFIGURABLE_SCHEMA = null; // TODO FIXME

	/**
	 * Writes a default XML representation like the following:<p>
	 * <pre>
	 * &lt;configurable isSerializable=&quot;true&quot; class=&quot;com.someorg.somepackage.SomeClass&quot;&gt;
	 *   &lt;properties&gt;
	 *     &lt;property name=&quot;someProperty_1&quot; value=&quot;someValue_1&quot;/&gt;
	 *     &lt;property name=&quot;someProperty_2&quot; value=&quot;someValue_2&quot;/&gt;
	 *   &lt;/properties&gt;
	 * &lt;/configurable&gt;
	 * </pre>
	 * @return
	 */
	String toXML();

}
