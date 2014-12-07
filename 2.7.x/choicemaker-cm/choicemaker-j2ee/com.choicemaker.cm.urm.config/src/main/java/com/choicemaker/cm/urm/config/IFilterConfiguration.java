/*
 * Created on Dec 19, 2006
 *
 */
package com.choicemaker.cm.urm.config;

import java.io.Serializable;

/**
 * @author emoussikaev
 */
public interface IFilterConfiguration extends Serializable {

	public abstract Integer getBatchSize();

	public abstract Float getDefaultPrefilterFromPercentage();

	public abstract Float getDefaultPrefilterToPercentage();

	public abstract Float getDefaultPostfilterFromPercentage();

	public abstract Float getDefaultPostfilterToPercentage();

	public abstract Integer getDefaultPairSamplerSize();

	public abstract Boolean getUseDefaultPrefilter();

	public abstract Boolean getUseDefaultPostfilter();

	public abstract Boolean getUseDefaultPairSampler();

	public abstract void setBatchSize(Integer integer);

	public abstract void setDefaultPrefilterFromPercentage(Float float1);

	public abstract void setDefaultPrefilterToPercentage(Float float1);

	public abstract void setDefaultPostfilterFromPercentage(Float float1);

	public abstract void setDefaultPostfilterToPercentage(Float float1);

	public abstract void setDefaultPairSamplerSize(Integer integer);

	public abstract void setUseDefaultPrefilter(Boolean boolean1);

	public abstract void setUseDefaultPostfilter(Boolean boolean1);

	public abstract void setUseDefaultPairSampler(Boolean boolean1);
}
