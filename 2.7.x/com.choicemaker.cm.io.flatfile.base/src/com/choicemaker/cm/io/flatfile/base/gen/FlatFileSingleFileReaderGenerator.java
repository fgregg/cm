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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jdom.Element;

import com.choicemaker.cm.core.Constants;
import com.choicemaker.cm.core.DerivedSource;
import com.choicemaker.cm.core.util.StringUtils;
import com.choicemaker.cm.core.gen.CoreTags;
import com.choicemaker.cm.core.gen.GenException;
import com.choicemaker.cm.core.gen.IGenerator;
import com.choicemaker.cm.core.gen.GeneratorHelper;
import com.choicemaker.cm.core.gen.GeneratorPlugin;

/**
 * Description
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:58 $
 */
public class FlatFileSingleFileReaderGenerator implements GeneratorPlugin {
	private static DerivedSource src = DerivedSource.valueOf("flatfile");

	public static FlatFileSingleFileReaderGenerator instance = new FlatFileSingleFileReaderGenerator();

	private Set recordTypes;
	private int recordNum;

	public void generate(IGenerator g) throws GenException {
		String className = g.getSchemaName() + "SingleFileFlatFileReader";
		String packageName = g.getPackage() + ".flatfile";
		g.addAccessorBody(
			"public Object getSingleFileFlatFileReader(Object tokenizer, boolean tagged, boolean singleLine) {"
				+ Constants.LINE_SEPARATOR);
		g.addAccessorBody(
			"return new "
				+ packageName
				+ "."
				+ className
				+ "((Tokenizer)tokenizer, tagged, singleLine);"
				+ Constants.LINE_SEPARATOR);
		g.addAccessorBody("}" + Constants.LINE_SEPARATOR);
		try {
			String directoryName = g.getSourceCodePackageRoot() + File.separator + "flatfile";
			String fileName = directoryName + File.separator + className + ".java";
			g.addGeneratedFile(fileName);
			FileOutputStream fs = new FileOutputStream(new File(fileName).getAbsoluteFile());
			Writer w = new OutputStreamWriter(new BufferedOutputStream(fs));
			w.write("// Generated by ChoiceMaker. Do not edit." + Constants.LINE_SEPARATOR);
			w.write("package " + packageName + ";" + Constants.LINE_SEPARATOR);
			w.write("import org.apache.log4j.*;" + Constants.LINE_SEPARATOR);
			w.write("import java.util.*;" + Constants.LINE_SEPARATOR);
			w.write("import java.io.*;" + Constants.LINE_SEPARATOR);
			w.write("import com.choicemaker.cm.core.*;" + Constants.LINE_SEPARATOR);
			w.write("import com.choicemaker.cm.io.flatfile.base.*;" + Constants.LINE_SEPARATOR);
			w.write("import " + g.getPackage() + ".*;" + Constants.LINE_SEPARATOR);
			w.write(g.getImports());
			w.write("public final class " + className + " implements FlatFileReader {" + Constants.LINE_SEPARATOR);
			w.write(
				"private static Logger logger = Logger.getLogger("
					+ packageName
					+ "."
					+ className
					+ ".class);"
					+ Constants.LINE_SEPARATOR);
			w.write("private Tokenizer tokenizer;" + Constants.LINE_SEPARATOR);
			w.write("private boolean tagged;" + Constants.LINE_SEPARATOR);
			w.write("private boolean singleLine;" + Constants.LINE_SEPARATOR);
			w.write(
				"private static DerivedSource src = DerivedSource.valueOf(\"flatfile\");" + Constants.LINE_SEPARATOR);
			w.write(
				"public "
					+ className
					+ "(Tokenizer tokenizer, boolean tagged, boolean singleLine) {"
					+ Constants.LINE_SEPARATOR);
			w.write("this.tokenizer = tokenizer;" + Constants.LINE_SEPARATOR);
			w.write("this.tagged = tagged;" + Constants.LINE_SEPARATOR);
			w.write("this.singleLine = singleLine;" + Constants.LINE_SEPARATOR);
			w.write("}" + Constants.LINE_SEPARATOR);
			w.write("public void open() {}" + Constants.LINE_SEPARATOR);
			Element rootRecord = g.getRootRecord();
			w.write("public Record getRecord() throws IOException {" + Constants.LINE_SEPARATOR);
			w.write(
				"Record r = getRecord" + rootRecord.getAttributeValue("className") + "();" + Constants.LINE_SEPARATOR);
			w.write("r.computeValidityAndDerived(src);" + Constants.LINE_SEPARATOR);
			w.write("return r;" + Constants.LINE_SEPARATOR);
			w.write("}" + Constants.LINE_SEPARATOR);
			writeGetters(g, w, rootRecord, null);
			w.write("}" + Constants.LINE_SEPARATOR);
			w.flush();
			fs.close();
		} catch (IOException ex) {
			throw new GenException("Problem writing file.", ex);
		}
	}

