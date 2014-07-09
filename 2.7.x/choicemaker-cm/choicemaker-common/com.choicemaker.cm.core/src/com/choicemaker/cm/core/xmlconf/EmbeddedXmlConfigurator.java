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
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Platform;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.MachineLearner;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.core.compiler.ICompiler;
import com.choicemaker.cm.core.configure.ChoiceMakerConfiguration;
import com.choicemaker.cm.core.configure.ChoiceMakerConfigurator;
import com.choicemaker.cm.core.configure.MachineLearnerPersistence;
import com.choicemaker.cm.core.configure.ProbabilityModelPersistence;
import com.choicemaker.cm.core.report.Reporter;
import com.choicemaker.util.SystemPropertyUtils;

/**
 * XML configuration file reader.
 *
 * A ChoiceMaker configuration file consists of the following three types of elements:
 * <ul>
 *   <li>An arbitrary number of modules. Modules are self-initializing, that is, they
 *       list the class to be used for their initialization. All modules are initialized
 *       by calling <code>initModules</code>, which is also called by <code>init</code>.
 *       An example of a module is the file collections, e.g., the file containing generic
 *       first names, such as "BABY".
 *   </li>
 *   <li>Static initialization components. It is the client code's responsibility to call
 *       the specific initializers for these components. The <code>OracleConnectionCacheXmlConf</code>
 *       is an example of such an initializer.
 *   </li>
 * </ul>
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/24 18:57:16 $
 */
public class EmbeddedXmlConfigurator implements ChoiceMakerConfigurator, ChoiceMakerConfiguration {

	private static final Logger logger = Logger.getLogger(EmbeddedXmlConfigurator.class);

	public static final String EMBEDDED_FILENAME = "META-INF/project.xml";

	public static final String DEFAULT_LOG4J_CONF = "j2ee";

	private static EmbeddedXmlConfigurator instance = new EmbeddedXmlConfigurator();

	private XmlConfigurator delegate = XmlConfigurator.getInstance();

	public static EmbeddedXmlConfigurator getInstance() {
		return instance;
	}

	/**
	 * @see #init()
	 * @param ignored may be null
	 * @throws XmlConfException
	 */
	public synchronized void embeddedInit(ICompiler ignored) throws XmlConfException {
		getInstance().init();
	}

	protected void initClassLoader() throws XmlConfException {
			delegate.classLoader = EmbeddedXmlConfigurator.class.getClassLoader();
	}

	protected void initReloadClassPath() throws XmlConfException {
		delegate.reloadClassPath = new URL[0];
	}

	public ClassLoader reload() throws XmlConfException {
		// FIXME non-functional method stub
		throw new Error("not yet implemented");
//		delegate.reloadClassLoader = EmbeddedXmlConfigurator.class.getClassLoader();
//		Element rl = delegate.getCore().getChild("reload");
//		if (rl != null) {
//			delegate.initializeModules(rl.getChildren("module"), delegate.reloadClassLoader);
//		}
//		return delegate.reloadClassLoader;
	}

	protected void readConfigurationFile() throws XmlConfException {
		readConfigurationFileFromResource();
	}

	protected void readConfigurationFileFromResource() throws XmlConfException {
		SAXBuilder builder = XmlParserFactory.createSAXBuilder(false);
		try {
			URL url = XmlConfigurator.class.getClassLoader().getResource(
					EMBEDDED_FILENAME);
			if (url == null) {
				throw new XmlConfException(
						"Unable to load " + EMBEDDED_FILENAME);
			}
			delegate.document = builder.build(url);
		} catch (Exception ex) {
			throw new XmlConfException("Internal error.", ex);
		}
	}

	private EmbeddedXmlConfigurator() {
	}

	public ClassLoader getRmiClassLoader() {
		return delegate.classLoader;
	}

	public String getJavaDocClasspath() {
		String pathSeparator = System.getProperty(SystemPropertyUtils.PATH_SEPARATOR);
		String res = null;
		IPluginDescriptor[] plugins = Platform.getPluginRegistry().getPluginDescriptors();
		for (int i = 0; i < plugins.length; i++) {
			URL[] ucp = ((URLClassLoader) plugins[i].getPluginClassLoader()).getURLs();
			for (int j = 0; j < ucp.length; j++) {
				if (res == null) {
					res = "";
				} else {
					res += pathSeparator;
				}
				res += ucp[j].getPath();
			}
		}
		return res;
	}

