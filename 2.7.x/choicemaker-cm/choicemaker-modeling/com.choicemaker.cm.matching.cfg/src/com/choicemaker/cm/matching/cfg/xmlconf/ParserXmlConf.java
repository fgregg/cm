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
package com.choicemaker.cm.matching.cfg.xmlconf;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.choicemaker.cm.core.util.DateHelper;
import com.choicemaker.cm.core.xmlconf.XmlConfException;
import com.choicemaker.cm.core.xmlconf.XmlConfigurator;
import com.choicemaker.cm.matching.cfg.ContextFreeGrammar;
import com.choicemaker.cm.matching.cfg.ParseTreeNodeStandardizer;
import com.choicemaker.cm.matching.cfg.ParsedData;
import com.choicemaker.cm.matching.cfg.Parser;
import com.choicemaker.cm.matching.cfg.SymbolFactory;
import com.choicemaker.cm.matching.cfg.Tokenizer;
import com.choicemaker.cm.matching.cfg.cyk.CykParser;
import com.choicemaker.cm.matching.gen.Maps;
import com.choicemaker.cm.matching.gen.Relation;
import com.choicemaker.cm.matching.gen.Relations;
import com.choicemaker.cm.matching.gen.Sets;

/**
 * @author ajwinkel
 *
 */
public class ParserXmlConf {

	private static ClassLoader defaultClassLoader = XmlConfigurator.getRmiClassLoader();

	// name and class attributes expected, plus
	//   zero or more tokenizers
	//   zero or one symbol factories
	//   exactly one grammar
	//   zero or one standardizers
	//		- the default standardizer appends each Token's standard token
	//			to the field named by TokenType.toString()
	//   zero or one parsedDataC elements

	public static Parser readFromFile(String file) throws XmlConfException, FileNotFoundException, IOException, JDOMException {
		return readFromStream(new FileInputStream(file), null);
	}

	public static Parser readFromFile(File file) throws XmlConfException, FileNotFoundException, IOException, JDOMException {
		return readFromStream(new FileInputStream(file), null);
	}

	public static Parser readFromStream(InputStream is) throws XmlConfException, IOException, JDOMException {
		return readFromStream(is, null);
	}

	public static Parser readFromStream(InputStream is, URL pluginUrl) throws XmlConfException, IOException, JDOMException {
		Document doc = new SAXBuilder().build(is);
		return readFromElement(doc.getRootElement(), pluginUrl);
	}

	/**
	 * A parser element can have "name" and "class" attributes.  Name is optional, and
	 * class defaults to CykParser.
	 *
	 * We expect zero or more "tokenizer" elements, at most one "factory" element,
	 * exactly one "grammar" element, and at most one "standardizer" element.
	 */
	public static Parser readFromElement(Element e, URL pluginUrl) throws XmlConfException {
		Class cls = ParserXmlConf.getClass(e, CykParser.class);
		Parser parser = (Parser) ParserXmlConf.instantiate(cls);

		List tokenizerElements = new ArrayList();
		Element factoryElement = null;
		Element grammarElement = null;
		Element standardizerElement = null;
		Element parsedDataElement = null;

		// break
		List kids = e.getChildren();
		for (int i = 0; i < kids.size(); i++) {
			Element kid = (Element) kids.get(i);
			String kidName = kid.getName().intern();
			if (kidName == "tokenizer") {
				tokenizerElements.add(kid);
			} else if (kidName == "symbolFactory") {
				if (factoryElement != null) {
					throw new XmlConfException("'parser' element can specify at most one 'symbolFactory' element");
				} else {
					factoryElement = kid;
				}
			} else if (kidName == "grammar") {
				if (grammarElement != null) {
					throw new XmlConfException("'parser' element can not specify more than one 'grammar' element.");
				} else {
					grammarElement = kid;
				}
			} else if (kidName == "standardizer") {
				if (standardizerElement != null) {
					throw new XmlConfException("'parser' element can specify at most one 'standardizer' element.");
				} else {
					standardizerElement = kid;
				}
			} else if (kidName == "parsedData") {
				if (parsedDataElement != null) {
					throw new XmlConfException("'parser' element can specify at most one 'parsedData' element.");
				} else {
					parsedDataElement = kid;
				}
			} else {
				throw new XmlConfException("Unknown child element " + kidName + " in 'parser' element.");
			}
		}

		// Make sure we have our grammar element
		if (grammarElement == null) {
			throw new XmlConfException("'parser' element must specify a 'grammar' element.");
		}

		// Temporary workarounds until we actually implement the defaults
		if (tokenizerElements.size() == 0) {
			throw new XmlConfException("Temporarily, parsers must specify at least one tokenizer.");
		} else if (factoryElement == null) {
			throw new XmlConfException("Temporarily, parsers must specify a symbol factory.");
		} else if (standardizerElement == null) {
			throw new XmlConfException("Temporarily, parsers must specify a standardizer.");
		}

		// create tokenizers
		for (int i = 0; i < tokenizerElements.size(); i++) {
			Element kid = (Element) tokenizerElements.get(i);
			Tokenizer t = TokenizerXmlConf.readFromElement(kid);
			parser.addTokenizer(t);
		}

		// create symbol factory
		SymbolFactory factory = SymbolFactoryXmlConf.readFromElement(factoryElement);
		parser.setSymbolFactory(factory);

		// create grammar
		ContextFreeGrammar grammar = ContextFreeGrammarXmlConf.readFromElement(grammarElement, factory, pluginUrl);
		parser.setGrammar(grammar);

		// create standardizer
		ParseTreeNodeStandardizer standardizer =
			StandardizerXmlConf.readFromElement(standardizerElement, factory);
		parser.setStandardizer(standardizer);

		// use parsed data element, if it exists.
		if (parsedDataElement != null) {
			Class pdClass = getClass(parsedDataElement, ParsedData.class);
			parser.setParsedDataClass(pdClass);
		}

		return parser;
	}

