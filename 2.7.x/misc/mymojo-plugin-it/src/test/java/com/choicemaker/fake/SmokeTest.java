package com.choicemaker.fake;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;

import com.choicemaker.fake.SourceComparisonListener.FILE_COMPARISON;
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

	private static final String MORE_DIFFERENCES_INDICATOR = "...";

	/** Files excluded from validity checks */
	private static final Set<Path> EXCLUDED_COMPARISONS = new HashSet<>();
	static {
		// ClueSets and CluesAccessors can differ by a build date (dumb
		// versioning)
		EXCLUDED_COMPARISONS
				.add(Paths
						.get("com/choicemaker/demo/simple_person_matching/gendata/gend/internal/Person/SimplePersonCluesAccessor.java"));
		EXCLUDED_COMPARISONS
				.add(Paths
						.get("com/choicemaker/demo/simple_person_matching/gendata/gend/internal/Person/SimplePersonCluesClueSet.java"));
		EXCLUDED_COMPARISONS
				.add(Paths
						.get("com/choicemaker/demo/simple_person_matching/gendata/gend/internal/Person2/SimplePersonClues2Accessor.java"));
		EXCLUDED_COMPARISONS
				.add(Paths
						.get("com/choicemaker/demo/simple_person_matching/gendata/gend/internal/Person2/SimplePersonClues2ClueSet.java"));
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

		// Walk the expected tree first, checking the corresponding path in
		// generated tree
		SourceComparison visitor =
			new SourceComparison(rootExpectedSource, rootGeneratedSource);
		SourceComparisonListener scl = new DefaultSourceComparisonListener();
		visitor.addListener(scl);
		visitor.addExcludedPaths(EXCLUDED_COMPARISONS);
		try {
			Files.walkFileTree(rootExpectedSource, visitor);
		} catch (IOException e) {
			fail(e.toString());
		}
		List<String> differences = scl.getResults();
		assertTrue(differences != null);
		if (!differences.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (String difference : differences) {
				sb.append(difference).append(EOL);
				if (differences.size() > MAX_REPORTED_DIFFERENCES) {
					sb.append(MORE_DIFFERENCES_INDICATOR).append(EOL);
					break;
				}
			}
			fail(sb.toString());
		}
		assertTrue(differences.isEmpty());

		// Walk the generated tree next, checking the corresponding path in
		// expected tree
		visitor =
			new SourceComparison(rootGeneratedSource, rootExpectedSource);
		scl.clear();
		visitor.addListener(scl);
		visitor.addExcludedPaths(EXCLUDED_COMPARISONS);
		try {
			Files.walkFileTree(rootGeneratedSource, visitor);
		} catch (IOException e) {
			fail(e.toString());
		}
		assertTrue(differences != null);
		differences = scl.getResults();
		assertTrue(differences != null);
		if (!differences.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (String difference : differences) {
				sb.append(difference).append(EOL);
				if (differences.size() > MAX_REPORTED_DIFFERENCES) {
					sb.append(MORE_DIFFERENCES_INDICATOR).append(EOL);
					break;
				}
			}
			fail(sb.toString());
		}
		assertTrue(differences.isEmpty());

	}
}

class DefaultSourceComparisonListener implements SourceComparisonListener {

	private static final String MSGPREFIX_SAME = "Same content: ";
	private static final String MSGPREFIX_DIFFERENT = "Different content: ";
	private static final String MSGPREFIX_REF_ONLY = "Only in one tree: ";
	private static final String MSGPREFIX_UNREACHABLE =
		"Unreachable reference file: ";

	private final boolean printResultIfSame;
	private List<String> results = new LinkedList<>();;

	DefaultSourceComparisonListener() {
		this(false);
	}

	DefaultSourceComparisonListener(boolean printSames) {
		this.printResultIfSame = printSames;
	}

	@Override
	public void fileComparison(Path p, FILE_COMPARISON result) {
		assert p != null;
		assert result != null;
		String msg = null;
		switch (result) {
		case ONLY_IN_REFERENCE:
			msg = MSGPREFIX_REF_ONLY + p.toString();
			break;
		case DIFFERENT_CONTENT:
			msg = MSGPREFIX_DIFFERENT + p.toString();
			break;
		case SAME_CONTENT:
			if (printResultIfSame) {
				msg = MSGPREFIX_SAME + p.toString();
			}
			break;
		case UNREACHABLE_REFERENCE:
			msg = MSGPREFIX_UNREACHABLE + p.toString();
			break;
		default:
			throw new Error("Unexpected file comparison result: " + result);
		}
		if (msg != null) {
			results.add(msg);
		}
	}

