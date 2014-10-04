package com.choicemaker.demo.simple_person_matching;

import static com.choicemaker.cm.core.PropertyNames.INSTALLABLE_CHOICEMAKER_CONFIGURATOR;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import com.choicemaker.cm.core.configure.ConfigurationManager;
import com.choicemaker.cm.core.xmlconf.ProbabilityModelsXmlConf;
import com.choicemaker.cm.core.xmlconf.XmlConfigurator;
import com.choicemaker.e2.CMConfigurationElement;
import com.choicemaker.e2.CMExtension;
import com.choicemaker.e2.CMPluginDescriptor;
import com.choicemaker.e2.embed.EmbeddedPlatform;
import com.choicemaker.e2.platform.CMPlatformUtils;
import com.choicemaker.e2.utils.ExtensionDeclaration;

public class SimplePersonPluginTest {

	private static final Logger logger = Logger
			.getLogger(SimplePersonPluginTest.class.getName());

	/** Location as a resource */
	private static final String CHOICEMAKER_CONFIGURATION =
		"choicemaker-configuration.xml";

	private static final String TEMP_FILE_PREFIX = "SimplePersonPluginTest_";
	private static final String TEMP_FILE_SUFFIX = ".xml";

	private static final String AN_MODEL_FILE = "model";
	private static final String AN_DATABASE_CONFIGURATION =
		"databaseConfiguration";
	private static final String AN_BLOCKING_CONFIGURATION =
		"blockingConfiguration";

	private static final String EXPECTED_DATABASE_CONFIG = "default";
	private static final String EXPECTED_BLOCKING_CONFIG = "defaultAutomated";

	/**
	 * This test requires a filtered version of a configuration file, which is
	 * placed by a Maven build at target/test-classes. However, to avoid
	 * hard-coding that path (which can be changed in the POM configuration),
	 * this method looks for the configuration file as a resource using the
	 * class loader of this test; creates a temporary file at a known location;
	 * and dumps this content of the configuration file (resource) into the
	 * known location.
	 * 
	 * @return a non-null file with the content of the configuration file.
	 * @throws IOException
	 */
	public static Path getFilteredConfiguration() throws IOException {
		ClassLoader cl = SimplePersonPluginTest.class.getClassLoader();
		InputStream is = cl.getResourceAsStream(CHOICEMAKER_CONFIGURATION);
		File f = File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
		URI targetURI = f.toURI();
		f.delete();
		Path retVal = Paths.get(targetURI);
		Files.copy(is, retVal);
		return retVal;
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		EmbeddedPlatform.install();
		String pn = INSTALLABLE_CHOICEMAKER_CONFIGURATOR;
		String pv = XmlConfigurator.class.getName();
		System.setProperty(pn, pv);
		final boolean useReload = true;
		final boolean initGui = false;
		final String absFilePath =
			getFilteredConfiguration().toFile().getAbsolutePath();
		ConfigurationManager.getInstance()
				.init(absFilePath, useReload, initGui);
	}

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
	 * This test must be run as an integration test -- that is, after packaging
	 * -- because it requires ClueMaker models to be copied to the META-INF
	 * directory tree. This copying occurs as a part of packaging (currently via
	 * the Maven Assembly Plugin).
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
			final ClassLoader cl = SimplePersonPluginTest.class.getClassLoader();
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
			final Map<?, ?> computedModelProperties = pm.properties();
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