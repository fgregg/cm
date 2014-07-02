package com.choicemaker.cm.core.configure;

import java.io.File;
import java.util.List;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.MachineLearner;
import com.choicemaker.cm.core.compiler.ICompiler;

/**
 * A read-only bean that represents a ChoiceMaker configuration in a particular
 * application in a particular operating environment. The structure of the bean
 * is basically dictated by the structure of the so-called project.xml
 * configuration files that have been part of every ChoiceMaker deployment at
 * least since version 2.3.<br/>
 * <br/>
 * Configuration instances are created and initialized by a
 * {@link ChoiceMakerConfigurator configurator}. Configuration instances are
 * immutable. Once a configuration is initialized by a configurator, the
 * configuration can not be updated. However, new configurations may be created
 * which are based on the original configuration; see the various mutator
 * operations on the configurator interface. If a new configuration is created
 * by a mutator, the original configuration should be invalidated.<br/>
 * <br/>
 * The current ChoiceMaker configuration for an application may be obtained from
 * the singleton instance of the
 * {@link com.choicemaker.cm.core.install.InstalledConfiguration
 * InstalledConfiguration} class.
 *
 * @author rphall
 *
 */
public interface ChoiceMakerConfiguration {

	/**
	 * Name of a public, static, final field referencing a non-null
	 * ChoiceMakerConfiguration instance
	 */
	public static final String INSTANCE = "instance";

	String getFileName();

	MachineLearnerPersistence getMachineLearnerPersistence(MachineLearner model);

	ProbabilityModelPersistence getModelPersistence(
			ImmutableProbabilityModel model);

	List getProbabilityModelConfigurations();

	ClassLoader getClassLoader();

	ClassLoader getRmiClassLoader();

	String getClassPath();

	String getReloadClassPath();

	String getJavaDocClasspath();

	File getWorkingDirectory();

	ICompiler getChoiceMakerCompiler();

	void reloadClasses();

	void deleteGeneratedCode();

	String getCodeRoot();

	String toXml();

}
