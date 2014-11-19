package com.choicemaker.cm.io.blocking.automated.offline.server.ejb;

import java.util.List;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.io.blocking.automated.AbaSettings;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.DefaultSettingsBean;

/**
 * Manages a database of ABA and OABA settings.
 * 
 * @author rphall
 *
 */
public interface SettingsManager {

	AbaSettings save(AbaSettings configuration);

	OabaSettings save(OabaSettings configuration);

	AbaSettings findAbaSettings(long id);

	OabaSettings findOabaSettings(long id);

	DefaultSettingsBean setDefaultAbaConfiguration(
			ImmutableProbabilityModel model, String databaseConfiguration,
			String blockingConfiguration, AbaSettings aba);

	DefaultSettingsBean setDefaultOabaConfiguration(ImmutableProbabilityModel model,
			String databaseConfiguration, String blockingConfiguration,
			OabaSettings oaba);

	AbaSettings findDefaultAbaSettings(
			ImmutableProbabilityModel model, String databaseConfiguration,
			String blockingConfiguration);

	OabaSettings findDefaultOabaSettings(ImmutableProbabilityModel model,
			String databaseConfiguration, String blockingConfiguration);

	List<AbaSettings> findAllAbaSettings();

	List<OabaSettings> findAllOabaSettings();

	List<DefaultSettingsBean> findAllDefaultAbaSettings();

	List<DefaultSettingsBean> findAllDefaultOabaSettings();

}
