/**
 * @(#)$RCSfile: EclipseRMIClassLoaderSpi.java,v $  $Revision: 1.1 $ $Date: 2010/01/27 03:58:28 $
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

package org.eclipse.core.launcher;

import java.net.MalformedURLException;
import java.rmi.server.RMIClassLoader;
import java.rmi.server.RMIClassLoaderSpi;
import java.util.*;

/**
 * Comment
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.1 $ $Date: 2010/01/27 03:58:28 $
 */
public class EclipseRMIClassLoaderSpi extends RMIClassLoaderSpi {
	private static ClassLoader classLoader;

	public static void setClassLoader(ClassLoader v) {
		classLoader = v;
	}

	public ClassLoader getClassLoader(String codebase) throws MalformedURLException {
		return RMIClassLoader.getDefaultProviderInstance().getClassLoader(codebase);
	}

	public String getClassAnnotation(Class cl) {
		RMIClassLoaderSpi defaultProvider = RMIClassLoader.getDefaultProviderInstance();
		if (cl.isPrimitive() || cl.isArray()) {
			return defaultProvider.getClassAnnotation(cl);
		} else {
			Set codebaseElements = new HashSet();
			addClassAnnotations(codebaseElements, cl, defaultProvider);
			StringBuffer annotation = new StringBuffer();
			boolean first = true;
			for (Iterator iCodeBaseElements = codebaseElements.iterator(); iCodeBaseElements.hasNext();) {
				if (first) {
					first = false;
				} else {
					annotation.append(' ');
				}
				annotation.append((String) iCodeBaseElements.next());
			}
			return annotation.toString();
		}
	}

	private void addClassAnnotations(Set codebaseElements, Class cl, RMIClassLoaderSpi defaultProvider) {
		String defaultAnnotation = defaultProvider.getClassAnnotation(cl);
		StringTokenizer st = new StringTokenizer(defaultAnnotation, " ");
		while(st.hasMoreTokens()) {
			codebaseElements.add(st.nextToken());
		}
		Class superclass = cl.getSuperclass();
		if(superclass != null) {
			addClassAnnotations(codebaseElements, superclass, defaultProvider);
		}
		Class[] interfaces = cl.getInterfaces();
		for (int i = 0; i < interfaces.length; i++) {
			addClassAnnotations(codebaseElements, interfaces[i], defaultProvider);
		}
	}

	public Class loadClass(String codebase, String name, ClassLoader defaultLoader)
		throws MalformedURLException, ClassNotFoundException {
		if (defaultLoader != null) {
			try {
				return defaultLoader.loadClass(name);
			} catch (ClassNotFoundException ex) {
			}
		}
		if (codebase != null) {
			try {
				return getClassLoader(codebase).loadClass(name);
			} catch (ClassNotFoundException ex) {
			}
		}
		return classLoader.loadClass(name);
	}

	public Class loadProxyClass(String codebase, String[] interfaces, ClassLoader defaultLoader)
		throws MalformedURLException, ClassNotFoundException {
		return RMIClassLoader.getDefaultProviderInstance().loadProxyClass(codebase, interfaces, classLoader);
	}

}
