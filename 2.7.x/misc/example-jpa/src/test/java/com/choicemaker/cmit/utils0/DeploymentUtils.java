package com.choicemaker.cmit.utils0;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenFormatStage;
import org.jboss.shrinkwrap.resolver.api.maven.MavenStrategyStage;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;

import com.choicemaker.cm.persist0.BatchJob;
import com.choicemaker.cm.persist0.BatchJobBean;
import com.choicemaker.cm.persist0.BatchJobStatus;
import com.choicemaker.cm.persist0.BatchParameters;
import com.choicemaker.cm.persist0.BatchParametersBean;
import com.choicemaker.cm.persist0.CMP_AuditEvent;
import com.choicemaker.cm.persist0.CMP_Feature;
import com.choicemaker.cm.persist0.CMP_ModelBean;
import com.choicemaker.cm.persist0.CMP_ModelConfigurationBean;
import com.choicemaker.cm.persist0.CMP_ModelConfigurationPK;
import com.choicemaker.cm.persist0.CMP_WellKnownEventType;
import com.choicemaker.cm.persist0.EmbeddedEclipseBean;
import com.choicemaker.cm.persist0.EmbeddedEclipseBean2;
import com.choicemaker.cm.persist0.OfflineMatchingAuditEvent;
import com.choicemaker.cm.persist0.OfflineMatchingBean;
import com.choicemaker.cm.persist0.StatusLog;
import com.choicemaker.cm.persist0.StatusLogBean;
import com.choicemaker.cm.persist0.TransitivityJob;
import com.choicemaker.cm.persist0.TransitivityJobAuditEvent;
import com.choicemaker.cm.persist0.TransitivityJobBean;

public class DeploymentUtils {
	
	private static final Logger logger = Logger.getLogger(DeploymentUtils.class.getName());

	public static final String PROJECT_POM = "pom.xml";

	public static final String PERSISTENCE_CONFIGURATION =
		"src/test/resources/jboss/sqlserver/persistence.xml";

	public static JavaArchive createEjbJar(String unused,
			String MavenCoordinates, List<Class<?>> testClasses,
			String persistenceConfiguration) {

		JavaArchive retVal = ShrinkWrap.create(JavaArchive.class, "ejb.jar");

		// Flag this JAR to CDI as containing injectable beans
		retVal.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

		// Add persistence configuration
		if (persistenceConfiguration != null) {
			File f = new File(persistenceConfiguration);
			assertTrue(f.exists());
			FileAsset fileAsset = new FileAsset(f);
			retVal.addAsManifestResource(fileAsset, "persistence.xml");
		}

		// Add test classes to JAR
		for (Class<?> testClass : testClasses) {
			retVal.addClass(testClass);
		}

		retVal.addClass(BatchJob.class);
		retVal.addClass(BatchJobBean.class);
		retVal.addClass(BatchJobStatus.class);
		retVal.addClass(BatchParameters.class);
		retVal.addClass(BatchParametersBean.class);
		retVal.addClass(CMP_AuditEvent.class);
		retVal.addClass(CMP_Feature.class);
		retVal.addClass(CMP_ModelBean.class);
		retVal.addClass(CMP_ModelConfigurationBean.class);
		retVal.addClass(CMP_ModelConfigurationPK.class);
		retVal.addClass(CMP_WellKnownEventType.class);
		retVal.addClass(EmbeddedEclipseBean.class);
		retVal.addClass(EmbeddedEclipseBean2.class);
		retVal.addClass(OfflineMatchingAuditEvent.class);
		retVal.addClass(OfflineMatchingBean.class);
		retVal.addClass(StatusLog.class);
		retVal.addClass(StatusLogBean.class);
		retVal.addClass(TransitivityJob.class);
		retVal.addClass(TransitivityJobAuditEvent.class);
		retVal.addClass(TransitivityJobBean.class);

		// Print the JAR contents
		if (logger.isLoggable(Level.INFO)) {
			 logger.info("");;
			 logger.info("EJB JAR:");
			 logger.info(retVal.toString(true));
			 logger.info("");;
		}
		return retVal;
	}

	public static File[] createTestDependencies(String dependenciesPOM) {
		if (dependenciesPOM == null) {
			throw new IllegalArgumentException("null POM");
		}
		if (!new File(dependenciesPOM).exists()) {
			throw new IllegalArgumentException("POM doesn't exist: '"
					+ dependenciesPOM + "'");
		}

		// Break out steps for easier debugging
		PomEquippedResolveStage pom =
			Maven.resolver().loadPomFromFile(dependenciesPOM);
		pom = pom.importDependencies(ScopeType.COMPILE);
		MavenStrategyStage mss = pom.resolve();
		assertTrue(mss != null);
		MavenFormatStage mfs = mss.withTransitivity();
		assertTrue(mfs != null);

		File[] retVal = mfs.asFile();

		// Print the dependencies
		if (logger.isLoggable(Level.INFO)) {
			logger.info("");
			logger.info("Test dependencies:");
			for (File f : retVal) {
				logger.info(f.getAbsolutePath());
			}
			logger.info("");
		}
		return retVal;
	}

	public static EnterpriseArchive createEarArchive(JavaArchive ejb,
			File[] dependencies) {
		// Create the EAR
		EnterpriseArchive retVal = ShrinkWrap.create(EnterpriseArchive.class);

		// Create and add the EJB
		if (ejb != null) {
			retVal.addAsModule(ejb);
			// retVal.addAsLibrary(ejb1);
		}

		// Add the EJB dependencies
		if (dependencies != null) {
			try {
				retVal.addAsLibraries(dependencies);
			} catch (Exception x) {
				String msg =
					"WARNING: failed to add test dependencies: " + x.toString();
				System.err.println(msg);
			}
		}

		// Print the EAR contents
		if (logger.isLoggable(Level.INFO)) {
			 logger.info("");;
			 logger.info("Deployment EAR:");
			 logger.info(retVal.toString(true));
			 logger.info("");;
		}
		return retVal;
	}

	private DeploymentUtils() {
	}

}
