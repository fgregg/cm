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

import org.apache.maven.it.Verifier;

/**
 * Based on the maven-it-sample archetype.
 *
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a>
 * @version $Id: MavenITmngXXXXDescriptionOfProblemTest.java 707999 2008-10-26 14:42:38Z bentmann $
 */
public class SmokeTest
    extends AbstractMavenIntegrationTestCase
{

    public static final String GROUPID = "com.choicemaker.fake";

    // TODO: RENAME THIS PATH TO MATCH YOUR ISSUE ID.
    public static final String STEP000_WORKING_DIR = "/smoke-test";

    public static final String STEP000_ARTIFACT_ID = "smoke-test";
    public static final String STEP000_VERSION = "1.0-SNAPSHOT";
    public static final String STEP000_PACKAGING = "jar";

    public static final String STEP001_GOAL = "generate";

	public SmokeTest() {
		super("(1.0,)"); // only test in 1.0+
	}

	public void testGenerate() throws Exception {

		String MY_PROJECT_PATH = "src/test/resources/smoke-test";
//		String MY_PROJECT_PATH = "."; // Relative to POM
//		String MY_PROJECT_PATH = "/"; // Absolute (File system root)
		File MY_PROJECT = new File(MY_PROJECT_PATH);
		assertTrue(MY_PROJECT.exists());
		String absPath = MY_PROJECT.getAbsolutePath();
		Verifier verifier = new Verifier(absPath);
		verifier.executeGoal("generate-sources");

//    	// The root of the test directories
//        File testDir = ResourceExtractor.simpleExtractResources( getClass(), STEP000_WORKING_DIR );
//
//        // Create or cleanup the test directories
//        Verifier verifier = new Verifier( testDir.getAbsolutePath() );
//        verifier.deleteArtifact( GROUPID, STEP000_ARTIFACT_ID, STEP000_VERSION, STEP000_PACKAGING );
//
//        // Set the command-line options
//        List<String> cliOptions = new ArrayList<String>();
//        cliOptions.add( "-N" );
//        cliOptions.add( "-X" );
//        verifier.setCliOptions( cliOptions );
//
//        // Test step 1 (generation)
//        verifier = new Verifier( new File( testDir.getAbsolutePath(), STEP000_WORKING_DIR ).getAbsolutePath() );
//        verifier.executeGoal( STEP001_GOAL );
//        verifier.verifyErrorFreeLog();
////      verifier.resetStreams();
//
////        /*
////         * Now we are running the actual test. This
////         * particular test will attempt to load the
////         * resources from the extension jar previously
////         * installed. If Maven doesn't pass this to the
////         * classpath correctly, the build will fail. This
////         * particular test will fail in Maven <2.0.6.
////         */
////        verifier = new Verifier( new File( testDir.getAbsolutePath(), STEP002_WORKING_DIR ).getAbsolutePath() );
////        verifier.executeGoal( STEP002_GOAL );
////        verifier.verifyErrorFreeLog();
////        verifier.resetStreams();
////
////        /*
////         * The verifier also supports beanshell scripts for
////         * verification of more complex scenarios. There are
////         * plenty of examples in the core-it tests here:
////         * http://svn.apache.org/repos/asf/maven/core-integration-testing/trunk
////         */
    }
}
