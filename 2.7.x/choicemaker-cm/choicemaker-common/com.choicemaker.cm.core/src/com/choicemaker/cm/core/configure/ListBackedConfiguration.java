package com.choicemaker.cm.core.configure;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.tools.ant.util.FileUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.MachineLearner;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.compiler.ICompiler;
import com.choicemaker.cm.core.compiler.InstallableCompiler;
import com.choicemaker.cm.core.util.FileUtilities;
import com.choicemaker.cm.core.xmlconf.XmlParserFactory;

class ListBackedConfiguration implements ChoiceMakerConfiguration {

	private static final Logger logger = Logger
			.getLogger(ListBackedConfiguration.class.getName());

	private static final FileUtils FILE_UTILS = FileUtils.newFileUtils();

	public static final String DEFAULT_CODE_ROOT = "etc/models/gen";

	public static final String CONFIGURATION_GENERATOR_ELEMENT = "generator";

	public static final String CONFIGURATION_CODE_ROOT = "codeRoot";

	public static final String CONFIGURATION_CORE_ELEMENT = "core";

	public static final String CONFIGURATION_CLASSPATH_ELEMENT = "classpath";

	public static final String CONFIGURATION_WORKINGDIR_ELEMENT = "workingDir";

	public static final String CONFIGURATION_RELOAD_ELEMENT = "reload";

	public static final String SYSTEM_USER_DIR = "user.dir";

	public static final String SYSTEM_JAVA_CLASS_PATH = "java.class.path";

	/** An absolute path to the configuration file */
	private final String filePath;

	/** The working directory for the configuration */
	private final File workingDirectory;

	/** The class path */
	private final String classpath;

	/** The directory where generated code is created */
	private final String codeRoot;

	/**
	 *
	 * @param filePath
	 *            an absolute or relative path to the configuration file name.
	 * @throws XmlConfException
	 */
	public ListBackedConfiguration(String fileName) throws XmlConfException {
		this.filePath = new File(fileName).getAbsolutePath();

		Document document = readConfigurationFile(this.filePath);
		this.workingDirectory = getWorkingDirectory(this.filePath, document);
		System.setProperty(SYSTEM_USER_DIR, this.workingDirectory.toString());
		this.classpath = getClassPath(this.workingDirectory, document);
		this.codeRoot = getCodeRoot(this.workingDirectory, document);
	}

	private Document readConfigurationFile(String fileName)
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

	File getWorkingDirectory(String configFile, Document document)
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

	public Element getRoot(Document document) {
		return document.getRootElement();
	}

	public Element getCore(Document document) {
		return getRoot(document).getChild(CONFIGURATION_CORE_ELEMENT);
	}

	protected String getClassPath(File wdir, Document document)
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
			logger.error("Problem with classpath", ex);
			throw new XmlConfException(ex.toString(), ex);
		}
		return res;
	}

	public String getCodeRoot(File wdir, Document document) throws XmlConfException {
		String retVal = null;
		try {
			File f = FileUtilities.resolveFile(wdir,DEFAULT_CODE_ROOT);
			Element e = getCore(document);
			if (e != null) {
				e = e.getChild(CONFIGURATION_GENERATOR_ELEMENT);
				if (e != null) {
					String t = e.getAttributeValue(CONFIGURATION_CODE_ROOT);
					if (t != null) {
						f = FileUtilities.resolveFile(wdir,t);
					}
				}
			}
			retVal = f.getAbsolutePath();
		} catch (IOException e1) {
			logger.error("Problem with code root", e1);
			throw new XmlConfException(e1.toString(), e1);
		}

		return retVal;
	}

	public String getFileName() {
		return filePath;
	}

	public MachineLearnerPersistence getMachineLearnerPersistence(
			MachineLearner model) {
		// FIXME non-functional method stub
		throw new Error("not yet implemented");
	}

	public ProbabilityModelPersistence getModelPersistence(
			ImmutableProbabilityModel model) {
		// FIXME non-functional method stub
		throw new Error("not yet implemented");
	}

	public List getProbabilityModelConfigurations() {
		// FIXME non-functional method stub
		throw new Error("not yet implemented");
	}

	public ClassLoader getClassLoader() {
		return ListBackedConfiguration.class.getClassLoader();
	}

	public ClassLoader getRmiClassLoader() {
		return getClassLoader();
	}

	public String getClassPath() {
		return this.classpath;
	}

	public String getReloadClassPath() {
		// FIXME non-functional method stub
		throw new Error("not yet implemented");
	}

	public String getJavaDocClasspath() {
		// FIXME non-functional method stub
		throw new Error("not yet implemented");
	}

	public ICompiler getChoiceMakerCompiler() {
		return InstallableCompiler.getInstance();
	}

	public void reloadClasses() {
		// FIXME non-functional method stub
		throw new Error("not yet implemented");
	}

	public String toXml() {
		// FIXME non-functional method stub
		throw new Error("not yet implemented");
	}

	public File getWorkingDirectory() {
		return this.workingDirectory;
	}

	public String getCodeRoot() {
		return this.codeRoot;
	}

	public void deleteGeneratedCode() {
		File f = new File(getCodeRoot()).getAbsoluteFile();
		if (f.exists()) {
			logger.info("Deleting codeRoot('" + f.getAbsoluteFile() + "')");
			FileUtilities.removeDir(f);
		}
	}

}
