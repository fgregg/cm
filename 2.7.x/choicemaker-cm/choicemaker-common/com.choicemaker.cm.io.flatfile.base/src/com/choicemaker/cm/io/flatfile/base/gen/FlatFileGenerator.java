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
package com.choicemaker.cm.io.flatfile.base.gen;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;

import com.choicemaker.cm.core.Constants;
import com.choicemaker.cm.core.gen.CoreTags;
import com.choicemaker.cm.core.gen.GenException;
import com.choicemaker.cm.core.gen.IGenerator;
import com.choicemaker.cm.core.gen.GeneratorHelper;
import com.choicemaker.cm.core.gen.GeneratorPlugin;

/**
 * Main generator plugin for Flat File IO.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:58 $
 */
public class FlatFileGenerator implements GeneratorPlugin {
	public static final char DEFAULT_NULL_REPRESENTATION = ' ';

	private int num;
	private Set recordTypes;
	private List typeNames;

	public void generate(IGenerator g) throws GenException {
		recordTypes = new TreeSet();
		num = -1;
		typeNames = new ArrayList();
		Element ffDef = GeneratorHelper.getGlobalExt(g.getRootElement(), "flatfile");
		String baseFileName = "";
		if (ffDef != null) {
			String t = ffDef.getAttributeValue("multiFileName");
			if (t != null) {
				baseFileName = t;
			}
		}
		typeNames.add(baseFileName);
		setRecordType(g, g.getRootRecord());
		writeFileNames(g);
		g.addAccessorImport("import com.choicemaker.cm.io.flatfile.base.*;" + Constants.LINE_SEPARATOR);
		g.addAccessorImport("import " + g.getPackage() + ".flatfile.*;" + Constants.LINE_SEPARATOR);
		g.addAccessorImplements(", com.choicemaker.cm.io.flatfile.base.FlatFileAccessor");
		int[] descWidths = { 2, 10, 10, 6, 10, 32, 32, 32 };
		Element ffdef = GeneratorHelper.getGlobalExt(g.getRootElement(), "flatfile");
		if (ffdef != null) {
			Element dw = ffdef.getChild("descWidths");
			if (dw != null) {
				List atts = dw.getAttributes();
				Iterator iAtts = atts.iterator();
				while (iAtts.hasNext()) {
					Attribute a = (Attribute) iAtts.next();
					String name = a.getName().intern();
					try {
						if (name == "tag") {
							descWidths[0] = a.getIntValue();
						} else if (name == "id") {
							descWidths[2] = descWidths[1] = a.getIntValue();
						} else if (name == "decision") {
							descWidths[3] = a.getIntValue();
						} else if (name == "date") {
							descWidths[4] = a.getIntValue();
						} else if (name == "user") {
							descWidths[5] = a.getIntValue();
						} else if (name == "src") {
							descWidths[6] = a.getIntValue();
						} else if (name == "comment") {
							descWidths[7] = a.getIntValue();
						}
					} catch (DataConversionException ex) {
						g.error("Invalid value for " + name + ": " + a.getValue());
					}
				}
			}
		}
		g.addAccessorBody("private static int[] descWidths = {");
		for (int i = 0; i < descWidths.length; ++i) {
			if (i != 0) {
				g.addAccessorBody(", ");
			}
			g.addAccessorBody(String.valueOf(descWidths[i]));
		}
		g.addAccessorBody("};" + Constants.LINE_SEPARATOR);
		g.addAccessorBody("public int[] getDescWidths() {" + Constants.LINE_SEPARATOR);
		g.addAccessorBody("return descWidths;" + Constants.LINE_SEPARATOR);
		g.addAccessorBody("}" + Constants.LINE_SEPARATOR);
		String directoryName = g.getSourceCodePackageRoot() + File.separator + "flatfile";
		new File(directoryName).getAbsoluteFile().mkdir();
		FlatFileSingleFileReaderGenerator.instance.generate(g);
		FlatFileMultiFileReaderGenerator.instance.generate(g);
		FlatFileRecordOutputterGenerator.instance.generate(g);
	}

