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
package com.choicemaker.cm.core;

import com.choicemaker.cm.core.base.ActiveClues;

/**
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/24 17:00:57 $
 */
public interface IRecordPair extends ImmutableRecordPair {

	public abstract ActiveClues getActiveClues();
	public abstract void setActiveClues(ActiveClues af);
	public abstract void setQueryRecord(Record q);
	public abstract Record getQueryRecord();
	public abstract void setMatchRecord(Record m);
	public abstract Record getMatchRecord();
	public abstract void setCmDecision(Decision cmDecision);
	public abstract Decision getCmDecision();
	public abstract void setProbability(float probability);
	public abstract float getProbability();
}
