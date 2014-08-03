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
package com.choicemaker.cm.urm.ejb;

/**
 * @author rphall
 *
 */
public interface IMrpsRequestConfiguration {
	
	/**
	 * Name of the property for batch size
	 * @since 2.5.22.5
	 */
	public static final String PN_BATCH_SIZE = "mrpsRequest.batchSize";

	/**
	 * Name of the property that controls whether to use a DefaultMatchRecord2Filter
	 * @since 2.5.22.5
	 * @see com.choicemaker.cm.analyzer.filter.DefaultMatchRecord2Filter
	 */
	public static final String PN_USE_DEFAULT_PREFILTER =
		"mrpsRequest.useDefaultMatchRecord2Filter";

	/**
	 * Name of the property for the default filter "fromPercentage"
	 * @since 2.5.22.5
	 */
	public static final String PN_DEFAULT_PREFILTER_FROM_PERCENTAGE =
		"mrpsRequest.defaultMatchRecord2FilterFromPercentage";

	/**
	 * Name of the property for the default filter "toPercentage"
	 * @since 2.5.22.5
	 */
	public static final String PN_DEFAULT_PREFILTER_TO_PERCENTAGE =
		"mrpsRequest.defaultMatchRecord2FilterToPercentage";

	/**
	 * Name of the property that controls whether to use a DefaultPairFilter
	 * @since 2.5.22.5
	 * @see com.choicemaker.cm.analyzer.filter.DefaultPairFilter
	 */
	public static final String PN_USE_DEFAULT_POSTFILTER =
		"mrpsRequest.useDefaultPairFilter";

	/**
	 * Name of the property for the default filter "fromPercentage"
	 * @since 2.5.22.5
	 */
	public static final String PN_DEFAULT_POSTFILTER_FROM_PERCENTAGE =
		"mrpsRequest.defaultPairFilterFromPercentage";

	/**
	 * Name of the property for the default filter "toPercentage"
	 * @since 2.5.22.5
	 */
	public static final String PN_DEFAULT_POSTFILTER_TO_PERCENTAGE =
		"mrpsRequest.defaultPairFilterToPercentage";

	/**
	 * Name of the property that controls whether to use a DefaultPairSampler
	 * @since 2.5.22.5
	 * @see com.choicemaker.cm.analyzer.sampler.DefaultPairSampler
	 */
	public static final String PN_USE_DEFAULT_PAIR_SAMPLER =
		"mrpsRequest.useDefaultPairSampler";

	/**
	 * Name of the property for sample size 
	 * @since 2.5.22.5
	 */
	public static final String PN_DEFAULT_PAIR_SAMPLER_SIZE =
		"mrpsRequest.defaultPairSamplerSize";

}
