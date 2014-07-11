package com.choicemaker.fake;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;

/**
 * This test doesn't work, but it may be a useful template or example. Based on
 * the maven-it-sample archetype.<br/>
 * <br/>
 * <strong><em>NOTE:</em></strong> This class is marked abstract so that it is
 * not executed during Maven tests. It is not meant to serve as an actual base
 * class.
 * 
 * @author rphall
 * @deprecated
 */
public abstract class AbstractITmngXXXXDescriptionOfProblemTest extends
		AbstractMavenIntegrationTestCase {

	public static final String GROUPID = "org.apache.maven.its.itsample";

	// TODO: RENAME THIS PATH TO MATCH YOUR ISSUE ID.
	public static final String STEP000_WORKING_DIR = "/mng-xxxx";

	public static final String STEP000_ARTIFACT_ID = "parent";
	public static final String STEP000_VERSION = "1.0";
	public static final String STEP000_PACKAGING = "pom";
	public static final String STEP000_GOAL = "install";

	public static final String STEP001_WORKING_DIR = "checkstyle-assembly";
	public static final String STEP001_ARTIFACT_ID = "checkstyle-assembly";
	public static final String STEP001_VERSION = "1.0";
	public static final String STEP001_PACKAGING = "jar";
	public static final String STEP001_GOAL = "install";

	public static final String STEP002_WORKING_DIR = "checkstyle-test";
	public static final String STEP002_ARTIFACT_ID = "checkstyle-test";
	public static final String STEP002_VERSION = "1.0";
	public static final String STEP002_PACKAGING = "jar";
	public static final String STEP002_GOAL = "install";

	// TODO: RENAME THIS TEST TO SUIT YOUR SCENARIO.
	// Usign the Jira issue id this reproduces is a good
	// start, along with a description:
	// ie MavenITmngXXXXHoustonWeHaveAProblemTest (must end in test)
	public AbstractITmngXXXXDescriptionOfProblemTest() {
		super("(1.0.0,)"); // only test in 1.0.0+
	}

	public void testitMNGxxxx() throws Exception {

		// The testdir is computed from the location of this
		// file.
		File testDir =
			ResourceExtractor.simpleExtractResources(getClass(),
					STEP000_WORKING_DIR);

		Verifier verifier;

		/*
		 * We must first make sure that any artifact created by this test has
		 * been removed from the local repository. Failing to do this could
		 * cause unstable test results. Fortunately, the verifier makes it easy
		 * to do this.
		 */
		verifier = new Verifier(testDir.getAbsolutePath());
		verifier.deleteArtifact(GROUPID, STEP000_ARTIFACT_ID, STEP000_VERSION,
				STEP000_PACKAGING);
		verifier.deleteArtifact(GROUPID, STEP001_ARTIFACT_ID, STEP001_VERSION,
				STEP001_PACKAGING);
		verifier.deleteArtifact(GROUPID, STEP002_ARTIFACT_ID, STEP002_VERSION,
				STEP002_PACKAGING);

		/*
		 * The Command Line Options (CLI) are passed to the verifier as a list.
		 * This is handy for things like redefining the local repository if
		 * needed. In this case, we use the -N flag so that Maven won't recurse.
		 * We are only installing the parent pom to the local repo here.
		 */
		List<String> cliOptions = new ArrayList<>();
		cliOptions.add("-N");
		cliOptions.add("-X");
		verifier.setCliOptions(cliOptions);
		verifier.executeGoal(STEP000_GOAL);

		/*
		 * This is the simplest way to check a build succeeded. It is also the
		 * simplest way to create an IT test: make the build pass when the test
		 * should pass, and make the build fail when the test should fail. There
		 * are other methods supported by the verifier. They can be seen here:
		 * http://maven.apache.org/shared/maven-verifier/apidocs/index.html
		 */
		verifier.verifyErrorFreeLog();

		/*
		 * Reset the streams before executing the verifier again.
		 */
		verifier.resetStreams();

		/*
		 * This particular test requires an extension containing resources to be
		 * installed that is then used by the actual IT test. Here we invoker
		 * Maven again to install it. Again, this is just preparation for the
		 * test.
		 */
		verifier =
			new Verifier(new File(testDir.getAbsolutePath(),
					STEP001_WORKING_DIR).getAbsolutePath());
		verifier.executeGoal(STEP001_GOAL);
		verifier.verifyErrorFreeLog();
		verifier.resetStreams();

		/*
		 * Now we are running the actual test. This particular test will attempt
		 * to load the resources from the extension jar previously installed. If
		 * Maven doesn't pass this to the classpath correctly, the build will
		 * fail. This particular test will fail in Maven <2.0.6.
		 */
		verifier =
			new Verifier(new File(testDir.getAbsolutePath(),
					STEP002_WORKING_DIR).getAbsolutePath());
		verifier.executeGoal(STEP002_GOAL);
		verifier.verifyErrorFreeLog();
		verifier.resetStreams();

		/*
		 * The verifier also supports beanshell scripts for verification of more
		 * complex scenarios. There are plenty of examples in the core-it tests
		 * here:
		 * http://svn.apache.org/repos/asf/maven/core-integration-testing/trunk
		 */
	}
}
