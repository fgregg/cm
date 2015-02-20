package com.choicemaker.cm.core.configure;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.xmlconf.XmlParserFactory;
import com.choicemaker.util.FileUtilities;

public class ConfigurationUtils {

	private ConfigurationUtils() {
	}

	private static final Logger logger = Logger
			.getLogger(ConfigurationUtils.class.getName());

//	private static final FileUtils FILE_UTILS = FileUtils.newFileUtils();

	public static final String DEFAULT_CODE_ROOT = "etc/models/gen";
	public static final String CONFIGURATION_GENERATOR_ELEMENT = "generator";
	public static final String CONFIGURATION_CODE_ROOT = "codeRoot";
	public static final String CONFIGURATION_CORE_ELEMENT = "core";
	public static final String CONFIGURATION_CLASSPATH_ELEMENT = "classpath";
	public static final String CONFIGURATION_WORKINGDIR_ELEMENT = "workingDir";
	public static final String CONFIGURATION_RELOAD_ELEMENT = "reload";
	public static final String CONFIGURATION_MODULE_ELEMENT = "module";
	public static final String SYSTEM_USER_DIR = "user.dir";
	public static final String SYSTEM_JAVA_CLASS_PATH = "java.class.path";

	public static Document readConfigurationFile(String fileName)
			throws XmlConfException {
		Document document = null;
		SAXBuilder builder = XmlParserFactory.createSAXBuilder(false);
		try {
			document = builder.build(fileName);
		} catch (Exception ex) {
			throw new XmlConfException("Internal error.", ex);
		}
		assert document != null;
		return document;
	}

	public static File getWorkingDirectory(String configFile, Document document)
			throws XmlConfException {
		File wdir = new File(configFile).getAbsoluteFile().getParentFile();
		Element e = getCore(document);
		if (e != null) {
			e = e.getChild(CONFIGURATION_WORKINGDIR_ELEMENT);
			if (e != null) {
				wdir = FileUtilities.getAbsoluteFile(wdir, e.getText());
			}
		}
		try {
			wdir = wdir.getCanonicalFile();
		} catch (IOException e1) {
			throw new XmlConfException(e1.toString(), e1);
		}
		return wdir;
	}

	public static Element getRoot(Document document) {
		return document.getRootElement();
	}

	public static Element getCore(Document document) {
		return getRoot(document).getChild(CONFIGURATION_CORE_ELEMENT);
	}

	public static List getModules(Document document) {
		return getCore(document).getChildren(CONFIGURATION_MODULE_ELEMENT);
	}

	public static List getReloadModules(Document document) {
		List retVal = Collections.EMPTY_LIST;
		Element e = getCore(document).getChild(CONFIGURATION_RELOAD_ELEMENT);
		if (e != null) {
			retVal = e.getChildren(CONFIGURATION_MODULE_ELEMENT);
		}
		return retVal;
	}

	public static String getClassPath(File wdir, Document document)
			throws XmlConfException {
		String res = System.getProperty(SYSTEM_JAVA_CLASS_PATH);
		try {
			Element e = getCore(document).getChild(
					CONFIGURATION_CLASSPATH_ELEMENT);
			if (e != null) {
				res += FileUtilities.toAbsoluteClasspath(wdir, e.getText());
			}
			e = getCore(document).getChild(CONFIGURATION_RELOAD_ELEMENT);
			if (e != null) {
				e = e.getChild(CONFIGURATION_CLASSPATH_ELEMENT);
				if (e != null) {
					res += FileUtilities.toAbsoluteClasspath(wdir, e.getText());
				}
			}
		} catch (IOException ex) {
			logger.severe("Problem with classpath: " + ex.toString());
			throw new XmlConfException(ex.toString(), ex);
		}
		return res;
	}

	public static String getCodeRoot(File wdir, Document document)
			throws XmlConfException {
		String retVal = null;
		File f = FileUtilities.resolveFile(wdir, DEFAULT_CODE_ROOT);
		Element e = getCore(document);
		if (e != null) {
			e = e.getChild(CONFIGURATION_GENERATOR_ELEMENT);
			if (e != null) {
				String t = e.getAttributeValue(CONFIGURATION_CODE_ROOT);
				if (t != null) {
					f = FileUtilities.resolveFile(wdir, t);
				}
			}
		}
		retVal = f.getAbsolutePath();

		return retVal;
	}

}
