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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Platform;
import org.jdom.Element;

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
import com.choicemaker.cm.core.configure.ChoiceMakerConfiguration;
import com.choicemaker.cm.core.configure.ChoiceMakerConfigurator;
import com.choicemaker.cm.core.install.InstallableChoiceMakerConfigurator;
import com.choicemaker.cm.core.install.InstalledChoiceMakerConfiguration;
import com.choicemaker.cm.core.util.FileUtilities;
import com.choicemaker.cm.core.util.MessageUtil;
import com.choicemaker.cm.core.xmlconf.GeneratorXmlConf;
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
			MessageUtil.m.formatMessage(
				"compiler.comp.usage",
				"$Revision: 1.1 $, $Date: 2010/03/24 20:10:52 $"));
	}

	protected String getClassPath() {
		String res = System.getProperty("java.class.path");
		try {
			Element e = XmlConfigurator.getCore().getChild("classpath");
			if (e != null) {
				res += FileUtilities.toAbsoluteClasspath(e.getText());
			}
			e = XmlConfigurator.getCore().getChild("reload");
			if (e != null) {
				e = e.getChild("classpath");
				if (e != null) {
					res += FileUtilities.toAbsoluteClasspath(e.getText());
				}
			}
		} catch (IOException ex) {
			logger.error("Problem with classpath", ex);
		}
		IPluginDescriptor[] plugins =
			Platform.getPluginRegistry().getPluginDescriptors();
		for (int i = 0; i < plugins.length; i++) {
			URL[] ucp =
				((URLClassLoader) plugins[i].getPluginClassLoader()).getURLs();
			for (int j = 0; j < ucp.length; j++) {
				res += File.pathSeparator + ucp[j].getPath();
			}
		}
		return res;
	}

	protected abstract ICompilationUnit getCompilationUnit(
		CompilationEnv env,
		Sourcecode source);

	public abstract Properties getFeatures();







	public String compile(CompilationArguments arguments, final Writer statusOutput)
		throws CompilerException {
		String file = arguments.files()[0];
		try {
			String classPath = getClassPath();
			CompilationEnv env = new CompilationEnv(arguments, classPath, statusOutput);
			Sourcecode source =
				new Sourcecode(file, arguments.argumentVal("-encoding"), statusOutput);
			ICompilationUnit unit = getCompilationUnit(env, source);
			unit.compile();
			unit.conclusion(statusOutput);
			// env.conclusion();
			if (unit.getErrors() == 0)
			{
				String targetdir = null;
				targetdir =
					new File(
						GeneratorXmlConf.getCodeRoot()
							+ File.separator
							+ "classes")
						.getAbsolutePath();
				new File(targetdir).getAbsoluteFile().mkdirs();
				List generatedFiles = unit.getGeneratedJavaSourceFiles();
				final int numStdArgs = 5;
				Object[] args = new String[generatedFiles.size() + numStdArgs];
				args[0] = "-classpath";
				args[1] = classPath;
				args[2] = "-d";
				args[3] = targetdir;
				args[4] = "-O";
				for (int i = 0; i < generatedFiles.size(); ++i) {
					args[i + numStdArgs] =
						new File((String) generatedFiles.get(i))
							.getAbsolutePath();
				}

				// result will store a return code from the compile function
				int result = -1;

				//save the location of System.out and System.err
				PrintStream out = System.out;
				PrintStream err = System.err;
				ClassLoader cl = getJavacClassLoader();
				try {

					// Change the location of System.out and System.err
					PrintStream ps =
						new PrintStream(
							new OutputStream()
							{
								public void write(int c) throws IOException
								{
									statusOutput.write(c);
								}
							}
						);
					System.setErr(ps);
					System.setOut(ps);

					// Get a handle on Sun's compiler object
					Class c = Class.forName("com.sun.tools.javac.Main", true, cl);
					Object compiler = c.newInstance();

					// Use reflection to call the compile method with the args setup earlier
					Method compile =
						c.getMethod(
							"compile",
							new Class[] {
								(new String[0]).getClass()
							}
						);
					Integer returncode = (Integer) compile.invoke(compiler, new Object[] {args});

					//save the return code
					result = returncode.intValue();
				}
				catch (ClassNotFoundException e)
                {
                	logger.error("Could not find com.sun.tools.javac.Main in the classloader " + cl.toString());
					logger.error("Compiler.compile()", e);
                }
                catch (SecurityException e)
                {
					logger.error("Compiler.compile()", e);
                }
                catch (NoSuchMethodException e)
                {
					logger.error("Compiler.compile()", e);
                }
                catch (IllegalArgumentException e)
                {
					logger.error("Compiler.compile()", e);
                }
                catch (IllegalAccessException e)
                {
					logger.error("Compiler.compile()", e);
                }
                catch (InvocationTargetException e)
                {
					logger.error("Compiler.compile()", e);
                }
                catch (InstantiationException e)
                {
					logger.error("Compiler.compile()", e);
                }
				catch (Exception ex) {
					System.err.println(
						"The Java compiler javac could not be found.");
					logger.error("Javac", ex);
					return null;
				}

				finally {
					System.setErr(err);
					System.setOut(out);
				}


				if (result == MODERN_COMPILER_SUCCESS)
				{
					return unit.getAccessorClass();
				}
				else
				{
					return null;
				}
			}
			else
			{
				return null;
			}
		}
		catch (IOException e)
		{
			System.err.println(
				MessageUtil.m.formatMessage("compiler.comp.file.error", file));
			logger.error("Compiler", e);
			return null;
		}
	}











	public boolean compile(IProbabilityModel model, Writer statusOutput)
		throws CompilerException {
		CompilationArguments arguments = new CompilationArguments();
		String[] compilerArgs = new String[1];
		compilerArgs[0] = model.getClueFileName();
		arguments.enter(compilerArgs);
		String accessorClass = compile(arguments, statusOutput);
		if (accessorClass != null) {
			try {
				model.setAccessor(
					PMManager.createAccessor(
						accessorClass,
						XmlConfigurator.reload()));
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
		compilerArgs[0] = spec.getClueFileName();
		arguments.enter(compilerArgs);
		String accessorClass = compile(arguments, statusOutput);
		ImmutableProbabilityModel retVal = null;
		if (accessorClass == null) {
			String status = statusOutput.toString();
			assert status != null && !status.trim().isEmpty();
			throw new CompilerException("Compilation failed: " + status);
		} else {
			assert !accessorClass.trim().isEmpty();
			try {
				/* FIXME Review this design */
//				Accessor acc = InstallableModelManager.getInstance().createAccessor(accessorClass,
//						XmlConfigurator.getInstance().reload());
//				retVal = InstallableModelManager.getInstance().createModelInstance(spec,acc);
				ChoiceMakerConfiguration cmc = InstalledChoiceMakerConfiguration
						.getInstance();
				ChoiceMakerConfigurator configurator = InstallableChoiceMakerConfigurator
						.getInstance();
				cmc = configurator.reloadClasses(cmc);
				ClassLoader cl = cmc.getClassLoader();
//				ProbabilityModelManager pmm = InstallableModelManager
//						.getInstance();
//				Accessor acc = pmm.createAccessor(accessorClass, cl);
//				retVal = pmm.createModelInstance(spec, acc);
				Class accessorC = Class.forName(accessorClass, true, cl);
				Accessor acc = (Accessor) accessorC.newInstance();
				retVal = new MutableProbabilityModel(spec,acc);
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

