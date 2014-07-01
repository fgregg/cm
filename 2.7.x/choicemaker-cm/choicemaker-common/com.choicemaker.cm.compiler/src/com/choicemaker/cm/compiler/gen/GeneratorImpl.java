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
package com.choicemaker.cm.compiler.gen;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.SAXParseException;

import com.choicemaker.cm.compiler.ICompilationUnit;
import com.choicemaker.cm.compiler.parser.Scanner;
import com.choicemaker.cm.core.Constants;
import com.choicemaker.cm.core.DerivedSource;
import com.choicemaker.cm.core.compiler.CompilerException;
import com.choicemaker.cm.core.gen.CoreTags;
import com.choicemaker.cm.core.gen.GenException;
import com.choicemaker.cm.core.gen.GeneratorHelper;
import com.choicemaker.cm.core.gen.GeneratorPlugin;
import com.choicemaker.cm.core.gen.IGenerator;
import com.choicemaker.cm.core.gen.IGeneratorPluginFactory;
import com.choicemaker.cm.core.gen.InstallableGeneratorPluginFactory;
import com.choicemaker.cm.core.xmlconf.GeneratorXmlConf;
import com.choicemaker.cm.core.xmlconf.XmlParserFactory;

/**
 * Main generator for translating ChoiceMaker schemas.
 *
 * This class currently has many functions. Might be split in the future:
 * <ul>
 *   <li>Main entry point for generation.</li>
 *   <li>Static holder of list of plugins.</li>
 *   <li>Adds some useful attributes to tree.</li>
 *   <li>Generates holder classes.</li>
 *   <li>Soon: Will generate Accessor class.</li>
 * </ul>
 *
 * @todo      Type checking.
 * @todo      Name clashes (fields, records, keywords)
 * @todo      Generate accessor class.
 * @author    Martin Buechi
 * @version   $Revision: 1.3 $ $Date: 2010/03/24 20:05:32 $
 */
public class GeneratorImpl implements IGenerator {

	protected class DoubleWriter {
		private Writer cw;
		private Writer ucw;
		protected DoubleWriter(Writer cw, Writer ucw){
			this.cw = cw;
			this.ucw = ucw;
		}
		protected void write (String s) throws IOException {
			cw.write(s);
			if(ucw != null)
				ucw.write(s);
		}
	}
	private static final String PUBLIC = "public";
	private static final String PRIVATE = "private";
	private static final String PROTECTED = "protected";

	private String externalSourceCodePackageRoot;
	/** log4j category */
	private static Logger logger = Logger.getLogger(GeneratorImpl.class);

	protected class Version {

		public static final int UNDEFINED = -1;
		long major = UNDEFINED;
		long minor = UNDEFINED;
		long revision = UNDEFINED;

		protected Version(String s){
			if(s == null || s.length() == 0)
				return;
			int end = s.indexOf('.');
			String majorStr = s.substring(0,end == -1?s.length():end);
			try {
				major = Long.parseLong(majorStr);
			} catch (NumberFormatException e){
				logger.error("invalid schema version value "+s);
				return;
			}
		}

		protected boolean isDefined(){
			return major != UNDEFINED;
		}
	}

	/** directory for generated code */
	private String sourceCodePackageRoot;
	/** JDOM document of schema */
	private Document document;
	/** deepest stacking depth */
	private int stackingDepth;
	/** auxiliary for numbering records */
	private int recordNumber;
	/** imports statements from schema */
	private String imports;
	/** name of the clue set, used as prefix for generated classes */
	private String clueSetName;
	private String schemaName;
	/** name of the schema file */
	private String schemaFileName;
	/** compilation unit for callbacks to compiler for populating symbol table */
	private ICompilationUnit cu;
	/** Accessor class imports */
	private StringBuffer accessorImports;
	/** Accessor class implements */
	private StringBuffer accessorImplements;
	/** Accessor class body */
	private StringBuffer accessorBody;
	private String pckage;
	private String externalPackage;
	private boolean intern;
	private Version version;

	/**
	 * Returns the package of the generated code.
	 * Plugins may also generate code in subpackages.
	 *
	 * @return   The package of the generated code.
	 * @throws   GenException  if the data cannot be read.
	 */
	public String getPackage() {
		return pckage;
	}

	public String getExternalPackage() {
		return externalPackage;
	}

	public boolean isIntern() {
		return intern;
	}

	/**
	 * Returns the root directory for the generated source code.
	 *
	 * If this returns <code>/tmp</code> and <code>getPackage</code>
	 * returns <code>cust.gend</code>, then the source code should
	 * be placed into <code>/tmp/cust/gend</code>. This value
	 * is returned by <code>getSourceCodeRoot</code>.
	 *
	 * @return   The root directory for the generated source code.
	 * @throws   GenException  if the data cannot be read.
	 * @see      getSourceCodePackageRoot
	 * @see      getPackage
	 */
	public String getSourceCodeRoot() throws GenException {
		return GeneratorXmlConf.getCodeRoot() + File.separator + "src";
	}

	/**
	 * Returns the directory for the generated source code.
	 *
	 * @return   The directory for the generated source code.
	 * @throws   GenException  if the data cannot be read.
	 */
	public String getSourceCodePackageRoot() throws GenException {
		if (sourceCodePackageRoot == null) {
			sourceCodePackageRoot =
				new File(getSourceCodeRoot() + File.separator + getPackage().replace('.', File.separatorChar))
					.getAbsolutePath();
		}
		return sourceCodePackageRoot;
	}

	public String getExternalSourceCodePackageRoot() throws GenException {
		if (externalSourceCodePackageRoot == null) {
			externalSourceCodePackageRoot =
				new File(getSourceCodeRoot() + File.separator + getExternalPackage().replace('.', File.separatorChar))
					.getAbsolutePath();
		}
		return externalSourceCodePackageRoot;
	}

	/**
	 * Returns the name of the clue set.
	 *
	 * @return   The name of the clue set.
	 */
	public String getClueSetName() {
		return clueSetName;
	}

	public String getSchemaName() {
		return schemaName;
	}

	/**
	 * Returns the JDOM Document representing the ChoiceMaker schema.
	 *
	 * @return  The JDOM Document representing the ChoiceMaker schema.
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * Returns the root element of the JDOM Document representing the ChoiceMaker schema.
	 *
	 * @return  The root element of the JDOM Document representing the ChoiceMaker schema.
	 */
	public Element getRootElement() {
		return document.getRootElement();
	}

	/**
	 * Returns the root record of the ChoiceMaker schema.
	 *
	 * @return  The root record of the ChoiceMaker schema.
	 */
	public Element getRootRecord() {
		return getRootElement().getChild(CoreTags.NODE_TYPE);
	}

	/**
	 * Returns the maximal record stacking depth.
	 *
	 * For example, if there is a main record patient, which has
	 * a nested record contacts, which in turn has a nested record
	 * race (and this is the deepest stacking), this method returns 3.
	 *
	 * @return   The maximal record stacking depth.
	 */
	public int getStackingDepth() {
		return stackingDepth;
	}

	/**
	 * Returns the import statements to be added to generated code.
	 *
	 * @return   The import statements to be added to generated code.
	 */
	public String getImports() {
		if (imports == null) {
			StringBuffer b = new StringBuffer();
			List imp = getRootElement().getChildren("import");
			Iterator i = imp.iterator();
			while (i.hasNext()) {
				b.append("import " + ((Element) i.next()).getText() + ";" + Constants.LINE_SEPARATOR);
			}
			imports = b.toString();
		}
		return imports;
	}

	/**
	 * Adds a generated Java source file to the list of generated files.
	 *
	 * @param  fileName  The fully qualified file name of the Java source file.
	 */
	public void addGeneratedFile(String fileName) {
		cu.addGeneratedJavaSourceFile(fileName);
	}

	public void error(String message) {
		logger.error(message);
	}

	public void warning(String message) {
		logger.warn(message);
	}

	public boolean hasErrors() {
		return cu.getErrors() > 0;
	}

	/**
	 * Constructs a generator.
	 *
	 * @param   cu  The CompilationUnit to be used for callbacks.
	 * @param   clueSetName  The name of the clue set.
	 * @param   schemaFileName  The name of the file containing the ChoiceMaker schema.
	 */
	public GeneratorImpl(ICompilationUnit cu, String clueSetName, String schemaFileName, String schemaName, String pckage) {
		this.cu = cu;
		this.clueSetName = clueSetName;
		this.schemaFileName = schemaFileName;
		this.schemaName = schemaName;
		this.pckage = pckage + ".internal." + schemaName;
		this.externalPackage = pckage + "." + schemaName;
	}

