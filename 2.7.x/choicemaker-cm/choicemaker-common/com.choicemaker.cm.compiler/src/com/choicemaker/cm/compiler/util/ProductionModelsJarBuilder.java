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
package com.choicemaker.cm.compiler.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.tools.ant.util.JavaEnvUtils;

import com.choicemaker.cm.compiler.impl.CompilerFactory;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.core.compiler.CompilationArguments;
import com.choicemaker.cm.core.compiler.ICompiler;
import com.choicemaker.cm.core.configure.ConfigurationManager;
import com.choicemaker.cm.core.util.FileUtilities;
import com.choicemaker.cm.core.util.ObjectMaker;
import com.choicemaker.cm.core.util.StreamRelayer;
import com.choicemaker.cm.core.xmlconf.ProbabilityModelsXmlConf;

/**
 * @author    Adam Winkel
 * @version
 */
public class ProductionModelsJarBuilder {

	private static boolean DELETE_TEMP = true;

	/**
	 * Runs this class as an Eclipse command-line application.
	 * @param args command-line arguments for the application. One is
	 * required:<ul>
	 * <li>-conf &lt;<em>path-to-project.xml</em>&gt>;<br>
	 * e.g. <code>-conf C:/eclipse/workspace/projects/citeseer/project.xml</code>
	 * <li>
	 * </ul>
	 * Another is optional:<ul>
	 * <li>-outDir &lt;<em>path-to-output-directory</em>&gt;<br>
	 * The default value is <code>etc/models/gen/ou</code></li>
	 * </ul>
	 * In addition, the usual Java and Eclipse command-line arguments are required,
	 * namely the extension id for this application. A minimal command line
	 * for starting this application from the Eclipse directory would look like
	 * the following:<pre>
	 * java -cp startup.jar org.eclipse.core.launcher.Main
	 *         -application com.choicemaker.cm.compiler.ProductionModelsJarBuilder
	 *         - conf C:/eclipse/workspace/projects/citeseer/project.xml
	 *         - outDir C:/temp
	 * </pre>
	 * This command should be typed on a single line; it is broken across multiple
	 * lines here for readability.
	 */
    public void run(String conf, File outDir, ObjectMaker[] objectMakers)
	    throws Exception {
	if (conf == null) {
	    throw new IllegalArgumentException("Null conf argument");
	}
	if (outDir == null) {
	    throw new IllegalArgumentException("Null output directory");
	}
	if (!outDir.exists() || !outDir.isDirectory() || !outDir.canWrite()) {
	    String msg = "Output directory (" + outDir.getAbsolutePath()
		    + ") doesn't exist or isn't a directory or isn't writable";
	    throw new IllegalArgumentException(msg);
	}

	ConfigurationManager.getInstance().init(conf, null, true, false);

	ProductionModelsJarBuilder.refreshProductionProbabilityModels();

	if (objectMakers != null) {
	    for (int i = 0; i < objectMakers.length; i++) {
		ObjectMaker maker = objectMakers[i];
		if (maker == null) {
		    throw new IllegalArgumentException(
			    "null object maker at array index " + i);
		}
		maker.generateObjects(outDir);
	    }
	}
    }

	//
	// static stuff below...
	//

	public static void refreshProductionProbabilityModels() throws XmlConfException {
		// File genDir = new File(GeneratorXmlConf.getCodeRoot()).getAbsoluteFile();
		File d = new File(ConfigurationManager.getInstance().getSourceCodeRoot()).getAbsoluteFile();
		if (d.exists() && d.isDirectory()) {
			FileUtilities.removeChildren(d);
		}
		d = new File(ConfigurationManager.getInstance().getCompiledCodeRoot()).getAbsoluteFile();
		if (d.exists() && d.isDirectory()) {
			FileUtilities.removeChildren(d);
		}
		d = new File(ConfigurationManager.getInstance().getPackagedCodeRoot()).getAbsoluteFile();
		if (d.exists() && d.isDirectory()) {
			FileUtilities.removeChildren(d);
		}

		CompilerFactory factory = CompilerFactory.getInstance ();
		ICompiler compiler = factory.getDefaultCompiler();

		ProbabilityModelsXmlConf.loadProductionProbabilityModels(compiler);
		IProbabilityModel[] models = PMManager.getModels();
		if (models.length <= 0) {
			throw new RuntimeException("No model configurations defined. Check the project configuration file (project.xml).");
		}
	}

