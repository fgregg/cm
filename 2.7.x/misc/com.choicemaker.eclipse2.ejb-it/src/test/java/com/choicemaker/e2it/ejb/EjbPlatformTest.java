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

import com.choicemaker.e2.CMPlatform;
import com.choicemaker.e2.ejb.EjbPlatform;
import com.choicemaker.e2.embed.EmbeddedPlatform;
import com.choicemaker.e2.platform.InstallablePlatform;
import com.choicemaker.e2it.PlatformTest;

@RunWith(Arquillian.class)
public class EjbPlatformTest {
	
	private static final String MAVEN_COORDINATE_SEPARATOR = ":";
	
	static final String PROJECT_POM = "pom.xml";
	
	static final String DEPENDENCIES_POM = PROJECT_POM;
	
	static final String EJB_MAVEN_GROUPID =
		"com.choicemaker.e2";

	static final String EJB_MAVEN_ARTIFACTID =
			"com.choicemaker.e2.ejb";

	static final String EJB_MAVEN_VERSION =
			"2.7.1-SNAPSHOT";

	static final String EJB_MAVEN_COORDINATES = new StringBuilder()
			.append(EJB_MAVEN_GROUPID).append(MAVEN_COORDINATE_SEPARATOR)
			.append(EJB_MAVEN_ARTIFACTID).append(MAVEN_COORDINATE_SEPARATOR)
			.append(EJB_MAVEN_VERSION).toString();

	@Deployment
	public static EnterpriseArchive createEarArchive() {
		List<Class<?>> testClasses = new ArrayList<>();
		testClasses.add(EjbPlatformTest.class);
		testClasses.add(PlatformTest.class);

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

	@Test
	public void testInstallablePlatform() {
		InstallablePlatform ip = InstallablePlatform.getInstance();
		assertTrue(ip != null);
		
//		CMPlatform delegate = InstallablePlatform.getInstance().getDelegate();
//		String delegateFQCN = delegate.getClass().getName();
//		assertTrue(delegateFQCN.equals(EmbeddedPlatform.class.getName()));
	}

	@Test
	public void testEmbeddedRegistry() {
		CMPlatform ep = new EmbeddedPlatform();
		PlatformTest.testRegistry(ep);
	}

}
