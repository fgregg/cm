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

import java.io.IOException;

/**
 * Sink of record pairs.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/24 20:56:31 $
 */
public interface RecordPairSink extends Sink {
	/**
	 * Stores a record to the sink.
	 *
	 * @param   r The record to be stored.
	 * @throws  IOException  if there is a problem retrieving the data.
	 */
	void put(ImmutableRecordPair r) throws IOException;
}
