package com.choicemaker.cm.compiler.app;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.choicemaker.cm.core.PropertyNames;
import com.choicemaker.cm.core.WellKnownPropertyValues;
import com.choicemaker.cm.core.compiler.CompilationArguments;
import com.choicemaker.cm.core.compiler.ICompiler;
import com.choicemaker.cm.core.compiler.InstallableCompiler;

/**
 * Hello world!
 *
 */
public class Main {

	public static final int ARG_CONFIGURATION_FILE = 0;
	public static final int ARG_CLUE_FILE = 1;
	public static final int ARG_LOG_CONFIGURATION_NAME = 2;
	public static final String NO_LOG_CONFIGURATION = "--log-configuration=none";
	public static final int MIN_ARG_COUNT = 3;

	public static final boolean RELOAD = false;
	public static final boolean INITGUI = false;

	public static void main(String[] args) throws Exception {
		if (args.length < MIN_ARG_COUNT) {
			throw new IllegalArgumentException("missing arguments");
		}

		String fileName = args[ARG_CONFIGURATION_FILE];
		File configFile = new File(fileName);
		if (!configFile.exists()) {
			throw new IllegalArgumentException(
					"non-existent configuration file");
		}

		fileName = args[ARG_CLUE_FILE];
		File clueFile = new File(fileName);
//		if (!clueFile.exists()) {
//			throw new IllegalArgumentException("non-existent clue file");
//		}

		String logConfig = args[ARG_LOG_CONFIGURATION_NAME].trim();
		if (logConfig.isEmpty()) {
			throw new IllegalArgumentException(
					"blank name for the log configuration");
		} else if (logConfig.equals(NO_LOG_CONFIGURATION)) {
			logConfig = null;
		}

		// All arguments after the required arguments are Additional Compilation
		// Arguments (aca)
		int acaCount = args.length - MIN_ARG_COUNT;
		String[] additionalCompilationArgs = new String[acaCount];
		System.arraycopy(args, 1, additionalCompilationArgs, 0, acaCount);

		// Configure ChoiceMaker
		System.setProperty(
				PropertyNames.INSTALLABLE_CHOICEMAKER_CONFIGURATOR,
				WellKnownPropertyValues.LIST_BACKED_CONFIGURATOR);
		System.setProperty(
				PropertyNames.INSTALLABLE_GENERATOR_PLUGIN_FACTORY,
				WellKnownPropertyValues.LIST_BACKED_GENERATOR_PLUGIN_FACTORY);
		System.setProperty(
				PropertyNames.INSTALLABLE_COMPILER,
				WellKnownPropertyValues.BASIC_COMPILER);

		// Get the configured compiler
		ICompiler compiler = InstallableCompiler.getInstance();
		if (compiler == null) {
			throw new IllegalStateException("null compiler");
		}

		// Initialize ChoiceMaker configuration
		ChoiceMakerInit.initialize(configFile, logConfig, RELOAD, INITGUI);

		// Compute the compilation arguments
		CompilationArguments arguments = new CompilationArguments();
		String[] configSpecific = CompilerConfig
				.getCompilerArguments(clueFile);
		int i = arguments.enter(configSpecific);
		assert i == -1;
		if (additionalCompilationArgs.length > 0) {
			i = arguments.enter(additionalCompilationArgs);
			assert i == -1;
		}
		String[] files = arguments.files();
		assert files.length == 1;

		// Compile the target clue file
//		final Writer w = new StringWriter();
		Writer w = new BufferedWriter(new OutputStreamWriter(System.out));
		ChoiceMakerInit.deleteGeneratedCode();
		String accessorName = compiler.compile(arguments, w);
		System.out.println(w.toString());
		assert accessorName != null;
		if (accessorName == null) {
			System.err.println("Error: compilation failed");
		} else {
			System.out.println("SUCCESS: accessor == '" + accessorName + "'");
		}
	}
}
