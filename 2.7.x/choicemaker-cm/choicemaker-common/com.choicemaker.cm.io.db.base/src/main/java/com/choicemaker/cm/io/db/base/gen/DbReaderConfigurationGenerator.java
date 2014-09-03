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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jdom.Element;

import com.choicemaker.cm.core.Constants;
import com.choicemaker.cm.core.DerivedSource;
import com.choicemaker.cm.core.gen.CoreTags;
import com.choicemaker.cm.core.gen.GenException;
import com.choicemaker.cm.core.gen.GeneratorHelper;
import com.choicemaker.cm.core.gen.GeneratorHelper.Id;
import com.choicemaker.cm.core.gen.IGenerator;
import com.choicemaker.cm.io.db.base.DbField;
import com.choicemaker.cm.io.db.base.DbView;
import com.choicemaker.cm.io.db.base.Index;

/**
 * Description
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/28 09:06:55 $
 */
public class DbReaderConfigurationGenerator {
	private static final List EMPTY_LIST = new ArrayList(0);

	private static DerivedSource src = DerivedSource.valueOf("db");

	private int noCursors;
	private IGenerator g;
	String confName;
	private DerivedSource conf;
	private Element def;
	String baseClassName;
	private ArrayList views;
	private int nextViewNum;
	private String masterId;
	private String masterIdType;
	private boolean virtualRootRecord;

	DbReaderConfigurationGenerator(IGenerator g, String confName, Element def) throws GenException {
		this.g = g;
		this.confName = confName;
		this.conf = DerivedSource.valueOf(confName);
		this.def = def;
		generate();
	}

	private void generate() throws GenException {
		views = new ArrayList();
		nextViewNum = 0;
		baseClassName = g.getSchemaName() + "__" + confName + "__DbReader";
		String className = baseClassName + "Parallel";
		String packageName = g.getPackage() + ".db";
		try {
			String directoryName = g.getSourceCodePackageRoot() + File.separator + "db";
			String fileName = directoryName + File.separator + className + ".java";
			g.addGeneratedFile(fileName);
			FileOutputStream fs = new FileOutputStream(new File(fileName).getAbsoluteFile());
			Writer w = new OutputStreamWriter(new BufferedOutputStream(fs));
			w.write("// Generated by ChoiceMaker. Do not edit." + Constants.LINE_SEPARATOR);
			w.write("package " + packageName + ";" + Constants.LINE_SEPARATOR);
			w.write("import java.util.logging.*;" + Constants.LINE_SEPARATOR);
			w.write("import java.util.*;" + Constants.LINE_SEPARATOR);
			w.write("import java.sql.*;" + Constants.LINE_SEPARATOR);
			w.write("import com.choicemaker.cm.core.*;" + Constants.LINE_SEPARATOR);
			w.write("import com.choicemaker.cm.core.base.*;" + Constants.LINE_SEPARATOR);
			w.write("import com.choicemaker.cm.io.db.base.*;" + Constants.LINE_SEPARATOR);
			w.write("import " + g.getPackage() + ".*;" + Constants.LINE_SEPARATOR);
			w.write(g.getImports());
			w.write("public final class " + className + " implements DbReaderParallel {" + Constants.LINE_SEPARATOR);
			w.write("private static Logger logger = Logger.getLogger(" + packageName + "." + className + ".class.getName());" + Constants.LINE_SEPARATOR);
			w.write("private ResultSet[] rs;" + Constants.LINE_SEPARATOR);
			w.write("private static DerivedSource src = DerivedSource.valueOf(\"db\");" + Constants.LINE_SEPARATOR);
			w.write("public String getName() {" + Constants.LINE_SEPARATOR);
			w.write("return \"" + g.getSchemaName() + ":r:" + confName + "\";" + Constants.LINE_SEPARATOR);
			w.write("}" + Constants.LINE_SEPARATOR);
			Element rootRecord = g.getRootRecord();
			GeneratorHelper.multiFileFieldDeclarations(g, w, rootRecord, "db", new LinkedList());
			w.write("public void open(ResultSet[] rs) throws java.sql.SQLException {" + Constants.LINE_SEPARATOR);
			w.write("this.rs = rs;" + Constants.LINE_SEPARATOR);
			noCursors = 0;
			callGetters(g, w, rootRecord);
			w.write("}" + Constants.LINE_SEPARATOR);
			w.write("public Record getNext() throws java.sql.SQLException {" + Constants.LINE_SEPARATOR);
			String rootRecordClassName = rootRecord.getAttributeValue("className");
			w.write("Record __res = o__" + rootRecordClassName + ";" + Constants.LINE_SEPARATOR);
			w.write("__res.computeValidityAndDerived(src);" + Constants.LINE_SEPARATOR);
			w.write("getRecord" + rootRecordClassName + "();" + Constants.LINE_SEPARATOR);
			w.write("return __res;" + Constants.LINE_SEPARATOR);
			w.write("}" + Constants.LINE_SEPARATOR);
			w.write("public boolean hasNext() {" + Constants.LINE_SEPARATOR);
			w.write("return o__" + rootRecordClassName + " != null;" + Constants.LINE_SEPARATOR);
			w.write("}" + Constants.LINE_SEPARATOR);
			w.write("public int getNoCursors() {" + Constants.LINE_SEPARATOR);
			w.write("return NO_CURSORS;" + Constants.LINE_SEPARATOR);
			w.write("}" + Constants.LINE_SEPARATOR);
			w.write("static final int NO_CURSORS = " + noCursors + ";" + Constants.LINE_SEPARATOR);
			noCursors = -1;
			writeGetters(g, w, rootRecord, new LinkedList());
			writeViews(w);
			writeIndices(w);
			w.write("}" + Constants.LINE_SEPARATOR);
			w.flush();
			fs.close();
		} catch (IOException ex) {
			throw new GenException("Problem writing file.", ex);
		}
	}

