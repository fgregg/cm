package com.choicemaker.e2.platform;

import java.net.URL;

import com.choicemaker.e2.CMPlatform;
import com.choicemaker.e2.CMPlatformRunnable;
import com.choicemaker.e2.CMPluginRegistry;

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
