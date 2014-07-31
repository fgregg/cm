package com.choicemaker.fake;

import static com.choicemaker.fake.EmbeddedPluginDiscovery.PREFIX;
import static com.choicemaker.fake.ExampleData.EXPECTED_1;
import static com.choicemaker.fake.ExampleData.EXPECTED_2;
import static com.choicemaker.fake.ExampleData.EXPECTED_3;
import static com.choicemaker.fake.ExampleData.BAD_CONFIGURATIONS;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;

public class EmbeddedPluginDiscoveryTest {

	private static Map<String, List<String>> tests = new HashMap<>();
	static {
		tests.put("default", EXPECTED_1);
		tests.put("config_2", EXPECTED_2);
		tests.put("config_3", EXPECTED_3);
	}

	public void compare(Set<URL> discovered, List<String> expected) {
		assertTrue(discovered.size() == expected.size());
		for (URL url : discovered) {
			String s = url.toString();
			int index = s.indexOf(PREFIX) + PREFIX.length();
			String path = s.substring(index);
			assertTrue(expected.contains(path));
		}
	}

	/**
	 * Tests anonymous discovery
	 */
	@Test
	public void testAnonymousDiscovery() {
		PluginDiscovery pd = new EmbeddedPluginDiscovery();
		Set<URL> pluginIds = pd.getPluginUrls();
		assertTrue(pluginIds != null);
		List<String> expectedPaths = EXPECTED_1;
		compare(pluginIds, expectedPaths);
	}

	/**
	 * Tests named discovery
	 */
	@Test
	public void testNamedDiscovery() {
		for (Entry<String, List<String>> test : tests.entrySet()) {
			String configName = test.getKey();
			List<String> expectedPaths = test.getValue();
			PluginDiscovery pd = new EmbeddedPluginDiscovery(configName);
			Set<URL> pluginIds = pd.getPluginUrls();
			assertTrue(pluginIds != null);
			compare(pluginIds, expectedPaths);
		}
	}

	/**
	 * Test detection of bad configurations
	 */
	@Test
	public void testBadConfigurations() {
		for (String name : BAD_CONFIGURATIONS) {
			try {
				PluginDiscovery pd = new EmbeddedPluginDiscovery(name);
				pd.getPluginUrls();
				fail("Error not caught in configuration '" + name + "'");
			} catch (PluginDiscoveryException x) {
				// Expected
			} catch (Exception x) {
				fail("Unexpected exception " + x);
			}
		}
	}

}
