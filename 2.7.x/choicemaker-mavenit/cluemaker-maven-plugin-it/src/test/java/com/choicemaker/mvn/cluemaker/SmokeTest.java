package com.choicemaker.mvn.cluemaker;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;

import com.choicemaker.cmit.utils.DefaultFileContentListener;
import com.choicemaker.cmit.utils.FileTreeComparator;
import com.choicemaker.cmit.utils.PathComparison;
import com.choicemaker.util.FileUtilities;
import com.choicemaker.util.SystemPropertyUtils;

/**
 * Based on the maven-it-sample archetype.
 *
 * @author rphall
 */
public class SmokeTest extends AbstractMavenIntegrationTestCase {

	public static final int MAX_REPORTED_DIFFERENCES = 3;

	private static final String EOL = System
			.getProperty(SystemPropertyUtils.LINE_SEPARATOR);

	private static final String LOG_FILE_SUFFIX = ".log";

	private static final String NAME_SEPARATOR = "_";

	private static String concatentateNames(String n1, String n2) {
		return n1 + NAME_SEPARATOR + n2;
	}

	private static final String MORE_DIFFERENCES_INDICATOR = "...";

	/** A convenient abbreviation to shorten long path names */
	private static final String BASE_EXCLUSION_PATH =
		"com/choicemaker/demo/simple_person_matching/gendata/gend/internal/";

	/** The path to the Accessor for the Person model */
	private static final String SIMPLEPERSON_CLUESACCESSOR_PATH =
		BASE_EXCLUSION_PATH + "Person/SimplePersonCluesAccessor.java";

	/** The path to the ClueSet for the Person model */
	private static final String SIMPLEPERSON_CLUESET_PATH = BASE_EXCLUSION_PATH
			+ "Person/SimplePersonCluesClueSet.java";

	/** The path to the Accessor for the Person2 model */
	private static final String SIMPLEPERSON2_CLUESACCESSOR_PATH =
		BASE_EXCLUSION_PATH + "Person2/SimplePersonClues2Accessor.java";

	/** The path to the ClueSet for the Person2 model */
	private static final String SIMPLEPERSON2_CLUESET_PATH =
		BASE_EXCLUSION_PATH + "Person2/SimplePersonClues2ClueSet.java";

	/** Files excluded from validity checks */
	private static final Set<Path> EXCLUDED_COMPARISONS = new HashSet<>();
	static {
		// ClueSets and CluesAccessors can differ by a build date (dumb
		// versioning)
		EXCLUDED_COMPARISONS.add(Paths.get(SIMPLEPERSON_CLUESACCESSOR_PATH));
		EXCLUDED_COMPARISONS.add(Paths.get(SIMPLEPERSON_CLUESET_PATH));
		EXCLUDED_COMPARISONS.add(Paths.get(SIMPLEPERSON2_CLUESACCESSOR_PATH));
		EXCLUDED_COMPARISONS.add(Paths.get(SIMPLEPERSON2_CLUESET_PATH));
	}

	/**
	 * The name of the System property that controls whether
	 * {@link #isMavenDebug} is set.
	 */
	public static final String CHOICEMAKER_IT_MAVEN_DEBUG_MESSAGES =
		"com.choicemaker.cm.it.MavenDebugMessages";

	/**
	 * Set to true to enable verbose Maven log messages. (Unrelated to the
	 * {@link #useDebugger} connection setting.)
	 *
	 * @see #MAVEN_DEBUG_PARAMETER
	 */
	private static final Boolean isMavenDebug = Boolean
			.getBoolean(CHOICEMAKER_IT_MAVEN_DEBUG_MESSAGES);

	/**
	 * The name of the System property that controls whether
	 * {@link #useDebugger} is set.
	 */
	public static final String CHOICEMAKER_IT_USE_MAVEN_DEBUGGER =
		"com.choicemaker.cm.it.UseMavenDebugger";

	/**
	 * Set to true to enable useDebugger connection to the Maven build that is
	 * executed by the Verifier. (Unrelated to the
	 * {@link #MAVEN_DEBUG_PARAMETER} that controls the level of logging
	 * detail.)
	 */
	private static final Boolean useDebugger = Boolean
			.getBoolean(CHOICEMAKER_IT_USE_MAVEN_DEBUGGER);

	/**
	 * Name of Maven environment variable
	 *
	 * @see #useDebugger
	 * @see #MAVEN_DEBUGGER_OPTIONS
	 */
	private static final String MAVEN_OPTIONS_PROPERTY = "MAVEN_OPTS";

	/** Maven debugger port */
	public static final String MAVEN_DEBUGGER_PORT = "8785";

