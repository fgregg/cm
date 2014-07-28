package com.choicemaker.fake;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class EmbeddedPluginDiscoveryTest {
	
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

	@Test
	public void testListPluginIds() {
		PluginDiscovery pd = new EmbeddedPluginDiscovery();
		List<String> pluginIds = pd.listPluginIds();
		Assert.assertTrue(pluginIds != null);
		Assert.assertTrue(pluginIds.size() == expected.length);

		Set<String> normalized = new HashSet<>();
		for (String s : pluginIds) {
			int index = s.indexOf(EmbeddedPluginDiscovery.PREFIX);
			String s2 = s.substring(index);
			normalized.add(s2);
		}
		String[] computed = normalized.toArray(new String[pluginIds.size()]);
		Arrays.sort(computed);

		Assert.assertArrayEquals(expected, computed);
	}

}
