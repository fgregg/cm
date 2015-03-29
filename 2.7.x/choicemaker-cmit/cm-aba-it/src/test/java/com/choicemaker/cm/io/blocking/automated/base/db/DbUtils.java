package com.choicemaker.cm.io.blocking.automated.base.db;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.xmlconf.XmlParserFactory;

public class DbUtils {

	private static final Logger logger = Logger.getLogger(DbUtils.class
			.getName());

	static final String SQLSERVER_CONFIG_FILE = "sqlserver-configuration.xml";
	
	static final String TARGET_NAME = "localhost";

//	static Set<String> getExpectedExtensionPoints() {
//		Set<String> retVal = new HashSet<>();
//		retVal.add(uid("matcherBlockingToolkit"));
//		retVal.add(uid("mlTrainGuiPlugin"));
//		retVal.add(uid("mrpsReaderGui"));
//		retVal.add(uid("pluggableController"));
//		retVal.add(uid("pluggableMenuItem"));
//		retVal.add(uid("rsReaderGui"));
//		retVal.add(uid("toolMenuItem"));
//		return retVal;
//	}
//
//	static Set<ExtensionDeclaration> getExpectedExtensions() {
//		Set<ExtensionDeclaration> retVal = new HashSet<>();
//		retVal.add(new ExtensionDeclaration(uid("ModelMaker"),
//				"com.choicemaker.e2.applications"));
//		retVal.add(new ExtensionDeclaration(uid("AllBlocker"),
//				"com.choicemaker.cm.modelmaker.matcherBlockingToolkit"));
//		retVal.add(new ExtensionDeclaration(uid("NoMachineLearningGui"),
//				"com.choicemaker.cm.modelmaker.mlTrainGuiPlugin"));
//		return retVal;
//	}

	static Document readConfigurationFileFromResource(String path)
			throws XmlConfException, JDOMException, IOException {
		SAXBuilder builder = XmlParserFactory.createSAXBuilder(false);
		URL url = DbUtils.class.getClassLoader().getResource(path);
		if (url == null) {
			throw new XmlConfException("Unable to load '" + path + "'");
		}
		Document document = builder.build(url);
		return document;
	}

//	static String[] getModelMakerRunArgs() throws URISyntaxException {
//		URL configURL = DbUtils.class.getResource(SQLSERVER_CONFIG_FILE);
//		if (configURL == null) {
//			String msg = "Invalid configuration path: " + SQLSERVER_CONFIG_FILE;
//			logger.severe(msg);
//			fail(msg);
//		}
//		logger.info("configURL: " + configURL.toString());
//		URI configURI = configURL.toURI();
//		File configFile = new File(configURI);
//		assertTrue(configFile.toString() + " doesn't exist",
//				configFile.exists());
//		assertTrue(configFile.toString() + " isn't readable",
//				configFile.canRead());
//		String configPath = configFile.getAbsolutePath();
//		String[] retVal = new String[] {
//				CONFIGURATION_ARG, configPath };
//		return retVal;
//	}

//	/**
//	 * Returns a unique identifier given the id specified in the ModelMaker
//	 * plugin descriptor
//	 */
//	static String uid(String id) {
//		return DbUtils.MM_PLUGIN_ID + "." + id;
//	}

	private DbUtils() {
	}

}
