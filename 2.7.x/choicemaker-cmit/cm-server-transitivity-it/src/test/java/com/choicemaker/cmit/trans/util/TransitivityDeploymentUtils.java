package com.choicemaker.cmit.trans.util;

import static com.choicemaker.cmit.trans.util.TransitivityConstants.CURRENT_MAVEN_COORDINATES;
import static com.choicemaker.cmit.trans.util.TransitivityConstants.PERSISTENCE_CONFIGURATION;
import static com.choicemaker.cmit.utils.DeploymentUtils.DEFAULT_HAS_BEANS;
import static com.choicemaker.cmit.utils.DeploymentUtils.DEFAULT_MODULE_NAME;
import static com.choicemaker.cmit.utils.DeploymentUtils.DEFAULT_POM_FILE;
import static com.choicemaker.cmit.utils.DeploymentUtils.DEFAULT_TEST_CLASSES_PATH;
import static com.choicemaker.cmit.utils.DeploymentUtils.createEAR;
import static com.choicemaker.cmit.utils.DeploymentUtils.createJAR;
import static com.choicemaker.cmit.utils.DeploymentUtils.resolveDependencies;
import static com.choicemaker.cmit.utils.DeploymentUtils.resolvePom;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;

public class TransitivityDeploymentUtils {
	
	public static final String REGEX_EJB_DEPENDENCIES =
		"com.choicemaker.cm.io.blocking.automated.offline.server.*.jar"
				+ "|com.choicemaker.e2.ejb.*.jar"
				+ "|com.choicemaker.cm.batch.*.jar"
				+ "|com.choicemaker.cm.transitivity.server.*.jar"
//				+ "|cmit-server-oaba-it.*.jar"
//				+ "|cmit-server-transitivity-it.*.jar"
	;

	public static final String[] removedPaths(Class<?>[] removedClasses) {
		Set<String> removedPaths = new LinkedHashSet<>();
		if (removedClasses != null) {
			for (Class<?> c : removedClasses) {
				if (c == null) {
					throw new IllegalArgumentException(
							"class array contains a null element");
				}
				String path = "/" + c.getName().replace('.', '/') + ".class";
				removedPaths.add(path);
			}
		}
		String[] retVal = removedPaths.toArray(new String[removedPaths.size()]);
		return retVal;
	}

	/**
	 * Creates an EAR deployment in which the OABA server JAR is missing the
	 * specified classes. During testing, this allows other classes to attach to
	 * the queues used by the removed classes.
	 * 
	 * @param removedClasses
	 *            an array of classes that will be removed from OABA and E2
	 *            server jar files. If the array is null or empty, no files will
	 *            be removed. If the array is not null, it must not contain any
	 *            null elements.
	 * @param testsAsEjbModule
	 *            if true, tests will be added as an EJB module to the EAR. If
	 *            false, they will be added as a regular library. Tests should
	 *            be added as an EJB module if and only if they contain at least
	 *            one class annotated as an EJB.
	 */
	public static EnterpriseArchive createEarArchive(Class<?>[] removedClasses,
			boolean testsAsEjbModule) {

		PomEquippedResolveStage pom = resolvePom(DEFAULT_POM_FILE);
		File[] libs = resolveDependencies(pom);

		// Filter the OABA server and E2Plaform JARs from the dependencies
		final Pattern p =
			Pattern.compile(TransitivityDeploymentUtils.REGEX_EJB_DEPENDENCIES);
		Set<File> ejbJARs = new LinkedHashSet<>();
		List<File> filteredLibs = new LinkedList<>();
		for (File lib : libs) {
			String name = lib.getName();
			Matcher m = p.matcher(name);
			if (m.matches()) {
				boolean isAdded = ejbJARs.add(lib);
				if (!isAdded) {
					String path = lib.getAbsolutePath();
					throw new RuntimeException("failed to add (duplicate?): "
							+ path);
				}
			} else {
				filteredLibs.add(lib);
			}
		}
		File[] libs2 = filteredLibs.toArray(new File[filteredLibs.size()]);

		JavaArchive tests =
			createJAR(pom, CURRENT_MAVEN_COORDINATES, DEFAULT_MODULE_NAME,
					DEFAULT_TEST_CLASSES_PATH, PERSISTENCE_CONFIGURATION,
					DEFAULT_HAS_BEANS);
		EnterpriseArchive retVal = createEAR(tests, libs2, testsAsEjbModule);

		// Filter the targeted paths from the EJB JARs
		for (File ejb : ejbJARs) {
			JavaArchive filteredEJB =
				ShrinkWrap.createFromZipFile(JavaArchive.class, ejb);
			if (removedClasses != null && removedClasses.length > 0) {
				for (String path : removedPaths(removedClasses)) {
					filteredEJB.delete(path);
				}
			}
			retVal.addAsModule(filteredEJB);
		}

		return retVal;
	}

	private TransitivityDeploymentUtils() {
	}

}
