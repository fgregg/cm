package com.choicemaker.cmit.modelmaker;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import junit.framework.TestCase;

import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cmit.util.Eclipse2BootLoader;
import com.choicemaker.cmit.util.Eclipse2Launcher;
import com.choicemaker.cmit.util.Eclipse2Utils;
import com.choicemaker.util.InstanceRegistry;

public class ModelMakerTest extends TestCase {

	// private static final Logger logger =
	// Logger.getLogger(ModelMakerTest.class
	// .getName());

	private static final int SLEEP_INTERVAL_MSECS = 1000;

	private static final int MAX_INTERVALS = 100;

	/**
	 * The name of the System property that controls whether
	 * {@link #useDebugger} is set.
	 */
	public static final String CHOICEMAKER_IT_USE_DEBUGGER =
		"com.choicemaker.cm.it.UseDebugger";

	/** Set to true to enable useDebugger connection to the Eclipse platform */
	private static final Boolean useDebugger = Boolean
			.getBoolean(CHOICEMAKER_IT_USE_DEBUGGER);

	public static final String JAVA_OPTS = "-Xms384M -Xmx512M";

	public static final String DEBUG_PORT = "28787";

	public static final String DEBUG_OPTS = "-Xdebug "
			+ "-Xrunjdwp:transport=dt_socket,address=" + DEBUG_PORT
			+ ",server=y,suspend=y";

	// public static final String LOG4J_FILE = "log4j.xml";
	//
	// public static final String LOG4J_OPTS = "-Dlog4j.configuration="
	// + LOG4J_FILE;

	private static final String RESOURCE_ROOT = "/";

	private static final String RESOURCE_NAME_SEPARATOR = "/";

	public static final String ECLIPSE_APPLICATION_DIRECTORY = RESOURCE_ROOT
			+ "eclipse.application.dir";

	public static final String STARTUP_JAR = "startup.jar";

	public static final String STARTUP_JAR_PATH = ECLIPSE_APPLICATION_DIRECTORY
			+ RESOURCE_NAME_SEPARATOR + STARTUP_JAR;

	// public static final String CLASSPATH_OPTS = "-cp " + STARTUP_JAR;

	public static final String PLUGIN_DIRECTORY = ECLIPSE_APPLICATION_DIRECTORY
			+ RESOURCE_NAME_SEPARATOR + "plugins";

	public static final String BOOT_PLUGIN = "org.eclipse.core.boot";

	public static final String BOOT_PLUGIN_STEM = BOOT_PLUGIN + "_";

	// FIXME Note trailing '.' because of Maven packaging error
	public static final String BOOT_PLUGIN_VERSION = "2.1.1.";

	public static final String BOOT_PLUGIN_DIRECTORY = BOOT_PLUGIN_STEM
			+ BOOT_PLUGIN_VERSION;

	public static final String BOOT_PLUGIN_JAR = "boot.jar";

	public static final String BOOT_PLUGIN_JAR_PATH = PLUGIN_DIRECTORY
			+ RESOURCE_NAME_SEPARATOR + BOOT_PLUGIN_DIRECTORY
			+ RESOURCE_NAME_SEPARATOR + BOOT_PLUGIN_JAR;

	public static final String INSTALL_ARG = "-install";

	public static final String LAUNCHER_CLASS =
		"org.eclipse.core.launcher.Main";

	public static final String APP_PLUGIN_ID =
		"com.choicemaker.cm.modelmaker.ModelMaker";

	public static final String APPLICATION_OPTS = "-application "
			+ APP_PLUGIN_ID;

	public static final String WORKSPACE = "target/workspace";

	public static final String DATA_OPTS = "-data " + WORKSPACE;

	public static final String MISC_OPTS = "-noupdate";

	private static final String SP = " ";

	public static URL getJarUrl(String path) throws URISyntaxException,
			MalformedURLException {
		URL retVal = ModelMakerTest.class.getResource(path);
		return retVal;
	}

	public static String getJarUrlAsString(String path) throws URISyntaxException,
			MalformedURLException {
		URL startupJarUrl = getJarUrl(path);
		Path startupJarPath = Paths.get(startupJarUrl.toURI());
		Path installPath = startupJarPath.getParent();
		URI installUri = installPath.toUri();
		URL installUrl = installUri.toURL();
		String retVal = installUrl.toExternalForm();
		return retVal;
	}

