package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.choicemaker.cm.args.AbaSettings;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;

@Stateless
public class OabaSettingsControllerBean implements OabaSettingsController {

	private static final Logger logger = Logger
			.getLogger(OabaSettingsControllerBean.class.getName());

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	@EJB
	private OabaJobControllerBean jobController;

	@Override
	public AbaSettings save(final AbaSettings settings) {
		if (settings == null) {
			throw new IllegalArgumentException("null settings");
		}
		// Have the settings already been persisted?
		final long settingsId = settings.getId();
		AbaSettingsEntity retVal = null;
		if (AbaSettings.NONPERSISTENT_ABA_SETTINGS_ID != settingsId) {
			// Settings appear to be persistent -- check them against the DB
			retVal = findAbaSettingsInternal(settingsId);
			if (retVal == null) {
				String msg =
					"The specified settings (" + settingsId
							+ ") are missing in the DB. "
							+ "A new copy will be persisted.";
				logger.warning(msg);
				retVal = null;
			} else if (!retVal.equals(settings)) {
				String msg =
					"The specified settings ("
							+ settingsId
							+ ") are different in the DB. "
							+ "The DB values will be used instead of the specified values.";
				logger.warning(msg);
			}
		}
		if (retVal == null) {
			// Save the specified settings to the DB
			retVal = new AbaSettingsEntity(settings);
			assert retVal.getId() == AbaSettings.NONPERSISTENT_ABA_SETTINGS_ID;
			em.persist(retVal);
			assert retVal.getId() != AbaSettings.NONPERSISTENT_ABA_SETTINGS_ID;
			String msg =
				"The specified settings were persisted in the database with settings id = "
						+ retVal.getId();
			logger.info(msg);
		}
		assert retVal != null;
		assert retVal.getId() != AbaSettings.NONPERSISTENT_ABA_SETTINGS_ID;

		return retVal;
	}

	@Override
	public OabaSettings save(final OabaSettings settings) {
		if (settings == null) {
			throw new IllegalArgumentException("null settings");
		}
		// Have the settings already been persisted?
		final long settingsId = settings.getId();
		OabaSettingsEntity retVal = null;
		if (AbaSettings.NONPERSISTENT_ABA_SETTINGS_ID != settingsId) {
			// Settings appear to be persistent -- check them against the DB
			retVal = findOabaSettingsInternal(settingsId);
			if (retVal == null) {
				String msg =
					"The specified settings (" + settingsId
							+ ") are missing in the DB. "
							+ "A new copy will be persisted.";
				logger.warning(msg);
				retVal = null;
			} else if (!retVal.equals(settings)) {
				String msg =
					"The specified settings ("
							+ settingsId
							+ ") are different in the DB. "
							+ "The DB values will be used instead of the specified values.";
				logger.warning(msg);
			}
		}
		if (retVal == null) {
			// Save the specified settings to the DB
			retVal = new OabaSettingsEntity(settings);
			assert retVal.getId() == AbaSettings.NONPERSISTENT_ABA_SETTINGS_ID;
			em.persist(retVal);
			assert retVal.getId() != AbaSettings.NONPERSISTENT_ABA_SETTINGS_ID;
			String msg =
				"The specified settings were persisted in the database with settings id = "
						+ retVal.getId();
			logger.info(msg);
		}
		assert retVal != null;
		assert retVal.getId() != AbaSettings.NONPERSISTENT_ABA_SETTINGS_ID;
		return retVal;
	}

	@Override
	public AbaSettings findAbaSettings(long id) {
		return findAbaSettingsInternal(id);
	}

	protected AbaSettingsEntity findAbaSettingsInternal(long id) {
		return em.find(AbaSettingsEntity.class, id);
	}

	@Override
	public OabaSettings findOabaSettings(long id) {
		return findOabaSettingsInternal(id);
	}

	protected OabaSettingsEntity findOabaSettingsInternal(long id) {
		return em.find(OabaSettingsEntity.class, id);
	}

	@Override
	public OabaSettings findOabaSettingsByJobId(long jobId) {
		OabaSettings retVal = null;
		OabaJob oabaJob = jobController.findOabaJob(jobId);
		if (oabaJob != null) {
			long settingsId = oabaJob.getOabaSettingsId();
			retVal = findOabaSettings(settingsId);
		}
		return retVal;
	}

	@Override
	public DefaultSettingsEntity setDefaultAbaConfiguration(
			ImmutableProbabilityModel model, String databaseConfiguration,
			String blockingConfiguration, AbaSettings settings) {
		if (settings == null) {
			throw new IllegalArgumentException("null settings");
		}

		// Create a primary key for the default (validates other arguments)
		final DefaultSettingsPK pk =
			new DefaultSettingsPK(model.getModelName(),
					AbaSettingsJPA.DISCRIMINATOR_VALUE, databaseConfiguration,
					blockingConfiguration);

		// Remove the existing default if it is different
		AbaSettings aba = null;
		DefaultSettingsEntity old = em.find(DefaultSettingsEntity.class, pk);
		if (old != null) {
			aba = findAbaSettings(old.getSettingsId());
			if (!settings.equals(aba)) {
				aba = null;
				em.remove(old);
			}
		}

		// Conditionally save the specified settings as the new default
		DefaultSettingsEntity retVal = null;
		if (aba == null) {
			aba = save(settings);
			retVal = new DefaultSettingsEntity(pk, aba.getId());
			em.persist(retVal);
		}
		assert retVal != null;
		assert aba != null;
		assert aba.getId() != AbaSettings.NONPERSISTENT_ABA_SETTINGS_ID;

		return retVal;
	}

