package com.choicemaker.e2it.ejb;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.e2.CMPluginRegistry;
import com.choicemaker.e2.ejb.EjbPlatform;
import com.choicemaker.e2it.ExtensionPointTest;

@RunWith(Arquillian.class)
public class EjbExtensionPointTest {

	private static final String MAVEN_COORDINATE_SEPARATOR = ":";

	static final String PROJECT_POM = "pom.xml";

	static final String DEPENDENCIES_POM = PROJECT_POM;

	static final String EJB_MAVEN_GROUPID = "com.choicemaker.e2";

	static final String EJB_MAVEN_ARTIFACTID = "com.choicemaker.e2.ejb";

	static final String EJB_MAVEN_VERSION = "2.7.1-SNAPSHOT";

	static final String EJB_MAVEN_COORDINATES = new StringBuilder()
			.append(EJB_MAVEN_GROUPID).append(MAVEN_COORDINATE_SEPARATOR)
			.append(EJB_MAVEN_ARTIFACTID).append(MAVEN_COORDINATE_SEPARATOR)
			.append(EJB_MAVEN_VERSION).toString();

	@Deployment
	public static EnterpriseArchive createEarArchive() {
		List<Class<?>> testClasses = new ArrayList<>();
		testClasses.add(EjbExtensionPointTest.class);
		testClasses.add(ExtensionPointTest.class);

		JavaArchive ejb =
			DeploymentUtils.createEjbJar(PROJECT_POM, EJB_MAVEN_COORDINATES,
					testClasses, null);

		File[] deps = DeploymentUtils.createTestDependencies(DEPENDENCIES_POM);

		EnterpriseArchive retVal = DeploymentUtils.createEarArchive(ejb, deps);
		return retVal;
	}

	@EJB
	EjbPlatform e2service;

	@Test
	public void testEclipse2Service() {
		assertTrue(e2service != null);
	}

	public CMPluginRegistry getPluginRegistry() {
		CMPluginRegistry retVal = e2service.getPluginRegistry();
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

	// @Test
	// public void testComChoicemakerCmServerBaseBoundObject() {
	// CMPluginRegistry registry = getPluginRegistry();
	// ExtensionPointTest.testComChoicemakerCmServerBaseBoundObject(registry);
	// }

	// @Test
	// public void testComChoicemakerCmServerBaseInitializer() {
	// CMPluginRegistry registry = getPluginRegistry();
	// ExtensionPointTest.testComChoicemakerCmServerBaseInitializer(registry);
	// }

	// @Test
	// public void testComChoicemakerCmUrmUpdateDerivedFields() {
	// CMPluginRegistry registry = getPluginRegistry();
	// ExtensionPointTest.testComChoicemakerCmUrmUpdateDerivedFields(registry);
	// }

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
