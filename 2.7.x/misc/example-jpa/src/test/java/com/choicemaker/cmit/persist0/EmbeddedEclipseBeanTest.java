package com.choicemaker.cmit.persist0;

import static com.choicemaker.cmit.persist0.BatchDeploymentUtils.EJB_MAVEN_COORDINATES;
import static com.choicemaker.cmit.utils0.DeploymentUtils.PERSISTENCE_CONFIGURATION;
import static com.choicemaker.cmit.utils0.DeploymentUtils.PROJECT_POM;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.cm.persist0.EmbeddedEclipseBean;
import com.choicemaker.cmit.utils0.DeploymentUtils;
import com.choicemaker.e2.CMPlatform;
import com.choicemaker.e2.CMPluginRegistry;
import com.choicemaker.e2.platform.InstallablePlatform;

@RunWith(Arquillian.class)
public class EmbeddedEclipseBeanTest {

	private static final Logger logger = Logger
			.getLogger(EmbeddedEclipseBeanTest.class.getName());

	@Deployment
	public static EnterpriseArchive createEarArchive() {
		List<Class<?>> testClasses = new ArrayList<>();
		testClasses.add(EmbeddedEclipseBeanTest.class);
		// This seems odd, but the next line is needed OfflineMatchingController
		// class is needed to avoid a ClassNotFoundException by the JBoss
		// ModuleClassLoader when it looks for the EmbeddedEclipseBean2Test
		// class. (The OfflineMatchingController not used in any way by this
		// test.)
		testClasses.add(OfflineMatchingController.class);

		JavaArchive ejb =
			DeploymentUtils.createEjbJar(PROJECT_POM, EJB_MAVEN_COORDINATES,
					testClasses, PERSISTENCE_CONFIGURATION);

		File[] deps = DeploymentUtils.createTestDependencies(PROJECT_POM);

		EnterpriseArchive retVal = DeploymentUtils.createEarArchive(ejb, deps);
		return retVal;
	}

	/**
	 * Check if embedded plugins are loaded -- some should be present
	 */
	@Test
	public void testGetPluginUrls() {
		EmbeddedEclipseBean eeb = new EmbeddedEclipseBean();
		Set<URL> plugins = eeb.getPluginUrls();
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Embedded plugins (START)");
			for (URL url : plugins) {
				logger.fine("Embedded plugin: " + url.toString());
			}
			logger.fine("Embedded plugins (END)");
		}
		assertTrue(plugins != null);
		assertTrue(!plugins.isEmpty());
	}
	
	/**
	 * Get the InstallablePlatform and the plugin registry. The default platform
	 * (delegate) will be the DoNothingPlatform and the plugin regsitry will be
	 * null.
	 */
	@Test
	public void testGetInstallablePlatform() {
		CMPlatform platform = InstallablePlatform.getInstance();
		assertTrue(platform != null);
		logger.fine("platform: " + platform.getClass().getName());
		CMPluginRegistry registry = platform.getPluginRegistry();
		logger.fine("registry: " + (registry == null ? null : registry.getClass().getName()));
	}

	/**
	 * Look up generator plugins. There several of these, since
	 * the POM for this project includes:<ul>
	 * <li>com.choicemaker.cm.io.flatfile.base</li>
	 * <li>com.choicemaker.cm.io.db.base</li>
	 * <li>com.choicemaker.cm.io.blocking.automated.base</li>
	 * <li>com.choicemaker.cm.io.xml.base</li>
	 * </ul>
	 */
	@Test
	public void testGetGeneratorPlugins() {
	}

}
