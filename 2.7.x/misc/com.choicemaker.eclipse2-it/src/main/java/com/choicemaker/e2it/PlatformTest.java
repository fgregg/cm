package com.choicemaker.e2it;

import static org.junit.Assert.assertTrue;

import com.choicemaker.e2.CMExtensionPoint;
import com.choicemaker.e2.CMPlatform;
import com.choicemaker.e2.CMPluginDescriptor;
import com.choicemaker.e2.CMPluginRegistry;

public class PlatformTest {

	private PlatformTest() {
	}
	
	public static void testRegistry(CMPlatform cmp) {
		assertTrue(cmp != null);
		CMPluginRegistry registry = cmp.getPluginRegistry();
		assertTrue(registry != null);
		
		CMPluginDescriptor[] descriptors = registry.getPluginDescriptors();
		assertTrue(descriptors != null);
		assertTrue(descriptors.length > 0);
		
		CMExtensionPoint[] extensionPoints = registry.getExtensionPoints();
		assertTrue(extensionPoints != null);
		assertTrue(extensionPoints.length > 0);
	}

}
