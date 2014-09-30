package com.choicemaker.cmit.modelmaker;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.choicemaker.cmit.util.Eclipse2BootLoader;
import com.choicemaker.cmit.util.Eclipse2Utils;
import com.choicemaker.cmit.util.Find;
import com.choicemaker.cmit.util.Find.Finder;
import com.choicemaker.e2.platform.InstallablePlatform;
import com.choicemaker.e2.plugin.InstallablePluginDiscovery;
import com.choicemaker.e2.standard.StandardPlatform;
import com.choicemaker.e2.std.plugin.StandardPluginDiscovery;

public class ModelMaker0IT {

	public static final int WAIT_HACK_5_SECONDS = 1000 * 5;

	private static final String RESOURCE_ROOT = "/";

	private static final String RESOURCE_NAME_SEPARATOR = "/";

	public static final String ECLIPSE_APPLICATION_DIRECTORY = RESOURCE_ROOT
			+ "eclipse.application.dir";

	public static final String PLUGIN_DIRECTORY = ECLIPSE_APPLICATION_DIRECTORY
			+ RESOURCE_NAME_SEPARATOR + "plugins";

	public static final String BOOT_PLUGIN_PATTERN = "org.eclipse.core.boot*";

	public static final String BOOT_PLUGIN_JAR = "boot.jar";

	public static final String EXAMPLE_DIRECTORY =
		ECLIPSE_APPLICATION_DIRECTORY + RESOURCE_NAME_SEPARATOR
				+ "examples/simple_person_matching";

	public static final String CONFIGURATION_FILE = "project.xml";

	public static final String CONFIGURATION_PATH = EXAMPLE_DIRECTORY
			+ RESOURCE_NAME_SEPARATOR + CONFIGURATION_FILE;

	public static String CONFIGURATION_ARG = "-conf";

	public static final String WORKSPACE = "target/workspace";

	public static final String MISC_OPTS = "-noupdate";

	// Copied from ModelMaker to avoid linking to that class
	private static final int EXIT_OK = 0;
	private static final String FQCN_MODELMAKER =
		"com.choicemaker.cm.modelmaker.gui.ModelMakerStd";
	private static final String APP_PLUGIN_ID =
		"com.choicemaker.cm.modelmaker.ModelMakerStd";
	
	/** @see http://links.rph.cx/1r1vyFo */
	public static void configureEclipseConsoleLogging() {
//		Logger topLogger = java.util.logging.Logger.getLogger("");
//		Handler consoleHandler = null;
//		for (Handler handler : topLogger.getHandlers()) {
//			if (handler instanceof ConsoleHandler) {
//				// found the console handler
//				consoleHandler = handler;
//				break;
//			}
//		}
//		if (consoleHandler == null) {
//			consoleHandler = new ConsoleHandler();
//			topLogger.addHandler(consoleHandler);
//		}
//		consoleHandler.setLevel(java.util.logging.Level.FINEST);
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		installStandardPlatform();
		configureEclipseConsoleLogging();
	}

	public static void installStandardPlatform() {
		StandardPlatform.init();
		String pn = InstallablePlatform.INSTALLABLE_PLATFORM;
		String pv = StandardPlatform.class.getName();
		System.setProperty(pn, pv);
		pn = InstallablePluginDiscovery.INSTALLABLE_PLUGIN_DISCOVERY;
		pv = StandardPluginDiscovery.class.getName();
		System.setProperty(pn, pv);
//		pn = com.choicemaker.cm.core.configure.InstallableConfigurator.;
//		pv = StandardPluginDiscovery.class.getName();
//		System.setProperty(pn, pv);
	}

	public void assertStandardPlatform() {
		String pv =
			System.getProperty(InstallablePlatform.INSTALLABLE_PLATFORM);
		assertTrue(StandardPlatform.class.getName().equals(pv));
	}

	public static URL getBootJarUrl() throws IOException {
		Finder finder = new Find.Finder(BOOT_PLUGIN_JAR);
		URI uri = new File(".").toURI();
		Path startingDir = Paths.get(uri);
		Files.walkFileTree(startingDir, finder);
		List<Path> found = finder.done();
		/*
		if (found.size() != 1) {
			throw new IllegalStateException("none/multiple boot plugins: "
					+ found.toString());
		}
		startingDir = found.get(0);
		finder = new Find.Finder(BOOT_PLUGIN_JAR);
		Files.walkFileTree(startingDir, finder);
		found = finder.done();
		*/
		if (found.size() != 1) {
			throw new IllegalStateException("none/multiple boot jars: "
					+ found.toString());
		}
		Path p = startingDir.resolve(found.get(0));
		URL retVal = p.toUri().toURL();
		return retVal;
	}

