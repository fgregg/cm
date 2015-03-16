package com.choicemaker.cm.transitivity.server.impl;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationControllerBean;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityConfigurationController;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJobController;

@Stateless
public class TransitivityConfigurationControllerBean
	extends ServerConfigurationControllerBean
	implements
	TransitivityConfigurationController {

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	@EJB
	private TransitivityJobController jobController;

	@Override
	public ServerConfiguration findConfigurationByTransitivityJobId(
			long jobId) {
		ServerConfiguration retVal = null;
		BatchJob batchJob = jobController.findTransitivityJob(jobId);
		if (batchJob != null) {
			long serverId = batchJob.getServerId();
			retVal = findServerConfiguration(serverId);
		}
		return retVal;
	}

}
