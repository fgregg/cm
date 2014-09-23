package com.choicemaker.eclipse2.pd;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ExampleData {

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
				"org_jboss_autonumber_3_2_3/plugin.xml",
				"org_jdom_0_9_0/plugin.xml",
				"org_eclipse_ant_core_2_1_3/plugin.xml",
				"com_choicemaker_cm_ml_me_base_2_5_0/plugin.xml",
				"com_choicemaker_cm_report_file_2_5_0/plugin.xml",
				"plugin/plugin.xml" };
	static {
		Arrays.sort(_expected_1);
	}
	public static final List<String> EXPECTED_1 = Collections
			.unmodifiableList(Arrays.asList(_expected_1));

	private static final String[] _expected_2 =
		new String[] {
				"com_choicemaker_cm_bootstrapping_2_5_0/plugin.xml",
				"com_choicemaker_cm_cass_2_5_0/plugin.xml",
				"com_choicemaker_cm_compiler_2_5_0/plugin.xml",
				"com_choicemaker_cm_compiler_tools_jdk142_2_5_0/fragment.xml",
				"com_choicemaker_cm_core_2_5_0/plugin.xml",
				"com_choicemaker_cm_docs_2_5_0/plugin.xml",
				"com_choicemaker_cm_gui_utils_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_blocking_automated_base_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_blocking_automated_offline_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_blocking_automated_offline_core_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_blocking_automated_offline_server_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_blocking_base_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_blocking_exact_base_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_blocking_exact_gui_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_composite_base_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_composite_gui_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_db_base_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_db_db2_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_db_gui_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_db_oracle_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_db_sqlserver_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_db_sqlserver_gui_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_flatfile_base_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_flatfile_gui_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_xml_base_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_xml_gui_2_5_0/plugin.xml",
				"com_choicemaker_cm_matching_cfg_2_5_0/plugin.xml",
				"com_choicemaker_cm_matching_en_2_5_0/plugin.xml",
				"com_choicemaker_cm_matching_en_us_2_5_0/plugin.xml",
				"com_choicemaker_cm_matching_en_us_ny_nyc_2_5_0/plugin.xml",
				"com_choicemaker_cm_matching_gen_2_5_0/plugin.xml",
				"com_choicemaker_cm_matching_geo_2_5_0/plugin.xml",
				"com_choicemaker_cm_matching_intl_2_5_0/plugin.xml",
				"com_choicemaker_cm_ml_me_base_2_5_0/plugin.xml",
				"com_choicemaker_cm_ml_me_gui_2_5_0/plugin.xml",
				"com_choicemaker_cm_ml_svm_base_2_5_0/plugin.xml",
				"com_choicemaker_cm_ml_svm_gui_2_5_0/plugin.xml",
				"com_choicemaker_cm_modelmaker_2_5_0/plugin.xml",
				"com_choicemaker_cm_report_db_2_5_0/plugin.xml",
				"com_choicemaker_cm_report_file_2_5_0/plugin.xml",
				"com_choicemaker_cm_reviewmaker_base_2_5_0/plugin.xml",
				"com_choicemaker_cm_server_base_2_5_0/plugin.xml",
				"com_choicemaker_cm_server_ejb_impl_2_5_0/plugin.xml",
				"com_choicemaker_cm_urm_2_5_0/plugin.xml",
				"com_choicemaker_eclipse_runtime_1_0_0/plugin.xml",
				"com_jrefinery_0_9_6/plugin.xml", "javax_ejb_2_0_0/plugin.xml",
				"javax_help_2_0_0/plugin.xml", "javax_mail_1_3_0/plugin.xml",
				"javax_sql_2_0_0/plugin.xml",
				"org_apache_ant_1_5_3/plugin.xml",
				"org_apache_bcel_5_0_0/plugin.xml",
				"org_apache_log4j_1_2_8/plugin.xml",
				"org_eclipse_ant_core_2_1_1/plugin.xml",
				"org_eclipse_core_boot_2_1_1/plugin.xml",
				"org_eclipse_core_resources_2_1_1/plugin.xml",
				"org_eclipse_core_runtime_2_1_1/plugin.xml",
				"org_jboss_autonumber_3_2_3/plugin.xml",
				"org_jdom_0_9_0/plugin.xml" };
	static {
		Arrays.sort(_expected_2);
	}
	public static final List<String> EXPECTED_2 = Collections
			.unmodifiableList(Arrays.asList(_expected_2));

	private static final String[] _expected_3 =
		new String[] {
				"com_choicemaker_cm_compiler_2_5_0/plugin.xml",
				"com_choicemaker_cm_compiler_tools_jdk142_2_5_0/fragment.xml",
				"com_choicemaker_cm_core_2_5_0/plugin.xml",
				"com_choicemaker_cm_gui_utils_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_blocking_automated_base_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_blocking_automated_offline_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_blocking_automated_offline_core_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_blocking_automated_offline_server_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_blocking_base_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_blocking_exact_base_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_blocking_exact_gui_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_composite_base_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_composite_gui_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_db_base_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_db_gui_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_db_oracle_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_db_sqlserver_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_db_sqlserver_gui_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_flatfile_base_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_flatfile_gui_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_xml_base_2_5_0/plugin.xml",
				"com_choicemaker_cm_io_xml_gui_2_5_0/plugin.xml",
				"com_choicemaker_cm_matching_cfg_2_5_0/plugin.xml",
				"com_choicemaker_cm_matching_en_2_5_0/plugin.xml",
				"com_choicemaker_cm_matching_en_us_2_5_0/plugin.xml",
				"com_choicemaker_cm_matching_en_us_ny_nyc_2_5_0/plugin.xml",
				"com_choicemaker_cm_matching_gen_2_5_0/plugin.xml",
				"com_choicemaker_cm_matching_geo_2_5_0/plugin.xml",
				"com_choicemaker_cm_matching_intl_2_5_0/plugin.xml",
				"com_choicemaker_cm_ml_me_base_2_5_0/plugin.xml",
				"com_choicemaker_cm_ml_me_gui_2_5_0/plugin.xml",
				"com_choicemaker_cm_modelmaker_2_5_0/plugin.xml",
				"com_choicemaker_cm_report_db_2_5_0/plugin.xml",
				"com_choicemaker_cm_report_file_2_5_0/plugin.xml",
				"com_choicemaker_cm_server_base_2_5_0/plugin.xml",
				"com_choicemaker_cm_server_ejb_impl_2_5_0/plugin.xml",
				"com_choicemaker_cm_urm_2_5_0/plugin.xml",
				"com_choicemaker_eclipse_runtime_1_0_0/plugin.xml",
				"com_jrefinery_0_9_6/plugin.xml", "javax_ejb_2_0_0/plugin.xml",
				"javax_help_2_0_0/plugin.xml", "javax_mail_1_3_0/plugin.xml",
				"javax_sql_2_0_0/plugin.xml",
				"org_apache_ant_1_5_3/plugin.xml",
				"org_apache_bcel_5_0_0/plugin.xml",
				"org_apache_log4j_1_2_8/plugin.xml",
				"org_eclipse_ant_core_2_1_1/plugin.xml",
				"org_eclipse_core_boot_2_1_1/plugin.xml",
				"org_eclipse_core_resources_2_1_1/plugin.xml",
				"org_eclipse_core_runtime_2_1_1/plugin.xml",
				"org_jboss_autonumber_3_2_3/plugin.xml",
				"org_jdom_0_9_0/plugin.xml" };
	static {
		Arrays.sort(_expected_3);
	}
	public static final List<String> EXPECTED_3 = Collections
			.unmodifiableList(Arrays.asList(_expected_3));

	private static final String[] _expected_4 =
		new String[] { "com_choicemaker_cm_compiler_2_5_0/plugin.xml", };
	static {
		Arrays.sort(_expected_4);
	}
	public static final List<String> EXPECTED_4 = Collections
			.unmodifiableList(Arrays.asList(_expected_4));

	private static Map<String, List<String>> _testData = new HashMap<>();
	static {
		_testData.put("default", EXPECTED_1);
		_testData.put("config_02", EXPECTED_2);
		_testData.put("config_03", EXPECTED_3);
		_testData.put("config_04_BAD_DUP_PATHS", EXPECTED_4);
	}
	public static final Map<String, List<String>> TEST_DATA = Collections
			.unmodifiableMap(_testData);
	
	private static List<String> _bad_configurations = new LinkedList<>();
	static {
		_bad_configurations.add("config_05_BAD_DUP_PLUGIN");
		_bad_configurations.add("config_06_BAD_INVALID_PLUGIN");
		_bad_configurations.add("config_07_BAD_INVALID_PLUGIN");
		_bad_configurations.add("config_08_BAD_INVALID_PLUGIN");
		_bad_configurations.add("config_09_BAD_INVALID_FRAGMENT");
		_bad_configurations.add("config_10_BAD_INVALID_FRAGMENT");
		_bad_configurations.add("config_11_BAD_INVALID_FRAGMENT");
	}
	public static final List<String> BAD_CONFIGURATIONS = Collections
			.unmodifiableList(_bad_configurations);

	private ExampleData() {
	}

}
