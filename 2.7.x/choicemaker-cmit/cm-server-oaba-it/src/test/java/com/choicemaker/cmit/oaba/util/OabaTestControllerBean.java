package com.choicemaker.cmit.oaba.util;

import java.util.Date;
import java.util.Random;
import java.util.UUID;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.choicemaker.cm.args.OabaLinkageType;
import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.core.base.Thresholds;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParametersController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersEntity;
import com.choicemaker.cmit.OabaTestController;
import com.choicemaker.cmit.utils.EntityManagerUtils;
import com.choicemaker.cmit.utils.FakePersistableRecordSource;
import com.choicemaker.cmit.utils.TestEntityCounts;

/**
 * An EJB used to test TransitivityJob beans within container-defined
 * transactions.
 * 
 * @author rphall
 */
@Stateless
public class OabaTestControllerBean implements OabaTestController {

	private static final String DEFAULT_MODEL_NAME = "FakeModelConfig";
	private static final String UNDERSCORE = "_";

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	@EJB
	private OabaParametersController paramsController;

	@EJB
	private ServerConfigurationController serverController;

	/**
	 * Synthesizes the name of a fake modelId configuration using the specified
	 * tag which may be null
	 */
	@Override
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

	@Override
	public Thresholds createRandomThresholds() {
		Random random = new Random(new Date().getTime());
		float low = random.nextFloat();
		float highRange = 1.0f - low;
		float f = random.nextFloat();
		float high = low + f * highRange;
		Thresholds retVal = new Thresholds(low, high);
		return retVal;
	}

	@Override
	public OabaParametersEntity createBatchParameters(String tag,
			TestEntityCounts te) {
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		Thresholds thresholds = createRandomThresholds();
		PersistableRecordSource stage = new FakePersistableRecordSource(tag);
		OabaLinkageType task = EntityManagerUtils.createRandomOabaTask();
		PersistableRecordSource master =
			EntityManagerUtils.createFakeMasterRecordSource(tag, task);
		OabaParametersEntity retVal =
			new OabaParametersEntity(createRandomModelConfigurationName(tag),
					thresholds.getDifferThreshold(),
					thresholds.getMatchThreshold(), stage, master, task);
		paramsController.save(retVal);
		te.add(retVal);
		return retVal;
	}

}
