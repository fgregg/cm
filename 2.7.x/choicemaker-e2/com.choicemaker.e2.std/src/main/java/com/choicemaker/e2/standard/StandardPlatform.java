package com.choicemaker.e2.standard;

import java.net.URL;
import java.util.logging.Logger;

import org.eclipse.core.boot.IPlatformRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.Platform;

import com.choicemaker.e2.CMPlatform;
import com.choicemaker.e2.CMPlatformRunnable;
import com.choicemaker.e2.CMPluginRegistry;
import com.choicemaker.e2.platform.InstallablePlatform;
import com.choicemaker.e2.plugin.InstallablePluginDiscovery;
import com.choicemaker.e2.std.PlatformRunnableAdapter;
import com.choicemaker.e2.std.PluginRegistryAdapter;
import com.choicemaker.e2.std.plugin.StandardPluginDiscovery;

/**
 * An implementation of CMPlatform that uses the standard Eclipse 2 Platform
 * singleton to implement CMPlatform methods. To avoid tight-coupling to this
 * particular implementation, don't use it directly, but rather
 * {@link #install() install} it as a delegate to the
 * {@link #InstallablePlatform} class.
 *
 * @author rphall
 *
 */
public final class StandardPlatform implements CMPlatform {

	private static final Logger logger = Logger
			.getLogger(StandardPlatform.class.getName());

	public static final String ATTRIBUTE_EXEC_EXTENSION = "run"; //$NON-NLS-1$

	public CMPluginRegistry getPluginRegistry() {
		return PluginRegistryAdapter.convert(Platform.getPluginRegistry());
	}

	public CMPlatformRunnable loaderGetRunnable(String applicationName) {
		CMPlatformRunnable retVal = null;
		IPluginRegistry registry = Platform.getPluginRegistry();
		assert registry != null;
		IExtension extension =
			registry.getExtension(Platform.PI_RUNTIME,
					Platform.PT_APPLICATIONS, applicationName);
		if (extension != null) {
			IConfigurationElement[] configs =
				extension.getConfigurationElements();
			if (configs.length != 0) {
				try {
					IConfigurationElement config = configs[0];
					IPlatformRunnable ipr =
						(IPlatformRunnable) config
								.createExecutableExtension(ATTRIBUTE_EXEC_EXTENSION);
					retVal = PlatformRunnableAdapter.convert(ipr);
				} catch (CoreException e) {
					String msg =
						"Error getting runnable: " + applicationName + ": "
								+ e.toString();
					logger.severe(msg);
					retVal = null;
				}
			}
			throw new Error("not implemented");
		}
		return retVal;
	}

	public String getPluginDirectory(String id, String version) {
		// return PLUGIN_BASE_DIR + "/" + id.replace('.', '_') + "_" +
		// version.replace('.', '_') + "/";
		throw new Error("not yet implemented");
	}

	public URL getPluginDescriptorUrl(String id, String version,
			String descriptorFile) {
		// return classLoader.getResource(getPluginDirectory(id, version) +
		// descriptorFile);
		throw new Error("not yet implemented");
	}

	/**
	 * The well-known instance of this class.
	 * 
	 * @see #getInstance()
	 */
	private static final StandardPlatform theInstance = new StandardPlatform();

	/**
	 * Returns a well-known instance of this class. While there's nothing to
	 * prevent many instances of this class from being created, and there's
	 * little harm in doing so, there's also no point. Hence this method for
	 * getting
	 */
	public static StandardPlatform getInstance() {
		return theInstance;
	}

	public static void install() {
		// Set the System properties for consistency
		String pn = InstallablePlatform.INSTALLABLE_PLATFORM;
		final String platformFQCN = StandardPlatform.class.getName();
		System.setProperty(pn, platformFQCN);
		pn = InstallablePluginDiscovery.INSTALLABLE_PLUGIN_DISCOVERY;
		final String pdFQCN = StandardPluginDiscovery.class.getName();
		System.setProperty(pn, pdFQCN);

		// Install the well-known instance of this class and its usual method
		// of plugin discovery
		InstallablePlatform.getInstance().install(getInstance());
		InstallablePluginDiscovery.getInstance().install(pdFQCN);
	}

}
