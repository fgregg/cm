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

import com.choicemaker.cm.core.BlockingException;

/**
 * This is a sink into which we write the MergeGroup information.
 * 
 * @author pcheung
 *
 */
public interface IMergeGroupSink extends ISink {
	
	public void writeMergeGroup (IMergeGroup mg) throws BlockingException;

}
