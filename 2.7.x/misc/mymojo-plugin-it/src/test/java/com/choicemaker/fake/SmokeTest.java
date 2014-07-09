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
import java.util.Properties;

import org.apache.maven.it.Verifier;

/**
 * Based on the maven-it-sample archetype.
 *
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a>
 * @version $Id: MavenITmngXXXXDescriptionOfProblemTest.java 707999 2008-10-26
 *          14:42:38Z bentmann $
 */
public class SmokeTest extends AbstractMavenIntegrationTestCase {

	// /**
	// * Path to the log4j configuration for this test, relative to the project
	// * POM
	// */
	// public static final String LOG4J_PATH = "src/test/resources/log4j.xml";
	//
	// /** Log4j configuration property */
	// public static final String LOG4J_PROPERTY = "log4j.configuration";

	/** Set to true to enable debugger connection */
	private static final boolean debugger = true;

	/**
	 * Path to the resource directory for this test, relative to the project POM
	 */
	public static final String PROJECT_PATH = "src/test/resources/smoke-test";

	/**
	 * Path to the generated-sources directory, relative to the project POM
	 */
	public static final String GENERATED_SOURCES_PATH = "target/generated-sources";

	/** Property in the test POM */
	public static final String GENERATED_SOURCES_PROPERTY = "smoke-test-generated-sources";

	/** Maven debug property */
	public static final String MAVEN_DEBUG_PARAMETER = "-X";

	public static final String MAVEN_OPTIONS_PROPERTY =
			"MAVEN_OPTS";

	public static final String MAVEN_DEBUGGER_OPTIONS =
			"-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8785";

	/** Maven execution goal */
	public static final String MAVEN_EXECUTION_GOAL = "generate-sources";

	public SmokeTest() {
		super("(1.0,)"); // only test in 1.0+
	}

	public void testGenerate() throws Exception {

		// Get a copy of the current system properties
		Properties p = new Properties(System.getProperties());

		// Set properties used in generation
		File f = new File(GENERATED_SOURCES_PATH);
		if (!f.exists()) {
			f.mkdirs();
		}
		assertTrue(f.exists());
		p.setProperty(GENERATED_SOURCES_PROPERTY, f.getAbsolutePath());

		if (debugger) {
			p.setProperty(MAVEN_OPTIONS_PROPERTY, MAVEN_DEBUGGER_OPTIONS);
		}

		// Set command-line parameters for Maven
		List<String> cliOptions = new ArrayList<>();
		cliOptions.add(MAVEN_DEBUG_PARAMETER);

		// Verify that the default POM in the project executes the specified
		// goal
		f = new File(PROJECT_PATH);
		assertTrue(f.exists());
		Verifier verifier = new Verifier(f.getAbsolutePath());
		verifier.setCliOptions(cliOptions);
		verifier.executeGoal(MAVEN_EXECUTION_GOAL, p);

	}
}
