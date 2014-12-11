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
package com.choicemaker.demo.persist0;

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
public class CMP_Feature implements Serializable {

	protected static final String INVALID_MISSING = "INVALID: MISSING";

	public static final float DEFAULT_WEIGHT = 1.0f;

	public static final float WEIGHT_NOT_AVAILABLE = -1.0f;

	private static final long serialVersionUID = 271L;

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

	protected CMP_Feature() {
	}

	// public CMP_Feature(final ImmutableProbabilityModel ipm, final int idx) {
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
	// this.machineLearning = CMP_ModelBean.getMachineLearningTypeName(ml);
	// this.featureType =
	// CMP_DefaultFeatureType.fromDecision(isClue, d).toString();
	// if (ml instanceof MaximumEntropy) {
	// final MaximumEntropy me = (MaximumEntropy) ml;
	// final float[] weights = me.getWeights();
	// assert weights.length == clueCount;
	// this.weight = weights[idx];
	// } else {
	// this.weight = WEIGHT_NOT_AVAILABLE;
	// }
	// }

	CMP_Feature(String mlType, final ClueDesc[] clueDescriptors,
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
			CMP_DefaultFeatureType.fromDecision(isClue, d).toString();
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
		CMP_Feature other = (CMP_Feature) obj;
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
		return "CMP_Feature [featureName=" + featureName + ", featureType="
				+ featureType + ", weight=" + weight + "]";
	}

}
