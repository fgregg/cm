package com.choicemaker.demo.simple_person_matching;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import com.choicemaker.e2.CMExtension;
import com.choicemaker.e2.embed.EmbeddedPlatform;
import com.choicemaker.e2.platform.CMPlatformUtils;
import com.choicemaker.e2.utils.ExtensionDeclaration;

public class SimplePersonPluginTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		EmbeddedPlatform.install();
	}

	@Test
	public void testSimplePersonPluginExtensions() {
		Set<ExtensionDeclaration> expected = getExpectedExtensions();
		CMExtension[] exts = CMPlatformUtils.getPluginExtensions(SP_PLUGIN_ID);
		assertTrue(exts != null);
		assertTrue(exts.length == expected.size());
		Set<ExtensionDeclaration> computed = new HashSet<>();
		for (CMExtension ext : exts) {
			computed.add(new ExtensionDeclaration(ext));
		}
		assertTrue(computed.containsAll(expected));
	}

	public static final String SP_PLUGIN_ID =
		"com.choicemaker.cm.simplePersonMatching";

	/**
	 * Returns a unique identifier given the id specified in the ModelMaker
	 * plugin descriptor
	 */
	static String uid(String id) {
		return SP_PLUGIN_ID + "." + id;
	}

	static Set<ExtensionDeclaration> getExpectedExtensions() {
		Set<ExtensionDeclaration> retVal = new HashSet<>();
		retVal.add(new ExtensionDeclaration(uid("Model1"),
				"com.choicemaker.cm.core.modelConfiguration"));
		retVal.add(new ExtensionDeclaration(uid("Model2"),
				"com.choicemaker.cm.core.modelConfiguration"));
		return retVal;
	}

}
