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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.tools.ant.util.JavaEnvUtils;

import com.choicemaker.cm.compiler.impl.CompilerFactory;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.ModelConfigurationException;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.core.compiler.CompilationArguments;
import com.choicemaker.cm.core.compiler.ICompiler;
import com.choicemaker.cm.core.configure.ConfigurationManager;
import com.choicemaker.cm.core.util.ObjectMaker;
import com.choicemaker.cm.core.util.StreamRelayer;
import com.choicemaker.cm.core.xmlconf.ProbabilityModelsXmlConf;
import com.choicemaker.util.FileUtilities;

/**
 * @author Adam Winkel
 * @version
 */
public class ModelArtifactBuilder {

	private static final Logger logger = Logger
			.getLogger(ModelArtifactBuilder.class.getName());

	private static final String INDENT = "  ";

	private static boolean DELETE_TEMP = true;

	/**
	 * Creates various model artifacts such as compiled model classes, record
	 * holder classes, Javadoc documentation, and SQL view-creation scripts.
	 * 
	 * @param conf
	 *            the path to an application configuration file (project.xml or
	 *            analyzer-configuration.xml or similar)
	 * @param outDir
	 *            the directory in which JAR, ZIP and other output files are
	 *            placed.
	 * @param objectMakers
	 *            an array of non-null {@link ObjectMaker object makers} such
	 *            as:
	 *            <ul>
	 *            <li>{@link #ProductionModelsBuilder} -- creates a JAR file
	 *            containing compiled ChoiceMaker models</li>
	 *            <li>{@link #HolderClassesBuilder} -- creates a JAR file
	 *            containing Bean classes representing records used by
	 *            ChoiceMaker models</li>
	 *            <li>{@link #ZippedJavadocBuilder} -- creates a ZIP file
	 *            containing Javadoc HTML documentation for the Bean classes
	 *            representing records used by ChoiceMaker models.</li>
	 *            <li>
	 *            {@link com.choicemaker.cm.io.db.oracle.dbom.DbDbObjectMaker
	 *            DbDbObjectMaker} -- creates a SQL script that generates view
	 *            for an Oracle database that is used by Online ChoiceMaker
	 *            matching</li>
	 *            <li>
	 *            {@link com.choicemaker.cm.io.db.sqlserver.dbom.SqlDbObjectMaker
	 *            SqlDbObjectMaker} -- creates a SQL script that generates view
	 *            for a Microsoft Sql Server database that is used by Online
	 *            ChoiceMaker matching</li>
	 *            </ul>
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
			String msg =
				"Output directory ("
						+ outDir.getAbsolutePath()
						+ ") doesn't exist or isn't a directory or isn't writable";
			throw new IllegalArgumentException(msg);
		}

		ConfigurationManager.getInstance().init(conf, null, true, false);

		ModelArtifactBuilder.refreshProductionProbabilityModels();
		System.out.println("Models rebuilt and recompiled.");

		int failures = 0;
		int successes = 0;
		if (objectMakers != null) {
			for (int i = 0; i < objectMakers.length; i++) {
				ObjectMaker maker = objectMakers[i];
				if (maker == null) {
					throw new IllegalArgumentException(
							"null object maker at array index " + i);
				}
				try {
					maker.generateObjects(outDir);
					++successes;
					String msg =
						"Artifact generation succeeded for "
								+ maker.getClass().getSimpleName();
					logger.info(msg);
				} catch (Exception x) {
					++failures;
					String msg =
						"Artifact generation failed for "
								+ maker.getClass().getSimpleName();
					logger.severe(msg);
					System.err.println(msg);

				}
			}
		}
		System.out.println("Summary");
		System.out.println(INDENT + "Success: " + successes);
		System.out.println(INDENT + "Failure: " + failures);
		if (successes > 0 && failures == 0) {
			System.out.println("Results in '" + outDir.getAbsolutePath() + "'");
		} else if (successes > 0) {
			assert failures > 0;
			System.out.println("Partial results in '"
					+ outDir.getAbsolutePath() + "'");
		} else {
			assert successes == 0;
			System.out.println("No artifacts created");
		}
	}

	public static void refreshProductionProbabilityModels()
			throws ModelConfigurationException {

		String fn = ConfigurationManager.getInstance().getGeneratedSourceRoot();
		File d = new File(fn).getAbsoluteFile();
		if (d.exists() && d.isDirectory()) {
			FileUtilities.removeChildren(d);
		}

		fn = ConfigurationManager.getInstance().getCompiledCodeRoot();
		d = new File(fn).getAbsoluteFile();
		if (d.exists() && d.isDirectory()) {
			FileUtilities.removeChildren(d);
		}

		fn = ConfigurationManager.getInstance().getPackagedCodeRoot();
		d = new File(fn).getAbsoluteFile();
		if (d.exists() && d.isDirectory()) {
			FileUtilities.removeChildren(d);
		}

		CompilerFactory factory = CompilerFactory.getInstance();
		ICompiler compiler = factory.getDefaultCompiler();

		final boolean fromResource = false;
		ProbabilityModelsXmlConf.loadProductionProbabilityModels(compiler,
				fromResource);
		ImmutableProbabilityModel[] models = PMManager.getModels();
		if (models.length <= 0) {
			throw new RuntimeException("No model configurations defined. "
					+ "Check the project configuration file (project.xml).");
		}
	}

	/**
	 * Creates a JAR file containing compiled ChoiceMaker models.
	 */
	public static class ProductionModelsBuilder implements ObjectMaker {
		public void generateObjects(File outDir) throws XmlConfException,
				IOException {
			if (outDir == null) {
				throw new IllegalArgumentException("null output directory");
			}
			if (!outDir.isDirectory()) {
				throw new IllegalArgumentException("invalid output directory: "
						+ outDir.getPath());
			}
			File modelsJar = new File(outDir, "models.jar");
			jarProductionProbabilityModels(modelsJar);
		}
	}

