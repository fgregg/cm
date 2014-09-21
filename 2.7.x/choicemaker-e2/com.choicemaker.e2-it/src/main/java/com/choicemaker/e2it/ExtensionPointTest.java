package com.choicemaker.e2it;

import static org.junit.Assert.assertTrue;

import com.choicemaker.e2.CMExtensionPoint;
import com.choicemaker.e2.CMPluginRegistry;

public class ExtensionPointTest {

	public static final String EXT_PT_CM_CORE_ACCESSOR =
		"com.choicemaker.cm.core.accessor";

	public static final String EXT_PT_CM_CORE_COMMANDLINEARGUMENT =
		"com.choicemaker.cm.core.commandLineArgument";

	public static final String EXT_PT_CM_CORE_FILEMRPSREADER =
		"com.choicemaker.cm.core.fileMrpsReader";

	public static final String EXT_PT_CM_CORE_GENERATORPLUGIN =
		"com.choicemaker.cm.core.generatorPlugin";

	public static final String EXT_PT_CM_CORE_MACHINELEARNER =
		"com.choicemaker.cm.core.machineLearner";

	public static final String EXT_PT_CM_CORE_MATCHCANDIDATE =
		"com.choicemaker.cm.core.matchCandidate";

	public static final String EXT_PT_CM_CORE_MRPSREADER =
		"com.choicemaker.cm.core.mrpsReader";

	public static final String EXT_PT_CM_CORE_NAMEDRESOURCE =
		"com.choicemaker.cm.core.namedResource";

	public static final String EXT_PT_CM_CORE_OBJECTGENERATOR =
		"com.choicemaker.cm.core.objectGenerator";

	public static final String EXT_PT_CM_CORE_REPORTER =
		"com.choicemaker.cm.core.reporter";

	public static final String EXT_PT_CM_CORE_RSREADER =
		"com.choicemaker.cm.core.rsReader";

	public static final String EXT_PT_CM_CORE_RSSERIALIZER =
		"com.choicemaker.cm.core.rsSerializer";

	public static final String EXT_PT_CM_IO_BLOCKING_AUTOMATED_BASE_DATABASEABSTRACTION =
		"com.choicemaker.cm.io.blocking.automated.base.databaseAbstraction";

	public static final String EXT_PT_CM_IO_BLOCKING_AUTOMATED_BASE_DATABASEACCESSOR =
		"com.choicemaker.cm.io.blocking.automated.base.databaseAccessor";

	public static final String EXT_PT_CM_MATCHING_CFG_CASCADEDPARSER =
		"com.choicemaker.cm.matching.cfg.cascadedParser";

	public static final String EXT_PT_CM_MATCHING_CFG_PARSER =
		"com.choicemaker.cm.matching.cfg.parser";

	public static final String EXT_PT_CM_MATCHING_GEN_MAP =
		"com.choicemaker.cm.matching.gen.map";

	public static final String EXT_PT_CM_MATCHING_GEN_RELATION =
		"com.choicemaker.cm.matching.gen.relation";

	public static final String EXT_PT_CM_MATCHING_GEN_SET =
		"com.choicemaker.cm.matching.gen.set";

	public static final String EXT_PT_CM_MATCHING_WFST_PARSER =
		"com.choicemaker.cm.matching.wfst.parser";

	public static final String EXT_PT_CM_MODELMAKER_MATCHERBLOCKINGTOOLKIT =
		"com.choicemaker.cm.modelmaker.matcherBlockingToolkit";

	public static final String EXT_PT_CM_MODELMAKER_MLTRAINGUIPLUGIN =
		"com.choicemaker.cm.modelmaker.mlTrainGuiPlugin";

	public static final String EXT_PT_CM_MODELMAKER_MRPSREADERGUI =
		"com.choicemaker.cm.modelmaker.mrpsReaderGui";

	public static final String EXT_PT_CM_MODELMAKER_PLUGGABLECONTROLLER =
		"com.choicemaker.cm.modelmaker.pluggableController";

	public static final String EXT_PT_CM_MODELMAKER_PLUGGABLEMENUITEM =
		"com.choicemaker.cm.modelmaker.pluggableMenuItem";

	public static final String EXT_PT_CM_MODELMAKER_RSREADERGUI =
		"com.choicemaker.cm.modelmaker.rsReaderGui";

	public static final String EXT_PT_CM_MODELMAKER_TOOLMENUITEM =
		"com.choicemaker.cm.modelmaker.toolMenuItem";

	public static final String EXT_PT_CM_SERVER_BASE_BOUNDOBJECT =
		"com.choicemaker.cm.server.base.boundObject";

	public static final String EXT_PT_CM_SERVER_BASE_INITIALIZER =
		"com.choicemaker.cm.server.base.initializer";

