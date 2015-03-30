package com.choicemaker.demo.simple_person_matching;

import static com.choicemaker.cm.core.ProbabilityModelConfiguration.AN_BLOCKING_CONFIGURATION;
import static com.choicemaker.cm.core.ProbabilityModelConfiguration.AN_DATABASE_ABSTRACTION;
import static com.choicemaker.cm.core.ProbabilityModelConfiguration.AN_DATABASE_ACCESSOR;
import static com.choicemaker.cm.core.ProbabilityModelConfiguration.AN_DATABASE_CONFIGURATION;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.choicemaker.cm.core.ChoiceMakerExtensionPoint;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.ModelConfigurationException;
import com.choicemaker.cm.core.ProbabilityModelConfiguration;
import com.choicemaker.cm.core.base.DefaultProbabilityModelManager;
import com.choicemaker.cm.core.base.MutableProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.core.compiler.DoNothingCompiler;
import com.choicemaker.cm.core.compiler.ICompiler;
import com.choicemaker.cm.core.xmlconf.ProbabilityModelsXmlConf;
import com.choicemaker.e2.CMConfigurationElement;
import com.choicemaker.e2.CMExtension;
import com.choicemaker.e2.CMPluginDescriptor;
import com.choicemaker.e2.platform.CMPlatformUtils;
import com.choicemaker.e2.utils.ExtensionDeclaration;

/**
 * This class defines test routines, without annotating them as tests and
 * without defining any pre-Class or pre-Test configuration methods, so that the
 * routines can be reused within tests targeted for embedded, standard or ejb
 * platforms. <br/>
 * The tests must be included by reference, rather than inheritance, since this
 * class is marked as final.
 * 
 * @author rphall
 *
 */
public final class SimplePersonPluginTesting {

	private static final Logger logger = Logger
			.getLogger(SimplePersonPluginTesting.class.getName());

	public static final String AN_MODEL_FILE = "model";

	public static final String EXPECTED_BLOCKING_CONFIG = "defaultAutomated";

	public static final String EXPECTED_DATABASE_CONFIG = "default";

	private static final String[] _KNOWN_DATABASE_ABSTRACTIONS = new String[] {
			"com.choicemaker.cm.io.db.oracle.oracleDatabaseAbstraction",
			"com.choicemaker.cm.io.db.sqlserver.sqlServerDatabaseAbstraction" };

	private static final Set<String> _EXPECTED_DATABASE_ABSTRACTIONS =
		new HashSet<String>(Arrays.asList(_KNOWN_DATABASE_ABSTRACTIONS));

	public static final Set<String> EXPECTED_DATABASE_ABSTRACTIONS =
		Collections.unmodifiableSet(_EXPECTED_DATABASE_ABSTRACTIONS);

	private static final String[] _KNOWN_DATABASE_ACCESSORS = new String[] {
			"com.choicemaker.cm.io.db.oracle.oracleDatabaseAccessor",
			"com.choicemaker.cm.io.db.sqlserver.sqlServerDatabaseAccessor" };

	private static final Set<String> _EXPECTED_DATABASE_ACCESSORS =
		new HashSet<String>(Arrays.asList(_KNOWN_DATABASE_ACCESSORS));

	public static final Set<String> EXPECTED_DATABASE_ACCESSORS = Collections
			.unmodifiableSet(_EXPECTED_DATABASE_ACCESSORS);

	public static final String SP_PLUGIN_ID =
		"com.choicemaker.cm.simplePersonMatching";

	public static Set<ExtensionDeclaration> getExpectedExtensions() {
		Set<ExtensionDeclaration> retVal = new HashSet<>();
		retVal.add(new ExtensionDeclaration(uid("Model1"),
				"com.choicemaker.cm.core.modelConfiguration"));
		retVal.add(new ExtensionDeclaration(uid("Model2"),
				"com.choicemaker.cm.core.modelConfiguration"));
		return retVal;
	}

	/**
	 * Returns a unique identifier given the id specified in the ModelMaker
	 * plugin descriptor
	 */
	public static String uid(String id) {
		return SP_PLUGIN_ID + "." + id;
	}

