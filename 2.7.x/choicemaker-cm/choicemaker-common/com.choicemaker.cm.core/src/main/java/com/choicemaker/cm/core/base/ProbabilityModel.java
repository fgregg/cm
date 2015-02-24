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
package com.choicemaker.cm.core.base;

import java.util.Date;

import com.choicemaker.cm.core.Accessor;
import com.choicemaker.cm.core.MachineLearner;
import com.choicemaker.cm.core.ModelConfigurationException;

/**
 * A probability model consisting of holder classes, translators, a clue set,
 * weights, and a list of clues to be evaluated.
 * 
 * Class invariant: clueSet != null <=> cluesToEval != null AND
 * cluesToEval.length == clueSet.size() AND weights != null AND weights.length
 * == clueSet.size
 * 
 * This class is both a probability model as well as the collection of all
 * configured probability models.
 * 
 * @author Martin Buechi
 * @author S. Yoakum-Stover
 * @author rphall (Split this class into separate instance and manager types)
 * @version $Revision: 1.3 $ $Date: 2010/10/17 13:32:57 $
 * @deprecated use ImmutableProbabilityModel, IProbabilityModel, or
 *             MutableProbabilityModel. Note this interface must remain
 *             unchanged for the MCI/CDSS CORBA implementation
 */
public class ProbabilityModel extends MutableProbabilityModel {

	public ProbabilityModel() {
		super();
	}

	public ProbabilityModel(String fileName, String rawClueFileName) {
		super(fileName, rawClueFileName);
	}

	public ProbabilityModel(String fileName, String rawClueFileName,
			Accessor acc, MachineLearner ml, boolean[] cluesToEvaluate,
			String trainingSource, boolean trainedWithHolds,
			Date lastTrainingDate, boolean /* useAnt */ignored1,
			String /* antCommand */ignored2) throws IllegalArgumentException,
			ModelConfigurationException {
		super(fileName, rawClueFileName, acc, ml, cluesToEvaluate,
				trainingSource, trainedWithHolds, lastTrainingDate);
	}

}