	private void writeIndices(Writer w) throws IOException {
		w.write("public Map getIndices() {" + Constants.LINE_SEPARATOR);
		w.write("return indices;" + Constants.LINE_SEPARATOR);
		w.write("}" + Constants.LINE_SEPARATOR);
		w.write("private static Map indices = new HashMap();" + Constants.LINE_SEPARATOR);
		w.write("static {" + Constants.LINE_SEPARATOR);
		w.write("Map tableIndices;" + Constants.LINE_SEPARATOR);
		IndexCombinations indexCombinations = new IndexCombinations();
		List tables = def.getChildren("table");
		for (Iterator iTables = tables.iterator(); iTables.hasNext();) {
			Element table = (Element) iTables.next();
			String tableName = table.getAttributeValue(DbTags.NAME);
			w.write("tableIndices = new HashMap();" + Constants.LINE_SEPARATOR);
			w.write("indices.put(\"" + tableName + "\", tableIndices);" + Constants.LINE_SEPARATOR);
			List indexElements = table.getChildren("index");
			Index[] indices = new Index[indexElements.size()];
			int indexNum = 0;
			for (Iterator iIndices = indexElements.iterator(); iIndices.hasNext();) {
				Element index = (Element) iIndices.next();
				String indexName = index.getAttributeValue(DbTags.NAME);
				List fieldElements = index.getChildren("indexField");
				String[] fields = new String[fieldElements.size()];
				int fieldNum = 0;
				for (Iterator iFields = fieldElements.iterator(); iFields.hasNext();) {
					Element field = (Element) iFields.next();
					fields[fieldNum++] = field.getAttributeValue(DbTags.NAME);
				}
				Index idx = new Index(indexName, tableName, fields);
				indices[indexNum++] = idx;
				writeIndex(w, idx);
			}
			IndexCombinations.Combination[] combinations = indexCombinations.getCombinations(indices);
			for (int i = 0; i < combinations.length; i++) {
				IndexCombinations.Combination combination = combinations[i];
				w.write("tableIndices.put(\"" + combination.getFields() + "\", new Index[] {");
				Index[] combIndices = combination.getIndices();
				for (int j = 0; j < combIndices.length; j++) {
					w.write(indexName(combIndices[j]) + ",");
				}
				w.write("});" + Constants.LINE_SEPARATOR);
			}
		}
		w.write("}" + Constants.LINE_SEPARATOR);
	}

	private void writeIndex(Writer w, Index index) throws IOException {
		w.write("Index " + indexName(index) + " = new Index(\"" + index.getName() + "\", \"" + index.getTable() + "\", new String[]{");
		String[] fields = index.getFields();
		for (int i = 0; i < fields.length; i++) {
			w.write("\"" + fields[i] + "\",");
		}
		w.write("});" + Constants.LINE_SEPARATOR);
	}

	private String indexName(Index index) {
		return "idx" + index.getTable() + index.getName();
	}

