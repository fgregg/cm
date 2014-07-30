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
package com.choicemaker.cm.core.configure.xml;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.choicemaker.cm.core.IncompleteSpecificationException;

/**
 * A template for implementing the XmlSpecification interface.
 * <p>
 * This class may be used as a subclass or delegate for classes that
 * implement either the Configurable or XmlSpecification interfaces.
 * However, if it is used as a base class for an AbstractConfigurable type,
 * the (@link #equals(Object) equals}, {@link #hashCode() hashCode},
 * and {@link #compareTo(Object) compareTo} methods should be overridden
 * to account for any instance data that is not expressed as a property.
 * </p><p>
 * This class uses a versioning pattern discussed by Patrick Holthuizen
 * to handle the evolution of a type's required and allowed properties.
 * See Patrick's article, "Object serialization," at
 * <a href="http://www.eaze.org/patrick/java/objectserialization.jsp">
 * http://www.eaze.org/patrick/java/objectserialization.jsp
 * </a>.
 * </p>
 * @author rphall
 * @version 1.0.0
 * @since 2.5.206
 */
public abstract class AbstractXmlSpecification implements XmlSpecification {

	/** Subclasses may want to override this value */
	protected static final long serialVersionUID = -1724290709381948608L;

	/**
	 * Returns an array of required property names that are missing from the specified array.
	 */
	public static String[] checkForMissingProperties(
		Properties p,
		String[] requiredPropertyNames) {
		if (p == null || requiredPropertyNames == null) {
			throw new IllegalArgumentException("null argument"); //$NON-NLS-1$
		}
		List missing = new ArrayList();
		for (int i = 0; i < requiredPropertyNames.length; i++) {
			String name = requiredPropertyNames[i];
			if (name == null) {
				throw new IllegalArgumentException("null property name [" + i + "]"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (!p.contains(name)) {
				missing.add(name);
			}
		}
		String[] retVal =
			(String[]) missing.toArray(new String[missing.size()]);
		return retVal;
	}

	protected static String msgAboutStrings(String about, String[] s) {
		StringBuffer sb = new StringBuffer();
		sb.append(about).append(": ["); //$NON-NLS-1$
		if (s.length != 0) {
			for (int i = 0; i < s.length - 1; i++) {
				sb.append(s[i]).append(","); //$NON-NLS-1$
			}
			sb.append(s[s.length - 1]);
		}
		sb.append("]"); //$NON-NLS-1$
		String retVal = sb.toString();
		return retVal;
	}

	private final Properties properties;
	// private final List children = new LinkedList();

	/** Required for serialization */
	protected AbstractXmlSpecification() {
		this.properties = new Properties();
	}

	/**
	 * Constructs a configuration with the specified collection
	 * of required and optional properties.
	 * @param p may be null only if this type
	 * has no required properties.
	 * @see #getRequiredPropertyNames()
	 */
	public AbstractXmlSpecification(Properties p)
		throws IncompleteSpecificationException {

		this.properties = p == null ? new Properties() : (Properties) p.clone();
		String[] missingPropertyNames =
			checkForMissingProperties(
				this.properties,
				this.getRequiredPropertyNames());

		// Postcondition
		if (missingPropertyNames.length != 0) {
			String msg = msgAboutStrings("missing properties", missingPropertyNames); //$NON-NLS-1$
			throw new IllegalArgumentException(msg);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Cloneable
	 */
	public Object clone()
		throws
			CloneNotSupportedException,
			IncompleteSpecificationException,
			InvalidPropertyNameException,
			InvalidPropertyValueException {
		AbstractXmlSpecification retVal =
			(AbstractXmlSpecification) super.clone();
		Properties p = this.getProperties();
		retVal.setAllProperties(p);
		return retVal;
	}

	public boolean equals(Object o) {
		boolean retVal = false;
		if (o instanceof XmlSpecification) {
			XmlSpecification that = (XmlSpecification) o;
			Class thisClass = this.getClass();
			Class thatClass = that.getClass();
			if (thisClass.equals(thatClass)) {
				Properties theseProperties = this.getProperties();
				Properties thoseProperties = that.getProperties();
				retVal = theseProperties.equals(thoseProperties);
			}
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.base.configure.Configurable#getProperties()
	 */
	public Properties getProperties() {
		Properties retVal = (Properties) this.properties.clone();
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.base.configure.Configurable#getProperty(java.lang.String)
	 */
	public String getProperty(String propertyName) {
		return this.properties.getProperty(propertyName);
	}

	/**
	 * Gets the serial sub-version UID. This default method returns zero.
	 * Subclasses should override this method and the serialization methods
	 * {@link #writeObject(ObjectOutputStream) writeObject} and
	 * {@link #readObject(ObjectInputStream) readObject} when their sets
	 * of allowed or required properties change.
	 */
	public long getSerialSubVersionUID() {
		return 0L;
	}

	public int hashCode() {
		int retVal = this.getProperties().hashCode();
		return retVal;
	}

	/**
	 * Reads an instance of <code>Options</code> from the specified input stream.
	 * @param in the input stream to read the object from
	 * @throws java.lang.ClassNotFoundException if the class has an unrecognizable
	 * format
	 * @throws java.io.IOException if some I/O exception occurs
	 */
	protected void readObject(ObjectInputStream in)
		throws
			ClassNotFoundException,
			IOException,
			IncompleteSpecificationException,
			InvalidPropertyNameException,
			InvalidPropertyValueException {
		in.defaultReadObject();
		Long serialSubVersionUID = (Long) in.readObject();
		if (serialSubVersionUID.longValue() == 0L) {
			Properties p = (Properties) in.readObject();
			this.setAllProperties(p);
		} else if (serialSubVersionUID.longValue() > 0L) {
			// Subclasses should override this for sub-version values > 0
			throw new ClassNotFoundException("Unsupported object version"); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.base.configure.Configurable#removeProperty(java.lang.String)
	 */
	public void removeProperty(String propertyName)
		throws IncompleteSpecificationException {
		if (propertyName == null) {
			throw new IllegalArgumentException("null property name"); //$NON-NLS-1$
		}
		String[] required = this.getRequiredPropertyNames();
		for (int i = 0; i < required.length; i++) {
			if (required[i].equals(propertyName)) {
				throw new IncompleteSpecificationException("required property: '" + propertyName + "'"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		this.properties.remove(propertyName);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.base.configure.Configurable#setProperties(java.util.Properties)
	 */
	public void setAllProperties(Properties p)
		throws
			IncompleteSpecificationException,
			InvalidPropertyNameException,
			InvalidPropertyValueException {
		String[] missing =
			checkForMissingProperties(p, this.getRequiredPropertyNames());
		if (missing.length > 0) {
			String msg = msgAboutStrings("missing properties", missing); //$NON-NLS-1$
			throw new IncompleteSpecificationException(msg);
		}
		this.properties.clear();
		this.setProperties(p);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.base.configure.Configurable#setProperties(java.util.Properties)
	 */
	public void setProperties(Properties p)
		throws InvalidPropertyNameException, InvalidPropertyValueException {
		for (Iterator i = p.keySet().iterator(); i.hasNext();) {
			String name = (String) i.next();
			String value = p.getProperty(name);
			this.setProperty(name, value);
		}
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.base.configure.Configurable#setProperty(java.lang.String, java.lang.String)
	 */
	public void setProperty(String propertyName, String propertyValue)
		throws InvalidPropertyNameException, InvalidPropertyValueException {
		if (propertyName == null) {
			throw new IllegalArgumentException("null property name"); //$NON-NLS-1$
		}
		String[] allowedNames = this.getAllowedPropertyNames();
		boolean isAllowed = false;
		for (int i = 0; i < allowedNames.length; i++) {
			if (allowedNames[i].equals(propertyName)) {
				isAllowed = true;
				break;
			}
		}
		if (!isAllowed) {
			throw new InvalidPropertyNameException("invalid property name: '" + propertyName + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (!this.isAllowedPropertyValue(propertyName, propertyValue)) {
			throw new InvalidPropertyValueException("invalid property value: '" + propertyValue + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		this.properties.setProperty(propertyName, propertyValue);
	}

	public String toXML() {
			StringBuffer sb = new StringBuffer().append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>") //$NON-NLS-1$
		.append("<") //$NON-NLS-1$
		.append(ELEMENT_CONFIGURABLE).append(" ") //$NON-NLS-1$
		/*
		.append(ATTRIBUTE_CONFIGURABLE_SERIALIZABLE).append("=\"true\" ") //$NON-NLS-1$
		*/
		.append(ATTRIBUTE_CONFIGURABLE_CLASS).append("=\"") //$NON-NLS-1$
	.append(getClass().getName()).append("\">"); //$NON-NLS-1$
		Properties p = getProperties();
		if (p.size() > 0) {
			sb.append("<").append(ELEMENT_PROPERTIES).append(">"); //$NON-NLS-1$ //$NON-NLS-2$
			for (Iterator i = p.keySet().iterator(); i.hasNext();) {
				String name = (String) i.next();
				String value = p.getProperty(name);
				sb.append("<") //$NON-NLS-1$
				.append(ELEMENT_PROPERTY).append(" ") //$NON-NLS-1$
				.append(ATTRIBUTE_PROPERTY_NAME).append("=\"") //$NON-NLS-1$
				.append(name).append("\" ") //$NON-NLS-1$
				.append(ATTRIBUTE_PROPERTY_VALUE).append("=\"") //$NON-NLS-1$
				.append(value).append("\"/>"); //$NON-NLS-1$
			}
			sb.append("</").append(ELEMENT_PROPERTIES).append(">"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		sb.append("</").append(ELEMENT_CONFIGURABLE).append(">"); //$NON-NLS-1$ //$NON-NLS-2$
		String retVal = sb.toString();
		return retVal;
	}

	/**
	 * Writes an instance of <code>Options</code> to the specified output stream.
	 * @param out the output stream to write the object to
	 * @throws java.io.IOException if some I/O exception occurs
	 */
	protected void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		out.writeObject(new Long(getSerialSubVersionUID()));
		out.writeObject(this.getProperties());
	}

}
