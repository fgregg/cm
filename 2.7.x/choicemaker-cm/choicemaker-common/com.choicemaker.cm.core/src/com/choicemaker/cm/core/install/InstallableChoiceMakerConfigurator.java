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

import java.lang.reflect.Field;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.PropertyNames;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.configure.ChoiceMakerConfiguration;
import com.choicemaker.cm.core.configure.ChoiceMakerConfigurator;

public final class InstallableChoiceMakerConfigurator implements ChoiceMakerConfigurator {

	private static final Logger logger = Logger.getLogger(InstallableChoiceMakerConfigurator.class
			.getName());

	/**
	 * The default instance is an that doesn't actually do anything.
	 */
	static final ChoiceMakerConfigurator getDefaultInstance() {
		// FIXME Auto-generated method stub
		return new ChoiceMakerConfigurator() {

			public ChoiceMakerConfiguration init(String fn, boolean reload,
					boolean initGui) throws XmlConfException {
				return null;
			}

			public ChoiceMakerConfiguration reloadClasses(ChoiceMakerConfiguration cmc) {
				return null;
			}

			public ChoiceMakerConfiguration addProbabilityModel(
					ChoiceMakerConfiguration cmc) {
				return null;
			}

			public ChoiceMakerConfiguration init() {
				return null;
			}

			public ChoiceMakerConfiguration init(String fn,
					String log4jConfName, boolean reload, boolean initGui)
					throws XmlConfException {
				return null;
			}

		};
	}

	/** The singleton instance of this manager */
	private static ChoiceMakerConfigurator singleton = new InstallableChoiceMakerConfigurator();

	/** A method that returns the manager singleton */
	public static ChoiceMakerConfigurator getInstance() {
		assert singleton != null;
		return singleton;
	}

	private ChoiceMakerConfigurator delegate;

	/**
	 * If a delegate hasn't been set, this method looks up a System property to
	 * determine which type of manager to set and then sets it. If the property
	 * exists but the specified manager type can not be set, throws an
	 * IllegalStateException. If the property doesn't exist, sets the
	 * {@link #getDefaultInstance() default type}. If the default type can not be
	 * set -- for example, if the default type is misconfigured -- throws a
	 * IllegalStateException.
	 *
	 * @throws IllegalStateException
	 *             if a delegate does not exist and can not be set.
	 */
	ChoiceMakerConfigurator getDelegate() {
		if (delegate == null) {
			String msgPrefix = "Installing model manager: ";
			String fqcn = System
					.getProperty(PropertyNames.INSTALLABLE_CHOICEMAKER_CONFIGURATOR);
			try {
				if (fqcn != null) {
					logger.info(msgPrefix + fqcn);
					set(fqcn);
				} else {
					logger.info(msgPrefix
							+ getDefaultInstance().getClass().getName());
					set(getDefaultInstance());
				}
			} catch (Exception x) {
				String msg = msgPrefix + x.toString() + ": " + x.getCause();
				logger.error(msg, x);
				assert delegate == null;
				throw new IllegalStateException(msg);
			}
		}
		assert delegate != null;
		return delegate;
	}

	public ChoiceMakerConfiguration init(String fn, boolean reload,
			boolean initGui) throws XmlConfException {
		return getDelegate().init(fn, reload, initGui);
	}

	public ChoiceMakerConfiguration reloadClasses(ChoiceMakerConfiguration cmc) {
		return getDelegate().reloadClasses(cmc);
	}

	public ChoiceMakerConfiguration addProbabilityModel(
			ChoiceMakerConfiguration cmc) {
		return getDelegate().addProbabilityModel(cmc);
	}

	public ChoiceMakerConfiguration init() throws XmlConfException {
		return getDelegate().init();
	}

	public ChoiceMakerConfiguration init(String fn, String log4jConfName,
			boolean reload, boolean initGui) throws XmlConfException {
		return getDelegate().init(fn, log4jConfName, reload, initGui);
	}

	/** For testing only; otherwise treat as private */
	InstallableChoiceMakerConfigurator() {
	}

	/**
	 * Sets the manager delegate.
	 *
	 * @throws IllegalArgumentException
	 *             if the delegate can not be updated.
	 * */
	private void set(ChoiceMakerConfigurator delegate) {
		if (delegate == null) {
			throw new IllegalArgumentException("null delegate");
		}
		this.delegate = delegate;
	}

	/**
	 * An alternative method for setting a manager delegate using a FQCN manager
	 * name.
	 *
	 * @throws IllegalArgumentException
	 *             if the delegate can not be updated.
	 */
	private void set(String fqcn) {
		if (fqcn == null || fqcn.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"null or blank class name for model manager");
		}
		final String msgPrefix = "Installing model manager: ";
		try {
			Class c = Class.forName(fqcn);
			Field f = c.getDeclaredField(ChoiceMakerConfigurator.INSTANCE);
			Object o = f.get(null);
			assert o instanceof ChoiceMakerConfigurator;
			ChoiceMakerConfigurator pmm = (ChoiceMakerConfigurator) o;
			set(pmm);
		} catch (Exception e) {
			String msg = msgPrefix + e.toString() + ": " + e.getCause();
			logger.error(msg, e);
			throw new IllegalArgumentException(msg);
		}
	}

}
