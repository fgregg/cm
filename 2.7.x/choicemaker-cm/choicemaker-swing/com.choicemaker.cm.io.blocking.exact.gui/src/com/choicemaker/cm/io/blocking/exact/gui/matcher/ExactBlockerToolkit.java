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
package com.choicemaker.cm.io.blocking.exact.gui.matcher;

import com.choicemaker.cm.core.base.IProbabilityModel;
import com.choicemaker.cm.modelmaker.gui.matcher.BlockerToolkit;
import com.choicemaker.cm.modelmaker.gui.matcher.MatchDialogBlockerPlugin;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.2 $ $Date: 2010/03/28 15:39:27 $
 */
public class ExactBlockerToolkit implements BlockerToolkit {
	/**
	 * @see com.choicemaker.cm.train.matcher.BlockerToolkit#getDialogPlugin(com.choicemaker.cm.core.base.ProbabilityModel)
	 */
	public MatchDialogBlockerPlugin getDialogPlugin(IProbabilityModel model) {
		return new ExactBlockerDialogPlugin(model);
	}

	public String toString() {
		return "Exact Blocker";
	}
}
