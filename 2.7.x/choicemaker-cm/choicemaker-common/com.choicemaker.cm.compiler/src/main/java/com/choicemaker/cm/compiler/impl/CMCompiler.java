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
package com.choicemaker.cm.compiler.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.choicemaker.cm.compiler.CompilationEnv;
import com.choicemaker.cm.compiler.ICompilationUnit;
import com.choicemaker.cm.compiler.Sourcecode;
import com.choicemaker.cm.core.Accessor;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.ProbabilityModelSpecification;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.base.MutableProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.core.compiler.CompilationArguments;
import com.choicemaker.cm.core.compiler.CompilerException;
import com.choicemaker.cm.core.compiler.ICompiler;
import com.choicemaker.cm.core.configure.ConfigurationManager;
import com.choicemaker.cm.core.util.ChoiceMakerCoreMessages;
import com.choicemaker.cm.core.xmlconf.XmlConfigurator;

/**
 * ClueMaker compiler.
 *
 * @author    Matthias Zenger
 * @author    Martin Buechi
 * @version   $Revision: 1.1 $ $Date: 2010/03/24 20:10:52 $
 */
public abstract class CMCompiler implements ICompiler {

	public static final ClassLoader getJavacClassLoader() {
		String tools =
			new File(System.getProperty("java.home"))
				.getAbsoluteFile()
				.getParent()
				+ File.separator
				+ "lib"
				+ File.separator
				+ "tools.jar";
		try
		{
			URLClassLoader URLcl =
				new URLClassLoader(
					new URL[] { new File(tools).toURL() },
					CMCompiler.class.getClassLoader()
				);
			//logger.error("Returning URLClassLoader URLcl");
			return URLcl;
		}
		catch (MalformedURLException ex)
		{
			//logger.error("Returning ICompiler ClassLoader");
			return ICompiler.class.getClassLoader();
		}
	}

	protected static Logger logger = Logger.getLogger(CMCompiler.class);

	protected static void usage() {
		System.out.println(
			ChoiceMakerCoreMessages.m.formatMessage(
				"compiler.comp.usage",
				"$Revision: 1.1 $, $Date: 2010/03/24 20:10:52 $"));
	}

	protected String getClassPath() {
//		String res = System.getProperty("java.class.path");
//		try {
//			Element e = XmlConfigurator.getInstance().getCore().getChild("classpath");
//			if (e != null) {
//				res += FileUtilities.toAbsoluteClasspath(e.getText());
//			}
//			e = XmlConfigurator.getInstance().getCore().getChild("reload");
//			if (e != null) {
//				e = e.getChild("classpath");
//				if (e != null) {
//					res += FileUtilities.toAbsoluteClasspath(e.getText());
//				}
//			}
//		} catch (IOException ex) {
//			logger.error("Problem with classpath", ex);
//		}
//		IPluginDescriptor[] plugins =
//			Platform.getPluginRegistry().getPluginDescriptors();
//		for (int i = 0; i < plugins.length; i++) {
//			URL[] ucp =
//				((URLClassLoader) plugins[i].getPluginClassLoader()).getURLs();
//			for (int j = 0; j < ucp.length; j++) {
//				res += File.pathSeparator + ucp[j].getPath();
//			}
//		}
		return ConfigurationManager.getInstance().getClassPath();
	}

	protected abstract ICompilationUnit getCompilationUnit(
		CompilationEnv env,
		Sourcecode source);

	public abstract Properties getFeatures();

	public int generateJavaCode(CompilationArguments arguments,
			Writer statusOutput) throws CompilerException {
		ICompilationUnit unit = generateJavaCodeInternal(arguments,
			statusOutput);
		int retVal = unit.getErrors();
		return retVal;
	}

	/**
	 * Returns the number of ClueMaker errors
	 * @throws CompilerException 
	 */
	public ICompilationUnit generateJavaCodeInternal(CompilationArguments arguments,
			Writer statusOutput) throws CompilerException {

		String file = arguments.files()[0];
		String defaultPath = getClassPath();
		CompilationEnv env =
			new CompilationEnv(arguments, defaultPath, statusOutput);
		Sourcecode source;
		ICompilationUnit retVal = null;
		try {
			source = new Sourcecode(file, arguments.argumentVal(CompilationArguments.ENCODING),
					statusOutput);
			ICompilationUnit unit = getCompilationUnit(env, source);
			unit.compile();
			unit.conclusion(statusOutput);
			retVal = unit;
		} catch (IOException e) {
			e.fillInStackTrace();
			String msg = e.toString();
			logger.error(msg,e);
			throw new CompilerException(msg,e);
		}

		return retVal;
	}

