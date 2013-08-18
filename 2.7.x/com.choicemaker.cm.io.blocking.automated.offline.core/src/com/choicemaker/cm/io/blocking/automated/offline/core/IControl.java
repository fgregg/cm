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

import java.rmi.RemoteException;

/**
 * This interface tells OABA service objects if a loop should be interupted.
 * 
 * @author pcheung
 *
 */
public interface IControl {
	
	/** This method returns true if the long running loop should be stopped.
	 * 
	 * @return boolean
	 */
	public boolean shouldStop () throws RemoteException;

}
