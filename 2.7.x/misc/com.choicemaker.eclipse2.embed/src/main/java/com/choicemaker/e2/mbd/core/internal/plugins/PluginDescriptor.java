/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.choicemaker.e2.mbd.core.internal.plugins;

import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import com.choicemaker.e2.mbd.core.internal.runtime.Policy;
import com.choicemaker.e2.mbd.core.runtime.CoreException;
import com.choicemaker.e2.mbd.core.runtime.IConfigurationElement;
import com.choicemaker.e2.mbd.core.runtime.IExecutableExtension;
import com.choicemaker.e2.mbd.core.runtime.IExtension;
import com.choicemaker.e2.mbd.core.runtime.IExtensionPoint;
import com.choicemaker.e2.mbd.core.runtime.IPath;
import com.choicemaker.e2.mbd.core.runtime.IPluginDescriptor;
import com.choicemaker.e2.mbd.core.runtime.IPluginPrerequisite;
import com.choicemaker.e2.mbd.core.runtime.IStatus;
import com.choicemaker.e2.mbd.core.runtime.Platform;
import com.choicemaker.e2.mbd.core.runtime.Plugin;
import com.choicemaker.e2.mbd.core.runtime.PluginVersionIdentifier;
import com.choicemaker.e2.mbd.core.runtime.Status;
import com.choicemaker.e2.mbd.core.runtime.model.ExtensionModel;
import com.choicemaker.e2.mbd.core.runtime.model.ExtensionPointModel;
import com.choicemaker.e2.mbd.core.runtime.model.PluginDescriptorModel;
import com.choicemaker.e2.mbd.core.runtime.model.PluginPrerequisiteModel;

public class PluginDescriptor extends PluginDescriptorModel implements IPluginDescriptor {
	
	private static final Logger logger = Logger
			.getLogger(PluginDescriptor.class.getName());

	private boolean active = false; // plugin is active
	private boolean activePending = false; // being activated
	private boolean deactivated = false; // plugin deactivated due to startup errors
	protected Plugin pluginObject = null; // plugin object
	private boolean bundleNotFound = false; // marker to prevent unnecessary lookups

	// constants
	static final String VERSION_SEPARATOR = "_"; //$NON-NLS-1$

