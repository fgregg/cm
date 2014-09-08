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
package com.choicemaker.cm.io.blocking.automated.offline.server;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.choicemaker.cm.core.ClueDesc;
import com.choicemaker.cm.core.Decision;

/**
 * A model feature is a clue or a rule. Currently, this class is very specific
 * to the Maximum Entropy algorithm implemented by
 * {@link com.choicemaker.cm.ml.me.base.MaximumEntropy}, but the class contains
 * a table discriminator, the <code>ML_TYPE</code> column, so that it can be
 * extended to other algorithms and implementations in the future. For example,
 * other machine learning algorithms might use different type of features
 * besides the types currently enumerated.
 * 
 * @author rphall
 */
@Embeddable
public class CM_Feature implements Serializable {

	protected static final String INVALID_MISSING = "INVALID: MISSING";

	public static final float DEFAULT_WEIGHT = 1.0f;

	public static final float WEIGHT_NOT_AVAILABLE = -1.0f;

	private static final long serialVersionUID = 271L;

	public static enum DefaultFeatureType {
		CLUE_MATCH(true, "match"), CLUE_HOLD(true, "hold"), CLUE_DIFFER(true,
				"differ"), RULE_NONE(false, "none"),
		RULE_MATCH(false, "match"), RULE_HOLD(false, "hold"), RULE_DIFFER(
				false, "differ"), RULE_NOMATCH(false, "nomatch"), RULE_NOHOLD(
				false, "nohold"), RULE_NODIFFER(false, "nodiffer");
		static String CLUE = "clue";
		static String RULE = "rule";
		private final boolean isClue;
		private final String nickname;

		DefaultFeatureType(boolean isClue, String nick) {
			this.isClue = isClue;
			this.nickname = nick;
			assert nickname.equals(nick.toLowerCase());
		}

		public boolean isClue() {
			return isClue;
		}

		public boolean isRule() {
			return !isClue;
		}

		public String getNickName() {
			return nickname;
		}

		public String toString() {
			String type = isClue ? CLUE : RULE;
			String retVal = type + nickname;
			assert retVal.equals(retVal.toLowerCase());
			return retVal;
		}

		private static String toName(boolean isClue, String s1) {
			String s0 = isClue ? CLUE : RULE;
			return toName(s0, s1);
		}

		private static String toName(String s0, String s1) {
			return s0.toUpperCase() + "_" + s1.toUpperCase();
		}

		static DefaultFeatureType fromFlaggedNickname(boolean isClue, String s) {
			String enumName = toName(isClue, s);
			DefaultFeatureType retVal = DefaultFeatureType.valueOf(enumName);
			return retVal;
		}

		static DefaultFeatureType fromString(String s) {
			if (s == null) {
				throw new IllegalArgumentException("null argument");
			}
			s = s.trim().toLowerCase();
			if (s.isEmpty()) {
				throw new IllegalArgumentException("blank argument");
			}
			String[] components = s.split(":");
			if (components.length != 2) {
				throw new IllegalArgumentException("invalid form: '" + s + "'");
			}
			String enumName = toName(components[0], components[1]);
			DefaultFeatureType retVal = DefaultFeatureType.valueOf(enumName);
			return retVal;
		}

		static DefaultFeatureType fromDecision(boolean isClue, Decision d) {
			if (d == null) {
				throw new IllegalArgumentException("null argument");
			}
			DefaultFeatureType retVal =
				fromFlaggedNickname(isClue, d.getName());
			return retVal;
		}
	}

	@Column(name = "NAME")
	private String featureName;

	@Column(name = "ML_TYPE")
	private String machineLearning;

	@Column(name = "FEATURE_TYPE")
	private String featureType;

	// @Column(name = "NOTE")
	// private String note;
	//
	// @Column(name = "REPORTED")
	// private boolean isReport;

	@Column(name = "WEIGHT")
	private float weight;

	// -- Construction

	protected CM_Feature() {
	}

