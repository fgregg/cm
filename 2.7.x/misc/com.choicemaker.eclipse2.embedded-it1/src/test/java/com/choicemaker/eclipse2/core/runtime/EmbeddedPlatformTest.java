package com.choicemaker.eclipse2.core.runtime;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Test;

import com.choicemaker.eclipse2.embed.runtime.EmbeddedPlatform;

public class EmbeddedPlatformTest {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger
			.getLogger(EmbeddedPlatformTest.class.getName());
	
	@BeforeClass
	public void configureEmbeddedPlatform() {
		String pn = InstallablePlatform.INSTALLABLE_PLATFORM_DISCOVERY;
		String pv = EmbeddedPlatform.class.getName();
		System.setProperty(pn, pv);
	}

	@Test
	public void testEmbeddedPlatform() {
		@SuppressWarnings("unused")
		CMPlatform pd0 = new EmbeddedPlatform();
		fail("not yet implemented");

		CMPlatform pd1 = InstallablePlatform.getInstance();
		fail("not yet implemented");
		
		CMPlatform delegate = InstallablePlatform.getInstance().getDelegate();
		assertTrue(delegate instanceof EmbeddedPlatform);
	}

	@Test
	public void testEmbeddedRegistry() {
		CMPlatform pd = InstallablePlatform.getInstance();
		CMPluginRegistry registry = pd.getPluginRegistry();
		assertTrue(registry != null);
		
		CMPluginDescriptor[] descriptors = registry.getPluginDescriptors();
		assertTrue(descriptors != null);
		assertTrue(descriptors.length > 0);
		
		CMExtensionPoint[] extensionPoints = registry.getExtensionPoints();
		assertTrue(extensionPoints != null);
		assertTrue(extensionPoints.length > 0);
	}

}
