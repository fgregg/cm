package com.choicemaker.cmit.oaba;

import static com.choicemaker.cmit.oaba.util.OabaUtils.DEPENDENCIES_POM;
import static com.choicemaker.cmit.oaba.util.OabaUtils.EJB_MAVEN_COORDINATES;
import static com.choicemaker.cmit.oaba.util.OabaUtils.PROJECT_POM;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.cmit.utils.DeploymentUtils;

@RunWith(Arquillian.class)
public class OabaIT {

	@Deployment
	public static EnterpriseArchive createEarArchive() {
		List<Class<?>> testClasses = new ArrayList<>();
		testClasses.add(OabaIT.class);

		JavaArchive ejb =
			DeploymentUtils.createEjbJar(PROJECT_POM, EJB_MAVEN_COORDINATES,
					testClasses, null);

		File[] deps = DeploymentUtils.createTestDependencies(DEPENDENCIES_POM);

		EnterpriseArchive retVal = DeploymentUtils.createEarArchive(ejb, deps);
		return retVal;
	}

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	@Test
	public void testEntityManager() {
		assertTrue(em != null);
	}

}
