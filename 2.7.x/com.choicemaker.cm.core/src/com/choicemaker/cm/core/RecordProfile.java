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

/**
 * This object wraps a Record for findMatches.
 * 
 * 
 * @author pcheung
 *
 * ChoiceMaker Technologies, Inc.
 */
public class RecordProfile implements Profile {
	
	private Record record;
	
	public RecordProfile (Record r) {
		record = r;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.Profile#getRecord(com.choicemaker.cm.core.ProbabilityModel)
	 */
	public Record getRecord(IProbabilityModel model)
		throws InvalidProfileException {
		return record;
	}

}
