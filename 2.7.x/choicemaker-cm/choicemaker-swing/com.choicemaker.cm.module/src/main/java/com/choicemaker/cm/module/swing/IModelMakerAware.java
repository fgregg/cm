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
package com.choicemaker.cm.module.swing;



/**
 * An object that knows about ModelMaker. To reduce dependencies
 * between plugins, object references are used in place of type-safe
 * references to ModelMaker. It should be possible to replace this interface
 * with a type-safe one by refactoring ModelMaker (quite) a bit.
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/27 19:27:57 $
 */
public interface IModelMakerAware {

	Object getModelMakerObject();
	void setModelMakerObject(Object mm);	

}

