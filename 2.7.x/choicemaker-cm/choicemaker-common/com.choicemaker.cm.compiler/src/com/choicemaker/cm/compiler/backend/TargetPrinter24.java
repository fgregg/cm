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

import java.io.IOException;
import java.io.PrintWriter;

import com.choicemaker.cm.compiler.ICompilationUnit;
import com.choicemaker.cm.compiler.Printer;
import com.choicemaker.cm.compiler.Tree;
import com.choicemaker.cm.compiler.backend.TargetTree.Assign;
import com.choicemaker.cm.compiler.backend.TargetTree.Block;
import com.choicemaker.cm.compiler.backend.TargetTree.Break;
import com.choicemaker.cm.compiler.backend.TargetTree.Case;
import com.choicemaker.cm.compiler.backend.TargetTree.Catch;
import com.choicemaker.cm.compiler.backend.TargetTree.ClassDecl;
import com.choicemaker.cm.compiler.backend.TargetTree.Cond;
import com.choicemaker.cm.compiler.backend.TargetTree.Continue;
import com.choicemaker.cm.compiler.backend.TargetTree.DoWhile;
import com.choicemaker.cm.compiler.backend.TargetTree.For;
import com.choicemaker.cm.compiler.backend.TargetTree.JMethodDecl;
import com.choicemaker.cm.compiler.backend.TargetTree.Return;
import com.choicemaker.cm.compiler.backend.TargetTree.Switch;
import com.choicemaker.cm.compiler.backend.TargetTree.Taged;
import com.choicemaker.cm.compiler.backend.TargetTree.Try;
import com.choicemaker.cm.compiler.backend.TargetTree.Visitor;
import com.choicemaker.cm.compiler.backend.TargetTree.While;
import com.choicemaker.cm.core.compiler.CompilerException;

/**
 * Printing of Java source code.
 *
 * @author   Matthias Zenger
 * @author   Martin Buechi
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:36 $
 */
public class TargetPrinter24 extends Printer implements Visitor, TargetTags, ITargetPrinter {
	/** constructors
	 */
	public TargetPrinter24() {
		super();
	}

	public TargetPrinter24(PrintWriter out) {
		super(out);
	}

	public TargetPrinter24(String filename) throws IOException {
		super(filename);
	}

	public void printUnit(ICompilationUnit unit) throws CompilerException {
		printProlog(unit.getSource().toString());
		for (int i = 0; i < unit.getTarget().length; i++) {
			println(unit.getTarget()[i]);
		}
		out.close();
	}

	public void visit(ClassDecl t) throws CompilerException {
		printModifiers(t.modifiers);
		print("class " + t.name);
		if (t.superclass != null) {
			print(" extends ");
			print(t.superclass);
		}
		if ((t.interfaces != null) && (t.interfaces.length > 0)) {
			print(" implements ");
			print(t.interfaces, ", ");
		}
		println(" {");
		indent();
		println(t.body, ";");
		undent();
		align();
		print("}");
	}

	public void visit(JMethodDecl t) throws CompilerException {
		printModifiers(t.modifiers);
		if (t.restpe != null) {
			print(t.restpe);
			print(" ");
		}
		print(t.name + " (");
		print(t.params, ", ");
		if ((t.thrown != null) && (t.thrown.length > 0)) {
			print(") throws ");
			print(t.thrown, ", ");
		} else
			print(")");
		println(" {");
		indent();
		println(t.body, ";");
		undent();
		align();
		print("}");
	}

	public void visit(Block t) throws CompilerException {
		println("{");
		indent();
		println(t.stats, ";");
		undent();
		align();
		print("}");
	}

	public void visit(Cond t) throws CompilerException {
		print("if(");
		printExpr(t.cond);
		print(") ");
		indent();
		print(t.thenp);
		undent();
		if (t.elsep != null) {
			print(" else ");
			indent();
			print(t.elsep);
			undent();
		}
	}

	public void visit(Try t) throws CompilerException {
		println("try {");
		indent();
		if(t.body.isBlock()) {
			println(t.body);
		} else {
			print(t.body);
			println(";");
		}
		undent();
		print("}");
		println(t.catches, "");
	}

	public void visit(Catch t) throws CompilerException {
		println("catch (");
		print(t.ex);
		print(") ");
		println(" {");
		indent();
		print(t.body);
		print(";");
		undent();
		println("}");
	}

	public void visit(While t) throws CompilerException {
		print("while (");
		print(t.cond);
		print(") ");
		print(t.body);
	}

	public void visit(DoWhile t) throws CompilerException {
		print("do ");
		print(t.body);
		print(" while (");
		print(t.cond);
		print(")");
	}

	public void visit(For t) throws CompilerException {
		print("for (");
		print(t.inits, ", ");
		print("; ");
		print(t.cond);
		print("; ");
		print(t.increments, ", ");
		print(") ");
		print(t.body);
	}

	public void visit(Taged t) throws CompilerException {
		print(t.label + ": ");
		print(t.stat);
	}

	public void visit(Switch t) throws CompilerException {
		print("switch (");
		print(t.selector);
		print(") {");
		indent();
		println(t.cases, "");
		undent();
		align().print("}");
	}

	public void visit(Case t) throws CompilerException {
		for (int i = 0; i < t.guard.length; i++) {
			print("case ");
			print(t.guard[i]);
			println(": ");
			if (i < (t.guard.length - 1))
				align();
		}
		indent();
		println(t.body, ";");
		undent();
	}

	public void visit(Break t) throws CompilerException {
		print("break " + t.label);
	}

	public void visit(Continue t) throws CompilerException {
		print("continue " + t.label);
	}

	public void visit(Return t) throws CompilerException {
		print("return ");
		print(t.expr);
	}

	public void visit(Assign t) throws CompilerException {
		print(t.lhs);
		print(" = ");
		print(t.rhs);
	}

	public void visit(Tree.Self t) throws CompilerException {
		switch (t.stag) {
			case THIS :
				print("this");
				break;
			case SUPER :
				print("super");
				break;
			default :
				super.visit(t);
		}
	}
}
