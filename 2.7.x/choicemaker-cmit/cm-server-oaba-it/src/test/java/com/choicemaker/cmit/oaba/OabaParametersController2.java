package com.choicemaker.cmit.oaba;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaTaskType;
import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.core.base.Thresholds;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersEntity;
import com.choicemaker.cmit.utils.EntityManagerUtils;
import com.choicemaker.cmit.utils.TestEntities;

/**
 * Effectively extends {@link OabaParametersControllerBean} by adding some methods
 * useful for testing.
 * An EJB used to test BatchParameter beans within container-defined
 * transactions; see {@link OabaJobController2} as an example of a similar
 * controller.
 * 
 * @author rphall
 */
@Stateless
public class OabaParametersController2 {

	private static final String DEFAULT_MODEL_NAME = "FakeModelConfig";
	private static final String UNDERSCORE = "_";
	
	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	@EJB
	OabaParametersControllerBean paramsController;

	@EJB
	OabaJobControllerBean jobController;

	/**
	 * Synthesizes the name of a fake modelId configuration using the specified
	 * tag which may be null
	 */
	public String createRandomModelConfigurationName(String tag) {
		if (tag == null) {
			tag = DEFAULT_MODEL_NAME;
		}
		tag = tag.trim();
		if (tag.isEmpty()) {
			tag = DEFAULT_MODEL_NAME;
		}
		StringBuilder sb = new StringBuilder(tag);
		if (!tag.endsWith(UNDERSCORE)) {
			sb.append(UNDERSCORE);
		}
		sb.append(UUID.randomUUID().toString());
		String retVal = sb.toString();
		return retVal;
	}

	public Thresholds createRandomThresholds() {
		Random random = new Random(new Date().getTime());
		float low = random.nextFloat();
		float highRange = 1.0f - low;
		float f = random.nextFloat();
		float high = low + f * highRange;
		Thresholds retVal = new Thresholds(low, high);
		return retVal;
	}

	public OabaParametersEntity createBatchParameters(String tag,
			TestEntities te) {
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		Thresholds thresholds = createRandomThresholds();
		PersistableRecordSource stage =
			EntityManagerUtils.createFakePersistableRecordSource(tag);
		OabaTaskType task = EntityManagerUtils.createRandomOabaTask();
		PersistableRecordSource master =
			EntityManagerUtils.createFakePersistableRecordSource(tag, task);
		OabaParametersEntity retVal =
			new OabaParametersEntity(createRandomModelConfigurationName(tag),
					thresholds.getDifferThreshold(),
					thresholds.getMatchThreshold(), stage, master, task);
		paramsController.save(retVal);
		te.add(retVal);
		return retVal;
	}

	public OabaParameters save(OabaParameters batchParameters) {
		return paramsController.save(batchParameters);
	}

	public OabaParameters find(long id) {
		return paramsController.find(id);
	}

	public List<OabaParameters> findAllBatchParameters() {
		return paramsController.findAllBatchParameters();
	}

	public List<OabaJob> findAllBatchJobs() {
		return jobController.findAll();
	}

	public void removeTestEntities(TestEntities te) {
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		te.removePersistentObjects(em);
	}

	public void delete(OabaParameters batchParameters) {
		paramsController.delete(batchParameters);
	}

	void delete(OabaParametersEntity batchParameters) {
		paramsController.delete(batchParameters);
	}

	public void detach(OabaParameters oabaParameters) {
		paramsController.detach(oabaParameters);
	}

}
