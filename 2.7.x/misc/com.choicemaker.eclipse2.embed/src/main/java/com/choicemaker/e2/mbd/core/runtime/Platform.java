/**
 * @(#)$RCSfile: Platform.java,v $  $Revision: 1.1 $ $Date: 2010/01/27 03:58:28 $
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

package com.choicemaker.e2.mbd.core.runtime;

import java.net.URL;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.choicemaker.e2.mbd.adapter.PluginDescriptorAdapter;
import com.choicemaker.e2.mbd.core.boot.BootLoader;
import com.choicemaker.e2.mbd.core.boot.IPlatformRunnable;
import com.choicemaker.e2.mbd.core.internal.plugins.InternalFactory;
import com.choicemaker.e2.mbd.core.internal.plugins.PluginDescriptor;
import com.choicemaker.e2.mbd.core.internal.plugins.PluginRegistry;
import com.choicemaker.e2.mbd.core.internal.plugins.RegistryLoader;
import com.choicemaker.e2.mbd.core.internal.plugins.RegistryResolver;
import com.choicemaker.e2.mbd.core.internal.runtime.Policy;
import com.choicemaker.e2.mbd.core.runtime.model.PluginRegistryModel;
import com.choicemaker.e2.mbd.pd.EmbeddedPluginDiscovery;
import com.choicemaker.eclipse2.core.runtime.CMPluginDescriptor;
import com.choicemaker.eclipse2.core.runtime.CMPluginPrerequisite;
import com.choicemaker.eclipse2.pd.PluginDiscovery;

/**
 * Comment
 *
 * @author Martin Buechi
 * @version $Revision: 1.1 $ $Date: 2010/01/27 03:58:28 $
 */
public class Platform {

	private static final Logger logger = Logger.getLogger(Platform.class
			.getName());

	/**
	 * The unique identifier constant (value "
	 * <code>org.eclipse.core.runtime</code>") of the Core Runtime (pseudo-)
	 * plug-in.
	 */
	public static final String PI_RUNTIME = "org.eclipse.core.runtime"; //$NON-NLS-1$

	public static final String PLUGIN_BASE_DIR = "META-INF/plugins"; //$NON-NLS-1$
	public static final String PLUGINS_FILE = PLUGIN_BASE_DIR + "/plugins.xml"; //$NON-NLS-1$
	public static final String PLUGIN_DESCRIPTOR_FILE = "plugin.xml"; //$NON-NLS-1$
	public static final String FRAGMENT_DESCRIPTOR_FILE = "fragment.xml"; //$NON-NLS-1$

	private static final String EXECUTABLE_PROPERTY_NAME = "run"; //$NON-NLS-1$

	/**
	 * The simple identifier constant (value "<code>applications</code>") of the
	 * extension point of the Core Runtime plug-in where plug-ins declare the
	 * existence of runnable applications. A plug-in may define any number of
	 * applications; however, the platform is only capable of running one
	 * application at a time.
	 * 
	 * @see com.choicemaker.e2.mbd.core.boot.BootLoader#run
	 */
	public static final String PT_APPLICATIONS = "applications"; //$NON-NLS-1$

	/**
	 * Status code constant (value 1) indicating a problem in a plug-in manifest
	 * (<code>plugin.xml</code>) file.
	 */
	public static final int PARSE_PROBLEM = 1;

	/**
	 * Status code constant (value 2) indicating an error occurred while running
	 * a plug-in.
	 */
	public static final int PLUGIN_ERROR = 2;

	private static IPluginRegistry registry;
	private static boolean initialized;
	private static boolean isReady;

	static {
		init();
	}

	public static IPluginRegistry getPluginRegistry() {
		return registry;
	}

