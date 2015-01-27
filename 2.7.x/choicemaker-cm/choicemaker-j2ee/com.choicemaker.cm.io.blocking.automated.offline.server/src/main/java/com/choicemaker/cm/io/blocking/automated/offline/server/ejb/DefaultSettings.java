package com.choicemaker.cm.io.blocking.automated.offline.server.ejb;

import com.choicemaker.cm.io.blocking.automated.offline.server.impl.DefaultSettingsPK;

public interface DefaultSettings {

	DefaultSettingsPK getPrimaryKey();

	String getModel();

	String getType();

	String getDatabaseConfiguration();

	String getBlockingConfiguration();

	long getSettingsId();

}