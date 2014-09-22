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
package com.choicemaker.e2.std;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

import com.choicemaker.e2.E2Exception;
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

	public static E2Exception convert(CoreException x) {
		return new E2Exception(StatusAdapter.convert(x.getStatus()));
	}
	
	public static CoreException convert(E2Exception x) {
		return new CoreException(StatusAdapter.convert(x.getStatus()));
	}
	
	private CoreExceptionAdapter() {
	}

}