	//
	// Utilities
	//

	public static Class getClass(Element e, Class defaultCls) throws XmlConfException {
		String clsName = e.getAttributeValue("class");
		if (clsName == null) {
			return defaultCls;
		} else {
			try {
				return Class.forName(clsName, true, defaultClassLoader);
			} catch (ClassNotFoundException ex) {
				throw new XmlConfException("Class not found: " + clsName, ex);
			}
		}
	}

	public static Object instantiate(Class cls) throws XmlConfException {
		return instantiate(cls, new Class[0], new Object[0]);
	}

	public static Object instantiate(Class cls, Class[] argTypes, Object[] args) throws XmlConfException {
		Constructor constructor = null;
		try {
			constructor = cls.getConstructor(argTypes);
		} catch (NoSuchMethodException ex) {
			throw new XmlConfException("Unable to find " + args.length + "-arg constructor for " + cls.getName(), ex);
		}

		try {
			return constructor.newInstance(args);
		} catch (IllegalArgumentException ex) {
			throw new XmlConfException("", ex);
		} catch (InstantiationException ex) {
			throw new XmlConfException("", ex);
		} catch (IllegalAccessException ex) {
			throw new XmlConfException("", ex);
		} catch (InvocationTargetException ex) {
			throw new XmlConfException("", ex);
		}
	}

	public static void invoke(Object target, List elements) throws XmlConfException {
		for (int i = 0; i < elements.size(); i++) {
			Element e = (Element) elements.get(i);
			String name = e.getName().intern();

			if (name == "property") {
				setProperty(target, e);
			} else if (name == "method") {
				invokeMethod(target, e);
			} else {
				throw new XmlConfException("Unknown element found: " + name);
			}
		}
	}

	public static void setProperty(Object target, Element e) throws XmlConfException {
		BeanInfo info = null;

		try {
			info = Introspector.getBeanInfo(target.getClass());
		} catch (IntrospectionException ex) {
			throw new XmlConfException("Unable to get BeanInfo for class " + target.getClass().getName());
		}

		PropertyDescriptor[] props = info.getPropertyDescriptors();

		String propName = e.getAttributeValue("name");
		if (propName == null) {
			throw new XmlConfException("Must specify propertyName");
		}

		// NOTE: this can be null...
		String stringVal = e.getAttributeValue("value");

		for (int i = 0; i < props.length; i++) {
			PropertyDescriptor pd = props[i];
			if (pd.getName().equals(propName)) {
				Class cls = pd.getPropertyType();
				Object val = convertToType(stringVal, cls);

				Method setter = pd.getWriteMethod();
				try {
					setter.invoke(target, new Object[]{val});
				} catch (IllegalArgumentException ex) {
					throw new XmlConfException("", ex);
				} catch (IllegalAccessException ex) {
					throw new XmlConfException("", ex);
				} catch (InvocationTargetException ex) {
					throw new XmlConfException("", ex);
				}

				return;
			}
		}

		throw new XmlConfException("Unable to find property '" + propName + "' in class " + target.getClass().getName());
	}

	public static void invokeMethod(Object target, Element e) throws XmlConfException {
		String methodName = e.getAttributeValue("name");
		if (methodName == null) {
			throw new XmlConfException("Must specify methodName");
		}

		Class[] argTypes = buildArgTypes(e);
		Method method = null;
		try {
			method = target.getClass().getMethod(methodName, argTypes);
		} catch (NoSuchMethodException ex) {
			throw new XmlConfException("", ex);
		}

		Object[] args = buildArgs(e);
		try {
			method.invoke(target, args);
		} catch (IllegalArgumentException ex) {
			throw new XmlConfException("", ex);
		} catch (IllegalAccessException ex) {
			throw new XmlConfException("", ex);
		} catch (InvocationTargetException ex) {
			throw new XmlConfException("", ex);
		}
	}

