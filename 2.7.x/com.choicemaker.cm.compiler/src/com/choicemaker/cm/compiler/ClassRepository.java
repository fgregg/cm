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
import java.util.HashMap;

import com.choicemaker.cm.compiler.FileWrapper.JarArchive;
import com.choicemaker.cm.compiler.FileWrapper.NativeFile;
import com.choicemaker.cm.compiler.Symbol.ClassSymbol;
import com.choicemaker.cm.compiler.Symbol.PackageSymbol;

/**
 * Imported classes
 *
 * @author   Matthias Zenger
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:35 $
 */
public class ClassRepository {

	/** the corresponding compilation environment
	 */
	public CompilationEnv env;

	/** repository of all opened jar files
	 */
	public HashMap jarfiles = new HashMap();

	/** a pool of all loaded classes
	 */
	public HashMap classes = new HashMap();

	/** a pool of all known packages
	 */
	public HashMap packages = new HashMap();

	/** predefined classes
	 */
	public ClassSymbol objectClass;
	public ClassSymbol stringClass;

	/** the empty package
	 */
	public PackageSymbol emptyPackage;

	public ClassRepository(CompilationEnv env) {
		this.env = env;
		this.emptyPackage = new PackageSymbol("", Symbol.NONE, this);
		this.packages.put("", this.emptyPackage);
		this.objectClass = defineClass("java.lang.Object");
		this.stringClass = defineClass("java.lang.String");
	}

	/** open file in the given root directory; the root directory may
	 *  also be a jar file
	 */
	public FileWrapper open(String root, String name) {
		FileWrapper res;
		if (root == null)
			res = new NativeFile(new File(name).getAbsoluteFile());
		else if (root.endsWith(".jar")) {
			if ((res = (FileWrapper) jarfiles.get(root)) == null) {
				if ((res = new JarArchive(new File(root).getAbsoluteFile())).isDirectory())
					jarfiles.put(root, res);
			}
			if (name != null)
				res = res.access(name);
		} else if (name == null)
			res = new NativeFile(new File(root).getAbsoluteFile());
		else
			res = new NativeFile(new File(root, name).getAbsoluteFile());
		if (!res.exists())
			return null;
		else
			return res;
	}

	/** create a new file, specified by a fully qualified from the given
	 *  root directory with the given suffix; we use this to write our
	 *  target code
	 */
	public File create(String root, String name, String suffix) throws IOException {
		File outdir = new File(root).getAbsoluteFile();
		int left = 0, right = name.indexOf('.');
		while (right >= left) {
			if (!(outdir = new File(outdir, name.substring(left, right)).getAbsoluteFile()).exists())
				outdir.mkdir();
			right = name.indexOf('.', left = right + 1);
		}
		return new File(outdir, name.substring(left) + suffix).getAbsoluteFile();
	}

	/** find file with given name in class path
	 */
	public FileWrapper find(String name) {
		String[] root = env.classPath.getComponents();
		FileWrapper file;
		for (int i = 0; i < root.length; i++)
			if ((file = open(root[i], name)) != null)
				return file;
		return null;
	}

	/** return the last dot-separated substring; e.g. for fullname = "aa.bb.cc"
	 *  this method returns "cc"
	 */
	private static String shortname(String fullname) {
		return fullname.substring(fullname.lastIndexOf('.') + 1).intern();
	}

	/** return the prefix of fullname without the last dot-separated substring;
	 *  e.g. for fullname = "aa.bb.cc" this method returns "aa.bb"
	 */
	private static String prefix(String fullname) {
		return fullname.substring(0, fullname.lastIndexOf('.')).intern();
	}

	/** define a (new) package; this method maps full package names to
	 *  package symbols
	 */
	public PackageSymbol definePackage(String fullname) {
		if ((fullname == null) || (fullname.length() == 0))
			return emptyPackage;
		//System.out.println("Package: " + fullname);
		fullname = fullname.intern();
		PackageSymbol sym = (PackageSymbol) packages.get(fullname);
		if (sym == null) {
			sym = new PackageSymbol(shortname(fullname), definePrefixPackage(fullname), this);
			packages.put(fullname, sym);
		}
		return sym;
	}

	/** define a package corresponding to the prefix of fullname */
	private Symbol definePrefixPackage(String fullname) {
		if (fullname.indexOf('.') == -1)
			return emptyPackage;
		else
			return definePackage(prefix(fullname));
	}

	/** define a (new) class; this method maps full class names to
	 *  class symbols. Classes are only loaded on demand to avoid
	 *  memory overflows.
	 */
	public ClassSymbol defineClass(String fullname) {
		// the superclass of Object gets represented with null
		if ((fullname == null) || (fullname.length() == 0))
			return null;
		// now lookup class in the repository
		fullname = fullname.intern();
		ClassSymbol sym = (ClassSymbol) classes.get(fullname);
		if (sym == null) {
			// create new uninitialized class symbol
			sym = new ClassSymbol(shortname(fullname), definePrefixPackage(fullname), this);
			classes.put(fullname, sym);
		}
		return sym;
	}

	/** define a set of (new) classes
	 */
	public ClassSymbol[] defineClasses(String[] fullnames) {
		ClassSymbol[] res = new ClassSymbol[fullnames.length];
		for (int i = 0; i < res.length; i++)
			res[i] = defineClass(fullnames[i]);
		return res;
	}

	/** define a set of (new) classes and return the corresponding
	 *  types in an array
	 */
	public Type[] defineClassTypes(String[] fullnames) {
		Type[] res = new Type[fullnames.length];
		for (int i = 0; i < res.length; i++)
			res[i] = defineClass(fullnames[i]).getType();
		return res;
	}
}
