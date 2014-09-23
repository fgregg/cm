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
package com.choicemaker.cm.modelmaker.gui.ml;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.choicemaker.cm.core.ChoiceMakerExtensionPoint;
import com.choicemaker.cm.core.MachineLearner;
import com.choicemaker.e2.CMConfigurationElement;
import com.choicemaker.e2.CMExtension;
import com.choicemaker.e2.CMExtensionPoint;
import com.choicemaker.e2.platform.CMPlatformUtils;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:09 $
 */
public class MlGuiFactories {
	public static final String EXTENSION_POINT = ChoiceMakerExtensionPoint.CM_MODELMAKER_MLTRAINGUIPLUGIN;
	
	private static Map guis = new HashMap();
	private static boolean initialized = false;
	
	public static Collection getAllGuis() {
		init();
		return guis.values();
	}
	
	public static MlGuiFactory getGui(MachineLearner learner) {
		init();
		return (MlGuiFactory) guis.get(learner.getClass().getName());
	}
	
	private static void init() {
		if (!initialized) {
			CMExtensionPoint pt = CMPlatformUtils.getExtensionPoint(EXTENSION_POINT);
			CMExtension[] extensions = pt.getExtensions();
			for (int i = 0; i < extensions.length; i++) {
				CMExtension extension = extensions[i];
				CMConfigurationElement[] elems = extension.getConfigurationElements();
				String handledClassName = elems[0].getAttribute("handledClass");
				MlGuiFactory mlgf = null;
				try {
					mlgf = (MlGuiFactory) elems[0].createExecutableExtension("class");
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				if (mlgf != null) {
					guis.put(handledClassName, mlgf);
				}
			}
		}
		initialized = true;
	}

}
