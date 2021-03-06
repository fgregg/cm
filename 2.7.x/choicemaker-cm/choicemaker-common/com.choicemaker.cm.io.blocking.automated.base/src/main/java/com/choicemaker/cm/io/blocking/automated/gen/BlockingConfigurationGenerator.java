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
package com.choicemaker.cm.io.blocking.automated.gen;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jdom.Element;

import com.choicemaker.cm.core.Constants;
import com.choicemaker.cm.core.DerivedSource;
import com.choicemaker.cm.core.gen.CoreTags;
import com.choicemaker.cm.core.gen.GenException;
import com.choicemaker.cm.core.gen.GeneratorHelper;
import com.choicemaker.cm.core.gen.IGenerator;
import com.choicemaker.cm.io.blocking.base.gen.BlockingTags;
import com.choicemaker.cm.io.db.base.gen.DbGenerator;
import com.choicemaker.cm.io.db.base.gen.DbTags;
;

/**
 * Description
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/28 09:32:54 $
 */
public class BlockingConfigurationGenerator {
	IGenerator g;
	String name;
	int defaultCount;
	Element blockingGlobal;
	String className;
	DerivedSource conf;
	List<?> illegalCombinations; // @todo
	Writer w;
	Set<String> usedRecords;
	String uniqueId;
	DbC[] dbCs;
	boolean generated;

	private static class DbC {
		QField qf;
		List<QField> qfs = new ArrayList<>();
		List<DView> dbtbs = new ArrayList<>();
		List<DField> dbfs = new ArrayList<>();
		List<BField> bfs = new ArrayList<>();
		String name;
		int number;
		DbC(String name, int number) {
			this.name = name;
			this.number = number;
		}
	}

	BlockingConfigurationGenerator(IGenerator g, String name, int defaultCount, Element def)
		throws IOException, GenException {
		this.g = g;
		this.name = name;
		this.className = g.getSchemaName() + "__" + name + "__BlockingConfiguration";
		this.defaultCount = defaultCount;
		this.blockingGlobal = def;
		this.conf = DerivedSource.valueOf(name);
		illegalCombinations = new ArrayList<>();
		usedRecords = new HashSet<>();
		initDbCs();
		if(dbCs.length > 0) {
			generated = true;
			generate();
		} else {
			generated = false;
		}
	}

	private void initDbCs() {
		String[] dbConfs = DbGenerator.getDbConfigurations(g);
		DerivedSource dbConf = DerivedSource.valueOf(blockingGlobal.getAttributeValue(BlockingTags.DB_CONF));
		List<DbC> l = new ArrayList<>();
		for (int i = 0; i < dbConfs.length; i++) {
			if(dbConf.includes(dbConfs[i])) {
				l.add(new DbC(dbConfs[i], i));
			}
		}
		dbCs = (DbC[]) l.toArray(new DbC[l.size()]);
	}

