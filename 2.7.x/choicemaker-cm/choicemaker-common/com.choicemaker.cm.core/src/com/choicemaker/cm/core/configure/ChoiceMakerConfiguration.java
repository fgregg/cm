package com.choicemaker.cm.core.configure;

import java.io.File;
import java.util.List;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.MachineLearner;
import com.choicemaker.cm.core.XmlConfException;
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

	void deleteGeneratedCode() throws UnsupportedOperationException;

	ICompiler getChoiceMakerCompiler();

	ClassLoader getClassLoader();

	String getClassPath();

	/** The root directory for ClueMaker model, clue and schema files */
	String getClueMakerSourceRoot();

	/** The root directory for generated Java source code files. */
	String getGeneratedSourceRoot();

	/** The root directory for compiled Java class files. */
	String getCompiledCodeRoot();

	/**
	 * The root directory for packaged code such as model
	 * JAR files, Holder Class JAR files, JavaDoc Zip files, and
	 * SQL scripts.
	 */
	String getPackagedCodeRoot();

	String getFileName();

	String getJavaDocClasspath();

	MachineLearnerPersistence getMachineLearnerPersistence(MachineLearner model);

	ProbabilityModelPersistence getModelPersistence(
			ImmutableProbabilityModel model);

	List getProbabilityModelConfigurations();

	String getReloadClassPath();

	ClassLoader getRmiClassLoader();

	File getWorkingDirectory();

	void reloadClasses() throws UnsupportedOperationException, XmlConfException;

	String toXml();

}
