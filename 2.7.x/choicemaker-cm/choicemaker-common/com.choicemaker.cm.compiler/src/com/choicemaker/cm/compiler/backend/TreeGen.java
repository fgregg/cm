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

import com.choicemaker.cm.compiler.ICompilationUnit;
import com.choicemaker.cm.compiler.SemanticTags;
import com.choicemaker.cm.compiler.Tags;
import com.choicemaker.cm.compiler.Tree;
import com.choicemaker.cm.compiler.Tree.Apply;
import com.choicemaker.cm.compiler.Tree.Ident;
import com.choicemaker.cm.compiler.Tree.PrimitiveType;
import com.choicemaker.cm.compiler.Tree.Select;
import com.choicemaker.cm.compiler.Type;
import com.choicemaker.cm.compiler.Type.MethodType;
import com.choicemaker.cm.compiler.parser.DefaultVisitor;
import com.choicemaker.cm.compiler.typechecker.DeriveType;
import com.choicemaker.cm.core.compiler.CompilerException;


abstract class TreeGen extends DefaultVisitor implements Tags {
	
	private DeriveType dt;
	private String packageName;
	private String newPackageName;
	protected Type objectType;
	protected Type stringType;
	protected Type equalsType;
	
	public TreeGen(ICompilationUnit unit) throws CompilerException {
		dt = new DeriveType(unit);
		packageName = unit.getPackage().fullname();
		newPackageName = unit.getPackage().fullname() + ".internal." + unit.getSchemaName();
		objectType = dt.typeOf("java.lang.Object");
		stringType = dt.typeOf("java.lang.String");
		equalsType = new MethodType(new Type[]{objectType}, Type.BOOLEAN, new Type[0]);
	}
	
	protected Tree callEquals(Tree tree, Tree[] args) {
		Tree res = new Select(tree.pos, tree, "equals");
		res.type = equalsType;
		res = new Apply(tree.pos, res, args);
		res.type = Type.BOOLEAN;
		return res;
	}
	
	protected Tree qualid(int pos, String str) {
		int p = str.lastIndexOf('.');
		if (p >= 0)
			return new Select(
				pos,
				qualid(pos, str.substring(0, p)),
				str.substring(p+1, str.length()));
		else
			return new Ident(pos, str);
	}
	
	protected Tree typeToTree(int pos, Type type) {
		Tree res = null;
		switch (type.tag) {
			case SemanticTags.VOID:
				res = new PrimitiveType(pos, VOID);
				break;
			case SemanticTags.BYTE:
				res = new PrimitiveType(pos, BYTE);
				break;
			case SemanticTags.CHAR:
				res = new PrimitiveType(pos, CHAR);
				break;
			case SemanticTags.SHORT:
				res = new PrimitiveType(pos, SHORT);
				break;
			case SemanticTags.INT:
				res = new PrimitiveType(pos, INT);
				break;
			case SemanticTags.LONG:
				res = new PrimitiveType(pos, LONG);
				break;
			case SemanticTags.FLOAT:
				res = new PrimitiveType(pos, FLOAT);
				break;
			case SemanticTags.DOUBLE:
				res = new PrimitiveType(pos, DOUBLE);
				break;
			case SemanticTags.BOOLEAN:
				res = new PrimitiveType(pos, BOOLEAN);
				break;
			case SemanticTags.CLASS:
				// this is a hack, but there is a design flaw in ClueMaker which places
				// the generated classfiles into a different package than the declared
				// one
				String s = type.toString();
				if (s.startsWith(packageName)) {
					s = newPackageName + s.substring(packageName.length());
					//System.out.println(">>>> new name = " + s);
				}
				res = qualid(pos, s);
				break;
			case SemanticTags.ARRAY:
				res = new Tree.ArrayType(pos, typeToTree(pos, ((Type.ArrayType)type).elemtype));
				break;
			default:
				throw new Error(type.toString());
		}
		res.type = type;
		return res;
	}
}
