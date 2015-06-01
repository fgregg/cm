package com.choicemaker.cm.io.db.sqlserver.dbom;

import static com.choicemaker.cm.core.PropertyNames.INSTALLABLE_CHOICEMAKER_CONFIGURATOR;
import static com.choicemaker.cm.io.db.sqlserver.dbom.SqlServerUtils.SQLSERVER_PLUGIN_ID;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.maven.it.util.ResourceExtractor;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.choicemaker.cm.core.ModelConfigurationException;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.core.util.ObjectMaker;
import com.choicemaker.cm.core.xmlconf.XmlConfigurator;
import com.choicemaker.cmit.utils.PathComparison;
import com.choicemaker.e2.CMExtension;
import com.choicemaker.e2.embed.EmbeddedPlatform;
import com.choicemaker.e2.platform.CMPlatformUtils;
import com.choicemaker.e2.utils.ExtensionDeclaration;
import com.choicemaker.util.FileUtilities;

public class SqlDbObjectMakerIT {

	private static final Logger logger = Logger
			.getLogger(SqlDbObjectMakerIT.class.getName());

	private static final String SIMPLE_CLASS = SqlDbObjectMakerIT.class
			.getSimpleName();

	/**
	 * The working directory for this test. All paths are relative to this
	 * directory.
	 */
	public static final String WORKING_DIR = "/expected_sqlserver_objects";

	/** Default build directory relative to {@link #WORKING_DIR} */
	public static final String BUILD_DIRECTORY = "../target";

	/** The root of Java code that the Verifier results are checked against */
	public static final String EXPECTED_CODE_ROOT = "expected-source";

	private File workingDir;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		final String METHOD = "SqlDbObjectMakerIT.setUpBeforeClass: ";
		EmbeddedPlatform.install();
		String pn = INSTALLABLE_CHOICEMAKER_CONFIGURATOR;
		String pv = XmlConfigurator.class.getName();
		System.setProperty(pn, pv);
		try {
			int count = PMManager.loadModelPlugins();
			if (count == 0) {
				String msg = METHOD + "No probability models loaded";
				logger.warning(msg);
			}
		} catch (ModelConfigurationException | IOException e) {
			String msg =
				METHOD + "Unable to load model plugins: " + e.toString();
			logger.severe(msg);
			throw new IllegalStateException(msg);
		}
	}

	public static File computeTargetDirectory(File workingDir)
			throws IOException {
		return new File(workingDir, BUILD_DIRECTORY).getCanonicalFile();
	}

	@Before
	public void setUp() {
		// Set up the working directory for a test
		Class<? extends SqlDbObjectMakerIT> c = getClass();
		workingDir = null;
		try {
			workingDir =
				ResourceExtractor.simpleExtractResources(c, WORKING_DIR);
			logger.fine("workingDir: " + workingDir);
			assertTrue(workingDir != null);

			File targetDir = computeTargetDirectory(workingDir);
			logger.fine("targetDir: " + targetDir);
			if (targetDir.exists()) {
				FileUtilities.removeDir(targetDir);
			}
		} catch (IOException e) {
			fail(e.toString());
		}
	}

	@Test
	public void testSqlServerExtensions() {
		final String METHOD = "testSqlServerExtensions";
		logger.entering(SIMPLE_CLASS, METHOD);

		Set<ExtensionDeclaration> expected =
			SqlServerUtils.getExpectedExtensions();
		CMExtension[] exts =
			CMPlatformUtils.getPluginExtensions(SQLSERVER_PLUGIN_ID);
		assertTrue(exts != null);
		assertTrue(exts.length == expected.size());
		Set<ExtensionDeclaration> computed = new HashSet<>();
		for (CMExtension ext : exts) {
			computed.add(new ExtensionDeclaration(ext));
		}
		assertTrue(computed.containsAll(expected));
		assertTrue(computed.size() == expected.size());

		logger.exiting(SIMPLE_CLASS, METHOD);
	}

	@Test
	public void testGenerateObjects() {
		final String METHOD = "testGenerateObjects";
		logger.entering(SIMPLE_CLASS, METHOD);

		final String extensionId = SqlServerUtils.uid("sqlDbObjectMaker");
		ObjectMaker maker = SqlServerUtils.getObjectMaker(extensionId);
		try {
			final Path expectedPath = Paths.get(workingDir.toURI());
			final File computedDir = computeTargetDirectory(workingDir);
			final Path computedPath = Paths.get(computedDir.toURI());
			Files.createDirectories(computedPath);
			maker.generateObjects(computedDir);
			PathComparison.assertSameContent(expectedPath, computedPath);
		} catch (XmlConfException | IOException e) {
			fail(e.toString());
		}

		logger.exiting(SIMPLE_CLASS, METHOD);
	}

	// @Test
	// public void testGetAllModels() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testProcessAllModels() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testCreateObjects() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetMultiKeyImmutableProbabilityModelString() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetMultiKeyDbReader() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetMultiQuery() {
	// fail("Not yet implemented");
	// }

}
