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
package com.choicemaker.e2.mbd;

import com.choicemaker.e2.CMPlatformRunnable;
import com.choicemaker.e2.mbd.boot.IPlatformRunnable;

/**
 * Bootstrap type for the platform. Platform runnables represent executable 
 * entry points into plug-ins.  Runnables can be configured into the Platform's
 * <code>org.eclipse.core.runtime.applications</code> extension-point 
 * or be made available through code or extensions on other plug-in's extension-points.
 *
 * <p>
 * Clients may implement this interface.
 * </p>
 */
public class PlatformRunnableAdapter implements CMPlatformRunnable {

	private final IPlatformRunnable delegate;
	
	public PlatformRunnableAdapter(IPlatformRunnable cmpr) {
		if (cmpr == null) {
			throw new IllegalArgumentException("null delegate");
		}
		this.delegate = cmpr;
	}
	
/**
 * Runs this runnable with the given args and returns a result.
 * The content of the args is unchecked and should conform to the expectations of
 * the runnable being invoked.  Typically this is a <code>String<code> array.
 * 
 * @exception Exception if there is a problem running this runnable.
 */
public Object run(Object args) throws Exception {
	return this.delegate.run(args);
}

}
