package com.choicemaker.cmit.eclipse2.embedded.it2;

import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Set;

import org.junit.Test;

import com.choicemaker.eclipse2.embedded.pd.EmbeddedPluginDiscovery;
import com.choicemaker.eclipse2.pd.PluginDiscovery;

public class EmbeddedPluginDiscoveryTest {

	/**
	 * Tests anonymous discovery
	 */
	@Test
	public void testAnonymousDiscovery() {
		PluginDiscovery pd = new EmbeddedPluginDiscovery();
		Set<URL> pluginIds = pd.getPluginUrls();
		assertTrue(pluginIds != null);
		for (URL url : pluginIds) {
			System.out.println(url.toString());
		}
	}

}
