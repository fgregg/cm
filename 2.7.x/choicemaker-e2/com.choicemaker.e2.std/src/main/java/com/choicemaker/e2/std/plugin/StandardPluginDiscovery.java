/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.choicemaker.e2.std.plugin;

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.choicemaker.e2.CMPluginDescriptor;
import com.choicemaker.e2.CMPluginRegistry;
import com.choicemaker.e2.PluginDiscovery;
import com.choicemaker.e2.standard.StandardPlatform;

/**
 * Singleton that implements PluginDiscovery
 * 
 * @author rphall
 */
public class StandardPluginDiscovery implements PluginDiscovery {

	private static final String SOURCE_CLASS = StandardPluginDiscovery.class
			.getSimpleName();

	private static final Logger logger = Logger
			.getLogger(StandardPluginDiscovery.class.getName());

	private final StandardPlatform stdPlatform;

	public StandardPluginDiscovery() {
		this(new StandardPlatform());
	}

	public StandardPluginDiscovery(StandardPlatform platform) {
		if (platform == null) {
			throw new IllegalArgumentException("null argument");
		}
		this.stdPlatform = platform;
	}

	@Override
	public Set<URL> getPluginUrls() {
		final String METHOD = "getPluginUrls";
		logger.entering(SOURCE_CLASS, METHOD);
		Set<URL> urls = new HashSet<>();
		CMPluginRegistry registry = stdPlatform.getPluginRegistry();
		CMPluginDescriptor[] descriptors = registry.getPluginDescriptors();
		for (CMPluginDescriptor descriptor : descriptors) {
			logger.finest("descriptor: " + descriptor.getUniqueIdentifier());
			URL url = descriptor.getInstallURL();
			logger.finer("descriptor/URL: " + descriptor.getUniqueIdentifier() + "/" + url );
			urls.add(url);
		}
		Set<URL> retVal = Collections.unmodifiableSet(urls);
		logger.exiting(SOURCE_CLASS, METHOD, retVal);
		return retVal;
	}
}
