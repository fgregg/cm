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
package com.choicemaker.cm.core;

import java.util.Iterator;
import java.util.Properties;

import com.choicemaker.cm.core.configure.IncompleteSpecificationException;

/**
 * Provides standardized methods for
 * {@link ISerializableRecordSource.toXML toXML()},
 * {@link ISerializableRecordSource.getProperties() getProperties()} and
 * {@link ISerializableRecordSource.setProperties(Properties) setProperties(..)}
 * operations.
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/24 18:02:27 $
 */
public abstract class AbstractSerializableRecordSource
	implements ISerializableRecordSource {
		
	private static final long serialVersionUID = 1L;

	/** The record source class attribute, <code>class</code> */
	public static final String SERIALIZABLE_RECORD_SOURCE_CLASS = "class"; //$NON-NLS-1$

	/** The record source properties configuration element, <code>rsProperties</code> */
	public static final String SERIALIZABLE_RECORD_SOURCE_PROPERTIES = "rsProperties"; //$NON-NLS-1$

	/** An record source property configuration element, <code>rsProperty</code> */
	public static final String SERIALIZABLE_RECORD_SOURCE_PROPERTY = "rsProperty"; //$NON-NLS-1$

	/** An property name attribute, <code>name</code> */
	public static final String SERIALIZABLE_RECORD_SOURCE_PROPERTY_NAME = "name"; //$NON-NLS-1$

	/** An property value attribute, <code>value</code> */
	public static final String SERIALIZABLE_RECORD_SOURCE_PROPERTY_VALUE = "value"; //$NON-NLS-1$

	/** The record source configuration element, <code>rs</code> */
	public static final String SERIALIZABLE_RECORD_SOURCE = "recordSource"; //$NON-NLS-1$

	/**
	 * Creates a standardized XML representation of the
	 * specified record source.
	 * @param rs must be non-null
	 * @return a standardized XML representation of record source
	 */
	public static String toXML(ISerializableRecordSource rs) {
		if (rs == null) {
			throw new IllegalArgumentException("null record source"); //$NON-NLS-1$
		}
		StringBuffer sb = new StringBuffer();
		sb.append("<") //$NON-NLS-1$
		.append(SERIALIZABLE_RECORD_SOURCE).append(" isSerializable=\"true\" ") //$NON-NLS-1$
		.append(SERIALIZABLE_RECORD_SOURCE_CLASS).append("=\"") //$NON-NLS-1$
		.append(rs.getClass().getName()).append("\">"); //$NON-NLS-1$
		sb.append("<").append(SERIALIZABLE_RECORD_SOURCE_PROPERTIES).append(">"); //$NON-NLS-1$ //$NON-NLS-2$
		Properties p = rs.getProperties();
		for (Iterator i = p.keySet().iterator(); i.hasNext();) {
			String name = (String) i.next();
			String value = p.getProperty(name);
			sb.append("<") //$NON-NLS-1$
			.append(SERIALIZABLE_RECORD_SOURCE_PROPERTY).append(" ") //$NON-NLS-1$
			.append(SERIALIZABLE_RECORD_SOURCE_PROPERTY_NAME).append("=\"") //$NON-NLS-1$
			.append(name).append("\" ") //$NON-NLS-1$
			.append(SERIALIZABLE_RECORD_SOURCE_PROPERTY_VALUE).append("=\"") //$NON-NLS-1$
			.append(value).append("\"/>"); //$NON-NLS-1$
		}
		sb.append("</").append(SERIALIZABLE_RECORD_SOURCE_PROPERTIES).append(">"); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append("</recordSource>"); //$NON-NLS-1$
		sb.append("</").append(SERIALIZABLE_RECORD_SOURCE).append(">"); //$NON-NLS-1$ //$NON-NLS-2$
		String retVal = sb.toString();
		return retVal;
	}
	
	/**
	 * Recreates a serializable record source from standardized XML.
	 * @param xml standardized XML representation of a record source
	 * @return non-null
	 * @throws InstantiationException if the record source can not be recreated
	 */
	public static ISerializableRecordSource fromXML(String xml) {
		// TODO NOT YET IMPLEMENTED
		throw new RuntimeException("not yet implemented");
	}

	private Properties properties = new Properties();

	/**
	 * Subclasses must implement this method to
	 * check whether the specified Properties instance contains
	 * <em>all</em> the properties necessary to completely specify
	 * the configuration of a record source. This method is invoked from
	 * the default method for {@link #setProperties(Properties)}.
	 * @param p non-null, but may not contain all the property values
	 * necessary to completely specify the configuration of this record source.
	 * @throws IncompleteSpecificationException if the
	 * specified (non-null) Properties object is missing required properties. 
	 */
	protected abstract void checkProperties(Properties p)
		throws IncompleteSpecificationException;

	public Properties getProperties() {
		return (Properties) this.properties.clone();
	}

	/**
	 * A default implementation of
	 * {@link com.choicemaker.cm.urm.ISerializableRecordSource#setProperties(Properties) setProperties}
	 * that checks whether the specific Properties instance is non-null
	 * contains all properties necessary to completely configure this record source.
	 * This method invokes a subclass-specific method to check the completeness
	 * of the specified Properties.
	 * @throws IllegalArgumentException if the specified Properties
	 * object is null.
	 * @throws UrmIncompleteSpecificationException if the
	 * specified (non-null) Properties object is missing required properties.
	 * @see #checkProperties(Properties)
	 */
	public void setProperties(Properties p)
		throws IncompleteSpecificationException {
		if (p == null) {
			throw new IllegalArgumentException("null properties");
		}
		checkProperties(p);
		this.properties = (Properties) p.clone();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.urm.ISerializableRecordSource#toXML()
	 */
	public String toXML() {
		return toXML(this);
	}

}