	private void generate() throws IOException, GenException {
		String directoryName = g.getSourceCodePackageRoot() + File.separator + "blocking";
		String fileName = directoryName + File.separator + className + ".java";
		g.addGeneratedFile(fileName);
		FileOutputStream fs = new FileOutputStream(new File(fileName).getAbsoluteFile());
		w = new OutputStreamWriter(new BufferedOutputStream(fs));
		w.write("// Generated by ChoiceMaker. Do not edit." + Constants.LINE_SEPARATOR);
		String packageName = g.getPackage() + ".blocking";
		w.write("package " + packageName + ";" + Constants.LINE_SEPARATOR);
		w.write("import java.util.logging.*;" + Constants.LINE_SEPARATOR);
		w.write("import java.util.*;" + Constants.LINE_SEPARATOR);
		w.write("import com.choicemaker.cm.core.*;" + Constants.LINE_SEPARATOR);
		w.write("import com.choicemaker.cm.core.base.*;" + Constants.LINE_SEPARATOR);
		w.write("import com.choicemaker.cm.io.blocking.automated.*;" + Constants.LINE_SEPARATOR);
		w.write("import com.choicemaker.cm.io.blocking.automated.base.*;" + Constants.LINE_SEPARATOR);
		w.write("import " + g.getPackage() + ".*;" + Constants.LINE_SEPARATOR);
		w.write(g.getImports());
		w.write("public final class " + className + " extends BlockingConfiguration {" + Constants.LINE_SEPARATOR);
		w.write(
			"private static Logger logger = Logger.getLogger("
				+ packageName
				+ "."
				+ className
				+ ".class.getName());"
				+ Constants.LINE_SEPARATOR);
		w.write("public " + className + "(String dbConf) {" + Constants.LINE_SEPARATOR);
		w.write("int dbConfIndex;" + Constants.LINE_SEPARATOR);
		for (int i = 0; i < dbCs.length; i++) {
			w.write("if(\"" + dbCs[i].name + "\".equals(dbConf)) {" + Constants.LINE_SEPARATOR);
			w.write("dbConfIndex = " + i + ";" + Constants.LINE_SEPARATOR);
			w.write(" } else ");
		}
		w.write("{" + Constants.LINE_SEPARATOR);
		w.write("throw new IllegalArgumentException(\"dbConf: \" + dbConf);" + Constants.LINE_SEPARATOR);
		w.write("}" + Constants.LINE_SEPARATOR);
		w.write("dbTables = dbConfigurations[dbConfIndex].dbts;" + Constants.LINE_SEPARATOR);
		w.write("dbFields = dbConfigurations[dbConfIndex].dbfs;" + Constants.LINE_SEPARATOR);
		w.write("blockingFields = dbConfigurations[dbConfIndex].bfs;" + Constants.LINE_SEPARATOR);
		w.write("name = dbConfigurations[dbConfIndex].name;" + Constants.LINE_SEPARATOR);
		w.write("}" + Constants.LINE_SEPARATOR);
		w.write("public IBlockingValue[] createBlockingValues(Record q) {" + Constants.LINE_SEPARATOR);
		w.write("init(NUM_BLOCKING_FIELDS);" + Constants.LINE_SEPARATOR);
		Element rootRecord = g.getRootRecord();
		String rcn = rootRecord.getAttributeValue("className");
		w.write("add" + rcn + "((" + rcn + ")q);" + Constants.LINE_SEPARATOR);
		w.write("return unionValues();" + Constants.LINE_SEPARATOR);
		w.write("}" + Constants.LINE_SEPARATOR);
		boolean used = computeUsedRecords(rootRecord);
		if (!used) {
			g.error("No blocking fields in configuration " + conf);
		} else {
			processRecord(rootRecord, rootRecord.getAttributeValue("name"));
			writeFields();
			illegalCombinations();
		}
		w.write("}" + Constants.LINE_SEPARATOR);
		w.close();
		fs.close();
	}

	private void illegalCombinations() throws IOException {
		// dependency to illegal
		// multi-dependency transitive closure missing
		for (int k = 0; k < dbCs.length; ++k) {
			DbC dbC = dbCs[k];
			Iterator<BField> iBfs = dbC.bfs.iterator();
			while (iBfs.hasNext()) {
				BField bf = (BField) iBfs.next();
				DField df = bf.dfield;
				QField qf = bf.qfield;
				BField[] base = bf.base;
				if (base.length != 0) {
					df.addIllegal(toDbf(base, null, null, 0));
					qf.addIllegal(toDbf(base, null, null, 1));
					for (int i = 0; i < base.length; ++i) {
						BField c = base[i];
						c.dfield.addIllegal(toDbf(base, c, bf, 0));
						c.qfield.addIllegal(toDbf(base, c, bf, 1));
					}
				}
			}
			// custom
			@SuppressWarnings("unchecked")
			List<Element> illegal = blockingGlobal.getChildren(BlockingTags.ILLEGAL_COMBINATION);
			Iterator<Element> iIllegal = illegal.iterator();
			while (iIllegal.hasNext()) {
				Element ic = (Element) iIllegal.next();
				@SuppressWarnings("unchecked")
				List<Element> fields = ic.getChildren();
				int size = fields.size();
				IField[][] ics = new IField[size][size - 1];
				for (int i = 0; i < size; ++i) {
					IField f = getField(dbC, (Element) fields.get(i));
					if (f != null) {
						f.addIllegal(ics[i]);
						for (int j = 0; j < i; ++j) {
							ics[j][i - 1] = f;
						}
						for (int j = i + 1; j < size; ++j) {
							ics[j][i] = f;
						}
					}
				}
			}
			// write
			if (!g.hasErrors()) {
				w.write("static {" + Constants.LINE_SEPARATOR);
				writeIllegal(dbC, dbC.qfs);
				writeIllegal(dbC, dbC.dbfs);
				writeIllegal(dbC, dbC.bfs);
				w.write("}" + Constants.LINE_SEPARATOR);
			}
		}
	}

