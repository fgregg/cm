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
package com.choicemaker.cm.module;

import com.choicemaker.cm.module.IModule.IEventModel;

/**
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/27 19:27:56 $
 */
public interface INamedEventControl extends IEventModel {
	
	void addEventListener(INamedEventListener l);
	void removeEventListener(INamedEventListener l);

}