	public static synchronized void init() {
		if (!initialized) {
			assert isReady == false;
			boolean maybeReady = true;

			MultiStatus problems =
				new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM,
						Policy.bind("parse.registryProblems"), null); //$NON-NLS-1$
			InternalFactory factory = new InternalFactory(problems);
			if (!problems.isOK()) {
				String msg = "PluginRegistry factory PROBLEM(s): "
				// + problems.getMessage();
						+ problems.toString();
				logger.severe(msg);
				maybeReady = false;
			}

			URL[] pluginPath = getPluginPaths();
			if (pluginPath == null || pluginPath.length == 0) {
				String msg = "(embedded) Platform: no plugin paths";
				logger.severe(msg);
				maybeReady = false;
			}

			boolean debugParse = false;
			if (logger.isLoggable(Level.FINE)) {
				debugParse = true;
			}
			registry =
				(PluginRegistry) RegistryLoader.parseRegistry(pluginPath,
						factory, debugParse);
			if (registry == null) {
				String msg = "(embedded) Platform: failed to parse registry";
				logger.severe(msg);
				maybeReady = false;
			} else {

				boolean debugRegistryResolver = false;
				if (logger.isLoggable(Level.FINE)) {
					debugRegistryResolver = true;
				}
				RegistryResolver registryResolver =
					new RegistryResolver(debugRegistryResolver);

				IStatus status =
					registryResolver.resolve((PluginRegistryModel) registry);
				if (status.isOK()) {
					String msg =
						"PluginRegistry resolution: OK (" + pluginPath.length
								+ " plugin paths resolved)";
					logger.info(msg);
				} else {
					String msg = "PluginRegistry resolution PROBLEM(s): "
					// + status.getMessage();
							+ status.toString();
					logger.severe(msg);
					maybeReady = false;
				}

				IPluginDescriptor[] pluginDescriptors =
					registry.getPluginDescriptors();
				if (pluginDescriptors == null || pluginDescriptors.length == 0) {
					String msg = "(embedded) Platform: no plugin descriptors";
					logger.severe(msg);
					maybeReady = false;
				}

				for (int i = 0; i < pluginDescriptors.length; i++) {
					PluginDescriptor pluginDescriptor =
						(PluginDescriptor) pluginDescriptors[i];
					try {
						CMPluginDescriptor cmpd =
							PluginDescriptorAdapter.convert(pluginDescriptor);
						activatePlugin(cmpd);
					} catch (CoreException e) {
						String msg =
							"PluginRegistry activation failed for '"
									+ pluginDescriptor + "': " + e.toString();
						logger.severe(msg);
						maybeReady = false;
					}
				}
			}
			isReady = maybeReady;
			if (isReady) {
				String msg = "(embedded) Platform: READY";
				logger.info(msg);
			} else {
				String msg = "(embedded) Platform: NOT READY";
				logger.severe(msg);
			}
			initialized = true;
		}
	}

	private static void activatePlugin(CMPluginDescriptor cmpd)
			throws CoreException {
		if (!cmpd.isPluginActivated()) {
			CMPluginPrerequisite[] pluginPrerequisites =
				cmpd.getPluginPrerequisites();
			for (int i = 0; i < pluginPrerequisites.length; i++) {
				IPluginDescriptor prereq =
					registry.getPluginDescriptor(pluginPrerequisites[i]
							.getUniqueIdentifier());
				CMPluginDescriptor cmprereq =
					PluginDescriptorAdapter.convert(prereq);
				activatePlugin(cmprereq);
			}
			IPluginDescriptor ipd =
				registry.getPluginDescriptor(cmpd.getUniqueIdentifier());
			if (!(ipd instanceof PluginDescriptor)) {
				String msg =
					"(embedded) Platform: unknown plugin descriptor class: "
							+ ipd.getClass().getName();
				logger.severe(msg);
				IStatus status =
					new Status(IStatus.ERROR, Platform.PI_RUNTIME,
							Platform.PLUGIN_ERROR, msg, null);
				throw new CoreException(status);
			}
			((PluginDescriptor) ipd).doPluginActivation();
		}
	}

	private static URL[] getPluginPaths() {
		assert !initialized;
		ClassLoader classLoader = Platform.class.getClassLoader();
		logger.fine("Platform classLoader: " + classLoader + "("
				+ classLoader.getClass().getName() + ": "
				+ classLoader.hashCode() + ")");
		String confName = EmbeddedPluginDiscovery.DEFAULT_PLUGIN_CONFIGURATION;
		PluginDiscovery pd = new EmbeddedPluginDiscovery(confName, classLoader);
		Set<URL> plugins = pd.getPluginUrls();
		URL[] retVal = plugins.toArray(new URL[plugins.size()]);
		assert retVal != null;
		if (retVal.length == 0) {
			String msg = "(embedded) Platform: returning 0 [zero] plugin paths";
			logger.severe(msg);
		}
		return retVal;
	}

	/**
	 * Internal method for finding and returning a runnable instance of the
	 * given class as defined in the specified plug-in. The returned object is
	 * initialized with the supplied arguments.
	 * <p>
	 * This method is used by the platform boot loader; is must not be called
	 * directly by client code.
	 * </p>
	 * 
	 * @see BootLoader
	 */
	public static IPlatformRunnable loaderGetRunnable(String applicationName) {
		assert initialized;
		IPlatformRunnable retVal;
		if (!isReady) {
			String msg = "(embedded) Platform: NOT READY";
			logger.severe(msg);
			retVal = null;
		} else {
			IExtension extension =
				registry.getExtension(Platform.PI_RUNTIME,
						Platform.PT_APPLICATIONS, applicationName);
			if (extension == null) {
				String msg =
					"(embedded) Platform: no executable extension for '"
							+ applicationName + "'";
				logger.severe(msg);
				retVal = null;
			} else {
				IConfigurationElement[] configs =
					extension.getConfigurationElements();
				if (configs.length == 0) {
					String msg =
						"(embedded) Platform: no configured elements for '"
								+ applicationName + "'";
					logger.severe(msg);
					retVal = null;
				}
				try {
					if (configs.length > 1) {
						String msg =
							"(embedded) Platform: multiple configured elements ("
									+ configs.length + ") for '"
									+ applicationName
									+ "'; using first configured element";
						logger.warning(msg);
					}
					IConfigurationElement config = configs[0];
					retVal =
						(IPlatformRunnable) config
								.createExecutableExtension(EXECUTABLE_PROPERTY_NAME);
				} catch (CoreException e) {
					String msg =
						"(embedded) Platform: failed to create executable extension for '"
								+ applicationName + "'";
					logger.severe(msg);
					retVal = null;
				}
			}
		}
		if (retVal == null) {
			String msg = "(embedded) Platform: returning null PlatformRunnable";
			logger.severe(msg);
		}
		return retVal;
	}

	public static String getPluginDirectory(String id, String version) {
		// return PLUGIN_BASE_DIR + "/" + id.replace('.', '_') + "_"
		// + version.replace('.', '_') + "/";
		throw new Error("not implemented");
	}

	public static URL getPluginDescriptorUrl(String id, String version,
			String descriptorFile) {
		// return classLoader.getResource(getPluginDirectory(id, version)
		// + descriptorFile);
		throw new Error("not implemented");
	}

}
