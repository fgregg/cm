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
package com.choicemaker.cm.module.namedevent;

import java.util.EventObject;

import com.choicemaker.cm.module.IModuleController;
import com.choicemaker.cm.module.INamedEvent;



/**
 * Notification sent to a listener that an event has occured within
 * the module, such as a state change or the start of some
 * operation.
 * @author rphall
 */
public class NamedEvent extends EventObject implements INamedEvent {
	
	public final String DEFAULT_EVENT_NAME = "EV.DEFAULT_EVENT_NAME";
	
	public NamedEvent(IModuleController source) {
		super(source);
	}
	
	public String getName() {
		return DEFAULT_EVENT_NAME;
	}

}

