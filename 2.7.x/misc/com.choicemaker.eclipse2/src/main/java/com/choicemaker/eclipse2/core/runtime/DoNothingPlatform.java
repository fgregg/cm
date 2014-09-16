package com.choicemaker.eclipse2.core.runtime;

import java.net.URL;

import com.choicemaker.eclipse2.core.boot.CMPlatformRunnable;

public final class DoNothingPlatform implements CMPlatform {

	@Override
	public CMPluginRegistry getPluginRegistry() {
		return null;
	}

	@Override
	public CMPlatformRunnable loaderGetRunnable(String applicationName) {
		return null;
	}

	@Override
	public String getPluginDirectory(String id, String version) {
		return null;
	}

	@Override
	public URL getPluginDescriptorUrl(String id, String version,
			String descriptorFile) {
		return null;
	}

}
