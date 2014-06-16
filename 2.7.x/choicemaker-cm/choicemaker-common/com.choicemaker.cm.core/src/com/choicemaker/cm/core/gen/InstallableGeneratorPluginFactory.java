package com.choicemaker.cm.core.gen;

import java.util.List;

import org.apache.log4j.Logger;

public class InstallableGeneratorPluginFactory implements
		IGeneratorPluginFactory {

	private static final Logger logger = Logger
			.getLogger(InstallableGeneratorPluginFactory.class.getName());

	/**
	 * A System property that holds the FQCN of the default factory type
	 */
	public static final String PROPERTY_INSTALLABLE_GENERATOR_PLUGIN_FACTORY = "installableGeneratorPluginFactory";

	/** The default factory instance (Eclipse2GeneratorPluginFactory) */
	private static final IGeneratorPluginFactory getDefaultGeneratorPluginFactory() {
		return new Eclipse2GeneratorPluginFactory();
	}

	/** The singleton factory */
	private static IGeneratorPluginFactory singleton;

	/**
	 * A default initialization method that looks up a System property to
	 * determine which type of factory to install. Leaves the class in an
	 * invalid state (which is checked in the {@link #getInstance()} method) if
	 * a factory can not be installed.
	 */
	static {
		String msgPrefix = "Installing generator plugin factory: ";
		boolean isOK = false;
		String fqcn = System
				.getProperty(PROPERTY_INSTALLABLE_GENERATOR_PLUGIN_FACTORY);
		if (fqcn != null) {
			try {
				install(fqcn);
				isOK = true;
			} catch (ClassNotFoundException e) {
				String msg = msgPrefix + e.toString() + ": " + e.getCause();
				logger.warn(msg);
			} catch (InstantiationException e) {
				String msg = msgPrefix + e.toString() + ": " + e.getCause();
				logger.warn(msg);
			} catch (IllegalAccessException e) {
				String msg = msgPrefix + e.toString() + ": " + e.getCause();
				logger.warn(msg);
			}

		}
		if (fqcn == null || !isOK) {
			logger.info(msgPrefix
					+ getDefaultGeneratorPluginFactory().getClass().getName());
			try {
				install(getDefaultGeneratorPluginFactory());
			} catch (Exception x) {
				String msg = msgPrefix + x.toString() + ": " + x.getCause();
				logger.error(msg);
				singleton = null;
			}
		}
	}

	/** A method for installing a different factory type */
	public static void install(IGeneratorPluginFactory instance) {
		if (instance == null) {
			throw new IllegalArgumentException("null generator plugin factory");
		}
		singleton = instance;
	}

	/**
	 * An alternative method for installing a different factory type, using a
	 * FQCN factory name
	 *
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static void install(String fqcn) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		if (fqcn == null || fqcn.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"null or blank class name for generator plugin factory");
		}
		Class c = Class.forName(fqcn);
		IGeneratorPluginFactory instance = (IGeneratorPluginFactory) c
				.newInstance();
		install(instance);
	}

	/** A method to get the installed factory */
	public static IGeneratorPluginFactory getInstance() {
		if (singleton == null) {
			throw new IllegalStateException(
					"null instance -- check log for related warnings or errors");
		}
		return singleton;
	}

	public List lookupGeneratorPlugins() throws GenException {
		List retVal = getInstance().lookupGeneratorPlugins();
		assert retVal != null;
		return retVal;
	}

	/** For testing only; otherwise treat as private */
	InstallableGeneratorPluginFactory() {
	}

}
