package com.choicemaker.fake.ejb;

import static com.choicemaker.fake.ejb.Utils.EXPECTED_1;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class EarTest {

	@Inject
	PluginDiscoveryEJB pluginDiscoveryEJB;

	public static JavaArchive createEjbJar() {
		JavaArchive retVal =
			ShrinkWrap.create(JavaArchive.class)
					.addClass(PluginDiscoverySingletonBean.class)
					.addClass(PluginDiscoveryEJB.class)
					.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

		// Add test class to JAR
		retVal.addClass(EarTest.class);
		retVal.addClass(Utils.class);

		// Print the JAR contents
		System.out.println();
		System.out.println("EJB JAR:");
		System.out.println(retVal.toString(true));
		System.out.println();
		return retVal;
	}

	public static JavaArchive createPluginsLib() {
		JavaArchive retVal =
			ShrinkWrap
					.create(JavaArchive.class);
		URL resourceUrl = EarTest.class.getResource("/META-INF/plugins");
		try {
			Path resourcePath;
			resourcePath = Paths.get(resourceUrl.toURI());
			File d = resourcePath.toFile();
			Assert.assertTrue(d.exists());
			Assert.assertTrue(d.isDirectory());
			retVal.addAsManifestResource(d);
		} catch (URISyntaxException e) {
			System.err.println("WARNING: " + e.toString());
		}
		System.out.println();
		System.out.println("Plugins LIB:");
		System.out.println(retVal.toString(true));
		System.out.println();
		return retVal;
	}

	public static File[] createTestDependencies() {
		PomEquippedResolveStage pom =
			Maven.resolver().loadPomFromFile("pom.xml");
//		File[] retVal = pom.resolve("log4j:log4j:1.2.16").withTransitivity().asFile();
		File[] retVal =
			pom.importDependencies(ScopeType.COMPILE).resolve()
					.withTransitivity().asFile();
		System.out.println();
		System.out.println("Test dependencies:");
		for (File f : retVal) {
			System.out.println(f.getAbsolutePath());
		}
		System.out.println();
		return retVal;
	}

	@Deployment
	public static EnterpriseArchive createEarArchive() {
		JavaArchive lib1 = createPluginsLib();
		JavaArchive ejb1 = createEjbJar();
		// create the EAR
		EnterpriseArchive retVal =
			ShrinkWrap.create(EnterpriseArchive.class);
		retVal.addAsLibrary(lib1);
		retVal.addAsModule(ejb1);
		try {
		File[] deps = createTestDependencies();
		retVal.addAsLibraries(deps);
		} catch( Exception x) {
			String msg = "WARNING: failed to add test dependencies: " + x.toString();
			System.out.println(msg);
		}
		System.out.println();
		System.out.println("Deployment EAR:");
		System.out.println(retVal.toString(true));
		System.out.println();
		return retVal;
	}

	@Test
	public void testListPluginIds() {
		// Get the computed list of plugin URLs
		Set<URL> pluginIds = pluginDiscoveryEJB.listPluginIds();

		// Check the basics: not null, correct size
		Assert.assertTrue(pluginIds != null);
		Assert.assertTrue(pluginIds.size() == Utils.EXPECTED_1.size());

		// Check that elements of the expected and computed lists are the same
		Utils.compare(pluginIds, EXPECTED_1);
	}

}
