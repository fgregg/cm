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
package com.choicemaker.cm.compiler;

import java.util.Vector;

import com.choicemaker.cm.compiler.Type.ArrayType;
import com.choicemaker.cm.compiler.Type.MethodType;

/**
 * Parsing of bytecode signatures
 *
 * @author   Matthias Zenger
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:35 $
 * @see      BCEL
 */
public class SignatureParser {

	protected ClassRepository repository;
	protected String signature;
	protected int offset;

	public SignatureParser(String signature, ClassRepository rep) {
		this.repository = rep;
		this.signature = signature;
	}

	public SignatureParser(ClassRepository rep) {
		this.repository = rep;
	}

	public Type parse(String signature) {
		this.signature = signature;
		this.offset = 0;
		return parse();
	}

	public Type parse() {
		switch (signature.charAt(offset)) {
			case 'B' :
				offset++;
				return Type.BYTE;
			case 'C' :
				offset++;
				return Type.CHAR;
			case 'D' :
				offset++;
				return Type.DOUBLE;
			case 'F' :
				offset++;
				return Type.FLOAT;
			case 'I' :
				offset++;
				return Type.INT;
			case 'J' :
				offset++;
				return Type.LONG;
			case 'L' :
				int start = ++offset;
				while (signature.charAt(offset) != ';')
					offset++;
				return repository.defineClass(signature.substring(start, offset++).replace('/', '.')).getType();
			case 'S' :
				offset++;
				return Type.SHORT;
			case 'V' :
				offset++;
				return Type.VOID;
			case 'Z' :
				offset++;
				return Type.BOOLEAN;
			case '[' :
				offset++;
				return new ArrayType(parse());
			case '(' :
				offset++;
				return new MethodType(parseArgs(), parse(), Type.EMPTY);
			default :
				throw new RuntimeException("bad signature: " + signature);
		}
	}

	protected Type[] parseArgs() {
		Vector args = new Vector();
		while (signature.charAt(offset) != ')')
			args.add(parse());
		offset++;
		return (Type[]) args.toArray(new Type[args.size()]);
	}
}
