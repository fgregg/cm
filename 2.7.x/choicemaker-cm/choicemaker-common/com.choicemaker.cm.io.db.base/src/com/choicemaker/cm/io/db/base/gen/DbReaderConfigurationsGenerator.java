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
package com.choicemaker.cm.io.db.base.gen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

import com.choicemaker.cm.core.base.Constants;
import com.choicemaker.cm.core.gen.CoreTags;
import com.choicemaker.cm.core.gen.GenException;
import com.choicemaker.cm.core.gen.GeneratorHelper;
import com.choicemaker.cm.core.gen.GeneratorPlugin;
import com.choicemaker.cm.core.gen.IGenerator;

/**
 * Description
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/28 09:06:55 $
 */
public class DbReaderConfigurationsGenerator implements GeneratorPlugin {
	public static DbReaderConfigurationsGenerator instance = new DbReaderConfigurationsGenerator();

	private DbReaderConfigurationsGenerator() {
	}

	private DbReaderConfigurationGenerator[] addConfigurations(IGenerator g) throws IOException, GenException {
		List confList = new ArrayList();
		List dbDefs = GeneratorHelper.getGlobalExts(g.getRootElement(), DbTags.DB);
		Iterator iDbReaderDefs = dbDefs.iterator();
		while (iDbReaderDefs.hasNext()) {
			Element d = (Element) iDbReaderDefs.next();
			DbReaderConfigurationGenerator c = new DbReaderConfigurationGenerator(g, d.getAttributeValue(CoreTags.CONF), d);
			confList.add(c);
			new DbReaderSequentialConfigurationGenerator(g, d.getAttributeValue(CoreTags.CONF), d);
		}
		return (DbReaderConfigurationGenerator[]) confList.toArray(new DbReaderConfigurationGenerator[confList.size()]);
	}

	public synchronized void generate(IGenerator g) throws GenException {
		try {
			DbReaderConfigurationGenerator[] confs = addConfigurations(g);
			addDbConfigurations(g, confs);
			add(g, confs, "Parallel");
			add(g, confs, "Sequential");
		} catch (IOException ex) {
			throw new GenException("Problem writing file.", ex);
		}
	}

	private void addDbConfigurations(IGenerator g, DbReaderConfigurationGenerator[] confs) {
		g.addAccessorBody("private static String[] dbConfigurations = {");
		for (int i = 0; i < confs.length; i++) {
			if(i != 0) {
				g.addAccessorBody(",");
			}
			g.addAccessorBody("\"" + confs[i].confName + "\"");
		}
		g.addAccessorBody("};" + Constants.LINE_SEPARATOR);
		g.addAccessorBody("public String[] getDbConfigurations() {");
		g.addAccessorBody("return dbConfigurations;" + Constants.LINE_SEPARATOR);
		g.addAccessorBody("}" + Constants.LINE_SEPARATOR);
	}

	private void add(IGenerator g, DbReaderConfigurationGenerator[] confs, String suffix) {
		String packageName = g.getPackage() + ".db";
		g.addAccessorBody("public Object getDbReader" + suffix + "(String name) {" + Constants.LINE_SEPARATOR);
		for (int i = 0; i < confs.length; ++i) {
			g.addAccessorBody(
				"if(\""
					+ confs[i].confName
					+ "\".equals(name)) return new "
					+ packageName
					+ "."
					+ confs[i].baseClassName
					+ suffix
					+ "();" + Constants.LINE_SEPARATOR);
		}
		if (confs.length == 0) {
			g.addAccessorBody("return null;" + Constants.LINE_SEPARATOR);
		} else {
			DbGenerator.filesAdded = true;
			g.addAccessorBody("return new " + packageName + "." + confs[0].baseClassName + suffix + "();" + Constants.LINE_SEPARATOR);
		}
		g.addAccessorBody("}" + Constants.LINE_SEPARATOR);
	}

	static String getField(int cursorNo, int fieldNo, String type, boolean intern) {
		String t = type.substring(type.lastIndexOf(".") + 1).intern();
		String c1 = "";
		String c2 = "";
		if (t == "boolean") {
			t = "Boolean";
		} else if (t == "byte") {
			t = "Byte";
		} else if (t == "short") {
			t = "Short";
		} else if (t == "char") {
			t = "String";
			c1 = "com.choicemaker.cm.core.util.StringUtils.getChar(";
			c2 = ")";
		} else if (t == "int") {
			t = "Int";
		} else if (t == "long") {
			t = "Long";
		} else if (t == "float") {
			t = "Float";
		} else if (t == "double") {
			t = "Double";
		} else if (t == "String" | t == "java.lang.String") {
			t = "String";
			if (intern) {
				c1 = "(__tmpStr = ";
				c2 = ") != null ? __tmpStr.intern() : null";
			}
		} else if (t == "Date") {
			t = "Date";
		} else {
			t = "String";
			c1 = "new " + type + "(";
			c2 = ")";
		}
		return c1 + "rs" + (cursorNo == -1 ? "" : ("[" + cursorNo + "]")) + ".get" + t + "(" + fieldNo + ")" + c2;
	}
}
