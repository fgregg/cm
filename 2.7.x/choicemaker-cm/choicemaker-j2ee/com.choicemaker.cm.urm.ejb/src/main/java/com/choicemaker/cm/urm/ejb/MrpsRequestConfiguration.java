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

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.helpers.Loader;

/**
 * Defines allowed property names and default values for an MrpsRequest. This
 * implementation goes to some lengths to define names and values in one
 * definitive spot, namely the property file defined by {@link #PROPERTY_FILE}
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/25 00:17:22 $
 */
public class MrpsRequestConfiguration implements IMrpsRequestConfiguration {

	private final static Logger theLog =
		Logger.getLogger(MrpsRequestConfiguration.class);

	/** The property file that defines allowed property names and default values */
	public static final String PROPERTY_FILE =
		"com/choicemaker/cm/urm/ejb/mrpsRequest.properties";

	private final static Properties defaultProperties = new Properties();
	static {
		InputStream is = null;
		try {
			URL url = Loader.getResource(PROPERTY_FILE);
			is = url.openStream();
			defaultProperties.load(is);
			if (theLog.isDebugEnabled()) {
				theLog.debug("loaded properties from '" + PROPERTY_FILE + "'");
				java.util.Enumeration _e = defaultProperties.propertyNames();
				while (_e.hasMoreElements()) {
					theLog.debug("property: " + _e.nextElement());
				}
			}
		} catch (Exception x) {
			String msg =
				"unable to load default properties from '"
					+ PROPERTY_FILE
					+ "'";
			theLog.fatal(msg, x);
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

	/** A special property that is accessible only from {@link #getVersion()}*/
	private static final String PN_VERSION = "mrpsRequest.configurationVersion";

	/** The required extension for MRPS specification files */
	public static final String MRPS_EXTENSION = ".mrps";

	/**
 * A utility that standardizes the file name of an MRPS specification
 * file so that it ends with <code>mrps</code>
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

	/**
 * A utility that creates an MRPS specification file with the
 * specified name. The name is not standdardized before the file
 * is created. If a standardized name is desired, invoke
 * {@link #standardizeMrpsSpecificationFileName(String)} before
 * invoking this method.
 */
  public static File createMrpsSpecificationFile(String fileName) {
		// FIXME IMPLEMENTME
		return new File(fileName);
	}

	/**
 * Checks that the file contents are valid XML for an MRPS
 * specification file. This methood does not check if the file
 * name is properly standardized.
 */
  public static boolean isValidMrpsSpecificationFile(File file) {
		// FIXME IMPLEMENTME
		return true;
	}

	/** Moved from deprecated AbstractConfigureable */
	private Properties _properties = new Properties();

	/** The version of this implementation */
	private String version;

	/** Creates a configuration with default values */
	public MrpsRequestConfiguration() {
		super();
		Properties p = (Properties) defaultProperties.clone();
		this.version = p.getProperty(PN_VERSION);
		p.remove(PN_VERSION);
		this.setProperties(p);
	}

	/** Creates a copy  of the specified configuration */
	public MrpsRequestConfiguration(MrpsRequestConfiguration config) {
		try {
			MrpsRequestConfiguration copy =
				(MrpsRequestConfiguration) config.clone();
			this.version = copy.version;
			this.setProperties(copy.getProperties());
		} catch (CloneNotSupportedException x) {
			String msg = "Unexpected CloneNotSupportedException: " + x.getMessage();
			throw new Error(msg);
		}
	}

	/** Moved from deprecated AbstractConfigureable */
	public Properties getProperties() {
		Properties retVal = (Properties) this._properties.clone();
		return retVal;
	}


	/** Moved from deprecated AbstractConfigureable */
	public String getProperty(String propertyName) {
		return this._properties.getProperty(propertyName);
	}


	/** Moved from deprecated AbstractConfigureable */
	public void removeProperty(String propertyName) {
		this.removeProperty(propertyName);
	}


	/** Moved from deprecated AbstractConfigureable */
	public void setProperties(Properties p) {

		// Precondition
		if (p == null) {
			throw new IllegalArgumentException("null set of new properties");
		}

		Set newKeys = p.keySet();
		Set allowedKeys = getAllowedPropertyNames();
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
		this._properties.putAll(p);
	}


	/** Moved from deprecated AbstractConfigureable */
	public void setProperty(String propertyName, String propertyValue) {
		if (propertyName == null
			|| !getAllowedPropertyNames().contains(propertyName)) {
			throw new IllegalArgumentException(
				"Null or illegal property name: '" + propertyName + "'");
		}
		this._properties.setProperty(propertyName, propertyValue);
	}

	/* (non-Javadoc)
	 * @see java.lang.Cloneable
	 */
	public Object clone() throws CloneNotSupportedException {
		MrpsRequestConfiguration retVal = null;
		retVal = (MrpsRequestConfiguration) super.clone();
		retVal._properties = (Properties) this._properties.clone();
		retVal.version = this.version;
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.configure.AbstractConfigurable#getAllowedPropertyNames()
	 */
	public Set getAllowedPropertyNames() {
		Set retVal = defaultProperties.keySet();
		retVal.remove(PN_VERSION);
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.configure.Configureable#getVersion()
	 */
	public String getVersion() {
		return this.version;
	}

}