	private IField getField(DbC dbC, Element e) {
		String type = e.getName().intern();
		if (type == BlockingTags.SOURCE_FIELD_REF) {
			return getQField(
				dbC,
				e.getAttributeValue(BlockingTags.SOURCE_NODE_TYPE_NAME),
				e.getAttributeValue(BlockingTags.SOURCE_FIELD_NAME));
		} else if (type == BlockingTags.BLOCKING_FIELD_REF) {
			String sourceNodeTypeName = e.getAttributeValue(BlockingTags.SOURCE_NODE_TYPE_NAME);
			String sourceFieldName = e.getAttributeValue(BlockingTags.SOURCE_FIELD_NAME);
			String targetNodeTypeName = e.getAttributeValue(BlockingTags.TARGET_NODE_TYPE_NAME);
			String targetFieldName = e.getAttributeValue(BlockingTags.TARGET_FIELD_NAME);
			return getBField(
				dbC,
				sourceNodeTypeName,
				sourceFieldName,
				targetNodeTypeName != null ? targetNodeTypeName : sourceNodeTypeName,
				targetFieldName != null ? targetFieldName : sourceFieldName,
				true);
		} else if (type == BlockingTags.TARGET_FIELD_REF) {
			return getDField(
				dbC,
				e.getAttributeValue(BlockingTags.TARGET_NODE_TYPE_NAME),
				e.getAttributeValue(BlockingTags.TARGET_FIELD_NAME));
		}
		g.error("Unknown field type: " + type);
		return null;
	}

	private void writeIllegal(DbC dbC, List<?> qfs) throws IOException {
		Iterator<?> iL = qfs.iterator();
		while (iL.hasNext()) {
			IField f = (IField) iL.next();
			if (!f.illegal.isEmpty()) {
				w.write(
					"dbConfigurations["
						+ dbC.number
						+ "]."
						+ f
						+ ".illegalCombinations = new Field[][] {"
						+ Constants.LINE_SEPARATOR);
				int si = f.illegal.size();
				for (int i = 0; i < si; ++i) {
					if (i != 0)
						w.write("," + Constants.LINE_SEPARATOR);
					w.write("{");
					IField[] ic = (IField[]) f.illegal.get(i);
					for (int j = 0; j < ic.length; ++j) {
						if (j != 0)
							w.write(", ");
						w.write("dbConfigurations[" + dbC.number + "]." + ic[j].toString());
					}
					w.write("}");
				}
				w.write(Constants.LINE_SEPARATOR + "};" + Constants.LINE_SEPARATOR);
			}
		}
	}

	private IField[] toDbf(BField[] base, BField replace, BField with, int ifn) {
		IField[] res = new IField[base.length];
		for (int i = 0; i < base.length; ++i) {
			BField b = base[i];
			if (b == replace) {
				res[i] = with.getIField(ifn);
			} else {
				res[i] = b.getIField(ifn);
			}
		}
		return res;
	}