	private void writeViews(Writer w) throws IOException {
		w.write("public String getMasterId() {" + Constants.LINE_SEPARATOR);
		w.write("return masterId;" + Constants.LINE_SEPARATOR);
		w.write("}" + Constants.LINE_SEPARATOR);
		w.write("static final String masterId = \"" + masterId + "\";" + Constants.LINE_SEPARATOR);
		w.write("static final String masterIdType = \"" + masterIdType + "\";" + Constants.LINE_SEPARATOR);
		w.write("public DbView[] getViews() {" + Constants.LINE_SEPARATOR);
		w.write("return views;" + Constants.LINE_SEPARATOR);
		w.write("}" + Constants.LINE_SEPARATOR);
		w.write("static DbView[] views = {" + Constants.LINE_SEPARATOR);
		int size = views.size();
		for (int i = 0; i < size; ++i) {
			DbView v = (DbView) views.get(i);
			w.write("new DbView(" + v.number + ",");
			writeDbFieldArray(w, v.fields);
			w.write("," + Constants.LINE_SEPARATOR + "\"" + v.from + "\", " + GeneratorHelper.writeNullableString(v.where) + ",");
			writeDbFieldArray(w, v.orderBy);
			w.write(")");
			if (i != size - 1)
				w.write("," + Constants.LINE_SEPARATOR);
		}
		w.write("};" + Constants.LINE_SEPARATOR);
	}

	private void writeDbFieldArray(Writer w, DbField[] fields) throws IOException {
		w.write("new DbField[]{" + Constants.LINE_SEPARATOR);
		int vlen = fields.length;
		for (int j = 0; j < vlen; ++j) {
			DbField d = fields[j];
			w.write("new DbField(" + GeneratorHelper.writeNullableString(d.table) + ", \"" + d.name + "\")");
			if (j != vlen - 1)
				w.write("," + Constants.LINE_SEPARATOR);
		}
		w.write("}");
	}

	private void callGetters(IGenerator g, Writer w, Element r) throws IOException {
		Element hd = GeneratorHelper.getNodeTypeExt(r, "db");
		boolean virtual = false;
		if (hd != null) {
			virtual = "true".equals(hd.getAttributeValue("virtual"));
			virtualRootRecord |= virtual;
			if (virtual && noCursors != 0) {
				g.error("Only root record may be virtual.");
			}
		}
		if (!virtual) {
			++noCursors;
		}
		List records = r.getChildren(CoreTags.NODE_TYPE);
		Iterator iR = records.iterator();
		while (iR.hasNext()) {
			callGetters(g, w, (Element) iR.next());
		}
		String className = r.getAttributeValue("className");
		w.write("getRecord" + className + "();" + Constants.LINE_SEPARATOR);
	}

