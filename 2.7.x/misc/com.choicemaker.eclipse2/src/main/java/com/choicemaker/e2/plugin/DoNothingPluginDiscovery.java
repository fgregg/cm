package com.choicemaker.e2.plugin;

import java.net.URL;
import java.util.Collections;
import java.util.Set;

import com.choicemaker.e2.PluginDiscovery;

public class DoNothingPluginDiscovery implements PluginDiscovery {

	@Override
	public Set<URL> getPluginUrls() {
		return Collections.emptySet();
	}

}
