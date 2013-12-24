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

/**
 * Creates Configurable instances from XML documents.
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/24 18:04:31 $
 */
public interface XmlSpecificationParser {

	/**
	 * Recreates a Configurable object from an XML specification
	 * using the specified ClassLoader.
	 * @param classLoader a non-null ClassLoader
	 * @param xml non-null XML data conforming to
	 * {@link XmlSpecification.XML_CONFIGURABLE_SCHEMA}
	 * @return non-null
	 * @throws XmlSpecificationException if the xml document
	 * can not be parsed.
	 * @throws IncompleteSpecificationException if the xml
	 * document is missing required properties
	 * @throws InvalidPropertyNameException if the xml
	 * document contains an invalid property name
	 * @throws InvalidPropertyValueException if the xml
	 * document contains an invalid property value
	 */
	XmlConfigurable fromXML(ClassLoader classLoader, String xml)
		throws
			XmlSpecificationException,
			IncompleteSpecificationException,
			InvalidPropertyNameException,
			InvalidPropertyValueException;

	/**
	 * Recreates a Configurable object from a document
	 * using the specified ClassLoader.
	 * @param classLoader a non-null ClassLoader
	 * @param xml non-null XML data conforming to
	 * {@link XmlSpecification.XML_CONFIGURABLE_SCHEMA}
	 * @return non-null
	 * @throws XmlSpecificationException if the xml document
	 * can not be parsed.
	 * @throws IncompleteSpecificationException if the xml
	 * document is missing required properties
	 * @throws InvalidPropertyNameException if the xml
	 * document contains an invalid property name
	 * @throws InvalidPropertyValueException if the xml
	 * document contains an invalid property value
	 */
	XmlConfigurable fromXML(ClassLoader classLoader, IDocument document)
		throws
			XmlSpecificationException,
			IncompleteSpecificationException,
			InvalidPropertyNameException,
			InvalidPropertyValueException;

}
