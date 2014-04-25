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


/**
 * Types
 *
 * @author   Matthias Zenger
 * @version  $Revision: 1.2 $ $Date: 2010/03/24 20:09:46 $
 */
public class Type implements SemanticTags {

	/** no type
	 */
	public static final Type NONE = new Type(SemanticTags.NONE);

	/** no types
	 */
	public static final Type[] EMPTY = new Type[0];

	/** every type
	 */
	public static final Type ANY = new Type(SemanticTags.ALL);

	/** the error type
	 */
	public static final Type ERROR = new Type(SemanticTags.ERROR);

	/** the primitive type void
	 */
	public static final Type VOID = new Type(SemanticTags.VOID);

	/** the primitive type byte
	 */
	public static final Type BYTE = new Type(SemanticTags.BYTE);

	/** the primitive type char
	 */
	public static final Type CHAR = new Type(SemanticTags.CHAR);

	/** the primitive type short
	 */
	public static final Type SHORT = new Type(SemanticTags.SHORT);

	/** the primitive type int
	 */
	public static final Type INT = new Type(SemanticTags.INT);

	/** the primitive type long
	 */
	public static final Type LONG = new Type(SemanticTags.LONG);

	/** the primitive type float
	 */
	public static final Type FLOAT = new Type(SemanticTags.FLOAT);

	/** the primitive type double
	 */
	public static final Type DOUBLE = new Type(SemanticTags.DOUBLE);

	/** the primitive type boolean
	 */
	public static final Type BOOLEAN = new Type(SemanticTags.BOOLEAN);

	/** the type of null
	 */
	public static final Type NULL = new Type(SemanticTags.NULL);

	/** the tag of this type
	 */
	public int tag;

	private Type(int tag) {
		this.tag = tag;
	}

	public String toString() {
		switch (tag) {
			case SemanticTags.NONE :
				return "<none>";
			case SemanticTags.ALL :
				return "<any>";
			case SemanticTags.ERROR :
				return "<error>";
			case SemanticTags.VOID :
				return "void";
			case SemanticTags.BYTE :
				return "byte";
			case SemanticTags.CHAR :
				return "char";
			case SemanticTags.SHORT :
				return "short";
			case SemanticTags.INT :
				return "int";
			case SemanticTags.LONG :
				return "long";
			case SemanticTags.FLOAT :
				return "float";
			case SemanticTags.DOUBLE :
				return "double";
			case SemanticTags.BOOLEAN :
				return "boolean";
			case SemanticTags.NULL :
				return "null";
			default :
				throw new IllegalArgumentException("bad type tag " + tag);
		}
	}

	public String getObjectType() {
		switch (tag) {
			case SemanticTags.BYTE :
				return "Byte";
			case SemanticTags.CHAR :
				return "Char";
			case SemanticTags.SHORT :
				return "Short";
			case SemanticTags.INT :
				return "Integer";
			case SemanticTags.LONG :
				return "Long";
			case SemanticTags.FLOAT :
				return "Float";
			case SemanticTags.DOUBLE :
				return "Double";
			case SemanticTags.BOOLEAN :
				return "Boolean";
			default :
				throw new IllegalArgumentException("bad type tag " + tag);
		}
	}

	public String getConstant() {
		switch (tag) {
			case SemanticTags.NONE :
				return "NONE";
			case SemanticTags.ALL :
				return "ANY";
			case SemanticTags.ERROR :
				return "ERROR";
			case SemanticTags.VOID :
				return "VOID";
			case SemanticTags.BYTE :
				return "BYTE";
			case SemanticTags.CHAR :
				return "CHAR";
			case SemanticTags.SHORT :
				return "SHORT";
			case SemanticTags.INT :
				return "INT";
			case SemanticTags.LONG :
				return "LONG";
			case SemanticTags.FLOAT :
				return "FLOAT";
			case SemanticTags.DOUBLE :
				return "DOUBLE";
			case SemanticTags.BOOLEAN :
				return "BOOLEAN";
			case SemanticTags.NULL :
				return "NULL";
			default :
				throw new IllegalArgumentException("bad type tag " + tag);
		}
	}

