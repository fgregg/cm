package com.choicemaker.cm.core.base;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.ProbabilityModelConfiguration;
import com.choicemaker.cm.core.XmlConfException;

/**
 * Constructs a model configuration instance from a model-weights file and the
 * names of a database and blocking configuration that are valid for the model
 * 
 * @author rphall
 * @deprecated unused and only partially implemented
 */
public class BasicModelConfiguration implements ProbabilityModelConfiguration {

	private final ImmutableProbabilityModel model;
	private final String blockingConfigName;
	private final String dbAccessorName;
	private final String dbConfigName;

	public BasicModelConfiguration(String modelPath, String dbConfigName,
			String blockingConfigName, String dbAccessorName) throws XmlConfException {
		this.model = loadModel(modelPath);
		this.dbConfigName = validateDatabaseConfiguration(model, dbConfigName);
		this.dbAccessorName = validateDatabaseAccessor(model, dbAccessorName);
		this.blockingConfigName =
			validateBlockingConfiguration(model, blockingConfigName);
	}

	public ImmutableProbabilityModel getProbabilityModel() {
		return this.model;
	}

	public String getBlockingConfigurationName() {
		return this.blockingConfigName;
	}

	public String getDatabaseAccessorName() {
		return dbAccessorName;
	}

	public String getDatabaseConfigurationName() {
		return this.dbConfigName;
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

	private static String validateDatabaseAccessor(
			ImmutableProbabilityModel model, String dbAccessorName)
			throws XmlConfException {
		throw new Error("not yet implemented");
	}

	private static String validateBlockingConfiguration(
			ImmutableProbabilityModel model, String blockingConfigName)
			throws XmlConfException {
		throw new Error("not yet implemented");
	}

}
