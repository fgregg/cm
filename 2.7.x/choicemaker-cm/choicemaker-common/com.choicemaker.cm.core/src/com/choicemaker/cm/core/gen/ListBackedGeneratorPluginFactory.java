package com.choicemaker.cm.core.gen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

public class ListBackedGeneratorPluginFactory implements
		IGeneratorPluginFactory {

	private static final Logger logger = Logger
			.getLogger(ListBackedGeneratorPluginFactory.class.getName());

	/**
	 * The default list of generator plugin classes.
	 */
	public static final String DEFAULT_LIST = "/com/choicemaker/cm/core/gen/defaultGeneratorPlugins.txt";

	/**
	 * The default character set for lists of generator plugin classes.
	 */
	public static final String DATA_CHARSET = "UTF-8";

	/**
	 * Blank lines and lines beginning with '#' are skipped in a list of
	 * generator plugin classes
	 */
	public static final String COMMENT = "#";

	/**
	 * A System property that holds the name of the factory list
	 */
	public static final String PROPERTY_GENERATOR_PLUGIN_FACTORIES = "generatorPluginFactories";

	// An unmodifiable list of generator plugins
	private final List generatorPlugins;

	/**
	 * A default constructor method that looks up a System property to determine
	 * the list of plugins to install
	 */
	public ListBackedGeneratorPluginFactory() throws IllegalArgumentException {
		this(load());
	}

	/**
	 * A constructor method that uses the specified file name to load a list of
	 * generator plugins
	 */
	public ListBackedGeneratorPluginFactory(String listFileName)
			throws IllegalArgumentException {
		this(load(listFileName));
	}

	/**
	 * A constructor method that loads the specified list of generator plugins
	 */
	public ListBackedGeneratorPluginFactory(List generatorPlugins) {
		this.generatorPlugins = validateAndCopy(generatorPlugins);
	}

	/**
	 * Loads generator plugins from a list specified by the System property
	 * {@link #PROPERTY_GENERATOR_PLUGIN_FACTORIES}, or uses the
	 * {@link #DEFAULT_LIST default list} if the property is not set
	 *
	 * @return a non-null list (possibly empty)
	 * @throws IllegalArgumentException
	 *             if a specified list cannot be found or is not a valid list of
	 *             generators
	 * @throws IllegalStateException
	 *             if the default list cannot be found or is not a valid list of
	 *             generators
	 */
	public static List load() throws IllegalArgumentException {
		List retVal = null;
		String gpf = System.getProperty(PROPERTY_GENERATOR_PLUGIN_FACTORIES);
		gpf = gpf == null ? null : gpf.trim();
		if (gpf != null && !gpf.isEmpty()) {
			retVal = load(gpf);
		} else if (gpf != null) {
			assert gpf.isEmpty();
			logger.warn("Ignoring empty list: " + gpf);
		}
		if (retVal == null) {
			logger.debug("Loading generator plugins from " + DEFAULT_LIST);
			try {
				retVal = load(DEFAULT_LIST);
			} catch (IllegalArgumentException x) {
				String msg = "Default list doesn't exist or is not valid: "
						+ x.getMessage() + ": " + x.getCause();
				throw new IllegalStateException(msg);
			}
		}
		assert retVal != null;
		return retVal;
	}

	/**
	 * An alternative method for loading a list of generator plugins from a
	 * specified file
	 *
	 * @throws IllegalArgumentException
	 */
	public static List load(String resourceName)
			throws IllegalArgumentException {
		if (resourceName == null || resourceName.trim().isEmpty()) {
			throw new IllegalArgumentException("null or blank file name");
		}

		final String msgPrefix = "Loading generator plugins: ";
		final List retVal = new LinkedList();
		BufferedReader r = null;
		try {
			InputStream is = ListBackedGeneratorPluginFactory.class
					.getResourceAsStream(resourceName);
			r = new BufferedReader(new InputStreamReader(is, DATA_CHARSET));
			String fqcn = r.readLine();
			while (fqcn != null) {
				if (!fqcn.isEmpty() && !fqcn.startsWith(COMMENT)) {
					Class c = Class.forName(fqcn);
					GeneratorPlugin instance = (GeneratorPlugin) c
							.newInstance();
					retVal.add(instance);
				}
				fqcn = r.readLine();
			}
		} catch (Exception x) {
			String msg = msgPrefix + x.toString() + ": " + x.getCause();
			logger.error(msg, x);
			throw new IllegalArgumentException(msg);
		} finally {
			if (r != null) {
				try {
					r.close();
				} catch (IOException x) {
					String msg = msgPrefix + x.toString() + ": " + x.getCause();
					logger.error(msg);
				}
			}
		}

		assert retVal != null;
		return retVal;
	}

	/**
	 * Checks that the specified list of generator plugins is valid and returns
	 * an unmodifiable list of the generator plugins
	 *
	 * @return a non-null list (possibly empty) of validated generator plugins
	 * @throws IllegalArgumentException
	 *             if the list is null or is not a valid list of generators
	 */
	public static List validateAndCopy(List generatorPlugins)
			throws IllegalArgumentException {
		if (generatorPlugins == null) {
			throw new IllegalArgumentException("null list of generator plugins");
		}
		List retVal = new LinkedList();
		for (Iterator i = generatorPlugins.iterator(); i.hasNext();) {
			Object o = i.next();
			try {
				GeneratorPlugin gp = (GeneratorPlugin) o;
				if (gp == null) {
					throw new IllegalArgumentException("null generator plugin");
				}
				retVal.add(gp);
			} catch (ClassCastException x) {
				String msg = "Invalid type at index " + i + ": "
						+ o.getClass().getName() + ": " + x.getMessage() + ": "
						+ x.getCause();
				throw new IllegalArgumentException(msg);
			}
		}
		if (retVal.isEmpty()) {
			logger.warn("Empty list of generator plugins");
		}
		return Collections.unmodifiableList(retVal);
	}

	public List lookupGeneratorPlugins() {
		return this.generatorPlugins;
	}

}
