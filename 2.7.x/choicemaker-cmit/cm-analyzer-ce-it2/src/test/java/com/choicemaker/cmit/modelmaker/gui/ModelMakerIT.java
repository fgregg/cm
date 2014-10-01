package com.choicemaker.cmit.modelmaker.gui;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
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

public class ModelMakerIT {

	public static final String MM_PLUGIN_ID = "com.choicemaker.cm.modelmaker";

	/**
	 * Returns a unique identifier given the id specified in the
	 * ModelMaker plugin descriptor
	 */
	protected static String uid(String id) {
		return MM_PLUGIN_ID + "." + id;
	}

	public static class Extension {
		public final String extensionId;
		public final String extensionPoint;

		public Extension(String id, String pt) {
			this.extensionId = id;
			this.extensionPoint = pt;
		}

		public Extension(CMExtension cme) {
			this.extensionId = cme.getUniqueIdentifier();
			this.extensionPoint = cme.getExtensionPointUniqueIdentifier();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result =
				prime * result
						+ ((extensionId == null) ? 0 : extensionId.hashCode());
			result =
				prime
						* result
						+ ((extensionPoint == null) ? 0 : extensionPoint
								.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Extension other = (Extension) obj;
			if (extensionId == null) {
				if (other.extensionId != null) {
					return false;
				}
			} else if (!extensionId.equals(other.extensionId)) {
				return false;
			}
			if (extensionPoint == null) {
				if (other.extensionPoint != null) {
					return false;
				}
			} else if (!extensionPoint.equals(other.extensionPoint)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return "Extension [extensionId=" + extensionId
					+ ", extensionPoint=" + extensionPoint + "]";
		}
	}

	public static Set<Extension> getExpectedExtensions() {
		Set<Extension> retVal = new HashSet<>();
		retVal.add(new Extension(uid("ModelMaker"),
				"com.choicemaker.e2.applications"));
		retVal.add(new Extension(uid("ModelMakerStd"),
				"org.eclipse.runtime.applications"));
		retVal.add(new Extension(uid("AllBlocker"),
				"com.choicemaker.cm.modelmaker.matcherBlockingToolkit"));
		retVal.add(new Extension(uid("NoMachineLearningGui"),
				"com.choicemaker.cm.modelmaker.mlTrainGuiPlugin"));
		return retVal;
	}
	
	public static Set<String> getExpectedExtensionPoints() {
		Set<String> retVal = new HashSet<>();
		retVal.add(uid("mrpsReaderGui"));
		retVal.add(uid("mlTrainGuiPlugin"));
		retVal.add(uid("rsReaderGui"));
		retVal.add(uid("matcherBlockingToolkit"));
		retVal.add(uid("toolMenuItem"));
		retVal.add(uid("pluggableMenuItem"));
		retVal.add(uid("pluggableController"));
		return retVal;
	}

	public static void installEmbeddedPlatform() {
		String pn = InstallablePlatform.INSTALLABLE_PLATFORM;
		String pv = EmbeddedPlatform.class.getName();
		System.setProperty(pn, pv);
	}

	public void assertEmbeddedPlatform() {
		String pv =
			System.getProperty(InstallablePlatform.INSTALLABLE_PLATFORM);
		assertTrue(EmbeddedPlatform.class.getName().equals(pv));
	}

	/** @see http://links.rph.cx/1r1vyFo */
	public static void configureEclipseConsoleLogging() {
		Logger topLogger = java.util.logging.Logger.getLogger("");
		Handler consoleHandler = null;
		for (Handler handler : topLogger.getHandlers()) {
			if (handler instanceof ConsoleHandler) {
				// found the console handler
				consoleHandler = handler;
				break;
			}
		}
		if (consoleHandler == null) {
			consoleHandler = new ConsoleHandler();
			topLogger.addHandler(consoleHandler);
		}
		consoleHandler.setLevel(java.util.logging.Level.FINEST);
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		installEmbeddedPlatform();
		configureEclipseConsoleLogging();
	}

	@Test
	public void testModelMakerExtensions() {
		assertEmbeddedPlatform();

		Set<Extension> expected = getExpectedExtensions();
		CMExtension[] exts = CMPlatformUtils.getPluginExtensions(MM_PLUGIN_ID);
		assertTrue(exts != null);
		assertTrue(exts.length == expected.size());
		Set<Extension> computed = new HashSet<>();
		for (CMExtension ext : exts) {
			computed.add(new Extension(ext));
		}
		assertTrue(computed.containsAll(expected));
	}

	@Test
	public void testModelMakerExtensionPoints() {
		assertEmbeddedPlatform();

		Set<String> expected = getExpectedExtensionPoints();
		CMExtensionPoint[] pts = CMPlatformUtils.getPluginExtensionPoints(MM_PLUGIN_ID);
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
		assertEmbeddedPlatform();
		String[] args = new String[0];
		try {
			ModelMaker.main(args);
		} catch (IOException e) {
			fail(e.toString());
		}
	}

	@Test
	public void testRunObject() {
		assertEmbeddedPlatform();
		CMPlatform cmp = InstallablePlatform.getInstance();

//		final String extensionPt = "org.eclipse.core.runtime.applications";
		final String extensionId = "com.choicemaker.cm.modelmaker.ModelMaker";
		CMPlatformRunnable runnable = cmp.loaderGetRunnable(extensionId);
		try {
			String[] args = new String[0];
			runnable.run(args);
		} catch (Exception e) {
			fail(e.toString());
		}
	}

}
