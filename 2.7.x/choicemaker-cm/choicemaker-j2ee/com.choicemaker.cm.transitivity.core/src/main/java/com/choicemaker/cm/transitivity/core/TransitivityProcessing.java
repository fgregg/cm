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
package com.choicemaker.cm.transitivity.core;

import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;

/**
 * This interface defines event ids and completion estimates used in transitivity
 * processing.
 */
public interface TransitivityProcessing extends OabaProcessing {

	// -- Ordered events

	int EVT_DONE_TRANS_DEDUP_OVERSIZED = EVT_DONE_DEDUP_OVERSIZED;

	// -- Completion estimates

	float PCT_DONE_TRANS_DEDUP_OVERSIZED = PCT_DONE_DEDUP_OVERSIZED;

}
