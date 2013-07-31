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
 * Represent an XML document, or a fragment of
 * a document, that has exactly one top-level
 * <code>configurable</code> element.
 * @see XmlConfigurable
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/24 18:04:31 $
 */
public interface IDocument {

	/**
	 * This will return the top-level <code>configurable</code>
	 * element for this <code>Document</code>. (By defiinition,
	 * a document has exactly one top-level configurable element.)
	 * @return <code>IElement</code> - the document's top-level
	 * configurable element
	 * @throws IllegalStateException if the root element hasn't been set
	 */
	public IElement getConfigurableElement();

}

