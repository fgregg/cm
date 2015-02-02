package com.choicemaker.cmit.trans.util;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.choicemaker.cm.args.AnalysisResultFormat;
import com.choicemaker.cm.args.OabaLinkageType;
import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.args.TransitivityParameters;
import com.choicemaker.cm.args.WellKnownGraphPropertyNames;
import com.choicemaker.cm.core.base.Thresholds;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersController;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationControllerBean;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityParametersController;
import com.choicemaker.cm.transitivity.server.impl.TransitivityParametersEntity;
import com.choicemaker.cmit.TransitivityTestController;
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
public class TransitivityTestControllerBean implements
		TransitivityTestController {

	private static final String DEFAULT_MODEL_NAME = "FakeModelConfig";

	private static final String UNDERSCORE = "_";

	private final Random random = new Random();

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	@EJB
	private OabaParametersController oabaParamsController;

	@EJB
	private TransitivityParametersController transParamsController;

	@EJB
	private ServerConfigurationController serverController;

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

	public TransitivityParameters createTransitivityParameters(
			String tag, TestEntityCounts te) {
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		Thresholds thresholds = createRandomThresholds();
		PersistableRecordSource stage = new FakePersistableRecordSource(tag);
		OabaLinkageType task = EntityManagerUtils.createRandomOabaTask();
		PersistableRecordSource master =
			EntityManagerUtils.createFakeMasterRecordSource(tag, task);
		AnalysisResultFormat format = createRandomAnalysisResultFormat();
		String graphPropertyName = createRandomGraphPropertyName();
		TransitivityParameters retVal =
			new TransitivityParametersEntity(
					createRandomModelConfigurationName(tag),
					thresholds.getDifferThreshold(),
					thresholds.getMatchThreshold(), stage, master, format,
					graphPropertyName);
		retVal = transParamsController.save(retVal);
		te.add((TransitivityParameters) retVal);
		return retVal;
	}

	private String createRandomGraphPropertyName() {
		List<String> values = WellKnownGraphPropertyNames.GPN_NAMES;
		int range = values.size();
		int i = random.nextInt(range);
		String retVal = values.get(i);
		return retVal;
	}

	private AnalysisResultFormat createRandomAnalysisResultFormat() {
		AnalysisResultFormat[] values = AnalysisResultFormat.values();
		int range = values.length;
		int i = random.nextInt(range);
		AnalysisResultFormat retVal = values[i];
		return retVal;
	}

	public ServerConfiguration getDefaultServerConfiguration() {
		String hostName = ServerConfigurationControllerBean.computeHostName();
		final boolean computeFallback = true;
		ServerConfiguration retVal =
			serverController.getDefaultConfiguration(hostName, computeFallback);
		assert retVal != null;
		assert retVal.getId() != ServerConfigurationControllerBean.INVALID_ID;
		return retVal;
	}

}
