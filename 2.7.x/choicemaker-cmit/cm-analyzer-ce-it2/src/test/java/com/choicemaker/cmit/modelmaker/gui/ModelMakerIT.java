package com.choicemaker.cmit.modelmaker.gui;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.e2.CMExtension;
import com.choicemaker.e2.CMExtensionPoint;
import com.choicemaker.e2.CMPlatform;
import com.choicemaker.e2.CMPlatformRunnable;
import com.choicemaker.e2.embed.EmbeddedPlatform;
import com.choicemaker.e2.platform.CMPlatformUtils;
import com.choicemaker.e2.platform.InstallablePlatform;
import com.choicemaker.e2.utils.ExtensionDeclaration;

public class ModelMakerIT {

	public static final String MM_PLUGIN_ID = "com.choicemaker.cm.modelmaker";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		EmbeddedPlatform.install();
	}

	@Test
	public void testModelMakerExtensions() {
		Set<ExtensionDeclaration> expected =
			ModelMakerUtils.getExpectedExtensions();
		CMExtension[] exts = CMPlatformUtils.getPluginExtensions(MM_PLUGIN_ID);
		assertTrue(exts != null);
		assertTrue(exts.length == expected.size());
		Set<ExtensionDeclaration> computed = new HashSet<>();
		for (CMExtension ext : exts) {
			computed.add(new ExtensionDeclaration(ext));
		}
		assertTrue(computed.containsAll(expected));
	}

	@Test
	public void testModelMakerExtensionPoints() {
		Set<String> expected = ModelMakerUtils.getExpectedExtensionPoints();
		CMExtensionPoint[] pts =
			CMPlatformUtils.getPluginExtensionPoints(MM_PLUGIN_ID);
		assertTrue(pts != null);
		assertTrue(pts.length == expected.size());
		Set<String> computed = new HashSet<>();
		for (CMExtensionPoint pt : pts) {
			computed.add(pt.getUniqueIdentifier());
		}
		assertTrue(computed.containsAll(expected));
	}

	@Test
	public void testMain() {
		try {
			String[] args = ModelMakerUtils.getModelMakerRunArgs();
			ModelMaker.main(args);
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	@Test
	public void testRunObject() {
		CMPlatform cmp = InstallablePlatform.getInstance();

		final String extensionId = "com.choicemaker.cm.modelmaker.ModelMaker";
		CMPlatformRunnable runnable = cmp.loaderGetRunnable(extensionId);
		try {
			String[] args = ModelMakerUtils.getModelMakerRunArgs();
			runnable.run(args);
		} catch (Exception e) {
			fail(e.toString());
		}
	}

}
