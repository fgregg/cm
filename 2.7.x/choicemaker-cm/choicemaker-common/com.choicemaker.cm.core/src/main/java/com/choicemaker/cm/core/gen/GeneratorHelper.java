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
package com.choicemaker.cm.core.gen;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Element;

import com.choicemaker.cm.core.Constants;
import com.choicemaker.cm.core.DerivedSource;

/**
 * Auxiliary methods for generators.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/27 21:03:47 $
 */
public class GeneratorHelper {
	private static final List EMPTY_LIST = new ArrayList();

	private GeneratorHelper() {
	}

	public static final int OK = 0;
	public static final int DUPLICATE_POS = 1;
	public static final int POS_OUTSIDE_RANGE = 2;
	private static final int RANGE = 1024;

	/**
	 * Remove the fields that are derived for the specified source.
	 *
	 * @param   fields  The list of fields to be filtered.
	 * @param   src  The source for which the filtering is to be done.
	 */
	public static int filterFields(List fields, DerivedSource src, String field) {
		return filterFields(fields, src, field, "all");
	}

	public static boolean isDerived(Element field, DerivedSource src) {
		Element d = field.getChild("derived");
		if (d != null) {
			String fSrc = d.getAttributeValue("src");
			if (fSrc != null) {
				if (DerivedSource.valueOf(fSrc).includes(src)) {
					return true;
				}
			} else {
				return true;
			}
		}
		return false;
	}

	public static int filterFields(List fields, DerivedSource src, String field, String confName) {
		DerivedSource conf = DerivedSource.valueOf(confName);
		Element[] p = new Element[RANGE];
		int lastPos = -1;
		int maxPos = -1;
		for (Iterator iFields = fields.iterator(); iFields.hasNext();) {
			Element f = (Element) iFields.next();
			Element d = f.getChild("derived");
			if (d != null) {
				String fSrc = d.getAttributeValue("src");
				if (fSrc == null || DerivedSource.valueOf(fSrc).includes(src)) {
					continue;
				}
			}
			Element c = f.getChild(field);
			if (c != null) {
				if ("false".equals(c.getAttributeValue(CoreTags.USE)) || !includesConf(c, conf)) {
					continue;
				}
				String pos = c.getAttributeValue("pos");
				if (pos != null) {
					lastPos = Integer.parseInt(pos) - 1;
				}
			}
			++lastPos;
			if (lastPos < 0 || lastPos >= RANGE) {
				return POS_OUTSIDE_RANGE;
			}
			if (p[lastPos] != null) {
				return DUPLICATE_POS;
			}
			p[lastPos] = f;
			maxPos = Math.max(maxPos, lastPos);
		}
		fields.clear();
		for (int j = 0; j <= maxPos; ++j) {
			fields.add(p[j]);
		}
		return OK;
	}


	/** This version fixes the assumption that each field can only have 1 dbField.
	 * 
	 * @param fields
	 * @param src
	 * @param field
	 * @param confName
	 * @return
	 */
	public static int filterFields2(List fields, DerivedSource src, String field, String confName) {
		DerivedSource conf = DerivedSource.valueOf(confName);
		Element[] p = new Element[RANGE];
		int lastPos = -1;
		int maxPos = -1;
		for (Iterator iFields = fields.iterator(); iFields.hasNext();) {
			Element f = (Element) iFields.next();
			Element d = f.getChild("derived");
			if (d != null) {
				String fSrc = d.getAttributeValue("src");
				if (fSrc == null || DerivedSource.valueOf(fSrc).includes(src)) {
					continue;
				}
			}
			
			//key fix here
			//Element c = f.getChild(field);
			
			//gets the dbField children for this field
			List list = f.getChildren(field);
			boolean found = false;
			for (int i=0; i<list.size(); i++) {
				Element c = (Element) list.get(i);
				if (c != null) {
					if (includesConf(c, conf) && !"false".equals(c.getAttributeValue(CoreTags.USE))) {
						found = true;
					}
					String pos = c.getAttributeValue("pos");
					if (pos != null) {
						lastPos = Integer.parseInt(pos) - 1;
					}
				}
			} //end for i
			if (list.size() == 0) found = true; //if no dbField is specified, assume it's included.
			if (!found) 
				continue;
			
			++lastPos;
			if (lastPos < 0 || lastPos >= RANGE) {
				return POS_OUTSIDE_RANGE;
			}
			if (p[lastPos] != null) {
				return DUPLICATE_POS;
			}
			p[lastPos] = f;
			maxPos = Math.max(maxPos, lastPos);
		} //end for iterator
		fields.clear();
		for (int j = 0; j <= maxPos; ++j) {
			fields.add(p[j]);
		}
		return OK;
	}

