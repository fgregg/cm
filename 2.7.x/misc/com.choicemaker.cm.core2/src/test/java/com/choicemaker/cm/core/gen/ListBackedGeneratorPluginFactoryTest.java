package com.choicemaker.cm.core.gen;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

public class ListBackedGeneratorPluginFactoryTest extends TestCase {

	public static final String TEST_LIST_FILENAME = "testGeneratorPlugins.txt";

	public static final String TEST_LIST_ABSOLUTE_FILENAME = "/com/choicemaker/cm/core/gen/testGeneratorPlugins.txt";

	public static final int TEST_LIST_COUNT = 1;

	public static int validateAndCountGeneratorPluginList(List list) {
		assertTrue(list != null);
		int retVal = 0;
		for (Iterator i = list.iterator(); i.hasNext();) {
			Object o = i.next();
			assertTrue(o != null);
			assertTrue(o instanceof GeneratorPlugin);
			++retVal;
		}
		return retVal;
	}

	public void testListBackedGeneratorPluginFactoryString()
			throws GenException {
		IGeneratorPluginFactory test = new ListBackedGeneratorPluginFactory(
				TEST_LIST_FILENAME);
		List generatorPlugins = test.lookupGeneratorPlugins();
		int count = validateAndCountGeneratorPluginList(generatorPlugins);
		assert (count == TEST_LIST_COUNT);
	}

	public void testListBackedGeneratorPluginFactoryList()
			throws GenException {
		List list = new LinkedList();
		IGeneratorPluginFactory test = new ListBackedGeneratorPluginFactory(
				list);
		List generatorPlugins = test.lookupGeneratorPlugins();
		int count = validateAndCountGeneratorPluginList(generatorPlugins);
		assert (count == 0);

		list.add(new FakeGeneratorPlugin());
		test = new ListBackedGeneratorPluginFactory(list);
		generatorPlugins = test.lookupGeneratorPlugins();
		count = validateAndCountGeneratorPluginList(generatorPlugins);
		assert (count == 1);
	}

	public void testLoad() {
		System.setProperty(
				ListBackedGeneratorPluginFactory.PROPERTY_GENERATOR_PLUGIN_FACTORIES,
				TEST_LIST_ABSOLUTE_FILENAME);
		List list = ListBackedGeneratorPluginFactory.load();
		int count = validateAndCountGeneratorPluginList(list);
		assert (count == TEST_LIST_COUNT);
	}

	public void testLoadString() {
		List list = ListBackedGeneratorPluginFactory
				.load(TEST_LIST_ABSOLUTE_FILENAME);
		int count = validateAndCountGeneratorPluginList(list);
		assert (count == TEST_LIST_COUNT);
	}

	public void testValidateAndCopy() {
		List list = new LinkedList();
		List list2 = ListBackedGeneratorPluginFactory.validateAndCopy(list);
		int count = validateAndCountGeneratorPluginList(list2);
		assert (count == 0);

		list.add(new FakeGeneratorPlugin());
		list2 = ListBackedGeneratorPluginFactory.validateAndCopy(list2);
		count = validateAndCountGeneratorPluginList(list);
		assert (count == 1);
	}

}
