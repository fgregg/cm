package com.choicemaker.cm.core.gen;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * A singleton implementation that uses an installable delegate to implement
 * IGeneratorPluginFactory methods. In general, a delegate should be installed
 * only once in an application context, and this class enforces this restriction
 * by using a {@link #PROPERTY_INSTALLABLE_GENERATOR_PLUGIN_FACTORY System
 * property} to specify the delegate type. If the property is not set, a
 * {@link #getDefaultGeneratorPluginFactory() default factory} is used.
 *
 * @author rphall
 *
 */
public class InstallableGeneratorPluginFactory implements
		IGeneratorPluginFactory {

	private static final Logger logger = Logger
			.getLogger(InstallableGeneratorPluginFactory.class.getName());

	/**
	 * A System property that holds the FQCN of the installable factory delegate
	 */
	public static final String PROPERTY_INSTALLABLE_GENERATOR_PLUGIN_FACTORY = "installableGeneratorPluginFactory";

	/**
	 * The default factory instance is a stubbed implementation of
	 * IGeneratorPluginFactory that returns an empty list of generator plugins.
	 */
	public static final IGeneratorPluginFactory getDefaultGeneratorPluginFactory() {
		return new IGeneratorPluginFactory() {
			public List lookupGeneratorPlugins() throws GenException {
				return new LinkedList();
			}
		};
	}

	/** The singleton instance of this factory */
	private static InstallableGeneratorPluginFactory singleton = new InstallableGeneratorPluginFactory();

	/** A method that returns the factory singleton */
	public static IGeneratorPluginFactory getInstance() {
		assert singleton != null;
		return singleton;
	}

	/**
	 * The delegate used by the factory singleton to implement the
	 * IGeneratorPluginFactory interface.
	 */
	private IGeneratorPluginFactory delegate;

	/**
	 * If a delegate hasn't been set, this method looks up a System property to
	 * determine which type of factory to set and then sets it. If the property
	 * exists but the specified factory type can not be set, throws an
	 * IllegalStateException. If the property doesn't exist, sets the
	 * {@link #getDefaultGeneratorPluginFactory() default type}. If the default
	 * type can not be set -- for example, if the default type is misconfigured
	 * -- throws a IllegalStateException.
	 *
	 * @throws IllegalStateException
	 *             if a delegate does not exist and can not be set.
	 */
	public IGeneratorPluginFactory getDelegate() {
		if (delegate == null) {
			String msgPrefix = "Installing generator plugin factory: ";
			String fqcn = System
					.getProperty(PROPERTY_INSTALLABLE_GENERATOR_PLUGIN_FACTORY);
			try {
				if (fqcn != null) {
					logger.info(msgPrefix + fqcn);
					set(fqcn);
				} else {
					logger.info(msgPrefix
							+ getDefaultGeneratorPluginFactory().getClass()
									.getName());
					set(getDefaultGeneratorPluginFactory());
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

	public List lookupGeneratorPlugins() throws GenException {
		List retVal = getDelegate().lookupGeneratorPlugins();
		assert retVal != null;
		return retVal;
	}

	/** For testing only; otherwise treat as private */
	InstallableGeneratorPluginFactory() {
	}

	/**
	 * Sets the factory delegate.
	 *
	 * @throws IllegalArgumentException
	 *             if the delegate can not be updated.
	 * */
	private void set(IGeneratorPluginFactory delegate) {
		if (delegate == null) {
			throw new IllegalArgumentException("null delegate");
		}
		this.delegate = delegate;
	}

	/**
	 * An alternative method for setting a factory delegate using a FQCN factory
	 * name.
	 *
	 * @throws IllegalArgumentException
	 *             if the delegate can not be updated.
	 */
	private void set(String fqcn) {
		if (fqcn == null || fqcn.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"null or blank class name for generator plugin factory");
		}
		final String msgPrefix = "Installing generator plugin factory: ";
		try {
			Class c = Class.forName(fqcn);
			IGeneratorPluginFactory instance = (IGeneratorPluginFactory) c
					.newInstance();
			set(instance);
		} catch (Exception e) {
			String msg = msgPrefix + e.toString() + ": " + e.getCause();
			logger.error(msg, e);
			throw new IllegalArgumentException(msg);
		}
	}

}
