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
package com.choicemaker.cm.core.xmlconf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.MachineLearner;
import com.choicemaker.cm.core.PropertyNames;
import com.choicemaker.cm.core.WellKnownPropertyValues;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.compiler.ICompiler;
import com.choicemaker.cm.core.compiler.InstallableCompiler;
import com.choicemaker.cm.core.configure.ChoiceMakerConfiguration;
import com.choicemaker.cm.core.configure.ChoiceMakerConfigurator;
import com.choicemaker.cm.core.configure.ConfigurationUtils;
import com.choicemaker.cm.core.configure.MachineLearnerPersistence;
import com.choicemaker.cm.core.configure.ProbabilityModelPersistence;
import com.choicemaker.e2.platform.CMPlatformUtils;
import com.choicemaker.util.FileUtilities;
import com.choicemaker.util.SystemPropertyUtils;

/**
 * XML configuration file reader.
 *
 * A ChoiceMaker configuration file consists of the following three types of
 * elements:
 * <ul>
 * <li>An arbitrary number of modules. Modules are self-initializing, that is,
 * they list the class to be used for their initialization. All modules are
 * initialized by calling <code>initModules</code>, which is also called by
 * <code>init</code>. An example of a module is the file collections, e.g., the
 * file containing generic first names, such as "BABY".</li>
 * <li>Static initialization components. It is the client code's responsibility
 * to call the specific initializers for these components. The
 * <code>OracleConnectionCacheXmlConf</code> is an example of such an
 * initializer.</li>
 * </ul>
 *
 * @author Martin Buechi
 * @version $Revision: 1.2 $ $Date: 2010/03/24 18:57:16 $
 */