	/**
	 * Returns the configuration file name.
	 *
	 * @return  The configuration file name.
	 */
	public String getFileName() {
		return delegate.fileName;
	}

	public ClassLoader getClassLoader() {
		return delegate.classLoader;
	}

	public boolean isValid() {
		return true;
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

	public String getClassPath() {
		// FIXME non-functional method stub
		throw new Error("not yet implemented");
	}

	public String getReloadClassPath() {
		// FIXME non-functional method stub
		throw new Error("not yet implemented");
	}

	public void reloadClasses() {
		// FIXME non-functional method stub
		throw new Error("not yet implemented");
	}

	public String toXml() {
		// FIXME non-functional method stub
		throw new Error("not yet implemented");
	}

	/**
	 * Initializes ChoiceMaker from an XML configuration file.
	 * Reads the XML configuration file and initializes logging and all modules.
	 *
	 * @param   fn  The name of the configuration file.
	 * @return
	 * @throws  XmlConfException  if any error occurs.
	 */
	public ChoiceMakerConfiguration init() throws XmlConfException {

		// FIXME non-functional method stub
		throw new Error("not yet implemented");
//		Platform.getPluginRegistry();
//		delegate.reload = false;
//		delegate.initializeInstallableComponents();
//		delegate.fileName = EMBEDDED_FILENAME;
//		readConfigurationFileFromResource();
//		delegate.setWorkingDir();
//		Log4jXmlConf.configIfPresent(DEFAULT_LOG4J_CONF);
//		initClassLoader();
//		initReloadClassPath();
//		delegate.initializeModules(delegate.getCore().getChildren("module"), delegate.classLoader);
//		ICompiler compiler = getChoiceMakerCompiler();
//		ProbabilityModelsXmlConf.loadProductionProbabilityModels(compiler, true);
//		initReports();
//
//		return this;
	}

	/**
	 * All argument are ignored
	 */
	public ChoiceMakerConfiguration init(String fn, String log4jConfName, boolean reload, boolean initGui) throws XmlConfException {
		return init();
	}

	/**
	 * All arguments are ignored
	 */
	public ChoiceMakerConfiguration init(String fn, boolean reload,
			boolean initGui) throws XmlConfException {
		return init();
	}

	void initReports() {
		List reporters = new ArrayList();
		IExtensionPoint reporterExts = Platform.getPluginRegistry().getExtensionPoint("com.choicemaker.cm.core.reporter");
		List reporterConfigs = delegate.getCore().getChildren("reporter");
		for (Iterator iReporterConfigs = reporterConfigs.iterator(); iReporterConfigs.hasNext();) {
			Element reporterConfig = (Element) iReporterConfigs.next();
			try {
				String ext = reporterConfig.getAttributeValue("extension");
				Reporter reporter = (Reporter) reporterExts.getExtension(ext).getConfigurationElements()[0].createExecutableExtension("class");
				Method[] methods = reporter.getClass().getMethods();
				HashMap methodMap = new HashMap();
				for (int i = 0; i < methods.length; i++) {
					methodMap.put(methods[i].getName(), methods[i]);
				}
				List properties = reporterConfig.getChildren("property");
				for (Iterator iProperties = properties.iterator(); iProperties.hasNext();) {
					Element property = (Element) iProperties.next();
					String name = property.getAttributeValue("name");
					name = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
					String value = property.getAttributeValue("value");
					delegate.set((Method) methodMap.get(name), reporter, value);
				}
				reporter.open();
				reporters.add(reporter);
			} catch (Exception ex) {
				logger.error("Configuring reporter", ex);
			}
		}
		PMManager.setGlobalReporters((Reporter[]) reporters.toArray(new Reporter[reporters.size()]));
	}

	public ICompiler getChoiceMakerCompiler() {
		// FIXME non-functional method stub
		throw new Error("not yet implemented");
	}

	public File getWorkingDirectory() {
		return delegate.getWorkingDirectory();
	}

	public String getClueMakerSourceRoot() {
		// FIXME non-functional method stub
		throw new Error("not yet implemented");
	}

	public String getGeneratedSourceRoot() {
		// FIXME non-functional method stub
		throw new Error("not yet implemented");
	}

	public String getCompiledCodeRoot() {
		// FIXME non-functional method stub
		throw new Error("not yet implemented");
	}

	public String getPackagedCodeRoot() {
		// FIXME non-functional method stub
		throw new Error("not yet implemented");
	}

	public void deleteGeneratedCode() {
		throw new UnsupportedOperationException();
	}

}