	/**
	 * Creates a JAR file containing Bean classes representing records used by
	 * ChoiceMaker models.
	 */
	public static class HolderClassesBuilder implements ObjectMaker {
		public void generateObjects(File outDir) throws XmlConfException,
				IOException {
			if (outDir == null) {
				throw new IllegalArgumentException("null output directory");
			}
			if (!outDir.isDirectory()) {
				throw new IllegalArgumentException("invalid output directory: "
						+ outDir.getPath());
			}
			File holderClassesJar = new File(outDir, "holderClasses.jar");
			jarHolderClasses(holderClassesJar);
		}
	}

	/**
	 * Creates a ZIP file containing Javadoc HTML documentation for the Bean
	 * classes representing records used by ChoiceMaker models.
	 */
	public static class ZippedJavadocBuilder implements ObjectMaker {
		public void generateObjects(File outDir) throws XmlConfException,
				IOException {
			if (outDir == null) {
				throw new IllegalArgumentException("null output directory");
			}
			if (!outDir.isDirectory()) {
				throw new IllegalArgumentException("invalid output directory: "
						+ outDir.getPath());
			}
			File javadocZip = new File(outDir, "holderJavadoc.zip");
			zipHolderJavadoc(javadocZip);
		}
	}

	public static void zipHolderJavadoc(File outputFile)
			throws XmlConfException, IOException {

		if (outputFile == null) {
			throw new IllegalArgumentException("null output file");
		}

		File tempDir = null;
		Process proc = null;
		try {
			File srcDir =
				new File(ConfigurationManager.getInstance()
						.getGeneratedSourceRoot()).getAbsoluteFile();

			tempDir = createTempDirectory();
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
			for (int i = 0; i < argv.length; i++) {
				debug += "\"" + argv[i] + "\"";
			}
			logger.finer(debug);

			// run javadoc
			ByteArrayOutputStream infoStream = new ByteArrayOutputStream();
			ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
			Runtime r = Runtime.getRuntime();
			proc = r.exec(argv);
			Thread input = new StreamRelayer(proc.getInputStream(), infoStream);
			Thread error =
				new StreamRelayer(proc.getErrorStream(), errorStream);
			input.start();
			error.start();

			try {
				proc.waitFor();
				input.join();
				error.join();

				String info = infoStream.toString();
				if (info.length() > 0) {
					logger.info(info);
				}
				String errors = errorStream.toString();
				if (errors.length() > 0) {
					logger.severe(errors);
				}

				File zipFile = outputFile.getAbsoluteFile();
				zipContents(javadocDir, zipFile);
			} catch (InterruptedException ex) {
				proc.destroy();
			}

		} finally {
			if (tempDir != null && DELETE_TEMP) {
				FileUtilities.removeDir(tempDir);
			}
		}
	}

	public static void jarHolderClasses(File outputFile)
			throws XmlConfException, IOException {

		if (outputFile == null) {
			throw new IllegalArgumentException("null output file");
		}

		File tempDir = null;
		try {
			File classesDir =
				new File(ConfigurationManager.getInstance()
						.getCompiledCodeRoot()).getAbsoluteFile();

			tempDir = createTempDirectory();

			// create $TEMP/jar
			File jarDir = new File(tempDir, "jar").getAbsoluteFile();
			if (jarDir.exists()) {
				FileUtilities.removeDir(jarDir);
			}
			jarDir.mkdir();

			copyHolderClasses(classesDir, jarDir);

			File jarFile = outputFile.getAbsoluteFile();
			jarContents(jarDir, jarFile);
		} finally {
			if (tempDir != null && DELETE_TEMP) {
				FileUtilities.removeDir(tempDir);
			}
		}
	}

