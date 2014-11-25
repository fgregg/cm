package com.choicemaker.cmit.oaba;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationControllerBean;
import com.choicemaker.cmit.utils.EntityManagerUtils;
import com.choicemaker.cmit.utils.TestEntities;

/**
 * An EJB used to test TransitivityJob beans within container-defined
 * transactions; see {@link OabaJobController2} as an example of a similar
 * controller.
 * 
 * @author rphall
 */
@Stateless
public class TestController {

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	@EJB
	private ServerConfigurationController serverController;

	public ServerConfiguration getDefaultServerConfiguration() {
		String hostName = ServerConfigurationControllerBean.computeHostName();
		final boolean computeFallback = true;
		ServerConfiguration retVal =
			serverController.getDefaultConfiguration(hostName, computeFallback);
		assert retVal != null;
		assert retVal.getId() != ServerConfigurationControllerBean.INVALID_ID;
		return retVal;
	}
	
	public OabaParameters createPersistentOabaParameters(String tag, TestEntities te) {
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		return EntityManagerUtils.createPersistentOabaParameters(em, tag, te);
	}

	/**
	 * An externalId for the returned OabaJob is synthesized using the
	 * specified tag
	 */
	public OabaJob createPersistentOabaJobBean(String tag, TestEntities te) {
		return createPersistentOabaJobBean(te, EntityManagerUtils.createExternalId(tag));
	}

	/**
	 * The specified externalId is assigned without alteration to the returned
	 * OabaJob
	 */
	public OabaJob createPersistentOabaJobBean(TestEntities te, String extId) {
		ServerConfiguration sc = getDefaultServerConfiguration();
		return EntityManagerUtils.createPersistentOabaJobBean(sc, em, te, extId);
	}

	public void removeTestEntities(TestEntities te) {
		EntityManagerUtils.removeTestEntities(em, te);
	}

	public List<OabaParameters> findAllOabaParameters() {
		return EntityManagerUtils.findAllOabaParameters(em);
	}

	public List<OabaJobEntity> findAllOabaJobs() {
		return EntityManagerUtils.findAllOabaJobs(em);
	}

	public List<OabaProcessing> findAllOabaProcessing() {
		return EntityManagerUtils.findAllOabaProcessing(em);
	}

}
