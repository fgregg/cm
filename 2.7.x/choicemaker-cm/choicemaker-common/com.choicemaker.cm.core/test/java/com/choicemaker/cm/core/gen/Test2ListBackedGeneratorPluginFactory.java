package com.choicemaker.cm.core.gen;

import junit.framework.TestCase;

/**
 * This junit test deliberately violates the JUnit naming convention so that it
 * will not be automatically included in Maven builds. The test requires
 * ChoiceMaker libraries that can not be linked to the com.choicemaker.cm.core
 * library, even within a test scope, because the linkages would introduce
 * dependency loops. Instead, this test must be run manually, with the required
 * libraries on the Java class path.<br/>
 * <br/>
 * The required libraries are:
 * <ul>
 * <li>com.choicemaker.cm.compiler</li>
 * <li>com.choicemaker.cm.io.blocking.automated.base</li>
 * <li>com.choicemaker.cm.io.blocking.exact.base</li>
 * <li>com.choicemaker.cm.io.db.base</li>
 * <li>com.choicemaker.cm.io.flatfile.base</li>
 * <li>com.choicemaker.cm.io.xml.base</li>
 * </ul>
 *
 * @author rphall
 *
 */
public class Test2ListBackedGeneratorPluginFactory extends TestCase {

	public void testListBackedGeneratorPluginFactory() {
		fail("Not yet implemented");
	}

}
