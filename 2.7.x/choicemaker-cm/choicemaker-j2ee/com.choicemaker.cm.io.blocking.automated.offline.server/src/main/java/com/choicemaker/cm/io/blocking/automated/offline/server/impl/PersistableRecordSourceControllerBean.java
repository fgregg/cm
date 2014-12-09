package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaTaskType;
import com.choicemaker.cm.args.PersistableFlatFileRecordSource;
import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.args.PersistableSqlRecordSource;
import com.choicemaker.cm.args.PersistableXmlRecordSource;

@Stateless
public class PersistableRecordSourceControllerBean {

	private static final Logger logger = Logger
			.getLogger(PersistableRecordSourceControllerBean.class.getName());

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	@EJB
	private OabaJobControllerBean jobController;

	@EJB
	private OabaParametersControllerBean paramsController;

	@EJB
	private SqlRecordSourceControllerBean sqlRsController;

	public PersistableRecordSource save(final PersistableRecordSource psrs) {
		if (psrs == null) {
			throw new IllegalArgumentException("null settings");
		}
		final String type = psrs.getType();
		PersistableRecordSource retVal = null;
		if (PersistableSqlRecordSource.TYPE.equals(type)) {
			assert psrs instanceof PersistableSqlRecordSource;
			PersistableSqlRecordSource sqlRs =
				(PersistableSqlRecordSource) psrs;
			retVal = sqlRsController.save(sqlRs);
		} else if (PersistableFlatFileRecordSource.TYPE.equals(type)) {
			throw new Error("not yet implemented for record source type: '" + type + "'");
		} else if (PersistableXmlRecordSource.TYPE.equals(type)) {
			throw new Error("not yet implemented for record source type: '" + type + "'");
		} else {
			throw new IllegalStateException("unknown record source type: '" + type + "'");
		}
		assert retVal != null;
		return retVal;
	}

	public PersistableRecordSource findStagingRecordSource(long rsId, String type) {
		PersistableRecordSource retVal = null;
		if (rsId == PersistableRecordSource.NONPERSISTENT_ID) {
			logger.info("non-persistent record source id; returning null");
		} else if (type == null) {
			logger.info("null record source type; returning null");
		} else {
			type = type.trim().toUpperCase();
			if (type.isEmpty()) {
				logger.info("blank record source type; returning null");
			}
			if (PersistableSqlRecordSource.TYPE.equals(type)) {
				retVal = sqlRsController.find(rsId);
			} else if (PersistableFlatFileRecordSource.TYPE.equals(type)) {
				throw new Error("not yet implemented for record source type: '" + type + "'");
			} else if (PersistableXmlRecordSource.TYPE.equals(type)) {
				throw new Error("not yet implemented for record source type: '" + type + "'");
			} else {
				throw new IllegalStateException("unknown record source type: '" + type + "'");
			}
			if (retVal == null) {
				String msg = "Missing persistent record source: " + rsId + "/" + type;
				logger.warning(msg);
			}
		}
		return retVal;
	}

	public ISerializableRecordSource findRecordSource(PersistableRecordSource prs) {
		PersistableRecordSource
		if (psr == null) {
			throw new IllegalArgumentException("null persistable record source");
		}
		findRecordSource
		assert rsId != PersistableRecordSource.NONPERSISTENT_ID;
		PersistableRecordSource retVal = null;
		if (PersistableSqlRecordSource.TYPE.equals(type)) {
			retVal = sqlRsController.find(rsId);
		} else if (PersistableFlatFileRecordSource.TYPE.equals(type)) {
			throw new Error("not yet implemented for record source type: '" + type + "'");
		} else if (PersistableXmlRecordSource.TYPE.equals(type)) {
			throw new Error("not yet implemented for record source type: '" + type + "'");
		} else {
			throw new IllegalStateException("unknown record source type: '" + type + "'");
		}
		if (retVal == null) {
			String msg = "Missing persistent record source: " + rsId + "/" + type;
			logger.warning(msg);
		}
		return retVal;
	}

}
