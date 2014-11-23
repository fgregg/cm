package com.choicemaker.cm.io.blocking.automated.offline.server.impl;


public interface DefaultServerConfigurationJPA {

	String TABLE_NAME = "CMT_DEFAULT_SERVER_CONFIG";

	String CN_SERVERCONFIG = "SERVER_CONFIG";

	String CN_HOSTNAME = "HOST_NAME";

	String QN_DSC_FIND_ALL = "defaultServerConfigFindAll";

	String JPQL_DSC_FIND_ALL =
		"Select dscb from DefaultServerConfigurationEntity dscb";

}