	private static final String DEFAULT_BUNDLE_NAME = "plugin"; //$NON-NLS-1$
	private static final String KEY_PREFIX = "%"; //$NON-NLS-1$
	private static final String KEY_DOUBLE_PREFIX = "%%"; //$NON-NLS-1$

public PluginDescriptor() {
	super();
}

public Object createExecutableExtension(String className, Object initData,
		IConfigurationElement cfig, String propertyName)
		throws CoreException {
// load the requested class from this plugin
Class<?> classInstance = null;
try {
	classInstance = getPluginClassLoader(true).loadClass(className);
} catch (Exception e1) {
	throwException(Policy.bind("plugin.loadClassError", getId(), className), e1); //$NON-NLS-1$
}

// create a new instance
Object result = null;
try {
	result = classInstance.newInstance();
} catch (Exception e) {
	throwException(Policy.bind("plugin.instantiateClassError", getId(), className), e); //$NON-NLS-1$
}

// check if we have extension adapter and initialize
if (result instanceof IExecutableExtension) {
	try {
		// make the call even if the initialization string is null
		 ((IExecutableExtension) result).setInitializationData(cfig, propertyName, initData);
	} catch (CoreException ce) {
		// user code threw exception
//			logError(ce.getStatus());
		throw new CoreException(ce.getStatus());
	} catch (Exception te) {
		// user code caused exception
		throwException(Policy.bind("policy.initObjectError", getId(), className), te); //$NON-NLS-1$
	}
}
return result;
}

Object createExecutableExtension(String pluginName, String className,
		Object initData, IConfigurationElement cfig, String propertyName)
		throws CoreException {
	String id = getUniqueIdentifier(); // this plugin id
	// check if we need to delegate to some other plugin
	if (pluginName != null && !pluginName.equals("") && !pluginName.equals(id)) { //$NON-NLS-1$
		PluginDescriptor plugin = null;
		plugin = (PluginDescriptor) getPluginRegistry().getPluginDescriptor(pluginName);
		return plugin.createExecutableExtension(className, initData, cfig, propertyName);
	}
	return createExecutableExtension(className, initData, cfig, propertyName);
}
public synchronized void doPluginActivation() throws CoreException {
	// this method is called by the class loader just prior 
	// to getting a class. It needs to handle the
	// case where it is called multiple times during the activation
	// processing itself (as a result of other classes from this
	// plugin being directly referenced by the plugin class)

	// NOTE: there is a remote scenario where the plugin class can
	// deadlock, if it starts separate thread(s) within its
	// constructor or startup() method, and waits on those
	// threads before returning (ie. calls join()).

	boolean errorExit = true;

	// check if already activated or pending
	if (pluginActivationEnter()) {
		try {
//			if (DelegatingURLClassLoader.MONITOR_PLUGINS)			
//				PluginStats.startActivation(this.getUniqueIdentifier());			
			internalDoPluginActivation();
			errorExit = false;
		} finally {
			pluginActivationExit(errorExit);
//			if (DelegatingURLClassLoader.MONITOR_PLUGINS)			
//				PluginStats.endActivation(this.getUniqueIdentifier());			
		}
	}
}
synchronized void doPluginDeactivation() {
//	loader = null;
	pluginObject = null;
	active = false;
	activePending = false;
	deactivated = false;
}

/**
 * @see IPluginDescriptor
 */
public IExtension getExtension(String id) {
	if (id == null)
		return null;
	ExtensionModel[] list = getDeclaredExtensions();
	if (list == null)
		return null;
	for (int i = 0; i < list.length; i++) {
		if (id.equals(list[i].getId()))
			return (IExtension) list[i];
	}
	return null;
}
/**
 * @see IPluginDescriptor
 */
public IExtensionPoint getExtensionPoint(String extensionPointId) {
	if (extensionPointId == null)
		return null;
	ExtensionPointModel[] list = getDeclaredExtensionPoints();
	if (list == null)
		return null;
	for (int i = 0; i < list.length; i++) {
		if (extensionPointId.equals(list[i].getId()))
			return (IExtensionPoint) list[i];
	}
	return null;
}
/**
 * @see IPluginDescriptor
 */
public IExtensionPoint[] getExtensionPoints() {
	ExtensionPointModel[] list = getDeclaredExtensionPoints();
	if (list == null)
		return new IExtensionPoint[0];
	IExtensionPoint[] newValues = new IExtensionPoint[list.length];
	System.arraycopy(list, 0, newValues, 0, list.length);
	return newValues;
}
/**
 * @see IPluginDescriptor
 */
public IExtension[] getExtensions() {
	ExtensionModel[] list = getDeclaredExtensions();
	if (list == null)
		return new IExtension[0];
	IExtension[] newValues = new IExtension[list.length];
	System.arraycopy(list, 0, newValues, 0, list.length);
	return newValues;
}
/**
 * @see IPluginDescriptor
 */
public URL getInstallURL() {
	try {
		URL pluginXmlUrl = Platform.getPluginDescriptorUrl(getId(), getVersion(), Platform.PLUGIN_DESCRIPTOR_FILE);
		return new URL(pluginXmlUrl, "./");
	} catch (MalformedURLException e) {
		throw new IllegalStateException(); // unchecked
	}
}
//public URL getInstallURLInternal() {
//	String url = getLocation();
//	try {
//		return PlatformURLFactory.createURL(url);
//	} catch (MalformedURLException e) {
//		throw new IllegalStateException(); // unchecked
//	}
//}
/**
 * @see IPluginDescriptor
 */
public String getLabel() {
	String s = getName();
	if (s == null)
		return ""; //$NON-NLS-1$
	 String localized = getResourceString(s);
	 if (localized != s)
	 	setLocalizedName(localized);
	 return localized;
}
/**
 * @see IPluginDescriptor
 */
public Plugin getPlugin() throws CoreException {
	if (pluginObject == null)
		doPluginActivation();
	return pluginObject;
}
/**
 * @see IPluginDescriptor
 */
public ClassLoader getPluginClassLoader() {
	return getPluginClassLoader(true);
}
public ClassLoader getPluginClassLoader(boolean eclipseURLs) {
	return this.getClass().getClassLoader();
//	if (loader != null)
//		return loader;
//
//	Object[] path = getPluginClassLoaderPath(eclipseURLs);
//	URL[] codePath = (URL[]) path[0];
//	URLContentFilter[] codeFilters = (URLContentFilter[]) path[1];
//	URL[] resourcePath = (URL[]) path[2];
//	URLContentFilter[] resourceFilters = (URLContentFilter[]) path[3];
//	// Create the classloader.  The parent should be the parent of the platform class loader.  
//	// This allows us to decouple standard parent loading from platform loading.
//	loader = new PluginClassLoader(codePath, codeFilters, resourcePath, resourceFilters, PlatformClassLoader.getDefault().getParent(), this);
//	loader.initializeImportedLoaders();
//	// Note: need to be able to give out a loader reference before
//	// its prereqs are initialized. Otherwise loops in prereq
//	// definition will cause endless loop in initializePrereqs()
//	return loader;
}

public String getFileFromURL(URL target) {
//	String protocol = target.getProtocol();
//	if (protocol.equals(PlatformURLHandler.FILE))
//		return target.getFile();
//	if (protocol.equals(PlatformURLHandler.JAR)) {
//		// strip off the jar separator at the end of the url then do a recursive call
//		// to interpret the sub URL.
//		String file = target.getFile();
//		file = file.substring(0, file.length() - PlatformURLHandler.JAR_SEPARATOR.length());
//		try {
//			return getFileFromURL(PlatformURLFactory.createURL(file));
//		} catch (MalformedURLException e) {
//			// ignore bad URLs
//		}
//	}
	return null;
}

/**
 * @see IPluginDescriptor
 */
public IPluginPrerequisite[] getPluginPrerequisites() {
	PluginPrerequisiteModel[] list = getRequires();
	if (list == null)
		return new IPluginPrerequisite[0];
	IPluginPrerequisite[] newValues = new IPluginPrerequisite[list.length];
	System.arraycopy(list, 0, newValues, 0, list.length);
	return newValues;
}
public PluginRegistry getPluginRegistry() {
	return (PluginRegistry) getRegistry();
}
/**
 * @see IPluginDescriptor
 */
public String getProviderName() {
	String s = super.getProviderName();
	if (s == null)
		return ""; //$NON-NLS-1$
	 String localized = getResourceString(s);
	 if (localized != s)
		setLocalizedProviderName(localized);
	 return localized;
}
/**
 * @see IPluginDescriptor
 */
public ResourceBundle getResourceBundle() throws MissingResourceException {
	return getResourceBundle(Locale.getDefault());
}
public ResourceBundle getResourceBundle(Locale targetLocale) throws MissingResourceException {
	String resourceName = "META-INF.plugins." + getPluginId().replace('.', '_') + "_" + getVersion().replace('.', '_') + "." + DEFAULT_BUNDLE_NAME;
	if(bundleNotFound) {
		throw new MissingResourceException("Resource not found", getId(), resourceName + "_" + targetLocale);
	}
	bundleNotFound = true;
	ResourceBundle bundle = ResourceBundle.getBundle(resourceName, targetLocale);
	bundleNotFound = false;
	return bundle;
//	// we cache the bundle for a single locale 
//	if (bundle != null && targetLocale.equals(locale))
//		return bundle;
//
//	// check if we already tried and failed
//	if (bundleNotFound)
//		throw new MissingResourceException(Policy.bind("plugin.bundleNotFound", getId(), DEFAULT_BUNDLE_NAME + "_" + targetLocale), DEFAULT_BUNDLE_NAME + "_" + targetLocale, ""); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
//
//	// try to load bundle from this plugin. A new loader is created to include the base 
//	// install directory on the search path (to maintain compatibility with current handling
//	// of plugin.properties bundles (not in code jar)).
//	URL[] cp = (URL[])getPluginClassLoaderPath(true)[0];
//	URL[] newcp = new URL[cp.length+1];
//	for (int i=0; i<cp.length; i++) newcp[i+1] = cp[i];
//	try {
//		newcp[0] = Platform.resolve(getInstallURL()); // always try to resolve URLs used in loaders
//	} catch(IOException e) {
//		newcp[0] = getInstallURL();
//	}
//	ClassLoader resourceLoader = new URLClassLoader(newcp, null);
//	ResourceBundle newBundle = null;
//	try {
//		newBundle = ResourceBundle.getBundle(DEFAULT_BUNDLE_NAME, targetLocale, resourceLoader);
////		if (DelegatingURLClassLoader.MONITOR_BUNDLES) 
////			ClassloaderStats.loadedBundle(getUniqueIdentifier(), new BundleStats(getUniqueIdentifier(), DEFAULT_BUNDLE_NAME+".properties", newBundle)); //$NON-NLS-1$
//		bundle = newBundle;
//		locale = targetLocale;
//	} catch (MissingResourceException e) {
//		bundleNotFound = true;
//		throw e;
//	}
//
//	return newBundle;
}
/**
 * @see IPluginDescriptor
 */
public String getResourceString(String value) {
	return getResourceString(value, null);
}
/**
 * @see IPluginDescriptor
 */
public String getResourceString(String value, ResourceBundle b) {
	String s = value.trim();
	if (!s.startsWith(KEY_PREFIX)) return s;
	if (s.startsWith(KEY_DOUBLE_PREFIX)) return s.substring(1);
	
	int ix = s.indexOf(" "); //$NON-NLS-1$
	String key = ix == -1 ? s : s.substring(0,ix);
	String dflt = ix == -1 ? s : s.substring(ix+1);

	if (b==null) {
		try { 
			b = getResourceBundle();
		} catch (MissingResourceException e) {
			// just return the default (dflt)
		};
	}
	
	if (b==null) return dflt;
	
	try { 
		return b.getString(key.substring(1));
	} catch(MissingResourceException e) {
		//this will avoid requiring a bundle access on the next lookup
		return "%" + dflt;
	}
}
///**
// * @see IPluginDescriptor
// */
//public ILibrary[] getRuntimeLibraries() {
//	LibraryModel[] list = getRuntime();
//	if (list == null)
//		return new ILibrary[0];
//	ILibrary[] newValues = new ILibrary[list.length];
//	System.arraycopy(list, 0, newValues, 0, list.length);
//	return newValues;
//}
/**
 * @see IPluginDescriptor
 */
public String getUniqueIdentifier() {
	return getId();
}
/**
 * @see #toString
 */
public static String getUniqueIdentifierFromString(String pluginString) {	
	int ix = pluginString.indexOf(VERSION_SEPARATOR);
	return ix==-1 ? pluginString : pluginString.substring(0,ix);
}
/**
 * @see IPluginDescriptor
 */
public PluginVersionIdentifier getVersionIdentifier() {
	String version = getVersion();
	if (version == null)
		return new PluginVersionIdentifier("1.0.0"); //$NON-NLS-1$
	try {
		return new PluginVersionIdentifier(version);
	} catch (Exception e) {
		return new PluginVersionIdentifier("1.0.0"); //$NON-NLS-1$
	}
}
///**
// * @see #toString
// */
//public static PluginVersionIdentifier getVersionIdentifierFromString(String pluginString) {
//	int ix = pluginString.indexOf("_"); //$NON-NLS-1$
//	if (ix==-1) return null;
//	String vid = pluginString.substring(ix+1);	
//	try {
//		return new PluginVersionIdentifier(vid);
//	} catch (Exception e) {
//		return null;
//	}
//}
/**
 * Returns all pre-requisites that have been properly resolved, excluding any
 * redundant references to Platform.PI_RUNTIME and BootLoader.PI_BOOT.
 */
public IPluginPrerequisite[] getPluginResolvedPrerequisites() {
	PluginPrerequisiteModel[] prereqs = this.getRequires();
	if (prereqs == null || prereqs.length == 0)
		return new IPluginPrerequisite[0];
	List<PluginPrerequisiteModel> resolvedPrerequisites = new ArrayList<>(prereqs.length);
	for (int i = 0; i < prereqs.length; i++) {
		if (prereqs[i].getResolvedVersion() == null)
			continue;
		resolvedPrerequisites.add(prereqs[i]);
	}
	if (resolvedPrerequisites.isEmpty())
		return new IPluginPrerequisite[0];
	return (IPluginPrerequisite[]) resolvedPrerequisites.toArray(new IPluginPrerequisite[resolvedPrerequisites.size()]);
}

private void internalDoPluginActivation() throws CoreException {

	String errorMsg;
	// load the runtime class
	String pluginClassName = getPluginClass();
	Class<?> runtimeClass = null;
	if (pluginClassName == null || pluginClassName.equals("")) { //$NON-NLS-1$
		runtimeClass = DefaultPlugin.class;
	} else {
		// FIXME externalize message
		errorMsg = "Ignoring customized plugin class: " + pluginClassName;
		logger.warning(errorMsg);
		// runtimeClass =
		// getPluginClassLoader(true).loadClass(pluginClassName);
		runtimeClass = DefaultPlugin.class;
	}

	// find the correct constructor
	Constructor<?> construct = null;
	try {
		construct =
			runtimeClass
					.getConstructor(new Class[] { IPluginDescriptor.class });
	} catch (NoSuchMethodException eNoConstructor) {
		errorMsg = Policy.bind("plugin.instantiateClassError", getId(), pluginClassName ); //$NON-NLS-1$
		throwException(errorMsg, eNoConstructor);
	}

	// create a new instance
	try {
		pluginObject =
			(Plugin) construct.newInstance(new Object[] { this });
	} catch (ClassCastException e) {
		errorMsg = Policy.bind("plugin.notPluginClass", pluginClassName); //$NON-NLS-1$
		throwException(errorMsg, e);
	} catch (Exception e) {
		errorMsg =
			Policy.bind(
					"plugin.instantiateClassError", getId(), pluginClassName); //$NON-NLS-1$
		throwException(errorMsg, e);
	}

	// skip startup()
	// FIXME externalize message
	errorMsg = "Skipping startup: " + pluginClassName;
	logger.fine(errorMsg);
}

/**
 * @see IPluginDescriptor
 */
public synchronized boolean isPluginActivated() {
	//note that this method is synchronized for good reason.  
	//During plugin activation, neither true nor false would be valid
	//return values for this method, so it must block until activation
	//completes.  For example, returning false during activation
	//would break the registry shutdown procedure, because a
	//plugin being activated during shutdown would never be shut down.
	return active;
}

public synchronized boolean isPluginDeactivated() {
	return deactivated;
}

/**
 * Returns <code>true</code> if we should continue with the plugin activation.
 */
private boolean pluginActivationEnter() throws CoreException {
	if (deactivated) {
		// had permanent error on startup
		String errorMsg = Policy.bind("plugin.pluginDisabled", getId()); //$NON-NLS-1$
		throwException(errorMsg, null);
	}
	if (active || activePending) {
		// already up and running 
		return false;
	}
	activePending = true;
	// go ahead and try to activate
	return true;
}
private void pluginActivationExit(boolean errorExit) {
	// we are done with with activation
	activePending = false;
	if (errorExit) {
		active = false;
		deactivated = true;
	} else
		active = true;
}

private void throwException(String message, Throwable exception) throws CoreException {
	IStatus status = new Status(IStatus.ERROR, Platform.PI_RUNTIME, Platform.PLUGIN_ERROR, message, exception);
	logger.severe(message);
	throw new CoreException(status);
}

/**
 * @see #getUniqueIdentifierFromString
 * @see #getVersionIdentifierFromString
 */
public String toString() {
	return getUniqueIdentifier()+VERSION_SEPARATOR + getVersionIdentifier().toString();
}

/**
 * @see IPluginDescriptor
 */
public final URL find(IPath path) {
	return null; // find(path, null);
}

}
