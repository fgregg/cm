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
package com.choicemaker.cm.core.ml.none;

import java.util.List;

import org.jdom.Element;

import com.choicemaker.cm.core.Accessor;
import com.choicemaker.cm.core.ml.MachineLearner;
import com.choicemaker.cm.core.xmlconf.MlModelConf;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.1 $ $Date: 2010/01/20 15:05:05 $
 */
public class NoneFactory implements MlModelConf {
	public static NoneFactory instance = new NoneFactory();

	public NoneFactory() {
	}

	/**
	 * @see com.choicemaker.cm.xmlconf.MlModelConf#readMachineLearner(org.jdom.Element, com.choicemaker.cm.core.Accessor, java.util.List, int)
	 */
	public MachineLearner readMachineLearner(Element e, Accessor acc, List clues, int[] oldClueNums) {
		return new None();
	}

	/**
	 * @see com.choicemaker.cm.xmlconf.MlModelConf#saveClue(org.jdom.Element, int)
	 */
	public void saveClue(Element e, int clueNum) {
	}

	/**
	 * @see com.choicemaker.cm.xmlconf.MlModelConf#saveMachineLearner(org.jdom.Element)
	 */
	public void saveMachineLearner(Element e) {
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.xmlconf.MlModelConf#getExtensionPointId()
	 */
	public String getExtensionPointId() {
		return "com.choicemaker.cm.core.none";
	}
}
