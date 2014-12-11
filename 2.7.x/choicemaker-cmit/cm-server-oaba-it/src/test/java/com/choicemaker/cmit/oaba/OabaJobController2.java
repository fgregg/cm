package com.choicemaker.cmit.oaba;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationControllerBean;
import com.choicemaker.cmit.utils.EntityManagerUtils;
import com.choicemaker.cmit.utils.TestEntities;

/**
 * Effectively extends {@link OabaJobControllerBean} by adding some methods
 * useful for testing.
 * 
 * @author rphall
 */
@Stateless
public class OabaJobController2 {

	@EJB
	private OabaJobControllerBean oabaController;

	@EJB
	private ServerConfigurationController serverController;

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	public ServerConfiguration getDefaultServerConfiguration() {
		String hostName = ServerConfigurationControllerBean.computeHostName();
		final boolean computeFallback = true;
		ServerConfiguration retVal =
			serverController.getDefaultConfiguration(hostName, computeFallback);
		assert retVal != null;
		assert retVal.getId() != ServerConfigurationControllerBean.INVALID_ID;
		return retVal;
	}

	public OabaJob createEphemeralOabaJob(String tag, TestEntities te) {
		ServerConfiguration sc = getDefaultServerConfiguration();
		return EntityManagerUtils.createEphemeralOabaJob(sc, em, tag, te);
	}

	public OabaJob createEphemeralOabaJob(TestEntities te, String extId) {
		ServerConfiguration sc = getDefaultServerConfiguration();
		return EntityManagerUtils.createEphemeralOabaJob(sc, em, te, extId);
	}

	public OabaJob save(OabaJob batchJob) {
		return oabaController.save(batchJob);
	}

	public OabaJob find(long id) {
		return oabaController.find(id);
	}

	public List<OabaJob> findAll() {
		return oabaController.findAll();
	}

	public List<OabaParameters> findAllOabaParameters() {
		return EntityManagerUtils.findAllOabaParameters(em);
	}

	public List<OabaJob> findAllOabaJobs() {
		return findAll();
	}

	public void delete(OabaJob batchJob) {
		oabaController.delete(batchJob);
	}

	public void detach(OabaJob oabaJob) {
		oabaController.detach(oabaJob);
	}

}
