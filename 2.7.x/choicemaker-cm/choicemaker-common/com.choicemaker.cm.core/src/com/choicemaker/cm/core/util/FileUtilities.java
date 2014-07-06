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
package com.choicemaker.cm.core.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.tools.ant.util.FileUtils;

/**
 * Description
 *
 * @author  Martin Buechi
 * @version $Revision: 1.1 $ $Date: 2010/01/20 15:05:03 $
 */
public class FileUtilities {
	private static Logger logger = Logger.getLogger(FileUtilities.class);
	private static final FileUtils FILE_UTILS = FileUtils.newFileUtils();

//	public static File resolveFile(String fileName) throws IOException {
//		return FILE_UTILS.resolveFile(XmlConfigurator.getInstance().wdir, fileName).getCanonicalFile();
//	}

	public static File resolveFile(File relativeTo, String fileName) throws IOException {
		return FILE_UTILS.resolveFile(relativeTo, fileName).getCanonicalFile();
	}

	/**
	 * Returns a file <code>rel</code> such that
	 *
	 *  file.getCanonicalFile().toString.equals( new File(relativeToDir, rel.toString()).getCanonicalFile() )
	 *
	 * returns true.
	 */
	public static File getRelativeFile(File relativeToDir, String file) {

		File relDir = relativeToDir.getAbsoluteFile();
		ArrayList relPieces = new ArrayList();
		while (relDir != null) {
			relPieces.add(0, relDir);
			relDir = relDir.getParentFile();
		}

		File f = new File(file);
		ArrayList fPieces = new ArrayList();
		while (f != null) {
			fPieces.add(0, f);
			f = f.getParentFile();
		}

		// figure out how long they're the same...
		int sameIndex = -1;
		while (sameIndex + 1 < relPieces.size() && sameIndex + 1< fPieces.size()) {
			if (relPieces.get(sameIndex + 1).equals(fPieces.get(sameIndex + 1))) {
				sameIndex++;
			} else {
				break;
			}
		}

		if (sameIndex < 0) {
			throw new IllegalArgumentException("Files on different disks!");
		}

		File rel = null;

		for (int i = sameIndex + 1; i < relPieces.size(); i++) {
			rel = new File(rel, "..");
		}

		for (int i = sameIndex + 1; i < fPieces.size(); i++) {
			File piece = (File) fPieces.get(i);
			rel = new File(rel, piece.getName());
		}

		return rel;
	}

	public static File getAbsoluteFile(File relativeTo, String file) {
		File f = new File(file);
		if (f.isAbsolute()) {
			return f;
		} else {
			return new File(relativeTo, file).getAbsoluteFile();
		}
	}

	public static File getAbsoluteFile(File relativeTo, File file) {
		if (file.isAbsolute()) {
			return file;
		} else {
			return new File(relativeTo, file.getPath()).getAbsoluteFile();
		}
	}

	public static boolean isFileAbsolute(String file) {
		return new File(file).isAbsolute();
	}

	public static URL[] cpToUrls(File wdir, String cp) throws MalformedURLException, IOException {
		StringTokenizer st = new StringTokenizer(cp, ";:" + File.pathSeparator);
		List l = new ArrayList();
		while (st.hasMoreElements()) {
			String t = st.nextToken();
			l.add(toURL(FileUtilities.resolveFile(wdir, t)));
		}
		return (URL[]) l.toArray(new URL[l.size()]);
	}

	public static URL toURL(File f) throws MalformedURLException, IOException {
		String path = f.getCanonicalPath();
		if (File.separatorChar != '/') {
			path = path.replace(File.separatorChar, '/');
		}
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		String ext = path.substring(Math.max(0, path.length() - 4), path.length()).toUpperCase().intern();
		if (!path.endsWith("/") && (f.isDirectory() || !(ext == ".JAR" || ext == ".ZIP"))) {
			path = path + "/";
		}
		return new URL("file", "", path);
	}

	public static File findFileOnClasspath(String name) {
		File file = null;
		StringTokenizer st = new StringTokenizer(System.getProperty("java.class.path"), ";:" + File.pathSeparator);
		while (file == null && st.hasMoreElements()) {
			String t = st.nextToken();
			File f = new File(t + File.separatorChar + name);
			if (f.exists()) {
				file = f;
			}
		}
		return file;
	}

	public static String toAbsoluteClasspath(File wdir, String cp) throws IOException {
		StringBuffer res = new StringBuffer();
		StringTokenizer st = new StringTokenizer(cp, ";" + File.pathSeparator);
		while (st.hasMoreElements()) {
			res.append(File.pathSeparator);
			res.append(resolveFile(wdir, st.nextToken()));
		}
		return res.toString();
	}

	public static String toAbsoluteUrlClasspath(File wdir, String cp) throws IOException {
		StringBuffer res = new StringBuffer();
		StringTokenizer st = new StringTokenizer(cp, ";" + File.pathSeparator);
		while (st.hasMoreElements()) {
			res.append(" ");
			res.append(resolveFile(wdir, st.nextToken()).toURL().toString());
		}
		return res.toString();
	}
	
	/**
	 * Deletes the files and subdirectories contained in the specified
	 * directory <code>d</code>. If the specified file <code>d</code>
	 * is not a directory, this method has no effect.
	 * @param d a non-null file or directory. If a file, this method has no effect.
	 * If a directory has empty, this method has no effect.
	 */
	public static void removeChildren(File d) {
		if (d == null) {
			throw new IllegalArgumentException("null file or directory");
		}
		String[] list = d.list();
		if (d.isDirectory() && list != null) {
			for (int i = 0; i < list.length; i++) {
				String s = list[i];
				File f = new File(d, s);
				if (f.isDirectory()) {
					removeDir(f);
				} else {
					if (!f.delete()) {
						logger.error("Unable to delete file "
								+ f.getAbsolutePath());
					}
				}
			}
		}
	}

	/**
	 * Deletes the specified file or directory <code>f</code>. If <code>f</code>
	 * is a directory, removes all the children in the directory. If a file or
	 * directory or child can not be removed, this method fails without an
	 * exception being thrown, although the error is logged.
	 * 
	 * @param f
	 *            file or directory
	 */
	public static void removeDir(File f) {
		if (f == null) {
			throw new IllegalArgumentException("null file or directory");
		}
		if (f.isDirectory()) {
			removeChildren(f);
		}
		if (!f.delete()) {
			logger.error("Unable to delete directory " + f.getAbsolutePath());
		}
	}

	public static String getExtension(String fileName) {
		int n = fileName.lastIndexOf('.');
		if(n > 0) {
			return fileName.substring(n + 1);
		} else {
			return "";
		}
	}

	public static String getBase(String fileName) {
		int n = fileName.lastIndexOf('.');
		if(n > 0) {
			return fileName.substring(0, n);
		} else {
			return fileName;
		}
	}

	public static boolean hasExtension(File f, String extension) {
		return f.getName().toLowerCase().endsWith("." + extension.toLowerCase());
	}

	public static File addExtensionIfNecessary(File f, String extension) {
		if(hasExtension(f, extension)) {
			return f;
		} else {
			return new File(f.getAbsolutePath() + "." + extension);
		}
	}
}
