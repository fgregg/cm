package com.choicemaker.cmit.modelmaker.gui;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

import com.choicemaker.e2.embed.EmbeddedPlatform;
import com.choicemaker.e2.mbd.plugin.EmbeddedPluginDiscovery;
import com.choicemaker.e2.platform.InstallablePlatform;
import com.choicemaker.e2.plugin.InstallablePluginDiscovery;

class ModelMakerUtils {

	private static final Logger logger = Logger.getLogger(ModelMakerUtils.class
			.getName());

	private static final String RESOURCE_ROOT = "/";

	static final String RESOURCE_NAME_SEPARATOR = "/";

	static final String ECLIPSE_APPLICATION_DIRECTORY = RESOURCE_ROOT
			+ "eclipse.application.dir";

	static final String EXAMPLE_DIRECTORY =
		ECLIPSE_APPLICATION_DIRECTORY + RESOURCE_NAME_SEPARATOR
				+ "examples/simple_person_matching";

	static final String CONFIGURATION_FILE =
		"analyzer-configuration.xml";

	static final String CONFIGURATION_PATH = EXAMPLE_DIRECTORY
			+ RESOURCE_NAME_SEPARATOR + CONFIGURATION_FILE;

	static String CONFIGURATION_ARG = "-conf";

	static void assertEmbeddedPlatform() {
		String pv =
			System.getProperty(InstallablePlatform.INSTALLABLE_PLATFORM);
		assertTrue(EmbeddedPlatform.class.getName().equals(pv));
	}

	/** @see http://links.rph.cx/1r1vyFo */
	static void configureEclipseConsoleLogging() {
		Logger topLogger = java.util.logging.Logger.getLogger("");
		Handler consoleHandler = null;
		for (Handler handler : topLogger.getHandlers()) {
			if (handler instanceof ConsoleHandler) {
				// found the console handler
				consoleHandler = handler;
				break;
			}
		}
		if (consoleHandler == null) {
			consoleHandler = new ConsoleHandler();
			topLogger.addHandler(consoleHandler);
		}
		consoleHandler.setLevel(java.util.logging.Level.FINEST);
	}

	static Set<String> getExpectedExtensionPoints() {
		Set<String> retVal = new HashSet<>();
		retVal.add(ModelMakerUtils.uid("mrpsReaderGui"));
		retVal.add(ModelMakerUtils.uid("mlTrainGuiPlugin"));
		retVal.add(ModelMakerUtils.uid("rsReaderGui"));
		retVal.add(ModelMakerUtils.uid("matcherBlockingToolkit"));
		retVal.add(ModelMakerUtils.uid("toolMenuItem"));
		retVal.add(ModelMakerUtils.uid("pluggableMenuItem"));
		retVal.add(ModelMakerUtils.uid("pluggableController"));
		return retVal;
	}

	static Set<Extension> getExpectedExtensions() {
		Set<Extension> retVal = new HashSet<>();
		retVal.add(new Extension(ModelMakerUtils.uid("ModelMaker"),
				"com.choicemaker.e2.applications"));
		retVal.add(new Extension(ModelMakerUtils.uid("AllBlocker"),
				"com.choicemaker.cm.modelmaker.matcherBlockingToolkit"));
		retVal.add(new Extension(ModelMakerUtils.uid("NoMachineLearningGui"),
				"com.choicemaker.cm.modelmaker.mlTrainGuiPlugin"));
		return retVal;
	}

	static void installEmbeddedPlatform() {
		// Install the Embedded Platform
		String pn = InstallablePlatform.INSTALLABLE_PLATFORM;
		String pv = EmbeddedPlatform.class.getName();
		System.setProperty(pn, pv);
		pn = InstallablePluginDiscovery.INSTALLABLE_PLUGIN_DISCOVERY;
		pv = EmbeddedPluginDiscovery.class.getName();
		System.setProperty(pn, pv);
	}

	/**
	 * Returns a unique identifier given the id specified in the
	 * ModelMaker plugin descriptor
	 */
	static String uid(String id) {
		return ModelMakerIT.MM_PLUGIN_ID + "." + id;
	}

	private ModelMakerUtils() {
	}

	static String[] getModelMakerRunArgs() throws URISyntaxException {
		URL configURL = ModelMaker0IT.class.getResource(CONFIGURATION_PATH);
		if (configURL == null) {
			String msg = "Invalid configuration path: " + CONFIGURATION_PATH;
			logger.severe(msg);
			fail(msg);
		}
		logger.info("configURL: " + configURL.toString());
		URI configURI = configURL.toURI();
		File configFile = new File(configURI);
		assertTrue(configFile.toString() + " doesn't exist",
				configFile.exists());
		assertTrue(configFile.toString() + " isn't readable",
				configFile.canRead());
		String configPath = configFile.getAbsolutePath();
		String[] retVal = new String[] {
				CONFIGURATION_ARG, configPath };
		return retVal;
	}

}
