/**
 * @(#)$RCSfile: AllPluginsClassLoader.java,v $  $Revision: 1.1 $ $Date: 2010/01/27 03:58:28 $
 * 
 * Copyright (c) 2003 ChoiceMaker Technologies, Inc. 
 * 71 W 23rd St, Ste 515, New York, NY 10010 
 * All rights reserved.
 * 
 * This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */

package com.choicemaker.e2.mbd.core.resources;

import java.net.URL;
import java.net.URLClassLoader;

import com.choicemaker.e2.mbd.core.launcher.EclipseRMIClassLoaderSpi;

/**
 * Comment
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.1 $ $Date: 2010/01/27 03:58:28 $
 */
public class AllPluginsClassLoader {
	private static final String RMI_CLASS_LOADER_SPI = "java.rmi.server.RMIClassLoaderSpi";
	private static final String ECLIPSE_RMI_CLASS_LOADER_SPI = "org.eclipse.core.launcher.EclipseRMIClassLoaderSpi";

	public static synchronized ClassLoader getInstance() {
		return AllPluginsClassLoader.class.getClassLoader();
	}
	
	public static ClassLoader getInstance(URL[] classPath, ClassLoader parent) {
		return new Loader(classPath, parent);
	}

	public static void setRMIClassLoaderSpi() {
		EclipseRMIClassLoaderSpi.setClassLoader(getInstance());
		System.setProperty(RMI_CLASS_LOADER_SPI, ECLIPSE_RMI_CLASS_LOADER_SPI);
	}

	private static class Loader extends URLClassLoader {
		public Loader(URL[] path, ClassLoader parent) {
			super(path, parent);
		}
		
		protected Class findClass(String name) throws ClassNotFoundException {
			try {
				return super.findClass(name);
			} catch (ClassNotFoundException ex) {
				return Class.forName(name);
			}
		}		
	}
}