	private void writeFields() throws IOException {
		w.write("private static final BlockingConfiguration.DbConfiguration[] dbConfigurations = new BlockingConfiguration.DbConfiguration[" + dbCs.length + "];" + Constants.LINE_SEPARATOR);
		w.write("static {" + Constants.LINE_SEPARATOR);
		w.write("QueryField[] qfs;" + Constants.LINE_SEPARATOR);
		w.write("DbTable[] dbts;" + Constants.LINE_SEPARATOR);
		w.write("DbField[] dbfs;" + Constants.LINE_SEPARATOR);
		w.write("BlockingField[] bfs;" + Constants.LINE_SEPARATOR);
		// QueryFields
		for (int k = 0; k < dbCs.length; ++k) {
			DbC dbC = dbCs[k];
			w.write("qfs = new QueryField[] {" + Constants.LINE_SEPARATOR);
			Iterator<QField> iQfs = dbC.qfs.iterator();
			while (iQfs.hasNext()) {
				QField q = (QField) iQfs.next();
				w.write((q.number != 0 ? "," + Constants.LINE_SEPARATOR : "") + "new QueryField()");
			}
			w.write(Constants.LINE_SEPARATOR + "};" + Constants.LINE_SEPARATOR);
			// DbTable
			if (uniqueId == null) {
				g.error("Root record must have key.");
			}
			w.write("dbts = new DbTable[] {" + Constants.LINE_SEPARATOR);
			Iterator<DView> iDbts = dbC.dbtbs.iterator();
			while (iDbts.hasNext()) {
				DView d = (DView) iDbts.next();
				w.write(
					(d.number != 0 ? "," + Constants.LINE_SEPARATOR : "")
						+ "new DbTable(\""
						+ d.name
						+ "\", "
						+ d.number
						+ ", \""
						+ uniqueId
						+ "\")");
			}
			w.write(Constants.LINE_SEPARATOR + "};" + Constants.LINE_SEPARATOR);
			// DbField
			w.write("dbfs = new DbField[] {" + Constants.LINE_SEPARATOR);
			Iterator<DField> iDbfs = dbC.dbfs.iterator();
			while (iDbfs.hasNext()) {
				DField d = (DField) iDbfs.next();
				w.write(
					(d.number != 0 ? "," + Constants.LINE_SEPARATOR : "")
						+ "new DbField("
						+ d.number
						+ ", \""
						//+ d.targetFieldName elmus
						+ d.targetColumnName
						+ "\", \""
						+ d.type
						+ "\", dbts["
						+ d.view.number
						+ "], "
						+ d._defaultCount
						+ ")");
			}
			w.write(Constants.LINE_SEPARATOR + "};" + Constants.LINE_SEPARATOR);
			// BlockingField
			w.write("bfs = new BlockingField[] {" + Constants.LINE_SEPARATOR);
			Iterator<BField> iBfs = dbC.bfs.iterator();
			while (iBfs.hasNext()) {
				BField b = (BField) iBfs.next();
				w.write(
					(b.number != 0 ? "," + Constants.LINE_SEPARATOR : "")
						+ "new BlockingField("
						+ b.number
						+ ", qfs["
						+ b.qfield.number
						+ "], dbfs["
						+ b.dfield.number
						+ "], \""
						+ b.group
						+ "\")");
			}
			w.write(Constants.LINE_SEPARATOR + "};" + Constants.LINE_SEPARATOR);
			w.write(
				"dbConfigurations[" + k + "] = new DbConfiguration(\""
					+ g.getSchemaName()
					+ ":b:"
					+ name
					+ ":"
					+ dbC.name
					+ "\", qfs, dbts, dbfs, bfs);"
					+ Constants.LINE_SEPARATOR);
		}
		w.write("};" + Constants.LINE_SEPARATOR);
		// others
		int size = dbCs[0].bfs.size();
		w.write("private static final int NUM_BLOCKING_FIELDS = " + size + ";" + Constants.LINE_SEPARATOR);
		for (int i = 0; i < size; ++i) {
			w.write("private IBlockingValue __l" + i + ";" + Constants.LINE_SEPARATOR);
		}
	}

	private boolean computeUsedRecords(Element r) {
		boolean used = false;
		@SuppressWarnings("unchecked")
		List<Element> records = r.getChildren(CoreTags.NODE_TYPE);
		Iterator<Element> iRecords = records.iterator();
		while (iRecords.hasNext()) {
			used |= computeUsedRecords((Element) iRecords.next());
		}
		if (!used) {
			@SuppressWarnings("unchecked")
			List<Element> fields = r.getChildren("field");
			Iterator<Element> iFields = fields.iterator();
			while (!used && iFields.hasNext()) {
				Element f = (Element) iFields.next();
				@SuppressWarnings("unchecked")
				Iterator<Element> iBlockingFields = f.getChildren(BlockingTags.BLOCKING_FIELD).iterator();
				while (!used && iBlockingFields.hasNext()) {
					Element bf = (Element) iBlockingFields.next();
					used =
						bf != null
							&& GeneratorHelper.includesConf(bf, conf)
							&& !"false".equals(bf.getAttributeValue(CoreTags.USE));
				}
			}
		}
		if (used) {
			usedRecords.add(r.getAttributeValue("className"));
		}
		return used;
	}