	private void writeGetters(IGenerator g, Writer w, Element r, LinkedList ids) throws IOException {
		boolean virtual = ids.isEmpty() && virtualRootRecord;
		if (!virtual) {
			++noCursors;
		}
		String recordName = r.getAttributeValue("name");
		String className = r.getAttributeValue("className");
		
		//begin debug
		System.out.println ("DbReaderConfigurationGenerator");
		System.out.println ("confName " + confName + " recordName " + recordName + " className " + className);
/*
		Element test0 = r.getChild("nodeTypeExt");
		List test = test0.getChildren("dbNodeType");
		for (int i=0; i<test.size(); i++) {
			Element et = (Element) test.get(i);
			System.out.println ("element " + et.getName() + " " + et.getAttributeValue("conf") + " " + et.getAttributeValue("from"));
		}
*/
		//end debug
		
		Element hd = GeneratorHelper.getNodeTypeExt(r, "db", confName);
		if (hd == null) {
			g.error("Missing dbHd in record " + recordName + " of configuration " + confName);
			return;
		}
		
		//begin debug
		System.out.println ("element " + hd.getName() + " " + hd.getAttributeValue("conf") + " " + hd.getAttributeValue("from"));
		//end debug
		
		String defaultTable = hd.getAttributeValue("table");
		if (defaultTable == null)
			defaultTable = hd.getAttributeValue("from");
		w.write("private void getRecord" + className + "() throws java.sql.SQLException {" + Constants.LINE_SEPARATOR);
		List records = r.getChildren(CoreTags.NODE_TYPE);
		List keyTables = hd.getChildren("key");
		Id key = null;
		List fields = new ArrayList(r.getChildren("field"));
		
		System.out.println ("fields size: " + fields.size());
		GeneratorHelper.filterFields2(fields, src, "dbField", confName);
		System.out.println ("fields size: " + fields.size());
		
		DbField[] dbFields = new DbField[fields.size() + ids.size()];
		if (!records.isEmpty() || ids.isEmpty()) {
			key = GeneratorHelper.getId(g, r, "db");
		}
		if (virtual) {
			Element nestedRecord = (Element) records.iterator().next();
			String nestedClassName = nestedRecord.getAttributeValue("className");
			w.write("o__" + className + " = new " + className + "();" + Constants.LINE_SEPARATOR);
			w.write(key.type + " curId = " + nestedClassName + "__" + key.name + ";" + Constants.LINE_SEPARATOR);
			w.write("while(o__" + nestedClassName + " != null && curId == " + nestedClassName + "__" + key.name + ") {" + Constants.LINE_SEPARATOR);
			w.write("l__" + nestedClassName + ".add(o__" + nestedClassName + ");" + Constants.LINE_SEPARATOR);
			w.write("o__" + nestedClassName + ".outer = o__" + className + ";" + Constants.LINE_SEPARATOR);
			w.write("getRecord" + nestedClassName + "();" + Constants.LINE_SEPARATOR);
			w.write("}" + Constants.LINE_SEPARATOR);
			w.write("if(l__" + nestedClassName + ".size() != 0) {" + Constants.LINE_SEPARATOR);
			w.write("o__" + className + "." + key.name + " = curId;" + Constants.LINE_SEPARATOR);
			w.write(
				"l__"
					+ nestedClassName
					+ ".toArray(o__"
					+ className
					+ "."
					+ nestedRecord.getAttributeValue("name")
					+ " = new "
					+ nestedClassName
					+ "[l__"
					+ nestedClassName
					+ ".size()]);"
					+ Constants.LINE_SEPARATOR);
			w.write("l__" + nestedClassName + ".clear();" + Constants.LINE_SEPARATOR);
		} else {
			w.write("String __tmpStr;" + Constants.LINE_SEPARATOR);
			w.write("if(rs[" + noCursors + "].next()) {" + Constants.LINE_SEPARATOR);
			w.write("o__" + className + " = new " + className + "();" + Constants.LINE_SEPARATOR);
			int fieldNo = 0;
			Iterator iL = ids.iterator();
			while (iL.hasNext()) {
				++fieldNo;
				Id id = (Id) iL.next();
				w.write(className + "__" + id.name + " = " + getField(noCursors, fieldNo, id.type, g.isIntern()) + ";" + Constants.LINE_SEPARATOR);
				dbFields[fieldNo - 1] = getDbField(id.field, defaultTable, keyTables);
			}
			Iterator iF = fields.iterator();
			while (iF.hasNext()) {
				++fieldNo;
				Element field = (Element) iF.next();
				String fieldName = field.getAttributeValue("name");
				w.write("o__" + className + "." + fieldName + " = ");
				w.write(getField(noCursors, fieldNo, field.getAttributeValue("type"), g.isIntern()) + ";" + Constants.LINE_SEPARATOR);
				dbFields[fieldNo - 1] = getDbField(field, defaultTable, EMPTY_LIST);
			}
			if (!records.isEmpty()) {
				Iterator iR = records.iterator();
				while (iR.hasNext()) {
					Element e = (Element) iR.next();
					String eClassName = e.getAttributeValue("className");
					w.write("while(o__" + eClassName + " != null");
					Iterator iI = ids.iterator();
					while (iI.hasNext()) {
						Id id = (Id) iI.next();
						w.write(" && " + GeneratorHelper.compareField(className + "__" + id.name, eClassName + "__" + id.name, id.type, true));
					}
					w.write(" && " + GeneratorHelper.compareField("o__" + className + "." + key.name, eClassName + "__" + key.name, key.type, true));
					w.write(") {" + Constants.LINE_SEPARATOR);
					w.write("l__" + eClassName + ".add(o__" + eClassName + ");" + Constants.LINE_SEPARATOR);
					w.write("o__" + eClassName + ".outer = o__" + className + ";" + Constants.LINE_SEPARATOR);
					w.write("getRecord" + eClassName + "();" + Constants.LINE_SEPARATOR);
					w.write("}" + Constants.LINE_SEPARATOR);
					w.write("if(l__" + eClassName + ".size() == 0) {" + Constants.LINE_SEPARATOR);
					w.write("o__" + className + "." + e.getAttributeValue("name") + " = " + eClassName + ".__zeroArray;" + Constants.LINE_SEPARATOR);
					w.write("} else {" + Constants.LINE_SEPARATOR);
					w.write(
						"l__"
							+ eClassName
							+ ".toArray(o__"
							+ className
							+ "."
							+ e.getAttributeValue("name")
							+ " = new "
							+ eClassName
							+ "[l__"
							+ eClassName
							+ ".size()]);"
							+ Constants.LINE_SEPARATOR);
					w.write("l__" + eClassName + ".clear();" + Constants.LINE_SEPARATOR);
					w.write("}" + Constants.LINE_SEPARATOR);
				}
			}
		}
		w.write("} else {" + Constants.LINE_SEPARATOR);
		w.write("o__" + className + " = null;" + Constants.LINE_SEPARATOR);
		w.write("}" + Constants.LINE_SEPARATOR);
		w.write("}" + Constants.LINE_SEPARATOR);
		int idsize = ids.size();
		List viewsForHd = hd.getChildren("view");
		int numViews = viewsForHd.size();
		int numIds = idsize + (records.isEmpty() ? 0 : 1);
		DbView[] dbvs;
		if (hd.getAttribute("from") != null) {
			DbView dbv = new DbView(nextViewNum, dbFields, hd.getAttributeValue("from"), hd.getAttributeValue("where"), new DbField[numIds]);
			views.add(dbv);
			dbvs = new DbView[numViews + 1];
			dbvs[numViews] = dbv;
		} else {
			dbvs = new DbView[numViews];
		}
		for (int j = 0; j < numViews; ++j) {
			Element v = (Element) viewsForHd.get(j);
			DbView dbv = new DbView(nextViewNum, dbFields, v.getAttributeValue("from"), v.getAttributeValue("where"), new DbField[numIds]);
			views.add(dbv);
			dbvs[j] = dbv;
		}
		if (!virtual) {
			++nextViewNum;
		}
		for (int i = 0; i < idsize; ++i) {
			DbField f = getOrderField((Id) ids.get(i), defaultTable, keyTables);
			for (int j = 0; j < dbvs.length; ++j) {
				dbvs[j].orderBy[i] = f;
			}
		}
		if (ids.isEmpty()) {
			masterId = getOrderField(key, defaultTable, keyTables).name;
			Element dbf = GeneratorHelper.getFld(conf, key.field, "db");
			if (dbf != null) {
				masterIdType = dbf.getAttributeValue("type");
			}
			if (masterIdType == null) {
				g.error("The master id must have a type.");
			}
		}
		if (!records.isEmpty()) {
			DbField f = getOrderField(key, defaultTable, keyTables);
			for (int j = 0; j < dbvs.length; ++j) {
				dbvs[j].orderBy[dbvs[j].orderBy.length - 1] = f;
			}
			ids.add(key);
			Iterator iR = records.iterator();
			while (iR.hasNext()) {
				writeGetters(g, w, (Element) iR.next(), ids);
			}
			ids.removeLast();
		}
	}

