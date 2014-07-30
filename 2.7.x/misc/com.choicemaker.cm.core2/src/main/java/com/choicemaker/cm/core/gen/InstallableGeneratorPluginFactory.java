package com.choicemaker.cm.core.gen;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.PropertyNames;

/**
 * A singleton implementation that uses an installable delegate to implement
 * IGeneratorPluginFactory methods. In general, a delegate should be installed
 * only once in an application context, and this class encourages this restriction
 * by using a {@link PropertyNames#INSTALLABLE_COMPILER System
 * property} to specify the delegate type. If the property is not set, a
 * {@link #getDefaultInstance() default factory} is used.
 *
 * @author rphall
 *
 */
public final class InstallableGeneratorPluginFactory implements
		IGeneratorPluginFactory {

	private static final Logger logger = Logger
			.getLogger(InstallableGeneratorPluginFactory.class.getName());

	/**
	 * The default factory instance is a stubbed implementation of
	 * IGeneratorPluginFactory that returns an empty list of generator plugins.
	 */
	static final IGeneratorPluginFactory getDefaultInstance() {
		return new IGeneratorPluginFactory() {
			public List lookupGeneratorPlugins() throws GenException {
				return new LinkedList();
			}
		};
	}

	/** The singleton instance of this factory */
	private static InstallableGeneratorPluginFactory singleton = new InstallableGeneratorPluginFactory();

	/** A method that returns the factory singleton */
	public static InstallableGeneratorPluginFactory getInstance() {
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
	 * {@link #getDefaultInstance() default type}. If the default
	 * type can not be set -- for example, if the default type is misconfigured
	 * -- throws a IllegalStateException.
	 *
	 * @throws IllegalStateException
	 *             if a delegate does not exist and can not be set.
	 */
	IGeneratorPluginFactory getDelegate() {
		if (delegate == null) {
			String msgPrefix = "Installing generator plugin factory: ";
			String fqcn = System
					.getProperty(PropertyNames.INSTALLABLE_GENERATOR_PLUGIN_FACTORY);
			try {
				if (fqcn != null) {
					logger.info(msgPrefix + fqcn);
					install(fqcn);
				} else {
					logger.info(msgPrefix
							+ getDefaultInstance().getClass()
									.getName());
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

	public List lookupGeneratorPlugins() throws GenException {
		List retVal = getDelegate().lookupGeneratorPlugins();
		assert retVal != null;
		return retVal;
	}

	/** For testing only; otherwise treat as private */
	InstallableGeneratorPluginFactory() {
	}

	/**
	 * Sets the factory delegate explicitly.
	 *
	 * @throws IllegalArgumentException
	 *             if the delegate can not be updated.
	 * */
	public void install(IGeneratorPluginFactory delegate) {
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
	private void install(String fqcn) {
		if (fqcn == null || fqcn.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"null or blank class name for generator plugin factory");
		}
		final String msgPrefix = "Installing generator plugin factory: ";
		try {
			Class c = Class.forName(fqcn);
			IGeneratorPluginFactory instance = (IGeneratorPluginFactory) c
					.newInstance();
			install(instance);
		} catch (Exception e) {
			String msg = msgPrefix + e.toString() + ": " + e.getCause();
			logger.error(msg, e);
			throw new IllegalArgumentException(msg);
		}
	}

}
