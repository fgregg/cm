package com.choicemaker.cm.core.gen;

import junit.framework.TestCase;

//public class InstallableGeneratorPluginFactoryTest extends TestCase {

	public void testInstallIGeneratorPluginFactory() {
		fail("Not yet implemented");
	}

	public void testInstallString() {
		fail("Not yet implemented");
	}

	public void testGetInstance() {
		fail("Not yet implemented");
	}

	public void testLookupGeneratorPlugins() {
		fail("Not yet implemented");
	}

	public void testInstallableGeneratorPluginFactory() {
		String fqcn = System.getProperty(InstallableGeneratorPluginFactory);
		assertTrue(fqcn == null);
		InstallableGeneratorPluginFactory igpf = new InstallableGeneratorPluginFactory();
		fail("Not yet implemented");
	}

}
