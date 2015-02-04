package com.choicemaker.cmit.utils;

import static com.choicemaker.cm.args.WellKnownGraphPropertyNames.GPN_NAMES;

import java.util.Random;
import java.util.UUID;

import com.choicemaker.cm.args.AnalysisResultFormat;
import com.choicemaker.cm.args.OabaLinkageType;
import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.core.base.Thresholds;

public class EntityManagerUtils {

	private EntityManagerUtils() {
	}

	static final String DEFAULT_RECORDSOURCE_TAG = "Random record source";
	static final String TAG_DELIMITER = ": ";
	static final String PREFIX_FAKE_RECORDSOURCE = "FAKE_RECORDSOURCE_";
	static final String PREFIX_FAKE_RECORDSOURCE_FILE =
		"FAKE_RECORDSOURCE_FILE_";

	private static final String DEFAULT_EXTERNALID_TAG = "Random external id";
	private static final String DEFAULT_MODEL_NAME = "FakeModelConfig";
	private static final String COLON = ":";
	private static final String SPACE = " ";
	private static final String UNDERSCORE = "_";

	private static final Random random = new Random();

	/** Synthesizes an externalId using the specified tag which may be null */
	public static String createExternalId(String tag) {
		if (tag == null) {
			tag = DEFAULT_EXTERNALID_TAG;
		}
		tag = tag.trim();
		if (tag.isEmpty()) {
			tag = DEFAULT_EXTERNALID_TAG;
		}
		StringBuilder sb = new StringBuilder(tag);
		if (tag.endsWith(COLON)) {
			sb.append(SPACE);
		} else {
			sb.append(TAG_DELIMITER);
		}
		sb.append(UUID.randomUUID().toString());
		String retVal = sb.toString();
		return retVal;
	}

	/**
	 * Synthesizes the name of a fake modelId configuration using the specified
	 * tag which may be null
	 */
	public static String createRandomModelConfigurationName(String tag) {
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

	public static Thresholds createRandomThresholds() {
		float low = random.nextFloat();
		float highRange = 1.0f - low;
		float f = random.nextFloat();
		float high = low + f * highRange;
		Thresholds retVal = new Thresholds(low, high);
		return retVal;
	}

	public static OabaLinkageType createRandomOabaTask() {
		OabaLinkageType retVal;
		int i = random.nextInt(3);
		switch (i) {
		case 0:
			retVal = OabaLinkageType.STAGING_DEDUPLICATION;
			break;
		case 1:
			retVal = OabaLinkageType.STAGING_TO_MASTER_LINKAGE;
			break;
		case 2:
			retVal = OabaLinkageType.MASTER_TO_MASTER_LINKAGE;
			break;
		default:
			throw new Error("not possible");
		}
		return retVal;
	}

	public static AnalysisResultFormat createRandomAnalysisFormat() {
		AnalysisResultFormat[] values = AnalysisResultFormat.values();
		int i = random.nextInt(values.length);
		AnalysisResultFormat retVal = values[i];
		return retVal;
	}

	public static String createRandomGraphName() {
		int range = GPN_NAMES.size();
		int i = random.nextInt(range);
		String retVal = GPN_NAMES.get(i);
		return retVal;
	}

	public static PersistableRecordSource createFakeMasterRecordSource(
			String tag, OabaLinkageType task) {
		PersistableRecordSource retVal;
		switch (task) {
		case STAGING_DEDUPLICATION:
			retVal = null;
			break;
		case STAGING_TO_MASTER_LINKAGE:
		case MASTER_TO_MASTER_LINKAGE:
		case TRANSITIVITY_ANALYSIS:
			retVal = new FakePersistableRecordSource(tag);
			break;
		default:
			throw new Error("invalid task type: " + task);
		}
		return retVal;
	}

}
