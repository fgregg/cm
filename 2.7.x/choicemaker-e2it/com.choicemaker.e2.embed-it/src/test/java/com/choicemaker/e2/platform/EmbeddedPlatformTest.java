package com.choicemaker.e2.platform;

import static org.junit.Assert.assertTrue;

import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Test;

import com.choicemaker.e2.CMPlatform;
import com.choicemaker.e2.embed.EmbeddedPlatform;
import com.choicemaker.e2it.PlatformTest;

public class EmbeddedPlatformTest {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger
			.getLogger(EmbeddedPlatformTest.class.getName());
	
	@BeforeClass
	public static void configureEmbeddedPlatform() {
		EmbeddedPlatform.install();
	}

	@Test
	public void testEmbeddedPlatform() {
		@SuppressWarnings("unused")
		CMPlatform ep0 = new EmbeddedPlatform();

		CMPlatform ep1 = InstallablePlatform.getInstance();
		assertTrue(ep1 != null);
		
		CMPlatform delegate = InstallablePlatform.getInstance().getDelegate();
		assertTrue(delegate instanceof EmbeddedPlatform);
	}

	@Test
	public void testEmbeddedRegistry() {
		CMPlatform ep = new EmbeddedPlatform();
		PlatformTest.testRegistry(ep);
	}

}
