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
package com.choicemaker.cm.core.install;

import java.util.List;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.MachineLearner;
import com.choicemaker.cm.core.configure.ChoiceMakerConfiguration;
import com.choicemaker.cm.core.configure.ChoiceMakerConfigurator;
import com.choicemaker.cm.core.configure.MachineLearnerPersistence;
import com.choicemaker.cm.core.configure.ProbabilityModelPersistence;

public final class InstalledChoiceMakerConfiguration implements ChoiceMakerConfiguration {

	private static final Logger logger = Logger.getLogger(InstalledChoiceMakerConfiguration.class
			.getName());

	/**
	 * The default instance is an that doesn't actually do anything.
	 */
	static final ChoiceMakerConfiguration getDefaultInstance() {
		// FIXME Auto-generated method stub
		return new ChoiceMakerConfiguration() {

			public ProbabilityModelPersistence getModelPersistence(
					ImmutableProbabilityModel model) {
				return null;
			}

			public MachineLearnerPersistence getMachineLearnerPersistence(
					MachineLearner model) {
				return null;
			}

			public ClassLoader getClassLoader() {
				return null;
			}

			public List getProbabilityModelConfigurations() {
				return null;
			}

			public boolean isValid() {
				return false;
			}

			public String getClassPath() {
				return null;
			}

			public String getReloadClassPath() {
				return null;
			}

			public String getJavaDocClasspath() {
				return null;
			}

			public String toXml() {
				return null;
			}

			public String getFileName() {
				return null;
			}

			public ClassLoader getRmiClassLoader() {
				return null;
			}

		};
	}

	/** The singleton instance of this manager */
	private static ChoiceMakerConfiguration singleton = new InstalledChoiceMakerConfiguration();

	/** A method that returns the manager singleton */
	public static ChoiceMakerConfiguration getInstance() {
		assert singleton != null;
		return singleton;
	}

	private ChoiceMakerConfiguration delegate;

	/**
	 * Returns a delegate if one has been configured, or if one hasn't been configured,
	 * sets the delegate to a {@link #getDefaultInstance() default type} and returns it.
	 * Delegates are configured via an instance of {@link ChoiceMakerConfigurator}.
	 *
	 * @throws IllegalStateException
	 *             if a delegate does not exist and a default can not be configured.
	 */
	ChoiceMakerConfiguration getDelegate() {
		if (delegate == null) {
			String msgPrefix = "Installing a default configuration: ";
					logger.info(msgPrefix
							+ getDefaultInstance().getClass().getName());
					set(getDefaultInstance());
		}
		assert delegate != null;
		return delegate;
	}

	public ProbabilityModelPersistence getModelPersistence(
			ImmutableProbabilityModel model) {
		return getDelegate().getModelPersistence(model);
	}

	public MachineLearnerPersistence getMachineLearnerPersistence(MachineLearner machineLearner) {
		return getDelegate().getMachineLearnerPersistence(machineLearner);
	}

	public ClassLoader getClassLoader() {
		return getDelegate().getClassLoader();
	}

	public ClassLoader getRmiClassLoader() {
		return getDelegate().getRmiClassLoader();
	}

	public List getProbabilityModelConfigurations() {
		return getDelegate().getProbabilityModelConfigurations();
	}

	public boolean isValid() {
		return getDelegate().isValid();
	}

	public String getClassPath() {
		return getDelegate().getClassPath();
	}

	public String getReloadClassPath() {
		return getDelegate().getReloadClassPath();
	}

	public String getJavaDocClasspath() {
		return getDelegate().getJavaDocClasspath();
	}

	public String toXml() {
		return getDelegate().toXml();
	}

	public String getFileName() {
		return getDelegate().getFileName();
	}

	/** For testing only; otherwise treat as private */
	InstalledChoiceMakerConfiguration() {
	}

	/**
	 * Sets the manager delegate. Used only by the {@link InstallableChoiceMakerConfigurator}
	 * class.
	 *
	 * @throws IllegalArgumentException
	 *             if the delegate can not be updated.
	 * */
	void set(ChoiceMakerConfiguration delegate) {
		if (delegate == null) {
			throw new IllegalArgumentException("null delegate");
		}
		this.delegate = delegate;
	}

}
