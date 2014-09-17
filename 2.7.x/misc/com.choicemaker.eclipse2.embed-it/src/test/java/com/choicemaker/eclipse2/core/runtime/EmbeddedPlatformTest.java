package com.choicemaker.eclipse2.core.runtime;

import static org.junit.Assert.assertTrue;

import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Test;

import com.choicemaker.cmit.e2.PlatformTest;
import com.choicemaker.eclipse2.embed.EmbeddedPlatform;

public class EmbeddedPlatformTest {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger
			.getLogger(EmbeddedPlatformTest.class.getName());
	
	@BeforeClass
	public static void configureEmbeddedPlatform() {
		String pn = InstallablePlatform.INSTALLABLE_PLATFORM_DISCOVERY;
		String pv = EmbeddedPlatform.class.getName();
		System.setProperty(pn, pv);
	}

	@Test
	public void testEmbeddedPlatform() {
		@SuppressWarnings("unused")
		CMPlatform pd0 = new EmbeddedPlatform();

		CMPlatform pd1 = InstallablePlatform.getInstance();
		assertTrue(pd1 != null);
		
		CMPlatform delegate = InstallablePlatform.getInstance().getDelegate();
		assertTrue(delegate instanceof EmbeddedPlatform);
	}

	@Test
	public void testEmbeddedRegistry() {
		CMPlatform pd = new EmbeddedPlatform();
		PlatformTest.testRegistry(pd);
	}

}