	public String compile(CompilationArguments arguments,
			final Writer statusOutput) throws CompilerException {
		ICompilationUnit unit =
			generateJavaCodeInternal(arguments, statusOutput);
		if (unit.getErrors() == 0) {
			// Create the output directory
			File targetDir =
				new File(ConfigurationManager.getInstance().getCompiledCodeRoot());
			targetDir.getAbsoluteFile().mkdirs();
			
			// Create the compilation arguments
			String targetDirPath = targetDir.getAbsolutePath();
			String classPath = getClassPath();
			List generatedFiles = unit.getGeneratedJavaSourceFiles();
			final int numStdArgs = 5;
			Object[] args = new String[generatedFiles.size() + numStdArgs];
			args[0] = CompilationArguments.CLASSPATH;
			args[1] = classPath;
			args[2] = CompilationArguments.OUTPUT_DIRECTORY;
			args[3] = targetDirPath;
			args[4] = "-O";
			for (int i = 0; i < generatedFiles.size(); ++i) {
				args[i + numStdArgs] =
					new File((String) generatedFiles.get(i)).getAbsolutePath();
			}

			// result will store a return code from the compile function
			int result = -1;

			// save the location of System.out and System.err
			PrintStream out = System.out;
			PrintStream err = System.err;
			ClassLoader cl = getJavacClassLoader();
			PrintStream ps = null;
			try {

				// Change the location of System.out and System.err
				ps = new PrintStream(new OutputStream() {
					public void write(int c) throws IOException {
						statusOutput.write(c);
					}
				});
				System.setErr(ps);
				System.setOut(ps);

				// Get a handle on Sun's compiler object
				Class c = Class.forName("com.sun.tools.javac.Main", true, cl);
				Object compiler = c.newInstance();

				// Use reflection to call the compile method with the args setup
				// earlier
				Method compile =
					c.getMethod("compile",
							new Class[] { (new String[0]).getClass() });
				Integer returncode =
					(Integer) compile.invoke(compiler, new Object[] { args });

				// save the return code
				result = returncode.intValue();
			}

			catch (Exception ex) {
				logger.error("Compiler.compile(): " + ex.toString(), ex);
				return null;
			}

			finally {
				if (ps != null) {
					ps.flush();
					ps.close();
					ps = null;
				}
				System.setErr(err);
				System.setOut(out);
			}

			if (result == MODERN_COMPILER_SUCCESS) {
				return unit.getAccessorClass();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public boolean compile(IProbabilityModel model, Writer statusOutput)
		throws CompilerException {
		CompilationArguments arguments = new CompilationArguments();
		String[] compilerArgs = new String[1];
		compilerArgs[0] = model.getClueFilePath();
		arguments.enter(compilerArgs);
		String accessorClass = compile(arguments, statusOutput);
		if (accessorClass != null) {
			try {
				model.setAccessor(
					PMManager.createAccessor(
						accessorClass,
						XmlConfigurator.getInstance().reload()));
			} catch (ClassNotFoundException ex) {
				logger.error(ex);
				return false;
			} catch (InstantiationException ex) {
				logger.error(ex);
				return false;
			} catch (IllegalAccessException ex) {
				logger.error(ex);
				return false;
			} catch (XmlConfException ex) {
				logger.error(ex);
				return false;
			}
		} else {
			return false;
		}
		return true;
	}

	public ImmutableProbabilityModel compile(
			ProbabilityModelSpecification spec, Writer statusOutput)
			throws CompilerException {
		CompilationArguments arguments = new CompilationArguments();
		String[] compilerArgs = new String[1];
		compilerArgs[0] = spec.getClueFilePath();
		arguments.enter(compilerArgs);
		String accessorFQCN = compile(arguments, statusOutput);
		ImmutableProbabilityModel retVal = null;
		if (accessorFQCN == null) {
			String status = statusOutput.toString();
			assert status != null && !status.trim().isEmpty();
			throw new CompilerException("Compilation failed: " + status);
		} else {
			assert !accessorFQCN.trim().isEmpty();
			try {
				/* FIXME Review this design */
//				Accessor acc = InstallableModelManager.getInstance().createAccessor(accessorClass,
//						XmlConfigurator.getInstance().reload());
//				retVal = InstallableModelManager.getInstance().createModelInstance(spec,acc);
//				ChoiceMakerConfiguration cmc = InstalledConfiguration
//						.getInstance();
//				ChoiceMakerConfigurator configurator = InstallableConfigurator
//						.getInstance();
//				cmc = configurator.reloadClasses(cmc);
//				ClassLoader cl = cmc.getClassLoader();
				ClassLoader cl = ConfigurationManager.getInstance()
						.getClassLoader();
//				ProbabilityModelManager pmm = InstallableModelManager
//						.getInstance();
//				Accessor acc = pmm.createAccessor(accessorClass, cl);
//				retVal = pmm.createModelInstance(spec, acc);
				Class accessorClass = Class.forName(accessorFQCN, true, cl);
				Accessor acc = (Accessor) accessorClass.newInstance();
				retVal = new MutableProbabilityModel(spec, acc);
				/* END */
			} catch (Exception ex) {
				String msg = "Compilation failed: " + ex.toString();
				logger.error(msg);
				throw new CompilerException(msg);
			}
		}
		assert retVal != null;

		return retVal;
	}

} // Compiler

