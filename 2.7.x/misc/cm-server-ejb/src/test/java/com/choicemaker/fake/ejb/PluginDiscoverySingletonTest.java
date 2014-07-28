package com.choicemaker.fake.ejb;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.junit.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.fake.EmbeddedPluginDiscovery;

@RunWith(Arquillian.class)
public class PluginDiscoverySingletonTest {

	private static final String[] expected = new String[] {
		"META-INF/plugins/org_eclipse_core_runtime_2_1_1/plugin.xml",
		"META-INF/plugins/org_eclipse_core_resources_2_1_1/plugin.xml",
		"META-INF/plugins/com_choicemaker_eclipse_runtime_1_0_0/plugin.xml",
		"META-INF/plugins/com_choicemaker_cm_urm_2_5_0/plugin.xml",
		"META-INF/plugins/com_choicemaker_cm_urm_ejb_1_0_0/plugin.xml",
		"META-INF/plugins/com_choicemaker_cm_compiler_2_5_0/plugin.xml",
		"META-INF/plugins/com_choicemaker_cm_core_2_5_0/plugin.xml",
		"META-INF/plugins/com_choicemaker_cm_io_blocking_automated_base_2_5_0/plugin.xml",
		"META-INF/plugins/com_choicemaker_cm_io_blocking_automated_offline_2_5_0/plugin.xml",
		"META-INF/plugins/com_choicemaker_cm_io_blocking_automated_offline_core_2_5_0/plugin.xml",
		"META-INF/plugins/com_choicemaker_cm_io_blocking_automated_offline_server_2_5_0/plugin.xml",
		"META-INF/plugins/com_choicemaker_cm_matching_en_2_5_0/plugin.xml",
		"META-INF/plugins/com_choicemaker_cm_matching_en_us_2_5_0/plugin.xml",
		"META-INF/plugins/com_choicemaker_cm_matching_gen_2_5_0/plugin.xml",
		"META-INF/plugins/com_choicemaker_cm_server_ejb_base_2_5_0/plugin.xml",
		"META-INF/plugins/com_choicemaker_cm_io_blocking_base_2_5_0/plugin.xml",
		"META-INF/plugins/com_choicemaker_cm_io_db_base_2_5_0/plugin.xml",
		"META-INF/plugins/com_choicemaker_cm_io_flatfile_base_2_5_0/plugin.xml",
		"META-INF/plugins/com_choicemaker_cm_io_xml_base_2_5_0/plugin.xml",
		"META-INF/plugins/com_choicemaker_cm_matching_cfg_2_5_0/plugin.xml",
		"META-INF/plugins/com_choicemaker_cm_io_composite_base_2_5_0/plugin.xml",
		"META-INF/plugins/com_choicemaker_cm_reviewmaker_base_2_5_0/plugin.xml",
		"META-INF/plugins/com_choicemaker_cm_server_base_2_5_0/plugin.xml",
		"META-INF/plugins/com_choicemaker_cm_server_ejb_impl_2_5_0/plugin.xml",
		"META-INF/plugins/com_choicemaker_cm_transitivity_2_5_0/plugin.xml",
		"META-INF/plugins/com_choicemaker_cm_transitivity_core_2_5_0/plugin.xml",
		"META-INF/plugins/com_choicemaker_cm_transitivity_server_2_5_0/plugin.xml",
		"META-INF/plugins/javax_sql_2_0_0/plugin.xml",
		"META-INF/plugins/com_choicemaker_cm_report_db_2_5_0/plugin.xml",
		"META-INF/plugins/org_apache_ant_1_5_3/plugin.xml",
		"META-INF/plugins/org_apache_bcel_5_0_0/plugin.xml",
		"META-INF/plugins/org_apache_log4j_1_2_8/plugin.xml",
		"META-INF/plugins/org_apache_xerces_4_0_13/plugin.xml",
		"META-INF/plugins/org_jboss_autonumber_3_2_3/plugin.xml",
		"META-INF/plugins/org_jdom_0_9_0/plugin.xml",
		"META-INF/plugins/org_eclipse_ant_core_2_1_3/plugin.xml",
		"META-INF/plugins/com_choicemaker_cm_ml_me_base_2_5_0/plugin.xml",
		"META-INF/plugins/com_choicemaker_cm_report_file_2_5_0/plugin.xml"	
	};
	static {
		Arrays.sort(expected);
	}

	public static List<String> getExpected() {
		return Arrays.asList(expected);
	}
	
	@Inject
	PluginDiscoveryEJB pluginDiscoveryEJB;

	@Deployment
	public static JavaArchive createDeployment() {
		JavaArchive retVal =
			ShrinkWrap.create(JavaArchive.class)
					.addClass(PluginDiscoverySingletonBean.class)
					.addClass(PluginDiscoveryEJB.class)
					.addClass(com.choicemaker.fake.EmbeddedPluginDiscovery.class)
					.addClass(com.choicemaker.fake.IModel.class)
					.addClass(com.choicemaker.fake.InstallablePluginDiscovery.class)
					.addClass(com.choicemaker.fake.PluginDiscovery.class)
					.addClass(com.choicemaker.fake.PluginDiscoveryException.class)
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
		System.out.println(retVal.toString(true));
		return retVal;
	}

	@Test
	public void testListPluginIds() {
		
		// Get the computed list of plugin ids
		List<String> pluginIds = pluginDiscoveryEJB.listPluginIds();
		
		// Check the basics: not null, correct size
		Assert.assertTrue(pluginIds != null);
		Assert.assertTrue(pluginIds.size() == getExpected().size());

		// Check that there is no redundancy in the plugin list
		Set<String> normalized = new HashSet<>();
		for (String s : pluginIds) {
			int index = s.indexOf(EmbeddedPluginDiscovery.PREFIX);
			String s2 = s.substring(index);
			normalized.add(s2);
		}
		Assert.assertTrue(normalized.size() == getExpected().size());
		
		// Check that elements of the expected and computed lists are the same
		for (String s : getExpected()) {
			Assert.assertTrue(s + " in computed", normalized.contains(s));
		}
		// Given that the sizes are the same, this last test is redundant.
		// Unless there's a problem with the logic above, failure should never
		// occur after this point.
		for (String s : normalized) {
			Assert.assertTrue(s + " in expected", getExpected().contains(s));
		}
	}

}
