package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.io.blocking.automated.AbaSettings;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettings;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.SettingsManager;

@Stateless
public class SettingsManagerBean implements SettingsManager {

	private static final Logger logger = Logger
			.getLogger(ServerConfigurationBean.class.getName());

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	@Override
	public AbaSettings save(final AbaSettings settings) {
		if (settings == null) {
			throw new IllegalArgumentException("null settings");
		}
		// Have the settings already been persisted?
		AbaSettingsBean retVal = null;
		if (AbaSettingsBean.NONPERSISTENT_ABA_SETTINGS_ID != settings.getId()) {
			// Settings appear to be persistent -- check them against the DB
			retVal = findAbaSettingsInternal(settings.getId());
			if (retVal == null || !retVal.equals(settings)) {
				// The specified settings are missing or different in the DB.
				retVal = null;
			}
		}
		if (retVal == null) {
			// Save the specified settings to the DB
			retVal = new AbaSettingsBean(settings);
			assert retVal.getId() == AbaSettingsBean.NONPERSISTENT_ABA_SETTINGS_ID;
			em.persist(retVal);
		}
		assert retVal != null;
		assert retVal.getId() != AbaSettingsBean.NONPERSISTENT_ABA_SETTINGS_ID;

		return retVal;
	}

	@Override
	public OabaSettings save(final OabaSettings settings) {
		if (settings == null) {
			throw new IllegalArgumentException("null settings");
		}
		// Have the settings already been persisted?
		OabaSettingsBean retVal = null;
		if (OabaSettingsBean.NONPERSISTENT_ABA_SETTINGS_ID != settings.getId()) {
			// Settings appear to be persistent -- check them against the DB
			retVal = findOabaSettingsInternal(settings.getId());
			if (retVal == null || !retVal.equals(settings)) {
				// The specified settings are missing or different in the DB.
				retVal = null;
			}
		}
		if (retVal == null) {
			// Save the specified settings to the DB
			retVal = new OabaSettingsBean(settings);
			assert retVal.getId() == OabaSettingsBean.NONPERSISTENT_ABA_SETTINGS_ID;
			em.persist(retVal);
		}
		assert retVal != null;
		assert retVal.getId() != OabaSettingsBean.NONPERSISTENT_ABA_SETTINGS_ID;

		return retVal;
	}

	@Override
	public AbaSettings findAbaSettings(long id) {
		return findAbaSettingsInternal(id);
	}

	protected AbaSettingsBean findAbaSettingsInternal(long id) {
		return em.find(AbaSettingsBean.class, id);
	}

	@Override
	public OabaSettings findOabaSettings(long id) {
		return findOabaSettingsInternal(id);
	}

	protected OabaSettingsBean findOabaSettingsInternal(long id) {
		return em.find(OabaSettingsBean.class, id);
	}

	@Override
	public DefaultSettingsBean setDefaultAbaConfiguration(
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
		DefaultSettingsBean old = em.find(DefaultSettingsBean.class, pk);
		if (old != null) {
			aba = findAbaSettings(old.getSettingsId());
			if (!settings.equals(aba)) {
				aba = null;
				em.remove(old);
			}
		}

		// Conditionally save the specified settings as the new default
		DefaultSettingsBean retVal = null;
		if (aba == null) {
			aba = save(settings);
			retVal =
				new DefaultSettingsBean(pk, aba.getId());
			em.persist(retVal);
		}
		assert retVal != null;
		assert aba != null;
		assert aba.getId() != OabaSettingsBean.NONPERSISTENT_ABA_SETTINGS_ID;

		return retVal;
	}

	@Override
	public DefaultSettingsBean setDefaultOabaConfiguration(
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
		DefaultSettingsBean old = em.find(DefaultSettingsBean.class, pk);
		if (old != null) {
			oaba = findOabaSettings(old.getSettingsId());
			if (!settings.equals(oaba)) {
				oaba = null;
				em.remove(old);
			}
		}

		// Conditionally save the specified settings as the new default
		DefaultSettingsBean retVal = null;
		if (oaba == null) {
			oaba = save(settings);
			retVal =
				new DefaultSettingsBean(pk, oaba.getId());
			em.persist(retVal);
		}
		assert retVal != null;
		assert oaba != null;
		assert oaba.getId() != OabaSettingsBean.NONPERSISTENT_ABA_SETTINGS_ID;

		return retVal;
	}

	@Override
	public AbaSettings findDefaultAbaSettings(ImmutableProbabilityModel model,
			String databaseConfiguration, String blockingConfiguration) {
		final DefaultSettingsPK pk =
			new DefaultSettingsPK(model.getModelName(),
					AbaSettingsJPA.DISCRIMINATOR_VALUE, databaseConfiguration,
					blockingConfiguration);
		DefaultSettingsBean dsb = em.find(DefaultSettingsBean.class, pk);
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
	public OabaSettings findDefaultOabaSettings(
			ImmutableProbabilityModel model, String databaseConfiguration,
			String blockingConfiguration) {
		final DefaultSettingsPK pk =
			new DefaultSettingsPK(model.getModelName(),
					OabaSettingsJPA.DISCRIMINATOR_VALUE, databaseConfiguration,
					blockingConfiguration);
		DefaultSettingsBean dsb = em.find(DefaultSettingsBean.class, pk);
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
	public List<DefaultSettingsBean> findAllDefaultAbaSettings() {
		Query query =
			em.createNamedQuery(DefaultSettingsJPA.QN_DSET_FIND_ALL_ABA);
		@SuppressWarnings("unchecked")
		List<DefaultSettingsBean> retVal = query.getResultList();
		return retVal;
	}

	@Override
	public List<DefaultSettingsBean> findAllDefaultOabaSettings() {
		Query query =
			em.createNamedQuery(DefaultSettingsJPA.QN_DSET_FIND_ALL_OABA);
		@SuppressWarnings("unchecked")
		List<DefaultSettingsBean> retVal = query.getResultList();
		return retVal;
	}

}
