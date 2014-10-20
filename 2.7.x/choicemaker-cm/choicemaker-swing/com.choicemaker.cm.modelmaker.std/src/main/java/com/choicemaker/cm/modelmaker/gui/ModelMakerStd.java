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
package com.choicemaker.cm.modelmaker.gui;

import java.io.IOException;
import java.util.logging.Logger;

import org.eclipse.core.boot.IPlatformRunnable;

import com.choicemaker.e2.platform.InstallablePlatform;
import com.choicemaker.e2.plugin.InstallablePluginDiscovery;

/**
 * A wrapper around ModelMaker that adapts it as an IPlatformRunnable
 * instance.
 *
 * @author rphall
 */
public class ModelMakerStd extends ModelMaker implements IPlatformRunnable {

	private static final long serialVersionUID = 1L;

	public static Logger logger = Logger.getLogger(ModelMakerStd.class.getName());
	
	public static String PLUGIN_APPLICATION_ID = "com.choicemaker.cm.modelmaker.ModelMakerStd";

  protected static String STANDARD_PLATFORM =
    "com.choicemaker.e2.standard.StandardPlatform";

  protected static String STANDARD_PLUGIN_DISCOVERY =
    "com.choicemaker.e2.std.plugin.StandardPluginDiscovery";

	/**
	 * Calls the {@link ModelMaker#_setEclipse2ConfigurationProperties()
	 * parent method}, then configures System properties that specify
	 * the standard Eclipse 2 platform and plugin discovery classes.
	 */
	protected void _setEclipse2ConfigurationProperties() {
		super._setEclipse2ConfigurationProperties();

		String pn = InstallablePlatform.INSTALLABLE_PLATFORM;
		String pv = STANDARD_PLATFORM;
		System.setProperty(pn,pv);
		
		pn = InstallablePluginDiscovery.INSTALLABLE_PLUGIN_DISCOVERY;
		pv = STANDARD_PLUGIN_DISCOVERY;
		System.setProperty(pn,pv);
	}

	/**
	 * Displays the GUI of an instance; waits for user input; and tears down the
	 * GUI when the user indirectly invokes {@link #programExit(int)} through
	 * some menu choice.
	 * 
	 * @param args2
	 *            typically a non-null String array. Other types, including
	 *            null, are ignored.
	 * @return the exit code that was specified when programExit(int) was
	 *         invoked.
	 */
	public Object run(Object args2) {
		return super.run(args2);
	}

	public static void main(String[] args) throws IOException {
		System.err.println("Not implemented. Run under the standard Eclipse 2 platform.");
	}

}
