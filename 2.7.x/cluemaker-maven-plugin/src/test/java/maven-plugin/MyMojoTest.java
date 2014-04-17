package com.choicemaker.maven;

import java.io.File;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;

public class MyMojoTest
    extends AbstractMojoTestCase
{
    /** {@inheritDoc} */
    protected void setUp()
        throws Exception
    {
        // required
        super.setUp();
    }

    /** {@inheritDoc} */
    protected void tearDown()
        throws Exception
    {
        // required
        super.tearDown();
    }

    /**
     * @throws Exception if any
     */
    public void testSomething()
        throws Exception
    {
        File pom = getTestFile( "src/test/resources/unit/mymojo/test-pom.xml" );
        assertNotNull( pom );
        assertTrue( pom.exists() );

        MyMojo myMojo = (MyMojo) lookupMojo( "touch", pom );
        assertNotNull( myMojo );
        myMojo.execute();
    }
}
