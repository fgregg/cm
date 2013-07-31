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
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import com.choicemaker.cm.core.configure.IncompleteSpecificationException;
import com.choicemaker.cm.core.util.DoNothingRecordSourceSerializer;
import com.choicemaker.cm.core.util.Precondition;
import com.choicemaker.cm.core.xmlconf.XmlConfException;

/**
 * An eclipse-based abstract class that partially implements
 * {@link com.choicemaker.cm.core.IRecordSourceSerializer IRecordSourceSerializer}.
 * The class is designed to work with
 * {@link com.choicemaker.cm.core.DefaultRecordSourceSerializationRegistry DefaultRecordSourceSerializationRegistry}.
 * Subclasses must implement a three-parameter constructor with the same signature
 * as the constructor for this class.
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/24 18:02:30 $
 */
public abstract class AbstractRecordSourceSerializer
	implements IRecordSourceSerializer {

	/** The serializer class attribute, <code>class</code> */
	public static final String SERIALIZABLE_RECORD_SOURCE_CLASS = "class"; //$NON-NLS-1$

	/** The serializer properties configuration element, <code>serializerProperties</code> */
	public static final String SERIALIZABLE_RECORD_SOURCE_PROPERTIES = "serializerProperties"; //$NON-NLS-1$

	/** An serializer property configuration element, <code>serializerProperty</code> */
	public static final String SERIALIZABLE_RECORD_SOURCE_PROPERTY = "serializerProperty"; //$NON-NLS-1$

	/** An property name attribute, <code>name</code> */
	public static final String SERIALIZABLE_RECORD_SOURCE_PROPERTY_NAME = "name"; //$NON-NLS-1$

	/** An property value attribute, <code>value</code> */
	public static final String SERIALIZABLE_RECORD_SOURCE_PROPERTY_VALUE = "value"; //$NON-NLS-1$

	/** The serializer configuration element, <code>serializer</code> */
	public static final String RECORD_SOURCE_SERIALIZER = "serializer"; //$NON-NLS-1$

	/**
	 * The name of the <code>uriPattern</code> attribute,
	 * <code></code>.
	 * @see #getSerializerConfiguration()
	 */
	public static final String AN_URI_PATTERN = "uriPattern";

	/**
	 * A convenience method that instantiates a serializable record source
	 * from  a standardized XML representation.
	 * @param xml a non-null, non-blank String produced by the
	 * {@link #toXML(ISerializableRecordSource) toXML method}.
	 * @return a non-null instance of ISerializableRecordSource
	 * @throws XmlConfException if the XML can not be parsed;
	 * i.e. if it is not produced by the <code>toXML</code> method.
	 * @throws IncompleteSpecificationException if the XML
	 * does not completely specify all the properties required to configure
	 * the record source.
	 * @see #toXML(ISerializableRecordSource)
	 */
	public static ISerializableRecordSource fromXML(String xml)
		throws XmlConfException, IncompleteSpecificationException {
		Precondition.assertNonEmptyString(xml);
		// TODO NOT YET IMPLEMENTED
		throw new RuntimeException("not yet implemented");
	}

	public static void logIgnoredClasses(
		Logger log,
		Priority priority,
		Class[] handledClasses) {
		Precondition.assertNonNullArgument("null logger", log);
		Precondition.assertNonNullArgument("null priority", priority);
		if (handledClasses != null && handledClasses.length > 0) {
			StringBuffer sb = new StringBuffer();
			sb.append("Ignoring classes: ");
			for (int i = 0; i < handledClasses.length; i++) {
				if (handledClasses[i] == null) {
					sb.append("'null'");
				} else {
					sb.append("'").append(handledClasses[i]).append("'");
				}
				sb.append(" ");
			}
			String msg = sb.toString().trim();
			log.log(priority, msg);
		}
	}

	public static void logIgnoredPattern(
		Logger log,
		Priority priority,
		Pattern uriPattern) {
		Precondition.assertNonNullArgument("null logger", log);
		Precondition.assertNonNullArgument("null priority", priority);
		if (uriPattern != null) {
			String msg = "Ignoring uriPattern '" + uriPattern.pattern() + "'";
			log.log(priority, msg);
		}
	}

	public static void logIgnoredProperties(
		Logger log,
		Priority priority,
		Properties properties) {
		Precondition.assertNonNullArgument("null logger", log);
		Precondition.assertNonNullArgument("null priority", priority);
		if (properties != null && properties.size() > 0) {
			StringBuffer sb = new StringBuffer();
			sb.append("Ignoring properties: ");
			Set keySet = properties.keySet();
			for (Iterator i = keySet.iterator(); i.hasNext();) {
				String key = (String) i.next();
				String value = properties.getProperty(key);
				sb.append("(").append(key).append("/").append(value).append(
					")");
				sb.append(" ");
			}
			String msg = sb.toString().trim();
			log.log(priority, msg);
		}
	}

	public static void logIgnoredPropertyChange(
		Logger log,
		Priority priority,
		String propName,
		String propValue) {
		Precondition.assertNonNullArgument("null logger", log);
		Precondition.assertNonNullArgument("null priority", priority);
		log.warn(
			"Ignoring new value '"
				+ propValue
				+ "' for property named '"
				+ propName
				+ "'");
	}

	public static void logNullPropertyReturned(
		Logger log,
		Priority priority,
		String propName) {
		Precondition.assertNonNullArgument("null logger", log);
		Precondition.assertNonNullArgument("null priority", priority);
		log.log(
			priority,
			"Returning null for property named '" + propName + "'");
	}

	public static String msgCanNotSerializeInstance(RecordSource rs) {
		String msg =
			DoNothingRecordSourceSerializer.class.getName()
				+ " can not serializer '"
				+ (rs == null ? "null" : rs.getName())
				+ "' ("
				+ (rs == null ? "null" : rs.getClass().getClass().getName())
				+ ")";
		return msg;
	}

	public static String msgCanNotSerializeProperties(Properties p) {
		String msg =
			DoNothingRecordSourceSerializer.class.getName()
				+ " can not serialize '"
				+ (p == null ? "null properties" : p.toString())
				+ "'";
		return msg;
	}

	/**
	 * A convenience method that returns a standardized XML
	 * representation of a serializable record source.
	 */
	public static String toXML(ISerializableRecordSource srs) {
		Precondition.assertNonNullArgument(
			"null serializable record source",
			srs);
		StringBuffer sb = new StringBuffer();
		sb
			.append("<recordSource serializable=\"true\" class=\"")
			.append(srs.getClass().getName())
			.append("\" name=\"")
			.append(srs.getName())
			.append("\" fileName=\"")
			.append(srs.getFileName())
			.append("\" model=\"")
			.append(srs.getModel().getName())
			.append("\">");
		sb.append("<properties>");
		Properties p = srs.getProperties();
		for (Iterator i = p.keySet().iterator(); i.hasNext();) {
			String name = (String) i.next();
			String value = p.getProperty(name);
			sb
				.append("<property name=\"")
				.append(name)
				.append("\" value=\"")
				.append(value)
				.append("\"/>");
		}
		sb.append("</properties>");
		sb.append("</recordSource>");
		String retVal = sb.toString();
		return retVal;
	}

	private final Class[] handledClasses;
	private final Properties serializerProperties;
	private final Pattern uriPattern;

	/**
	 * General constructor for subclasses.
	 * @param uriPattern a pattern that matches the RecordSource URI string
	 * that this serializer can map to an ISerializableRecordSource instance. May be
	 * null if the sublclass allows null values.
	 * @param handledClasses the types of RecordSource that this serializer can
	 * map to an ISerializableRecordSource instance. May be
	 * null if the sublclass allows null values. If the array is non-null, then all
	 * of the array elements should be non-null.
	 * @param otherSerializerProperties other properties (besides the
	 * {@link RECORDSOURCE_SERIALIZER_CLASS class},
	 * {@link RECORDSOURCE_SERIALIZER_URI_PATTERN uriPattern} and
	 * {@link RECORDSOURCE_SERIALIZER_HANDLED_CLASS handledClass}
	 * attributes). May be null if the subclass does not require other properties.
	 */
	protected AbstractRecordSourceSerializer(
		Pattern uriPattern,
		Class[] handledClasses,
		Properties otherSerializerProperties) {

		// Preconditions
		if (handledClasses != null) {
			for (int i = 0; i < handledClasses.length; i++) {
				Precondition.assertNonNullArgument(
					"null class for element " + i,
					handledClasses[i]);
			}
		}

		this.uriPattern = uriPattern;
		this.handledClasses = handledClasses;
		if (otherSerializerProperties == null) {
			this.serializerProperties = new Properties();
		} else {
			this.serializerProperties =
				(Properties) otherSerializerProperties.clone();
		}
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.IRecordSourceSerializer#canSerialize(com.choicemaker.cm.core.RecordSource)
	 */
	public boolean canSerialize(RecordSource rs) {
		boolean retVal = false;
		Class[] classes = this.handledClasses;
		if (classes != null && rs != null) {
			for (int i = 0; !retVal && i < classes.length; i++) {
				retVal = classes[i].isInstance(rs);
			}
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.IRecordSourceSerializer#canSerialize(java.lang.String)
	 */
	public boolean canSerialize(String uri) {
		boolean retVal = false;
		if (this.getUriPattern() != null && uri != null) {
			retVal = this.getUriPattern().matcher(uri).matches();
		}
		return retVal;
	}

	/** May be null */
	public Class[] getHandledClasses() {
		Class[] retVal = null;
		if (this.handledClasses != null) {
			retVal = new Class[this.handledClasses.length];
			System.arraycopy(this.handledClasses, 0, retVal, 0, retVal.length);
		}
		return retVal;
	}

	/**
	 * Provides a non-null and unmodifiable map of all the (non-String)
	 * attribute values and (String) property values used to configure
	 * a serializer.
	 * @return non-null and unmodifiable, but may be empty.
	 */
	public Map getSerializerConfiguration() {
		/*
		public static final String AN_HANDLED_CLASSES = "handledClasses";
		public static final String AN_URI_PATTERN = "uriPattern";
		public static final String AN_SERIALIZER_PROPERTIES = "serializerProperties";
		return Collections.unmodifiableMap(this.serializerProperties);
		*/
		// TODO NOT YET IMPLEMENTED
		throw new RuntimeException("not yet implemented");
	}

	/** May be null */
	public Pattern getUriPattern() {
		return uriPattern;
	}

}
