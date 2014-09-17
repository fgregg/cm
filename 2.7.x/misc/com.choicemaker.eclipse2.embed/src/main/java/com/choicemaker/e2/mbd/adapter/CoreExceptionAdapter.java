/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.choicemaker.e2.mbd.adapter;

import com.choicemaker.e2.mbd.core.runtime.CoreException;
import com.choicemaker.e2.mbd.core.runtime.IStatus;
import com.choicemaker.eclipse2.core.runtime.CMCoreException;
/**
 * A checked exception representing a failure.
 * <p>
 * Core exceptions contain a status object describing the 
 * cause of the exception.
 * </p>
 *
 * @see IStatus
 */
public class CoreExceptionAdapter {

	public static CMCoreException convert(CoreException x) {
		return new CMCoreException(StatusAdapter.convert(x.getStatus()));
	}
	
	public static CoreException convert(CMCoreException x) {
		return new CoreException(StatusAdapter.convert(x.getStatus()));
	}
	
	private CoreExceptionAdapter() {
	}

}
