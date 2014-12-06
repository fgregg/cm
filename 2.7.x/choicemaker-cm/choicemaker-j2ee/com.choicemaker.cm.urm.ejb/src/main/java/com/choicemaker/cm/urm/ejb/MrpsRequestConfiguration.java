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

import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Defines allowed property names and default values for an MrpsRequest. This
 * implementation goes to some lengths to define names and values in one
 * definitive spot, namely the property file defined by {@link #PROPERTY_FILE}
 * 
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/25 00:17:22 $
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class MrpsRequestConfiguration implements IMrpsRequestConfiguration,
		Cloneable {

	private final static Logger theLog = Logger
			.getLogger(MrpsRequestConfiguration.class.getName());

	/** Allowed property names (excluding PN_VERSION) */
	private static String[] ALLOWED_NONVERSION_NAMES = new String[] {
			IMrpsRequestConfiguration.PN_BATCH_SIZE,
			IMrpsRequestConfiguration.PN_USE_DEFAULT_PREFILTER,
			IMrpsRequestConfiguration.PN_DEFAULT_PREFILTER_FROM_PERCENTAGE,
			IMrpsRequestConfiguration.PN_DEFAULT_PREFILTER_TO_PERCENTAGE,
			IMrpsRequestConfiguration.PN_USE_DEFAULT_POSTFILTER,
			IMrpsRequestConfiguration.PN_DEFAULT_POSTFILTER_FROM_PERCENTAGE,
			IMrpsRequestConfiguration.PN_DEFAULT_POSTFILTER_TO_PERCENTAGE,
			IMrpsRequestConfiguration.PN_USE_DEFAULT_PAIR_SAMPLER,
			IMrpsRequestConfiguration.PN_DEFAULT_PAIR_SAMPLER_SIZE };

	/** The property file that defines allowed property names and default values */
	public static final String PROPERTY_FILE =
		"com/choicemaker/cm/urm/ejb/mrpsRequest.properties";

	/** A special property that is accessible only from {@link #getVersion()} */
	private static final String PN_VERSION = "mrpsRequest.configurationVersion";

	/** The required extension for MRPS specification files */
	public static final String MRPS_EXTENSION = ".mrps";

	private final static Properties defaultProperties = new Properties();
	static {
		InputStream is = null;
		try {
			is = MrpsRequestConfiguration.class.getClassLoader().getResourceAsStream(PROPERTY_FILE);
			defaultProperties.load(is);
			if (theLog.isLoggable(Level.FINE)) {
				theLog.fine("loaded properties from '" + PROPERTY_FILE + "'");
				java.util.Iterator _e =
					getDefaultproperties().entrySet().iterator();
				while (_e.hasNext()) {
					Entry entry = (Entry) _e.next();
					theLog.fine("property: " + entry.getKey() + ", value: "
							+ entry.getValue());
				}
			}
		} catch (Exception x) {
			String msg =
				"unable to load default properties from '" + PROPERTY_FILE
						+ "'";
			theLog.severe(msg + ": " + x);
			throw new IllegalStateException(msg);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception x) {
				}
				is = null;
			}
		} // finally
	} // static

	public static Map getDefaultproperties() {
		return Collections.unmodifiableMap(defaultProperties);
	}

	/**
	 * A utility that standardizes the file name of an MRPS specification file
	 * so that it ends with <code>mrps</code>
	 */
	public static String standardizeMrpsSpecificationFileName(String fileName) {
		if (fileName == null || fileName.trim().length() == 0) {
			throw new IllegalArgumentException("null or blank fileName");
		}
		String retVal = fileName;
		if (!retVal.endsWith(MRPS_EXTENSION)) {
			retVal += MRPS_EXTENSION;
		}
		return retVal;
	}

	// /**
	// * A utility that creates an MRPS specification file with the specified
	// * name. The name is not standdardized before the file is created. If a
	// * standardized name is desired, invoke
	// * {@link #standardizeMrpsSpecificationFileName(String)} before invoking
	// * this method.
	// */
	// public static File createMrpsSpecificationFile(String fileName) {
	// throw new Error("not yet implemented");
	// }
	//
	// /**
	// * Checks that the file contents are valid XML for an MRPS specification
	// * file. This methood does not check if the file name is properly
	// * standardized.
	// */
	// public static boolean isValidMrpsSpecificationFile(File file) {
	// throw new Error("not yet implemented");
	// }

	private Properties _properties = new Properties();

	/** The version of this implementation */
	private String version;

	/** Creates a configuration with default values */
	public MrpsRequestConfiguration() {
		super();
		// Clone isn't working as expected -- Java 1.4, 2014-09-02
		// Properties p = (Properties) defaultProperties.clone();
		Properties p = new Properties();
		for (Iterator it = getDefaultproperties().entrySet().iterator(); it
				.hasNext();) {
			Entry e = (Entry) it.next();
			String key = (String) e.getKey();
			String value = (String) e.getValue();
			p.put(key, value);
		}
		this.version = p.getProperty(PN_VERSION);
		p.remove(PN_VERSION);
		this.setProperties(p);
	}

	/** Creates a copy of the specified configuration */
	public MrpsRequestConfiguration(MrpsRequestConfiguration config) {
		try {
			MrpsRequestConfiguration copy =
				(MrpsRequestConfiguration) config.clone();
			this.version = copy.version;
			this.setProperties(copy.getProperties());
		} catch (CloneNotSupportedException x) {
			String msg =
				"Unexpected CloneNotSupportedException: " + x.getMessage();
			throw new Error(msg);
		}
	}

	public Properties getProperties() {
		Properties retVal = (Properties) this._properties.clone();
		return retVal;
	}

	public String getProperty(String propertyName) {
		return this._properties.getProperty(propertyName);
	}

	public void removeProperty(String propertyName) {
		this._properties.remove(propertyName);
	}

	public void setProperties(Properties p) {

		// Precondition
		if (p == null) {
			throw new IllegalArgumentException("null set of new properties");
		}

		Set newKeys = p.keySet();
		Set allowedKeys = new HashSet();
		allowedKeys.addAll(getAllowedPropertyNames());
		allowedKeys.add(PN_VERSION);
		if (!allowedKeys.containsAll(newKeys)) {
			String msg = "new keys contain unknown property name(s): '";
			for (Iterator i = newKeys.iterator(); i.hasNext();) {
				Object o = i.next();
				if (!allowedKeys.contains(o)) {
					msg += i.next().toString() + "' ";
				}
			}
			throw new IllegalArgumentException(msg);
		}
		this._properties.clear();
		this._properties.putAll(p);
	}

	public void setProperty(String propertyName, String propertyValue) {
		if (propertyName == null
				|| !getAllowedPropertyNames().contains(propertyName)) {
			throw new IllegalArgumentException(
					"Null or illegal property name: '" + propertyName + "'");
		}
		this._properties.setProperty(propertyName, propertyValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Cloneable
	 */
	public Object clone() throws CloneNotSupportedException {
		MrpsRequestConfiguration retVal = null;
		retVal = (MrpsRequestConfiguration) super.clone();
		retVal._properties = (Properties) this._properties.clone();
		retVal.version = this.version;
		return retVal;
	}

	public Set getAllowedPropertyNames() {
		Set retVal = new HashSet();
		for (int i = 0; i < ALLOWED_NONVERSION_NAMES.length; i++) {
			retVal.add(ALLOWED_NONVERSION_NAMES[i]);
		}
		return retVal;
	}

	public String getVersion() {
		return this.version;
	}

}