	public Symbol sym() {
		return Symbol.NONE;
	}

	public Type elemtype() {
		if (this == ERROR)
			return this;
		throw new UnsupportedOperationException(this +" does not have an element type");
	}

	public Type[] argtypes() {
		if (this == ERROR)
			return EMPTY;
		throw new UnsupportedOperationException(this +" does not have argument types");
	}

	public Type restype() {
		if (this == ERROR)
			return this;
		throw new UnsupportedOperationException(this +" does not have a result type");
	}

	public Type[] thrown() {
		if (this == ERROR)
			return EMPTY;
		throw new UnsupportedOperationException(this +" does not have thrown exceptions");
	}

	public Type supertype() {
		if (this == ERROR)
			return this;
		throw new UnsupportedOperationException(this +" does not have a supertype");
	}

	public Type[] implemented() {
		if (this == ERROR)
			return EMPTY;
		throw new UnsupportedOperationException(this +" does not have implemented interfaces");
	}

	public boolean isPrim() {
		switch (tag) {
			case SemanticTags.NONE :
			case SemanticTags.NULL :
			case SemanticTags.VOID :
				return false;
			case SemanticTags.ERROR :
			case SemanticTags.ALL :
			case SemanticTags.BYTE :
			case SemanticTags.CHAR :
			case SemanticTags.SHORT :
			case SemanticTags.INT :
			case SemanticTags.LONG :
			case SemanticTags.FLOAT :
			case SemanticTags.DOUBLE :
			case SemanticTags.BOOLEAN :
				return true;
			default :
				throw new IllegalArgumentException("bad type tag");
		}
	}

	public boolean isRef() {
		switch (tag) {
			case SemanticTags.ERROR :
			case SemanticTags.NULL :
			case SemanticTags.ALL :
				return true;
			case SemanticTags.NONE :
			case SemanticTags.BYTE :
			case SemanticTags.CHAR :
			case SemanticTags.SHORT :
			case SemanticTags.INT :
			case SemanticTags.LONG :
			case SemanticTags.FLOAT :
			case SemanticTags.DOUBLE :
			case SemanticTags.BOOLEAN :
				return false;
			default :
				throw new IllegalArgumentException("bad type tag");
		}
	}

	public boolean isMethod() {
		return tag == SemanticTags.ERROR;
	}

	public boolean isArray() {
		return tag == SemanticTags.ERROR;
	}

	public boolean isObject() {
		return tag == SemanticTags.ERROR;
	}

	public boolean isPackage() {
		return tag == SemanticTags.ERROR;
	}

	public boolean equals(Object that) {
		if (!(that instanceof Type) || (this.tag == ALL) || (((Type) that).tag == ALL))
			return false;
		if ((this.tag == SemanticTags.ERROR) || (((Type) that).tag == SemanticTags.ERROR))
			return true;
		return this == that;
	}

	public int hashCode() {
		return tag;
	}

