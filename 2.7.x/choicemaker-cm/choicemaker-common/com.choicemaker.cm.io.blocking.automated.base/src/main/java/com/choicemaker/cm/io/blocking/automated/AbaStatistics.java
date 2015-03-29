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
package com.choicemaker.cm.io.blocking.automated;

import java.io.IOException;

/**
 * Computes and sets the number of occurrences ('counts') of blocking values
 * for a particular blocking configuration.
 */
public interface AbaStatistics {
	long computeBlockingValueCounts(IBlockingConfiguration configuration,
			IBlockingValue[] blockingValues) throws IOException;
}
