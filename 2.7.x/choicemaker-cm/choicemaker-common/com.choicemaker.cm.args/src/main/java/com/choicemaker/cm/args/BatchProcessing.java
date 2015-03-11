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
package com.choicemaker.cm.args;

/**
 * This interface defines event ids and completion estimates used in batch
 * processing.
 * 
 * @author pcheung (implemented as IStatus)
 * @author rphall (refactored from IStatus to BatchProcessing)
 */
public interface BatchProcessing {
	
	// -- Event names
	
	String NAME_INIT = "INIT";
	String NAME_DONE = "DONE";

	// -- Ordered event ids used by batch processing

	int EVT_INIT = 0;
	int EVT_DONE = 260;

	// -- Estimates of the completion status of a job, 0.0 - 1.00

	/** Minimum estimate of the amount of processing completed (inclusive) */
	public static final float MINIMUM_FRACTION_COMPLETE = 0.0f;

	/** Default estimate of the amount of processing completed */
	public static final float DEFAULT_FRACTION_COMPLETE =
		MINIMUM_FRACTION_COMPLETE;

	/** Maximum estimate of the amount of processing completed (inclusive) */
	public static final float MAXIMUM_FRACTION_COMPLETE = 1.0f;

	float PCT_INIT = MINIMUM_FRACTION_COMPLETE;
	float PCT_DONE = MAXIMUM_FRACTION_COMPLETE;

	// -- Other manifest constants

	char DELIMIT = ':';

}