	public static String getArgs() {
		StringBuilder sb = new StringBuilder();
		sb.append(JAVA_OPTS).append(SP);
		if (useDebugger) {
			sb.append(DEBUG_OPTS).append(SP);
		}
		// sb.append(LOG4J_OPTS).append(SP);
		// sb.append(CLASSPATH_OPTS).append(SP);
		// sb.append(INSTALL_ARG).append(SP);

		String installURL = null;
		try {
			installURL = getJarUrlAsString(STARTUP_JAR_PATH);
		} catch (MalformedURLException | URISyntaxException e) {
			throw new Error("Unexpected (i.e. design error): " + e.toString());
		}
		assert installURL != null;

		sb.append(INSTALL_ARG).append(SP).append(installURL).append(SP);
		sb.append(LAUNCHER_CLASS).append(SP);
		sb.append(APPLICATION_OPTS).append(SP);
		sb.append(DATA_OPTS).append(SP);
		sb.append(MISC_OPTS);
		return sb.toString();
	}

	public static String[] getArgsAsArray() {
		return getArgs().split(SP);
	}

	private Eclipse2Launcher launcher;
	private Eclipse2BootLoader bootLoader;
	private ClassLoader initialClassLoader;

	Thread createModelMakerThread(final ClassLoader cl,
			final Eclipse2Launcher launcher, final String[] args) {
		assert cl != null;
		assert launcher != null;
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					Thread.currentThread().setContextClassLoader(cl);
					System.out.println("ModelMaker thread class loader: "
							+ Thread.currentThread().getContextClassLoader()
									.toString());
					launcher.run(args);
				} catch (Exception e) {
					System.err.println("ModelMaker thread failed: "
							+ e.toString());
				}
			}
		};
		return new Thread(r);
	}

	@SuppressWarnings("unchecked")
	protected void setUp() throws Exception {

		System.out.println("Starting setUp()");
		super.setUp();

		assertTrue(this.initialClassLoader == null);
		this.initialClassLoader =
			Thread.currentThread().getContextClassLoader();
		System.out.println("setUp() initialClassLoader: "
				+ this.initialClassLoader.toString());
		ClassLoader cl = Eclipse2Utils.getSystemClassLoader();
		Thread.currentThread().setContextClassLoader(cl);
		System.out.println("setUp() new ContextClassLoader: " + cl.toString());

		assertTrue(this.launcher == null);
		URL startupURL = getJarUrl(STARTUP_JAR_PATH);
		URL bootURL = getJarUrl(BOOT_PLUGIN_JAR_PATH);
		this.launcher = new Eclipse2Launcher(startupURL, bootURL);
		String[] args = getArgsAsArray();
		System.out.println("Args: " + args);
		Thread t = createModelMakerThread(cl, this.launcher, args);
		t.start();

//		int count = 0;
//		this.bootLoader = new Eclipse2BootLoader(this.launcher);
//		while (!Eclipse2Utils.isEclipseRunning(this.bootLoader)) {
//			System.out.println("setUp() wait interval: " + count);
//			if (count > MAX_INTERVALS) {
//				break;
//			}
//			Thread.sleep(SLEEP_INTERVAL_MSECS);
//			++count;
//		}
		int count = 0;
		Map<String,Object> mms = InstanceRegistry.getInstance().findRegisteredInstances(ModelMaker.PLUGIN_APPLICATION_ID);
		while (mms.size() < 1) {
			System.out.println("setUp() wait interval: " + count);
			if (count > MAX_INTERVALS) {
				break;
			}
			Thread.sleep(SLEEP_INTERVAL_MSECS);
			++count;
			mms = InstanceRegistry.getInstance().findRegisteredInstances(ModelMaker.PLUGIN_APPLICATION_ID);
		}

		mms = InstanceRegistry.getInstance().findRegisteredInstances(ModelMaker.PLUGIN_APPLICATION_ID);
		if (mms.size() < 1) {
			fail("setUp() failed: unable to start Eclipse 2");
		}
		System.out.println("setUp() complete");
	}

	protected void tearDown() throws Exception {
		System.out.println("Starting tearDown()");
		super.tearDown();
		Eclipse2Utils.shutdownEclipse(this.bootLoader);

		assertTrue(this.initialClassLoader != null);
		System.out.println("setUp() restoring initialClassLoader: "
				+ this.initialClassLoader.toString());
		Thread.currentThread().setContextClassLoader(this.initialClassLoader);

		System.out.println("tearDown() complete");
	}

	public void testSetupTeardown() throws Exception {
		System.out.println("testSetupTeardown");
		System.out.println("starting test");
		assertTrue(this.launcher != null);
		assertTrue(Eclipse2Utils.isEclipseRunning(this.bootLoader));
		@SuppressWarnings("unchecked")
		Map<String, Object> mmInstances =
			InstanceRegistry.getInstance().findRegisteredInstances(
					ModelMaker.PLUGIN_APPLICATION_ID);
		assertTrue(mmInstances != null);
		assertTrue(mmInstances.size() == 1);
		for (Map.Entry<String, Object> entry : mmInstances.entrySet()) {
			assertTrue(entry.getKey().startsWith(
					ModelMaker.PLUGIN_APPLICATION_ID));
			assertTrue(entry.getValue() instanceof ModelMaker);
		}
		System.out.println("test completed");
	}

}