	// public CM_Feature(final ImmutableProbabilityModel ipm, final int idx) {
	// if (ipm == null) {
	// throw new IllegalArgumentException("null model");
	// }
	// if (idx < 0) {
	// throw new IllegalArgumentException("negative index: " + idx);
	// }
	// final ClueSet cs = ipm.getClueSet();
	// final ClueDesc[] clueDescriptors = cs.getClueDesc();
	// final int clueCount = clueDescriptors.length;
	// if (idx >= clueCount) {
	// String msg =
	// "index '" + idx + "' larger than the maximum clue index ("
	// + (clueCount - 1) + ")";
	// throw new IllegalArgumentException(msg);
	// }
	// final ClueDesc cd = clueDescriptors[idx];
	// final MachineLearner ml = ipm.getMachineLearner();
	// final boolean isClue = !cd.rule;
	// final Decision d = cd.getDecision();
	// this.featureName = cd.getName();
	// this.machineLearning = CM_ModelBean.getMachineLearningTypeName(ml);
	// this.featureType =
	// DefaultFeatureType.fromDecision(isClue, d).toString();
	// if (ml instanceof MaximumEntropy) {
	// final MaximumEntropy me = (MaximumEntropy) ml;
	// final float[] weights = me.getWeights();
	// assert weights.length == clueCount;
	// this.weight = weights[idx];
	// } else {
	// this.weight = WEIGHT_NOT_AVAILABLE;
	// }
	// }

	CM_Feature(String mlType, final ClueDesc[] clueDescriptors,
			final float[] weights, final int idx) {
		if (mlType == null) {
			throw new IllegalArgumentException("null machine learning type");
		}
		mlType = mlType.trim();
		if (mlType.isEmpty()) {
			throw new IllegalArgumentException("blank machine learning type");
		}
		if (clueDescriptors == null) {
			throw new IllegalArgumentException("null clue descriptors");
		}
		if (weights != null && weights.length != clueDescriptors.length) {
			throw new IllegalArgumentException("weights (" + weights.length
					+ ") out of synch with clue descriptors ("
					+ clueDescriptors.length + ")");
		}
		if (idx < 0) {
			throw new IllegalArgumentException("negative index: " + idx);
		}
		if (idx >= clueDescriptors.length) {
			String msg =
				"index '" + idx + "' larger than the maximum clue index ("
						+ (clueDescriptors.length - 1) + ")";
			throw new IllegalArgumentException(msg);
		}

		final ClueDesc cd = clueDescriptors[idx];
		this.featureName = cd.getName();

		this.machineLearning = mlType;

		final boolean isClue = !cd.rule;
		final Decision d = cd.getDecision();
		this.featureType =
			DefaultFeatureType.fromDecision(isClue, d).toString();
		if (weights != null) {
			this.weight = weights[idx];
		} else {
			this.weight = WEIGHT_NOT_AVAILABLE;
		}
	}

	public String getFeatureName() {
		return featureName;
	}

	public String getMachineLearning() {
		return machineLearning;
	}

	public String getFeatureType() {
		return featureType;
	}

	public float getWeight() {
		return weight;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result =
			prime * result
					+ ((featureName == null) ? 0 : featureName.hashCode());
		result =
			prime * result
					+ ((featureType == null) ? 0 : featureType.hashCode());
		result =
			prime
					* result
					+ ((machineLearning == null) ? 0 : machineLearning
							.hashCode());
		result = prime * result + Float.floatToIntBits(weight);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CM_Feature other = (CM_Feature) obj;
		if (featureName == null) {
			if (other.featureName != null) {
				return false;
			}
		} else if (!featureName.equals(other.featureName)) {
			return false;
		}
		if (featureType == null) {
			if (other.featureType != null) {
				return false;
			}
		} else if (!featureType.equals(other.featureType)) {
			return false;
		}
		if (machineLearning == null) {
			if (other.machineLearning != null) {
				return false;
			}
		} else if (!machineLearning.equals(other.machineLearning)) {
			return false;
		}
		if (Float.floatToIntBits(weight) != Float.floatToIntBits(other.weight)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "CM_Feature [featureName=" + featureName + ", featureType="
				+ featureType + ", weight=" + weight + "]";
	}

}
