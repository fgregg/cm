/**
 * @(#)$RCSfile: EclipseObjectInputStream.java,v $  $Revision: 1.1 $ $Date: 2010/01/27 03:58:28 $
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

package org.eclipse.core.resources;

import java.io.*;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;

/**
 * Comment
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.1 $ $Date: 2010/01/27 03:58:28 $
 */
public class EclipseObjectInputStream extends ObjectInputStream {
	public EclipseObjectInputStream(InputStream in) throws IOException {
		super(in);
	}

	protected Class resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
		try {
			return super.resolveClass(desc);
		} catch (ClassNotFoundException ex) {
			return Class.forName(desc.getName(), false, AllPluginsClassLoader.getInstance());
		}
	}

	protected Class resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException {
		try {
			return super.resolveProxyClass(interfaces);
		} catch (ClassNotFoundException ex) {
			ClassLoader allLoader = AllPluginsClassLoader.getInstance();
			ClassLoader nonPublicLoader = null;
			boolean hasNonPublicInterface = false;
			Class[] classObjs = new Class[interfaces.length];
			for (int i = 0; i < interfaces.length; i++) {
				Class cl = Class.forName(interfaces[i], false, allLoader);
				if ((cl.getModifiers() & Modifier.PUBLIC) == 0) {
					if (hasNonPublicInterface) {
						if (nonPublicLoader != cl.getClassLoader()) {
							throw new IllegalAccessError("conflicting non-public interface class loaders");
						}
					} else {
						nonPublicLoader = cl.getClassLoader();
						hasNonPublicInterface = true;
					}
				}
				classObjs[i] = cl;
			}
			try {
				return Proxy.getProxyClass(hasNonPublicInterface ? nonPublicLoader : allLoader, classObjs);
			} catch (IllegalArgumentException e) {
				throw new ClassNotFoundException(null, e);
			}
		}
	}
}
