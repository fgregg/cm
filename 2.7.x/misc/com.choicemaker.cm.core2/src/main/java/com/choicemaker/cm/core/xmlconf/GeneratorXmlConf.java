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

import java.io.File;

import org.jdom.Element;

/**
 * Description
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.1 $ $Date: 2010/01/20 15:05:01 $
 * @deprecated
 */
public class GeneratorXmlConf {

	public static String getCodeRoot() {
		String codeRoot = new File("etc/models/gen").getAbsolutePath();
		Element e = XmlConfigurator.getInstance().getCore();
		if (e != null) {
			e = e.getChild("generator");
			if (e != null) {
				String t = e.getAttributeValue("codeRoot");
				if (t != null) {
					codeRoot = new File(t).getAbsolutePath();
				}
			}
		}

		return codeRoot;
	}

}
