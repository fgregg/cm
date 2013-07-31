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
package com.choicemaker.cm.core.configure.jdom;

import java.io.IOException;
import java.io.Reader;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.choicemaker.cm.core.configure.IBuilder;
import com.choicemaker.cm.core.configure.IDocument;
import com.choicemaker.cm.core.configure.XmlSpecificationException;
import com.choicemaker.cm.core.xmlconf.XmlParserFactory;

/**
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/24 18:04:30 $
 */
public class JdomBuilder implements IBuilder {

	private static Logger logger = Logger.getLogger(JdomBuilder.class);

	private final SAXBuilder builder;

	public JdomBuilder() {
		this.builder = XmlParserFactory.createSAXBuilder(false);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.configure.IBuilder#build(java.io.Reader)
	 */
	public IDocument build(Reader characterStream)
		throws XmlSpecificationException, IOException {
		IDocument retVal;
		try {
			Document document = this.getSAXBuilder().build(characterStream);
			retVal = new JdomDocument(document);
		} catch (JDOMException x) {
			String msg = "Unable to build document: " + x.toString();
			logger.error(msg, x);
			throw new XmlSpecificationException(msg, x);
		}
		return retVal;
	}

	public SAXBuilder getSAXBuilder() {
		return builder;
	}

}
