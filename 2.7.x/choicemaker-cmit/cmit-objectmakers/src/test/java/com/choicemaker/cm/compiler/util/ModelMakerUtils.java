package com.choicemaker.cm.compiler.util;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.choicemaker.e2.utils.ExtensionDeclaration;

class ModelMakerUtils {

	private static final Logger logger = Logger.getLogger(ModelMakerUtils.class
			.getName());

	private static final String RESOURCE_ROOT = "/";

	static final String RESOURCE_NAME_SEPARATOR = "/";

	static final String ECLIPSE_APPLICATION_DIRECTORY = RESOURCE_ROOT
			+ "eclipse.application.dir";

	static final String EXAMPLE_DIRECTORY = ECLIPSE_APPLICATION_DIRECTORY
			+ RESOURCE_NAME_SEPARATOR + "examples/simple_person_matching";

	static final String CONFIGURATION_FILE = "analyzer-configuration.xml";

	static final String CONFIGURATION_PATH = EXAMPLE_DIRECTORY
			+ RESOURCE_NAME_SEPARATOR + CONFIGURATION_FILE;

	static String CONFIGURATION_ARG = "-conf";

	static Set<String> getExpectedExtensionPoints() {
		Set<String> retVal = new HashSet<>();
		retVal.add(uid("matcherBlockingToolkit"));
		retVal.add(uid("mlTrainGuiPlugin"));
		retVal.add(uid("mrpsReaderGui"));
		retVal.add(uid("pluggableController"));
		retVal.add(uid("pluggableMenuItem"));
		retVal.add(uid("rsReaderGui"));
		retVal.add(uid("toolMenuItem"));
		return retVal;
	}

	static Set<ExtensionDeclaration> getExpectedExtensions() {
		Set<ExtensionDeclaration> retVal = new HashSet<>();
		retVal.add(new ExtensionDeclaration(uid("ModelMaker"),
				"com.choicemaker.e2.applications"));
		retVal.add(new ExtensionDeclaration(uid("AllBlocker"),
				"com.choicemaker.cm.modelmaker.matcherBlockingToolkit"));
		retVal.add(new ExtensionDeclaration(uid("NoMachineLearningGui"),
				"com.choicemaker.cm.modelmaker.mlTrainGuiPlugin"));
		return retVal;
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

	/**
	 * Returns a unique identifier given the id specified in the ModelMaker
	 * plugin descriptor
	 */
	static String uid(String id) {
		return ModelMakerIT.MM_PLUGIN_ID + "." + id;
	}

	private ModelMakerUtils() {
	}

}
