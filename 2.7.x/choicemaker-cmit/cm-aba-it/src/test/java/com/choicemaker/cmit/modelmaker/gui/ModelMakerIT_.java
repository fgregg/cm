package com.choicemaker.cmit.modelmaker.gui;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

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

public class ModelMakerIT_ {

	private static final Logger logger = Logger.getLogger(ModelMakerIT_.class.getName());
	
	private static final String SIMPLE_CLASS = ModelMakerIT_.class.getSimpleName();

	public static final String MM_PLUGIN_ID = "com.choicemaker.cm.modelmaker";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		EmbeddedPlatform.install();
	}

	@Test
	public void testModelMakerExtensions() {
		final String METHOD = "testModelMakerExtensions";
		logger.entering(SIMPLE_CLASS, METHOD);

		Set<ExtensionDeclaration> expected =
			ModelMakerUtils_.getExpectedExtensions();
		CMExtension[] exts = CMPlatformUtils.getPluginExtensions(MM_PLUGIN_ID);
		assertTrue(exts != null);
		assertTrue(exts.length == expected.size());
		Set<ExtensionDeclaration> computed = new HashSet<>();
		for (CMExtension ext : exts) {
			computed.add(new ExtensionDeclaration(ext));
		}
		assertTrue(computed.containsAll(expected));
		
		logger.exiting(SIMPLE_CLASS, METHOD);
	}

	@Test
	public void testModelMakerExtensionPoints() {
		final String METHOD = "testModelMakerExtensionPoints";
		logger.entering(SIMPLE_CLASS, METHOD);
		Set<String> expected = ModelMakerUtils_.getExpectedExtensionPoints();
		CMExtensionPoint[] pts =
			CMPlatformUtils.getPluginExtensionPoints(MM_PLUGIN_ID);
		assertTrue(pts != null);
		assertTrue(pts.length == expected.size());
		Set<String> computed = new HashSet<>();
		for (CMExtensionPoint pt : pts) {
			computed.add(pt.getUniqueIdentifier());
		}
		assertTrue(computed.containsAll(expected));
		logger.exiting(SIMPLE_CLASS, METHOD);
	}

	@Test
	public void testMain() {
		final String METHOD = "testMain";
		logger.entering(SIMPLE_CLASS, METHOD);
		try {
			String[] args = ModelMakerUtils_.getModelMakerRunArgs();
			ModelMaker.main(args);
		} catch (Exception e) {
			fail(e.toString());
		}
		logger.exiting(SIMPLE_CLASS, METHOD);
	}

	@Test
	public void testRunObject() {
		final String METHOD = "testRunObject";
		logger.entering(SIMPLE_CLASS, METHOD);

		CMPlatform cmp = InstallablePlatform.getInstance();
		final String extensionId = "com.choicemaker.cm.modelmaker.ModelMaker";
		CMPlatformRunnable runnable = cmp.loaderGetRunnable(extensionId);
		try {
			String[] args = ModelMakerUtils_.getModelMakerRunArgs();
			runnable.run(args);
		} catch (Exception e) {
			fail(e.toString());
		}

		logger.exiting(SIMPLE_CLASS, METHOD);
	}

}
