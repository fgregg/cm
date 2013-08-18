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
package com.choicemaker.cm.io.blocking.automated.offline.server.data;

import java.io.Serializable;

/**
 * This is the data object that gets passed to the UpdateStatus message bean.
 * 
 * @author pcheung
 *
 */
public class UpdateData implements Serializable {

	/* As of 2010-03-10 */
	static final long serialVersionUID = -4267683410613907029L;

	public long jobID;
	public int percentComplete;
	public int OABAStatus;
}