	public static void testLoadSimplePersonModels() {
		// Note: this method duplicates many of the steps of
		// PMManager.loadModelPlugins(), but checks assertions for
		// intermediate results.
		
		CMExtension[] exts = CMPlatformUtils.getPluginExtensions(SP_PLUGIN_ID);
		assertTrue(exts != null);
		for (CMExtension ext : exts) {
			CMConfigurationElement[] cmces = ext.getConfigurationElements();
			assertTrue(cmces != null);
			assertTrue(cmces.length == 1);
			CMConfigurationElement cmce = cmces[0];

			String[] attributeNames = cmce.getAttributeNames();
			assertTrue(attributeNames != null);
			assertTrue(attributeNames.length == 5);

			String dbAbstraction = cmce.getAttribute(AN_DATABASE_ABSTRACTION);
			assertTrue(dbAbstraction != null);
			assertTrue(EXPECTED_DATABASE_ABSTRACTIONS.contains(dbAbstraction));

			String dbAccessor = cmce.getAttribute(AN_DATABASE_ACCESSOR);
			assertTrue(dbAccessor != null);
			assertTrue(EXPECTED_DATABASE_ACCESSORS.contains(dbAccessor));

			String dbConfig = cmce.getAttribute(AN_DATABASE_CONFIGURATION);
			assertTrue(dbConfig != null);
			assertTrue(EXPECTED_DATABASE_CONFIG.equals(dbConfig));

			String blockConfig = cmce.getAttribute(AN_BLOCKING_CONFIGURATION);
			assertTrue(blockConfig != null);
			assertTrue(EXPECTED_BLOCKING_CONFIG.equals(blockConfig));

			String modelFile = cmce.getAttribute(AN_MODEL_FILE);
			assertTrue(modelFile != null);

			CMPluginDescriptor plugin =
				CMPlatformUtils.getPluginDescriptor(SP_PLUGIN_ID);
			URL installURL = plugin.getInstallURL();
			assertTrue(installURL != null);

			URL modelURL = null;
			try {
				modelURL = new URL(installURL, modelFile);
			} catch (MalformedURLException e) {
				String msg =
					"Failed to create URL from the plugin installURL ("
							+ installURL + ") and the model path (" + modelFile
							+ "): " + e.toString();
				fail(msg);
			}
			assertTrue(modelURL != null);

			// Read in the model
			final ICompiler compiler = new DoNothingCompiler();
			final ClassLoader cl =
				SimplePersonPluginTesting.class.getClassLoader();
			IProbabilityModel pm = null;
			try {
				final InputStream is = modelURL.openStream();
				final StringWriter compilerMessages = new StringWriter();
				final boolean allowCompile = false;
				pm =
					ProbabilityModelsXmlConf.readModel(modelFile, is, compiler,
							compilerMessages, cl, allowCompile);
				logger.fine("Compiler messages: " + compilerMessages.toString());
				pm.setModelFilePath(modelFile);
			} catch (Exception e) {
				String msg =
					"Failed to load a model from the plugin installURL ("
							+ installURL + ") and the model path (" + modelFile
							+ "): " + e.toString();
				fail(msg);
			}
			assertTrue(pm != null);

			// Check that some stuff is computed correctly
			final String computedModelPath = pm.getModelFilePath();
			assertTrue(modelFile.equals(computedModelPath));
			// Model properties are deprecated
			// final Map<?, ?> computedModelProperties = pm.properties();
			// assertTrue(computedModelProperties != null);
			// assertTrue(computedModelProperties.isEmpty());

			// Register the model with the Probability Model Manager
			PMManager.addModel(pm);

			// Check that the model is registered. This also checks that
			// the model name is set correctly.
			final String computedModelName = pm.getModelName();
			assertTrue(computedModelName != null);
			ImmutableProbabilityModel ipm =
				PMManager.getImmutableModelInstance(computedModelName);
			assertTrue(ipm != null);
			// Should be the identical object right now to the memory location
			assertTrue(pm == ipm);
		}
	}

	public static void testSimplePersonPluginExtensions() {
		Set<ExtensionDeclaration> expected = getExpectedExtensions();
		CMExtension[] exts = CMPlatformUtils.getPluginExtensions(SP_PLUGIN_ID);
		assertTrue(exts != null);
		assertTrue(exts.length == expected.size());
		Set<ExtensionDeclaration> computed = new HashSet<>();
		for (CMExtension ext : exts) {
			computed.add(new ExtensionDeclaration(ext));
		}
		assertTrue(computed.containsAll(expected));
	}

}
