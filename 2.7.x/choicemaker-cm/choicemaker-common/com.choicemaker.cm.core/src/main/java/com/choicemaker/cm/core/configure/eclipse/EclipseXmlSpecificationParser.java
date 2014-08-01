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
package com.choicemaker.cm.core.configure.eclipse;

import com.choicemaker.cm.core.configure.xml.AbstractXmlSpecificationParser;
import com.choicemaker.cm.core.configure.xml.IBuilder;

/**
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/24 18:04:31 $
 */
public class EclipseXmlSpecificationParser
	extends AbstractXmlSpecificationParser {

	/**
	 * Returns an instance of <code>EclipseBuilder</code>
	 * @see EclipseBuilder
	 * @see com.choicemaker.cm.core.base.configure.AbstractXmlSpecificationParser#getBuilder()
	 */
	public IBuilder getBuilder() {
		return new EclipseBuilder();
	}

}
