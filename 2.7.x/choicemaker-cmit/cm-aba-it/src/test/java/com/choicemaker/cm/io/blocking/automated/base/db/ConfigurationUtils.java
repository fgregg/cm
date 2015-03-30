package com.choicemaker.cm.io.blocking.automated.base.db;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.xmlconf.XmlParserFactory;

public class ConfigurationUtils {

	private static final Logger logger = Logger.getLogger(ConfigurationUtils.class
			.getName());

	static final String SQLSERVER_CONFIG_FILE = "sqlserver-configuration.xml";
	
	static final String TARGET_NAME = "localhost";

	static Document readConfigurationFromResource(String path)
			throws XmlConfException, JDOMException, IOException {
		SAXBuilder builder = XmlParserFactory.createSAXBuilder(false);
		URL url = ConfigurationUtils.class.getClassLoader().getResource(path);
		if (url == null) {
			String msg = "Unable to load '" + path + "'";
			logger.severe(msg);
			throw new XmlConfException(msg);
		}
		Document document = builder.build(url);
		return document;
	}

	private ConfigurationUtils() {
	}

}
