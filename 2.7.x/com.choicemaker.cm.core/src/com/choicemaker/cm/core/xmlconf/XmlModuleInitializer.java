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
package com.choicemaker.cm.core.xmlconf;

import org.jdom.Element;

/**
 * Base interface for all XML module initializers.
 *
 * Classes that implement this interface should be singletons. The single instance
 * should be accessible as a public (final) static field <code>instance</code>. This field
 * must be intialized when the class is loaded.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.1 $ $Date: 2010/01/20 15:05:01 $
 */
public interface XmlModuleInitializer {
	/**
	 * Initializes the non-GUI parts of the module.
	 *
	 * @param   e  The JDOM element for the module from the configuration file.
	 * @throws  XmlConfException  if any error occurs during the configuration.
	 */
	void init(Element e) throws XmlConfException;
}
