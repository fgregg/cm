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
package com.choicemaker.cm.core.configure.xml;

import java.net.URL;

/**
 * A composite of XmlConfigurable instances
 * @author rphall
 * @version 1.0.0
 * @since 2.5.206
 */
public interface XmlSpecification extends XmlConfigurable {

	/** XML element <code>children<code> */
	String ELEMENT_CHILDREN = "children"; //$NON-NLS-1$

	/**
	 * URL for a default schema used to convert XmlSpecification objects
	 * to and from XML Strings
	 */
	URL XML_SPECIFICATION_SCHEMA = null; // TODO FIXME

	/**
	 * The configurable objects owned by this instance.
	 * @return a non-null (but possibly empty) array of XmlConfigurable instances.
	 */
	XmlConfigurable[] getChildren();

	/**
	 * Writes a default XML representation like the following:<p>
	 * <pre>
	 * &lt;configurable isSerializable=&quot;true&quot; class=&quot;com.someorg.somepackage.SomeClassA&quot;&gt;
	 *   &lt;properties&gt;
	 *     &lt;property name=&quot;someProperty_1&quot; value=&quot;someValue_1&quot;/&gt;
	 *     &lt;property name=&quot;someProperty_2&quot; value=&quot;someValue_2&quot;/&gt;
	 *   &lt;/properties&gt;
	 *   &lt;children&gt;
	 *   &lt;configurable isSerializable=&quot;true&quot; class=&quot;com.someorg.somepackage.SomeClassB&quot;&gt;
	 *     &lt;properties&gt;
	 *       &lt;property name=&quot;someProperty_3&quot; value=&quot;someValue_3&quot;/&gt;
	 *       &lt;property name=&quot;someProperty_4&quot; value=&quot;someValue_4&quot;/&gt;
	 *     &lt;/properties&gt;
	 *   &lt;configurable isSerializable=&quot;true&quot; class=&quot;com.someorg.somepackage.SomeClassC&quot;&gt;
	 *     &lt;properties&gt;
	 *       &lt;property name=&quot;someProperty_5&quot; value=&quot;someValue_5&quot;/&gt;
	 *       &lt;property name=&quot;someProperty_6&quot; value=&quot;someValue_6&quot;/&gt;
	 *     &lt;/properties&gt;
	 *   &lt;/children&gt;
	 * &lt;/configurable&gt;
	 * </pre>
	 * @return
	 */
	String toXML();

	/**
	 * Adds an XmlConfigurable object to this specification.
	 */
	void add(XmlConfigurable child);

	// TODO FIXME define other list operations

}
