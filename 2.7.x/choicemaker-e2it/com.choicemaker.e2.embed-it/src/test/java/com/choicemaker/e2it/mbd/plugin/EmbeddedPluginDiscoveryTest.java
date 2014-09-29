package com.choicemaker.e2it.mbd.plugin;

import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Set;
import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Test;

import com.choicemaker.e2.PluginDiscovery;
import com.choicemaker.e2.mbd.plugin.EmbeddedPluginDiscovery;
import com.choicemaker.e2.plugin.InstallablePluginDiscovery;

public class EmbeddedPluginDiscoveryTest {

	private static final Logger logger = Logger
			.getLogger(EmbeddedPluginDiscoveryTest.class.getName());

	@BeforeClass
	public static void configureEmbeddedPluginDiscovery() {
		String pn = InstallablePluginDiscovery.INSTALLABLE_PLUGIN_DISCOVERY;
		String pv = EmbeddedPluginDiscovery.class.getName();
		System.setProperty(pn, pv);
	}

	/**
	 * Tests anonymous discovery. There should be about 48 plugins based on the
	 * POM for this test:
	 * <ul>
	 * <li>com.choicemaker.cm.analyzer.base</li>
	 * <li>com.choicemaker.cm.analyzer.tools.gui</li>
	 * <li>com.choicemaker.cm.compiler</li>
	 * <li>com.choicemaker.cm.core</li>
	 * <li>com.choicemaker.cm.gui.utils</li>
	 * <li>com.choicemaker.cm.io.blocking.automated.base</li>
	 * <li>com.choicemaker.cm.io...</li>
	 * <li>com.choicemaker.cm.matching.cfg</li>
	 * <li>com.choicemaker.cm.matching...</li>
	 * <li>com.choicemaker.cm.ml.me.base</li>
	 * <li>com.choicemaker.cm.ml.me.gui</li>
	 * <li>com.choicemaker.cm.mmdevtools</li>
	 * <li>com.choicemaker.cm.modelmaker</li>
	 * <li>com.choicemaker.cm.module</li>
	 * <li>com.choicemaker.cm.urm.base</li>
	 * <li>com.choicemaker.cm.util.app</li>
	 * <li>com.choicemaker.cm.validation.eclipse</li>
	 * <li>com.sun.tools</li>
	 * <li>com.wcohen.ss.eclipse</li>
	 * <li>com.wcohen.ss</li>
	 * <li>org.apache.ant</li>
	 * <li>org....</li>
	 * <li>org.eclipse.core.boot</li>
	 * <li>org.eclipse...</li>
	 * <li>org.jdom</li>
	 * </ul>
	 */
	@Test
	public void testEmbeddedPluginDiscoveryDefaultConfiguration() {

		// Default constructor specifies the default configuration
		PluginDiscovery pd1 = new EmbeddedPluginDiscovery();
		Set<URL> pluginUrls1 = pd1.getPluginUrls();
		assertTrue(pluginUrls1 != null);
		assertTrue(!pluginUrls1.isEmpty());
		logger.fine("plugins <1>: " + pluginUrls1.size());
		for (URL url : pluginUrls1) {
			logger.fine("plugins <1>: " + url.toString());
		}

		// Null constructor argument specifies the default configuration
		String nullConfig = null;
		PluginDiscovery pd2 = new EmbeddedPluginDiscovery(nullConfig);
		Set<URL> pluginUrls2 = pd2.getPluginUrls();
		assertTrue(pluginUrls2 != null);
		assertTrue(!pluginUrls2.isEmpty());
		logger.fine("plugins <2>: " + pluginUrls2.size());
		for (URL url : pluginUrls2) {
			logger.fine("plugins <2>: " + url.toString());
		}

		// Explicit "default" argument specifies the default configuration
		String explicitConfig =
			EmbeddedPluginDiscovery.DEFAULT_PLUGIN_CONFIGURATION;
		PluginDiscovery pd3 = new EmbeddedPluginDiscovery(explicitConfig);
		Set<URL> pluginUrls3 = pd3.getPluginUrls();
		assertTrue(pluginUrls3 != null);
		assertTrue(!pluginUrls3.isEmpty());
		logger.fine("plugins <3>: " + pluginUrls3.size());
		for (URL url : pluginUrls3) {
			logger.fine("plugins <3>: " + url.toString());
		}

		assertTrue(pluginUrls1.equals(pluginUrls2));
		assertTrue(pluginUrls2.equals(pluginUrls3));
	}

	/**
	 * This test relies on the System property
	 * {@link InstallablePluginDiscovery#INSTALLABLE_PLUGIN_DISCOVERY
	 * "cmInstallablePluginDiscovery"} being set to
	 * {@link EmbeddedPluginDiscovery 
	 * "com.choicemaker.e2.platform.pd.EmbeddedPluginDiscovery.EmbeddedPluginDiscovery"
	 * }.
	 * @see #configureEmbeddedPluginDiscovery()
	 */
	@Test
	public void testInstallableDefaultConfiguration() {

		// Get the expected plugins of the default configuration
		PluginDiscovery pdDefaultConfig = new EmbeddedPluginDiscovery();
		Set<URL> expectedPlugins = pdDefaultConfig.getPluginUrls();
		assertTrue(expectedPlugins != null);
		assertTrue(!expectedPlugins.isEmpty());
		logger.fine("plugins <4>: " + expectedPlugins.size());
		for (URL url : expectedPlugins) {
			logger.fine("plugins <4>: " + url.toString());
		}

		PluginDiscovery pd = InstallablePluginDiscovery.getInstance();
		Set<URL> pluginUrls = pd.getPluginUrls();
		assertTrue(pluginUrls != null);
		assertTrue(!pluginUrls.isEmpty());
		logger.fine("plugins <5>: " + pluginUrls.size());
		for (URL url : pluginUrls) {
			logger.fine("plugins <5>: " + url.toString());
		}

		assertTrue(expectedPlugins.equals(pluginUrls));
	}

}
