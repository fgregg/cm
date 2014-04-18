package com.choicemaker.maven;

import java.io.File;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;

public class MyMojoTest extends AbstractMojoTestCase {

    protected static final String pomDir = "src/test/resources/unit/mymojo/";

    protected static final String[] poms = { "test-pom.xml", "test-pom-02.xml" };

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
	// required
	super.setUp();
    }

    /** {@inheritDoc} */
    protected void tearDown() throws Exception {
	// required
	super.tearDown();
    }

    /**
     * @throws Exception
     *             if any
     */
    public void testSomething() throws Exception {
	for (String p : poms) {
	    try {
		File pom = getTestFile(pomDir + p);
		assertNotNull(pom);
		assertTrue(pom.exists());

		MyMojo myMojo = (MyMojo) lookupMojo("touch", pom);
		assertNotNull(myMojo);
		myMojo.execute();

	    } catch (Exception x) {
		String msg = "Exception while processing '" + p + "': "
			+ x.toString();
		fail(msg);
	    }
	}
    }
}
