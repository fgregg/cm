package com.choicemaker.cm.core.configure;

import java.util.List;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.MachineLearner;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.compiler.ICompiler;

/**
 * A flyweight class (no instance data) that provides standard methods for
 * working with an InstallableConfigurator and the
 * InstalledConfiguration.
 *
 * @author rphall
 *
 */
public class ConfigurationManager {

	private static final Logger logger = Logger
			.getLogger(ConfigurationManager.class.getName());

	private static final ConfigurationManager instance = new ConfigurationManager();

	public static final ConfigurationManager getInstance() {
		return instance;
	}
	
	public static void install(ChoiceMakerConfigurator configurator) {
		InstallableConfigurator.getInstance().install(configurator);
	}

	private boolean isInitialized() {
		return InstalledConfiguration.getInstance().hasDelegate();
	}

	private InstalledConfiguration getConfiguration() {
		return InstalledConfiguration.getInstance();
	}

	private ChoiceMakerConfigurator getConfigurator() {
		return InstallableConfigurator.getInstance();
	}

	public ProbabilityModelPersistence getModelPersistence(
			ImmutableProbabilityModel model) {
		return getConfiguration().getModelPersistence(model);
	}

	public MachineLearnerPersistence getMachineLearnerPersistence(
			MachineLearner model) {
		return getConfiguration().getMachineLearnerPersistence(model);
	}

	public ClassLoader getClassLoader() {
		return getConfiguration().getClassLoader();
	}

	public ClassLoader getRmiClassLoader() {
		return getConfiguration().getRmiClassLoader();
	}

	public List getProbabilityModelConfigurations() {
		return getConfiguration().getProbabilityModelConfigurations();
	}

	public String getClassPath() {
		return getConfiguration().getClassPath();
	}

	public String getReloadClassPath() {
		return getConfiguration().getReloadClassPath();
	}

	public String getJavaDocClasspath() {
		return getConfiguration().getJavaDocClasspath();
	}

	public String getSourceCodeRoot() {
		return getConfiguration().getSourceCodeRoot();
	}

	public String getCompiledCodeRoot() {
		return getConfiguration().getCompiledCodeRoot();
	}

	public String getPackagedCodeRoot() {
		return getConfiguration().getPackagedCodeRoot();
	}

	public ICompiler getChoiceMakerCompiler() {
		return getConfiguration().getChoiceMakerCompiler();
	}

	public void reloadClasses() throws XmlConfException {
		getConfiguration().reloadClasses();
	}

	public void deleteGeneratedCode() {
		getConfiguration().deleteGeneratedCode();
	}

	public String toXml() {
		return getConfiguration().toXml();
	}

	public String getFileName() {
		return getConfiguration().getFileName();
	}

	public void init() throws XmlConfException {
		if (isInitialized()) {
			logger.warn("Already initialized");
		}
		ChoiceMakerConfiguration cmc = getConfigurator().init();
		getConfiguration().setDelegate(cmc);

		// Postcondition
		assert isInitialized();
	}

	public void init(String fn, boolean reload, boolean initGui)
			throws XmlConfException {
		if (isInitialized()) {
			logger.warn("Already initialized");
		}
		ChoiceMakerConfiguration cmc = getConfigurator().init(fn, reload,
				initGui);
		getConfiguration().setDelegate(cmc);

		// Postcondition
		assert isInitialized();
	}

	public void init(String fn, String log4jConfName, boolean reload,
			boolean initGui) throws XmlConfException {
		if (isInitialized()) {
			logger.warn("Already initialized");
		}
		ChoiceMakerConfiguration cmc = getConfigurator().init(fn,
				log4jConfName, reload, initGui);
		getConfiguration().setDelegate(cmc);

		// Postcondition
		assert isInitialized();
	}

}
