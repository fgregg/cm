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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import com.choicemaker.cm.compiler.Tree.Apply;
import com.choicemaker.cm.compiler.Tree.ArrayType;
import com.choicemaker.cm.compiler.Tree.Bad;
import com.choicemaker.cm.compiler.Tree.Binop;
import com.choicemaker.cm.compiler.Tree.ClueDecl;
import com.choicemaker.cm.compiler.Tree.ClueSetDecl;
import com.choicemaker.cm.compiler.Tree.Ident;
import com.choicemaker.cm.compiler.Tree.If;
import com.choicemaker.cm.compiler.Tree.ImportDecl;
import com.choicemaker.cm.compiler.Tree.Index;
import com.choicemaker.cm.compiler.Tree.Indexed;
import com.choicemaker.cm.compiler.Tree.Let;
import com.choicemaker.cm.compiler.Tree.Literal;
import com.choicemaker.cm.compiler.Tree.MethodDecl;
import com.choicemaker.cm.compiler.Tree.New;
import com.choicemaker.cm.compiler.Tree.NewArray;
import com.choicemaker.cm.compiler.Tree.PackageDecl;
import com.choicemaker.cm.compiler.Tree.PrimitiveType;
import com.choicemaker.cm.compiler.Tree.Quantified;
import com.choicemaker.cm.compiler.Tree.Select;
import com.choicemaker.cm.compiler.Tree.Self;
import com.choicemaker.cm.compiler.Tree.Shorthand;
import com.choicemaker.cm.compiler.Tree.Typeop;
import com.choicemaker.cm.compiler.Tree.Unop;
import com.choicemaker.cm.compiler.Tree.Valid;
import com.choicemaker.cm.compiler.Tree.VarDecl;
import com.choicemaker.cm.compiler.Tree.Visitor;
import com.choicemaker.cm.core.compiler.CompilerException;
import com.choicemaker.cm.core.util.MessageUtil;

/**
 * Printing of ClueMaker code.
 * 
 * @author   Matthias Zenger
 * @author   Martin Buechi
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:35 $
 */
public class Printer implements Visitor, Tags {

	/** the output stream
	 */
	protected PrintWriter out;

	/** close this stream again?
	 */
	protected boolean closeStream;

	/** indentation width
	 */
	protected int width = 3;

	/** the current left margin
	 */
	protected int lmargin = 0;

	/** constructors
	 */
	public Printer() {
		out = new PrintWriter(System.out, true);
	}

	public Printer(PrintWriter out) {
		this.out = out;
	}

