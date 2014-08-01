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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.ExceptionTable;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import com.choicemaker.cm.compiler.Tree.ClueDecl;
import com.choicemaker.cm.compiler.Type.MethodType;
import com.choicemaker.cm.compiler.Type.ObjectType;
import com.choicemaker.cm.compiler.Type.PackageType;

/**
 * Symbols
 *
 * @author   Matthias Zenger   
 * @version  $Revision: 1.2 $ $Date: 2010/03/24 20:10:05 $
 */
public class Symbol implements SemanticTags {

	/** the absent symbol
	 */
	public static final Symbol NONE = new Symbol(0, "<none>", Type.NONE);
	
	public static final Symbol SIMPLE_SHORTHAND = new Symbol(0, "<none>", Type.NONE);

	/** the error symbol
	 */
	public static final Symbol BAD = new Symbol(SemanticTags.BAD, "<bad>", Type.ERROR);

	/** a symbol representing null
	 */
	public static final Symbol NULL = new Symbol(TYP, "null", Type.NULL);

	/** the symbol kind
	 */
	private int kind;

	/** the name of the symbol
	 */
	private String name;

	/** the type of the symbol
	 */
	private Type type;

	/** the modifiers
	 */
	private int modifiers;

	/** the owner of the symbol
	 */
	private Symbol owner;

	public Symbol(int kind, String name, Type type, int mods, Symbol owner) {
		setKind(kind);
		setName(name);
		setType(type);
		setModifiers(mods);
		setOwner(owner);
	}

	private Symbol(int kind, String name, Type type) {
		this(kind, name, type, 0, null);
	}

	public int modifiers() {
		return modifiers;
	}

	public String fullname() {
		return getName();
	}

	public ClassSymbol superclass() {
		throw new UnsupportedOperationException(this +" does not have a superclass");
	}

	public ClassSymbol[] interfaces() {
		return new ClassSymbol[0];
	}

	public Scope members() {
		throw new UnsupportedOperationException(this +" does not have a scope");
	}

	public ClassSymbol schema() {
		throw new UnsupportedOperationException(this +" is not a clue set");
	}

	public boolean equals(Object o) {
		boolean b = false;
		if (o != null)
			if (o instanceof Symbol) {
				Symbol s = (Symbol) o;
				b = (s.name.equals(this.name) && s.kind == this.kind);
			}
		return b;
	}

	public int hashCode() {
		return getName().hashCode();
	}

	public String toString() {
		return getName();
	}

	private static String createFullName(String name, Symbol owner) {
		return ((owner == NONE) || (owner.fullname().length() == 0)) ? name : (owner.fullname() + "." + name);
	}

	public Symbol innermostPackage() {
		Symbol s = this;
		while (s.getKind() != PCK) {
			if (s == Symbol.NONE)
				return s;
			s = s.getOwner();
		}
		return s;
	}

	public Symbol innermostClass() {
		Symbol s = this;
		while ((s.getKind() != TYP) && (s.getKind() != SET)) {
			if (s == Symbol.NONE)
				return s;
			s = s.getOwner();
		}
		return s;
	}

	void setKind(int kind) {
		this.kind = kind;
	}

	public int getKind() {
		return kind;
	}

	void setModifiers(int modifiers) {
		this.modifiers = modifiers;
	}

	public int getModifiers() {
		return modifiers;
	}

	void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	void setOwner(Symbol owner) {
		this.owner = owner;
	}

