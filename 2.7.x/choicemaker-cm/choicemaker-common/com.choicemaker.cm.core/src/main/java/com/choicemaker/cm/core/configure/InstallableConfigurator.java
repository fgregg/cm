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

import java.lang.reflect.Field;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.PropertyNames;
import com.choicemaker.cm.core.XmlConfException;

public final class InstallableConfigurator implements ChoiceMakerConfigurator {

	private static final Logger logger = Logger.getLogger(InstallableConfigurator.class
			.getName());

	/**
	 * The default instance is a configurator that doesn't actually do anything.
	 */
	static final ChoiceMakerConfigurator getDefaultInstance() {
		// FIXME Auto-generated method stub
		return new ChoiceMakerConfigurator() {

			public ChoiceMakerConfiguration init(String fn, boolean reload,
					boolean initGui) throws XmlConfException {
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

	/** The singleton instance of this configurator */
	private static InstallableConfigurator singleton = new InstallableConfigurator();

	/** A method that returns the configurator singleton */
	public static InstallableConfigurator getInstance() {
		assert singleton != null;
		return singleton;
	}

	private ChoiceMakerConfigurator delegate;

	/**
	 * If a delegate hasn't been set, this method looks up a System property to
	 * determine which type of configurator to set and then sets it. If the property
	 * exists but the specified configurator type can not be set, throws an
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
			String msgPrefix = "Installing ChoiceMaker configurator: ";
			String fqcn = System
					.getProperty(PropertyNames.INSTALLABLE_CHOICEMAKER_CONFIGURATOR);
//			Properties p = System.getProperties();
//			for (Iterator i=p.keySet().iterator(); i.hasNext(); ) {
//				Object k = i.next();
//				Object v = p.getProperty((String) k);
//				System.out.println(k + ": " + v);
//			}
			try {
				if (fqcn != null) {
					logger.info(msgPrefix + fqcn);
					install(fqcn);
				} else {
					logger.info(msgPrefix
							+ getDefaultInstance().getClass().getName());
					install(getDefaultInstance());
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

	public ChoiceMakerConfiguration init() throws XmlConfException {
		return getDelegate().init();
	}

	public ChoiceMakerConfiguration init(String fn, String log4jConfName,
			boolean reload, boolean initGui) throws XmlConfException {
		return getDelegate().init(fn, log4jConfName, reload, initGui);
	}

	/** For testing only; otherwise treat as private */
	InstallableConfigurator() {
	}

	/**
	 * Sets the configurator delegate explicitly.
	 *
	 * @throws IllegalArgumentException
	 *             if the delegate can not be updated.
	 * */
	public void install(ChoiceMakerConfigurator delegate) {
		if (delegate == null) {
			throw new IllegalArgumentException("null delegate");
		}
		this.delegate = delegate;
	}

	/**
	 * An alternative method for setting a configurator delegate using a FQCN configurator
	 * name.
	 *
	 * @throws IllegalArgumentException
	 *             if the delegate can not be updated.
	 */
	private void install(String fqcn) {
		if (fqcn == null || fqcn.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"null or blank class name for ChoiceMaker configurator");
		}
		final String msgPrefix = "Installing ChoiceMaker configurator: ";
		try {
			Class c = Class.forName(fqcn);
			Field f = c.getDeclaredField(ChoiceMakerConfigurator.INSTANCE);
			Object o = f.get(null);
			assert o instanceof ChoiceMakerConfigurator;
			ChoiceMakerConfigurator pmm = (ChoiceMakerConfigurator) o;
			install(pmm);
		} catch (Exception e) {
			String msg = msgPrefix + e.toString() + ": " + e.getCause();
			logger.error(msg, e);
			throw new IllegalArgumentException(msg);
		}
	}

}
