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
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.choicemaker.util.Precondition;

/**
 * XML configurator for XML configurable objects
 * @author rphall
 * @version 1.0.0
 * @since 2.5.206
 */
public abstract class AbstractXmlSpecificationParser
	implements XmlSpecificationParser {

	private static final Logger log =
		Logger.getLogger(AbstractXmlSpecificationParser.class.getName());

	private static final Class CHILDREN_CLASS =
		(new XmlConfigurable[0]).getClass();

	public static Constructor getDefaultConstructor(Class c) {
		Precondition.assertNonNullArgument("null class", c);
		Constructor retVal = null;
		try {
			retVal = c.getConstructor(new Class[0]);
		} catch (Exception x) {
			log.debug(c.getName() + ": no default ctor");
		}
		return retVal;
	}

	public static Constructor getPropertiesChildrenConstructor(Class c) {
		Precondition.assertNonNullArgument("null class", c);
		Constructor retVal = null;
		try {
			retVal =
				c.getConstructor(
					new Class[] { Properties.class, CHILDREN_CLASS });
		} catch (Exception x) {
			log.debug(c.getName() + ": no (Properties,XmlConfigurable[]) ctor");
		}
		return retVal;
	}

	public static Constructor getPropertiesConstructor(Class c) {
		Precondition.assertNonNullArgument("null class", c);
		Constructor retVal = null;
		try {
			retVal = c.getConstructor(new Class[] { Properties.class });
		} catch (Exception x) {
			log.debug(c.getName() + ": no (Properties) ctor");
		}
		return retVal;
	}

	void addChildren(XmlConfigurable retVal, XmlConfigurable[] children) {
		if (retVal instanceof XmlSpecification) {
			XmlSpecification spec = (XmlSpecification) retVal;
			for (int i = 0; i < children.length; i++) {
				spec.add(children[i]);
			}
		} else {
			StringBuffer sb = new StringBuffer();
			sb.append("Ignoring child configurable elements: [");
			for (int i = 0; i < children.length; i++) {
				sb.append(children[i].getClass().getName());
				if (i < children.length - 1) {
					sb.append(", ");
				}
			}
			sb.append("]");
			log.warn(sb.toString());
		}
	}

	/**
	 * Recreates an XmlConfigurable object from standardized
	 * XML.
	 * @see com.choicemaker.cm.configure.XmlConfigurable#toXML()
	 * @see com.choicemaker.cm.configure.XmlSpecification#toXML()
	 * @param classLoader the class loader to use when constructing
	 * objects
	 * @param configurable a non-null
	 * {@link  com.choicemaker.cm.configure.XmlConfigurable#ELEMENT_CONFIGURABLE configurable element}
	 * in the XML specification
	 * @return a non-null XmlConfigurable object
	 * @throws ClassNotFoundException
	 * a class specified by an XmlConfigurable element could
	 * not be found
	 * @throws InstantiationException
	 * a specified class is abstract or an interface
	 * @throws IllegalAccessException
	 * no constructors have appropriate access permissions
	 * @throws InvocationTargetException
	 * a constructor threw an exception
	 */
	XmlConfigurable fromXML(ClassLoader classLoader, IElement configurable)
		throws
			ClassNotFoundException,
			InstantiationException,
			IllegalAccessException,
			InvocationTargetException,
			XmlSpecificationException {

		XmlConfigurable retVal = null;
		try {
			Properties p = getProperties(configurable);
			XmlConfigurable[] children = getChildren(classLoader, configurable);

			/* deprecated
			String serializable =
				configurable.getAttributeValue(
					XmlSpecification.ATTRIBUTE_CONFIGURABLE_SERIALIZABLE);
			if (serializable == null) {
				serializable = XmlConfigurable.ATTRIBUTE_CONFIGURABLE_SERIALIZABLE_DEFAULT;
			}
			boolean isSerializable =
				Boolean.valueOf(serializable).booleanValue();
			if (!isSerializable) {
				String msg =
					"Not serializable: '" + configurable.getName() + "'";
				log.error(msg);
				throw new XmlSpecificationException(msg);
			}
			*/

			String className =
				configurable.getAttributeValue(
					XmlSpecification.ATTRIBUTE_CONFIGURABLE_CLASS);
			Class configurableClass = classLoader.loadClass(className);

			//			// Array of {constructor parameter types, constructor}
			//			Object[][] preferences = new Object[][] {
			//				/* First choice: a constructor taking (Properties,XmlConfigurable[]) */
			//				new Object[] {
			//					new Class[] { Properties.class, CHILDREN_CLASS },
			//					null },
			//				/* Second choice: a constructor taking (Properties) */
			//				new Object[] { new Class[] { Properties.class }, null },
			//				/* Third choice: a default constructor */
			//				new Object[] { new Class[0], null }, };

			if (log.isDebugEnabled()) {
				Constructor[] ctors = configurableClass.getConstructors();
				for (int i = 0; i < ctors.length; i++) {
					Constructor ctor = ctors[i];
					Class[] types = ctor.getParameterTypes();
					logConstructorParams(i, ctor, types);
				}
			}

			// Try constructing with a two-parameter (Properties,XmlConfigurable[]) constructor
			if (XmlSpecification.class.isAssignableFrom(configurableClass)) {
				Constructor ctor =
					getPropertiesChildrenConstructor(configurableClass);
				if (ctor != null) {
					Object o = ctor.newInstance(new Object[] { p, children });
					retVal = (XmlConfigurable) o;
				}
			}

			// Try constructing with a one-parameter (Properties) constructor
			if (retVal == null) {
				Constructor ctor = getPropertiesConstructor(configurableClass);
				if (ctor != null) {
					Object o = ctor.newInstance(new Object[] { p });
					retVal = (XmlSpecification) o;
					addChildren(retVal, children);
				}
			}

			// Try constructing with a no-parameter default constructor
			if (retVal == null) {
				Constructor ctor = getDefaultConstructor(configurableClass);
				if (ctor != null) {
					Object o = ctor.newInstance(new Object[0]);
					retVal = (XmlConfigurable) o;
					addChildren(retVal, children);
				}
			}

			// Check if construction failed
			if (retVal == null) {
				String msg = "No appropriate contructor";
				log.error(msg);
				throw new InstantiationException(msg);
			}

		} catch (Exception x) {
			String msg =
				"Unable to create configurable object from XmlSpecification: "
					+ x.toString();
			log.error(msg, x);
			throw new XmlSpecificationException(msg, x);
		}

		return retVal;
	}

	private static void logConstructorParams(
		int i,
		Constructor ctor,
		Class[] types) {
		if (log.isDebugEnabled()) {
			StringBuffer sb =
				new StringBuffer()
					.append("Constructor ")
					.append(i)
					.append(": ")
					.append(ctor.getName())
					.append("(");
			for (int j = 0; j < types.length; j++) {
				sb.append(types[j].getClass().getName());
				if (j < types.length - 2) {
					sb.append(",");
				}
			}
			sb.append(")");
			log.debug(sb.toString());
		}
	}

	public abstract IBuilder getBuilder();

	/**
	 * A convenience method that recreates an XML-configurable object
	 * from an XML String using the specified builder. This is equivalent
	 * to calling:<p><pre>
	 *     Reader r = new StringReader(xml);
	 * 	IBuilder build = this.getBuilder();
	 *     IDocument document = builder.build(r);
	 *     fromXML(classLoader, IDocument);
	 * </pre>
	 * </p>
	 * @see #fromXML(ClassLoader,IDocument)
	 * @param classLoader the class loader to use when constructing
	 * objects
	 * @param builder a non-null builder
	 * @param xml a non-empty String conforming to the expected
	 * {@link  com.choicemaker.cm.configure.XmlConfigurable#XML_SPECIFICATION_SCHEMA expected schema}
	 * @return a non-null XmlConfigurable object
	 * @throws XmlSpecificationException if an error occurs.
	 * An error may be caused by:<ul>
	 * <li/> Any exception listed for {@link #fromXML(ClassLoader,IDocument)
	 * <li/> IOException
	 * -- (UNEXPECTED) The specified String could not be read
	 * </ul>
	 */
	public XmlConfigurable fromXML(ClassLoader classLoader, String xml)
		throws XmlSpecificationException {

		// Preconditions
		Precondition.assertNonNullArgument("null ClassLoader", classLoader);
		Precondition.assertNonEmptyString(xml);

		XmlConfigurable retVal;
		try {
			Reader r = new StringReader(xml);
			IBuilder builder = this.getBuilder();
			IDocument document = builder.build(r);
			retVal = this.fromXML(classLoader, document);
		} catch (IOException x) {
			String msg =
				"UNEXPECTED: The string '" + xml + "' could not be read";
			log.error(msg, x);
			throw new XmlSpecificationException(msg, x);
		}
		return retVal;
	}

	/**
	 * Recreates an XML-configurable object from a document.
	 * @param classLoader the class loader to use when constructing
	 * objects
	 * @param document a non-null document conforming to the expected
	 * {@link  com.choicemaker.cm.configure.XmlConfigurable#XML_SPECIFICATION_SCHEMA expected schema}
	 * @param configurable a non-null
	 * {@link  com.choicemaker.cm.configure.XmlConfigurable#ELEMENT_CONFIGURABLE configurable element}
	 * in the XML specification
	 * @return a non-null XmlConfigurable object
	 * @throws XmlSpecificationException if an error occurs.
	 * An error may be caused by:<ul>
	 * <li/> ClassNotFoundException
	 * -- a class specified by an XmlConfigurable element could
	 * not be found
	 * <li/> InstantiationException
	 * -- a specified class is abstract or an interface
	 * <li/> NoSuchMethodException
	 * -- an expected constructor does not exist
	 * <li/> IllegalAccessException
	 * -- no constructors have appropriate access permissions
	 * <li/> InvocationTargetException
	 * --  a constructor threw an exception
	 * </ul>
	 */
	public XmlConfigurable fromXML(ClassLoader classLoader, IDocument document)
		throws XmlSpecificationException {

		// Preconditions
		Precondition.assertNonNullArgument("null ClassLoader", classLoader);
		Precondition.assertNonNullArgument("null document", document);

		XmlConfigurable retVal;
		try {
			IElement configurable = document.getConfigurableElement();
			retVal = fromXML(classLoader, configurable);
		} catch (Exception ex) {
			String msg = "Unable to parse document";
			log.error(msg);
			throw new XmlSpecificationException(msg, ex);
		}
		return retVal;
	}

	XmlConfigurable[] getChildren(
		ClassLoader classLoader,
		IElement configurable)
		throws
			ClassNotFoundException,
			InstantiationException,
			IllegalAccessException,
			InvocationTargetException,
			XmlSpecificationException {
		List list = new ArrayList();
		IElement elChildren =
			configurable.getChild(XmlSpecification.ELEMENT_CHILDREN);
		if (elChildren != null) {
			List elChildrenList =
				elChildren.getChildren(XmlConfigurable.ELEMENT_CONFIGURABLE);
			for (Iterator i = elChildrenList.iterator(); i.hasNext();) {
				IElement elConfigurableChild = (IElement) i.next();
				XmlConfigurable configurableChild =
					fromXML(classLoader, elConfigurableChild);
				list.add(configurableChild);
			}
		}
		XmlConfigurable[] retVal =
			(XmlConfigurable[]) list.toArray(new XmlConfigurable[list.size()]);
		return retVal;
	}

	Properties getProperties(IElement configurable) {
		Properties retVal = new Properties();
		IElement elProperties =
			configurable.getChild(XmlConfigurable.ELEMENT_PROPERTIES);
		if (elProperties != null) {
			List elPropertyList =
				elProperties.getChildren(XmlConfigurable.ELEMENT_PROPERTY);
			for (Iterator i = elPropertyList.iterator(); i.hasNext();) {
				IElement elProperty = (IElement) i.next();
				String name =
					elProperty.getAttributeValue(
						XmlConfigurable.ATTRIBUTE_PROPERTY_NAME);
				String value =
					elProperty.getAttributeValue(
						XmlConfigurable.ATTRIBUTE_PROPERTY_VALUE);
				retVal.setProperty(name, value);
			}
		}
		return retVal;
	}

}
