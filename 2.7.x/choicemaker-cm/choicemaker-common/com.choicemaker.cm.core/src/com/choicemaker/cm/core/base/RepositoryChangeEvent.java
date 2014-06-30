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

import java.util.EventObject;


/**
 *
 * @author  Martin Buechi
 * @version $Revision: 1.1 $ $Date: 2010/01/20 15:05:04 $
 */
public class RepositoryChangeEvent extends EventObject {
	private static final long serialVersionUID = 1L;
	public static final int SET_CHANGED = 0;
	public static final int RECORD_DATA_CHANGED = 1;
	public static final int MARKUP_DATA_CHANGED = 2;

	private int id;
	private Object target;
	private Decision oldDecision;
	private Decision newDecision;

	public RepositoryChangeEvent(Object src) {
		super(src);
		id = SET_CHANGED;
	}

	public RepositoryChangeEvent(Object src, Object target) {
		super(src);
		this.target = target;
		id = RECORD_DATA_CHANGED;
	}

	public RepositoryChangeEvent(Object src, Object target, Decision oldDecision, Decision newDecision) {
		super(src);
		this.target = target;
		id = MARKUP_DATA_CHANGED;
		this.oldDecision = oldDecision;
		this.newDecision = newDecision;
	}

	public int getID() {
		return id;
	}

	public Decision getOldDecision() {
		return oldDecision;
	}

	public Decision getNewDecision() {
		return newDecision;
	}
	/**
	 * Returns the target.
	 * @return Object
	 */
	public Object getTarget() {
		return target;
	}

}