	public static URL getJarUrl(String path) throws URISyntaxException,
			MalformedURLException {
		URL retVal = ModelMaker0IT.class.getResource(path);
		return retVal;
	}

	public static String getJarUrlAsString(String path)
			throws URISyntaxException, MalformedURLException {
		URL startupJarUrl = getJarUrl(path);
		Path startupJarPath = Paths.get(startupJarUrl.toURI());
		Path installPath = startupJarPath.getParent();
		URI installUri = installPath.toUri();
		URL installUrl = installUri.toURL();
		String retVal = installUrl.toExternalForm();
		return retVal;
	}

	public static String[] getEclipseStartupArgs() {
		return new String[] { MISC_OPTS };
	}

	public static String[] getModelMakerRunArgs() throws URISyntaxException {
		URL configURL = ModelMaker0IT.class.getResource(CONFIGURATION_PATH);
		URI configURI = configURL.toURI();
		File configFile = new File(configURI);
		assertTrue(configFile.exists());
		assertTrue(configFile.canRead());
		String configPath = configFile.getAbsolutePath();
		String[] retVal = new String[] {
				CONFIGURATION_ARG, configPath };
		return retVal;
	}

	static Object startEclipse(final Class<?> bootLoader,
			final URL pluginPathLocation, final String location,
			final String[] args, final Runnable handler) throws Exception {
		assert bootLoader != null;
		Class<?>[] parameterTypes = new Class<?>[] {
				URL.class, String.class, String[].class, Runnable.class };
		Method m = bootLoader.getMethod("startup", parameterTypes);
		Object[] parameters = new Object[] {
				pluginPathLocation, location, args, handler };
		Object retVal = m.invoke(null, parameters);
		return retVal;
	}

	static Object instantiateModelMaker(final Class<?> bootLoader)
			throws Exception {
		assert bootLoader != null;
		Class<?>[] parameterTypes = new Class<?>[] { String.class };
		Method m = bootLoader.getMethod("getRunnable", parameterTypes);
		Object[] parameters = new Object[] { APP_PLUGIN_ID };
		Object retVal = m.invoke(null, parameters);
		System.out.println("BootLoader.getRunnable() return code: " + retVal);
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

	static void shutdownEclipse(final Class<?> bootLoader) throws Exception {
		assert bootLoader != null;
		Class<?>[] parameterTypes = null;
		Method m = bootLoader.getMethod("shutdown", parameterTypes);
		Object[] parameters = null;
		m.invoke(null, parameters);
	}

	private Class<?> bootLoader;
	private ClassLoader initialClassLoader;
	private Object modelMaker;

	@Before
	public void setUp() throws Exception {

		System.out.println("Starting setUp()");

		// Set up a restricted class path in the current thread context
		assertTrue(this.initialClassLoader == null);
		this.initialClassLoader =
			Thread.currentThread().getContextClassLoader();
		System.out.println("setUp() initialClassLoader: "
				+ this.initialClassLoader.toString());
		ClassLoader cl = Eclipse2Utils.getSystemClassLoader();
		Thread.currentThread().setContextClassLoader(cl);
		System.out.println("setUp() new ContextClassLoader: " + cl.toString());

		// Dynamically load the BootLoader class/singleton
		URL bootURL = getBootJarUrl();
		this.bootLoader = new Eclipse2BootLoader(bootURL).getBootLoaderClass();

		// Eclipse startup parameters
		final URL installURL = null;
		final Runnable handler = null;
		final String[] args0 = getEclipseStartupArgs();

		// Start Eclipse
		Object rc =
			startEclipse(this.bootLoader, installURL, WORKSPACE, args0, handler);
		System.out.println("Eclipse startup return code: " + rc);

		// Instantiate ModelMaker
		this.modelMaker = instantiateModelMaker(this.bootLoader);

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

		shutdownEclipse(this.bootLoader);
		System.out.println("BootLoader.shutdown() returned");

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