	public static class ProductionModelsBuilder implements ObjectMaker {
		public void generateObjects(File outDir) throws XmlConfException, IOException {
			if (outDir == null) {
				throw new IllegalArgumentException("null output directory");
			}
			if (!outDir.isDirectory()) {
				throw new IllegalArgumentException("invalid output directory: " + outDir.getPath());
			}
			File modelsJar = new File(outDir, "models.jar");
			jarProductionProbabilityModels(modelsJar);
		}
	}

	public static class HolderClassesBuilder implements ObjectMaker {
		public void generateObjects(File outDir) throws XmlConfException, IOException {
			if (outDir == null) {
				throw new IllegalArgumentException("null output directory");
			}
			if (!outDir.isDirectory()) {
				throw new IllegalArgumentException("invalid output directory: " + outDir.getPath());
			}
			File holderClassesJar = new File(outDir, "holderClasses.jar");
			jarHolderClasses(holderClassesJar);
		}
	}

	public static class ZippedJavadocBuilder implements ObjectMaker {
		public void generateObjects(File outDir) throws XmlConfException, IOException {
			if (outDir == null) {
				throw new IllegalArgumentException("null output directory");
			}
			if (!outDir.isDirectory()) {
				throw new IllegalArgumentException("invalid output directory: " + outDir.getPath());
			}
			File javadocZip = new File(outDir, "holderJavadoc.zip");
			zipHolderJavadoc(javadocZip);
		}
	}