	/**
	 * Generate files.
	 */
	public void generate() {
		boolean validate = XmlParserFactory.connected();
		SAXBuilder builder = XmlParserFactory.createSAXBuilder(validate);
		if (validate) {
			builder.setFeature("http://apache.org/xml/features/validation/schema", true);
		}
		//		builder.setFeature("http://apache.org/xml/features/validation/dynamic", true);
		builder.setErrorHandler(new SaxErrorHandler());
		ClassLoader oldCl = XmlParserFactory.setClassLoader();
		try {
			document = builder.build(new File(schemaFileName).getAbsoluteFile());
			if (cu.getErrors() == 0) {
				process();
			}
		} catch (JDOMException ex) {
			// BUG poor diagnostics
			// rphall 2005-10-12
			logger.error("XML error in schema file " + schemaFileName + Constants.LINE_SEPARATOR + ex);
		} catch (Exception ex) {
			// BUG poor diagnostics
			// rphall 2005-10-12
			logger.error("Error processing schema file " + schemaFileName + Constants.LINE_SEPARATOR + ex);
		} finally {
			XmlParserFactory.restoreClassLoader(oldCl);
		}

	}

	private class SaxErrorHandler implements org.xml.sax.ErrorHandler {
		public void error(SAXParseException ex) {
			logger.error("XML error in schema file " + schemaFileName + Constants.LINE_SEPARATOR + ex);
		}

		public void fatalError(SAXParseException ex) {
			logger.error("XML error in schema file " + schemaFileName + Constants.LINE_SEPARATOR + ex);
		}

		public void warning(SAXParseException ex) {
			logger.error("XML error in schema file " + schemaFileName + Constants.LINE_SEPARATOR + ex);
		}
	}

	private void process() throws GenException {
		precompute();
		Element rootRecord = getRootRecord();
		identifierCheck(rootRecord.getAttributeValue("name"), null);
		createInterfaceClasses(rootRecord, null);
		createInterfacePackage();
		createHolderClasses(rootRecord, null);
		createAccessor();
		IGeneratorPluginFactory factory = InstallableGeneratorPluginFactory
				.getInstance();
		List generatorPlugins = factory.lookupGeneratorPlugins();
		for (Iterator i = generatorPlugins.iterator(); i.hasNext();) {
			GeneratorPlugin gp = (GeneratorPlugin) i.next();
			logger.info("Generator: '" + gp.toString() + "'");
			gp.generate(this);
			// BUG never checks if GeneratorPlugin produces errors
			// rphall 2005-10-11
		}
		finishAccessor();
	}

	private void precompute() throws GenException {
		File parentDir = new File(getSourceCodePackageRoot()).getAbsoluteFile();
		parentDir.mkdirs();
		new File(getExternalSourceCodePackageRoot()).getAbsoluteFile().mkdirs();
		Element root = getRootElement();
		this.version = new Version(root.getAttributeValue("version"));
		root.setAttribute(CoreTags.RECORD_NUMBER, "-1");
		Element def = root.getChild(CoreTags.GLOBAL);
		if (def != null) {
			Element strings = def.getChild(CoreTags.STRINGS);
			if (strings != null) {
				intern = "true".equals(strings.getAttributeValue("intern"));
			}
		}
		cu.setIntern(intern);
		nameRecords(getRootRecord(), clueSetName, "__", 1, "");
	}

	private void nameRecords(Element r, String baseName, String separator, int level, String fqName) {
		if (level > stackingDepth) {
			stackingDepth = level;
		}
		String recordName = r.getAttributeValue("name");
		String interfaceName = Character.toUpperCase(recordName.charAt(0)) + recordName.substring(1);
		String className = interfaceName + "Impl";
		String baseInterfaceName = interfaceName + "Base";
		String holderClassName = interfaceName + "Holder";
		r.setAttribute("className", className);
		r.setAttribute(CoreTags.BASE_INTERFACE_NAME, baseInterfaceName);
		r.setAttribute(CoreTags.URM_BASE_INTERFACE_NAME, "I"+interfaceName+"RecordHolder");
		r.setAttribute(CoreTags.INTERFACE_NAME, interfaceName);
		r.setAttribute(CoreTags.HOLDER_CLASS_NAME, holderClassName);
		r.setAttribute(CoreTags.URM_HOLDER_CLASS_NAME, interfaceName+"RecordHolder");
		r.setAttribute(CoreTags.LEVEL, String.valueOf(level - 1));
		fqName += recordName;
		r.setAttribute(CoreTags.FQ_NAME, fqName);
		cu.addClassType(className);
		r.setAttribute(CoreTags.RECORD_NUMBER, String.valueOf(recordNumber++));
		List nestedRecords = r.getChildren(CoreTags.NODE_TYPE);
		if (nestedRecords.isEmpty()) {
			r.setAttribute("isLeafRecord", "true");
		} else {
			r.setAttribute("isLeafRecord", "false");
		}
		Iterator i = nestedRecords.iterator();
		while (i.hasNext()) {
			nameRecords((Element) i.next(), className, separator, level + 1, fqName + ".");
		}
	}

