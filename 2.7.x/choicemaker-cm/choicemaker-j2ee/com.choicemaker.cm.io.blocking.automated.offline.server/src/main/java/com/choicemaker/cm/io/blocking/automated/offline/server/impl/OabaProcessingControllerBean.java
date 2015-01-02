/*
 * Copyright (c) 2001, 2009 ChoiceMaker Technologies, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License
 * v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     ChoiceMaker Technologies, Inc. - initial API and implementation
 */
package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEventLog;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;

/**
 * This object contains method to get JMS and EJB objects from the J2EE server.
 *
 * @author pcheung
 * @author rphall (migration to EJB3)
 *
 */
@Stateless
public class OabaProcessingControllerBean {

//	static final long serialVersionUID = 271;

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	public List<OabaProcessingLogEntry> findProcessingLogEntriesByJobId(long id) {
		Query query =
			em.createNamedQuery(OabaProcessingJPA.QN_OABAPROCESSING_FIND_BY_JOBID);
		query.setParameter(
				OabaProcessingJPA.PN_OABAPROCESSING_FIND_BY_JOBID_JOBID, id);
		@SuppressWarnings("unchecked")
		List<OabaProcessingLogEntry> entries = query.getResultList();
		return entries;
	}

	public OabaEventLog getProcessingLog(OabaJob job) {
		return new OabaJobEventLog(em,job);
	}

}