	public static void zipHolderJavadoc(File outputFile) throws XmlConfException, IOException {
		if (outputFile == null) {
			throw new IllegalArgumentException("null output file");
		}

		File srcDir = new File(ConfigurationManager.getInstance().getSourceCodeRoot()).getAbsoluteFile();

		File tempDir = createTempDirectory();
		File javadocDir = new File(tempDir, "javadoc").getAbsoluteFile();
		javadocDir.mkdirs();

		List args = new ArrayList();

		// the command
		String javadoc = JavaEnvUtils.getJdkExecutable("javadoc");
		args.add(javadoc);

		// the outputDir
		args.add(CompilationArguments.OUTPUT_DIRECTORY);
		args.add(javadocDir.getAbsolutePath());

		// classpath to ChoiceMaker classes.
		args.add(CompilationArguments.CLASSPATH);
		args.add(ConfigurationManager.getInstance().getJavaDocClasspath());

		// link to the JDK.
		args.add("-link");
		args.add("http://java.sun.com/j2se/1.4.2/docs/api/");

		// the sourceDir
		args.add("-sourcepath");
		args.add(srcDir.getAbsolutePath());

		// the packages
		String[] packages = findPublicPackages(srcDir);
		for (int i = 0; i < packages.length; i++) {
			args.add(packages[i]);
		}

		String[] argv = (String[]) args.toArray(new String[args.size()]);


		String debug = "";
		for ( int i=0; i<argv.length; i++ ) {
			debug += "\"" + argv[i] + "\"";
		}
		System.out.println(debug);

		// run javadoc
		Runtime r = Runtime.getRuntime();
		Process proc = r.exec(argv);
		Thread input = new StreamRelayer(proc.getInputStream(), System.out);
		Thread error = new StreamRelayer(proc.getErrorStream(), System.err);
		input.start();
		error.start();
		// 2014-04-24 rphall: Commented out unused local variable.
//		boolean interrupted = false;
		try {
			proc.waitFor();
			input.join();
			error.join();
		} catch (InterruptedException ex) {
			proc.destroy();
		}

		//
		File zipFile = outputFile.getAbsoluteFile();
		try {
			zipContents(javadocDir, zipFile);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		// delete tempDir
		if (DELETE_TEMP) {
			FileUtilities.removeDir(tempDir);
		}
	}

	public static void jarHolderClasses(File outputFile) throws XmlConfException, IOException {
		if (outputFile == null) {
			throw new IllegalArgumentException("null output file");
		}

		File classesDir = new File(ConfigurationManager.getInstance().getCompiledCodeRoot()).getAbsoluteFile();

		File tempDir = createTempDirectory();

		// create $TEMP/jar
		File jarDir = new File(tempDir, "jar").getAbsoluteFile();
		if (jarDir.exists()) {
			FileUtilities.removeDir(jarDir);
		}
		jarDir.mkdir();

		try {
			copyHolderClasses(classesDir, jarDir);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		File jarFile = outputFile.getAbsoluteFile();
		try {
			jarContents(jarDir, jarFile);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		if (DELETE_TEMP) {
			FileUtilities.removeDir(tempDir);
		}
	}

	public static void jarProductionProbabilityModels(File outputFile) throws XmlConfException, IOException {
		if (outputFile == null) {
			throw new IllegalArgumentException("null output file");
		}

		File classesDir = new File(ConfigurationManager.getInstance().getCompiledCodeRoot()).getAbsoluteFile();

		File tempDir = createTempDirectory();

		// create $TEMP/jar
		File jarDir = new File(tempDir, "jar").getAbsoluteFile();
		if (jarDir.exists()) {
			FileUtilities.removeDir(jarDir);
		}
		jarDir.mkdir();

		// move generated class files from $TEMP/gen/classes to $TEMP/jar
		try {
			copyContents(classesDir, jarDir);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		File metaInfDir = new File(jarDir, "META-INF").getAbsoluteFile();
		metaInfDir.mkdir();
		File etcModelsDir = new File(metaInfDir, "etc/models").getAbsoluteFile();
		etcModelsDir.mkdirs();

		// copy projext.xml and the model files to $TEMP/jar/META-INF
		try {
			// AJW 2004-03-10: rename the project file to project.xml so XmlConfigurator.embeddedInit()
			//   can find it...
			File projectFrom = new File(ConfigurationManager.getInstance().getFileName()).getAbsoluteFile();
			File projectTo = new File(metaInfDir, "project.xml").getAbsoluteFile();
			copyFile(projectFrom, projectTo);
			//copyToDir(new File(XmlConfigurator.getFileName()).getAbsoluteFile(), metaInfDir);
			IProbabilityModel[] models = PMManager.getModels();
			for (int i=0; i<models.length; i++) {
				IProbabilityModel model = models[i];
				File f = new File(model.getFileName()).getAbsoluteFile();
				copyToDir(f, etcModelsDir);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		File jarFile = outputFile.getAbsoluteFile();
		try {
			jarContents(jarDir, jarFile);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		if (DELETE_TEMP) {
			FileUtilities.removeDir(tempDir);
		}
	}

	private static File createTempDirectory() throws IOException {
		File tempDir = File.createTempFile("ChoiceMaker", "");
		tempDir.delete();
		tempDir.mkdir();
		return tempDir;
	}

	private static String[] findPublicPackages(File srcDir) {
		List packages = new ArrayList();
		int pfxLen = srcDir.getAbsolutePath().length() + 1;
		findPublicPackages(srcDir, pfxLen, packages);
		return (String[]) packages.toArray(new String[packages.size()]);
	}

	private static void findPublicPackages(File srcDir, int pfxLen, List packages) {
		File[] kids = srcDir.listFiles();
		boolean hasClass = false;
		for (int i = 0; i < kids.length; i++) {
			File f = kids[i];
			if (f.isFile() && f.getName().endsWith(".java")) {
				hasClass = true;
			} else if (f.isDirectory()) {
				if (f.getName().equals("internal")) {
					// don't go there...
				} else {
					findPublicPackages(f, pfxLen, packages);
				}
			}
		}
		if (hasClass) {
			String packageName = srcDir.getAbsolutePath().substring(pfxLen);
			packageName = packageName.replace('/', '.');
			packageName = packageName.replace('\\', '.');
			packages.add(packageName);
		}
	}

	/**
	 * Recursively copies the contents of fromDir to toDir, unless it encounters a directory
	 * named &quot;internal&quot;, meaning the beginning of ChoiceMaker's internal classes
	 * and the end of the holder classes.
	 */
	private static void copyHolderClasses(File fromDir, File toDir) throws IOException {
		if (!toDir.exists()) {
			toDir.mkdirs();
		}
		File[] contents = fromDir.listFiles();
		for (int i = 0; i < contents.length; i++) {
			File from = contents[i];
			File to = new File(toDir, from.getName());
			if (from.isFile()) {
				copyFile(from, to);
			} else if (!from.getName().equals("internal")) {
				copyHolderClasses(from, to);
			}
		}
	}

	/**
	 *
	 */
	private static void copyContents(File fromDir, File toDir) throws IOException {
		if (!toDir.exists()) {
			toDir.mkdirs();
		}
		File[] contents = fromDir.listFiles();
		if (contents != null) {
			for (int i = 0; i < contents.length; i++) {
				File from = contents[i];
				File to = new File(toDir, from.getName());
				if (from.isDirectory()) {
					copyContents(from, to);
				} else {
					copyFile(from, to);
				}
			}
		}
	}

	private static void copyFile(File from, File to) throws IOException {
		FileInputStream ff = null;
		FileOutputStream tt = null;
		try {
			ff = new FileInputStream(from);
			tt = new FileOutputStream(to);
			byte[] buffer = new byte[8096];
			int read;
			while ((read = ff.read(buffer)) > 0) {
				tt.write(buffer, 0, read);
			}
		} finally {
			if (ff != null) {
				ff.close();
			}
			if (tt != null) {
				tt.close();
			}
		}
	}

	private static void copyToDir(File from, File toDir) throws IOException {
		File to = new File(toDir, from.getName());
		copyFile(from, to);
	}

	private static void jarContents(File dir, File jarFile) throws IOException {
		List args = new ArrayList();

		// the command
		String jar = JavaEnvUtils.getJdkExecutable("jar");
		args.add(jar);
		args.add("-cf");
		args.add(jarFile.getAbsolutePath());
		args.add("-C");
		args.add(dir.getAbsolutePath());
		args.add(".");

		String[] argv = (String[]) args.toArray(new String[args.size()]);

		String debug = "";
		for ( int i=0; i<argv.length; i++ ) {
			debug += "\"" + argv[i] + "\" ";
		}
		System.out.println(debug.trim());

		Runtime r = Runtime.getRuntime();
		Process proc = r.exec(argv);
		Thread input = new StreamRelayer(proc.getInputStream(), System.out);
		Thread error = new StreamRelayer(proc.getErrorStream(), System.err);
		input.start();
		error.start();
		// 2014-04-24 rphall: Commented out unused local variable.
//		boolean interrupted = false;
		try {
			proc.waitFor();
			input.join();
			error.join();
		} catch (InterruptedException ex) {
			proc.destroy();
		}
	}

	private static void zipContents(File dir, File zipFile) throws IOException {
		ZipOutputStream zos = null;
		try {
			zos = new ZipOutputStream(new FileOutputStream(zipFile));
			int pfxLen = dir.getAbsolutePath().length() + 1; // plus 1 for the path separator.
			zipContents(dir, pfxLen, zos);
		} finally {
			if (zos != null) {
				zos.close();
			}
		}
	}

	private static void zipContents(File dir, int pfxLen, ZipOutputStream zos) throws IOException {
		File[] contents = dir.listFiles();
		for (int i = 0; i < contents.length; i++) {
			File f = contents[i];
			if (f.isFile()) {
				String relPath = f.getAbsolutePath().substring(pfxLen);
				addFileZipEntry(f, relPath, zos);
			} else {
				String relPath = f.getAbsolutePath().substring(pfxLen) + File.separator;
				addDirZipEntry(f, relPath, zos);
				zipContents(f, pfxLen, zos);
			}
		}
	}

	private static void addFileZipEntry(File f, String relPath, ZipOutputStream zos) throws IOException {
		ZipEntry entry = new ZipEntry(relPath);

		InputStream in = new FileInputStream(f);
		byte[] buff = new byte[8 * 1024];

		zos.putNextEntry(entry);
		while (true) {
			int count = in.read(buff, 0, buff.length);
			if (count < 0) {
				break;
			}
			zos.write(buff, 0, count);
		}

		in.close();
	}

	private static void addDirZipEntry(File f, String relPath, ZipOutputStream zos) throws IOException {
		zos.putNextEntry(new ZipEntry(relPath));
	}

}
