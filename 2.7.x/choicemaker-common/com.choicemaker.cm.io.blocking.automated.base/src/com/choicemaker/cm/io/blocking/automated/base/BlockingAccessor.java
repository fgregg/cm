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
package com.choicemaker.cm.io.blocking.automated.base;

/**
 *
 * @author    
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:47 $
 */
public interface BlockingAccessor {
	BlockingConfiguration getBlockingConfiguration(String name, String dbConf);
	
	String[] getBlockingConfigurations();
}
