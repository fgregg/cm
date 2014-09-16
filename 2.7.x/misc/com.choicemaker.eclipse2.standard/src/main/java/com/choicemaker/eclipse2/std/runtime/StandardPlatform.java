package com.choicemaker.eclipse2.std.runtime;

import java.net.URL;

import org.eclipse.core.runtime.Platform;

import com.choicemaker.eclipse2.core.boot.CMPlatformRunnable;
import com.choicemaker.eclipse2.core.runtime.CMPlatform;
import com.choicemaker.eclipse2.core.runtime.CMPluginRegistry;
import com.choicemaker.eclipse2.std.adapter.PluginRegistryAdapter;

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

	public CMPluginRegistry getPluginRegistry() {
		return PluginRegistryAdapter.convert(Platform.getPluginRegistry());
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
