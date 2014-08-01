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

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

import com.choicemaker.cm.core.Constants;
import com.choicemaker.cm.core.gen.CoreTags;
import com.choicemaker.cm.core.gen.GenException;
import com.choicemaker.cm.core.gen.GeneratorHelper;
import com.choicemaker.cm.core.gen.GeneratorPlugin;
import com.choicemaker.cm.core.gen.IGenerator;

/**
 * Main generator plugin for Db IO.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/28 09:06:55 $
 */
public class DbGenerator implements GeneratorPlugin {
	static boolean filesAdded;

	public void generate(IGenerator g) throws GenException {
		filesAdded = false;
		g.addAccessorImport("import com.choicemaker.cm.io.db.base.*;" + Constants.LINE_SEPARATOR);
		g.addAccessorImplements(", com.choicemaker.cm.io.db.base.DbAccessor");
		String directoryName = g.getSourceCodePackageRoot() + File.separator + "db";
		new File(directoryName).mkdir();
		DbReaderConfigurationsGenerator.instance.generate(g);
		if (filesAdded) {
			g.addAccessorImport("import " + g.getPackage() + ".db.*;" + Constants.LINE_SEPARATOR);
		}
	}

	
	public static String getViewNameForField(Element field, String conf, IGenerator g) {
		String name = null;
		//Element dbField = field.getChild(DbTags.DB_FIELD);
		Element dbField = GeneratorHelper.getPhysicalField(field, conf, DbTags.DB); 
		if (dbField != null) {
			name = dbField.getAttributeValue(DbTags.TABLE);
		}
		if (name == null) {
			name = getViewNameForNode(field.getParent(), conf, g);
		}
		return name;
	}

	public static String getViewNameForNode(Element node, String conf, IGenerator g) {
		String name = null;
		Element dbNodeType = GeneratorHelper.getNodeTypeExt(node, DbTags.DB, conf);
		if (dbNodeType != null) {
			if (dbNodeType.getAttribute(DbTags.WHERE) == null) {
				name = dbNodeType.getAttributeValue(DbTags.FROM);
			} else {
				int num = Integer.parseInt(node.getAttributeValue(CoreTags.RECORD_NUMBER));
				if(isVirtual(g.getRootRecord())) {
					--num;
				}
				name = "vw_cmt_" + g.getSchemaName() + "_r_" + conf + num;
			}
		}
		return name;
	}
	
	private static boolean isVirtual(Element rootNode) {
		Element hd = GeneratorHelper.getNodeTypeExt(rootNode, "db");
		if (hd != null) {
			return "true".equals(hd.getAttributeValue("virtual"));
		}
		return false;
	}

	public static String[] getDbConfigurations(IGenerator g) {
		List dbGlobal = GeneratorHelper.getGlobalExts(g.getRootElement(), DbTags.DB);
		String[] res = new String[dbGlobal.size()];
		Iterator iDbGlobal = dbGlobal.iterator();
		for (int i = 0; i < res.length; i++) {
			res[i] = ((Element)iDbGlobal.next()).getAttributeValue(CoreTags.CONF);
		}
		return res;
	}
}
