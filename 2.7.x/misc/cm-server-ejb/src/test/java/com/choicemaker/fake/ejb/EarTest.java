package com.choicemaker.fake.ejb;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.fake.EmbeddedPluginDiscovery;

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

	public static JavaArchive createPluginDiscoveryLib() {
		JavaArchive retVal =
			ShrinkWrap
					.create(JavaArchive.class)
					.addClass(
							com.choicemaker.fake.EmbeddedPluginDiscovery.class)
					.addClass(com.choicemaker.fake.IModel.class)
					.addClass(
							com.choicemaker.fake.InstallablePluginDiscovery.class)
					.addClass(com.choicemaker.fake.PluginDiscovery.class)
					.addClass(
							com.choicemaker.fake.PluginDiscoveryException.class);
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
		System.out.println("PluginDiscovery LIB:");
		System.out.println(retVal.toString(true));
		System.out.println();
		return retVal;
	}

	public static File[] createTestDependencies() {
		PomEquippedResolveStage pom =
			Maven.resolver().loadPomFromFile("pom.xml");
		File[] retVal = pom.resolve("log4j:log4j:1.2.16").withTransitivity().asFile();
		// File[] retVal =
		// pom.importDependencies(ScopeType.COMPILE).resolve()
		// .withTransitivity().asFile();
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
		JavaArchive lib1 = createPluginDiscoveryLib();
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

		// Get the computed list of plugin ids
		List<String> pluginIds = pluginDiscoveryEJB.listPluginIds();

		// Check the basics: not null, correct size
		Assert.assertTrue(pluginIds != null);
		Assert.assertTrue(pluginIds.size() == Utils.getExpected().size());

		// Check that there is no redundancy in the plugin list
		Set<String> normalized = new HashSet<>();
		for (String s : pluginIds) {
			int index = s.indexOf(EmbeddedPluginDiscovery.PREFIX);
			String s2 = s.substring(index);
			normalized.add(s2);
		}
		Assert.assertTrue(normalized.size() == Utils.getExpected().size());

		// Check that elements of the expected and computed lists are the same
		for (String s : Utils.getExpected()) {
			Assert.assertTrue(s + " in computed", normalized.contains(s));
		}
		// Given that the sizes are the same, this last test is redundant.
		// Unless there's a problem with the logic above, failure should never
		// occur after this point.
		for (String s : normalized) {
			Assert.assertTrue(s + " in expected",
					Utils.getExpected().contains(s));
		}
	}

}
