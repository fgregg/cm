package com.choicemaker.cm.core;

/**
 * Names of system-wide properties used to configure
 * ChoiceMaker
 */
public interface PropertyNames {

	/**
	 * A property that specifies the FQCN of an installable compiler
	 */
	public static final String INSTALLABLE_COMPILER = "cmInstallableCompiler";

	/**
	 * A property that specifies the FQCN of an installable factory for generator plugins
	 */
	public static final String INSTALLABLE_GENERATOR_PLUGIN_FACTORY = "cmInstallableGeneratorPluginFactory";

	/**
	 * A property that specifies the FQCN of an installable manager for probability models
	 */
	public static final String INSTALLABLE_PROBABILITY_MODEL_MANAGER = "cmInstallableProbabilityModelManager";

	/**
	 * A property that specifies the FQCN of an installable manager for probability models
	 */
	public static final String INSTALLABLE_CHOICEMAKER_CONFIGURATOR = "cmInstallableConfigurator";

	/**
	 * A property that specifies the FQCN of an installable manager for probability models
	 */
	public static final String INSTALLABLE_MODEL_TRAINING_REGISTRY = "cmInstallableModelTrainingRegistry";

	/**
	 * A property that specifies the FQCN of an installable manager for probability models
	 */
	public static final String INSTALLABLE_RECORD_PAIR_CACHE = "cmInstallableRecordPairCache";

}
