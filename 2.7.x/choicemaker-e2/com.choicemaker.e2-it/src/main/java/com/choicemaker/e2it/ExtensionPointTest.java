package com.choicemaker.e2it;

import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_CORE_ACCESSOR;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_CORE_COMMANDLINEARGUMENT;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_CORE_FILEMRPSREADER;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_CORE_GENERATORPLUGIN;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_CORE_MACHINELEARNER;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_CORE_MATCHCANDIDATE;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_CORE_MRPSREADER;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_CORE_NAMEDRESOURCE;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_CORE_OBJECTGENERATOR;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_CORE_REPORTER;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_CORE_RSREADER;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_CORE_RSSERIALIZER;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_IO_BLOCKING_AUTOMATED_BASE_DATABASEABSTRACTION;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_IO_BLOCKING_AUTOMATED_BASE_DATABASEACCESSOR;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_MATCHING_CFG_CASCADEDPARSER;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_MATCHING_CFG_PARSER;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_MATCHING_GEN_MAP;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_MATCHING_GEN_RELATION;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_MATCHING_GEN_SET;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_MATCHING_WFST_PARSER;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_MODELMAKER_MATCHERBLOCKINGTOOLKIT;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_MODELMAKER_MLTRAINGUIPLUGIN;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_MODELMAKER_MRPSREADERGUI;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_MODELMAKER_PLUGGABLECONTROLLER;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_MODELMAKER_PLUGGABLEMENUITEM;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_MODELMAKER_RSREADERGUI;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_MODELMAKER_TOOLMENUITEM;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_SERVER_BASE_BOUNDOBJECT;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_SERVER_BASE_INITIALIZER;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_URM_UPDATEDERIVEDFIELDS;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_VALIDATION_ECLIPSE_AGGREGATEVALIDATOR;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_VALIDATION_ECLIPSE_SETBASEDVALIDATOR;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_VALIDATION_ECLIPSE_SIMPLEVALIDATOR;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_VALIDATION_ECLIPSE_VALIDATORFACTORY;
import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.SECONDSTRING_STRINGDISTANCE;
import static org.junit.Assert.assertTrue;

import com.choicemaker.e2.CMExtensionPoint;
import com.choicemaker.e2.CMPluginRegistry;

public class ExtensionPointTest {

	private ExtensionPointTest(CMPluginRegistry registry) {
	}

	public static void testComChoicemakerCmCoreAccessor(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_CORE_ACCESSOR);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmCoreCommandLineArgument(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_CORE_COMMANDLINEARGUMENT);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmCoreFileMrpsReader(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_CORE_FILEMRPSREADER);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmCoreGeneratorPlugin(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_CORE_GENERATORPLUGIN);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmCoreMachineLearner(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_CORE_MACHINELEARNER);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmCoreMatchCandidate(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_CORE_MATCHCANDIDATE);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmCoreMrpsReader(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_CORE_MRPSREADER);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmCoreNamedResource(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_CORE_NAMEDRESOURCE);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmCoreObjectGenerator(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_CORE_OBJECTGENERATOR);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmCoreReporter(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_CORE_REPORTER);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmCoreRsReader(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_CORE_RSREADER);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmCoreRsSerializer(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_CORE_RSSERIALIZER);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmIoBlockingAutomatedBaseDatabaseAbstraction(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_IO_BLOCKING_AUTOMATED_BASE_DATABASEABSTRACTION);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmIoBlockingAutomatedBaseDatabaseAccessor(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_IO_BLOCKING_AUTOMATED_BASE_DATABASEACCESSOR);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmMatchingCfgCascadedParser(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_MATCHING_CFG_CASCADEDPARSER);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmMatchingCfgParser(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_MATCHING_CFG_PARSER);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmMatchingGenMap(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_MATCHING_GEN_MAP);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmMatchingGenRelation(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_MATCHING_GEN_RELATION);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmMatchingGenSet(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_MATCHING_GEN_SET);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmMatchingWfstParser(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_MATCHING_WFST_PARSER);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmModelmakerMatcherBlockingToolkit(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_MODELMAKER_MATCHERBLOCKINGTOOLKIT);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmModelmakerMlTrainGuiPlugin(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_MODELMAKER_MLTRAINGUIPLUGIN);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmModelmakerMrpsReaderGui(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_MODELMAKER_MRPSREADERGUI);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmModelmakerPluggableController(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_MODELMAKER_PLUGGABLECONTROLLER);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmModelmakerPluggableMenuItem(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_MODELMAKER_PLUGGABLEMENUITEM);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmModelmakerRsReaderGui(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_MODELMAKER_RSREADERGUI);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmModelmakerToolMenuItem(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_MODELMAKER_TOOLMENUITEM);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmServerBaseBoundObject(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_SERVER_BASE_BOUNDOBJECT);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmServerBaseInitializer(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_SERVER_BASE_INITIALIZER);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmUrmUpdateDerivedFields(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_URM_UPDATEDERIVEDFIELDS);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmValidationEclipseAggregateValidator(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_VALIDATION_ECLIPSE_AGGREGATEVALIDATOR);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmValidationEclipseSetBasedValidator(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_VALIDATION_ECLIPSE_SETBASEDVALIDATOR);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmValidationEclipseSimpleValidator(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_VALIDATION_ECLIPSE_SIMPLEVALIDATOR);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmValidationEclipseValidatorFactory(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(CM_VALIDATION_ECLIPSE_VALIDATORFACTORY);
		assertTrue(ep != null);
	}

	public static void testComWcohenSsStringdistance(CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(SECONDSTRING_STRINGDISTANCE);
		assertTrue(ep != null);
	}

}