	/**
	 * Returns a Java source code expression to convert a value of a variable
	 * into an object type.
	 *
	 * @param   type  The type of the variable.
	 * @param   name  The name of the variable.
	 * @return  The Java source code expression to convert a value of a variable
	 *            into an object type.
	 */
	public static String getObjectExpr(String type, String name) {
		type = type.intern();
		if (type == "boolean") {
			return "new Boolean(" + name + ")";
		} else if (type == "byte") {
			return "new Byte(" + name + ")";
		} else if (type == "short") {
			return "new Short(" + name + ")";
		} else if (type == "char") {
			return "new Character(" + name + ")";
		} else if (type == "int") {
			return "new Integer(" + name + ")";
		} else if (type == "long") {
			return "new Long(" + name + ")";
		} else if (type == "float") {
			return "new Float(" + name + ")";
		} else if (type == "double") {
			return "new Double(" + name + ")";
		} else {
			return name;
		}
	}

	public static String getStringExpr(String type, String name) {
		type = type.intern();
		if (isPrimitiveType(type)) {
			return "String.valueOf(" + name + ")";
		} else if ("String" == type || "java.lang.String" == type) {
			return name;
		} else if ("Date" == type || "java.util.Date" == type || "java.sql.Date" == type) {
			return "com.choicemaker.cm.core.util.DateHelper.formatDb(" + name + ")";
		} else {
			return name + ".toString()";
		}
	}

	public static String getNullValue(String type) {
		type = type.intern();
		if (type == "boolean") {
			return "false";
		} else if (
			type == "byte"
				|| type == "short"
				|| type == "char"
				|| type == "int"
				|| type == "long"
				|| type == "float"
				|| type == "double") {
			return "0";
		} else {
			return "null";
		}
	}

	/**
	 * Returns a Java source code expression to convert a String expression into
	 * a value of a specified type.
	 *
	 * @param   type  The result type of the expression.
	 * @param   expr  The String expression.
	 * @return  Java source code expression to convert a String expression into
	 *            a value of a specified type.
	 */
	public static String getFromStringConversionExpression(
		String type,
		String expr,
		boolean sqlDate,
		boolean intern,
		boolean computed) {
		type = type.intern();
		if (type == "String" || type == "java.lang.String") {
			if (intern) {
				if (computed) {
					return "(__tmpStr = (" + expr + ")) != null ? __tmpStr.intern() : null";
				} else {
					return expr + " != null ? " + expr + ".intern() : null";
				}
			} else {
				return expr;
			}
		} else if (type == "boolean") {
			return expr + ".length() == 0 ? false : Boolean.getBoolean(" + expr + ")";
		} else if (type == "byte") {
			return expr + ".length() == 0 ? (byte)0 : Byte.parseByte(" + expr + ")";
		} else if (type == "short") {
			return expr + ".length() == 0 ? (short)0 : Short.parseShort(" + expr + ")";
		} else if (type == "char") {
			return expr + ".length() == 0 ? (char)0 : StringUtils.getChar(" + expr + ")";
		} else if (type == "int") {
			return expr + ".length() == 0 ? (int)0 : Integer.parseInt(" + expr + ")";
		} else if (type == "long") {
			return expr + ".length() == 0 ? (long)0 : Long.parseLong(" + expr + ")";
		} else if (type == "float") {
			return expr + ".length() == 0 ? (float)0 : Float.parseFloat(" + expr + ")";
		} else if (type == "double") {
			return expr + ".length() == 0 ? (double)0 : Double.parseDouble(" + expr + ")";
		} else if (type == "Date") {
			if (sqlDate) {
				return "DateHelper.parseSqlDateOrTimestamp(" + expr + ")";
			} else {
				return "DateHelper.parse(" + expr + ")";
			}
		} else {
			return type + ".valueOf(" + expr + ")";
		}
	}

