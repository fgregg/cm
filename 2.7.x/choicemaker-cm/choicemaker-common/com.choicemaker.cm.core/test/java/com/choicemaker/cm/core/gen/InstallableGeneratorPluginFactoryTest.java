package com.choicemaker.cm.core.gen;

import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

public class InstallableGeneratorPluginFactoryTest extends TestCase {

	public void setUp() {
		// The default list of generator plugins must be overridden for
		// this test because it may contain FQCNs that are not on the
		// limited classpath used for testing
		System.setProperty(
				ListBackedGeneratorPluginFactory.PROPERTY_GENERATOR_PLUGIN_FACTORIES,
				ListBackedGeneratorPluginFactoryTest.TEST_LIST_ABSOLUTE_FILENAME);
		assertTrue(System
				.getProperty(ListBackedGeneratorPluginFactory.PROPERTY_GENERATOR_PLUGIN_FACTORIES) != null);
	}

	public void tearDown() {
		Properties p = System.getProperties();
		p.remove(ListBackedGeneratorPluginFactory.PROPERTY_GENERATOR_PLUGIN_FACTORIES);
		assertTrue(System
				.getProperty(ListBackedGeneratorPluginFactory.PROPERTY_GENERATOR_PLUGIN_FACTORIES) == null);
	}

	public void testGetInstance() {
		IGeneratorPluginFactory gpf = InstallableGeneratorPluginFactory
				.getInstance();
		assertTrue(gpf != null);
	}

	public void testGetDefaultGeneratorPluginFactory() {
		IGeneratorPluginFactory gpf = InstallableGeneratorPluginFactory
				.getDefaultGeneratorPluginFactory();
		assertTrue(gpf != null);
	}

	public void testInstallableGeneratorPluginFactory() {
		// Check behavior when PROPERTY_GENERATOR_PLUGIN_FACTORIES is not set
		String fqcn = System
				.getProperty(InstallableGeneratorPluginFactory.PROPERTY_INSTALLABLE_GENERATOR_PLUGIN_FACTORY);
		assertTrue(fqcn == null);
		InstallableGeneratorPluginFactory igpf = new InstallableGeneratorPluginFactory();
		IGeneratorPluginFactory delegate = igpf.getDelegate();
		assertTrue(delegate != null);
		Class delegateClass = delegate.getClass();
		IGeneratorPluginFactory defaultGPF = InstallableGeneratorPluginFactory
				.getDefaultGeneratorPluginFactory();
		Class defaultClass = defaultGPF.getClass();
		assertTrue(delegateClass.equals(defaultClass));

		// Check behavior when PROPERTY_GENERATOR_PLUGIN_FACTORIES is set
		Class c = ListBackedGeneratorPluginFactory.class;
		String className = c.getName();
		System.setProperty(
				InstallableGeneratorPluginFactory.PROPERTY_INSTALLABLE_GENERATOR_PLUGIN_FACTORY,
				className);
		fqcn = System
				.getProperty(InstallableGeneratorPluginFactory.PROPERTY_INSTALLABLE_GENERATOR_PLUGIN_FACTORY);
		assertTrue(fqcn.equals(className));
		igpf = new InstallableGeneratorPluginFactory();
		delegate = igpf.getDelegate();
		assertTrue(delegate != null);
		delegateClass = delegate.getClass();
		assertTrue(delegateClass.equals(c));

		// The following 'getDelegate()' invocation fails in
		// this plain old JUnit test because the Eclipse platform
		// is not initialized and running
		c = Eclipse2GeneratorPluginFactory.class;
		className = c.getName();
		System.setProperty(
				InstallableGeneratorPluginFactory.PROPERTY_INSTALLABLE_GENERATOR_PLUGIN_FACTORY,
				className);
		fqcn = System
				.getProperty(InstallableGeneratorPluginFactory.PROPERTY_INSTALLABLE_GENERATOR_PLUGIN_FACTORY);
		assertTrue(fqcn.equals(className));
		igpf = new InstallableGeneratorPluginFactory();
		delegate = null;
		try {
			delegate = igpf.getDelegate();
			assertTrue(delegate != null);
			fail("getDelegate() method should have failed because "
					+ "the Eclipse framework shouldn't be running");
		} catch (IllegalStateException x) {
			assertTrue(delegate == null);
		}
	}

	public void testLookupGeneratorPlugins() throws GenException {
		// This set operation is also performed in the setUp() method
		// prior to this test. It is repeated here for clarity, since
		// this test validate and counts the entries in the configured
		// list of generator plugins.
		System.setProperty(
				ListBackedGeneratorPluginFactory.PROPERTY_GENERATOR_PLUGIN_FACTORIES,
				ListBackedGeneratorPluginFactoryTest.TEST_LIST_ABSOLUTE_FILENAME);

		Class c = ListBackedGeneratorPluginFactory.class;
		String className = c.getName();
		System.setProperty(
				InstallableGeneratorPluginFactory.PROPERTY_INSTALLABLE_GENERATOR_PLUGIN_FACTORY,
				className);
		InstallableGeneratorPluginFactory igpf = new InstallableGeneratorPluginFactory();
		List generatorPlugins = igpf.lookupGeneratorPlugins();
		int count = ListBackedGeneratorPluginFactoryTest
				.validateAndCountGeneratorPluginList(generatorPlugins);
		assertTrue(count == ListBackedGeneratorPluginFactoryTest.TEST_LIST_COUNT);
	}

}