	@Override
	public void clear() {
		results.clear();
	}

	@Override
	public List<String> getResults() {
		return Collections.unmodifiableList(results);
	}

}

interface SourceComparisonListener {

	enum FILE_COMPARISON {
		ONLY_IN_REFERENCE, DIFFERENT_CONTENT, SAME_CONTENT,
		UNREACHABLE_REFERENCE
	}

	void fileComparison(Path p, FILE_COMPARISON result);

	void clear();

	List<String> getResults();

}

class SourceComparison implements FileVisitor<Path> {

	private final Path thisRoot;
	private final Path thatRoot;
	private final Set<Path> excludedPaths = new HashSet<>();
	private final Set<SourceComparisonListener> listeners = new HashSet<>();

	/**
	 * Compares two file trees, rooted at <code>thisRoot</code> and
	 * <code>thatRoot</code>, respectively. The tree rooted at
	 * <code>thisRoot</code> is the reference tree. This class assumes that the
	 * reference tree is being {@link Files#walkFileTree(Path, FileVisitor)
	 * walked} and that paths in the reference tree are compared to the
	 * corresponding relative paths in the other tree. As each file in the
	 * reference tree is encountered, one of four results is reported to any
	 * registered listeners:
	 * <ul>
	 * <li>The file appears only in the reference tree; see
	 * {@link FILE_COMPARISON#ONLY_IN_REFERENCE}</li>
	 * <li>The file has the same content in the reference and comparison tree;
	 * see {@link FILE_COMPARISON#SAME_CONTENT}</li>
	 * <li>The file has the different content in the reference than in the
	 * comparison tree; see {@link FILE_COMPARISON#DIFFERENT_CONTENT}</li>
	 * <li>An error occurred while trying to read the reference file; see
	 * {@link FILE_COMPARISON#UNREACHABLE_REFERENCE}</li>
	 * </ul>
	 * 
	 * @param thisRoot
	 * @param thatRoot
	 */
	SourceComparison(Path thisRoot, Path thatRoot) {
		if (thisRoot == null) {
			throw new IllegalArgumentException();
		}
		if (thatRoot == null) {
			throw new IllegalArgumentException();
		}
		this.thisRoot = thisRoot;
		this.thatRoot = thatRoot;
	}

	public void addExcludedPath(Path p) {
		excludedPaths.add(p);
	}

	public void addExcludedPaths(Set<Path> paths) {
		excludedPaths.addAll(paths);
	}

	public void addListener(SourceComparisonListener l) {
		listeners.add(l);
	}

	protected void notifyListeners(Path p, FILE_COMPARISON result) {
		for (SourceComparisonListener l : listeners) {
			l.fileComparison(p, result);
		}
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
			throws IOException {
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path reference, BasicFileAttributes attrs)
			throws IOException {
		Path relative = this.thisRoot.relativize(reference);
		Path other = thatRoot.resolve(relative);
		if (!this.excludedPaths.contains(relative)
				&& !this.excludedPaths.contains(other)) {

			File fOther = other.toFile();
			if (fOther.exists()) {
				File fRef = reference.toFile();
				assert fRef.exists();
				String md5Ref =
					FileUtilities.computeHash(FileUtilities.MD5_HASH_ALGORITHM,
							fRef);
				String md5Other =
					FileUtilities.computeHash(FileUtilities.MD5_HASH_ALGORITHM,
							fOther);
				if (md5Ref.equals(md5Other)) {
					notifyListeners(relative, FILE_COMPARISON.SAME_CONTENT);
				} else {
					notifyListeners(relative, FILE_COMPARISON.DIFFERENT_CONTENT);
				}
			} else {
				File fRef = reference.toFile();
				assert fRef.exists();
				notifyListeners(relative, FILE_COMPARISON.ONLY_IN_REFERENCE);
			}
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path reference, IOException exc)
			throws IOException {
		Path relative = this.thisRoot.relativize(reference);
		notifyListeners(relative, FILE_COMPARISON.UNREACHABLE_REFERENCE);
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc)
			throws IOException {
		return FileVisitResult.CONTINUE;
	}

}