	private DbField getOrderField(Id id, String defaultTable, List keyTables) {
		String column = id.field.getAttributeValue("name");
		Element dbf = GeneratorHelper.getFld(conf, id.field, "db");
		if (dbf != null) {
			column = ifNotNull(column, dbf.getAttributeValue("name"));
		}
		String table = getTableName(defaultTable, column, keyTables);
		return new DbField(table, column);
	}

	private DbField getDbField(Element fld, String defaultTable, List keyTables) {
		String table = defaultTable;
		String column = fld.getAttributeValue("name");
		Element dbf = GeneratorHelper.getFld(conf, fld, "db");
		if (dbf != null) {
			table = ifNotNull(table, dbf.getAttributeValue("table"));
			column = ifNotNull(column, dbf.getAttributeValue("name"));
		}
		table = getTableName(table, column, keyTables);
		return new DbField(table, column);
	}

	private String getTableName(String defaultName, String column, List keyTables) {
		Iterator iKeyTables = keyTables.iterator();
		while (iKeyTables.hasNext()) {
			Element k = (Element) iKeyTables.next();
			if (column.equals(k.getAttributeValue("name"))) {
				return k.getAttributeValue("table");
			}
		}
		return defaultName;
	}

	private String ifNotNull(String def, String val) {
		return val != null ? val : def;
	}

	private String getField(int cursorNo, int fieldNo, String type, boolean intern) {
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
		return c1 + "rs[" + cursorNo + "].get" + t + "(" + fieldNo + ")" + c2;
	}
}
