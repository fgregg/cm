package com.choicemaker.cm.core.gen;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

public class Eclipse2GeneratorPluginFactory implements
		IGeneratorPluginFactory {

	private static final Logger logger = Logger
			.getLogger(Eclipse2GeneratorPluginFactory.class.getName());

	public static final String EXTENSION_POINT = "com.choicemaker.cm.core.base.generatorPlugin";

	public static final String EXTENSION_EXECUTABLE_PROPERTY = "class";

	// An unmodifiable list of generator plugins
	private final List generatorPlugins;

	/**
	 * A default constructor method that looks up a System property to determine
	 * the list of plugins to install
	 */
	public Eclipse2GeneratorPluginFactory() throws IllegalArgumentException {
		this(load());
	}

	/**
	 * A constructor method that loads the specified list of generator plugins
	 */
	public Eclipse2GeneratorPluginFactory(List generatorPlugins) {
		this.generatorPlugins = ListBackedGeneratorPluginFactory.validateAndCopy(generatorPlugins);
	}

	/**
	 * Loads generator plugins from the Eclipse 2.1 registry
	 *
	 * @return a non-null list (possibly empty)
	 * @throws IllegalStateException if exactly one configuration does not
	 * exist for each GeneratorPlugin defined in the registry,
	 */
	public static List load() throws IllegalStateException {
		final String msgPrefix = "Loading generatorPlugins from an Eclipse 2 registry: ";
		List retVal = new LinkedList();
		IExtensionPoint extensionPoint = Platform.getPluginRegistry().getExtensionPoint(EXTENSION_POINT);
		IExtension[] extensions = extensionPoint.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			IExtension extension = extensions[i];
			IConfigurationElement[] elems = extension.getConfigurationElements();
			if (elems.length == 0) {
				String msg = msgPrefix + "no configurations for '" + extension.toString() + "'";
				throw new IllegalStateException(msg);
			} else if (elems.length > 1) {
				String msg = msgPrefix + "multiple configurations for '" + extension.toString() + "'";
				throw new IllegalStateException(msg);
			}
			try {
				GeneratorPlugin gp = (GeneratorPlugin) elems[0].createExecutableExtension(EXTENSION_EXECUTABLE_PROPERTY);
				logger.debug("Generator: '" + gp.toString() + "'");
				retVal.add(gp);
			} catch (CoreException e) {
				String msg = msgPrefix + e.toString() + ": " + e.getCause();
				throw new IllegalStateException(msg);
			}
		}
		return retVal;
	}

	public List lookupGeneratorPlugins() {
		return this.generatorPlugins;
	}

}