	public Symbol getOwner() {
		return owner;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	/** a representation for clue set symbols
	 */
	public static class ClueSetSymbol extends Symbol {
		String fullname;
		Scope scope;
		ClassSymbol schema;

		public ClueSetSymbol(String name, Symbol owner, ClassSymbol schema) {
			super(SET, name, null, Modifiers.PUBLIC, owner);
			this.fullname = createFullName(name, owner);
			this.scope = new Scope(this);
			this.schema = schema;
		}

		public String fullname() {
			return fullname;
		}

		public Scope members() {
			return scope;
		}

		public ClassSymbol schema() {
			return schema;
		}
	}

	/** a representation for clue symbols
	 */
	public static class ClueSymbol extends Symbol {
		public ClueDecl decl;

		public ClueSymbol(String name, Symbol owner, ClueDecl decl) {
			super(CLU, name, Type.BOOLEAN, Modifiers.PUBLIC, owner);
			this.decl = decl;
		}
	}

	/** a representation for method symbols
	 */
	public static class MethodSymbol extends Symbol {
		public MethodSymbol(String name, Type type, int modifiers, Symbol owner) {
			super(MTH, name, type, modifiers, owner);
		}
	}

	/** a representation for variable/field symbols
	 */
	public static class VarSymbol extends Symbol {
		/** the range for variables of quantifiers
		 */
		public Tree range;
		//        public int max = -1;
		public boolean definedInsideSimpleShorthand;

		public VarSymbol(String name, Type type, int modifiers, Symbol owner) {
			super(VAR, name, type, modifiers, owner);
		}
		
		public VarSymbol(String name, Type type, int modifiers, Symbol owner, boolean definedInsideSimpleShorthand) {
			this(name, type, modifiers, owner);
			this.definedInsideSimpleShorthand = definedInsideSimpleShorthand;
		}

	}

	/** a representation for class symbols; we use this class also
	 *  for schemas (holder classes)
	 */
	public static class ClassSymbol extends Symbol {
		/** the full class name
		 */
		String fullname;

		/** the superclass of this class
		 */
		ClassSymbol superclass;

		/** the implemented interfaces
		 */
		ClassSymbol[] interfaces;

		/** the class members; if scope == null, then the
		 *  class did not get loaded already
		 */
		Scope scope;

		/** the class repository that created this class symbol
		 */
		ClassRepository repository;

		public ClassSymbol(String name, Symbol owner, ClassRepository rep) {
			super(TYP, name, null, 0, owner);
			this.fullname = createFullName(name, owner);
			setType(new ObjectType(this));
			this.repository = rep;
		}

		public void init(int modifiers, ClassSymbol superclass, ClassSymbol[] interfaces) {
			setModifiers(modifiers);
			this.superclass = superclass;
			this.interfaces = interfaces;
			this.scope = new Scope(this);
		}

		public void load() {
			if (scope != null)
				return;
			scope = new Scope(this);
			try {
				// load class
				long start = System.currentTimeMillis();
				FileWrapper file = repository.find(filename());
				if (file != null) {
					InputStream is = file.getInputStream();
					// parse class
					JavaClass clazz = new ClassParser(is, file.getPath()).parse();
					// update class symbol
					setModifiers(clazz.getAccessFlags());
					superclass =
						(this == repository.objectClass) ? null : repository.defineClass(clazz.getSuperclassName());
					interfaces = repository.defineClasses(clazz.getInterfaceNames());
					// enter fields
					SignatureParser sig = new SignatureParser(repository);
					Field[] fields = clazz.getFields();
					for (int i = 0; i < fields.length; i++)
						scope.enter(
							new VarSymbol(
								fields[i].getName().intern(),
								sig.parse(fields[i].getSignature()),
								fields[i].getAccessFlags(),
								this));
					// enter methods
					Method[] methods = clazz.getMethods();
					for (int i = 0; i < methods.length; i++) {
						// create method type
						MethodType tpe = (MethodType) sig.parse(methods[i].getSignature());
						// now lookup thrown exceptions in the method attributes
						Attribute[] attribs = methods[i].getAttributes();
						for (int j = 0; j < attribs.length; j++)
							if (attribs[j] instanceof ExceptionTable) {
								tpe.thrown =
									repository.defineClassTypes(((ExceptionTable) attribs[j]).getExceptionNames());
								break;
							}
						// enter method symbol
						scope.enter(
							new MethodSymbol(methods[i].getName().intern(), tpe, methods[i].getAccessFlags(), this));
					}
					is.close();
					repository.env.message(
						"[class " + fullname + " loaded in " + (System.currentTimeMillis() - start) + "ms]");
				} else {
					repository.env.error("class " + fullname + " could not be loaded");
				}
			} catch (IOException e) {
				superclass = repository.objectClass;
				interfaces = new ClassSymbol[0];
				repository.env.error("class " + fullname + " could not be loaded");
			}
		}

		public String filename() {
			return fullname.replace('.', File.separatorChar) + ".class";
		}

		public String fullname() {
			return fullname;
		}

		public int modifiers() {
			if (scope == null)
				load();
			return getModifiers();
		}

		public ClassSymbol superclass() {
			if (scope == null)
				load();
			return superclass;
		}

		public ClassSymbol[] interfaces() {
			if (scope == null)
				load();
			return interfaces;
		}

		public Scope members() {
			if (scope == null)
				load();
			return scope;
		}
	}

	public static class PackageSymbol extends Symbol {
		/** the full package name
		 */
		String fullname;

		/** the package members; if scope == null, then the
		 *  package did not get loaded already
		 */
		Scope scope;

		/** the class repository
		 */
		ClassRepository repository;

		/** does this package exist?
		 */
		boolean exists;

		public PackageSymbol(String name, Symbol owner, ClassRepository rep) {
			super(PCK, name, null, 0, owner);
			this.fullname = createFullName(name, owner);
			this.repository = rep;
			setType(new PackageType(this));
		}

		/** load the directory
		 */
		public void load() {
			if (scope == null) {
				if (!exists()) {
					if (!repository.env.sourcePackages.contains(fullname)) {
						repository.env.error("package " + fullname + " could not be loaded");
					}
				}
				//					repository.env.warning("package " + fullname + " could not be loaded");
				//				else
				//					repository.env.message("[package " + fullname + " contains " + scope.size() + " classes]");
			}
		}

		/** return the filename
		 */
		public String filename() {
			if ((fullname == null) || (fullname.length() == 0))
				return "./";
			else {
				String res = fullname.replace('.', File.separatorChar);
				if (!res.endsWith("/"))
					res += "/";
				return res;
			}
		}

		public String fullname() {
			return fullname;
		}

		/** read directory of a classpath directory and include classes
		 *  in package scope
		 */
		protected void enterClasses(FileWrapper dir) {
			if (dir == null)
				return;
			String[] content = null;
			try {
				content = dir.list();
			} catch (IOException e) {
			}
			if (content != null)
				for (int i = 0; i < content.length; i++)
					if (content[i].endsWith(".class")) {
						scope.enterIfNew(
							repository.defineClass(fullname(content[i].substring(0, content[i].length() - 6))));
					}
		}

		/** form a full name out of the package and a classname
		 */
		public String fullname(String name) {
			if ((fullname == null) || fullname.equals(""))
				return name;
			else
				return fullname + "." + name;
		}

		public Scope members() {
			if (scope == null)
				load();
			return scope;
		}

		public boolean exists() {
			if (scope != null)
				return exists;
			scope = new Scope(this);
			String dirname = filename();
			String[] root = repository.env.classPath.getComponents();
			for (int i = 0; i < root.length; i++) {
				FileWrapper pck = repository.open(root[i], dirname);
				exists = exists || (pck != null);
				enterClasses(pck);
			}
			return exists;
		}
	}
}
