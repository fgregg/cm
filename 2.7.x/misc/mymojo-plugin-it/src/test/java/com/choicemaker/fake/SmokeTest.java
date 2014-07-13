package com.choicemaker.fake;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;

import com.choicemaker.util.SystemPropertyUtils;
import com.choicemaker.util3.DefaultFileContentListener;
import com.choicemaker.util3.FileTreeComparator;

/**
 * Based on the maven-it-sample archetype.
 * 
 * @author rphall
 */
public class SmokeTest extends AbstractMavenIntegrationTestCase {

	public static final int MAX_REPORTED_DIFFERENCES = 3;

	private static final String EOL = System
			.getProperty(SystemPropertyUtils.LINE_SEPARATOR);

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
	 * Set to true to enable debugger connection to the Maven build that is
	 * executed by the Verifier. (Unrelated to the
	 * {@link #MAVEN_DEBUG_PARAMETER} that controls the level of logging
	 * detail.)
	 */
	private static final boolean debugger = false;

	/**
	 * Name of Maven environment variable
	 * 
	 * @see #debugger
	 * @see #MAVEN_DEBUGGER_OPTIONS
	 */
	private static final String MAVEN_OPTIONS_PROPERTY = "MAVEN_OPTS";

	/**
	 * Value of the Maven environment variable that configures the Verifier
	 * build to wait for a debugger connection.
	 * 
	 * @see #debugger
	 */
	private static final String MAVEN_DEBUGGER_OPTIONS =
		"-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8785";

	/**
	 * The working directory for this test. All paths are relative to this
	 * directory.
	 */
	public static final String WORKING_DIR = "/smoke-test";

	/** Path to the resource directory */
	public static final String PROJECT_PATH = "src/test/resources/smoke-test";

	/**
	 * Maven debug command-line parameter. Enables verbose output to the
	 * Verifier log. (Unrelated to the {@link #debugger} connection setting.)
	 */
	public static final String MAVEN_DEBUG_PARAMETER = "-X";

	/** Maven execution goal */
	public static final String MAVEN_EXECUTION_GOAL = "generate-sources";

	/**
	 * Default path to the tree of Java code that the Verifier builds. See
	 * {@link MyMojo2#GENERATED_SOURCE_PATH}
	 */
	public static final String GENERATED_SOURCE_ROOT =
		"target/generated-sources";

	/** The root of Java code that the Verifier results are checked against */
	public static final String EXPECTED_CODE_ROOT =
		"src/resources/expected-source";

	public SmokeTest() {
		super("(1.0,)"); // only test in 1.0+
	}

	public void testGenerate() {

		Class<? extends SmokeTest> c = getClass();
		File workingDir = null;
		try {
			workingDir =
				ResourceExtractor.simpleExtractResources(c, WORKING_DIR);
		} catch (IOException e) {
			fail(e.toString());
		}
		assertTrue(workingDir != null);

		// Get a copy of the current system properties and optionally configure
		// the Verifier build to wait for a debugger connection
		Properties p = new Properties(System.getProperties());
		if (debugger) {
			p.setProperty(MAVEN_OPTIONS_PROPERTY, MAVEN_DEBUGGER_OPTIONS);
		}

		// Set command-line parameters for Maven
		List<String> cliOptions = new ArrayList<>();
		cliOptions.add(MAVEN_DEBUG_PARAMETER);

		// Create and configure the Verifier
		Verifier verifier = null;
		try {
			verifier = new Verifier(workingDir.getAbsolutePath());
		} catch (VerificationException e) {
			fail(e.toString());
		}
		assertTrue(verifier != null);
		verifier.setCliOptions(cliOptions);

		// Execute the specified goal using the default POM in the test project
		try {
			verifier.executeGoal(MAVEN_EXECUTION_GOAL, p);
		} catch (VerificationException e) {
			fail(e.toString());
		}

		// Check that the file tree of expected code exists
		File f = new File(workingDir, EXPECTED_CODE_ROOT);
		assertTrue(f.exists() && f.canRead());
		Path rootExpectedSource = Paths.get(f.toURI());

		// Check that the file tree of generated code exists
		f = new File(workingDir, GENERATED_SOURCE_ROOT);
		assertTrue(f.exists() && f.canRead());
		Path rootGeneratedSource = Paths.get(f.toURI());

		// Compare the generated tree against the expected tree
		final int maxCollected = MAX_REPORTED_DIFFERENCES + 1;
		DefaultFileContentListener listener = new DefaultFileContentListener(maxCollected,false);
		FileTreeComparator ftc = new FileTreeComparator(rootExpectedSource, rootGeneratedSource);
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
