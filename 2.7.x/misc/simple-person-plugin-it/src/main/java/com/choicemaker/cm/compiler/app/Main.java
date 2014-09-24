package com.choicemaker.cm.compiler.app;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.ProbabilityModelConfiguration;
import com.choicemaker.cm.core.PropertyNames;
import com.choicemaker.cm.core.WellKnownPropertyValues;
import com.choicemaker.cm.core.configure.ConfigurationManager;

/**
 * Hello world!
 *
 */
public class Main {

	public static final int ARG_CONFIGURATION_FILE = 0;
	public static final int MIN_ARG_COUNT = 1;

	public static final String NO_LOG_CONFIGURATION = "--log-configuration=none";
	public static final boolean RELOAD = false;
	public static final boolean INITGUI = false;
	
	private static final String INDENT = "  ";

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

		// Configure ChoiceMaker
		System.setProperty(
				PropertyNames.INSTALLABLE_CHOICEMAKER_CONFIGURATOR,
				WellKnownPropertyValues.LIST_BACKED_CONFIGURATOR);

		// Initialize ChoiceMaker configuration
		ChoiceMakerInit.initialize(configFile, null, RELOAD, INITGUI);
		ConfigurationManager cmgr = ConfigurationManager.getInstance();
		
		// List the model to standard output
		@SuppressWarnings("unchecked")
		List<ProbabilityModelConfiguration> models =  cmgr.getProbabilityModelConfigurations();
		for (ProbabilityModelConfiguration pmc : models) {

			final ImmutableProbabilityModel ipm = pmc.getProbabilityModel();

			String cluesetName = null;
			// String schemaName = null;
			String modelName = null;
			String cluesetSignature = null;
			String schemaSignature = null;
			String modelSignature = null;
			if (ipm != null) {
				cluesetName = ipm.getClueSetName();
				// schemaName = ipm.getSchemaName();
				modelName = ipm.getModelName();
				cluesetSignature = ipm.getClueSetSignature();
				schemaSignature = ipm.getSchemaSignature();
				modelSignature = ipm.getModelSignature();
			}
			final String blocking = pmc.getBlockingConfigurationName();
			final String database = pmc.getDatabaseConfigurationName();
			
			final PrintStream out = System.out;
			out.println("Model: " + modelName);
			out.println(INDENT + "       Model signature: " + modelSignature);
			out.println(INDENT + "         Clue set name: " + cluesetName);
			out.println(INDENT + "    Clue set signature: " + cluesetSignature);
			out.println(INDENT + "      Schema signature: " + schemaSignature);
			out.println(INDENT + "Blocking configuration: " + blocking);
			out.println(INDENT + "Database configuration: " + database);
			out.println();
		}
		
	}
}
