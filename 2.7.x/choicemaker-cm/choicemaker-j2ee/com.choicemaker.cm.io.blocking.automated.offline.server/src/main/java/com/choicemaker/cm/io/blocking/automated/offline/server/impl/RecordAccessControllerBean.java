package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.choicemaker.cm.args.RecordAccess;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordAccessController;

@Stateless
public class RecordAccessControllerBean implements RecordAccessController {

	private static final Logger logger = Logger
			.getLogger(RecordAccessControllerBean.class.getName());

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	@EJB
	private RecordAccessController recordAccessController;

	@Override
	public RecordAccess save(final RecordAccess ra) {
		logger.fine("Saving " + ra);
		if (ra == null) {
			throw new IllegalArgumentException("null RecordAccess instance");
		}
		RecordAccess retVal = null;
		retVal = recordAccessController.save(ra);
		logger.fine("Saved " + retVal);
		return retVal;
	}

	@Override
	public RecordAccess find(Long id) {
		RecordAccess retVal = null;
		if (id != null) {
			retVal = recordAccessController.find(id);
			if (retVal == null) {
				logger.warning("Record source " + id + " not found");
			}
		} else {
			logger.warning("RecordAccessControllerBean.find: null id");
		}
		return retVal;
	}

}
