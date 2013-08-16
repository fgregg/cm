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
package com.choicemaker.cm.io.blocking.automated.inmemory.gui;

import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.modelmaker.gui.matcher.BlockerToolkit;
import com.choicemaker.cm.modelmaker.gui.matcher.MatchDialogBlockerPlugin;

/*
 * Created on Jan 21, 2004
 *
 */

/**
 * @author ajwinkel
 *
 */
public class InMemoryAutomatedBlockerToolkit implements BlockerToolkit {

	public MatchDialogBlockerPlugin getDialogPlugin(IProbabilityModel model) {
		return new InMemoryAutomatedBlockerDialogPlugin(model);
	}

}
