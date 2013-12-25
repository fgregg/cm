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
package com.choicemaker.cm.ml.me.xmlconf;

import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

import com.choicemaker.cm.core.Accessor;
import com.choicemaker.cm.core.ml.MachineLearner;
import com.choicemaker.cm.core.util.ArrayHelper;
import com.choicemaker.cm.core.xmlconf.MlModelConf;
import com.choicemaker.cm.ml.me.base.MaximumEntropy;

/**
 *
 * @author    
 * @version   $Revision: 1.2 $ $Date: 2010/03/24 23:19:35 $
 */
public class MeModelConf implements MlModelConf {
	private MaximumEntropy ml;

	public MeModelConf() {
	}

	public MeModelConf(MaximumEntropy ml) {
		this.ml = ml;
	}

	public MachineLearner readMachineLearner(Element e, Accessor acc, List clues, int[] oldClueNums) {
		MaximumEntropy me = new MaximumEntropy();
		me.setTrainingIterations(Integer.parseInt(e.getAttributeValue("trainingIterations")));
		float[] weights = ArrayHelper.getOneArray(acc.getClueSet().size());
		Iterator iClues = clues.iterator();
		int i = 0;
		while (iClues.hasNext()) {
			int clueNum = oldClueNums[i];
			Element cl = (Element) iClues.next();
			if (clueNum != -1) {
				weights[clueNum] = Float.parseFloat(cl.getAttributeValue("weight"));
			}
			++i;
		}
		me.setWeights(weights);
		return me;
	}

	public void saveMachineLearner(Element e) {
		e.setAttribute("trainingIterations", String.valueOf(ml.getTrainingIterations()));
	}

	public void saveClue(Element e, int clueNum) {
		e.setAttribute("weight", String.valueOf(ml.getWeights()[clueNum]));
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.xmlconf.MlModelConf#getExtensionPointId()
	 */
	public String getExtensionPointId() {
		return "com.choicemaker.cm.ml.me.base.me";
	}
}