	/**
	 * Value of the Maven environment variable that configures the Verifier
	 * build to wait for a useDebugger connection.
	 *
	 * @see #useDebugger
	 */
	private static final String MAVEN_DEBUGGER_OPTIONS =
		"-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address="
				+ MAVEN_DEBUGGER_PORT;

	/**
	 * The working directory for this test. All paths are relative to this
	 * directory.
	 */
	public static final String WORKING_DIR = "/smoke-test";

	/** Path to the resource directory */
	public static final String PROJECT_PATH = "src/test/resources/smoke-test";

	/**
	 * Maven debug command-line parameter. Enables verbose output to the
	 * Verifier log. (Unrelated to the {@link #useDebugger} connection setting.)
	 *
	 * @see #isMavenDebug
	 */
	public static final String MAVEN_DEBUG_PARAMETER = "-X";

	/** Maven 'clean' goal */
	public static final String MAVEN_CLEAN_GOAL = "clean";

	/** Maven 'generate-sources' goal */
	public static final String MAVEN_GENERATE_SOURCES_GOAL = "generate-sources";

	/** Maven 'compile' goal */
	public static final String MAVEN_COMPILE_GOAL = "compile";

	/** Default build directory */
	public static final String BUILD_DIRECTORY = "target";

	/**
	 * Default path to the tree of Java code that the Verifier builds. See
	 * {@link ClueMakerMojo#GENERATED_SOURCE_PATH}
	 */
	public static final String GENERATED_SOURCE_ROOT = BUILD_DIRECTORY
			+ File.separator + ClueMakerMojo.GENERATED_SOURCE_PATH;

	/** The root of Java code that the Verifier results are checked against */
	public static final String EXPECTED_CODE_ROOT =
		"src/resources/expected-source";

	private File workingDir;

	public SmokeTest() {
		super("(1.0,)"); // only test in 1.0+
	}

	public void setUp() {
		// Set up the working directory for a test
		Class<? extends SmokeTest> c = getClass();
		workingDir = null;
		try {
			workingDir =
				ResourceExtractor.simpleExtractResources(c, WORKING_DIR);
		} catch (IOException e) {
			fail(e.toString());
		}
		assertTrue(workingDir != null);
		final File targetDir = new File(workingDir, BUILD_DIRECTORY);
		if (targetDir.exists()) {
			FileUtilities.removeDir(targetDir);
		}
	}

	private Verifier createVerifier(String diagnostic) {
		// Set command-line parameters for Maven
		List<String> cliOptions = new ArrayList<>();
		if (isMavenDebug) {
			cliOptions.add(MAVEN_DEBUG_PARAMETER);
		}

		// Create and configure the Verifier
		assertTrue(workingDir != null);
		Verifier retVal = null;
		try {
			retVal = new Verifier(workingDir.getAbsolutePath());
		} catch (VerificationException e) {
			fail("Failed to create verifier (" + diagnostic + "): "
					+ e.toString());
		}
		assertTrue(retVal != null);
		retVal.setCliOptions(cliOptions);

		return retVal;
	}

	private void executeMavenGoal(Verifier verifier, String goal) {
		assert goal != null && !goal.trim().isEmpty();

		// Get a copy of the current system properties that optionally configure
		// the Verifier build to wait for a useDebugger connection
		Properties p = new Properties(System.getProperties());
		if (useDebugger) {
			out.print(EOL + "Waiting for debugger on port " + MAVEN_DEBUGGER_PORT + " ... ");
			p.setProperty(MAVEN_OPTIONS_PROPERTY, MAVEN_DEBUGGER_OPTIONS);
		}

		// Execute the specified goal using the default POM in the test project
		try {
			verifier.executeGoal(goal, p);
		} catch (VerificationException e) {
			fail("Failed to execute verifier (" + goal + "): " + e.toString());
		}
	}

	public void testClean() {
		verifyCleanBuildDirectory();
		String diagnostic = MAVEN_CLEAN_GOAL;
		Verifier verifier = createVerifier(diagnostic);
		assertTrue(verifier != null);
		verifier.setLogFileName(diagnostic + LOG_FILE_SUFFIX);
		executeMavenGoal(verifier, MAVEN_CLEAN_GOAL);
		verifyCleanBuildDirectory();
	}

	public void testGenerate() {
		verifyCleanBuildDirectory();
		String diagnostic = MAVEN_GENERATE_SOURCES_GOAL;
		Verifier verifier = createVerifier(diagnostic);
		assertTrue(verifier != null);
		verifier.setLogFileName(diagnostic + LOG_FILE_SUFFIX);
		executeMavenGoal(verifier, MAVEN_GENERATE_SOURCES_GOAL);
		verifyGeneratedSourcesDirectory();
	}