	public static String compareField(String f1, String f2, String type, boolean eq) {
		if (isPrimitiveType(type)) {
			if (eq) {
				return f1 + " == " + f2;
			} else {
				return f1 + " != " + f2;
			}
		} else {
			return (eq ? "" : "!") + f1 + ".equals(" + f2 + ")";
		}
	}

	public static String compareField(String f1, String f2, String type, boolean intern, boolean eq) {
		if (isPrimitiveType(type) || (intern && ("String".equals(type) || "java.lang.String".equals(type)))) {
			if (eq) {
				return f1 + " == " + f2;
			} else {
				return f1 + " != " + f2;
			}
		} else {
			String exp = f1 + " == null ? " + f2 + " == null : " + f1 + ".equals(" + f2 + ")";
			if (!eq) {
				exp = "!(" + exp + ")";
			}
			return exp;
		}
	}

	public static boolean isPrimitiveType(String type) {
		type = type.intern();
		return type == "boolean"
			|| type == "byte"
			|| type == "short"
			|| type == "char"
			|| type == "int"
			|| type == "long"
			|| type == "float"
			|| type == "double";
	}

	public static Element getNodeTypeExt(Element r, String name) {
		Element e = r.getChild(CoreTags.NODE_TYPE_EXT);
		if (e != null) {
			return e.getChild(name + "NodeType");
		} else {
			return null;
		}
	}

	public static Element getPhysicalField(Element schemaField, String confName, String physicalTypeName) {
		List list = schemaField.getChildren(physicalTypeName+"Field");
		if (list == null)
			return null;
		Element	allConfElm = null;
		for (int i=0; i< list.size(); i++) {
			Element et = (Element) list.get(i);
//				System.out.println ("element " + et.getName() + " " + et.getAttributeValue("conf") + " " + et.getAttributeValue("from"));
			String conf = et.getAttributeValue("conf");
			if (conf == null || conf.equals("all"))
				allConfElm = et;
			else {
				if( conf.indexOf(confName) != -1)
//					TODO: add condition preventing one conf substring of another
					return et;
			}	
		}
//		if nothing matches, then return the element with no conf filed because default is - all
		return allConfElm;
	}


	public static Element getNodeTypeExt(Element r, String name, String confName) {
		Element e = r.getChild(CoreTags.NODE_TYPE_EXT);
		if (e != null) {
//			return e.getChild(name + "NodeType");
			
			List list = e.getChildren(name + "NodeType");
//			System.out.println ("list size: " + list.size());

			if (list != null) {
				for (int i=0; i< list.size(); i++) {
					Element et = (Element) list.get(i);
//					System.out.println ("element " + et.getName() + " " + et.getAttributeValue("conf") + " " + et.getAttributeValue("from"));
					String conf = et.getAttributeValue("conf");
					if ((conf != null) && conf.equals(confName)) return et;
				}
				//if nothing matches, then return the first value
				return (Element) list.get(0);
			} else {
				return null;
			}
			
		} else {
			return null;
		}
	}

	public static Element getGlobalExt(Element root, String name) {
		Element e = root.getChild(CoreTags.GLOBAL);
		if (e != null) {
			e = e.getChild(CoreTags.GLOBAL_EXT);
			if (e != null) {
				return e.getChild(name + "Global");
			}
		}
		return null;
	}

	public static List getGlobalExts(Element root, String name) {
		Element e = root.getChild(CoreTags.GLOBAL);
		if (e != null) {
			e = e.getChild(CoreTags.GLOBAL_EXT);
			if (e != null) {
				return e.getChildren(name + "Global");
			}
		}
		return EMPTY_LIST;
	}

	public static Element getFld(DerivedSource conf, Element r, String name) {
		List l = r.getChildren(name + "Field");
		Iterator iL = l.iterator();
		while (iL.hasNext()) {
			Element e = (Element) iL.next();
			if (includesConf(e, conf))
				return e;
		}
		return null;
	}

	public static class Id {
		public String type;
		public String name;
		public Element field;
		Id(String type, String name, Element field) {
			this.type = type;
			this.name = name;
			this.field = field;
		}
	}

