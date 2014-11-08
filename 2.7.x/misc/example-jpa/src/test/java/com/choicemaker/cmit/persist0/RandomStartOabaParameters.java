package com.choicemaker.cmit.persist0;

import java.util.Date;
import java.util.Random;

import com.choicemaker.cm.core.SerializableRecordSource;

public class RandomStartOabaParameters {

	private static Random random = new Random(new Date().getTime());

	private static float getRandomThreshold() {
		return random.nextFloat();
	}

	private static float getRandomThreshold(float lowerBound) {
		if (lowerBound < 0 || lowerBound >= 1) {
			throw new IllegalArgumentException("Illegal lower bound: "
					+ lowerBound);
		}
		float range = 1f - lowerBound;
		float f = random.nextFloat();
		float offset = range * f;
		float retVal = lowerBound + offset;
		return retVal;
	}

	public static enum PARAMETER_OPTION {
		OPTION_6, OPTION_8, OPTION_9
	}

	private long jobID;
	public final String externalID;
	public final SerializableRecordSource staging;
	public final SerializableRecordSource master;
	public final float lowThreshold;
	public final float highThreshold;
	public final String stageModelName;
	public final String masterModelName;
	public final int maxSingle;
	public final boolean runTransitivity;

	public RandomStartOabaParameters(Object o) {
		this(PARAMETER_OPTION.OPTION_9, o);
	}

	public RandomStartOabaParameters(PARAMETER_OPTION option, Object o) {
		if (option == null || o == null) {
			throw new IllegalArgumentException("null option or object");
		}
		switch (option) {
		case OPTION_6:
			this.externalID = "EXT_ID: " + o.toString();
			this.staging = null;
			this.master = null;
			this.lowThreshold = getRandomThreshold();
			this.highThreshold = getRandomThreshold(lowThreshold);
			this.stageModelName = "STAGE: " + o.toString();
			this.masterModelName = null;
			this.maxSingle = random.nextInt(Integer.MAX_VALUE);
			this.runTransitivity = false;
			break;
		case OPTION_8:
			this.externalID = "EXT_ID: " + o.toString();
			this.staging = null;
			this.master = null;
			this.lowThreshold = getRandomThreshold();
			this.highThreshold = getRandomThreshold(lowThreshold);
			this.stageModelName = "STAGE: " + o.toString();
			this.masterModelName = "MASTER: " + o.toString();
			this.maxSingle = random.nextInt(Integer.MAX_VALUE);
			this.runTransitivity = false;
			break;
		case OPTION_9:
			this.externalID = "EXT_ID: " + o.toString();
			this.staging = null;
			this.master = null;
			this.lowThreshold = getRandomThreshold();
			this.highThreshold = getRandomThreshold(lowThreshold);
			this.stageModelName = "STAGE: " + o.toString();
			this.masterModelName = "MASTER: " + o.toString();
			this.maxSingle = random.nextInt(Integer.MAX_VALUE);
			this.runTransitivity = random.nextBoolean();
			break;
		default:
			throw new Error("Unexpected option: " + option);
		}
	}

	long getJobID() {
		return jobID;
	}

	void setJobID(long jobID) {
		this.jobID = jobID;
	}

}