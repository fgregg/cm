package com.choicemaker.fake.ejb;

import static com.choicemaker.fake.EmbeddedPluginDiscovery.PREFIX;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Utils {

	public static void compare(Set<URL> discovered, List<String> expected) {
		assertTrue(discovered.size() == expected.size());
		for (URL url : discovered) {
			String s = url.toString();
			int index = s.indexOf(PREFIX) + PREFIX.length();
			String path = s.substring(index);
			assertTrue(expected.contains(path));
		}
	}

	private static final String[] _expected_1 =
		new String[] {
				"org_eclipse_core_runtime_2_1_1/plugin.xml",
				"org_eclipse_core_resources_2_1_1/plugin.xml",
				"com_choicemaker_eclipse_runtime_1_0_0/plugin.xml",
				"com_choicemaker_cm_urm_2_5_0/plugin.xml",
				"com_choicemaker_cm_urm_ejb_1_0_0/plugin.xml",
				"com_choicemaker_cm_compiler_2_5_0/plugin.xml",
				"com_choicemaker_cm_core_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_blocking_automated_base_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_blocking_automated_offline_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_blocking_automated_offline_core_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_blocking_automated_offline_server_2_5_0/plugin.xml",
				"com_choicemaker_cm_matching_en_2_5_0/plugin.xml",
				"com_choicemaker_cm_matching_en_us_2_5_0/plugin.xml",
				"com_choicemaker_cm_matching_gen_2_5_0/plugin.xml",
				"com_choicemaker_cm_server_ejb_base_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_blocking_base_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_db_base_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_flatfile_base_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_xml_base_2_5_0/plugin.xml",
				"com_choicemaker_cm_matching_cfg_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_composite_base_2_5_0/plugin.xml",
				"com_choicemaker_cm_reviewmaker_base_2_5_0/plugin.xml",
				"com_choicemaker_cm_server_base_2_5_0/plugin.xml",
				"com_choicemaker_cm_server_ejb_impl_2_5_0/plugin.xml",
				"com_choicemaker_cm_transitivity_2_5_0/plugin.xml",
				"com_choicemaker_cm_transitivity_core_2_5_0/plugin.xml",
				"com_choicemaker_cm_transitivity_server_2_5_0/plugin.xml",
				"javax_sql_2_0_0/plugin.xml",
				"com_choicemaker_cm_report_db_2_5_0/plugin.xml",
				"org_apache_ant_1_5_3/plugin.xml",
				"org_apache_bcel_5_0_0/plugin.xml",
				"org_apache_log4j_1_2_8/plugin.xml",
				"org_apache_xerces_4_0_13/plugin.xml",
				"org_jboss_autonumber_3_2_3/plugin.xml",
				"org_jdom_0_9_0/plugin.xml",
				"org_eclipse_ant_core_2_1_3/plugin.xml",
				"com_choicemaker_cm_ml_me_base_2_5_0/plugin.xml",
				"com_choicemaker_cm_report_file_2_5_0/plugin.xml"	};
	static {
		Arrays.sort(_expected_1);
	}
	public static final List<String> EXPECTED_1 = Collections
			.unmodifiableList(Arrays.asList(_expected_1));

	private Utils() {
	}

}
