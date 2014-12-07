/*
 * Created on Dec 1, 2006
 *
 */
package com.choicemaker.cm.urm.config;

import java.util.Hashtable;

/**
 * @author emoussikaev
 *
 */
public class FilterConfiguration implements IFilterConfiguration {

	static final long serialVersionUID = 271L;

	protected Hashtable props = new Hashtable();

	/**
	 * Name of the property for batch size
	 * 
	 * @since 2.5.22.5
	 */
	protected static final String PN_BATCH_SIZE = "BatchSize";

	/**
	 * Name of the property that controls whether to use a
	 * DefaultMatchRecord2Filter
	 * 
	 * @since 2.5.22.5
	 * @see com.choicemaker.cm.analyzer.filter.DefaultMatchRecord2Filter
	 */
	protected static final String PN_USE_DEFAULT_PREFILTER =
		"UseDefaultPrefilter";

	/**
	 * Name of the property for the default filter "fromPercentage"
	 * 
	 * @since 2.5.22.5
	 */
	protected static final String PN_DEFAULT_PREFILTER_FROM_PERCENTAGE =
		"DefaultPrefilterFromPercentage";

	/**
	 * Name of the property for the default filter "toPercentage"
	 * 
	 * @since 2.5.22.5
	 */
	protected static final String PN_DEFAULT_PREFILTER_TO_PERCENTAGE =
		"DefaultPrefilterToPercentage";

	/**
	 * Name of the property that controls whether to use a DefaultPairFilter
	 * 
	 * @since 2.5.22.5
	 * @see com.choicemaker.cm.analyzer.filter.DefaultPairFilter
	 */
	protected static final String PN_USE_DEFAULT_POSTFILTER =
		"UseDefaultPostfilter";

	/**
	 * Name of the property for the default filter "fromPercentage"
	 * 
	 * @since 2.5.22.5
	 */
	protected static final String PN_DEFAULT_POSTFILTER_FROM_PERCENTAGE =
		"DefaultPostfilterFromPercentage";

	/**
	 * Name of the property for the default filter "toPercentage"
	 * 
	 * @since 2.5.22.5
	 */
	protected static final String PN_DEFAULT_POSTFILTER_TO_PERCENTAGE =
		"DefaultPostfilterToPercentage";

	/**
	 * Name of the property that controls whether to use a DefaultPairSampler
	 * 
	 * @since 2.5.22.5
	 * @see com.choicemaker.cm.analyzer.sampler.DefaultPairSampler
	 */
	protected static final String PN_USE_DEFAULT_PAIR_SAMPLER =
		"UseDefaultPairSampler";

	/**
	 * Name of the property for sample size
	 * 
	 * @since 2.5.22.5
	 */
	protected static final String PN_DEFAULT_PAIR_SAMPLER_SIZE =
		"DefaultPairSamplerSize";

	public FilterConfiguration() {
	}

	public FilterConfiguration(FilterConfiguration cmConf1) {
		if (cmConf1 != null)
			this.props = (Hashtable) cmConf1.props.clone();
	}

	public Integer getBatchSize() {
		return (Integer) props.get(PN_BATCH_SIZE);
	}

	public Float getDefaultPrefilterFromPercentage() {
		return (Float) props.get(PN_DEFAULT_PREFILTER_FROM_PERCENTAGE);
	}

	public Float getDefaultPrefilterToPercentage() {
		return (Float) props.get(PN_DEFAULT_PREFILTER_TO_PERCENTAGE);
	}

	public Float getDefaultPostfilterFromPercentage() {
		return (Float) props.get(PN_DEFAULT_POSTFILTER_FROM_PERCENTAGE);
	}

	public Float getDefaultPostfilterToPercentage() {
		return (Float) props.get(PN_DEFAULT_POSTFILTER_TO_PERCENTAGE);
	}

	public Integer getDefaultPairSamplerSize() {
		return (Integer) props.get(PN_DEFAULT_PAIR_SAMPLER_SIZE);
	}

	public Boolean getUseDefaultPrefilter() {
		return (Boolean) props.get(PN_USE_DEFAULT_PREFILTER);
	}

	public Boolean getUseDefaultPostfilter() {
		return (Boolean) props.get(PN_USE_DEFAULT_POSTFILTER);
	}

	public Boolean getUseDefaultPairSampler() {
		return (Boolean) props.get(PN_USE_DEFAULT_PAIR_SAMPLER);
	}

	public void setBatchSize(Integer integer) {
		props.put(PN_BATCH_SIZE, integer);
	}

	public void setDefaultPrefilterFromPercentage(Float integer) {
		props.put(PN_DEFAULT_PREFILTER_FROM_PERCENTAGE, integer);
	}

	public void setDefaultPrefilterToPercentage(Float integer) {
		props.put(PN_DEFAULT_PREFILTER_TO_PERCENTAGE, integer);
	}

	public void setDefaultPostfilterFromPercentage(Float integer) {
		props.put(PN_DEFAULT_POSTFILTER_FROM_PERCENTAGE, integer);
	}

	public void setDefaultPostfilterToPercentage(Float integer) {
		props.put(PN_DEFAULT_POSTFILTER_TO_PERCENTAGE, integer);
	}

	public void setDefaultPairSamplerSize(Integer integer) {
		props.put(PN_DEFAULT_PAIR_SAMPLER_SIZE, integer);
	}

	public void setUseDefaultPrefilter(Boolean boolean1) {
		props.put(PN_USE_DEFAULT_PREFILTER, boolean1);
	}

	public void setUseDefaultPostfilter(Boolean boolean1) {
		props.put(PN_USE_DEFAULT_POSTFILTER, boolean1);
	}

	public void setUseDefaultPairSampler(Boolean boolean1) {
		props.put(PN_USE_DEFAULT_PAIR_SAMPLER, boolean1);
	}

}