	public boolean subtype(Type that) {
		switch (tag) {
			case SemanticTags.ALL :
				return false;
			case SemanticTags.ERROR :
				return true;
			case SemanticTags.NULL :
				return that.isRef();
		}
		switch (that.tag) {
			case SemanticTags.ERROR :
			case SemanticTags.ALL :
				return true;
			case SemanticTags.NULL :
			case SemanticTags.BYTE :
				return this == that;
			case SemanticTags.CHAR :
				return (this == that) || (tag == SemanticTags.BYTE);
			case SemanticTags.SHORT :
				return (this == that) || (tag == SemanticTags.BYTE);
			case SemanticTags.INT :
				return (this == that)
					|| (tag == SemanticTags.BYTE)
					|| (tag == SemanticTags.SHORT)
					|| (tag == SemanticTags.CHAR);
			case SemanticTags.LONG :
				return (this == that)
					|| (tag == SemanticTags.BYTE)
					|| (tag == SemanticTags.SHORT)
					|| (tag == SemanticTags.CHAR)
					|| (tag == SemanticTags.INT);
			case SemanticTags.FLOAT :
				return (this == that)
					|| (tag == SemanticTags.BYTE)
					|| (tag == SemanticTags.SHORT)
					|| (tag == SemanticTags.CHAR)
					|| (tag == SemanticTags.INT)
					|| (tag == SemanticTags.LONG);
			case SemanticTags.DOUBLE :
				return (this == that)
					|| (tag == SemanticTags.FLOAT)
					|| (tag == SemanticTags.BYTE)
					|| (tag == SemanticTags.SHORT)
					|| (tag == SemanticTags.CHAR)
					|| (tag == SemanticTags.INT)
					|| (tag == SemanticTags.LONG);
			case SemanticTags.BOOLEAN :
			case SemanticTags.NONE :
				return (this == that);
			default :
				return false;
		}
	}

	public static boolean equals(Type[] thiz, Type[] that) {
		if (thiz == that)
			return true;
		if ((thiz == null) || (that == null) || (thiz.length != that.length))
			return false;
		for (int i = 0; i < thiz.length; i++)
			if (!thiz[i].equals(that[i]))
				return false;
		return true;
	}

	public static boolean subtypes(Type[] thiz, Type[] that) {
		if (thiz == that)
			return true;
		if ((thiz == null) || (that == null) || (thiz.length != that.length))
			return false;
		for (int i = 0; i < thiz.length; i++)
			if (!thiz[i].subtype(that[i]))
				return false;
		return true;
	}

	public static boolean incl(Type[] thiz, Type[] that) {
		if (thiz == that)
			return true;
		if ((thiz == null) || (that == null))
			return false;
		search : for (int i = 0; i < thiz.length; i++) {
			/*
			2014-04-24 rphall: Unused local variable j -- BUG?
			ORIGINAL CODE:
			  for (int j = 0; i < that.length; j++)
			REWRITE:
			  for (int j = 0; j < that.length; j++)
			*/
			for (int j = 0; j < that.length; j++)
				if (thiz[i].subtype(that[i]))
					continue search;
			return false;
		}
		return true;
	}

	/** types defined by classes
	 */
	public static class ObjectType extends Type {
		/** the corresponding class symbol
		 */
		public Symbol sym;

		public ObjectType(Symbol sym) {
			super(CLASS);
			this.sym = sym;
		}

		public String toString() {
			return sym.fullname();
		}

		public Symbol sym() {
			return sym;
		}

		public Type supertype() {
			Symbol.ClassSymbol c = sym.superclass();
			if (c == null)
				return Type.NONE;
			else
				return sym.superclass().getType();
		}

		public Type[] implemented() {
			Symbol[] syms = sym.interfaces();
			Type[] res = new Type[syms.length];
			for (int i = 0; i < syms.length; i++)
				res[i] = syms[i].getType();
			return res;
		}

		public boolean isPrim() {
			return false;
		}

		public boolean isRef() {
			return true;
		}

		public boolean isObject() {
			return true;
		}

		public boolean equals(Object that) {
			return (that == Type.ERROR)
				|| ((that instanceof Type) && (((Type) that).tag == CLASS) && (((Type) that).sym() == this.sym));
		}

		public int hashCode() {
			return sym.fullname().hashCode();
		}

		public boolean subtype(Type that) {
			if ((that == Type.ERROR) || (that == Type.ANY))
				return true;
			if (that.tag != CLASS)
				return false;
			if ((that.sym() == this.sym()) || supertype().subtype(that))
				return true;
			Type[] intfs = implemented();
			for (int i = 0; i < intfs.length; i++)
				if (intfs[i].subtype(that))
					return true;
			return false;
		}
	}

