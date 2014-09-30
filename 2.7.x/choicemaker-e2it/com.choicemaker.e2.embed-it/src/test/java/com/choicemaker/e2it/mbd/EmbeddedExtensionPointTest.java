package com.choicemaker.e2it.mbd;

import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Test;

import com.choicemaker.e2.CMPlatform;
import com.choicemaker.e2.CMPluginRegistry;
import com.choicemaker.e2.embed.EmbeddedPlatform;
import com.choicemaker.e2.platform.InstallablePlatform;
import com.choicemaker.e2it.ExtensionPointTest;

public class EmbeddedExtensionPointTest {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger
			.getLogger(EmbeddedExtensionPointTest.class.getName());

	@BeforeClass
	public static void configureEmbeddedPlatform() {
		String pn = InstallablePlatform.INSTALLABLE_PLATFORM;
		String pv = EmbeddedPlatform.class.getName();
		System.setProperty(pn, pv);
	}

	public CMPluginRegistry getPluginRegistry() {
		CMPlatform ep = new EmbeddedPlatform();
		CMPluginRegistry retVal = ep.getPluginRegistry();
		return retVal;
	}

	@Test
	public void testComChoicemakerCmCoreAccessor() {
		CMPluginRegistry registry = getPluginRegistry();
		ExtensionPointTest.testComChoicemakerCmCoreAccessor(registry);
	}

	@Test
	public void testComChoicemakerCmCoreCommandLineArgument() {
		CMPluginRegistry registry = getPluginRegistry();
		ExtensionPointTest
				.testComChoicemakerCmCoreCommandLineArgument(registry);
	}

	@Test
	public void testComChoicemakerCmCoreFileMrpsReader() {
		CMPluginRegistry registry = getPluginRegistry();
		ExtensionPointTest.testComChoicemakerCmCoreFileMrpsReader(registry);
	}

	@Test
	public void testComChoicemakerCmCoreGeneratorPlugin() {
		CMPluginRegistry registry = getPluginRegistry();
		ExtensionPointTest.testComChoicemakerCmCoreGeneratorPlugin(registry);
	}

	@Test
	public void testComChoicemakerCmCoreMachineLearner() {
		CMPluginRegistry registry = getPluginRegistry();
		ExtensionPointTest.testComChoicemakerCmCoreMachineLearner(registry);
	}

	@Test
	public void testComChoicemakerCmCoreMatchCandidate() {
		CMPluginRegistry registry = getPluginRegistry();
		ExtensionPointTest.testComChoicemakerCmCoreMatchCandidate(registry);
	}

	@Test
	public void testComChoicemakerCmCoreMrpsReader() {
		CMPluginRegistry registry = getPluginRegistry();
		ExtensionPointTest.testComChoicemakerCmCoreMrpsReader(registry);
	}

	@Test
	public void testComChoicemakerCmCoreNamedResource() {
		CMPluginRegistry registry = getPluginRegistry();
		ExtensionPointTest.testComChoicemakerCmCoreNamedResource(registry);
	}

	@Test
	public void testComChoicemakerCmCoreObjectGenerator() {
		CMPluginRegistry registry = getPluginRegistry();
		ExtensionPointTest.testComChoicemakerCmCoreObjectGenerator(registry);
	}

	@Test
	public void testComChoicemakerCmCoreReporter() {
		CMPluginRegistry registry = getPluginRegistry();
		ExtensionPointTest.testComChoicemakerCmCoreReporter(registry);
	}

	@Test
	public void testComChoicemakerCmCoreRsReader() {
		CMPluginRegistry registry = getPluginRegistry();
		ExtensionPointTest.testComChoicemakerCmCoreRsReader(registry);
	}

	@Test
	public void testComChoicemakerCmCoreRsSerializer() {
		CMPluginRegistry registry = getPluginRegistry();
		ExtensionPointTest.testComChoicemakerCmCoreRsSerializer(registry);
	}

	@Test
	public void testComChoicemakerCmIoBlockingAutomatedBaseDatabaseAbstraction() {
		CMPluginRegistry registry = getPluginRegistry();
		ExtensionPointTest
				.testComChoicemakerCmIoBlockingAutomatedBaseDatabaseAbstraction(registry);
	}

	@Test
	public void testComChoicemakerCmIoBlockingAutomatedBaseDatabaseAccessor() {
		CMPluginRegistry registry = getPluginRegistry();
		ExtensionPointTest
				.testComChoicemakerCmIoBlockingAutomatedBaseDatabaseAccessor(registry);
	}

	@Test
	public void testComChoicemakerCmMatchingCfgCascadedParser() {
		CMPluginRegistry registry = getPluginRegistry();
		ExtensionPointTest
				.testComChoicemakerCmMatchingCfgCascadedParser(registry);
	}

