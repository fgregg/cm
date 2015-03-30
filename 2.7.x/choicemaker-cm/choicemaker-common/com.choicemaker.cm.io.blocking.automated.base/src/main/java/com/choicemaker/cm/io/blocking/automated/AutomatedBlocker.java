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

import java.util.List;

import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordSource;

/**
 * Automatically blocks and returns candidate match records from a database
 * record source.
 * 
 * @author rphall
 */
public interface AutomatedBlocker extends RecordSource {

	List<IBlockingSet> getBlockingSets();

	int getNumberOfRecordsRetrieved();

	IBlockingConfiguration getBlockingConfiguration();

	DatabaseAccessor getDatabaseAccessor();

	AbaStatistics getCountSource();

	Record getQueryRecord();

	int getLimitPerBlockingSet();

	int getSingleTableBlockingSetGraceLimit();

	int getLimitSingleBlockingSet();
}
