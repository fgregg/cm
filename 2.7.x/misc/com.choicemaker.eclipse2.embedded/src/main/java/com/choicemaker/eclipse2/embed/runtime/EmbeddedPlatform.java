package com.choicemaker.eclipse2.embed.runtime;

import java.net.URL;

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
public final class EmbeddedPlatform implements CMPlatform {

	public CMPluginRegistry getPluginRegistry() {
		throw new Error("not implemented");
//		return PluginRegistryAdapter.convert(Platform.getPluginRegistry());
	}

	public CMPlatformRunnable loaderGetRunnable(String applicationName) {
		throw new Error("not implemented");
	}

	public String getPluginDirectory(String id, String version) {
		throw new Error("not implemented");
	}

	public URL getPluginDescriptorUrl(String id, String version,
			String descriptorFile) {
		throw new Error("not implemented");
	}

}
