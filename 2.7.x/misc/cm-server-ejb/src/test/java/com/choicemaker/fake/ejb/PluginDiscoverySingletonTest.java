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
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class PluginDiscoverySingletonTest {

	@Inject
	PluginDiscoveryEJB pluginDiscoveryEJB;

	@Deployment
	public static JavaArchive createDeployment() {
		JavaArchive retVal =
			ShrinkWrap
					.create(JavaArchive.class)
					.addClass(PluginDiscoverySingletonBean.class)
					.addClass(PluginDiscoveryEJB.class)
					.addClass(Utils.class)
					.addClass(
							com.choicemaker.fake.EmbeddedPluginDiscovery.class)
					.addClass(com.choicemaker.fake.IModel.class)
					.addClass(
							com.choicemaker.fake.InstallablePluginDiscovery.class)
					.addClass(com.choicemaker.fake.PluginDiscovery.class)
					.addClass(
							com.choicemaker.fake.PluginDiscoveryException.class)
					.addClass(
							com.choicemaker.fake.PluginIdVersionType.class)
					.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
		URL resourceUrl =
			PluginDiscoverySingletonTest.class.getResource("/META-INF/plugins");
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
		System.out.println("PluginDiscoverySingletonTest EJB:");
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