	public static Id getId(IGenerator g, Element r, String tpe) {
		String fld = tpe + "Field";
		Element keyField = null;
		List fields = r.getChildren("field");
		Iterator i = fields.iterator();
		while (i.hasNext()) {
			Element e = (Element) i.next();
			Element o = e.getChild(fld);
			if (o != null && "true".equals(o.getAttributeValue("key"))) {
				if (keyField == null) {
					keyField = e;
				} else {
					g.error("Record " + r.getAttributeValue("name") + " must have exactly 1 " + tpe + " key field.");
				}
			}
		}
		if (keyField != null) {
			return new Id(keyField.getAttributeValue("type"), keyField.getAttributeValue("name"), keyField);
		} else {
			// Return a placeholder and hope that it will be replaced
			String msg = "Using placeholder key field 'key' of type 'int'.";
			g.info(msg);
			return new Id("int", "key", null);
		}
	}

	public static void multiFileFieldDeclarations(IGenerator g, Writer w, Element r, String tpe, LinkedList ids)
		throws IOException {
		String className = r.getAttributeValue("className");
		w.write("private " + className + " o__" + className + ";" + Constants.LINE_SEPARATOR);
		if (!ids.isEmpty()) {
			w.write("private LinkedList l__" + className + " = new LinkedList();" + Constants.LINE_SEPARATOR);
		}
		Iterator i = ids.iterator();
		while (i.hasNext()) {
			Id id = (Id) i.next();
			w.write("private " + id.type + " " + className + "__" + id.name + ";" + Constants.LINE_SEPARATOR);
		}
		List records = r.getChildren(CoreTags.NODE_TYPE);
		if (!records.isEmpty()) {
			ids.add(GeneratorHelper.getId(g, r, tpe));
			Iterator iR = records.iterator();
			while (iR.hasNext()) {
				multiFileFieldDeclarations(g, w, (Element) iR.next(), tpe, ids);
			}
			ids.removeLast();
		}
	}

	public static boolean includesConf(Element e, DerivedSource conf) {
		String cName = e.getAttributeValue("conf");
		return cName == null || DerivedSource.valueOf(cName).includes(conf);
	}

	public static String writeNullableString(String s) {
		if (s == null) {
			return "null";
		} else {
			return "\"" + s + "\"";
		}
	}

	public static int getUnknown(Element e, String attributeName, int defaultValue) {
		String att = e.getAttributeValue(attributeName);
		if (att != null) {
			if (att.equals("warn")) {
				return CoreTags.WARN;
			} else if (att.equals("exception")) {
				return CoreTags.EXCEPTION;
			} else if (att.equals("ignore")) {
				return CoreTags.IGNORE;
			}
		}
		return defaultValue;
	}

	public static boolean getBooleanAttribute(Element e, String attributeName, boolean defaultValue) {
		return getBooleanAttribute(e.getAttribute(attributeName), defaultValue);
	}

	public static boolean getBooleanAttribute(Attribute attribute, boolean defaultValue) {
		boolean res = defaultValue;
		if (attribute != null) {
			String val = attribute.getValue();
			if ("true".equalsIgnoreCase(val)) {
				res = true;
			} else if ("false".equalsIgnoreCase(val)) {
				res = false;
			}
		}
		return res;
	}
	/**
	 * Method findNodeType.
	 * @param element
	 * @param targetNodeTypeName
	 * @return Element
	 */
	public static Element findNodeType(Element node, String fqNodeTypeName) {
		int pos = fqNodeTypeName.indexOf('.');
		String s = pos == -1 ? fqNodeTypeName : fqNodeTypeName.substring(0, pos);
		List l = node.getChildren(CoreTags.NODE_TYPE);
		for (Iterator iL = l.iterator(); iL.hasNext();) {
			Element e = (Element) iL.next();
			if (e.getAttributeValue(CoreTags.NAME).equals(s)) {
				if (pos == -1) {
					return e;
				} else {
					return findNodeType(e, fqNodeTypeName.substring(pos + 1));
				}
			}
		}
		return null;
	}
	/**
	 * Method findField.
	 * @param nodeType
	 * @param targetFieldName
	 * @return Element
	 */
	public static Element findField(Element nodeType, String fieldName) {
		List l = nodeType.getChildren(CoreTags.FIELD);
		for (Iterator iL = l.iterator(); iL.hasNext();) {
			Element field = (Element) iL.next();
			if (field.getAttributeValue(CoreTags.NAME).equals(fieldName)) {
				return field;
			}
		}
		return null;
	}

	public static boolean isNodeInitScope(Element field) {
		return CoreTags.NODE_INIT.equals(field.getAttributeValue(CoreTags.SCOPE));
	}
}