	private void processRecord(Element r, String sourceNodeTypeName) throws IOException {
		String className = r.getAttributeValue("className");
		int recordNumber = Integer.parseInt(r.getAttributeValue("recordNumber"));
		// 2014-04-24 rphall: Commented out unused local variable.
//		String nodeViewName = null;
		int recordDefaultCount = defaultCount;
		Element hd = GeneratorHelper.getNodeTypeExt(r, BlockingTags.BLOCKING);
		if (hd != null) {
			//			nodeViewName = hd.getAttributeValue(BlockingTags.VIEW);
			recordDefaultCount = BlockingConfigurationsGenerator.getDefaultCount(g, hd, recordDefaultCount);
		}
		if (recordNumber == 0) {
			w.write("private void add" + className + "(" + className + " r) {" + Constants.LINE_SEPARATOR);
			for (int k = 0; k < dbCs.length; k++) {
				Element dbNodeType = GeneratorHelper.getNodeTypeExt(r, DbTags.DB);
				if (dbNodeType != null && GeneratorHelper.getBooleanAttribute(dbNodeType, CoreTags.VIRTUAL, false)) {
					// virtual root
				} else {
					createOrGetDView(dbCs[k], DbGenerator.getViewNameForNode(r, dbCs[k].name, g));
				}
			}
		} else {
			w.write("private void add" + className + "(" + className + "[] rs) {" + Constants.LINE_SEPARATOR);
			w.write("for(int i = 0; i < rs.length; ++i) {" + Constants.LINE_SEPARATOR);
			w.write(className + " r = rs[i];" + Constants.LINE_SEPARATOR);
		}
		@SuppressWarnings("unchecked")
		List<Element> fields = r.getChildren("field");
		Iterator<Element> iFields = fields.iterator();
		while (iFields.hasNext()) {
			Element f = (Element) iFields.next();
			String sourceFieldName = f.getAttributeValue("name");
			String type = f.getAttributeValue("type");
			@SuppressWarnings("unchecked")
			Iterator<Element> iBlockingFields = f.getChildren(BlockingTags.BLOCKING_FIELD).iterator();
			for (int i = 0; i < dbCs.length; i++) {
				dbCs[i].qf = null;
			}
			while (iBlockingFields.hasNext()) {
				Element bf = (Element) iBlockingFields.next();
				if (GeneratorHelper.includesConf(bf, conf)) {
					String targetFieldName = bf.getAttributeValue(BlockingTags.TARGET_FIELD_NAME);
					if (targetFieldName == null) {
						targetFieldName = sourceFieldName;
					}
					String targetNodeTypeName = bf.getAttributeValue(BlockingTags.TARGET_NODE_TYPE_NAME);
					if (targetNodeTypeName == null) {
						targetNodeTypeName = sourceNodeTypeName;
					}
					int dc = BlockingConfigurationsGenerator.getDefaultCount(g, bf, recordDefaultCount);
					if ("true".equals(bf.getAttributeValue(BlockingTags.KEY))) {
						if (recordNumber == 0) {
							if (uniqueId == null) {
								uniqueId = targetFieldName;
								dc = 1;
							} else {
								g.error("Root record must have single key.");
							}
						}
					}
					if (!"false".equals(bf.getAttributeValue(CoreTags.USE))) {
						if (targetNodeTypeName == null) {
							g.error("View not defined for record: " + sourceNodeTypeName);
							return;
						}
						String group = bf.getAttributeValue(BlockingTags.TARGET_GROUP);
						if (group == null) {
							group = "";
						}
						if (getBField(dbCs[0],
							sourceNodeTypeName,
							sourceFieldName,
							targetNodeTypeName,
							targetFieldName,
							false)
							!= null) {
							g.error("Duplicate blocking field.");
						} else {
							BField tbf = null;
							for (int k = 0; k < dbCs.length; k++) {
								DbC dbC = dbCs[k];
								DField dbf = createOrGetDField(dbC, targetNodeTypeName, targetFieldName, type, dc);
								QField qf = dbC.qf;
								if (qf == null) {
									dbC.qf = qf = new QField(dbC.qfs.size(), sourceNodeTypeName, sourceFieldName);
									dbC.qfs.add(qf);
								}
								tbf = new BField(dbC.bfs.size(), dbf, qf, group);
								dbC.bfs.add(tbf);
								String base = bf.getAttributeValue(BlockingTags.BASE);
								if (base != null) {
									tbf.base =
										new BField[] {
											 getBField(
												dbC,
												sourceNodeTypeName,
												base,
												targetNodeTypeName,
												base,
												k == 0)};
								}
								Element bc = bf.getChild(BlockingTags.BASE);
								if (bc != null) {
									if (base != null && k == 0) {
										g.error("Cannot specify base as attribute and child.");
									} else {
										@SuppressWarnings("unchecked")
										List<Element> baseFields = bc.getChildren();
										int size = baseFields.size();
										tbf.base = new BField[size];
										for (int i = 0; i < size; ++i) {
											tbf.base[i] =
												(BField) getField(dbC,
														(Element) baseFields
																.get(i));
										}
									}
								}
							}
							String valid = bf.getAttributeValue(BlockingTags.SOURCE_VALID);
							w.write("__l" + tbf.number + " = r.__v_" + sourceFieldName);
							if (valid != null) {
								w.write(" && (" + valid + ")");
							}
							w.write(
								"? addField("
									+ tbf.number
									+ ", "
									+ GeneratorHelper.getStringExpr(type, "r." + sourceFieldName)
									+ ", ");
							if (tbf.base.length == 0) {
								w.write("null");
							} else {
								for (int i = 0; i < tbf.base.length; ++i) {
									w.write((i != 0 ? " &&" : "") + " __l" + tbf.base[i].number + " != null");
								}
								w.write(" ? new BlockingValue[]{");
								for (int i = 0; i < tbf.base.length; ++i) {
									w.write((i != 0 ? "," : "") + " __l" + tbf.base[i].number);
								}
								w.write("} : null");
							}
							w.write(") : null;" + Constants.LINE_SEPARATOR);
						}
					}
				}
			}
		}
		@SuppressWarnings("unchecked")
		List<Element> records = new ArrayList<>(r.getChildren(CoreTags.NODE_TYPE));
		Iterator<Element> iRecords = records.iterator();
		while (iRecords.hasNext()) {
			Element e = (Element) iRecords.next();
			String eName = e.getAttributeValue("name");
			String eClassName = e.getAttributeValue("className");
			if (usedRecords.contains(eClassName)) {
				w.write("add" + eClassName + "(r." + eName + ");" + Constants.LINE_SEPARATOR);
			} else {
				iRecords.remove();
			}
		}
		if (recordNumber != 0) {
			w.write("}" + Constants.LINE_SEPARATOR);
		}
		w.write("}" + Constants.LINE_SEPARATOR);
		iRecords = records.iterator();
		while (iRecords.hasNext()) {
			Element e = (Element) iRecords.next();
			String eName = e.getAttributeValue("name");
			processRecord(e, sourceNodeTypeName + "." + eName);
		}
	}