	private void createInterfacePackage() throws GenException {
		try {
			Writer w = new FileWriter(getExternalSourceCodePackageRoot() + File.separator + "package.html");
			w.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">" + Constants.LINE_SEPARATOR);
			w.write("<html><head/><body bgcolor=\"white\">" + Constants.LINE_SEPARATOR);
			w.write(
				"<p>Containts the public API generated from the ChoiceMaker schema "
					+ getSchemaName()
					+ ". For each node type <em>x</em>, two interfaces and"
					+ "one class are generated:</p>"
					+ Constants.LINE_SEPARATOR);
			w.write(
				"<ol><li>Base interface <em>X</em>Base: This interface contains getter methods for all fields and nested node types "
					+ "that are not derived (for the beans source). Clients should use this to read the results presented in a BeanMatchCandidate.</li>");
			w.write(
				"<li>Holder class <em>X</em>Holder: This class implements <em>X</em>Base. It also contains setter methods. Clients should "
					+ "use this to construct arguments sent in a BeanProfile.</li>"
					+ Constants.LINE_SEPARATOR);
			w.write(
				"<li>ClueMaker interface <em>X</em>: This interface extends <em>X</em>Base with getter methods for derived fields. "
					+ "This interface can be used to pass complete nodes from ClueMaker and schema expressions to embedded methods and external Java "
					+ "classes. If the latter only require the methods of <em>X</em>Base, it is recommended that that is used instead. This way the "
					+ "Java classes are isolated from changes in derived fields, which are typically more frequent.</li></ol>"
					+ Constants.LINE_SEPARATOR);
			w.write("</body></html>" + Constants.LINE_SEPARATOR);
			w.close();
		} catch (IOException ex) {
		}
	}

	private void writeValidJavaDoc(Writer w, Element field) throws IOException {
		if (w == null) return;
		String base =
			"hether the value of the field "
				+ field.getAttributeValue(CoreTags.NAME)
				+ " is valid according to the validity predicate in the ChoiceMaker schema.";
		w.write(
			"/**"
				+ Constants.LINE_SEPARATOR
				+ " * Returns w"
				+ base
				+ Constants.LINE_SEPARATOR
				+ " * @return  W"
				+ base
				+ Constants.LINE_SEPARATOR
				+ "*/"
				+ Constants.LINE_SEPARATOR);
	}

	private void writeGetFieldJavaDoc(Writer w, Element field) throws IOException {
		if (w == null) return;
		String base = "he value of " + field.getAttributeValue(CoreTags.NAME) + ".";
		w.write(
			"/**"
				+ Constants.LINE_SEPARATOR
				+ " * Returns t"
				+ base
				+ Constants.LINE_SEPARATOR
				+ " * @return  T"
				+ base
				+ Constants.LINE_SEPARATOR
				+ "*/"
				+ Constants.LINE_SEPARATOR);
	}


	private void writeGetOuterJavaDoc(Writer w) throws IOException {
		if (w == null) return;
		w.write(
			"/**"
				+ Constants.LINE_SEPARATOR
				+ " * Returns the outer node."
				+ Constants.LINE_SEPARATOR
				+ " * @return  The outer node."
				+ Constants.LINE_SEPARATOR
				+ "*/"
				+ Constants.LINE_SEPARATOR);
	}

	private void writeGetIdJavaDoc(Writer w) throws IOException {
		if (w == null) return;
		w.write(
			"/**"
				+ Constants.LINE_SEPARATOR
				+ " * Returns the ID of the record holder. "
				+ Constants.LINE_SEPARATOR
				+ " * @return  Returns the ID of the node."
				+ Constants.LINE_SEPARATOR
				+ "*/"
				+ Constants.LINE_SEPARATOR);
	}

	private void writeGetNestedJavaDoc(Writer w, Element record) throws IOException {
		if (w == null) return;
		String base = "he nested nodes of type " + record.getAttributeValue(CoreTags.NAME) + ".";
		w.write(
			"/**"
				+ Constants.LINE_SEPARATOR
				+ " * Returns t"
				+ base
				+ Constants.LINE_SEPARATOR
				+ " * @return  T"
				+ base
				+ Constants.LINE_SEPARATOR
				+ "*/"
				+ Constants.LINE_SEPARATOR);
	}

	private void writeGetNestedIndexedJavaDoc(Writer w, Element record) throws IOException {
		if (w == null) return;
		String base = "he nested " + record.getAttributeValue(CoreTags.NAME) + " at the specified index.";
		w.write(
			"/**"
				+ Constants.LINE_SEPARATOR
				+ " * Returns t"
				+ base
				+ Constants.LINE_SEPARATOR
				+ " * @param  __index  The index."
				+ Constants.LINE_SEPARATOR
				+ " * @return  T"
				+ base
				+ Constants.LINE_SEPARATOR
				+ "*/"
				+ Constants.LINE_SEPARATOR);
	}

	private void writeSetFieldJavaDoc(DoubleWriter w, Element field) throws IOException {
		String base = "he value of " + field.getAttributeValue(CoreTags.NAME) + ".";
		w.write(
			"/**"
				+ Constants.LINE_SEPARATOR
				+ " * Sets t"
				+ base
				+ Constants.LINE_SEPARATOR
				+ " * @param  __v  T"
				+ base
				+ Constants.LINE_SEPARATOR
				+ "*/"
				+ Constants.LINE_SEPARATOR);
	}

	private void writeSetOuterJavaDoc(Writer w) throws IOException {
		if (w == null) return;
		w.write(
			"/**"
				+ Constants.LINE_SEPARATOR
				+ " * Sets the outer node. This method should only be called by generated classes."
				+ Constants.LINE_SEPARATOR
				+ " * @param  outer  The outer node."
				+ Constants.LINE_SEPARATOR
				+ "*/"
				+ Constants.LINE_SEPARATOR);
	}

	private void writeSetNestedJavaDoc(DoubleWriter w, Element record) throws IOException {
		String base = "he nested nodes of type " + record.getAttributeValue(CoreTags.NAME) + ".";
		w.write(
			"/**"
				+ Constants.LINE_SEPARATOR
				+ " * Sets t"
				+ base
				+ Constants.LINE_SEPARATOR
				+ " * The value may not be <code>null</code> and all array elements must be non <code>null</code>."
				+ Constants.LINE_SEPARATOR
				+ " * @param  __val  T"
				+ base
				+ Constants.LINE_SEPARATOR
				+ "*/"
				+ Constants.LINE_SEPARATOR);
	}

	private void writeSetNestedIndexedJavaDoc(DoubleWriter w, Element record) throws IOException {
		// 2014-04-24 rphall: Commented out unused local variable
//		String base = "Sets the nested " + record.getAttributeValue(CoreTags.NAME) + " at the specified index.";
		w.write(
			"/**"
				+ Constants.LINE_SEPARATOR
				+ " * Sets the nested node at the specified index."
				+ Constants.LINE_SEPARATOR
				+ " * @param   __val  The value. Must not be <code>null</code>."
				+ Constants.LINE_SEPARATOR
				+ " * @param  __index  The index."
				+ Constants.LINE_SEPARATOR
				+ "*/"
				+ Constants.LINE_SEPARATOR);
	}
	private void createInterfaceClasses(Element r, Element outer) throws GenException {
		try {
			// 2014-04-24 rphall: Commented out unused local variable
//			Set identifiers = new HashSet();
//			SrcNames srcNames = new SrcNames();
			String thisRecordName = r.getAttributeValue(CoreTags.NAME);
			String className = r.getAttributeValue(CoreTags.HOLDER_CLASS_NAME);
			String urmClassName = r.getAttributeValue(CoreTags.URM_HOLDER_CLASS_NAME);
			String baseInterfaceName = r.getAttributeValue(CoreTags.BASE_INTERFACE_NAME);
			String urmBaseInterfaceName = r.getAttributeValue(CoreTags.URM_BASE_INTERFACE_NAME);
			String interfaceName = r.getAttributeValue(CoreTags.INTERFACE_NAME);
			String fileName = getExternalSourceCodePackageRoot() + File.separator + className + ".java";
			String urmFileName = getExternalSourceCodePackageRoot() + File.separator + urmClassName + ".java";
			String interfaceFileName = getExternalSourceCodePackageRoot() + File.separator + interfaceName + ".java";
			String baseInterfaceFileName =
				getExternalSourceCodePackageRoot() + File.separator + baseInterfaceName + ".java";
			String urmBaseInterfaceFileName =
							getExternalSourceCodePackageRoot() + File.separator + urmBaseInterfaceName + ".java";

			addGeneratedFile(fileName);
			addGeneratedFile(interfaceFileName);
			addGeneratedFile(baseInterfaceFileName);

			FileOutputStream fs = new FileOutputStream(new File(fileName).getAbsoluteFile());
			FileOutputStream ifs = new FileOutputStream(new File(interfaceFileName).getAbsoluteFile());
			FileOutputStream bifs = new FileOutputStream(new File(baseInterfaceFileName).getAbsoluteFile());
			FileOutputStream ufs = null;
			FileOutputStream ubifs = null;

			Writer w1 = new OutputStreamWriter(new BufferedOutputStream(fs));
			Writer iw = new OutputStreamWriter(new BufferedOutputStream(ifs));
			Writer biw = new OutputStreamWriter(new BufferedOutputStream(bifs));
			Writer uw = null;


			DoubleWriter w;
			Writer 		 ubiw = null;

			if(outer == null && version.isDefined()){
				ufs = new FileOutputStream(new File(urmFileName).getAbsoluteFile());
				ubifs = new FileOutputStream(new File(urmBaseInterfaceFileName).getAbsoluteFile());
				addGeneratedFile(urmFileName);
				addGeneratedFile(urmBaseInterfaceFileName);
				uw = new OutputStreamWriter(new BufferedOutputStream(ufs));
				w =  new DoubleWriter(w1,uw);
				ubiw = new OutputStreamWriter(new BufferedOutputStream(ubifs));
			}
			else {
				w = new DoubleWriter(w1,null);
			}

			w.write("// Generated by ChoiceMaker. Do not edit." + Constants.LINE_SEPARATOR);
			iw.write("// Generated by ChoiceMaker. Do not edit." + Constants.LINE_SEPARATOR);
			biw.write("// Generated by ChoiceMaker. Do not edit." + Constants.LINE_SEPARATOR);
			w.write("package " + getExternalPackage() + ";" + Constants.LINE_SEPARATOR);
			iw.write("package " + getExternalPackage() + ";" + Constants.LINE_SEPARATOR);
			biw.write("package " + getExternalPackage() + ";" + Constants.LINE_SEPARATOR);
			w.write(getImports());
			iw.write(getImports());
			biw.write(getImports());

			w.write(
				"/**"
					+ Constants.LINE_SEPARATOR
					+ " * Generated holder class for the node type "
					+ thisRecordName
					+ ". See package documentation for details."
					+ Constants.LINE_SEPARATOR
					+ " */"
					+ Constants.LINE_SEPARATOR);
			w1.write(
				"public class "
					+ className
					+ " implements "
					+ getExternalPackage()
					+ "."
					+ baseInterfaceName
					+ ", java.io.Serializable {"
					+ Constants.LINE_SEPARATOR);
			if(outer == null && version.isDefined())
				uw.write(
				"public class "
					+ urmClassName
					+ " implements "
					+ getExternalPackage()
					+ "."
					+ urmBaseInterfaceName
					+ ", java.io.Serializable {"
					+ Constants.LINE_SEPARATOR);

			biw.write(
				"/**"
					+ Constants.LINE_SEPARATOR
					+ " * Generated base interface for the node type "
					+ thisRecordName
					+ ". See package documentation for details."
					+ Constants.LINE_SEPARATOR
					+ " */"
					+ Constants.LINE_SEPARATOR);

			biw.write("public interface " + baseInterfaceName + " extends java.io.Serializable {" + Constants.LINE_SEPARATOR);
			if(outer == null && version.isDefined()){
				ubiw.write("// Generated by ChoiceMaker. Do not edit." + Constants.LINE_SEPARATOR);
				ubiw.write("package " + getExternalPackage() + ";" + Constants.LINE_SEPARATOR);
				ubiw.write(getImports());
				ubiw.write(
					"/**"
						+ Constants.LINE_SEPARATOR
						+ " * Generated base interface for the node type "
						+ thisRecordName
						+ ". See package documentation for details."
						+ Constants.LINE_SEPARATOR
						+ " */"
						+ Constants.LINE_SEPARATOR);
				ubiw.write("public interface " + urmBaseInterfaceName+" extends com.choicemaker.cm.urm.base.IRecordHolder, "+baseInterfaceName+" {");
			}
				//ubiw.write("public interface " + urmBaseInterfaceName+" extends com.choicemaker.cm.urm.base.IRecordHolder {");

			iw.write(
				"/**"
					+ Constants.LINE_SEPARATOR
					+ " * Generated interface for the node type "
					+ thisRecordName
					+ ". See package documentation for details."
					+ Constants.LINE_SEPARATOR
					+ " */"
					+ Constants.LINE_SEPARATOR);
			iw.write(
				"public interface "
					+ interfaceName
					+ " extends "
					+ baseInterfaceName
					+ " {"
					+ Constants.LINE_SEPARATOR);

			List embeddedRecords = r.getChildren(CoreTags.NODE_TYPE);
			w.write(
				"/** Default constructor. Initializes all all arrays for nested record to zero length arrays and all other values to their defaults (0/null). */");
			w1.write("public " + className + "() {" + Constants.LINE_SEPARATOR);
			if(outer == null && version.isDefined())
				uw.write("public " + urmClassName + "() {" + Constants.LINE_SEPARATOR);
			for (Iterator iEmbeddedRecords = embeddedRecords.iterator(); iEmbeddedRecords.hasNext();) {
				Element e = (Element) iEmbeddedRecords.next();
				w.write(
					e.getAttributeValue(CoreTags.NAME)
						+ " = "
						+ e.getAttributeValue(CoreTags.HOLDER_CLASS_NAME)
						+ ".ZERO_ARRAY;"
						+ Constants.LINE_SEPARATOR);
			}
			w.write("}" + Constants.LINE_SEPARATOR);
			if(version.isDefined()&& outer == null)
				w.write("public void accept(com.choicemaker.cm.urm.base.IRecordVisitor ext){	ext.visit((com.choicemaker.cm.urm.base.IRecordHolder)this); }"+ Constants.LINE_SEPARATOR);

			List fields = r.getChildren("field");
			if (outer != null) {
				w.write("/** Zero length array to be used by outer node class. */" + Constants.LINE_SEPARATOR);
				w.write(
					"public static final "
						+ className
						+ "[] ZERO_ARRAY = new "
						+ className
						+ "[0];"
						+ Constants.LINE_SEPARATOR);

				String iface = outer.getAttributeValue(CoreTags.BASE_INTERFACE_NAME);
				w.write(addField(iface, "outer", false, PRIVATE));

				writeGetOuterJavaDoc(w1);
				writeGetOuterJavaDoc(uw);
				w.write("public " + iface + " getOuter() {" + Constants.LINE_SEPARATOR);
				w.write("return outer;" + Constants.LINE_SEPARATOR);
				w.write("}" + Constants.LINE_SEPARATOR);

				writeGetOuterJavaDoc(biw);
				biw.write("public " + iface + " getOuter();" + Constants.LINE_SEPARATOR);

				writeSetOuterJavaDoc(w1);
				writeSetOuterJavaDoc(uw);
				w.write("public void setOuter(" + iface + " outer) {" + Constants.LINE_SEPARATOR);
				w.write("this.outer = outer;" + Constants.LINE_SEPARATOR);
				w.write("}" + Constants.LINE_SEPARATOR);

				writeSetOuterJavaDoc(biw);
				biw.write("public void setOuter(" + iface + " outer);" + Constants.LINE_SEPARATOR);

			}
			Iterator i = fields.iterator();
			DerivedSource beanSource = DerivedSource.valueOf("bean");
			String keyFiledName=null;
			String keyFiledType=null;
			while (i.hasNext()) {
				Element e = (Element) i.next();
				String typeName = e.getAttributeValue("type");
				String fieldName = e.getAttributeValue("name");
				if ("true".equals(e.getAttributeValue("key"))){
					keyFiledName =	fieldName;
					keyFiledType =	typeName;
				}

				boolean trans = "true".equals(e.getAttributeValue("transient"));
				boolean derived = GeneratorHelper.isDerived(e, beanSource);
				String methodStem = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
				String getMethod = "public " + typeName + " get" + methodStem + "()";
				String validMethod = "public boolean is" + methodStem + "Valid()";
				if (!derived) {
					w.write(addField("boolean", fieldName + "Valid", false, PRIVATE));
					w.write(addField(typeName, fieldName, trans, PROTECTED));
					writeValidJavaDoc(w1, e);
					writeValidJavaDoc(uw, e);
					w.write(validMethod + " {" + Constants.LINE_SEPARATOR);
					w.write("return " + fieldName + "Valid;" + Constants.LINE_SEPARATOR);
					w.write("}" + Constants.LINE_SEPARATOR);
					writeValidJavaDoc(biw, e);
					biw.write(validMethod + ";" + Constants.LINE_SEPARATOR);
					writeGetFieldJavaDoc(w1, e);
					writeGetFieldJavaDoc(uw, e);
					w.write(getMethod + " {" + Constants.LINE_SEPARATOR);
					w.write("return " + fieldName + ";" + Constants.LINE_SEPARATOR);
					w.write("}" + Constants.LINE_SEPARATOR);
					writeGetFieldJavaDoc(biw, e);
					biw.write(getMethod + ";" + Constants.LINE_SEPARATOR);
					writeSetFieldJavaDoc(w, e);
					w.write("public void set" + methodStem + "(" + typeName + " __v) {" + Constants.LINE_SEPARATOR);
					w.write("this." + fieldName + " = __v;" + Constants.LINE_SEPARATOR);
					w.write("}" + Constants.LINE_SEPARATOR);
				} else if(!GeneratorHelper.isNodeInitScope(e)) {
					writeValidJavaDoc(iw, e);
					iw.write(validMethod + ";" + Constants.LINE_SEPARATOR);
					writeGetFieldJavaDoc(iw, e);
					iw.write(getMethod + ";" + Constants.LINE_SEPARATOR);
				}
			}
			if(outer == null && version.isDefined() && keyFiledName != null){
				writeGetIdJavaDoc(uw);
				uw.write("public java.lang.Comparable getId() {" + Constants.LINE_SEPARATOR);
				String retResval = null;
				boolean isString = false;
				if (keyFiledType.equals("byte"))
					retResval = "new Byte(";
				else if (keyFiledType.equals("short"))
					retResval = "new Short(";
				else if (keyFiledType.equals("char"))
				retResval = "new Char(";
				else if (keyFiledType.equals("int"))
				retResval = "new Integer(";
				else if (keyFiledType.equals("long"))
				retResval = "new Long(";
				else if (keyFiledType.equals("float"))
				retResval = "new Float(";
				else if (keyFiledType.equals("double"))
				retResval = "new Double(";
				else if (keyFiledType.equals("boolean"))
				retResval = "new Boolean(";
				else if (keyFiledType.equals("String"))
					isString = true;
				else
					throw new GenException("Invalid data type");
				//TODO check the list of valid datatypes and test
				if(isString)
					uw.write("return "+keyFiledName+";" + Constants.LINE_SEPARATOR);
				else
					uw.write("return "+retResval+keyFiledName+");" + Constants.LINE_SEPARATOR);
				uw.write("}" + Constants.LINE_SEPARATOR);
			}

			i = embeddedRecords.iterator();
			while (i.hasNext()) {
				Element e = (Element) i.next();
				String typeName = e.getAttributeValue(CoreTags.BASE_INTERFACE_NAME);
				String recordName = e.getAttributeValue("name");
				w.write(addArrayField(typeName, recordName, PROTECTED));
				String methodStem = Character.toUpperCase(recordName.charAt(0)) + recordName.substring(1);
				String getMethod = "public " + typeName + "[] get" + methodStem + "()";
				writeGetNestedJavaDoc(w1, e);
				writeGetNestedJavaDoc(uw, e);
				w.write(getMethod + " {" + Constants.LINE_SEPARATOR);
				w.write("return " + recordName + ";" + Constants.LINE_SEPARATOR);
				w.write("}" + Constants.LINE_SEPARATOR);
				writeGetNestedJavaDoc(biw, e);
				biw.write(getMethod + ";" + Constants.LINE_SEPARATOR);
				writeSetNestedJavaDoc(w, e);
				w.write("public void set" + methodStem + "(" + typeName + "[] __val) {" + Constants.LINE_SEPARATOR);
				w.write("this." + recordName + " = __val;" + Constants.LINE_SEPARATOR);
				if (outer != null) {
					w.write("for(int __i = 0; __i < " + recordName + ".length; ++__i) {" + Constants.LINE_SEPARATOR);
					w.write("__val[__i].setOuter(this);" + Constants.LINE_SEPARATOR);
					w.write("}" + Constants.LINE_SEPARATOR);
				}
				w.write("}" + Constants.LINE_SEPARATOR);

				String indexGetMethod = "public " + typeName + " get" + methodStem + "(int __index)";
				writeGetNestedIndexedJavaDoc(w1, e);
				writeGetNestedIndexedJavaDoc(uw, e);
				w.write(indexGetMethod + " {" + Constants.LINE_SEPARATOR);
				w.write("return " + recordName + "[__index];" + Constants.LINE_SEPARATOR);
				w.write("}" + Constants.LINE_SEPARATOR);
				writeGetNestedIndexedJavaDoc(biw, e);
				biw.write(indexGetMethod + ";" + Constants.LINE_SEPARATOR);
				writeSetNestedIndexedJavaDoc(w, e);
				w.write(
					"public void set"
						+ methodStem
						+ "(int __index, "
						+ typeName
						+ " __val) {"
						+ Constants.LINE_SEPARATOR);
				w.write("this." + recordName + "[__index] = __val;" + Constants.LINE_SEPARATOR);
				if (outer != null) {
					w.write("__val.setOuter(this);" + Constants.LINE_SEPARATOR);
				}
				w.write("}" + Constants.LINE_SEPARATOR);
			}

			w.write(
				"/** Copy constructor. Performs a deep copy of the nodes, but not the values."
					+ Constants.LINE_SEPARATOR
					+ " * @param  __o  The node to copy."
					+ Constants.LINE_SEPARATOR
					+ "*/"
					+ Constants.LINE_SEPARATOR);
			w1.write("public " + className + "(" + baseInterfaceName + " __o) {" + Constants.LINE_SEPARATOR);
			if(outer == null && version.isDefined())
				uw.write("public " + urmClassName + "(" + baseInterfaceName + " __o) {" + Constants.LINE_SEPARATOR);
			for (Iterator iFields = fields.iterator(); iFields.hasNext();) {
				Element field = (Element) iFields.next();
				String name = field.getAttributeValue(CoreTags.NAME);
				String methodStem = Character.toUpperCase(name.charAt(0)) + name.substring(1);
				if (!GeneratorHelper.isDerived(field, beanSource)) {
					w.write(name + " = __o.get" + methodStem + "();" + Constants.LINE_SEPARATOR);
					w.write(name + "Valid = __o.is" + methodStem + "Valid();" + Constants.LINE_SEPARATOR);
				}
			}
			for (Iterator iEmbeddedRecords = embeddedRecords.iterator(); iEmbeddedRecords.hasNext();) {
				Element record = (Element) iEmbeddedRecords.next();
				String name = record.getAttributeValue(CoreTags.NAME);
				String iface = record.getAttributeValue(CoreTags.BASE_INTERFACE_NAME);
				w.write(
					iface
						+ "[] __o"
						+ name
						+ " = __o.get"
						+ Character.toUpperCase(name.charAt(0))
						+ name.substring(1)
						+ "();"
						+ Constants.LINE_SEPARATOR);
				w.write(name + " = new " + iface + "[__o" + name + ".length];" + Constants.LINE_SEPARATOR);
				w.write("for(int __i = 0; __i < " + name + ".length; ++__i) {" + Constants.LINE_SEPARATOR);
				w.write(
					"("
						+ name
						+ "[__i] = new "
						+ record.getAttributeValue(CoreTags.HOLDER_CLASS_NAME)
						+ "(__o"
						+ name
						+ "[__i])).setOuter(this);"
						+ Constants.LINE_SEPARATOR);
				w.write("}" + Constants.LINE_SEPARATOR);
			}
			w.write("}" + Constants.LINE_SEPARATOR);
			w.write("}" + Constants.LINE_SEPARATOR);
			iw.write("}" + Constants.LINE_SEPARATOR);
			biw.write("}" + Constants.LINE_SEPARATOR);
			w1.flush();
			iw.flush();
			biw.flush();
			if(outer == null && version.isDefined()){
				uw.flush();
				ubiw.write("}" + Constants.LINE_SEPARATOR);
				ubiw.flush();
				ufs.close();
				ubifs.close();

			}
			fs.close();
			ifs.close();
			bifs.close();
			i = embeddedRecords.iterator();
			while (i.hasNext()) {
				createInterfaceClasses((Element) i.next(), r);
			}
		} catch (IOException ex) {
			throw new GenException("Problem writing file.", ex);
		}
	}

	private void createHolderClasses(Element r, Element outer) throws GenException {
		try {
			Set identifiers = new HashSet();
			SrcNames srcNames = new SrcNames();
			String className = r.getAttributeValue("className");
			String interfaceName = r.getAttributeValue(CoreTags.INTERFACE_NAME);
			String baseInterfaceName = r.getAttributeValue(CoreTags.BASE_INTERFACE_NAME);
			String fileName = getSourceCodePackageRoot() + File.separator + className + ".java";
			addGeneratedFile(fileName);
			FileOutputStream fs = new FileOutputStream(new File(fileName).getAbsoluteFile());
			Writer w = new OutputStreamWriter(new BufferedOutputStream(fs));
			DerivedSource beanSource = DerivedSource.valueOf("bean");
			w.write("// Generated by ChoiceMaker. Do not edit." + Constants.LINE_SEPARATOR);
			w.write("package " + getPackage() + ";" + Constants.LINE_SEPARATOR);
			w.write("import com.choicemaker.cm.core.*;" + Constants.LINE_SEPARATOR);
			w.write("import com.choicemaker.cm.core.base.*;" + Constants.LINE_SEPARATOR);
			w.write("import org.apache.log4j.*;");
			w.write(getImports());
			w.write("import " + getExternalPackage() + ".*;" + Constants.LINE_SEPARATOR);
			w.write(
				"public class "
					+ className
					+ " implements "
					+ (outer == null ? "Record" : "BaseRecord")
					+ ", "
					+ interfaceName
					+ " {"
					+ Constants.LINE_SEPARATOR);
			w.write(
				"private static Logger logger = Logger.getLogger("
					+ getPackage()
					+ "."
					+ className
					+ ".class);"
					+ Constants.LINE_SEPARATOR);
			List fields = r.getChildren("field");
			if (outer == null) {
				cu.setBaseType(className);
				w.write("private com.choicemaker.cm.core.DerivedSource __src;" + Constants.LINE_SEPARATOR);
				w.write("public DerivedSource getDerivedSource() {" + Constants.LINE_SEPARATOR);
				w.write("return __src;" + Constants.LINE_SEPARATOR);
				w.write("}" + Constants.LINE_SEPARATOR);
				//w.write("public void accept(com.choicemaker.cm.urm.base.IRecordVisitor ext){ }"+ Constants.LINE_SEPARATOR);
				w.write("public void computeValidityAndDerived() {" + Constants.LINE_SEPARATOR);
				w.write("resetValidityAndDerived(__src);" + Constants.LINE_SEPARATOR);
				w.write("computeValidityAndDerived(__src);" + Constants.LINE_SEPARATOR);
				w.write("}" + Constants.LINE_SEPARATOR);
				w.write("public Comparable getId() {" + Constants.LINE_SEPARATOR);
				w.write("return ");
				Iterator i = fields.iterator();
				boolean keyDefd = false;
				while (i.hasNext()) {
					Element e = (Element) i.next();
					if ("true".equals(e.getAttributeValue("key"))) {
						String s =
							GeneratorHelper.getObjectExpr(e.getAttributeValue("type"), e.getAttributeValue("name"));
						w.write(s + ";" + Constants.LINE_SEPARATOR);
						keyDefd = true;
						break;
					}
				}
				w.write("}" + Constants.LINE_SEPARATOR);
				if (!keyDefd) {
					error("Root record must define key field");
				}
			} else {
				w.write(
					"public static "
						+ className
						+ "[] __zeroArray = new "
						+ className
						+ "[0];"
						+ Constants.LINE_SEPARATOR);
				String ocn = outer.getAttributeValue("className");
				w.write(addField(ocn, "outer", false, PUBLIC));
				String iface = outer.getAttributeValue(CoreTags.BASE_INTERFACE_NAME);
				w.write("public " + iface + " getOuter() {" + Constants.LINE_SEPARATOR);
				w.write("return outer;" + Constants.LINE_SEPARATOR);
				w.write("}" + Constants.LINE_SEPARATOR);
				w.write("public void setOuter(" + iface + " outer) {" + Constants.LINE_SEPARATOR);
				w.write("this.outer = (" + ocn + ")outer;" + Constants.LINE_SEPARATOR);
				w.write("}" + Constants.LINE_SEPARATOR);
			}
			Iterator i = fields.iterator();
			while (i.hasNext()) {
				Element field = (Element) i.next();
				if (!GeneratorHelper.isNodeInitScope(field)) {
					w.write(addField("boolean", "__v_" + (field).getAttributeValue("name"), false, PUBLIC));
				}
			}
			i = fields.iterator();
			while (i.hasNext()) {
				Element e = (Element) i.next();
				if (GeneratorHelper.isNodeInitScope(e)) {
					if (!derivedAll(e)) {
						error("Field with nodeInit scope must be derived for all sources.");
					}
				} else {
					String typeName = e.getAttributeValue("type");
					String fieldName = e.getAttributeValue("name");
					boolean trans = "true".equals(e.getAttributeValue("transient"));
					identifierCheck(fieldName, identifiers);
					// check type
					cu.addField(className, typeName, fieldName);
					w.write(addField(typeName, fieldName, trans, PUBLIC));
					String methodStem = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
					String validMethod = "public boolean is" + methodStem + "Valid()";
					w.write(validMethod + " {" + Constants.LINE_SEPARATOR);
					w.write("return __v_" + fieldName + ";" + Constants.LINE_SEPARATOR);
					w.write("}" + Constants.LINE_SEPARATOR);
					String getMethod = "public " + typeName + " get" + methodStem + "()";
					w.write(getMethod + " {" + Constants.LINE_SEPARATOR);
					w.write("return " + fieldName + ";" + Constants.LINE_SEPARATOR);
					w.write("}" + Constants.LINE_SEPARATOR);
				}
			}
			List embeddedRecords = r.getChildren(CoreTags.NODE_TYPE);
			i = embeddedRecords.iterator();
			while (i.hasNext()) {
				Element e = (Element) i.next();
				String typeName = e.getAttributeValue("className");
				String recordName = e.getAttributeValue("name");
				String iface = e.getAttributeValue(CoreTags.BASE_INTERFACE_NAME);
				identifierCheck(recordName, identifiers);
				cu.addNestedRecord(className, typeName, recordName);
				w.write(addArrayField(typeName, recordName, PUBLIC));
				String methodStem = Character.toUpperCase(recordName.charAt(0)) + recordName.substring(1);
				String getMethod = "public " + iface + "[] get" + methodStem + "()";
				w.write(getMethod + " {" + Constants.LINE_SEPARATOR);
				w.write("return " + recordName + ";" + Constants.LINE_SEPARATOR);
				w.write("}" + Constants.LINE_SEPARATOR);
				String indexGetMethod = "public " + iface + " get" + methodStem + "(int __index)";
				w.write(indexGetMethod + " {" + Constants.LINE_SEPARATOR);
				w.write("return " + recordName + "[__index];" + Constants.LINE_SEPARATOR);
				w.write("}" + Constants.LINE_SEPARATOR);
			}

			w.write("public " + className + "(" + baseInterfaceName + " __o) {" + Constants.LINE_SEPARATOR);
			for (Iterator iFields = fields.iterator(); iFields.hasNext();) {
				Element field = (Element) iFields.next();
				if (!GeneratorHelper.isDerived(field, beanSource)) {
					String name = field.getAttributeValue(CoreTags.NAME);
					String methodStem = Character.toUpperCase(name.charAt(0)) + name.substring(1);
					w.write(name + " = " + "__o.get" + methodStem + "();" + Constants.LINE_SEPARATOR);
				}
			}
			for (Iterator iEmbeddedRecords = embeddedRecords.iterator(); iEmbeddedRecords.hasNext();) {
				Element record = (Element) iEmbeddedRecords.next();
				String name = record.getAttributeValue(CoreTags.NAME);
				String ncn = record.getAttributeValue(CoreTags.CLASS_NAME);
				String iface = record.getAttributeValue(CoreTags.BASE_INTERFACE_NAME);
				w.write(
					iface
						+ "[] __o"
						+ name
						+ " = __o.get"
						+ Character.toUpperCase(name.charAt(0))
						+ name.substring(1)
						+ "();"
						+ Constants.LINE_SEPARATOR);
				w.write(name + " = new " + ncn + "[__o" + name + ".length];" + Constants.LINE_SEPARATOR);
				w.write("for(int __i = 0; __i < " + name + ".length; ++__i) {" + Constants.LINE_SEPARATOR);
				w.write(
					"("
						+ name
						+ "[__i] = new "
						+ ncn
						+ "(__o"
						+ name
						+ "[__i])).setOuter(this);"
						+ Constants.LINE_SEPARATOR);
				w.write("}" + Constants.LINE_SEPARATOR);
			}
			w.write("}" + Constants.LINE_SEPARATOR);
			w.write("public " + className + "() {" + Constants.LINE_SEPARATOR);
			w.write("}" + Constants.LINE_SEPARATOR);

			w.write("public void computeValidityAndDerived(DerivedSource __src) {" + Constants.LINE_SEPARATOR);
			w.write("java.lang.String __tmpStr;" + Constants.LINE_SEPARATOR);
			if (outer == null) {
				w.write("this.__src = __src;" + Constants.LINE_SEPARATOR);
			}
			w.write("try {" + Constants.LINE_SEPARATOR);
			i = fields.iterator();
			while (i.hasNext()) {
				Element field = (Element) i.next();
				Element d = field.getChild("derived");
				if (d != null) {
					boolean nodeInitScope = GeneratorHelper.isNodeInitScope(field);
					if (nodeInitScope) {
						String type = field.getAttributeValue(CoreTags.TYPE);
						String name = field.getAttributeValue(CoreTags.NAME);
						w.write(
							type
								+ " "
								+ name
								+ " = "
								+ GeneratorHelper.getNullValue(type)
								+ ";"
								+ Constants.LINE_SEPARATOR);
						w.write("boolean __v_" + name + " = false;" + Constants.LINE_SEPARATOR);
					}
					String fieldName = field.getAttributeValue("name");
					int srcId = srcNames.getId(d.getAttributeValue("src"));
					w.write("if(__src" + srcId + ".includes(__src)) {" + Constants.LINE_SEPARATOR);
					String pre = d.getAttributeValue("pre");
					if (pre != null) {
						w.write("if(" + ValidConverter.convertValids(pre) + ") {" + Constants.LINE_SEPARATOR);
					}
					String type = field.getAttributeValue("type").intern();
					String value = ValidConverter.convertValids(d.getAttributeValue("value"));
					if (isIntern() && (type == "String" || type == "java.lang.String")) {
						w.write(
							fieldName
								+ " = (__tmpStr = ("
								+ value
								+ ")) != null ? __tmpStr.intern() : null;"
								+ Constants.LINE_SEPARATOR);
					} else {
						w.write(fieldName + " = " + value + ";" + Constants.LINE_SEPARATOR);
					}
					writeValid(w, field);
					if (pre != null) {
						w.write("}" + Constants.LINE_SEPARATOR);
					}
					w.write("} else {" + Constants.LINE_SEPARATOR);
					writeValid(w, field);
					w.write("}" + Constants.LINE_SEPARATOR);
				} else {
					writeValid(w, field);
				}
			}
			// NEED A CALLBACK HERE FOR ITERATED NODES
			i = embeddedRecords.iterator();
			while (i.hasNext()) {
				Element e = (Element) i.next();
				String eName = e.getAttributeValue(CoreTags.NAME);
				// 2005-08-19 CHANGED: added processing for iterated nodes (rphall)
				Element iteratedNodeType = GeneratorHelper.getNodeTypeExt(e,"iterated"); // FIXME DEFINE CoreTags: iteratedNode and assigned
				if (iteratedNodeType == null) {
					w.write("for(int i = 0; i < " + eName + ".length; ++i) {" + Constants.LINE_SEPARATOR);
					w.write(eName + "[i].computeValidityAndDerived(__src);" + Constants.LINE_SEPARATOR);
					w.write("}" + Constants.LINE_SEPARATOR);
				} else {

					String srcName = iteratedNodeType.getAttributeValue("src");
					int srcId = srcNames.getId(srcName);

					w.write("if(__src" + srcId + ".includes(__src)) {" + Constants.LINE_SEPARATOR);
					String eClassName = e.getAttributeValue(CoreTags.CLASS_NAME);
					// 2014-04-24 rphall: Commented out unused local variable
					// Any side effects?
//					String eIFace = e.getAttributeValue(CoreTags.BASE_INTERFACE_NAME);
					String iterator = iteratedNodeType.getAttributeValue("iterator");
					String assignedFieldName = iteratedNodeType.getAttributeValue("assignedField");

					Element assignedField = GeneratorHelper.findField(e,assignedFieldName);
					if (assignedField==null) {
						throw new GenException("missing assigned field named '" + assignedFieldName + "'");
					}

					Element assignedMarker = assignedField.getChild("assigned");
					if (assignedMarker==null) {
						throw new GenException("missing 'assigned' child element for the assigned field named '" + assignedFieldName + "'");
					}

					String assignedType = assignedField.getAttributeValue("type");
					w.write(eName + " = null;" + Constants.LINE_SEPARATOR);
					final String listName = "__l_" + eName;
					final String listItemName = "__li_" + eName;
					w.write("List " + listName + " = new ArrayList();" + Constants.LINE_SEPARATOR);
					w.write("for(Iterator i = " + iterator + "; i.hasNext(); ) {" + Constants.LINE_SEPARATOR);
					w.write(assignedType + " __iterItem = (" + assignedType + ") i.next();" + Constants.LINE_SEPARATOR);
					w.write(eClassName + " " + listItemName + " = new " + eClassName + "();" + Constants.LINE_SEPARATOR);
					w.write(listItemName + "." + assignedFieldName + " = __iterItem;" + Constants.LINE_SEPARATOR);
					w.write(listItemName + ".computeValidityAndDerived(__src);" + Constants.LINE_SEPARATOR);
					w.write(listName + ".add(" + listItemName + ");" + Constants.LINE_SEPARATOR);
					w.write("}" + Constants.LINE_SEPARATOR);
					w.write(eName + " = (" + eClassName + "[]) " + listName + ".toArray(new " + eClassName + "[" + listName + ".size()]);" + Constants.LINE_SEPARATOR);

					w.write("} else {" + Constants.LINE_SEPARATOR);
					w.write("for(int i = 0; i < " + eName + ".length; ++i) {" + Constants.LINE_SEPARATOR);
					w.write(eName + "[i].computeValidityAndDerived(__src);" + Constants.LINE_SEPARATOR);
					w.write("}" + Constants.LINE_SEPARATOR);
					w.write("}" + Constants.LINE_SEPARATOR);

				}
				// 2005-08-19 END CHANGED
			}
			w.write("} catch(Exception __ex) {" + Constants.LINE_SEPARATOR);
			w.write(
				"logger.error(\"Computing validity and derived of "
					+ className
					+ "\", __ex);"
					+ Constants.LINE_SEPARATOR);
			w.write("}" + Constants.LINE_SEPARATOR);
			w.write("}" + Constants.LINE_SEPARATOR);
			w.write("public void resetValidityAndDerived(DerivedSource __src) {" + Constants.LINE_SEPARATOR);
			i = fields.iterator();
			while (i.hasNext()) {
				Element field = (Element) i.next();
				Element d = field.getChild("derived");
				if (d != null && !GeneratorHelper.isNodeInitScope(field)) {
					String fieldName = field.getAttributeValue("name");
					int srcId = srcNames.getId(d.getAttributeValue("src"));
					w.write("if(__src" + srcId + ".includes(__src)) {" + Constants.LINE_SEPARATOR);
					w.write(
						fieldName
							+ " = "
							+ GeneratorHelper.getNullValue(field.getAttributeValue("type"))
							+ ";"
							+ Constants.LINE_SEPARATOR);
					w.write("__v_" + fieldName + " = false;" + Constants.LINE_SEPARATOR);
					w.write("}" + Constants.LINE_SEPARATOR);
				}
			}
			i = embeddedRecords.iterator();
			while (i.hasNext()) {
				Element e = (Element) i.next();
				String eName = e.getAttributeValue(CoreTags.NAME);
				// 2005-08-19 CHANGED: added processing for iterated nodes (rphall)
				Element iteratedNodeType = GeneratorHelper.getNodeTypeExt(e,"iterated"); // FIXME DEFINE CoreTags: iteratedNode and assigned
				if (iteratedNodeType == null) {
					w.write("for(int i = 0; i < " + eName + ".length; ++i) {" + Constants.LINE_SEPARATOR);
					w.write(eName + "[i].resetValidityAndDerived(__src);" + Constants.LINE_SEPARATOR);
					w.write("}" + Constants.LINE_SEPARATOR);
				} else {
					String srcName = iteratedNodeType.getAttributeValue("src");
					int srcId = srcNames.getId(srcName);
					w.write("if(__src" + srcId + ".includes(__src)) {" + Constants.LINE_SEPARATOR);
					String eClassName = e.getAttributeValue(CoreTags.CLASS_NAME);
					w.write(eName + " = new " + eClassName + "[0];" + Constants.LINE_SEPARATOR);
					w.write("} else {" + Constants.LINE_SEPARATOR);
					w.write("for(int i = 0; i < " + eName + ".length; ++i) {" + Constants.LINE_SEPARATOR);
					w.write(eName + "[i].resetValidityAndDerived(__src);" + Constants.LINE_SEPARATOR);
					w.write("}" + Constants.LINE_SEPARATOR);
					w.write("}" + Constants.LINE_SEPARATOR);
				}
				// 2005-08-19 END CHANGED
			}
			w.write("}" + Constants.LINE_SEPARATOR);
			w.write("public static " + className + " instance() {" + Constants.LINE_SEPARATOR);
			w.write(className + " tmpInstance = new " + className + "();" + Constants.LINE_SEPARATOR);
			i = embeddedRecords.iterator();
			while (i.hasNext()) {
				Element e = (Element) i.next();
				String eName = e.getAttributeValue("name");
				String eClassName = e.getAttributeValue("className");
				w.write("tmpInstance." + eName + " = new " + eClassName + "[0];" + Constants.LINE_SEPARATOR);
			}
			w.write("return tmpInstance;" + Constants.LINE_SEPARATOR);
			w.write("}" + Constants.LINE_SEPARATOR);

			//ELMUS 8/29/2006 START
			List methodsTags = r.getChildren("method");
			Iterator methIter = methodsTags.iterator();
			while (methIter.hasNext()) {
				Element e = (Element) methIter.next();
				String methodCode = e.getText();
				w.write(methodCode + Constants.LINE_SEPARATOR);
			}
			//ELMUS 8/29/2006 END

			w.write(srcNames.getDeclarations());
			w.write("}" + Constants.LINE_SEPARATOR);
			w.flush();
			fs.close();
			i = embeddedRecords.iterator();
			while (i.hasNext()) {
				createHolderClasses((Element) i.next(), r);
			}
		} catch (GenException ex) {
			throw ex;
		} catch (IOException ex) {
			throw new GenException("Problem writing file.", ex);
		} catch (CompilerException ex) {
		throw new GenException("Problem compiling file.", ex);
	}
	}

	private boolean derivedAll(Element e) {
		Element derived = e.getChild(CoreTags.DERIVED);
		if (derived != null) {
			String src = derived.getAttributeValue(CoreTags.SRC);
			return src == null || "all".equals(src);
		}
		return false;
	}

	private void writeValid(Writer w, Element field) throws IOException {
		String valid = field.getAttributeValue("valid");
		String fieldName = field.getAttributeValue("name");
		if (valid == null) {
			valid = "true";
		} else {
			valid = ValidConverter.convertValids(valid);
		}
		w.write("__v_" + fieldName + " = " + valid + ";" + Constants.LINE_SEPARATOR);
	}

	private String addField(String typeName, String fieldName, boolean trans, String modifier) {
		return modifier
			+ " "
			+ (trans ? "transient " : "")
			+ typeName
			+ " "
			+ fieldName
			+ ";"
			+ Constants.LINE_SEPARATOR;
	}

	private String addArrayField(String typeName, String fieldName, String modifier) {
		return modifier + " " + typeName + "[] " + fieldName + ";" + Constants.LINE_SEPARATOR;
	}

	private boolean identifierCheck(String name, Set identifiers) {
		if (name == null || name.length() == 0) {
			error("No or empty name.");
			return false;
		}
		if (Scanner.keys.get(name) != null) {
			error("Illegal name. " + name + " is a ClueMaker keyword.");
			return false;
		} else if (name.length() == 0) {
			error("Empty identifier.");
			return false;
		} else if (!Character.isJavaIdentifierStart(name.charAt(0))) {
			error("Illegal identifier: " + name);
			return false;
		} else {
			for (int i = 1; i < name.length(); ++i) {
				if (!Character.isJavaIdentifierPart(name.charAt(i))) {
					error("Illegal identifier: " + name);
					return false;
				}
			}
		}
		if (identifiers != null) {
			if (identifiers.contains(name)) {
				error("Duplicate identifier: " + name);
				return false;
			}
			identifiers.add(name);
		}
		return true;
	}

	private void createAccessor() {
		accessorImports = new StringBuffer();
		accessorImplements = new StringBuffer();
		accessorBody = new StringBuffer();
	}

	private void finishAccessor() throws GenException {
		String className = getClueSetName() + "Accessor";
		cu.setAccessorClass(getPackage() + "." + className);
		String fileName = getSourceCodePackageRoot() + File.separator + className + ".java";
		addGeneratedFile(fileName);
		try {
			FileOutputStream fs = new FileOutputStream(new File(fileName).getAbsoluteFile());
			Writer w = new OutputStreamWriter(new BufferedOutputStream(fs));
			w.write("// Generated by ChoiceMaker. Do not edit." + Constants.LINE_SEPARATOR);
			w.write("package " + getPackage() + ";" + Constants.LINE_SEPARATOR);
			w.write("import java.io.Serializable;" + Constants.LINE_SEPARATOR);
			w.write("import com.choicemaker.cm.core.*;" + Constants.LINE_SEPARATOR);
			w.write("import com.choicemaker.cm.core.base.*;" + Constants.LINE_SEPARATOR);
			w.write(accessorImports.toString());
			w.write("import " + getExternalPackage() + ".*;" + Constants.LINE_SEPARATOR);
			w.write(
				"public class "
					+ className
					+ " implements java.io.Serializable, com.choicemaker.cm.core.Accessor"
					+ accessorImplements
					+ "{"
					+ Constants.LINE_SEPARATOR);
			w.write("public ClueSet getClueSet() {" + Constants.LINE_SEPARATOR);
			w.write("return new " + getClueSetName() + "ClueSet();" + Constants.LINE_SEPARATOR);
			w.write("}" + Constants.LINE_SEPARATOR);
			w.write("public Descriptor getDescriptor() {" + Constants.LINE_SEPARATOR);
			w.write(
				"return "
					+ getRootRecord().getAttributeValue("className")
					+ "____descriptor.instance;"
					+ Constants.LINE_SEPARATOR);
			w.write("}" + Constants.LINE_SEPARATOR);
			w.write("public String getSchemaFileName() {" + Constants.LINE_SEPARATOR);
			w.write(
				"return \"" + schemaFileName.replace('\\', '/').replace('\"', '\'') + "\";" + Constants.LINE_SEPARATOR);
			w.write("}" + Constants.LINE_SEPARATOR);
			w.write("public long getCreationDate() {" + Constants.LINE_SEPARATOR);
			w.write("return " + new Date().getTime() + "L;" + Constants.LINE_SEPARATOR);
			w.write("}" + Constants.LINE_SEPARATOR);
			w.write("public int getNumRecordTypes() {" + Constants.LINE_SEPARATOR);
			w.write("return " + (recordNumber - 1) + ";" + Constants.LINE_SEPARATOR);
			w.write("}" + Constants.LINE_SEPARATOR);
			w.write("public String getClueSetName() {" + Constants.LINE_SEPARATOR);
			w.write("return \"" + getClueSetName() + "\";" + Constants.LINE_SEPARATOR);
			w.write("}" + Constants.LINE_SEPARATOR);
			w.write("public String getSchemaName() {" + Constants.LINE_SEPARATOR);
			w.write("return \"" + schemaName + "\";" + Constants.LINE_SEPARATOR);
			w.write("}" + Constants.LINE_SEPARATOR);
			w.write(
				"private final DerivedSource BEAN_SOURCE = DerivedSource.valueOf(\"bean\");"
					+ Constants.LINE_SEPARATOR);
			Element rootRecord = getRootRecord();
			String rootHolder = rootRecord.getAttributeValue(CoreTags.HOLDER_CLASS_NAME);
			String rootURMHolder = rootRecord.getAttributeValue(CoreTags.URM_HOLDER_CLASS_NAME);
			String implClass = rootRecord.getAttributeValue(CoreTags.CLASS_NAME);
			String ifaceName = rootRecord.getAttributeValue(CoreTags.BASE_INTERFACE_NAME);
			// 2014-04-24 rphall: Commented out unused local variable
			// Any side effects?
//			String iURMFaceName = rootRecord.getAttributeValue(CoreTags.URM_BASE_INTERFACE_NAME);

			w.write("public Object toHolder(Record r) {" + Constants.LINE_SEPARATOR);
			w.write("return new " + rootHolder + "((" + ifaceName + ")r);" + Constants.LINE_SEPARATOR);
			w.write("}" + Constants.LINE_SEPARATOR);

			//By PC 10/10/06 - need to return recordHolder
			w.write("public Object toRecordHolder(Record r) {" + Constants.LINE_SEPARATOR);
			w.write("return new " + rootURMHolder + "((" + ifaceName + ")r);" + Constants.LINE_SEPARATOR);
			w.write("}" + Constants.LINE_SEPARATOR);
			//end by PC

			w.write("public Record toImpl(Object o) {" + Constants.LINE_SEPARATOR);
			w.write(implClass + " __res = new " + implClass + "((" + ifaceName + ")o);" + Constants.LINE_SEPARATOR);
			w.write("__res.computeValidityAndDerived(BEAN_SOURCE);" + Constants.LINE_SEPARATOR);
			w.write("return __res;" + Constants.LINE_SEPARATOR);
			w.write("}" + Constants.LINE_SEPARATOR);
			w.write(new String(accessorBody));
			w.write("}" + Constants.LINE_SEPARATOR);
			w.flush();
			fs.close();
		} catch (IOException ex) {
			throw new GenException("Problem writing file.", ex);
		}
	}

	/**
	 * Adds to the import section of the Accessor class.
	 *
	 * @param   imp  The text to be added to the import section.
	 */
	public void addAccessorImport(String imp) {
		accessorImports.append(imp);
	}

	/**
	 * Adds to the implements list of the Accessor class.
	 *
	 * @param   imp  The text to be added to the implements list.
	 *            Must start with a comma, e.g., <code>", OraAccessor"</code>.
	 */
	public void addAccessorImplements(String imp) {
//		accessorImplements.append(imp);
	}

	/**
	 * Adds to the body section of the Accessor class.
	 *
	 * @param   decls  The text to be added to the body section.
	 */
	public void addAccessorBody(String decls) {
		accessorBody.append(decls);
	}

}
