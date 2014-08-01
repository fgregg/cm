package com.choicemaker.cm.core.base;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.ProbabilityModelConfiguration;
import com.choicemaker.cm.core.XmlConfException;

/**
 * Constructs a model configuration instance from a model-weights file and the
 * names of a database and blocking configuration that are valid for the model
 * 
 * @author rphall
 * 
 */
public class BasicModelConfiguration implements ProbabilityModelConfiguration {

	private final ImmutableProbabilityModel model;
	private final String dbConfigName;
	private final String blockingConfigName;

	public BasicModelConfiguration(String modelPath, String dbConfigName,
			String blockingConfigName) throws XmlConfException {
		this.model = loadModel(modelPath);
		this.dbConfigName = validateDatabaseConfiguration(model, dbConfigName);
		this.blockingConfigName =
			validateBlockingConfiguration(model, blockingConfigName);
	}

	public ImmutableProbabilityModel getProbabilityModel() {
		return this.model;
	}

	public String getDatabaseConfigurationName() {
		return this.dbConfigName;
	}

	public String getBlockingConfigurationName() {
		return this.blockingConfigName;
	}

	private static ImmutableProbabilityModel loadModel(String modelPath)
			throws XmlConfException {
		throw new Error("not yet implemented");
	}

	private static String validateDatabaseConfiguration(
			ImmutableProbabilityModel model, String dbConfigName)
			throws XmlConfException {
		throw new Error("not yet implemented");
	}

	private static String validateBlockingConfiguration(
			ImmutableProbabilityModel model, String blockingConfigName)
			throws XmlConfException {
		throw new Error("not yet implemented");
	}

}