	private void setRecordType(IGenerator g, Element r) {
		++num;
		Element hd = r.getChild(CoreTags.NODE_TYPE_EXT);
		if (hd == null) {
			hd = new Element(CoreTags.NODE_TYPE_EXT);
			r.addContent(hd);
		}
		Element ffhd = hd.getChild("flatfileNodeType");
		if (ffhd == null) {
			ffhd = new Element("flatfileNodeType");
			hd.addContent(ffhd);
		}
		String recordType = ffhd.getAttributeValue("tag");
		if (recordType == null) {
			recordType = String.valueOf(num);
			ffhd.setAttribute("tag", recordType);
		}
		if (!recordTypes.add(recordType)) {
			g.error("Duplicate record type tag: " + recordType);
		}
		String multiFileName = ffhd.getAttributeValue("multiFileName");
		if (multiFileName == null) {
			multiFileName = r.getAttributeValue("name");
		}
		typeNames.add(multiFileName);
		Iterator i = r.getChildren(CoreTags.NODE_TYPE).iterator();
		while (i.hasNext()) {
			setRecordType(g, (Element) i.next());
		}
	}

	private void writeFileNames(IGenerator g) {
		g.addAccessorBody("public String[] getFlatFileFileNames() {" + Constants.LINE_SEPARATOR);
		g.addAccessorBody("return flatFileFileNames;" + Constants.LINE_SEPARATOR);
		g.addAccessorBody("}" + Constants.LINE_SEPARATOR);
		g.addAccessorBody("private static final String[] flatFileFileNames = {");
		Iterator iTypeNames = typeNames.iterator();
		boolean firstTypeName = true;
		while (iTypeNames.hasNext()) {
			if (firstTypeName) {
				firstTypeName = false;
			} else {
				g.addAccessorBody(",");
			}
			g.addAccessorBody("\"" + iTypeNames.next() + "\"");
		}
		g.addAccessorBody("};" + Constants.LINE_SEPARATOR);
	}

	static int defaultWidth(String type) {
		String t = type.substring(type.lastIndexOf(".") + 1).intern();
		if (t == "boolean") {
			return 5;
		} else if (t == "byte") {
			return 4;
		} else if (t == "short") {
			return 6;
		} else if (t == "char") {
			return 1;
		} else if (t == "int") {
			return 11;
		} else if (t == "long") {
			return 20;
		} else if (t == "float") {
			return 16;
		} else if (t == "double") {
			return 22;
		} else if (t == "Date") {
			return 10;
		} else {
			return 32;
		}
	}

	static String getField(String tok, String type, int start, int width, boolean trim, boolean valueOf, char nullRepresentation, boolean intern) {
		String t = type.substring(type.lastIndexOf(".") + 1).intern();
		String c1 = "";
		String c2 = "";
		String c3 = "";
		if (t == "boolean") {
			t = "Boolean";
		} else if (t == "byte") {
			t = "Byte";
		} else if (t == "short") {
			t = "Short";
		} else if (t == "char") {
			t = "Char";
			c2 = ", '" + charToCode(nullRepresentation) + "'";
		} else if (t == "int") {
			t = "Int";
		} else if (t == "long") {
			t = "Long";
		} else if (t == "float") {
			t = "Float";
		} else if (t == "double") {
			t = "Double";
		} else if (t == "String" || t == "java.lang.String") {
			if (intern) {
				t = trim ? "InternedTrimedString" : "InternedString";
			} else {
				t = trim ? "TrimedString" : "String";
			}
		} else if (t == "Date") {
			t = "Date";
		} else {
			t = trim ? "TrimedString" : "String";
			c1 = valueOf ? type + ".valueOf(" : "new " + type + "(";
			c3 = ")";
		}
		if(start == Integer.MIN_VALUE) {
		return c1 + tok + ".next" + t + "(" + width + c2 + ")" + c3;
		} else {
			return c1 + tok + ".get" + t + "(" + start + ", " + width + c2 + ")" + c3;
		}
	}

	public static String charToCode(char c) {
		if(c == '\n') {
			return "\\n";
		} else if(c == '\r') {
			return "\\r";
		} else {
			String s = Integer.toHexString(c);
			return "\\u" + "0000".substring(s.length()) + s;
		}
	}
}
