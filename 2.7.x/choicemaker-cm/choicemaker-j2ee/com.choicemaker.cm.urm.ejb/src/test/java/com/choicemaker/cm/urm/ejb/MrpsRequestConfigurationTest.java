package com.choicemaker.cm.urm.ejb;

import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;

public class MrpsRequestConfigurationTest extends TestCase {

	private static String[] EXPECTED_PROPERTY_NAMES = new String[] {
		IMrpsRequestConfiguration.PN_BATCH_SIZE,
		IMrpsRequestConfiguration.PN_USE_DEFAULT_PREFILTER,
		IMrpsRequestConfiguration.PN_DEFAULT_PREFILTER_FROM_PERCENTAGE,
		IMrpsRequestConfiguration.PN_DEFAULT_PREFILTER_TO_PERCENTAGE,
		IMrpsRequestConfiguration.PN_USE_DEFAULT_POSTFILTER,
		IMrpsRequestConfiguration.PN_DEFAULT_POSTFILTER_FROM_PERCENTAGE,
		IMrpsRequestConfiguration.PN_DEFAULT_POSTFILTER_TO_PERCENTAGE,
		IMrpsRequestConfiguration.PN_USE_DEFAULT_PAIR_SAMPLER,
		IMrpsRequestConfiguration.PN_DEFAULT_PAIR_SAMPLER_SIZE };

	protected static void setUpBeforeClass() throws Exception {
	}

	protected static void tearDownAfterClass() throws Exception {
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testStandardizeMrpsSpecificationFileName() {
		String name0 = "some/file/name";
		String name1 = MrpsRequestConfiguration.standardizeMrpsSpecificationFileName(name0);
		assertTrue(name1.endsWith(MrpsRequestConfiguration.MRPS_EXTENSION));

		name0 = name1;
		final int size0 = name0.length();
		name1 = MrpsRequestConfiguration.standardizeMrpsSpecificationFileName(name0);		
		final int size1 = name1.length();
		assertTrue(size0 == size1);
	}

	public void testMrpsRequestConfiguration() {
		MrpsRequestConfiguration mrc = new MrpsRequestConfiguration();

		Properties p = mrc.getProperties();
		assertTrue(p != null);
		assertTrue(p.size() > 0);

		Set ap = mrc.getAllowedPropertyNames();
		assertTrue(ap != null);
		for (int i = 0; i < EXPECTED_PROPERTY_NAMES.length; i++) {
			assertTrue("Missing '" + EXPECTED_PROPERTY_NAMES[i] + "'",
					ap.contains(EXPECTED_PROPERTY_NAMES[i]));
		}
	}

	public void testMrpsRequestConfigurationMrpsRequestConfiguration() {
		MrpsRequestConfiguration mrc = new MrpsRequestConfiguration();
		Properties p = new Properties();
		for (int i = 0; i < EXPECTED_PROPERTY_NAMES.length; i++) {
			String pn = EXPECTED_PROPERTY_NAMES[i];
			String pv = "FAKE " + pn;
			p.put(pn, pv);
		}
		mrc.setProperties(p);

		MrpsRequestConfiguration mrc2 = new MrpsRequestConfiguration(mrc);
		Properties p2 = mrc2.getProperties();
		assertTrue(p.equals(p2));
	}

	public void testGetProperty() {
		MrpsRequestConfiguration mrc = new MrpsRequestConfiguration();
		Properties p = new Properties();
		for (int i = 0; i < EXPECTED_PROPERTY_NAMES.length; i++) {
			String pn = EXPECTED_PROPERTY_NAMES[i];
			String pv = "FAKE " + pn;
			p.put(pn, pv);
		}
		mrc.setProperties(p);

		for (int i = 0; i < EXPECTED_PROPERTY_NAMES.length; i++) {
			String pn = EXPECTED_PROPERTY_NAMES[i];
			String pv = mrc.getProperty(pn);
			assertTrue(("FAKE " + pn).equals(pv));
		}
	}

	public void testRemoveProperty() {
		MrpsRequestConfiguration mrc = new MrpsRequestConfiguration();
		Properties p = new Properties();
		for (int i = 0; i < EXPECTED_PROPERTY_NAMES.length; i++) {
			String pn = EXPECTED_PROPERTY_NAMES[i];
			String pv = "FAKE " + pn;
			p.put(pn, pv);
		}

		mrc.setProperties(p);
		for (int i = 0; i < EXPECTED_PROPERTY_NAMES.length; i++) {
			String pn = EXPECTED_PROPERTY_NAMES[i];
			mrc.removeProperty(pn);
		}

		Properties p2 = mrc.getProperties();
		assertTrue(p2 != null);
		assertTrue(p2.size() == 0);
	}

	public void testSetProperty() {
		MrpsRequestConfiguration mrc = new MrpsRequestConfiguration();
		Properties p = new Properties();
		mrc.setProperties(p);
		Properties p2 = mrc.getProperties();
		assertTrue(p2 != null);
		assertTrue(p2.size() == 0);

		for (int i = 0; i < EXPECTED_PROPERTY_NAMES.length; i++) {
			String pn = EXPECTED_PROPERTY_NAMES[i];
			String pv = "FAKE " + pn;
			mrc.setProperty(pn, pv);
		}

		Properties p3 = mrc.getProperties();
		assertTrue(p3 != null);
		assertTrue(p3.size() == EXPECTED_PROPERTY_NAMES.length);
		for (int i = 0; i < EXPECTED_PROPERTY_NAMES.length; i++) {
			String pn = EXPECTED_PROPERTY_NAMES[i];
			final String expected = "FAKE " + pn;
			final String pv = mrc.getProperty(pn);
			assertTrue(expected.equals(pv));
		}
	}

	public void testClone() {
		MrpsRequestConfiguration mrc = new MrpsRequestConfiguration();
		Properties p = new Properties();
		for (int i = 0; i < EXPECTED_PROPERTY_NAMES.length; i++) {
			String pn = EXPECTED_PROPERTY_NAMES[i];
			String pv = "FAKE " + pn;
			p.put(pn, pv);
		}
		mrc.setProperties(p);

		MrpsRequestConfiguration mrc2 = null;
		try {
			mrc2 = (MrpsRequestConfiguration) mrc.clone();
		} catch (CloneNotSupportedException e) {
			fail(e.toString());
		}
		Properties p2 = mrc2.getProperties();
		assertTrue(p2 != p);
		assertTrue(p2.equals(p));
	}

	public void testGetVersion() {
		MrpsRequestConfiguration mrc = new MrpsRequestConfiguration();
		String version = mrc.getVersion();
		assertTrue(version != null);
		assertTrue(!version.trim().isEmpty());
	}

}