	public static Class[] buildArgTypes(Element e) throws XmlConfException {
		List clses = new ArrayList();
		for (int i = 1; ; i++) {
			String type = e.getAttributeValue("type" + i);
			if (type == null) {
				break;
			} else {
				clses.add(getType(type));
			}
		}

		Class[] classes = new Class[clses.size()];
		for (int i = 0 ; i < classes.length; i++) {
			classes[i] = (Class) clses.get(i);
		}

		return classes;
	}

	public static Object[] buildArgs(Element e) throws XmlConfException {
		List vals = new ArrayList();
		for (int i = 1; ; i++) {
			String type = e.getAttributeValue("type" + i);
			String value = e.getAttributeValue("arg" + i);
			if (type == null) {
				break;
			}

			if (value != null && value.length() >= 3 && value.startsWith("${") && value.endsWith("}")) {
				value = recoverStaticVariable(value);
			}

			vals.add(convertToType(value, type));
		}

		Object[] values = new Object[vals.size()];
		for (int i = 0 ; i < values.length; i++) {
			values[i] = vals.get(i);
		}

		return values;
	}

	private static String recoverStaticVariable(String value) throws XmlConfException {
		if (!(value.length() >= 3 && value.startsWith("${") && value.endsWith("}"))) {
			throw new IllegalArgumentException("Unable to recover static variable: " + value);
		}

		int len = value.length();
		int lastDot = value.lastIndexOf('.');

		String clsName = value.substring(2, lastDot);
		String fieldName = value.substring(lastDot + 1, len - 1);

		Field f = null;

		try {
			Class cls = Class.forName(clsName, false, defaultClassLoader);
			f = cls.getField(fieldName);
		} catch (ClassNotFoundException ex) {
			throw new XmlConfException("Unable to find class: " + clsName);
		} catch (NoSuchFieldException ex) {
			throw new XmlConfException("Class " + clsName + " does not have a field named: " + fieldName);
		}

		if (!Modifier.isStatic(f.getModifiers())) {
			throw new XmlConfException("Field " + fieldName + " in class " + clsName + " is not static!");
		}

		Object fieldVal = null;

		try {
			fieldVal = f.get(null);
		} catch (IllegalArgumentException ex) {
			throw new XmlConfException("", ex);
		} catch (IllegalAccessException ex) {
			throw new XmlConfException("Perhaps " + fieldName + " is not public...", ex);
		}

		if (fieldVal != null) {
			return fieldVal.toString();
		} else {
			return null;
		}
	}

	public static Object convertToType(String value, String type) throws XmlConfException {
		return convertToType(value, getType(type));
	}

	public static Object convertToType(String value, Class type) throws XmlConfException {
		if (type == String.class) {
			return value;
		} else if (type == Date.class) {
			return DateHelper.parse(value);
		} else if (type == Set.class || type == Collection.class) {
			Set s = (Set) Sets.getCollection(value);
			if (s == null) {
				List names = new ArrayList(Sets.getCollectionNames());
				System.out.println(names);
			}
			return s;
		} else if (type == Map.class) {
			return Maps.getMap(value);
		} else if (type == Relation.class) {
			return Relations.getRelation(value);
		} else if (type == Boolean.TYPE) {
			return Boolean.valueOf(value);
		} else if (type == Byte.TYPE) {
			return Byte.valueOf(value);
		} else if (type == Short.TYPE) {
			return Short.valueOf(value);
		} else if (type == Character.TYPE) {
			return new Character(value.length() > 0 ? value.charAt(0) : '\0');
		} else if (type == Integer.TYPE) {
			return Integer.valueOf(value);
		} else if (type == Long.TYPE) {
			return Long.valueOf(value);
		} else if (type == Float.TYPE) {
			return Float.valueOf(value);
		} else if (type == Double.TYPE) {
			return Double.valueOf(value);
		} else {
			throw new XmlConfException("Unknown type: " + type);
		}
	}

	public static Class getType(String type) throws XmlConfException {
		type = type.intern();
		if (type == "String") {
			return String.class;
		} else if (type == "Date") {
			return Date.class;
		} else if (type == "Set") {
			return Set.class;
		} else if (type == "Map") {
			return Map.class;
		} else if (type == "Relation") {
			return Relation.class;
		} else if (type == "boolean") {
			return Boolean.TYPE;
		} else if (type == "byte") {
			return Byte.TYPE;
		} else if (type == "short") {
			return Short.TYPE;
		} else if (type == "char") {
			return Character.TYPE;
		} else if (type == "int") {
			return Integer.TYPE;
		} else if (type == "long") {
			return Long.TYPE;
		} else if (type == "float") {
			return Float.TYPE;
		} else if (type == "double") {
			return Double.TYPE;
		} else {
			throw new XmlConfException("Unknown type: " + type);
		}
	}

}