	public static class ArrayType extends Type {
		/** the element type
		 */
		public Type elemtype;

		/** r arrays in simple shorthands, references to multi clues */
		public boolean multiIndex;

		public ArrayType(Type elemtype) {
			super(ARRAY);
			this.elemtype = elemtype;
		}

		public ArrayType(Type elemtype, boolean multiIndex) {
			this(elemtype);
			this.multiIndex = multiIndex;
		}

		public String toString() {
			return elemtype + "[]";
		}

		public Type elemtype() {
			return elemtype;
		}

		public boolean isPrim() {
			return false;
		}

		public boolean isRef() {
			return true;
		}

		public boolean isArray() {
			return true;
		}

		public boolean equals(Object that) {
			return (that == Type.ERROR)
				|| ((that instanceof Type)
					&& (((Type) that).tag == ARRAY)
					&& elemtype.equals(((Type) that).elemtype())
					&& multiIndex == ((ArrayType) that).multiIndex);
		}

		public int hashCode() {
			return elemtype.hashCode();
		}

		public boolean subtype(Type that) {
			return (that == Type.ERROR)
				|| (that == Type.ANY)
				|| (((that.tag == CLASS) && (that.supertype().equals(Type.NONE)))
					|| ((that.tag == ARRAY) && elemtype.subtype(that.elemtype()))
					&& multiIndex == ((ArrayType) that).multiIndex);
		}
	}

	public static class MethodType extends Type {
		/** the argument types
		 */
		public Type[] argtypes;

		/** the result type
		 */
		public Type restype;

		/** the thrown exceptions
		 */
		public Type[] thrown;

		public MethodType(Type[] argtypes, Type restype, Type[] thrown) {
			super(METHOD);
			this.argtypes = argtypes;
			this.restype = restype;
			this.thrown = thrown;
		}

		public Type[] argtypes() {
			return argtypes;
		}

		public Type restype() {
			return restype;
		}

		public Type[] thrown() {
			return thrown;
		}

		public boolean isPrim() {
			return false;
		}

		public boolean isRef() {
			return false;
		}

		public boolean isMethod() {
			return true;
		}

		public String toString() {
			String res = restype + " (";
			if (argtypes.length == 0)
				return res + ")";
			res += argtypes[0];
			for (int i = 1; i < argtypes.length; i++)
				res += ", " + argtypes[i];
			return res + ")";
		}

		public boolean equals(Object tha) {
			if (!(tha instanceof Type))
				return false;
			Type that = (Type) tha;
			return (that == Type.ERROR)
				|| (that.isMethod()
					&& equals(argtypes, that.argtypes())
					&& restype.equals(that.restype())
					&& incl(thrown, that.thrown())
					&& incl(that.thrown(), thrown));
		}

		public int hashCode() {
			return restype.hashCode();
		}

		public boolean subtype(Type that) {
			return (that == Type.ERROR)
				|| (that == Type.ANY)
				|| (that.isMethod() && subtypes(that.argtypes(), argtypes) && restype.subtype(that.restype()));
			//&& incl(thrown, that.thrown()));
		}
	}

	/** internal types defined by packages
	 */
	public static class PackageType extends Type {
		/** the corresponding package symbol
		 */
		public Symbol sym;

		public PackageType(Symbol sym) {
			super(PACKAGE);
			this.sym = sym;
		}

		public String toString() {
			return sym.fullname();
		}

		public Symbol sym() {
			return sym;
		}

		public boolean isPrim() {
			return false;
		}

		public boolean isRef() {
			return false;
		}

		public boolean isObject() {
			return false;
		}

		public boolean isPackage() {
			return true;
		}

		public boolean equals(Object that) {
			return (that == Type.ERROR)
				|| ((that instanceof Type) && (((Type) that).tag == PACKAGE) && (((Type) that).sym() == this.sym));
		}

		public int hashCode() {
			return sym.fullname().hashCode();
		}
	}
}
