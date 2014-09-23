package com.choicemaker.e2.embed;

import java.net.URL;

import com.choicemaker.e2.CMPlatform;
import com.choicemaker.e2.CMPlatformRunnable;
import com.choicemaker.e2.CMPluginRegistry;
import com.choicemaker.e2.mbd.PlatformRunnableAdapter;
import com.choicemaker.e2.mbd.PluginRegistryAdapter;
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

	public CMPluginRegistry getPluginRegistry() {
		return PluginRegistryAdapter.convert(Platform.getPluginRegistry());
	}

	public CMPlatformRunnable loaderGetRunnable(String applicationName) {
		return PlatformRunnableAdapter.convert(Platform.loaderGetRunnable(applicationName));
	}

	public String getPluginDirectory(String id, String version) {
		throw new Error("not implemented");
	}

	public URL getPluginDescriptorUrl(String id, String version,
			String descriptorFile) {
		throw new Error("not implemented");
	}

}
