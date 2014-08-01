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
package com.choicemaker.cm.core.gen;

/**
 * The interface of an extension that know how to generate a clue
 * for field in a ChoiceMaker record schema.
 * @author    rphall
 * @version   $Revision: 1.1 $ $Date: 2011/08/31 17:45:02 $
 */
public interface IClueGeneratorPlugin {
	void generate(IClueSetGenerator g) throws GenException;
}