	public static void jarProductionProbabilityModels(File outputFile)
			throws XmlConfException, IOException {

		if (outputFile == null) {
			throw new IllegalArgumentException("null output file");
		}

		File tempDir = null;
		try {
			File classesDir =
				new File(ConfigurationManager.getInstance()
						.getCompiledCodeRoot()).getAbsoluteFile();

			tempDir = createTempDirectory();

			// create $TEMP/jar
			File jarDir = new File(tempDir, "jar").getAbsoluteFile();
			if (jarDir.exists()) {
				FileUtilities.removeDir(jarDir);
			}
			jarDir.mkdir();

			// move generated class files from $TEMP/gen/classes to $TEMP/jar
			copyContents(classesDir, jarDir);

			File metaInfDir = new File(jarDir, "META-INF").getAbsoluteFile();
			metaInfDir.mkdir();
			File etcModelsDir =
				new File(metaInfDir, "etc/models").getAbsoluteFile();
			etcModelsDir.mkdirs();

			// copy projext.xml and the model files to $TEMP/jar/META-INF
			// AJW 2004-03-10: rename the project file to project.xml so
			// XmlConfigurator.embeddedInit()
			// can find it...
			File projectFrom =
				new File(ConfigurationManager.getInstance().getFileName())
						.getAbsoluteFile();
			File projectTo =
				new File(metaInfDir, "project.xml").getAbsoluteFile();
			copyFile(projectFrom, projectTo);
			// copyToDir(new
			// File(XmlConfigurator.getFileName()).getAbsoluteFile(),
			// metaInfDir);
			ImmutableProbabilityModel[] models = PMManager.getModels();
			for (int i = 0; i < models.length; i++) {
				ImmutableProbabilityModel model = models[i];
				File f = new File(model.getModelFilePath()).getAbsoluteFile();
				copyToDir(f, etcModelsDir);
			}

			File jarFile = outputFile.getAbsoluteFile();
			jarContents(jarDir, jarFile);
		} finally {
			if (tempDir != null && DELETE_TEMP) {
				FileUtilities.removeDir(tempDir);
			}
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

	private static void findPublicPackages(File srcDir, int pfxLen,
			List packages) {
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
	 * Recursively copies the contents of fromDir to toDir, unless it encounters
	 * a directory named &quot;internal&quot;, meaning the beginning of
	 * ChoiceMaker's internal classes and the end of the holder classes.
	 */
	private static void copyHolderClasses(File fromDir, File toDir)
			throws IOException {
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
	private static void copyContents(File fromDir, File toDir)
			throws IOException {
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

	public static final String MANIFEST_VERSION_NUMBER = "1.0";
	public static final String BACK_SLASH = "\\";
	public static final String FORWARD_SLASH = "/";

	public static void jarContents(File dir, File jarFile) throws IOException {
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION,
				MANIFEST_VERSION_NUMBER);
		JarOutputStream target =
			new JarOutputStream(new FileOutputStream(jarFile), manifest);
		add(dir, target);
		target.close();
	}

	public static void add(File source, JarOutputStream target)
			throws IOException {
		BufferedInputStream in = null;
		try {
			if (source.isDirectory()) {
				String name =
					source.getPath().replace(BACK_SLASH, FORWARD_SLASH);
				if (!name.isEmpty()) {
					if (!name.endsWith(FORWARD_SLASH))
						name += FORWARD_SLASH;
					JarEntry entry = new JarEntry(name);
					entry.setTime(source.lastModified());
					target.putNextEntry(entry);
					target.closeEntry();
				}
				final File[] files = source.listFiles();
				for (int i = 0; i < files.length; i++) {
					File nestedFile = files[i];
					add(nestedFile, target);
				}

			} else {
				JarEntry entry =
					new JarEntry(source.getPath().replace(BACK_SLASH,
							FORWARD_SLASH));
				entry.setTime(source.lastModified());
				target.putNextEntry(entry);
				in = new BufferedInputStream(new FileInputStream(source));

				byte[] buffer = new byte[1024];
				while (true) {
					int count = in.read(buffer);
					if (count == -1)
						break;
					target.write(buffer, 0, count);
				}
				target.closeEntry();
			}
		} finally {
			if (in != null)
				in.close();
		}
	}

	private static void zipContents(File dir, File zipFile) throws IOException {
		ZipOutputStream zos = null;
		try {
			zos = new ZipOutputStream(new FileOutputStream(zipFile));
			int pfxLen = dir.getAbsolutePath().length() + 1; // plus 1 for the
																// path
																// separator.
			zipContents(dir, pfxLen, zos);
		} finally {
			if (zos != null) {
				zos.close();
			}
		}
	}

	private static void zipContents(File dir, int pfxLen, ZipOutputStream zos)
			throws IOException {
		File[] contents = dir.listFiles();
		for (int i = 0; i < contents.length; i++) {
			File f = contents[i];
			if (f.isFile()) {
				String relPath = f.getAbsolutePath().substring(pfxLen);
				addFileZipEntry(f, relPath, zos);
			} else {
				String relPath =
					f.getAbsolutePath().substring(pfxLen) + File.separator;
				addDirZipEntry(f, relPath, zos);
				zipContents(f, pfxLen, zos);
			}
		}
	}

	private static void addFileZipEntry(File f, String relPath,
			ZipOutputStream zos) throws IOException {
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

	private static void addDirZipEntry(File f, String relPath,
			ZipOutputStream zos) throws IOException {
		zos.putNextEntry(new ZipEntry(relPath));
	}

}
