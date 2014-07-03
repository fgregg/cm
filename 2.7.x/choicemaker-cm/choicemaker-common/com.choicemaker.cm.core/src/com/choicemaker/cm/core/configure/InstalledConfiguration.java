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
package com.choicemaker.cm.core.configure;

import java.io.File;
import java.util.List;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.MachineLearner;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.compiler.ICompiler;

final class InstalledConfiguration implements
		ChoiceMakerConfiguration {

//	private static final Logger logger = Logger
//			.getLogger(InstalledConfiguration.class.getName());

	/** The singleton instance of this configuration */
	private static InstalledConfiguration singleton = new InstalledConfiguration();

	/** A method that returns the configuration singleton */
	static InstalledConfiguration getInstance() {
		assert singleton != null;
		return singleton;
	}

	private ChoiceMakerConfiguration delegate;

	/**
	 * Checks whether the delegate has been set.
	 */
	boolean hasDelegate() {
		return delegate != null;
	}

	/**
	 * Returns a delegate if one has been configured, or throws an
	 * IllegalStateException if one hasn't been
	 * configured.
	 *
	 * @throws IllegalStateException
	 *             if a delegate does not exist and can not be
	 *             created.
	 */
	ChoiceMakerConfiguration getDelegate() {
		if (delegate == null) {
			throw new IllegalStateException("no delegate");
		}
		assert delegate != null;
		return delegate;
	}

	public ProbabilityModelPersistence getModelPersistence(
			ImmutableProbabilityModel model) {
		return getDelegate().getModelPersistence(model);
	}

	public MachineLearnerPersistence getMachineLearnerPersistence(
			MachineLearner machineLearner) {
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

	public String getClassPath() {
		return getDelegate().getClassPath();
	}

	public String getReloadClassPath() {
		return getDelegate().getReloadClassPath();
	}

	public String getJavaDocClasspath() {
		return getDelegate().getJavaDocClasspath();
	}

	public ICompiler getChoiceMakerCompiler() {
		return getDelegate().getChoiceMakerCompiler();
	}

	public void reloadClasses() throws XmlConfException {
		getDelegate().reloadClasses();
	}

	public String toXml() {
		return getDelegate().toXml();
	}

	public String getFileName() {
		return getDelegate().getFileName();
	}

	public File getWorkingDirectory() {
		return getDelegate().getWorkingDirectory();
	}

	public String getCodeRoot() {
		return getDelegate().getCodeRoot();
	}

	public void deleteGeneratedCode() {
		getDelegate().deleteGeneratedCode();
	}

	/** For testing only; otherwise treat as private */
	InstalledConfiguration() {
	}

	/**
	 * Sets the configuration delegate. Used only by the
	 * {@link InstallableConfigurator} class.
	 *
	 * @throws IllegalArgumentException
	 *             if the delegate can not be updated.
	 * */
	void setDelegate(ChoiceMakerConfiguration delegate) {
		if (delegate == null) {
			throw new IllegalArgumentException("null delegate");
		}
		this.delegate = delegate;
	}

}
