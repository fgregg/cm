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
package com.choicemaker.cm.persist0;

import java.io.Serializable;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Logger;

import com.choicemaker.e2.PluginDiscovery;
import com.choicemaker.e2.mbd.plugin.EmbeddedPluginDiscovery;

/**
 * A test bean that looks up registered plugins in an
 * Embedded Eclipse 2 registry.
 * 
 * @author rphall
 *
 */
public class EmbeddedEclipseBean2 implements Serializable{

	private static final long serialVersionUID = 271L;

	private static Logger logger = Logger.getLogger(EmbeddedEclipseBean2.class
			.getName());

	private final Set<URL> plugins;
	
	// -- Instance data

	// -- Construction

	public EmbeddedEclipseBean2() {
		this(null);
	}

	public EmbeddedEclipseBean2(String config) {
		PluginDiscovery pd = new EmbeddedPluginDiscovery(config);
		plugins = pd.getPluginUrls();
//		plugins = Collections.emptySet();
		for (URL url : plugins) {
			logger.fine(url.toString());
		}
	}

	// -- Accessors
	
	public Set<URL> getPluginUrls() {
		return Collections.unmodifiableSet(plugins);
	}

	// -- Modifiers

	// -- Call backs

	// -- Identity

}
