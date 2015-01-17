package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

public interface DefaultSettingsJPA {

	String TABLE_NAME = "CMT_DEFAULT_SETTINGS";

	String CN_MODEL = "MODEL";
	String CN_TYPE = "TYPE";
	String CN_DATABASE_CONFIGURATION = "DATABASE_CONFIG";
	String CN_BLOCKING_CONFIGURATION = "BLOCKING_CONFIG";
	String CN_SETTINGS_ID = "SETTINGS_ID";

	String QN_DSET_FIND_ALL = "defaultSettingsFindAll";

	String JPQL_DSET_FIND_ALL = "Select dsb from DefaultSettingsEntity dsb";

	String QN_DSET_FIND_ALL_ABA = "defaultSettingsFindAllAba";

	String JPQL_DSET_FIND_ALL_ABA =
		"Select dsb from DefaultSettingsEntity dsb where dsb.key.type = 'ABA'";

	String QN_DSET_FIND_ALL_OABA = "defaultSettingsFindAllOaba";

	String JPQL_DSET_FIND_ALL_OABA =
		"Select dsb from DefaultSettingsEntity dsb where dsb.key.type = 'OABA'";

}
