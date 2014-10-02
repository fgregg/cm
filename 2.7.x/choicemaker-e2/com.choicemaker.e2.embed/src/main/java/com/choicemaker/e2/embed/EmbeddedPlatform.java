package com.choicemaker.e2.embed;

import java.net.URL;
import java.util.logging.Logger;

import com.choicemaker.e2.CMPlatform;
import com.choicemaker.e2.CMPlatformRunnable;
import com.choicemaker.e2.CMPluginRegistry;
import com.choicemaker.e2.mbd.PluginRegistryAdapter;
import com.choicemaker.e2.mbd.runtime.CoreException;
import com.choicemaker.e2.mbd.runtime.IConfigurationElement;
import com.choicemaker.e2.mbd.runtime.IExtension;
import com.choicemaker.e2.mbd.runtime.Platform;

/**
 * A singleton implementation that uses an installable delegate to implement
 * IPlatform methods. In general, a delegate should be installed only once in an
 * application context, and this class encourages this restriction by using a
 * {@link #INSTALLABLE_PLUGIN_DISCOVERY System property} to specify the delegate
 * type. If the property is not set, a {@link #getDefaultInstance() default
 * plugin-discovery} is used.
 *
 * @author rphall
 *
 */
public final class EmbeddedPlatform implements CMPlatform {
	
	private static final Logger logger = Logger.getLogger(EmbeddedPlatform.class.getName());

	public CMPluginRegistry getPluginRegistry() {
		return PluginRegistryAdapter.convert(Platform.getPluginRegistry());
	}

	public CMPlatformRunnable loaderGetRunnable(String applicationName) {
		assert Platform.isInitialized();
		CMPlatformRunnable retVal = null;
		if (!Platform.isReady()) {
			String msg = "(embedded) Platform: NOT READY";
			logger.severe(msg);
			assert retVal == null ;
		} else {
			IExtension extension =
				Platform.getPluginRegistry().getExtension(Platform.PI_RUNTIME,
						Platform.PT_APPLICATIONS, applicationName);
			if (extension == null) {
				String msg =
					"(embedded) Platform: no executable extension for '"
							+ applicationName + "'";
				logger.severe(msg);
				assert retVal == null ;
			} else {
				IConfigurationElement[] configs =
					extension.getConfigurationElements();
				if (configs.length == 0) {
					String msg =
						"(embedded) Platform: no configured elements for '"
								+ applicationName + "'";
					logger.severe(msg);
					assert retVal == null ;
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

}