public class XmlConfigurator implements ChoiceMakerConfigurator,
		ChoiceMakerConfiguration {

	private static final Logger logger = Logger.getLogger(XmlConfigurator.class
			.getName());

	public static final boolean DEFAULT_RELOAD = true;

	public static final boolean DEFAULT_INIT_GUI = true;

	private static final String SOURCE_DIRECTORY = "src";

	private static final String CLASSES_DIRECTORY = "classes";

	private static final String PACKAGES_DIRECTORY = "out";

	/** The XML document read as configuration file. */
	Document document;
	ClassLoader classLoader;
	URL[] reloadClassPath;
	private boolean reload;
	ClassLoader reloadClassLoader;
	String fileName;

	private File workingDirectory;

	private String classpath;

	private String codeRoot;

	public static XmlConfigurator instance = new XmlConfigurator();

	public static XmlConfigurator getInstance() {
		return instance;
	}

	public static String getClassPath(File workingDir, Document document)
			throws XmlConfException {
		String retVal = ConfigurationUtils.getClassPath(workingDir, document);
		URL[] urls = CMPlatformUtils.getPluginClassPaths();
		for (int j = 0; j < urls.length; j++) {
			retVal += File.pathSeparator + urls[j].getPath();
		}
		return retVal;
	}

	void set(Method method, Object obj, String value) throws Exception {
		Class paramType = method.getParameterTypes()[0];
		Object param = value;
		logger.info(obj.getClass().getName() + "." + method.getName() + "("
				+ value + ")");
		if (paramType == boolean.class || paramType == Boolean.class) {
			param = Boolean.valueOf(value);
		} else if (paramType == int.class || paramType == Integer.class) {
			param = new Integer(value);
		}
		method.invoke(obj, new Object[] { param });
	}

	/**
	 * ChoiceMaker uses System properties for configuration. If the
	 * configuration properties haven't already been set, this method sets some
	 * defaults expected in an Eclipse 2 context.
	 */
	protected static void initializeInstallableComponents() {
		// Configure the compiler
		System.setProperty(PropertyNames.INSTALLABLE_COMPILER,
				WellKnownPropertyValues.ECLIPSE2_COMPILER);

		// Configure a factory for generator plugins used by the compiler
		System.setProperty(PropertyNames.INSTALLABLE_GENERATOR_PLUGIN_FACTORY,
				WellKnownPropertyValues.ECLIPSE2_GENERATOR_PLUGIN_FACTORY);
	}

	static String initializeClassPath(File workingDir, Document document)
			throws XmlConfException {
		return getClassPath(workingDir, document);
	}

	static ClassLoader initializeClassLoader(File wdir, Document document)
			throws XmlConfException {
		URL[] classPath = new URL[0];
		Element cp =
			ConfigurationUtils.getCore(document).getChild(
					ConfigurationUtils.CONFIGURATION_CLASSPATH_ELEMENT);
		if (cp != null) {
			String s = cp.getText();
			try {
				classPath = FileUtilities.cpToUrls(wdir, s);
			} catch (Exception ex) {
				throw new XmlConfException("Classpath", ex);
			}
		}
		ClassLoader parentClassLoader = XmlConfigurator.class.getClassLoader();
		ClassLoader retVal = new PpsClassLoader(classPath, parentClassLoader);
		return retVal;
	}

	static URL[] initializeReloadClassPath(File wdir, Document document)
			throws XmlConfException {
		URL[] retVal = new URL[0];
		Element rl =
			ConfigurationUtils.getCore(document).getChild(
					ConfigurationUtils.CONFIGURATION_RELOAD_ELEMENT);
		if (rl != null) {
			Element cp =
				rl.getChild(ConfigurationUtils.CONFIGURATION_CLASSPATH_ELEMENT);
			if (cp != null) {
				String s = cp.getText();
				try {
					retVal = FileUtilities.cpToUrls(wdir, s);
				} catch (Exception ex) {
					throw new XmlConfException("Classpath", ex);
				}
			}
		}
		return retVal;
	}

	/**
	 * Initializes all the modules listed in the configuration file.
	 * 
	 * @throws XmlConfException
	 *             if any error occurs.
	 */
	static void initializeModules(Document document, ClassLoader cl)
			throws XmlConfException {
		try {
			List modules = ConfigurationUtils.getModules(document);
			Iterator i = modules.iterator();
			while (i.hasNext()) {
				Element e = (Element) i.next();
				String className = e.getAttributeValue("class");
				Class clazz = Class.forName(className, true, cl);
				XmlModuleInitializer m =
					(XmlModuleInitializer) clazz.getDeclaredField("instance")
							.get(null);
				m.init(e);
			}
		} catch (Exception ex) {
			throw new XmlConfException("Internal error: " + ex.toString(), ex);
		}
	}

	/**
	 * Initializes just the modules listed in the reload stanza.
	 * 
	 * @throws XmlConfException
	 *             if any error occurs.
	 */
	static void initializeReloadModules(Document document, ClassLoader cl)
			throws XmlConfException {
		try {
			List modules = ConfigurationUtils.getReloadModules(document);
			Iterator i = modules.iterator();
			while (i.hasNext()) {
				Element e = (Element) i.next();
				String className = e.getAttributeValue("class");
				Class clazz = Class.forName(className, true, cl);
				XmlModuleInitializer m =
					(XmlModuleInitializer) clazz.getDeclaredField("instance")
							.get(null);
				m.init(e);
			}
		} catch (Exception ex) {
			throw new XmlConfException("Internal error: " + ex.toString(), ex);
		}
	}

	/**
	 * Saves the XML configuration into the specified file.
	 *
	 * @param fn
	 *            The name of the file.
	 */
	public void save(String fn) throws XmlConfException {
		fileName = new File(fn).getAbsolutePath();
		try {
			FileOutputStream fs = new FileOutputStream(fileName);
			XMLOutputter o = new XMLOutputter("    ", true);
			o.setTextNormalize(true);
			o.output(document, fs);
			fs.close();
		} catch (IOException ex) {
			throw new XmlConfException("Internal error.", ex);
		}
	}

	/**
	 * Returns the JDOM document for the configuration file.
	 *
	 * @return the JDOM document for the configuration file.
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * Returns the JDOM root element for the configuration file.
	 *
	 * @return the JDOM root element for the configuration file.
	 */
	public Element getRoot() {
		return document.getRootElement();
	}

	Element get(String b, String t) {
		Element e = getRoot().getChild(b);
		if (e != null) {
			return e.getChild(t);
		} else {
			return null;
		}
	}

	public Element getCustom(String t) {
		return get("custom", t);
	}

	public Element getPlugin(String t) {
		return get("plugin", t);
	}

	public Element getCore() {
		return getRoot().getChild("core");
	}

	public ClassLoader getReloadClassLoader() throws XmlConfException {
		if (reloadClassLoader == null) {
			reloadClassLoader = reload();
		}
		return reloadClassLoader;
	}

	public ClassLoader reload() throws XmlConfException {
		ClassLoader retVal;
		if (!isReload() && reloadClassLoader != null) {
			retVal = reloadClassLoader;
		} else {
			retVal = new PpsClassLoader(reloadClassPath, getClassLoader());
			initializeReloadModules(getDocument(), retVal);
		}
		return retVal;
	}

	/**
	 * A PpsClassLoader first tries to load a class using a class loader of some
	 * plugin in the Plugin Registery. Next, if the class hasn't been loaded, a
	 * PpsClassLoader relies on the method of a URLClassLoader; namely, it
	 * searches through the parent of URLClassLoader (i.e. the grandparent of
	 * this loader), and then the URLs of the URLClassLoader in the order that
	 * they are specified in the first argument of the PpsClassLoader
	 * constructor. If the class isn't found, a ClassNotFound exception is
	 * thrown.
	 */
	public static class PpsClassLoader extends URLClassLoader {

		private static final Logger log = Logger
				.getLogger(XmlConfigurator.class.getName() + ".PpsClassLoader");

		private ClassLoader[] parents;

		public PpsClassLoader(URL[] path, ClassLoader parent) {
			super(path, parent);
			parents = CMPlatformUtils.getPluginClassLoaders();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.ClassLoader#findClass(java.lang.String)
		 */
		public Class loadClass(String name) throws ClassNotFoundException {
			Class c = null;
			for (int i = 0; i < parents.length; i++) {
				try {
					c = parents[i].loadClass(name);
					log.fine("Class '" + name + "' found by '" + parents[i]
							+ "'");
					break;
				} catch (ClassNotFoundException ex) {
					if (log.isLoggable(Level.FINEST)) {
						log.finest("Class '" + name + "' not found by '"
								+ parents[i] + "'; search continuing...");
					}
				}
			}
			if (c == null) {
				log.finer("Class '"
						+ name
						+ "' not found by plugin classloaders; search continuing with parent URLClassLoader.");
				c = super.findClass(name);
				if (log.isLoggable(Level.FINE)) {
					if (c != null) {
						String msg =
							"Class '" + name
									+ "' found by parent ULRClassLoader '"
									+ super.toString() + "'";
						log.fine(msg);
					} else {
						String msg =
							"Class '" + name
									+ "' not found by parent ULRClassLoader '"
									+ super.toString() + "'";
						log.fine(msg);
					}
				}
			}
			if (c == null) {
				String msg =
					"Class '" + name + "' not found by PpsClassLoader.";
				log.severe(msg);
				throw new ClassNotFoundException(msg);
			}
			return c;
		}
	}

	public String getRmiCodebase() {
		String res = "";
		try {
			res =
				FileUtilities.toAbsoluteUrlClasspath(getWorkingDirectory(),
						System.getProperty("java.class.path"));
			Element e = getCore().getChild("classpath");
			if (e != null) {
				res +=
					FileUtilities.toAbsoluteUrlClasspath(getWorkingDirectory(),
							e.getText());
			}
			e = getCore().getChild("reload");
			if (e != null) {
				e = e.getChild("classpath");
				if (e != null) {
					res +=
						FileUtilities.toAbsoluteUrlClasspath(
								getWorkingDirectory(), e.getText());
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		URL[] ucp = CMPlatformUtils.getPluginClassPaths();
		for (int j = 0; j < ucp.length; j++) {
			res += " " + ucp[j].toString();
		}
		return res;
	}

	public ChoiceMakerConfiguration init(String fn, boolean reload,
			boolean initGui) throws XmlConfException {
		return init(fn, null, reload, initGui);
	}

	XmlConfigurator() {
	}

	public void deleteGeneratedCode() {
		File f;
		f = new File(getGeneratedSourceRoot()).getAbsoluteFile();
		if (f.exists()) {
			logger.info("Deleting source code root('" + f.getAbsoluteFile()
					+ "')");
			FileUtilities.removeChildren(f);
		}
		f = new File(getCompiledCodeRoot()).getAbsoluteFile();
		if (f.exists()) {
			logger.info("Deleting compiled code root('" + f.getAbsoluteFile()
					+ "')");
			FileUtilities.removeChildren(f);
		}
		f = new File(getPackagedCodeRoot()).getAbsoluteFile();
		if (f.exists()) {
			logger.info("Deleting packaged code root('" + f.getAbsoluteFile()
					+ "')");
			FileUtilities.removeChildren(f);
		}
	}

	public ICompiler getChoiceMakerCompiler() {
		return InstallableCompiler.getInstance();
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public String getClassPath() {
		return classpath;
	}

	protected String getCodeRoot() {
		return codeRoot;
	}

	/** Returns the configuration file name */
	public String getFileName() {
		return fileName;
	}

	public String getJavaDocClasspath() {
		String pathSeparator =
			System.getProperty(SystemPropertyUtils.PATH_SEPARATOR);
		String res = null;
		URL[] ucp = CMPlatformUtils.getPluginClassPaths();
		for (int j = 0; j < ucp.length; j++) {
			res += " " + ucp[j].toString();
		}
		for (int j = 0; j < ucp.length; j++) {
			if (res == null) {
				res = "";
			} else {
				res += pathSeparator;
			}
			res += ucp[j].getPath();
		}
		return res;
	}

	public MachineLearnerPersistence getMachineLearnerPersistence(
			MachineLearner model) {
		// FIXME non-functional method stub
		throw new Error("not yet implemented");
	}

	public ProbabilityModelPersistence getModelPersistence(
			ImmutableProbabilityModel model) {
		// FIXME non-functional method stub
		throw new Error("not yet implemented");
	}

	public List getProbabilityModelConfigurations() {
		// FIXME non-functional method stub
		throw new Error("not yet implemented");
	}

	public String getReloadClassPath() {
		// FIXME non-functional method stub
		throw new Error("not yet implemented");
	}

	public ClassLoader getRmiClassLoader() {
		return new PpsClassLoader(new URL[0], null);
	}

	public File getWorkingDirectory() {
		return workingDirectory;
	}

	public void reloadClasses() throws XmlConfException {
		this.reloadClassLoader = reload();
	}

	public String toXml() {
		// FIXME non-functional method stub
		throw new Error("not yet implemented");
	}

	/**
	 * Initializes ChoiceMaker from an XML configuration file. Reads the XML
	 * configuration file and initializes logging and all modules.
	 *
	 * @param fn
	 *            The name of the configuration file.
	 * @return
	 * @throws XmlConfException
	 *             if any error occurs.
	 */
	public ChoiceMakerConfiguration init(String fn, String logConfName,
			boolean reload, boolean initGui) throws XmlConfException {

		initializeInstallableComponents();

		this.setReload(reload);
		this.fileName = new File(fn).getAbsolutePath();
		this.document = ConfigurationUtils.readConfigurationFile(fileName);

		this.workingDirectory =
			ConfigurationUtils
					.getWorkingDirectory(getFileName(), getDocument());
		System.setProperty(ConfigurationUtils.SYSTEM_USER_DIR,
				getWorkingDirectory().toString());

		this.codeRoot =
			ConfigurationUtils
					.getCodeRoot(getWorkingDirectory(), getDocument());
		this.classpath =
			initializeClassPath(getWorkingDirectory(), getDocument());

		if (logConfName != null && !logConfName.trim().isEmpty()) {
			logger.warning("Ignoring log configuration name: " + logConfName);

		}

		this.classLoader =
			initializeClassLoader(getWorkingDirectory(), getDocument());

		this.reloadClassPath =
			initializeReloadClassPath(getWorkingDirectory(), getDocument());

		initializeModules(getDocument(), getClassLoader());

		return this;
	}

	public ChoiceMakerConfiguration init() throws XmlConfException {
		String fn =
			System.getProperty(PropertyNames.CHOICEMAKER_CONFIGURATION_FILE);
		boolean reload = DEFAULT_RELOAD;
		boolean initGui = DEFAULT_INIT_GUI;
		return init(fn, reload, initGui);
	}

	public String getClueMakerSourceRoot() {
		// FIXME non-functional method stub
		throw new Error("not yet implemented");
	}

	public String getGeneratedSourceRoot() {
		return getCodeRoot() + File.separator + SOURCE_DIRECTORY;
	}

	public String getCompiledCodeRoot() {
		return getCodeRoot() + File.separator + CLASSES_DIRECTORY;
	}

	public String getPackagedCodeRoot() {
		return getCodeRoot() + File.separator + PACKAGES_DIRECTORY;
	}

	boolean isReload() {
		return reload;
	}

	private void setReload(boolean reload) {
		this.reload = reload;
	}

}
