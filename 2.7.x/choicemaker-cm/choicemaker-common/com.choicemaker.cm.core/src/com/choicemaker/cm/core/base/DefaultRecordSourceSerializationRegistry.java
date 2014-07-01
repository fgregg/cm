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
package com.choicemaker.cm.core.base;

import java.io.NotSerializableException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.Platform;

import com.choicemaker.cm.core.IRecordSourceSerializationRegistry;
import com.choicemaker.cm.core.IRecordSourceSerializer;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.util.Precondition;
import com.choicemaker.util.StringUtils;

/**
 * An eclipse-based registry of record source serializers.
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/24 18:02:23 $
 */
public class DefaultRecordSourceSerializationRegistry
	implements IRecordSourceSerializationRegistry {

	private static DefaultRecordSourceSerializationRegistry _instance;
	private static Object _instanceSynch = new Object();

	private static final Logger log =
		Logger.getLogger(DefaultRecordSourceSerializationRegistry.class);

	/**
	 * The extension point,
	 * <code>com.choicemaker.cm.urm.updateDerivedFields</code>
	 */
	public static final String SERIALIZABLE_RECORD_SOURCE_EXTENSION_POINT =
		"com.choicemaker.cm.core.rsSerializer";

	/** The serializer priority attribute, <code>priority</code> */
	public static final String SERIALIZABLE_RECORD_SOURCE_PRIORITY = "priority"; //$NON-NLS-1$

	// For brevity in the code that follows
	private static final String CLASS =
		AbstractRecordSourceSerializer.SERIALIZABLE_RECORD_SOURCE_CLASS;
	private static final int DEFAULT_PRIORITY =
		IRecordSourceSerializationRegistry
			.SERIALIZABLE_RECORD_SOURCE_DEFAULT_PRIORITY;
	private static final String POINT =
		SERIALIZABLE_RECORD_SOURCE_EXTENSION_POINT;
	private static final String PRIORITY = SERIALIZABLE_RECORD_SOURCE_PRIORITY;
	private static final String PROPERTIES =
		AbstractRecordSourceSerializer.SERIALIZABLE_RECORD_SOURCE_PROPERTIES;
	private static final String PROPERTY =
		AbstractRecordSourceSerializer.SERIALIZABLE_RECORD_SOURCE_PROPERTY;
	private static final String PROPERTY_NAME =
		AbstractRecordSourceSerializer
			.SERIALIZABLE_RECORD_SOURCE_PROPERTY_NAME;
	private static final String PROPERTY_VALUE =
		AbstractRecordSourceSerializer
			.SERIALIZABLE_RECORD_SOURCE_PROPERTY_VALUE;
	private static final String SERIALIZER =
		AbstractRecordSourceSerializer.RECORD_SOURCE_SERIALIZER;

	public static DefaultRecordSourceSerializationRegistry getInstance() {
		if (_instance == null) {
			synchronized (_instanceSynch) {
				if (_instance == null) {
					_instance = new DefaultRecordSourceSerializationRegistry();
				}
			}
		}
		return _instance;
	}

	private final List serializerRegistry = new LinkedList();

	private DefaultRecordSourceSerializationRegistry() {
		try {
			initialize();
		} catch (Exception x) {
			String msg =
				"Exception thrown during registry initialization: "
					+ x.toString();
			log.error(msg, x);
			throw new RuntimeException(msg);
		}
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.base.IRecordSourceSerializer#hasSerializer(com.choicemaker.cm.core.base.RecordSource)
	 */
	public boolean hasSerializer(RecordSource rs) {
		boolean retVal = false;
		for (Iterator i = this.serializerRegistry.iterator();
			!retVal && i.hasNext();
			) {
			PrioritizedSerializer ps = (PrioritizedSerializer) i.next();
			retVal = ps.serializer.canSerialize(rs);
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.base.IRecordSourceSerializer#hasSerializer(java.lang.String)
	 */
	public boolean hasSerializer(String url) {
		boolean retVal = false;
		for (Iterator i = this.serializerRegistry.iterator();
			!retVal && i.hasNext();
			) {
			PrioritizedSerializer ps = (PrioritizedSerializer) i.next();
			retVal = ps.serializer.canSerialize(url);
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.base.IRecordSourceSerializationRegistry#getPrioritizedInstanceSerializers()
	 */
	public PrioritizedSerializer[] getPrioritizedSerializers() {
		PrioritizedSerializer[] retVal =
			new PrioritizedSerializer[this.serializerRegistry.size()];
		retVal =
			(PrioritizedSerializer[]) this.serializerRegistry.toArray(retVal);
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.base.IRecordSourceSerializer#getSerializableRecordSource(com.choicemaker.cm.core.base.RecordSource)
	 */
	public IRecordSourceSerializer getRecordSourceSerializer(RecordSource rs)
		throws NotSerializableException {
		Precondition.assertNonNullArgument("null record source", rs);

		IRecordSourceSerializer retVal = null;
		for (Iterator i = this.serializerRegistry.iterator();
			(retVal == null) && i.hasNext();
			) {
			PrioritizedSerializer ps = (PrioritizedSerializer) i.next();
			if (ps.serializer.canSerialize(rs)) {
				retVal = ps.serializer;
			}
		}
		if (retVal == null) {
			String msg =
				"Unable to find record source serializer for '"
					+ (rs == null ? null : rs.toString())
					+ "'";
			log.error(msg);
			throw new NotSerializableException(msg);
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.base.IRecordSourceSerializer#getSerializableRecordSource(java.lang.String)
	 */
	public IRecordSourceSerializer getRecordSourceSerializer(String recordsourceURI)
		throws NotSerializableException {
		Precondition.assertNonEmptyString(recordsourceURI);

		IRecordSourceSerializer retVal = null;
		for (Iterator i = this.serializerRegistry.iterator();
			(retVal == null) && i.hasNext();
			) {
			PrioritizedSerializer ps = (PrioritizedSerializer) i.next();
			if (ps.serializer.canSerialize(recordsourceURI)) {
				retVal = ps.serializer;
			}
		}
		if (retVal == null) {
			String msg =
				"Unable to find record source serializer for '"
					+ recordsourceURI
					+ "'";
			log.error(msg);
			throw new NotSerializableException(msg);
		}
		return retVal;
	}

	private void initialize() {
		try {
			IPluginRegistry registry = Platform.getPluginRegistry();
			IExtensionPoint pt = registry.getExtensionPoint(POINT);
			IExtension[] extensions = pt.getExtensions();

			for (int i = 0; i < extensions.length; i++) {
				try {
					IExtension ext = extensions[i];
					IPluginDescriptor descriptor =
						ext.getDeclaringPluginDescriptor();
					ClassLoader pluginClassLoader =
						descriptor.getPluginClassLoader();

					IConfigurationElement[] els =
						ext.getConfigurationElements();
					// assert els.length >= 1;
					// assert els.length <= 2;
					IConfigurationElement elSerializer = els[0];
					IConfigurationElement elProperties = null;
					if (els.length == 2) {
						elProperties = els[1];
					}
					try {
						PrioritizedSerializer serializer =
							instantiateSerializer(
								elSerializer,
								elProperties,
								pluginClassLoader);

						// Don't use registerPrioritizedSerializer(..),
						// because it sorts on every registration.
						// Instead, defer sorting until all extensions
						// have been read.
						serializerRegistry.add(serializer);
					} catch (Exception x2) {
						String msg =
							"Unable to register serializer for extension "
								+ i
								+ " of the extension point '"
								+ POINT
								+ "' in the plug-in '"
								+ descriptor.getUniqueIdentifier()
								+ "' -- CONTINUING";
						log.error(msg, x2);
					}

				} catch (Exception x1) {
					String msg =
						"Unable to get extension "
							+ i
							+ " of the extension point '"
							+ POINT
							+ "' -- CONTINUING";
					log.error(msg, x1);
				}
			} // i, extensions
			Collections.sort(serializerRegistry);

		} catch (Exception x0) {
			String msg =
				"Unable to get extension point '" + POINT + "' -- FAILED";
			log.error(msg, x0);
			throw new RuntimeException(x0);
		}
	}

	private PrioritizedSerializer instantiateSerializer(
		IConfigurationElement elSerializer,
		IConfigurationElement elProperties,
		ClassLoader pluginClassLoader)
		throws Exception {

		IRecordSourceSerializer serializer;

		String className = elSerializer.getAttribute(CLASS);
		Class serializerClass =
			Class.forName(className, true, pluginClassLoader);

		int priority = DEFAULT_PRIORITY;
		String priorityValue = elSerializer.getAttribute(PRIORITY);
		if (!StringUtils.nonEmptyString(priorityValue)) {
			String msg =
				"null priority value -- DEFAULTING TO " + DEFAULT_PRIORITY;
			log.debug(msg);
		} else {
			try {
				priority = Integer.parseInt(priorityValue);
			} catch (NumberFormatException nfe) {
				String msg =
					"Bad priority value '"
						+ priorityValue
						+ "' -- DEFAULTING TO "
						+ DEFAULT_PRIORITY;
				log.error(msg);
			}
		}

		Properties properties;
		if (elProperties == null) {
			properties = null;
		} else {
			properties = new Properties();
			IConfigurationElement[] els = elProperties.getChildren(PROPERTY);
			for (int i = 0; i < els.length; i++) {
				String key = els[i].getAttribute(PROPERTY_NAME);
				String value = els[i].getAttribute(PROPERTY_VALUE);
				properties.setProperty(key, value);
			}
		}

		serializer = (IRecordSourceSerializer) serializerClass.newInstance();
		if (properties != null) {
			Properties propertiesWithDefaults = serializer.getProperties();
			for (Iterator i = properties.keySet().iterator(); i.hasNext();) {
				String key = (String) i.next();
				String value = properties.getProperty(key);
				propertiesWithDefaults.setProperty(key, value);
			}
			serializer.setProperties(propertiesWithDefaults);
		}
		PrioritizedSerializer retVal =
			new PrioritizedSerializer(serializer, priority);

		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.base.IRecordSourceSerializationRegistry#registerRecordSourceSerializer(com.choicemaker.cm.core.base.IRecordSourceSerializer, int)
	 */
	public void registerRecordSourceSerializer(
		IRecordSourceSerializer serializer,
		int priority) {
		Precondition.assertNonNullArgument("null serializer", serializer);

		PrioritizedSerializer ps =
			new PrioritizedSerializer(serializer, priority);
		this.serializerRegistry.add(ps);
		Collections.sort(this.serializerRegistry);
	}

}
