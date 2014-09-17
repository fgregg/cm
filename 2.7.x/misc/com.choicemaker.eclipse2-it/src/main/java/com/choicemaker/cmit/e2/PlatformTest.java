package com.choicemaker.cmit.e2;

import static org.junit.Assert.assertTrue;

import com.choicemaker.eclipse2.core.runtime.CMExtensionPoint;
import com.choicemaker.eclipse2.core.runtime.CMPlatform;
import com.choicemaker.eclipse2.core.runtime.CMPluginDescriptor;
import com.choicemaker.eclipse2.core.runtime.CMPluginRegistry;

public class PlatformTest {

	private PlatformTest() {
	}
	
	public static void testRegistry(CMPlatform pd) {
		assertTrue(pd != null);
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
