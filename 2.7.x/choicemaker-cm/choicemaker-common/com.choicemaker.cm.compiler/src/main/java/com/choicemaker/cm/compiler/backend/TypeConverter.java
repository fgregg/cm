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
package com.choicemaker.cm.compiler.backend;

import com.choicemaker.cm.compiler.Type;
import com.choicemaker.cm.core.ClueSetType;
import com.choicemaker.cm.core.compiler.CompilerException;

/**
 * @author rphall
 */
class TypeConverter {
	
	private TypeConverter() {}

	static ClueSetType convert(Type t) throws CompilerException {
		ClueSetType retVal = null;
		if (t == Type.BOOLEAN) {
			retVal = ClueSetType.BOOLEAN;
		} else if (t == Type.INT) {
			retVal = ClueSetType.INT;
		} else {
			throw new CompilerException("Only boolean and int cluesets are supported.");
		}
		return retVal;
	}

	static Type convert(ClueSetType cst) throws CompilerException {
		Type retVal = null;
		if (cst == ClueSetType.BOOLEAN) {
			retVal = Type.BOOLEAN;
		} else if (cst == ClueSetType.INT) {
			retVal = Type.INT;
		} else {
			throw new CompilerException("Only boolean and int cluesets are supported.");
		}
		return retVal;
	}
	
} // TypeConverter

