package com.choicemaker.eclipse2.pd;

import java.net.URL;
import java.util.Collections;
import java.util.Set;

public class DoNothingPluginDiscovery implements PluginDiscovery {

	@Override
	public Set<URL> getPluginUrls() {
		return Collections.emptySet();
	}

}