	@Override
	public DefaultSettingsEntity setDefaultOabaConfiguration(
			ImmutableProbabilityModel model, String databaseConfiguration,
			String blockingConfiguration, OabaSettings settings) {
		if (settings == null) {
			throw new IllegalArgumentException("null settings");
		}

		// Create a primary key for the default (validates other arguments)
		final DefaultSettingsPK pk =
			new DefaultSettingsPK(model.getModelName(),
					OabaSettingsJPA.DISCRIMINATOR_VALUE, databaseConfiguration,
					blockingConfiguration);

		// Remove the existing default if it is different
		OabaSettings oaba = null;
		DefaultSettingsEntity old = em.find(DefaultSettingsEntity.class, pk);
		if (old != null) {
			oaba = findOabaSettings(old.getSettingsId());
			if (!settings.equals(oaba)) {
				oaba = null;
				em.remove(old);
			}
		}

		// Conditionally save the specified settings as the new default
		DefaultSettingsEntity retVal = null;
		if (oaba == null) {
			oaba = save(settings);
			retVal = new DefaultSettingsEntity(pk, oaba.getId());
			em.persist(retVal);
		}
		assert retVal != null;
		assert oaba != null;
		assert oaba.getId() != AbaSettings.NONPERSISTENT_ABA_SETTINGS_ID;

		return retVal;
	}

	@Override
	public AbaSettings findDefaultAbaSettings(ImmutableProbabilityModel model) {
		if (model == null) {
			throw new IllegalArgumentException("null modelId");
		}
		return findDefaultAbaSettings(model.getModelName(),
				model.getDatabaseConfigurationName(),
				model.getBlockingConfigurationName());
	}

	@Override
	public AbaSettings findDefaultAbaSettings(String modelConfigurationId,
			String databaseConfiguration, String blockingConfiguration) {
		final DefaultSettingsPK pk =
			new DefaultSettingsPK(modelConfigurationId,
					AbaSettingsJPA.DISCRIMINATOR_VALUE, databaseConfiguration,
					blockingConfiguration);
		DefaultSettingsEntity dsb = em.find(DefaultSettingsEntity.class, pk);
		AbaSettings retVal = null;
		if (dsb != null) {
			final long settingsId = dsb.getSettingsId();
			retVal = findAbaSettings(settingsId);
			if (retVal == null) {
				String msg =
					"Invalid settings identifier for " + pk.toString() + ": "
							+ settingsId;
				logger.severe(msg);
			}
		}
		return retVal;
	}

	@Override
	public OabaSettings findDefaultOabaSettings(ImmutableProbabilityModel model) {
		return findDefaultOabaSettings(model.getModelName(),
				model.getDatabaseConfigurationName(),
				model.getBlockingConfigurationName());
	}

	@Override
	public OabaSettings findDefaultOabaSettings(String modelConfigurationId,
			String databaseConfiguration, String blockingConfiguration) {
		final DefaultSettingsPK pk =
			new DefaultSettingsPK(modelConfigurationId,
					OabaSettingsJPA.DISCRIMINATOR_VALUE, databaseConfiguration,
					blockingConfiguration);
		DefaultSettingsEntity dsb = em.find(DefaultSettingsEntity.class, pk);
		OabaSettings retVal = null;
		if (dsb != null) {
			final long settingsId = dsb.getSettingsId();
			retVal = findOabaSettings(settingsId);
			if (retVal == null) {
				String msg =
					"Invalid settings identifier for " + pk.toString() + ": "
							+ settingsId;
				logger.severe(msg);
			}
		}
		return retVal;
	}

	@Override
	public List<AbaSettings> findAllAbaSettings() {
		Query query = em.createNamedQuery(AbaSettingsJPA.QN_ABA_FIND_ALL);
		@SuppressWarnings("unchecked")
		List<AbaSettings> retVal = query.getResultList();
		return retVal;
	}

	@Override
	public List<OabaSettings> findAllOabaSettings() {
		Query query = em.createNamedQuery(OabaSettingsJPA.QN_OABA_FIND_ALL);
		@SuppressWarnings("unchecked")
		List<OabaSettings> retVal = query.getResultList();
		return retVal;
	}

	@Override
	public List<DefaultSettingsEntity> findAllDefaultAbaSettings() {
		Query query =
			em.createNamedQuery(DefaultSettingsJPA.QN_DSET_FIND_ALL_ABA);
		@SuppressWarnings("unchecked")
		List<DefaultSettingsEntity> retVal = query.getResultList();
		return retVal;
	}

	@Override
	public List<DefaultSettingsEntity> findAllDefaultOabaSettings() {
		Query query =
			em.createNamedQuery(DefaultSettingsJPA.QN_DSET_FIND_ALL_OABA);
		@SuppressWarnings("unchecked")
		List<DefaultSettingsEntity> retVal = query.getResultList();
		return retVal;
	}

}