	public Printer(String filename) throws IOException {
		out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(new File(filename).getAbsoluteFile())));
		closeStream = true;
	}

	/** flush buffer
	 */
	public void flush() {
		out.flush();
	}

	/** align to lmargin
	 */
	protected Printer align() throws CompilerException {
		for (int i = 1; i < lmargin; i += 2)
			print("  ");
		if ((lmargin % 2) == 1)
			print(" ");
		return this;
	}

	/** indent left margin
	 */
	protected Printer indent() throws CompilerException {
		lmargin += width;
		return this;
	}

	/** reverse indentation
	 */
	protected Printer undent() {
		lmargin -= width;
		return this;
	}

	/** basic print methods
	 */
	public Printer print(Tree[] trees, String sep) throws CompilerException {
		if ((trees == null) || (trees.length == 0))
			return this;
		trees[0].apply(this);
		if (trees.length == 1)
			return this;
		for (int i = 1; i < trees.length; i++) {
			out.print(sep);
			trees[i].apply(this);
		}
		return this;
	}

	public Printer print(String[] trees, String sep) {
		if ((trees == null) || (trees.length == 0))
			return this;
		out.print(trees[0]);
		if (trees.length == 1)
			return this;
		for (int i = 1; i < trees.length; i++) {
			out.print(sep + trees[i]);
		}
		return this;
	}

	public Printer println(Tree[] trees, String sep) throws CompilerException {
		if ((trees == null) || (trees.length == 0))
			return this;
		align();
		trees[0].apply(this);
		out.println(sep);
		if (trees.length == 1)
			return this;
		for (int i = 1; i < trees.length; i++) {
			align();
			trees[i].apply(this);
			out.println(sep);
		}
		return this;
	}

	public Printer printExpr(Tree tree) throws CompilerException {
		switch (tree.tag) {
			case QUANTIFIED :
			case LET :
			case SHORTHAND :
			case VALID :
			case APPLY :
			case NEW :
			case INDEXED :
			case SELECT :
			case IDENT :
			case SELF :
			case ARRAYTYPE :
			case PRIMTYPE :
			case LITERAL :
				tree.apply(this);
				return this;
			default :
				out.print("(");
				tree.apply(this);
				out.print(")");
				return this;
		}
	}

	public Printer print(Tree tree) throws CompilerException {
		tree.apply(this);
		return this;
	}

	public Printer print(Object obj) throws CompilerException {
		out.print(obj);
		return this;
	}

	public Printer println(Tree tree) throws CompilerException {
		tree.apply(this);
		out.println();
		return this;
	}

	public Printer println(Object str) {
		out.println(str);
		return this;
	}

	public Printer println() {
		out.println();
		return this;
	}

	public void printProlog(String filename) {
		println(
			MessageUtil.m.formatMessage(
				"compiler.printer.generated",
				"$Revision: 1.1.1.1 $, $Date: 2009/05/03 16:02:35 $"));
		println(MessageUtil.m.formatMessage("compiler.printer.file", filename));
		println(MessageUtil.m.formatMessage("compiler.printer.date", new Date()));
		println();
	}

	public void printUnit(ICompilationUnit unit) throws CompilerException {
		printProlog(unit.getSource().toString());
		for (int i = 0; i < unit.getDecls().length; i++)
			println(unit.getDecls()[i]);
		out.close();
	}

	protected void printModifiers(int mods) throws CompilerException {
		if ((mods & Modifiers.PUBLIC) != 0)
			print("public ");
		if ((mods & Modifiers.PRIVATE) != 0)
			print("private ");
		if ((mods & Modifiers.PROTECTED) != 0)
			print("protected ");
		if ((mods & Modifiers.STATIC) != 0)
			print("static ");
		if ((mods & Modifiers.FINAL) != 0)
//			print("final ");
		if ((mods & Modifiers.SYNCHRONIZED) != 0)
			print("synchronized ");
		if ((mods & Modifiers.VOLATILE) != 0)
			print("volatile ");
		if ((mods & Modifiers.TRANSIENT) != 0)
			print("transient ");
		if ((mods & Modifiers.NATIVE) != 0)
			print("native ");
		if ((mods & Modifiers.ABSTRACT) != 0)
			print("abstract ");
		if ((mods & Modifiers.STRICTFP) != 0)
			print("strictfp ");
		if ((mods & Modifiers.REPORT) != 0)
			print("report ");
		if ((mods & Modifiers.NOTE) != 0)
			print("note ");
	}

	public void visit(Bad t) throws CompilerException {
		print("<bad>");
	}

	public void visit(PackageDecl t) throws CompilerException {
		print("package ");
		print(t.pckage);
		print(";");
	}

	public void visit(ImportDecl t) throws CompilerException {
		print("import ");
		print(t.pckage);
		print(t.starImport ? ".*;" : ";");
	}

	public void visit(ClueSetDecl t) throws CompilerException {
		println();
		println(
			"clueset " + t.name + " oftype " + t.type + (t.decision ? " decision " : "") + " uses " + t.uses + " {");
		indent();
		println(t.body, "");
		undent();
		align().print("}");
	}

	public void visit(ClueDecl t) throws CompilerException {
		printModifiers(t.clueModifiers);
		println((t.rule ? "rule " : "clue ") + t.name + " {");
		indent().align();
		switch (t.decision) {
			case DIFFER :
				print("differ ");
				break;
			case MATCH :
				print("match ");
				break;
			case HOLD :
				print("hold ");
				break;
			case NODIFFER :
				print("nodiffer ");
				break;
			case NOMATCH :
				print("nomatch ");
				break;
			case NOHOLD :
				print("nohold ");
				break;
			case NONEDEC :
				print("none ");
				break;
			case NODEC :
				break;
			default :
				throw new IllegalArgumentException("unknown decision " + t.decision);
		}
		if ((t.indices != null) && (t.indices.length > 0)) {
			print("foreach (");
			print(t.indices, ", ");
			println("; ");
			indent().align();
			println(t.expr);
			undent().align();
			println(");");
			undent().align();
		} else {
			indent();
			print(t.expr);
			println(";");
			undent().undent().align();
		}
		print("}");
	}

	public void visit(Index t) throws CompilerException {
		print(t.tpe);
		print(" " + t.name + " = ");
		print(t.initializer);
	}

	public void visit(MethodDecl t) throws CompilerException {
		print(t.restpe);
		print(" " + t.name + "(");
		print(t.params, ", ");
		print(")");
		if ((t.thrown != null) && (t.thrown.length > 0)) {
			print("throws ");
			print(t.thrown, ", ");
		}
		print(" {");
		print(t.body);
	}

	public void visit(VarDecl t) throws CompilerException {
		printModifiers(t.modifiers);
		print(t.tpe);
		print(" " + t.name);
		if (t.initializer != null) {
			print(" = ");
			print(t.initializer);
		}
	}

	public void visit(Quantified t) throws CompilerException {
		switch (t.quantifier) {
			case EXISTS :
				print("exists(");
				break;
			case ALL :
				print("all(");
				break;
			case COUNT :
				print("count(");
				break;
			case MINIMUM :
				print("minimum");
				break;
			case MAXIMUM :
				print("maximum");
				break;
			case COUNTUNIQUE :
				print("countunique");
				break;
			default :
				throw new IllegalArgumentException("illegal quantifier " + t.quantifier);
		}
		print(t.vars, ", ");
		print("; ");
		print(t.expr);
		if(t.valueExpr != null) {
			print("; ");
			print(t.valueExpr);
		}
		print(")");
	}

	public void visit(Let t) throws CompilerException {
		print("let(");
		print(t.binders, ", ");
		print("; ");
		print(t.expr);
		print(")");
	}

	public void visit(Shorthand t) throws CompilerException {
		switch (t.form) {
			case SAME :
				print("same(");
				break;
			case DIFFERENT :
				print("different(");
				break;
			case COMPARE :
				print("compare(");
				break;
			case AND_SHORTHAND:
				print("and");
				break;
			case OR_SHORTHAND:
				print("or");
				break;
			case XOR_SHORTHAND :
				print("xor");
				break;
			case SWAPSAME :
				print("swapsame");
				break;
			case SWAPDIFFERENT :
				print("swapdifferent");
				break;
			default :
				throw new IllegalArgumentException("illegal shorthand form " + t.form);
		}
		print(t.exprs, ", ");
		if(t.cond != null) {
			print("; ");
			print(t.cond);
		}
		print(")");
	}

	public void visit(Valid t) throws CompilerException {
		print("valid(");
		print(t.access);
		print(")");
	}

	public void visit(If t) throws CompilerException {
		printExpr(t.cond);
		print(" ?");
		printCast(t.thenp.type);
		print("(");
		printExpr(t.thenp);
		print(") : ");
		printCast(t.elsep.type);
		print("(");
		printExpr(t.elsep);
		print(")");
	}
	
	private void printCast(Type t) throws CompilerException {
		if(t != null && t != Type.NONE) {
			print("(");
			print(t);
			print(")");
		}
	}

	public void visit(Apply t) throws CompilerException {
		print(t.fun);
		print("(");
		print(t.args, ", ");
		print(")");
	}

	public void visit(New t) throws CompilerException {
		print("new ");
		print(t.clazz);
		print("(");
		print(t.args, ", ");
		print(")");
	}

	public void visit(NewArray t) throws CompilerException {
		if ((t.dims == null) && (t.clazz == null)) {
			println("{");
			indent();
			println(t.init, ", ");
			undent();
			align();
			print("}");
		} else if (t.dims == null) {
			print("new ");
			print(t.clazz);
			println("{");
			indent();
			println(t.init, ", ");
			undent();
			align();
			print("}");
		} else {
			int bs = 0;
			Tree clazz = t.clazz;
			while (clazz.tag == ARRAYTYPE) {
				clazz = ((ArrayType) clazz).tpe;
				bs++;
			}
			print("new ");
			print(clazz);
			for (int i = 0; i < t.dims.length; i++)
				print("[").print(t.dims[i]).print("]");
			for (int i = 0; i < bs; i++)
				print("[]");
		}
	}

	public void visit(Typeop t) throws CompilerException {
		switch (t.tag) {
			case TEST :
				printExpr(t.expr);
				print(" instanceof ");
				print(t.tpe);
				break;
			case CAST :
				print("(");
				print(t.tpe);
				print(")");
				printExpr(t.expr);
				break;
			default :
				throw new IllegalArgumentException("illegal type operator " + t.tag);
		}
	}

	public void visit(Unop t) throws CompilerException {
		switch (t.opcode) {
			case NOT :
				print("!");
				break;
			case COMP :
				print("~");
				break;
			case PLUS :
				print("+");
				break;
			case MINUS :
				print("-");
				break;
			default :
				throw new IllegalArgumentException("illegal unary operator " + t.opcode);
		}
		printExpr(t.arg);
	}

	public void visit(Binop t) throws CompilerException {
		printExpr(t.left);
		switch (t.opcode) {
			case MULT :
				print(" * ");
				break;
			case DIV :
				print(" / ");
				break;
			case MOD :
				print(" % ");
				break;
			case PLUS :
				print(" + ");
				break;
			case MINUS :
				print(" - ");
				break;
			case LSHIFT :
				print(" << ");
				break;
			case RSHIFT :
				print(" >> ");
				break;
			case URSHIFT :
				print(" >>> ");
				break;
			case LT :
				print(" < ");
				break;
			case GT :
				print(" > ");
				break;
			case LTEQ :
				print(" <= ");
				break;
			case GTEQ :
				print(" >= ");
				break;
			case EQEQ :
				print(" == ");
				break;
			case NOTEQ :
				print(" != ");
				break;
			case AND :
				print(" & ");
				break;
			case OR :
				print(" | ");
				break;
			case XOR :
				print(" ^ ");
				break;
			case ANDAND :
				print(" && ");
				break;
			case OROR :
				print(" || ");
				break;
			default :
				throw new IllegalArgumentException("illegal binary operator");
		}
		printExpr(t.right);
	}

	public void visit(Indexed t) throws CompilerException {
		printExpr(t.expr);
		print("[").print(t.index).print("]");
	}

	public void visit(Select t) throws CompilerException {
		printExpr(t.qualifier);
		print("." + t.name);
	}

	public void visit(Ident t) throws CompilerException {
		print(t.name);
	}

	public void visit(Self t) throws CompilerException {
		switch (t.stag) {
			case Q :
				print("q");
				break;
			case M :
				print("m");
				break;
			case R :
				print("r");
				break;
			default :
				throw new IllegalArgumentException("illegal self tag " + t.stag);
		}
	}

	public void visit(ArrayType t) throws CompilerException {
		print(t.tpe);
		print("[]");
	}

	public void visit(PrimitiveType t) throws CompilerException {
		switch (t.ttag) {
			case BYTE :
				print("byte");
				break;
			case SHORT :
				print("short");
				break;
			case INT :
				print("int");
				break;
			case LONG :
				print("long");
				break;
			case FLOAT :
				print("float");
				break;
			case DOUBLE :
				print("double");
				break;
			case CHAR :
				print("char");
				break;
			case BOOLEAN :
				print("boolean");
				break;
			case VOID :
				print("void");
				break;
			default :
				throw new IllegalArgumentException("illegal primitive type " + t.ttag);
		}
	}

	public void visit(Literal t) throws CompilerException {
		switch (t.ltag) {
			case LONG :
				print(t.value);
				print("L");
				break;
			case FLOAT :
				print(t.value);
				print("F");
				break;
			case CHAR :
				print("'");
				print(escapeChar((Character)t.value));
				print("'");
				break;
			case STRING :
				print("\"");
				print(escapeString((String)t.value));
				print("\"");
				break;
			default :
				print(t.value);
		}
	}
	
	private String escapeString(String s) {
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if(c == '\\' || c == '"') {
				b.append('\\');
			}
			b.append(c);
		}
		return b.toString();
	}
	
	private Object escapeChar(Character cc) {
		char c = cc.charValue();
		if(c == '\\' || c == '\'') {
			return "\\" + c;
		} else {
			return cc;
		}
	}
}
