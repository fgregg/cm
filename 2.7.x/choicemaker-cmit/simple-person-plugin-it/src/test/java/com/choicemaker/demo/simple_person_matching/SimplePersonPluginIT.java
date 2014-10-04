package com.choicemaker.demo.simple_person_matching;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Test;

import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.core.compiler.DoNothingCompiler;
import com.choicemaker.cm.core.compiler.ICompiler;
import com.choicemaker.cm.core.xmlconf.ProbabilityModelsXmlConf;
import com.choicemaker.e2.CMConfigurationElement;
import com.choicemaker.e2.CMExtension;
import com.choicemaker.e2.CMPluginDescriptor;
import com.choicemaker.e2.embed.EmbeddedPlatform;
import com.choicemaker.e2.platform.CMPlatformUtils;
import com.choicemaker.e2.utils.ExtensionDeclaration;

public class SimplePersonPluginIT {
	
	private static final Logger logger = Logger.getLogger(SimplePersonPluginIT.class.getName());

	private static final String AN_MODEL_FILE = "model";
	private static final String AN_DATABASE_CONFIGURATION =
		"databaseConfiguration";
	private static final String AN_BLOCKING_CONFIGURATION =
		"blockingConfiguration";

	private static final String EXPECTED_DATABASE_CONFIG = "default";
	private static final String EXPECTED_BLOCKING_CONFIG = "defaultAutomated";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		EmbeddedPlatform.install();
	}

	/**
	 * Copied from the JUnit test for the simple-person-plugin. This
	 * test could be removed since it should already have been passed.
	 */
	@Test
	public void testSimplePersonPluginExtensions() {
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

	/**
	 * This test must be run as an integration test -- that is, after
	 * packaging -- because it requires ClueMaker models to be copied
	 * to the META-INF directory tree. This copying occurs as a part
	 * of packaging (currently via the Maven Assembly Plugin).
	 */
	@Test
	public void testLoadSimplePersonModels() {
		CMExtension[] exts = CMPlatformUtils.getPluginExtensions(SP_PLUGIN_ID);
		assertTrue(exts != null);
		for (CMExtension ext : exts) {
			CMConfigurationElement[] cmces = ext.getConfigurationElements();
			assertTrue(cmces != null);
			assertTrue(cmces.length == 1);
			CMConfigurationElement cmce = cmces[0];
			String[] attributeNames = cmce.getAttributeNames();
			assertTrue(attributeNames != null);
			assertTrue(attributeNames.length == 3);
			String dbConfiguration =
				cmce.getAttribute(AN_DATABASE_CONFIGURATION);
			assertTrue(EXPECTED_DATABASE_CONFIG.equals(dbConfiguration));
			String blockingConfig =
				cmce.getAttribute(AN_BLOCKING_CONFIGURATION);
			assertTrue(EXPECTED_BLOCKING_CONFIG.equals(blockingConfig));
			String modelFile = cmce.getAttribute(AN_MODEL_FILE);
			assertTrue(modelFile != null);
			CMPluginDescriptor plugin =
				CMPlatformUtils.getPluginDescriptor(SP_PLUGIN_ID);
			URL installURL = plugin.getInstallURL();
			assertTrue(installURL != null);

			URL modelURL = null;
			try {
				modelURL = new URL(installURL,modelFile);
			} catch (MalformedURLException e) {
				String msg = "Failed to create URL from the plugin installURL (" +
			installURL + ") and the model path (" + modelFile + "): " + e.toString();
				fail(msg);
			}
			assertTrue(modelURL != null);

			// Read in the model
			final ICompiler compiler = new DoNothingCompiler();
			IProbabilityModel pm = null;
			try {
				final InputStream is = modelURL.openStream();
				final StringWriter compilerMessages = new StringWriter();
				final boolean allowCompile = false;
				pm =
					ProbabilityModelsXmlConf.readModel(null, is, compiler,
							compilerMessages, allowCompile);
				logger.fine("Compiler messages: " + compilerMessages.toString());
				pm.setModelFilePath(modelFile);
			} catch (Exception e) {
				String msg = "Failed to load a model from the plugin installURL (" +
			installURL + ") and the model path (" + modelFile + "): " + e.toString();
				fail(msg);
			}
			assertTrue(pm != null);
			
			// Check that some stuff is computed correctly
			final String computedModelPath = pm.getModelFilePath();
			assertTrue(modelFile.equals(computedModelPath));
			final Map<?,?> computedModelProperties = pm.properties();
			assertTrue(computedModelProperties != null);
			assertTrue(computedModelProperties.isEmpty());

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

	public static final String SP_PLUGIN_ID =
		"com.choicemaker.cm.simplePersonMatching";

	/**
	 * Returns a unique identifier given the id specified in the ModelMaker
	 * plugin descriptor
	 */
	static String uid(String id) {
		return SP_PLUGIN_ID + "." + id;
	}

	static Set<ExtensionDeclaration> getExpectedExtensions() {
		Set<ExtensionDeclaration> retVal = new HashSet<>();
		retVal.add(new ExtensionDeclaration(uid("Model1"),
				"com.choicemaker.cm.core.modelConfiguration"));
		retVal.add(new ExtensionDeclaration(uid("Model2"),
				"com.choicemaker.cm.core.modelConfiguration"));
		return retVal;
	}

}
