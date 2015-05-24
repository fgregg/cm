package com.choicemaker.cm.io.blocking.automated.offline.server.ejb;

import java.util.List;

import javax.ejb.Local;

import com.choicemaker.cm.args.AbaSettings;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.DefaultSettingsEntity;

/**
 * Manages a database of ABA and OABA settings.
 * 
 * @author rphall
 *
 */
@Local
public interface OabaSettingsController {

	AbaSettings save(AbaSettings settings);

	OabaSettings save(OabaSettings settings);

	AbaSettings findAbaSettings(long id);

	OabaSettings findOabaSettings(long id);

	OabaSettings findOabaSettingsByJobId(long jobId);

	DefaultSettingsEntity setDefaultAbaConfiguration(
			ImmutableProbabilityModel model, String databaseConfiguration,
			String blockingConfiguration, AbaSettings aba);

	DefaultSettingsEntity setDefaultOabaConfiguration(
			ImmutableProbabilityModel model, String databaseConfiguration,
			String blockingConfiguration, OabaSettings oaba);

	AbaSettings findDefaultAbaSettings(String modelConfigurationId,
			String databaseConfiguration, String blockingConfiguration);

//	AbaSettings findDefaultAbaSettings(ImmutableProbabilityModel model,
//			String databaseConfigurationName, String blockingConfigurationName);

	OabaSettings findDefaultOabaSettings(String modelConfigurationId,
			String databaseConfiguration, String blockingConfiguration);

//	OabaSettings findDefaultOabaSettings(ImmutableProbabilityModel model,
//	String databaseConfigurationName, String blockingConfigurationName);

	List<AbaSettings> findAllAbaSettings();

	List<OabaSettings> findAllOabaSettings();

	List<DefaultSettingsEntity> findAllDefaultAbaSettings();

	List<DefaultSettingsEntity> findAllDefaultOabaSettings();

}
