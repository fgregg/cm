package com.choicemaker.cmit.oaba;

import static org.junit.Assert.assertTrue;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.cmit.oaba.util.OabaDeploymentUtils;

/**
 * Trivial tests -- but useful for checking that Arquillian is configured
 * properly for the other, more comprehensive tests in this module.
 *
 * @author rphall
 */
@RunWith(Arquillian.class)
public class OabaIT {

	public static final boolean TESTS_AS_EJB_MODULE = false;

	@Deployment
	public static Archive<?> createEAR() {
		Class<?>[] removedClasses = null;
		return OabaDeploymentUtils.createEarArchive(removedClasses,
				TESTS_AS_EJB_MODULE);
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
