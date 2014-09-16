package com.choicemaker.cm.compiler.app;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.configure.ConfigurationManager;
import com.choicemaker.util.SystemPropertyUtils;

public class MainTest extends TestCase {

	private static final Logger logger = Logger.getLogger(MainTest.class
			.getName());

	/**
	 * Determines whether logging is {@link #testLoggingConfiguration()
	 * reported}
	 */
	public static final boolean isLoggingReported = false;

	public static final String PN_LOGGING_CONFIGURATION_FILE =
		"java.util.logging.config.file";

	public static final String CONFIGURATION_FILE =
		"src/test/resources/projects/simple_person_matching/project.xml";

	/** Relative to the CONFIGURATION FILE */
	public static final String CLUE_DIR_PATH = "etc/models/";

	public static final File getClueFileDir() {
		File d = new File(CONFIGURATION_FILE).getParentFile();
		return new File(d, CLUE_DIR_PATH);
	}

	public static final String CLUE_FILE_NAME_1 = "SimplePersonClues.clues";

	public static final File CLUE_FILE_1 = new File(getClueFileDir(),
			CLUE_FILE_NAME_1);

	public static final String CLUE_FILE_NAME_2 = "SimplePersonClues2.clues";

	public static final File CLUE_FILE_2 = new File(getClueFileDir(),
			CLUE_FILE_NAME_2);

	protected static void setUpBeforeClass() throws Exception {
	}

	protected static void tearDownAfterClass() throws Exception {
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testLoggingConfiguration() {
		// Not a test, but just a report on whether logging is configured
		if (isLoggingReported) {
			String path = System.getProperty(PN_LOGGING_CONFIGURATION_FILE);
			if (path == null) {
				System.out.println(MainTest.class.getSimpleName()
						+ ": no logging configuration file specified");
			} else {
				File f = new File(path);
				if (!f.exists()) {
					System.err.println(MainTest.class.getSimpleName()
							+ ": no logging configuration file specified");
				}
			}
		}
	}

	public static final String missingFileMessage(String context, File f) {
		final String preface = context + ": ";
		String retVal;
		if (f == null) {
			retVal = preface + "the path is null";
		} else {
			final String workingDir =
				System.getProperty(SystemPropertyUtils.USER_DIR);
			final File d = new File(workingDir);
			logger.fine(preface + "user dir (abstract path):  " + d.toString());
			logger.fine(preface + "user dir (absolute path):  "
					+ d.getAbsolutePath());
			logger.fine(preface + "conf file (abstract path): " + f.toString());
			logger.fine(preface + "conf file (absolute path): "
					+ f.getAbsolutePath());
			String msg =
				"The path '"
						+ f.toString()
						+ "' does not exist relative to the current directory '"
						+ workingDir + "'";
			retVal = preface + msg;
		}
		return retVal;
	}

	public void testConfigurationFile() {
		File f = new File(CONFIGURATION_FILE);
		boolean isExistingFile = f.exists();
		if (!isExistingFile) {
			String msg = missingFileMessage("Configuration file", f);
			fail(msg);
		}
		assertTrue(f.canRead());
	}

	public void testClueFileDir() {
		File f = getClueFileDir();
		boolean isExistingFile = f.exists();
		if (!isExistingFile) {
			String msg = missingFileMessage("Clue file directory", f);
			fail(msg);
		}
		assertTrue(f.isDirectory());
		assertTrue(f.canRead());
	}

	public void testClueFile1() {
		File f = CLUE_FILE_1;
		boolean isExistingFile = f.exists();
		if (!isExistingFile) {
			String msg = missingFileMessage("Clue file 1", f);
			fail(msg);
		}
		assertTrue(f.canRead());
	}

	public void testClueFile2() {
		File f = CLUE_FILE_2;
		boolean isExistingFile = f.exists();
		if (!isExistingFile) {
			String msg = missingFileMessage("Clue file 2", f);
			fail(msg);
		}
		assertTrue(f.canRead());
	}

	public void testChoiceMakerInit() {
		File configurationFile = new File(CONFIGURATION_FILE);
		try {
			ChoiceMakerInit.initialize(configurationFile,
					Main.NO_LOG_CONFIGURATION, Main.RELOAD, Main.INITGUI);
		} catch (XmlConfException e) {
			fail(e.toString());
		}
		ConfigurationManager cmgr = ConfigurationManager.getInstance();
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Generated code root: " + cmgr.getGeneratedSourceRoot());
			logger.fine(" Compiled code root: " + cmgr.getCompiledCodeRoot());
			logger.fine(" Packaged code root: " + cmgr.getPackagedCodeRoot());
		}

		File f;
		cmgr.deleteGeneratedCode();
		f = new File(cmgr.getGeneratedSourceRoot());
		assertTrue(cmgr.getGeneratedSourceRoot(),
				!f.exists() || (f.isDirectory() || f.list().length == 0));
		f = new File(cmgr.getCompiledCodeRoot());
		assertTrue(cmgr.getCompiledCodeRoot(), !f.exists()
				|| (f.isDirectory() || f.list().length == 0));
		f = new File(cmgr.getPackagedCodeRoot());
		assertTrue(cmgr.getPackagedCodeRoot(), !f.exists()
				|| (f.isDirectory() || f.list().length == 0));
	}

	public void testMain(String[] args) {
		if (args == null || args.length != 3) {
			throw new IllegalArgumentException("invalid args");
		}
		File configurationFile = new File(args[0]);
		try {
			ChoiceMakerInit.initialize(configurationFile,
					Main.NO_LOG_CONFIGURATION, Main.RELOAD, Main.INITGUI);
		} catch (XmlConfException e) {
			fail(e.toString());
		}
		ConfigurationManager cmgr = ConfigurationManager.getInstance();
		cmgr.deleteGeneratedCode();

		final File gsRoot = new File(cmgr.getGeneratedSourceRoot());
		final File ccRoot = new File(cmgr.getCompiledCodeRoot());
		final File pcRoot = new File(cmgr.getPackagedCodeRoot());

		assertTrue(cmgr.getGeneratedSourceRoot(),
				!gsRoot.exists()
						|| (gsRoot.isDirectory() || gsRoot.list().length == 0));
		assertTrue(cmgr.getCompiledCodeRoot(),
				!ccRoot.exists()
						|| (ccRoot.isDirectory() || ccRoot.list().length == 0));
		assertTrue(cmgr.getPackagedCodeRoot(),
				!pcRoot.exists()
						|| (pcRoot.isDirectory() || pcRoot.list().length == 0));

		try {
			Main.main(args);
		} catch (Exception x) {
			fail(x.toString());
		}

		assertTrue(cmgr.getGeneratedSourceRoot(),
				gsRoot.exists() && gsRoot.isDirectory()
						&& gsRoot.list().length > 0);
		assertTrue(cmgr.getCompiledCodeRoot(),
				ccRoot.exists() && ccRoot.isDirectory()
						&& ccRoot.list().length > 0);
//		assertTrue(cmgr.getPackagedCodeRoot(),
//				pcRoot.exists() && pcRoot.isDirectory()
//						&& pcRoot.list().length > 0);
	}

	public void testMain1() {
		final String[] args =
			new String[] {
					CONFIGURATION_FILE, CLUE_DIR_PATH + CLUE_FILE_NAME_1,
					Main.NO_LOG_CONFIGURATION };
		testMain(args);
	}

	public void testMain2() {
		String[] args =
			new String[] {
					CONFIGURATION_FILE, CLUE_DIR_PATH + CLUE_FILE_NAME_2,
					Main.NO_LOG_CONFIGURATION };
		testMain(args);
	}

}
