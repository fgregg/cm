package com.choicemaker.cm.core;

/**
 * Commonly used values of the system-wide properties that are used to configure
 * ChoiceMaker. These values should never be changed, but may be deprecated in
 * favor of updated values that are defined as additional manifest constants in
 * this interface.
 */
public interface WellKnownPropertyValues {

	/**
	 * Specifies a basic version of the ChoiceMaker compiler appropriate for
	 * embedding into JAR files, where the classpath used to compile is
	 * specified by the application that invokes the compiler.
	 * @see PropertyNames#INSTALLABLE_COMPILER
	 */
	public static final String BASIC_COMPILER = "com.choicemaker.cm.compiler.impl.Compiler26";

	/**
	 * Specifies an Eclipse 2 backed version of the ChoiceMaker compiler, where
	 * the classpath used to compile ClueMaker files is augmented by the plugin
	 * descriptors of a Eclipse-2 registry.
	 * @see PropertyNames#INSTALLABLE_COMPILER
	 */
//	public static final String ECLIPSE2_COMPILER = "com.choicemaker.cm.compiler.impl.Eclipse2BackedCompiler";
	public static final String ECLIPSE2_COMPILER = BASIC_COMPILER;

	/**
	 * Specifies an Eclipse 2 backed version of a factory for generator plugins.
	 * @see PropertyNames#INSTALLABLE_GENERATOR_PLUGIN_FACTORY
	 */
	public static final String ECLIPSE2_GENERATOR_PLUGIN_FACTORY = "com.choicemaker.cm.core.gen.Eclipse2GeneratorPluginFactory";

	/**
	 * Specifies a list-backed version of a factory for generator plugins.
	 * @see PropertyNames#INSTALLABLE_GENERATOR_PLUGIN_FACTORY
	 */
	public static final String LIST_BACKED_GENERATOR_PLUGIN_FACTORY = "com.choicemaker.cm.core.gen.ListBackedGeneratorPluginFactory";

	/**
	 * Specifies an Eclipse 2 backed version of a ChoiceMaker configurator, suitable for desktop
	 * applications like CM Analyzer.
	 * @see PropertyNames#INSTALLABLE_CHOICEMAKER_CONFIGURATOR
	 */
	public static final String ECLIPSE2_CONFIGURATOR = "com.choicemaker.cm.core.xmlconf.XmlConfigurator";

	/**
	 * Specifies an embeddable Eclipse 2 backed version of a ChoiceMaker configurator, suitable for J2EE
	 * applications like CM Server.
	 * @see PropertyNames#INSTALLABLE_CHOICEMAKER_CONFIGURATOR
	 */
	public static final String EMBEDDED_ECLIPSE2_CONFIGURATOR = "com.choicemaker.cm.core.xmlconf.EmbeddedXmlConfigurator";

	/**
	 * Specifies a list backed version of a ChoiceMaker configurator, suitable for plain-old Java
	 * applications.
	 * @see PropertyNames#INSTALLABLE_CHOICEMAKER_CONFIGURATOR
	 */
	public static final String LIST_BACKED_CONFIGURATOR = "com.choicemaker.cm.core.configure.ListBackedConfigurator";

}
