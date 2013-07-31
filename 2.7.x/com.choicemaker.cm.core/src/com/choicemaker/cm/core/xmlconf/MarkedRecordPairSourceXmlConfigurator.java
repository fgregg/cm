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

import com.choicemaker.cm.core.DynamicDispatchHandler;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.MarkedRecordPairSource;

/**
 * Base interface for all XML marked record pair configurators.
 *
 * Classes that implement this interface should be singletons. The single instance
 * should be accessible as a public (final) static field <code>instance</code>. This field
 * must be intialized when the class is loaded.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/24 21:10:43 $
 */
public interface MarkedRecordPairSourceXmlConfigurator extends DynamicDispatchHandler {
	/**
	 * Returns an instance of the description of the specified <code>MarkedRecordPairSource</code>.
	 *
	 * @param   e  The JDOM element containing the XML configuration information.
	 * @param   pmDesc  The description of the probability model to be used.
	 * @return  The description of the specified <code>MarkedRecordPairSource</code>.
	 * @throws  XmlConfException  if any error occurs.
	 */
	MarkedRecordPairSource getMarkedRecordPairSource(String fileName, Element e, IProbabilityModel model)
		throws XmlConfException;

	void add(MarkedRecordPairSource desc) throws XmlConfException;
}
