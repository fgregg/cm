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
package com.choicemaker.cm.core.configure;

import java.util.List;

import org.jdom.Element;

import com.choicemaker.cm.core.Accessor;
import com.choicemaker.cm.core.MachineLearner;
import com.choicemaker.cm.core.XmlConfException;

/**
 *
 * @author
 * @version   $Revision: 1.1 $ $Date: 2010/01/20 15:05:01 $
 */
public interface MachineLearnerPersistence {
	public MachineLearner readMachineLearner(Element e, Accessor acc, List clues, int[] oldClueNums) throws XmlConfException;

	public void saveMachineLearner(Element e) throws XmlConfException;

	public void saveClue(Element e, int clueNum);

	public String getExtensionPointId();
}
