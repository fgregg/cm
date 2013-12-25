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
package com.choicemaker.cm.io.blocking.automated.offline.core;

/**
 * This determines if a block is valid or if a comparison pair is valid.
 * 
 * @author pcheung
 *
 */
public interface IValidator extends IValidatorBase{
	
	/** This returns true if this is a valid pair for comparison. */
	public boolean validPair (long id1, long id2);
	
	
	/** This returns true if id1 is for a staging record.
	 * 
	 * @param id1
	 * @return boolean - true if this id is from staging.
	 */
	public boolean isStaging (long id1);

}
