/*
 * Created on Dec 19, 2006
 *
 */
package com.choicemaker.cm.args;

import java.io.Serializable;

/**
 * @author emoussikaev
 */
public interface IFilterConfiguration extends Serializable {

	int getBatchSize();

	float getDefaultPrefilterFromPercentage();

	float getDefaultPrefilterToPercentage();

	float getDefaultPostfilterFromPercentage();

	float getDefaultPostfilterToPercentage();

	int getDefaultPairSamplerSize();

	boolean getUseDefaultPrefilter();

	boolean getUseDefaultPostfilter();

	boolean getUseDefaultPairSampler();

}
