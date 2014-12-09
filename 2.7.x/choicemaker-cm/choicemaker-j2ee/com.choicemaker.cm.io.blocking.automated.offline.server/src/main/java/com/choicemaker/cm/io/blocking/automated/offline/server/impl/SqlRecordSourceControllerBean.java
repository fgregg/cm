package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.choicemaker.cm.args.PersistableSqlRecordSource;
import com.choicemaker.cm.core.ISerializableRecordSource;

@Stateless
public class SqlRecordSourceControllerBean {

	private static final Logger logger = Logger
			.getLogger(SqlRecordSourceControllerBean.class.getName());

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

//	@EJB
//	private OabaJobControllerBean jobController;

	public PersistableSqlRecordSource save(final PersistableSqlRecordSource psrs) {
		if (psrs == null) {
			throw new IllegalArgumentException("null settings");
		}
		// Have the settings already been persisted?
		final long settingsId = psrs.getId();
		SqlRecordSourceEntity retVal = null;
		if (SqlRecordSourceEntity.NONPERSISTENT_ID != settingsId) {
			// Settings appear to be persistent -- check them against the DB
			retVal = findInternal(settingsId);
			if (retVal == null) {
				String msg = "The specified settings (" + settingsId + ") are missing in the DB. "
						+ "A new copy will be persisted.";
				logger.warning(msg);
				retVal = null;
			} else if (!retVal.equals(psrs)) {
				String msg = "The specified settings (" + settingsId + ") are different in the DB. "
						+ "The DB values will be used instead of the specified values.";
				logger.warning(msg);
			}
		}
		if (retVal == null) {
			// Save the specified settings to the DB
			retVal = new SqlRecordSourceEntity(psrs);
			assert retVal.getId() == SqlRecordSourceEntity.NONPERSISTENT_ID;
			em.persist(retVal);
			assert retVal.getId() != SqlRecordSourceEntity.NONPERSISTENT_ID;
			String msg = "The specified settings were persisted in the database with settings id = " + retVal.getId();
			logger.info(msg);
		}
		assert retVal != null;
		assert retVal.getId() != SqlRecordSourceEntity.NONPERSISTENT_ID;
		return retVal;
	}

	public PersistableSqlRecordSource find(long id) {
		return findInternal(id);
	}

	protected SqlRecordSourceEntity findInternal(long id) {
		return em.find(SqlRecordSourceEntity.class, id);
	}

	public ISerializableRecordSource getRecordSource(
			PersistableSqlRecordSource psrs) throws ClassNotFoundException,
			NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, ClassCastException {
		ISerializableRecordSource retVal = null;
		if (psrs == null) {
			logger.warning("null persistable record source");
		} else {
			Class<?> c = Class.forName(psrs.getClassName());
			Constructor<?> ctor =
				c.getConstructor(String.class, String.class, String.class,
						String.class);
			retVal =
				(ISerializableRecordSource) ctor.newInstance(
						psrs.getDataSource(), psrs.getModelId(),
						psrs.getDatabaseConfiguration(),
						psrs.getSqlSelectStatement());
		}
		return retVal;
	}

	public List<PersistableSqlRecordSource> findAll() {
		Query query = em.createNamedQuery(SqlRecordSourceJPA.QN_SQLRS_FIND_ALL);
		@SuppressWarnings("unchecked")
		List<PersistableSqlRecordSource> retVal = query.getResultList();
		return retVal;
	}

}
