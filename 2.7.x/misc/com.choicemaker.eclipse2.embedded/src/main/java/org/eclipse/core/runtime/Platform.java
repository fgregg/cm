/**
 * @(#)$RCSfile: Platform.java,v $  $Revision: 1.1 $ $Date: 2010/01/27 03:58:28 $
 * 
 * Copyright (c) 2003 ChoiceMaker Technologies, Inc. 
 * 71 W 23rd St, Ste 515, New York, NY 10010 
 * All rights reserved.
 * 
 * This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */

package org.eclipse.core.runtime;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.boot.IPlatformRunnable;
import org.eclipse.core.internal.plugins.IModel;
import org.eclipse.core.internal.plugins.InternalFactory;
import org.eclipse.core.internal.plugins.PluginDescriptor;
import org.eclipse.core.internal.plugins.PluginRegistry;
import org.eclipse.core.internal.plugins.RegistryLoader;
import org.eclipse.core.internal.plugins.RegistryResolver;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.model.PluginRegistryModel;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Comment
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.1 $ $Date: 2010/01/27 03:58:28 $
 */
public class Platform {
	/**
	 * The unique identifier constant (value "<code>org.eclipse.core.runtime</code>")
	 * of the Core Runtime (pseudo-) plug-in.
	 */
	public static final String PI_RUNTIME = "org.eclipse.core.runtime"; //$NON-NLS-1$

	public static final String PLUGIN_BASE_DIR = "META-INF/plugins";
	public static final String PLUGINS_FILE = PLUGIN_BASE_DIR + "/plugins.xml";
	public static final String PLUGIN_DESCRIPTOR_FILE = "plugin.xml";
	public static final String FRAGMENT_DESCRIPTOR_FILE = "fragment.xml";

	private static final ClassLoader classLoader = Platform.class.getClassLoader();


	/** 
	 * The simple identifier constant (value "<code>applications</code>") of
	 * the extension point of the Core Runtime plug-in where plug-ins declare
	 * the existence of runnable applications. A plug-in may define any
	 * number of applications; however, the platform is only capable
	 * of running one application at a time.
	 * 
	 * @see org.eclipse.core.boot.BootLoader#run
	 */
	public static final String PT_APPLICATIONS = "applications";	 //$NON-NLS-1$

	/** 
	 * Status code constant (value 1) indicating a problem in a plug-in
	 * manifest (<code>plugin.xml</code>) file.
	 */
	public static final int PARSE_PROBLEM = 1;

	/**
	 * Status code constant (value 2) indicating an error occurred while running a plug-in.
	 */
	public static final int PLUGIN_ERROR = 2;

	private static IPluginRegistry registry;
	private static boolean initialized;
	
	static {
		init();
	}

	public static IPluginRegistry getPluginRegistry() {
		return registry;
	}

	public static synchronized void init() {
		if (!initialized) {
			MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("parse.registryProblems"), null); //$NON-NLS-1$
			InternalFactory factory = new InternalFactory(problems);
			URL[] pluginPath = getPluginPaths();
			registry = (PluginRegistry) RegistryLoader.parseRegistry(pluginPath, factory, false);
			RegistryResolver registryResolver = new RegistryResolver();
			registryResolver.resolve((PluginRegistryModel) registry);
			IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors();
			for (int i = 0; i < pluginDescriptors.length; i++) {
				PluginDescriptor pluginDescriptor = (PluginDescriptor) pluginDescriptors[i];
				try {
					activatePlugin(pluginDescriptor);
				} catch (CoreException e) {
					throw new RuntimeException("Plugin activation failed", e);
				}
			}
			initialized = true;
		}
	}

	private static void activatePlugin(PluginDescriptor pluginDescriptor) throws CoreException {
		if (!pluginDescriptor.isPluginActivated()) {
			IPluginPrerequisite[] pluginPrerequisites = pluginDescriptor.getPluginPrerequisites();
			for (int i = 0; i < pluginPrerequisites.length; i++) {
				PluginDescriptor prereq =
					(PluginDescriptor) registry.getPluginDescriptor(pluginPrerequisites[i].getUniqueIdentifier());
				activatePlugin(prereq);
			}
			pluginDescriptor.doPluginActivation();
		}
	}

	private static URL[] getPluginPaths() {
		ClassLoader classLoader = Platform.class.getClassLoader();
		try {
			XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
			PluginsParser pluginsParser = new PluginsParser();
			xmlReader.setContentHandler(pluginsParser);
			InputSource is = new InputSource(classLoader.getResourceAsStream(PLUGINS_FILE));
			xmlReader.parse(is);
			return pluginsParser.getDescriptorUrls();
		} catch (Exception ex) {
			throw new RuntimeException("Reading plugins", ex);
		}
	}
	
	/**
	 * Internal method for finding and returning a runnable instance of the 
	 * given class as defined in the specified plug-in.
	 * The returned object is initialized with the supplied arguments.
	 * <p>
	 * This method is used by the platform boot loader; is must
	 * not be called directly by client code.
	 * </p>
	 * @see BootLoader
	 */
	public static IPlatformRunnable loaderGetRunnable(String applicationName) {
		IExtension extension = registry.getExtension(Platform.PI_RUNTIME, Platform.PT_APPLICATIONS, applicationName);
		if (extension == null)
			return null;
		IConfigurationElement[] configs = extension.getConfigurationElements();
		if (configs.length == 0)
			return null;
		try {
			IConfigurationElement config = configs[0];
			return (IPlatformRunnable) config.createExecutableExtension("run"); //$NON-NLS-1$
		} catch (CoreException e) {
			return null;
		}
	}


	private static class PluginsParser extends DefaultHandler {
		private List urls = new ArrayList();

		public URL[] getDescriptorUrls() {
			return (URL[]) urls.toArray(new URL[urls.size()]);
		}
		public void startElement(String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
			qName = qName.intern();
			if (qName == IModel.PLUGIN) {
				addElement(attributes, IModel.PLUGIN_ID, PLUGIN_DESCRIPTOR_FILE);
			} else if (qName == IModel.FRAGMENT) {
				addElement(attributes, IModel.FRAGMENT_ID, FRAGMENT_DESCRIPTOR_FILE);
			}
		}
		private void addElement(Attributes attributes, String idAttribute, String descriptorFile) {
			String idWithVersion = attributes.getValue(idAttribute) + "_" + attributes.getValue(IModel.PLUGIN_VERSION);
			idWithVersion = idWithVersion.replace('.', '_');
			String resource = PLUGIN_BASE_DIR + "/" + idWithVersion + "/" + descriptorFile;
			URL url = classLoader.getResource(resource);
			urls.add(url);
		}
	}
	
	public static String getPluginDirectory(String id, String version) {
		return PLUGIN_BASE_DIR + "/" + id.replace('.', '_') + "_" + version.replace('.', '_') + "/";
	}
	
	public static URL getPluginDescriptorUrl(String id, String version, String descriptorFile) {
		return classLoader.getResource(getPluginDirectory(id, version) + descriptorFile);
	}
}