	private void writeGetters(IGenerator g, Writer w, Element r, String outerClassName) throws IOException {
		++recordNum;
		String recordName = r.getAttributeValue("name");
		String className = r.getAttributeValue("className");
		if (outerClassName != null) {
			w.write("private List l__" + className + " = new ArrayList();" + Constants.LINE_SEPARATOR);
		}
		w.write("private " + className + " getRecord" + className + "(");
		if (outerClassName != null) {
			w.write(outerClassName + " outer");
		}
		w.write(") throws IOException {" + Constants.LINE_SEPARATOR);
		Element hd = GeneratorHelper.getNodeTypeExt(r, "flatfile");
		String recordType = hd.getAttributeValue("tag");
		if (outerClassName == null) {
			w.write("if(tagged && tokenizer.tag != \"" + recordType + "\") {" + Constants.LINE_SEPARATOR);
			w.write("throw new IOException(\"Illegal tag: \" + tokenizer.tag);" + Constants.LINE_SEPARATOR);
			w.write("}" + Constants.LINE_SEPARATOR);
		}
		w.write(className + " o__" + className + " = new " + className + "();" + Constants.LINE_SEPARATOR);
		if (outerClassName != null) {
			w.write("o__" + className + ".outer = outer;" + Constants.LINE_SEPARATOR);
		}
		List fields = new ArrayList(r.getChildren("field"));
		int res = GeneratorHelper.filterFields(fields, src, "flatfileField");
		switch (res) {
			case GeneratorHelper.OK :
				break;
			case GeneratorHelper.DUPLICATE_POS :
				g.error(recordName + ": Duplicate pos");
				return;
			case GeneratorHelper.POS_OUTSIDE_RANGE :
				g.error(recordName + ": pos outside range");
				return;
		}
		Iterator iF = fields.iterator();
		boolean hasStart = false;
		boolean first = true;
		while (iF.hasNext()) {
			Element field = (Element) iF.next();
			if (field != null) {
				w.write("o__" + className + "." + field.getAttributeValue("name") + " = ");
				int start = Integer.MIN_VALUE;
				int width = FlatFileGenerator.defaultWidth(field.getAttributeValue("type"));
				boolean trim = true;
				boolean valueOf = false;
				Element fff = field.getChild("flatfileField");
				char nullRepresentation = FlatFileGenerator.DEFAULT_NULL_REPRESENTATION;
				if (fff != null) {
					String t = fff.getAttributeValue("width");
					if (t != null) {
						width = Integer.parseInt(t);
					}
					t = fff.getAttributeValue(FlatFileTags.START);
					if (t != null) {
						if (first) {
							hasStart = true;
						} else if (!hasStart) {
							g.error("Either all or no field of a node must have a start position.");
						}
						start = Integer.parseInt(t);
					} else if (hasStart) {
						g.error("Either all or no field of a node must have a start position.");
					}
					trim = !"false".equals(fff.getAttributeValue("trim"));
					valueOf = "true".equals(fff.getAttributeValue("valueOf"));
					String nr = fff.getAttributeValue("nullRepresentation");
					if (nr != null) {
						nullRepresentation = StringUtils.getChar(nr);
					}
				} else if (hasStart) {
					g.error("Either all or no field of a node must have a start position.");
				}
				w.write(
					FlatFileGenerator.getField(
						"tokenizer",
						field.getAttributeValue("type"),
						start,
						width,
						trim,
						valueOf,
						nullRepresentation,
						g.isIntern())
						+ ";"
						+ Constants.LINE_SEPARATOR);
				first = false;
			} else {
				w.write("tokenizer.skip(1);" + Constants.LINE_SEPARATOR);
			}
		}
		w.write("if(!singleLine) tokenizer.readLine();" + Constants.LINE_SEPARATOR);
		List records = r.getChildren(CoreTags.NODE_TYPE);
		if (!records.isEmpty()) {
			w.write("while(true) {" + Constants.LINE_SEPARATOR);
			Iterator iR = records.iterator();
			while (iR.hasNext()) {
				Element e = (Element) iR.next();
				String eClassName = e.getAttributeValue("className");
				String eRecordType = GeneratorHelper.getNodeTypeExt(e, "flatfile").getAttributeValue("tag");
				w.write("if(tokenizer.tag == \"" + eRecordType + "\") {" + Constants.LINE_SEPARATOR);
				w.write(
					"l__"
						+ eClassName
						+ ".add(getRecord"
						+ eClassName
						+ "(o__"
						+ className
						+ "));"
						+ Constants.LINE_SEPARATOR);
				w.write("} else ");
			}

			// 2005-10-23 rphall
			// Bug: not skipping fields of child nodes for untagged, single-file input
			// BugFix: continue pulling fields from tokenizer for each child node
			w.write("if(singleLine && !tagged) {" + Constants.LINE_SEPARATOR);
			iR = records.iterator();
			while (iR.hasNext()) {
				Element e = (Element) iR.next();
				String eClassName = e.getAttributeValue("className");
				w.write(
					"l__"
						+ eClassName
						+ ".add(getRecord"
						+ eClassName
						+ "(o__"
						+ className
						+ "));"
						+ Constants.LINE_SEPARATOR);
			}
			// w.write("else {" + Constants.LINE_SEPARATOR);
			w.write("break;" + Constants.LINE_SEPARATOR);
			w.write("} else {" + Constants.LINE_SEPARATOR);
			// EndBugFix
			
			w.write("break;" + Constants.LINE_SEPARATOR);
			w.write("}" + Constants.LINE_SEPARATOR);
			w.write("}" + Constants.LINE_SEPARATOR);
			iR = records.iterator();
			while (iR.hasNext()) {
				Element e = (Element) iR.next();
				String eName = e.getAttributeValue("name");
				String eClassName = e.getAttributeValue("className");
				w.write("if(l__" + eClassName + ".size() == 0) {" + Constants.LINE_SEPARATOR);
				w.write(
					"o__" + className + "." + eName + " = " + eClassName + ".__zeroArray;" + Constants.LINE_SEPARATOR);
				w.write("} else {" + Constants.LINE_SEPARATOR);
				w.write(
					"l__"
						+ eClassName
						+ ".toArray((o__"
						+ className
						+ "."
						+ eName
						+ " = new "
						+ eClassName
						+ "[l__"
						+ eClassName
						+ ".size()]));"
						+ Constants.LINE_SEPARATOR);
				w.write("l__" + eClassName + ".clear();" + Constants.LINE_SEPARATOR);
				w.write("}" + Constants.LINE_SEPARATOR);
			}
		}
		w.write("return o__" + className + ";" + Constants.LINE_SEPARATOR);
		w.write("}" + Constants.LINE_SEPARATOR);
		Iterator iR = records.iterator();
		while (iR.hasNext()) {
			writeGetters(g, w, (Element) iR.next(), className);
		}
	}
}