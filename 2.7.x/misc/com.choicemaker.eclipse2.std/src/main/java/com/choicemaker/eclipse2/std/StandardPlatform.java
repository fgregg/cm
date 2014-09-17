package com.choicemaker.eclipse2.std;

import java.net.URL;
import java.util.logging.Logger;

import org.eclipse.core.boot.IPlatformRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.Platform;

import com.choicemaker.e2.std.adapter.PlatformRunnableAdapter;
import com.choicemaker.e2.std.adapter.PluginRegistryAdapter;
import com.choicemaker.eclipse2.core.boot.CMPlatformRunnable;
import com.choicemaker.eclipse2.core.runtime.CMPlatform;
import com.choicemaker.eclipse2.core.runtime.CMPluginRegistry;

/**
 * A singleton implementation that uses an installable delegate to implement
 * IPlatform methods. In general, a delegate should be installed only once
 * in an application context, and this class encourages this restriction by
 * using a {@link #INSTALLABLE_PLUGIN_DISCOVERY System property} to specify the
 * delegate type. If the property is not set, a {@link #getDefaultInstance()
 * default plugin-discovery} is used.
 *
 * @author rphall
 *
 */
public final class StandardPlatform implements CMPlatform {
	
	private static final Logger logger = Logger.getLogger(StandardPlatform.class.getName());
	
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
					String msg = "Error getting runnable: " + applicationName + ": " + e.toString();
					logger.severe(msg);
					retVal = null;
				}
			}
			throw new Error("not implemented");
		}
		return retVal;
	}

	public String getPluginDirectory(String id, String version) {
//		return PLUGIN_BASE_DIR + "/" + id.replace('.', '_') + "_" + version.replace('.', '_') + "/";
		throw new Error("not yet implemented");
	}

	public URL getPluginDescriptorUrl(String id, String version,
			String descriptorFile) {
//		return classLoader.getResource(getPluginDirectory(id, version) + descriptorFile);
		throw new Error("not yet implemented");
	}

}