	private QField getQField(DbC dbC, String compoundRecordName, String fieldName) {
		Iterator<QField> iQfs = dbC.qfs.iterator();
		while (iQfs.hasNext()) {
			QField qf = (QField) iQfs.next();
			if (compoundRecordName.equals(qf.sourceNodeTypeName) && fieldName.equals(qf.sourceFieldName)) {
				return qf;
			}
		}
		g.error("Field not found: qf record=\"" + compoundRecordName + "\" name=\"" + fieldName + "\"");
		return null;
	}

	private BField getBField(
		DbC dbC,
		String compoundRecordName,
		String fieldName,
		String view,
		String column,
		boolean errorIfNotFound) {
		Iterator<BField> iBfs = dbC.bfs.iterator();
		while (iBfs.hasNext()) {
			BField bf = (BField) iBfs.next();
			if (compoundRecordName.equals(bf.qfield.sourceNodeTypeName)
				&& fieldName.equals(bf.qfield.sourceFieldName)
				&& column.equals(bf.dfield.targetFieldName)
				&& view.equals(bf.dfield.targetNodeTypeName)) {
				return bf;
			}
		}
		if (errorIfNotFound)
			g.error(
				"Field not found: bf record=\""
					+ compoundRecordName
					+ "\" name=\""
					+ fieldName
					+ "\" column=\""
					+ column
					+ "\"");
		return null;
	}

	private DField getDField(DbC dbC, String viewName, String column) {
		Iterator<DField> iDbfs = dbC.dbfs.iterator();
		while (iDbfs.hasNext()) {
			DField df = (DField) iDbfs.next();
			if (viewName.equals(df.targetNodeTypeName) && column.equals(df.targetFieldName)) {
				return df;
			}
		}
		g.error("Field not found: dbf view=\"" + viewName + "\" column=\"" + column + "\"");
		return null;
	}

	private DField createOrGetDField(DbC dbC, String viewName, String column, String type, int defaultCount) {
		List<DField> dbfs = dbC.dbfs;
		Iterator<DField> iDbfs = dbfs.iterator();
		while (iDbfs.hasNext()) {
			DField df = (DField) iDbfs.next();
			if (viewName.equals(df.targetNodeTypeName) && column.equals(df.targetFieldName)) {
				if (defaultCount < df._defaultCount) {
					df._defaultCount = defaultCount;
				}
				return df;
			}
		}
		DField res = new DField(dbC, dbfs.size(), viewName, column, type, defaultCount);
		dbfs.add(res);
		return res;
	}

