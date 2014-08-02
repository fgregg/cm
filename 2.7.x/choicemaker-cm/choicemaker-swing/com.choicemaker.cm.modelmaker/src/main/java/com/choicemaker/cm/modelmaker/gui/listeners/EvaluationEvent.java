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
package com.choicemaker.cm.modelmaker.gui.listeners;

import java.util.EventObject;

/**
 *
 * @author  Martin Buechi
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:09 $
 */
public class EvaluationEvent extends EventObject {
	private static final long serialVersionUID = 1L;
	private boolean evaluated;
	public EvaluationEvent(Object source, boolean evaluated) {
		super(source);
		this.evaluated = evaluated;
	}
	public boolean isEvaluated() {
		return evaluated;
	}
}
