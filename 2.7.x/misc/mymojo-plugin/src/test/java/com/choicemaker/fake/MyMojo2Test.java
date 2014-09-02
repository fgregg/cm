package com.choicemaker.fake;

import java.io.File;

import org.apache.commons.io.FileUtils;
import java.util.logging.Logger;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;

public class MyMojo2Test extends AbstractMojoTestCase {

	private static final Logger logger = Logger.getLogger(MyMojo2Test.class.getName());

	protected static final String TEST_PROJECT_PATH = "src/test/resources/mymojo2/";

	protected static final String[] POMS = { "pom-1.xml", "pom-2.xml",
			"pom-3.xml", "pom-4.xml" };

	/** {@inheritDoc} */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/** {@inheritDoc} */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * @throws Exception
	 *             if any
	 */
	public void testConfiguration() throws Exception {

		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		logger.fine("ClassLoader: " + cl.getClass().getName());

		for (String p : POMS) {
			int index = p.indexOf(".xml");
			String baseName = p.substring(0, index);
			File projectDirectory = getTestFile("src/test/resources/"
					+ baseName);
			File buildDirectory = getTestFile("target/project-" + baseName
					+ "-test");

			File pom = getTestFile(TEST_PROJECT_PATH + p);
			assertNotNull(pom);
			assertTrue(pom.exists());

			// Create the MavenProject from the pom.xml file
			MavenExecutionRequest executionRequest = new DefaultMavenExecutionRequest();
			ProjectBuildingRequest buildingRequest = executionRequest
					.getProjectBuildingRequest();
			ProjectBuilder projectBuilder = this.lookup(ProjectBuilder.class);
			MavenProject project = projectBuilder.build(pom, buildingRequest)
					.getProject();
			assertNotNull(project);

			// Set the base directory (or it will write to src/test/resources/)
			Build build = project.getModel().getBuild();
			build.setDirectory(buildDirectory.getPath());
			File outputDirectory = new File(buildDirectory, "target/classes");
			build.setOutputDirectory(outputDirectory.getPath());

			// Copy resources
			File source = new File(projectDirectory, "src/main/resources");
			if (source.exists()) {
				FileUtils.copyDirectory(source, outputDirectory);
			}

			// Load the Mojo
			MyMojo2 myMojo = (MyMojo2) this.lookupConfiguredMojo(project,
					"generate");
			assertNotNull(myMojo);

			// Set the MavenProject on the Mojo (AbstractMojoTestCase does not
			// do this by default)
			setVariableValueToObject(myMojo, "project", project);

			// Execute the plugin (exceptions expected)
			try {
				myMojo.execute();
			} catch (MojoExecutionException x) {
				// Unfortunately, these errors are expected in this unit test,
				// because the ${plugin.artifacts} collection is not correctly
				// set outside of a real Maven environment. See
				// mymojo-maven-plugin-it for a (slow) integration test that
				// runs within a real Maven environment and tests for error-free
				// code generation.
			}
		}
	}
}
