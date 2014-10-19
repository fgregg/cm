package com.choicemaker.cmit.oaba;

import static com.choicemaker.cmit.oaba.DeploymentUtils.DEFAULT_HAS_BEANS;
import static com.choicemaker.cmit.oaba.DeploymentUtils.DEFAULT_MODULE_NAME;
import static com.choicemaker.cmit.oaba.DeploymentUtils.DEFAULT_POM_FILE;
import static com.choicemaker.cmit.oaba.DeploymentUtils.DEFAULT_TEST_CLASSES_PATH;
import static com.choicemaker.cmit.oaba.DeploymentUtils.createJAR;
import static com.choicemaker.cmit.oaba.DeploymentUtils.resolveDependencies;
import static com.choicemaker.cmit.oaba.DeploymentUtils.resolvePom;
import static com.choicemaker.cmit.oaba.util.OabaConstants.CURRENT_MAVEN_COORDINATES;
import static com.choicemaker.cmit.oaba.util.OabaConstants.PERSISTENCE_CONFIGURATION;
import static org.junit.Assert.assertTrue;

import java.io.File;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class OabaIT {

	public static final boolean TESTS_AS_EJB_MODULE = false;

	@Deployment
	public static Archive<?> createEAR() {
		PomEquippedResolveStage pom = resolvePom(DEFAULT_POM_FILE);
		File[] libs = resolveDependencies(pom);
		JavaArchive tests =
			createJAR(pom, CURRENT_MAVEN_COORDINATES, DEFAULT_MODULE_NAME,
					DEFAULT_TEST_CLASSES_PATH,
					PERSISTENCE_CONFIGURATION, DEFAULT_HAS_BEANS);
		Archive<?> retVal =
			DeploymentUtils.createEAR(tests, libs, TESTS_AS_EJB_MODULE);
		return retVal;
	}

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	@Test
	public void testTrivialTrue() {
		System.out.println("testTrivialTrue: TRIVIALLY VALID TEST");
		assertTrue(true);
	}

	@Test
	public void testEntityManager() {
		assertTrue(em != null);
	}

}
