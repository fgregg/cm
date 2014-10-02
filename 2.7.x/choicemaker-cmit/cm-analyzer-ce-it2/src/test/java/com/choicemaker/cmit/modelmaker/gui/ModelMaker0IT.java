package com.choicemaker.cmit.modelmaker.gui;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.choicemaker.e2.CMPlatformRunnable;
import com.choicemaker.e2.embed.EmbeddedPlatform;
import com.choicemaker.e2.mbd.plugin.EmbeddedPluginDiscovery;
import com.choicemaker.e2.platform.InstallablePlatform;
import com.choicemaker.e2.plugin.InstallablePluginDiscovery;

public class ModelMaker0IT {
	
	private static final Logger logger = Logger.getLogger(ModelMaker0IT.class.getName());

	private static final String RESOURCE_ROOT = "/";

	private static final String RESOURCE_NAME_SEPARATOR = "/";

	public static final String ECLIPSE_APPLICATION_DIRECTORY = RESOURCE_ROOT
			+ "eclipse.application.dir";

//	public static final String PLUGIN_DIRECTORY = ECLIPSE_APPLICATION_DIRECTORY
//			+ RESOURCE_NAME_SEPARATOR + "plugins";

//	public static final String BOOT_PLUGIN_PATTERN = "org.eclipse.core.boot*";

	public static final String BOOT_PLUGIN_JAR = "boot.jar";

	public static final String EXAMPLE_DIRECTORY =
		ECLIPSE_APPLICATION_DIRECTORY + RESOURCE_NAME_SEPARATOR
				+ "examples/simple_person_matching";

	public static final String CONFIGURATION_FILE = "analyzer-configuration.xml";

	public static final String CONFIGURATION_PATH = EXAMPLE_DIRECTORY
			+ RESOURCE_NAME_SEPARATOR + CONFIGURATION_FILE;

	public static String CONFIGURATION_ARG = "-conf";

	public static final String WORKSPACE = "target/workspace";

	public static final String MISC_OPTS = "-noupdate";

	// Copied from ModelMaker to avoid linking to that class
	private static final int EXIT_OK = 0;
	private static final String FQCN_MODELMAKER =
		"com.choicemaker.cm.modelmaker.gui.ModelMaker";
	private static final String APP_PLUGIN_ID =
		"com.choicemaker.cm.modelmaker.ModelMaker";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		installEmbeddedPlatform();
	}

	public static void installEmbeddedPlatform() {
		String pn = InstallablePlatform.INSTALLABLE_PLATFORM;
		String pv = EmbeddedPlatform.class.getName();
		System.setProperty(pn, pv);
		pn = InstallablePluginDiscovery.INSTALLABLE_PLUGIN_DISCOVERY;
		pv = EmbeddedPluginDiscovery.class.getName();
		System.setProperty(pn, pv);
	}

	public static String[] getModelMakerRunArgs() throws URISyntaxException {
		URL configURL = ModelMaker0IT.class.getResource(CONFIGURATION_PATH);
		if (configURL == null) {
			String msg = "Invalid configuration path: " + CONFIGURATION_PATH;
			logger.severe(msg);
			fail(msg);
		}
		logger.info("configURL: " + configURL.toString());
		URI configURI = configURL.toURI();
		File configFile = new File(configURI);
		assertTrue(configFile.toString() + " doesn't exist", configFile.exists());
		assertTrue(configFile.toString() + " isn't readable", configFile.canRead());
		String configPath = configFile.getAbsolutePath();
		String[] retVal = new String[] {
				CONFIGURATION_ARG, configPath };
		return retVal;
	}

	static Object instantiateModelMaker()
			throws Exception {
		CMPlatformRunnable retVal = InstallablePlatform.getInstance().loaderGetRunnable(APP_PLUGIN_ID);
		System.out.println("InstallablePlatform.loaderGetRunnable(): " + retVal);
		return retVal;
	}

	static void startupModelMaker(final Object modelMaker, final String[] args)
			throws Exception {
		assert modelMaker != null;
		Class<?>[] parameterTypes = new Class<?>[] { Object.class };
		Class<?> mmClass = modelMaker.getClass();
		Method m = mmClass.getMethod("startup", parameterTypes);
		Object[] parameters = new Object[] { args };
		m.invoke(modelMaker, parameters);
	}

	static boolean isModelMakerReady(final Object modelMaker) throws Exception {
		assert modelMaker != null;
		Class<?>[] parameterTypes = null;
		Class<?> mmClass = modelMaker.getClass();
		Method m = mmClass.getMethod("isReady", parameterTypes);
		Object[] parameters = null;
		Object rc = m.invoke(modelMaker, parameters);
		assertTrue(rc instanceof Boolean);
		boolean retVal = ((Boolean) rc).booleanValue();
		return retVal;
	}

	static Object tearDownModelMaker(final Object modelMaker, int exitCode)
			throws Exception {
		Object retVal = null;
		if (modelMaker != null) {
			Class<?>[] parameterTypes = new Class<?>[] { int.class };
			Class<?> mmClass = modelMaker.getClass();
			Method m = mmClass.getMethod("programExit", parameterTypes);
			Object[] parameters = new Object[] { Integer.valueOf(exitCode) };
			retVal = m.invoke(modelMaker, parameters);
		}
		return retVal;
	}

//	private ClassLoader initialClassLoader;
	private Object modelMaker;

	@Before
	public void setUp() throws Exception {

		System.out.println("Starting setUp()");

		// Instantiate ModelMaker
		this.modelMaker = instantiateModelMaker();

		// Prepare, but do not display, the ModelMaker GUI
		String[] args1 = getModelMakerRunArgs();
		startupModelMaker(ModelMaker0IT.this.modelMaker, args1);
		System.out.println("ModelMaker GUI prepared (but not displayed)");

		System.out.println("setUp() complete");
	}

	@After
	public void tearDown() throws Exception {
		System.out.println("Starting tearDown()");

		Object rc = tearDownModelMaker(this.modelMaker, EXIT_OK);
		System.out.println("ModelMaker.programExit() return code: " + rc);

		System.out.println("tearDown() complete");
	}

	@Test
	public void testModelMakerIsReady() throws Exception {
		System.out.println("testModelMakerIsReady");
		System.out.println("starting test");
		assertTrue(this.modelMaker != null);
		assertTrue(FQCN_MODELMAKER.equals(this.modelMaker.getClass().getName()));
		assertTrue(isModelMakerReady(this.modelMaker));
		System.out.println("test completed");
	}

}
