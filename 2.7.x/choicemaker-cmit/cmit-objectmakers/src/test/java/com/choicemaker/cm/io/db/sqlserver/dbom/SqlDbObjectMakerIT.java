package com.choicemaker.cm.io.db.sqlserver.dbom;

import static com.choicemaker.cm.core.PropertyNames.INSTALLABLE_CHOICEMAKER_CONFIGURATOR;
import static com.choicemaker.cm.io.db.sqlserver.dbom.SqlServerUtils.SQLSERVER_PLUGIN_ID;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Test;

import com.choicemaker.cm.core.ModelConfigurationException;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.core.xmlconf.XmlConfigurator;
import com.choicemaker.e2.CMExtension;
import com.choicemaker.e2.CMPlatform;
import com.choicemaker.e2.embed.EmbeddedPlatform;
import com.choicemaker.e2.platform.CMPlatformUtils;
import com.choicemaker.e2.platform.InstallablePlatform;
import com.choicemaker.e2.utils.ExtensionDeclaration;

public class SqlDbObjectMakerIT {

	private static final Logger logger = Logger.getLogger(SqlDbObjectMakerIT.class.getName());
	
	private static final String SIMPLE_CLASS = SqlDbObjectMakerIT.class.getSimpleName();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		final String METHOD = "SqlDbObjectMakerIT.setUpBeforeClass: ";
		EmbeddedPlatform.install();
		String pn = INSTALLABLE_CHOICEMAKER_CONFIGURATOR;
		String pv = XmlConfigurator.class.getName();
		System.setProperty(pn, pv);
		try {
			int count = PMManager.loadModelPlugins();
			if (count == 0) {
				String msg = METHOD + "No probability models loaded";
				logger.warning(msg);
			}
		} catch (ModelConfigurationException | IOException e) {
			String msg = METHOD + "Unable to load model plugins: " + e.toString();
			logger.severe(msg);
			throw new IllegalStateException(msg);
		}
	}

	@Test
	public void testSqlServerExtensions() {
		final String METHOD = "testSqlServerExtensions";
		logger.entering(SIMPLE_CLASS, METHOD);

		Set<ExtensionDeclaration> expected =
			SqlServerUtils.getExpectedExtensions();
		CMExtension[] exts = CMPlatformUtils.getPluginExtensions(SQLSERVER_PLUGIN_ID);
		assertTrue(exts != null);
		assertTrue(exts.length == expected.size());
		Set<ExtensionDeclaration> computed = new HashSet<>();
		for (CMExtension ext : exts) {
			computed.add(new ExtensionDeclaration(ext));
		}
		assertTrue(computed.containsAll(expected));
		assertTrue(computed.size() == expected.size());
		
		logger.exiting(SIMPLE_CLASS, METHOD);
	}

	@Test
	public void testGenerateObjects() {
		final String METHOD = "testGenerateObjects";
		logger.entering(SIMPLE_CLASS, METHOD);

		CMPlatform cmp = InstallablePlatform.getInstance();
		final String extensionId = SqlServerUtils.uid("sqlDbObjectMaker");
//		Object runnable = cmp.(extensionId);
//		try {
//			String[] args = ModelMakerUtils.getModelMakerRunArgs();
//			runnable.run(args);
//		} catch (Exception e) {
//			fail(e.toString());
//		}

		logger.exiting(SIMPLE_CLASS, METHOD);
	}

//	@Test
//	public void testGetAllModels() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testProcessAllModels() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testCreateObjects() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetMultiKeyImmutableProbabilityModelString() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetMultiKeyDbReader() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetMultiQuery() {
//		fail("Not yet implemented");
//	}

}