	public static final String EXT_PT_CM_URM_UPDATEDERIVEDFIELDS =
		"com.choicemaker.cm.urm.updateDerivedFields";

	public static final String EXT_PT_CM_VALIDATION_ECLIPSE_AGGREGATEVALIDATOR =
		"com.choicemaker.cm.validation.eclipse.aggregateValidator";

	public static final String EXT_PT_CM_VALIDATION_ECLIPSE_SETBASEDVALIDATOR =
		"com.choicemaker.cm.validation.eclipse.setBasedValidator";

	public static final String EXT_PT_CM_VALIDATION_ECLIPSE_SIMPLEVALIDATOR =
		"com.choicemaker.cm.validation.eclipse.simpleValidator";

	public static final String EXT_PT_CM_VALIDATION_ECLIPSE_VALIDATORFACTORY =
		"com.choicemaker.cm.validation.eclipse.validatorFactory";

	public static final String EXT_PT_WCOHEN_SS_STRINGDISTANCE =
		"com.wcohen.ss.stringdistance";

	private ExtensionPointTest(CMPluginRegistry registry) {
	}

	public static void testComChoicemakerCmCoreAccessor(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_CORE_ACCESSOR);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmCoreCommandLineArgument(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_CORE_COMMANDLINEARGUMENT);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmCoreFileMrpsReader(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_CORE_FILEMRPSREADER);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmCoreGeneratorPlugin(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_CORE_GENERATORPLUGIN);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmCoreMachineLearner(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_CORE_MACHINELEARNER);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmCoreMatchCandidate(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_CORE_MATCHCANDIDATE);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmCoreMrpsReader(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_CORE_MRPSREADER);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmCoreNamedResource(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_CORE_NAMEDRESOURCE);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmCoreObjectGenerator(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_CORE_OBJECTGENERATOR);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmCoreReporter(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_CORE_REPORTER);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmCoreRsReader(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_CORE_RSREADER);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmCoreRsSerializer(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_CORE_RSSERIALIZER);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmIoBlockingAutomatedBaseDatabaseAbstraction(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_IO_BLOCKING_AUTOMATED_BASE_DATABASEABSTRACTION);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmIoBlockingAutomatedBaseDatabaseAccessor(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_IO_BLOCKING_AUTOMATED_BASE_DATABASEACCESSOR);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmMatchingCfgCascadedParser(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_MATCHING_CFG_CASCADEDPARSER);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmMatchingCfgParser(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_MATCHING_CFG_PARSER);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmMatchingGenMap(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_MATCHING_GEN_MAP);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmMatchingGenRelation(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_MATCHING_GEN_RELATION);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmMatchingGenSet(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_MATCHING_GEN_SET);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmMatchingWfstParser(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_MATCHING_WFST_PARSER);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmModelmakerMatcherBlockingToolkit(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_MODELMAKER_MATCHERBLOCKINGTOOLKIT);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmModelmakerMlTrainGuiPlugin(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_MODELMAKER_MLTRAINGUIPLUGIN);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmModelmakerMrpsReaderGui(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_MODELMAKER_MRPSREADERGUI);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmModelmakerPluggableController(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_MODELMAKER_PLUGGABLECONTROLLER);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmModelmakerPluggableMenuItem(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_MODELMAKER_PLUGGABLEMENUITEM);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmModelmakerRsReaderGui(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_MODELMAKER_RSREADERGUI);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmModelmakerToolMenuItem(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_MODELMAKER_TOOLMENUITEM);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmServerBaseBoundObject(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_SERVER_BASE_BOUNDOBJECT);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmServerBaseInitializer(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_SERVER_BASE_INITIALIZER);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmUrmUpdateDerivedFields(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_URM_UPDATEDERIVEDFIELDS);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmValidationEclipseAggregateValidator(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_VALIDATION_ECLIPSE_AGGREGATEVALIDATOR);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmValidationEclipseSetBasedValidator(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_VALIDATION_ECLIPSE_SETBASEDVALIDATOR);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmValidationEclipseSimpleValidator(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_VALIDATION_ECLIPSE_SIMPLEVALIDATOR);
		assertTrue(ep != null);
	}

	public static void testComChoicemakerCmValidationEclipseValidatorFactory(
			CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_CM_VALIDATION_ECLIPSE_VALIDATORFACTORY);
		assertTrue(ep != null);
	}

	public static void testComWcohenSsStringdistance(CMPluginRegistry registry) {
		assertTrue(registry != null);
		CMExtensionPoint ep =
			registry.getExtensionPoint(EXT_PT_WCOHEN_SS_STRINGDISTANCE);
		assertTrue(ep != null);
	}

}