	public void testGenerateClean() {
		verifyCleanBuildDirectory();
		String diagnostic =
			concatentateNames(MAVEN_GENERATE_SOURCES_GOAL, MAVEN_CLEAN_GOAL);
		Verifier verifier = createVerifier(diagnostic);
		assertTrue(verifier != null);
		verifier.setLogFileName(diagnostic + LOG_FILE_SUFFIX);
		executeMavenGoal(verifier, MAVEN_GENERATE_SOURCES_GOAL);
		verifyGeneratedSourcesDirectory();
		executeMavenGoal(verifier, MAVEN_CLEAN_GOAL);
		verifyCleanBuildDirectory();
	}

	public void testCompile() {
		verifyCleanBuildDirectory();
		String diagnostic = MAVEN_COMPILE_GOAL;
		Verifier verifier = createVerifier(diagnostic);
		assertTrue(verifier != null);
		verifier.setLogFileName(diagnostic + LOG_FILE_SUFFIX);
		executeMavenGoal(verifier, MAVEN_COMPILE_GOAL);
	}

	private void verifyCleanBuildDirectory() {
		assertTrue(workingDir != null);
		final Path workingPath = Paths.get(workingDir.toURI());
		final Path targetPath =
			Paths.get(workingDir.getPath(), BUILD_DIRECTORY);
		FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
			int count = 0;

			@Override
			public FileVisitResult preVisitDirectory(Path dir,
					BasicFileAttributes attrs) throws IOException {
				FileVisitResult retVal = null;
				++count;
				if (count == 1) {
					assertTrue(dir.equals(targetPath));
					retVal = FileVisitResult.CONTINUE;
				}
				if (count > 1) {
					Path relative = workingPath.relativize(dir);
					fail("Maven goal (" + MAVEN_CLEAN_GOAL
							+ ") failed to clean '" + relative + "'");
					// Unreachable at runtime
					retVal = FileVisitResult.TERMINATE;
				}
				assert (retVal == FileVisitResult.CONTINUE);
				return retVal;
			}

			@Override
			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException {
				Path relative = workingPath.relativize(file);
				fail("Maven goal (" + MAVEN_CLEAN_GOAL + ") failed to clean '"
						+ relative + "'");
				// Unreachable at runtime
				return FileVisitResult.TERMINATE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc)
					throws IOException {
				FileVisitResult retVal = null;
				Path relative = workingPath.relativize(file);
				if (count == 0) {
					assertTrue(relative.equals(Paths.get(BUILD_DIRECTORY)));
					retVal = FileVisitResult.CONTINUE;
				} else {
					fail("ERROR (" + MAVEN_CLEAN_GOAL
							+ "): test failed to visit '" + relative + "'");
				}
				assertTrue(retVal == FileVisitResult.CONTINUE);
				return retVal;
			}
		};
		try {
			Files.walkFileTree(targetPath, visitor);
		} catch (IOException e) {
			fail("test failed to walk '" + targetPath + "': " + e.toString());
		}
	}

	private void verifyGeneratedSourcesDirectory() {
		// File tree of expected code
		File f = new File(workingDir, EXPECTED_CODE_ROOT);
		Path rootExpectedSource = Paths.get(f.toURI());

		// File tree of generated code
		f = new File(workingDir, GENERATED_SOURCE_ROOT);
		Path rootGeneratedSource = Paths.get(f.toURI());
		
		PathComparison.assertSameContent(rootExpectedSource, rootGeneratedSource, MAX_REPORTED_DIFFERENCES, EXCLUDED_COMPARISONS);

		// Compare the generated tree against the expected tree
		final int maxCollected = MAX_REPORTED_DIFFERENCES + 1;
		DefaultFileContentListener listener =
			new DefaultFileContentListener(maxCollected, false);
		FileTreeComparator ftc =
			new FileTreeComparator(rootExpectedSource, rootGeneratedSource);
		ftc.addListener(listener);
		ftc.addExcludedPaths(EXCLUDED_COMPARISONS);
		try {
			ftc.compare();
		} catch (IOException e) {
			fail(e.toString());
		}
		final int differences = listener.getDifferenceCount();
		if (differences != 0) {
			int count = 0;
			StringBuilder sb = new StringBuilder();
			sb.append("Differences found (" + differences + "): ").append(EOL);
			for (String msg : listener.getMessages()) {
				sb.append(msg).append(EOL);
				if (count > MAX_REPORTED_DIFFERENCES) {
					sb.append(MORE_DIFFERENCES_INDICATOR).append(EOL);
					break;
				}
			}
			fail(sb.toString());
		}
	}

}
