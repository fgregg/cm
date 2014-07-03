package com.choicemaker.cm.core.configure;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.tools.ant.util.FileUtils;
import org.jdom.Document;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.MachineLearner;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.compiler.ICompiler;
import com.choicemaker.cm.core.compiler.InstallableCompiler;
import com.choicemaker.cm.core.util.FileUtilities;

class ListBackedConfiguration implements ChoiceMakerConfiguration {

	private static final FileUtils FILE_UTILS = FileUtils.newFileUtils();

	private static final Logger logger = Logger
			.getLogger(ListBackedConfiguration.class.getName());

	/** The class path */
	private final String classpath;

	/** The directory where generated code is created */
	private final String codeRoot;

	/** An absolute path to the configuration file */
	private final String filePath;

	/** The working directory for the configuration */
	private final File workingDirectory;

	/**
	 *
	 * @param filePath
	 *            an absolute or relative path to the configuration file name.
	 * @throws XmlConfException
	 */
	public ListBackedConfiguration(String fileName) throws XmlConfException {
		this.filePath = new File(fileName).getAbsolutePath();

		Document document = ConfigurationUtils.readConfigurationFile(this.filePath);
		this.workingDirectory = ConfigurationUtils.getWorkingDirectory(this.filePath, document);
		System.setProperty(ConfigurationUtils.SYSTEM_USER_DIR, this.workingDirectory.toString());
		this.classpath = ConfigurationUtils.getClassPath(this.workingDirectory, document);
		this.codeRoot = ConfigurationUtils.getCodeRoot(this.workingDirectory, document);
	}

	public void deleteGeneratedCode() {
		File f = new File(getCodeRoot()).getAbsoluteFile();
		if (f.exists()) {
			logger.info("Deleting codeRoot('" + f.getAbsoluteFile() + "')");
			FileUtilities.removeDir(f);
		}
	}

	public ICompiler getChoiceMakerCompiler() {
		return InstallableCompiler.getInstance();
	}

	public ClassLoader getClassLoader() {
		return ListBackedConfiguration.class.getClassLoader();
	}

	public String getClassPath() {
		return this.classpath;
	}

	public String getCodeRoot() {
		return this.codeRoot;
	}

	public String getFileName() {
		return filePath;
	}

	public String getJavaDocClasspath() {
		return getClassPath();
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

	public String getReloadClassPath() {
		// FIXME non-functional method stub
		throw new Error("not yet implemented");
	}

	public ClassLoader getRmiClassLoader() {
		return getClassLoader();
	}

	public File getWorkingDirectory() {
		return this.workingDirectory;
	}

	public void reloadClasses() {
		// FIXME non-functional method stub
		throw new Error("not yet implemented");
	}

	public String toXml() {
		// FIXME non-functional method stub
		throw new Error("not yet implemented");
	}

}
