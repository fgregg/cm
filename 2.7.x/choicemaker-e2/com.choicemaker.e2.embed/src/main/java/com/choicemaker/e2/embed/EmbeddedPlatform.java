package com.choicemaker.e2.embed;

import java.net.URL;
import java.util.logging.Logger;

import com.choicemaker.e2.CMPlatform;
import com.choicemaker.e2.CMPlatformRunnable;
import com.choicemaker.e2.CMPluginRegistry;
import com.choicemaker.e2.mbd.PluginRegistryAdapter;
import com.choicemaker.e2.mbd.plugin.EmbeddedPluginDiscovery;
import com.choicemaker.e2.mbd.runtime.CoreException;
import com.choicemaker.e2.mbd.runtime.IConfigurationElement;
import com.choicemaker.e2.mbd.runtime.IExtension;
import com.choicemaker.e2.mbd.runtime.Platform;
import com.choicemaker.e2.platform.InstallablePlatform;
import com.choicemaker.e2.plugin.InstallablePluginDiscovery;

/**
 * An implementation of CMPlatform suitable for "embedded" use as plain-old Java
 * library. To avoid tight-coupling to this particular implementation, don't use
 * it directly, but rather {@link #install() install} it as a delegate to the
 * {@link #InstallablePlatform} class.
 *
 * @author rphall
 *
 */
public final class EmbeddedPlatform implements CMPlatform {

	private static final Logger logger = Logger
			.getLogger(EmbeddedPlatform.class.getName());

	public CMPluginRegistry getPluginRegistry() {
		return PluginRegistryAdapter.convert(Platform.getPluginRegistry());
	}

	public CMPlatformRunnable loaderGetRunnable(String applicationName) {
		assert Platform.isInitialized();
		CMPlatformRunnable retVal = null;
		if (!Platform.isReady()) {
			String msg = "(embedded) Platform: NOT READY";
			logger.severe(msg);
			assert retVal == null;
		} else {
			IExtension extension =
				Platform.getPluginRegistry().getExtension(Platform.PI_RUNTIME,
						Platform.PT_APPLICATIONS, applicationName);
			if (extension == null) {
				String msg =
					"(embedded) Platform: no executable extension for '"
							+ applicationName + "'";
				logger.severe(msg);
				assert retVal == null;
			} else {
				IConfigurationElement[] configs =
					extension.getConfigurationElements();
				if (configs.length == 0) {
					String msg =
						"(embedded) Platform: no configured elements for '"
								+ applicationName + "'";
					logger.severe(msg);
					assert retVal == null;
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
					Object o =
						config.createExecutableExtension(Platform.EXECUTABLE_PROPERTY_NAME);
					if (o instanceof CMPlatformRunnable) {
						retVal = (CMPlatformRunnable) o;
						assert retVal != null;
					} else {
						assert retVal == null;
					}
				} catch (CoreException e) {
					String msg =
						"(embedded) Platform: failed to create executable extension for '"
								+ applicationName + "'";
					logger.severe(msg);
					assert retVal == null;
				}
			}
		}
		if (retVal == null) {
			String msg = "(embedded) Platform: returning null PlatformRunnable";
			logger.severe(msg);
		}
		return retVal;
	}

	public String getPluginDirectory(String id, String version) {
		throw new Error("not implemented");
	}

	public URL getPluginDescriptorUrl(String id, String version,
			String descriptorFile) {
		throw new Error("not implemented");
	}

	/**
	 * The well-known instance of this class.
	 * 
	 * @see #getInstance()
	 */
	private static final EmbeddedPlatform theInstance = new EmbeddedPlatform();

	/**
	 * Returns a well-known instance of this class. While there's nothing to
	 * prevent many instances of this class from being created, and there's
	 * little harm in doing so, there's also no point. Hence this method for
	 * getting
	 */
	public static EmbeddedPlatform getInstance() {
		return theInstance;
	}

	public static void install() {
		// Set the System properties for consistency
		String pn = InstallablePlatform.INSTALLABLE_PLATFORM;
		final String platformFQCN = EmbeddedPlatform.class.getName();
		System.setProperty(pn, platformFQCN);
		pn = InstallablePluginDiscovery.INSTALLABLE_PLUGIN_DISCOVERY;
		final String pdFQCN = EmbeddedPluginDiscovery.class.getName();
		System.setProperty(pn, pdFQCN);

		// Install the well-known instance of this class and its usual method
		// of plugin discovery
		InstallablePlatform.getInstance().install(EmbeddedPlatform.getInstance());
		InstallablePluginDiscovery.getInstance().install(pdFQCN);
	}

}