	@Test
	public void testComChoicemakerCmMatchingCfgParser() {
		CMPluginRegistry registry = getPluginRegistry();
		ExtensionPointTest.testComChoicemakerCmMatchingCfgParser(registry);
	}

	@Test
	public void testComChoicemakerCmMatchingGenMap() {
		CMPluginRegistry registry = getPluginRegistry();
		ExtensionPointTest.testComChoicemakerCmMatchingGenMap(registry);
	}

	@Test
	public void testComChoicemakerCmMatchingGenRelation() {
		CMPluginRegistry registry = getPluginRegistry();
		ExtensionPointTest.testComChoicemakerCmMatchingGenRelation(registry);
	}

	@Test
	public void testComChoicemakerCmMatchingGenSet() {
		CMPluginRegistry registry = getPluginRegistry();
		ExtensionPointTest.testComChoicemakerCmMatchingGenSet(registry);
	}

	@Test
	public void testComChoicemakerCmMatchingWfstParser() {
		CMPluginRegistry registry = getPluginRegistry();
		ExtensionPointTest.testComChoicemakerCmMatchingWfstParser(registry);
	}

	@Test
	public void testComChoicemakerCmModelmakerMatcherBlockingToolkit() {
		CMPluginRegistry registry = getPluginRegistry();
		ExtensionPointTest
				.testComChoicemakerCmModelmakerMatcherBlockingToolkit(registry);
	}

	@Test
	public void testComChoicemakerCmModelmakerMlTrainGuiPlugin() {
		CMPluginRegistry registry = getPluginRegistry();
		ExtensionPointTest
				.testComChoicemakerCmModelmakerMlTrainGuiPlugin(registry);
	}

	@Test
	public void testComChoicemakerCmModelmakerMrpsReaderGui() {
		CMPluginRegistry registry = getPluginRegistry();
		ExtensionPointTest
				.testComChoicemakerCmModelmakerMrpsReaderGui(registry);
	}

	@Test
	public void testComChoicemakerCmModelmakerPluggableController() {
		CMPluginRegistry registry = getPluginRegistry();
		ExtensionPointTest
				.testComChoicemakerCmModelmakerPluggableController(registry);
	}

	@Test
	public void testComChoicemakerCmModelmakerPluggableMenuItem() {
		CMPluginRegistry registry = getPluginRegistry();
		ExtensionPointTest
				.testComChoicemakerCmModelmakerPluggableMenuItem(registry);
	}

	@Test
	public void testComChoicemakerCmModelmakerRsReaderGui() {
		CMPluginRegistry registry = getPluginRegistry();
		ExtensionPointTest.testComChoicemakerCmModelmakerRsReaderGui(registry);
	}

	@Test
	public void testComChoicemakerCmModelmakerToolMenuItem() {
		CMPluginRegistry registry = getPluginRegistry();
		ExtensionPointTest.testComChoicemakerCmModelmakerToolMenuItem(registry);
	}

//	@Test
//	public void testComChoicemakerCmServerBaseBoundObject() {
//		CMPluginRegistry registry = getPluginRegistry();
//		ExtensionPointTest.testComChoicemakerCmServerBaseBoundObject(registry);
//	}

//	@Test
//	public void testComChoicemakerCmServerBaseInitializer() {
//		CMPluginRegistry registry = getPluginRegistry();
//		ExtensionPointTest.testComChoicemakerCmServerBaseInitializer(registry);
//	}

//	@Test
//	public void testComChoicemakerCmUrmUpdateDerivedFields() {
//		CMPluginRegistry registry = getPluginRegistry();
//		ExtensionPointTest.testComChoicemakerCmUrmUpdateDerivedFields(registry);
//	}

	@Test
	public void testComChoicemakerCmValidationEclipseAggregateValidator() {
		CMPluginRegistry registry = getPluginRegistry();
		ExtensionPointTest
				.testComChoicemakerCmValidationEclipseAggregateValidator(registry);
	}

	@Test
	public void testComChoicemakerCmValidationEclipseSetBasedValidator() {
		CMPluginRegistry registry = getPluginRegistry();
		ExtensionPointTest
				.testComChoicemakerCmValidationEclipseSetBasedValidator(registry);
	}

	@Test
	public void testComChoicemakerCmValidationEclipseSimpleValidator() {
		CMPluginRegistry registry = getPluginRegistry();
		ExtensionPointTest
				.testComChoicemakerCmValidationEclipseSimpleValidator(registry);
	}

	@Test
	public void testComChoicemakerCmValidationEclipseValidatorFactory() {
		CMPluginRegistry registry = getPluginRegistry();
		ExtensionPointTest
				.testComChoicemakerCmValidationEclipseValidatorFactory(registry);
	}

	@Test
	public void testComWcohenSsStringdistance() {
		CMPluginRegistry registry = getPluginRegistry();
		ExtensionPointTest.testComWcohenSsStringdistance(registry);
	}

}