	private DView createOrGetDView(DbC dbC, String name) {
		List<DView> dbtbs = dbC.dbtbs;
		Iterator<DView> iDbtbs = dbtbs.iterator();
		while (iDbtbs.hasNext()) {
			DView d = (DView) iDbtbs.next();
			if (d.name.equals(name)) {
				return d;
			}
		}
		DView d = new DView(dbtbs.size(), name);
		dbtbs.add(d);
		return d;
	}

	private static abstract class IField {
		private static Comparator<IField> comparator = new Comparator<IField>() {
			public int compare(IField f1, IField f2) {
				int f1t = f1.getTypeId();
				int f2t = f2.getTypeId();
				if (f1t < f2t)
					return -1;
				if (f1t > f2t)
					return 1;
				if (f1.number < f2.number)
					return -1;
				if (f1.number > f2.number)
					return 1;
				return 0;
			}
		};
		int number;
		ArrayList<IField[]> illegal;
		IField(int number) {
			this.number = number;
			illegal = new ArrayList<>();
		}
		void addIllegal(IField[] ic) {
			Arrays.sort(ic, comparator);
			int size = illegal.size();
			for (int i = 0; i < size; ++i) {
				IField[] oc = (IField[]) illegal.get(i);
				if (ic.length == oc.length) {
					int j = 0;
					while (j < ic.length && ic[j] == oc[j])
						++j;
					if (j == ic.length)
						return;
				}
			}
			illegal.add(ic);
		}
		abstract int getTypeId();
	}

	private static class QField extends IField {
		String sourceNodeTypeName;
		String sourceFieldName;
		QField(int number, String sourceNodeTypeName, String sourceFieldName) {
			super(number);
			this.sourceNodeTypeName = sourceNodeTypeName;
			this.sourceFieldName = sourceFieldName;
		}
		public String toString() {
			return "qfs[" + number + "]";
		}
		int getTypeId() {
			return 0;
		}
	}

	private static class DView {
		int number;
		String name;
		DView(int number, String name) {
			this.number = number;
			this.name = name;
		}
	}

	private class DField extends IField {
		String targetNodeTypeName;
		DView view;
		String targetFieldName;
		String targetColumnName;
		String type;
		int _defaultCount;
		DField(DbC dbC, int number, String targetNodeTypeName, String targetFieldName, String type, int defaultCount) {
			super(number);
			this.targetNodeTypeName = targetNodeTypeName;
			this.targetFieldName = targetFieldName;
			this.type = type;
			this._defaultCount = defaultCount;
			Element nodeType = GeneratorHelper.findNodeType(g.getRootElement(), targetNodeTypeName);
			if (nodeType != null) {
				Element field = GeneratorHelper.findField(nodeType, targetFieldName);
				if (field != null) {
					Element targetColumnElm = GeneratorHelper.getPhysicalField(field, dbC.name, DbTags.DB);
					if( targetColumnElm != null)
						targetColumnName =  targetColumnElm.getAttributeValue("name"); //TODO "name"
					if(targetColumnName == null || targetColumnName.length() == 0 )
						targetColumnName = targetFieldName;
					view = createOrGetDView(dbC, DbGenerator.getViewNameForField(field, dbC.name, g));
				} else {
					g.error("Could not find targetField: " + targetFieldName);
				}
			} else {
				g.error("Could not find targetNodeType: " + targetNodeTypeName);
			}
		}
		public String toString() {
			return "dbfs[" + number + "]";
		}
		int getTypeId() {
			return 1;
		}
	}

	private static class BField extends IField {
		private static final BField[] NULL_BFIELD = new BField[0];
		DField dfield;
		QField qfield;
		String group;
		BField[] base;
		BField(int number, DField dfield, QField qfield, String group) {
			super(number);
			this.dfield = dfield;
			this.qfield = qfield;
			this.group = group;
			this.base = NULL_BFIELD;
		}
		public String toString() {
			return "bfs[" + number + "]";
		}
		int getTypeId() {
			return 2;
		}
		IField getIField(int i) {
			if (i == 0) {
				return dfield;
			} else {
				return qfield;
			}
		}
	}
}
