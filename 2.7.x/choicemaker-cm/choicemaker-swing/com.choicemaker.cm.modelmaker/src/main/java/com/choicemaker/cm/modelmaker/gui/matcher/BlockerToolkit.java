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
package com.choicemaker.cm.modelmaker.gui.matcher;

import com.choicemaker.cm.core.ImmutableProbabilityModel;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.2 $ $Date: 2010/03/29 12:39:59 $
 */
public interface BlockerToolkit {
	String toString();
	
	MatchDialogBlockerPlugin getDialogPlugin(ImmutableProbabilityModel model);
}
