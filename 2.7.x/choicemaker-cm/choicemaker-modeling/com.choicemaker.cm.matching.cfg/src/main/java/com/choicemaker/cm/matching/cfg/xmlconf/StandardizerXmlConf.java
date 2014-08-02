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
package com.choicemaker.cm.matching.cfg.xmlconf;

import java.util.List;

import org.jdom.Element;

import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.matching.cfg.ParseTreeNodeStandardizer;
import com.choicemaker.cm.matching.cfg.SymbolFactory;
import com.choicemaker.cm.matching.cfg.Variable;
import com.choicemaker.cm.matching.cfg.standardizer.RecursiveStandardizer;

/**
 * Comment
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:01 $
 */
public class StandardizerXmlConf {

	public static ParseTreeNodeStandardizer readFromElement(Element e, SymbolFactory factory) throws XmlConfException {
		Class cls = ParserXmlConf.getClass(e, RecursiveStandardizer.class);
		Class[] argTypes = ParserXmlConf.buildArgTypes(e);
		Object[] args = ParserXmlConf.buildArgs(e);

		ParseTreeNodeStandardizer standardizer = null;

		// first try to instantiate the standardizer with the symbol factory the the first argument...
		try {
			Class[] at = new Class[argTypes.length + 1];
			at[at.length-1] = SymbolFactory.class;
			Object[] a = new Object[at.length];
			a[a.length-1] = factory;

			standardizer = (ParseTreeNodeStandardizer) ParserXmlConf.instantiate(cls, at, a);
		} catch (XmlConfException ex) {
			assert standardizer == null;
		}

		// if that didn't work, then just try with the declared arguments...
		if (standardizer == null) {
			try {
				standardizer = (ParseTreeNodeStandardizer) ParserXmlConf.instantiate(cls, argTypes, args);
			} catch (XmlConfException ex) {
				ex.printStackTrace();
				throw new XmlConfException("Standardizer class " + cls +
					" has neither a zero-arg or a SymbolFactory-arg constructor.", ex);
			}
		}

		boolean isRecursive = RecursiveStandardizer.class.isAssignableFrom(cls);

		// children can be either property, method, or other standardizer elements.  Note that
		// a standardizer must be recursive in order to have standardizer children...
		List kids = e.getChildren();
		for (int i = 0; i < kids.size(); i++) {
			Element kid = (Element) kids.get(i);
			String kidName = kid.getName().intern();
			if (kidName == "property") {
				ParserXmlConf.setProperty(standardizer, kid);
			} else if (kidName == "method") {
				ParserXmlConf.invokeMethod(standardizer, kid);
			} else if (kidName == "standardizer") {
				if (isRecursive) {
					String vName = kid.getAttributeValue("variable");
					if (!factory.hasVariable(vName)) {
						throw new XmlConfException("Variable " + vName + " does not exist!");
					}

					Variable variable = factory.getVariable(vName);
					ParseTreeNodeStandardizer kidStandardizer = readFromElement(kid, factory);

					((RecursiveStandardizer)standardizer).putStandardizer(variable, kidStandardizer);
				} else {
					throw new XmlConfException("Standardizer must be recursive to have child standardizers.");
				}
			} else {
				throw new XmlConfException("Unknown child element of 'standardizer': " + kidName);
			}
		}

		return standardizer;
	}

}
