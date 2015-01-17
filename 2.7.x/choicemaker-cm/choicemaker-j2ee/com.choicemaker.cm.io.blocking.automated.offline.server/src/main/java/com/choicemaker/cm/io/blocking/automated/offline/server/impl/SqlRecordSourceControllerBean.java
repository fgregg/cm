package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.args.PersistableSqlRecordSource;
import com.choicemaker.cm.core.ISerializableRecordSource;

@Stateless
public class SqlRecordSourceControllerBean /*
											 * implements RecordSourceController
											 */{

	private static final Logger logger = Logger
			.getLogger(SqlRecordSourceControllerBean.class.getName());

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	// @Override
	public PersistableSqlRecordSource save(final PersistableRecordSource rs) {
		if (rs == null) {
			throw new IllegalArgumentException("null settings");
		}
		if (!(rs instanceof PersistableSqlRecordSource)) {
			throw new IllegalArgumentException("invalid type: "
					+ rs.getClass().getName());
		}
		final String type = rs.getType();
		assert PersistableSqlRecordSource.TYPE.equals(type);

		// Has the record source already been persisted?
		final long rsId = rs.getId();
		SqlRecordSourceEntity retVal = null;
		if (PersistableRecordSource.NONPERSISTENT_ID != rsId) {
			// The record source appears to be persistent -- check the DB
			retVal = findInternal(rsId);
			if (retVal == null) {
				String msg =
					"The specified record source (" + rsId
							+ ") is missing in the DB. "
							+ "A new copy will be persisted.";
				logger.warning(msg);
				retVal = null;
			} else if (!retVal.equals(rs)) {
				String msg =
					"The specified record source (" + rsId
							+ ") is different in the DB. The DB copy will be "
							+ "used instead of the specified record source.";
				logger.warning(msg);
			}
		}

		// Conditionally save the record to the DB
		if (retVal == null) {
			// Save the specified settings to the DB
			PersistableSqlRecordSource psrs = (PersistableSqlRecordSource) rs;
			retVal = new SqlRecordSourceEntity(psrs);
			assert retVal.getId() == PersistableRecordSource.NONPERSISTENT_ID;
			em.persist(retVal);
			assert retVal.getId() != PersistableRecordSource.NONPERSISTENT_ID;
			String msg =
				"The record source is persistent in the database with id = "
						+ retVal.getId();
			logger.info(msg);
		}
		assert retVal != null;
		assert retVal.getId() != PersistableRecordSource.NONPERSISTENT_ID;
		return retVal;
	}

	// @Override
	public PersistableSqlRecordSource find(Long id, String type) {
		PersistableSqlRecordSource retVal = null;
		if (id != null && PersistableSqlRecordSource.TYPE.equals(type)) {
			retVal = findInternal(id.longValue());
		}
		return retVal;
	}

	protected SqlRecordSourceEntity findInternal(long id) {
		return em.find(SqlRecordSourceEntity.class, id);
	}

	// @Override
	public ISerializableRecordSource getRecordSource(Long rsId, String type)
			throws Exception {
		ISerializableRecordSource retVal = null;
		if (rsId != null && PersistableSqlRecordSource.TYPE.equals(type)) {
			PersistableSqlRecordSource psrs = findInternal(rsId.longValue());
			if (psrs != null) {
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
		}
		return retVal;
	}

	// @Override
	public List<PersistableRecordSource> findAll() {
		Query query = em.createNamedQuery(SqlRecordSourceJPA.QN_SQLRS_FIND_ALL);
		@SuppressWarnings("unchecked")
		List<PersistableRecordSource> retVal = query.getResultList();
		return retVal;
	}

}
