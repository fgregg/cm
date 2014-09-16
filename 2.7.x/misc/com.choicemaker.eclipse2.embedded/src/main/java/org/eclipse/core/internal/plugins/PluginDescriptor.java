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

package org.eclipse.core.internal.plugins;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IPluginPrerequisite;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.model.ExtensionModel;
import org.eclipse.core.runtime.model.ExtensionPointModel;
import org.eclipse.core.runtime.model.PluginDescriptorModel;
import org.eclipse.core.runtime.model.PluginFragmentModel;
import org.eclipse.core.runtime.model.PluginPrerequisiteModel;

public class PluginDescriptor extends PluginDescriptorModel implements IPluginDescriptor {

	private boolean active = false; // plugin is active
	private boolean activePending = false; // being activated
	private boolean deactivated = false; // plugin deactivated due to startup errors
	protected Plugin pluginObject = null; // plugin object
	private boolean usePlatformURLs = true;
	private ResourceBundle bundle = null; // plugin.properties
	private Locale locale = null; // bundle locale
	private boolean bundleNotFound = false; // marker to prevent unnecessary lookups
	private Object[] cachedClasspath = null; // cached value of class loader's classpath

	// constants
//	static final String PLUGIN_URL = PlatformURLHandler.PROTOCOL + PlatformURLHandler.PROTOCOL_SEPARATOR + "/" + PlatformURLPluginConnection.PLUGIN + "/"; //$NON-NLS-1$ //$NON-NLS-2$
	static final String VERSION_SEPARATOR = "_"; //$NON-NLS-1$

	private static final String DEFAULT_BUNDLE_NAME = "plugin"; //$NON-NLS-1$
	private static final String KEY_PREFIX = "%"; //$NON-NLS-1$
	private static final String KEY_DOUBLE_PREFIX = "%%"; //$NON-NLS-1$

	private static final String URL_PROTOCOL_FILE = "file"; //$NON-NLS-1$

	// Places to look for library files 
	private static String[] WS_JAR_VARIANTS = buildWSVariants();
	private static String[] OS_JAR_VARIANTS = buildOSVariants();
	private static String[] NL_JAR_VARIANTS = buildNLVariants(BootLoader.getNL());
	private static String[] JAR_VARIANTS = buildVanillaVariants();

public PluginDescriptor() {
	super();
}
private static String[] buildWSVariants() {
	ArrayList result = new ArrayList();
	result.add("ws/" + BootLoader.getWS()); //$NON-NLS-1$
	result.add(""); //$NON-NLS-1$
	return (String[])result.toArray(new String[result.size()]);
}
private static String[] buildOSVariants() {
	ArrayList result = new ArrayList();
	result.add("os/" + BootLoader.getOS() + "/" + BootLoader.getOSArch()); //$NON-NLS-1$ //$NON-NLS-2$
	result.add("os/" + BootLoader.getOS()); //$NON-NLS-1$
	result.add(""); //$NON-NLS-1$
	return (String[])result.toArray(new String[result.size()]);
}
private static String[] buildNLVariants(String nl) {
	ArrayList result = new ArrayList();
	IPath base = new Path("nl"); //$NON-NLS-1$
	
	IPath path = new Path(nl.replace('_', '/'));
	while (path.segmentCount() > 0) {
		result.add(base.append(path).toString());
		// for backwards compatibility only, don't replace the slashes
		if (path.segmentCount() > 1)
			result.add(base.append(path.toString().replace('/', '_')).toString());
		path = path.removeLastSegments(1);
	}

	return (String[]) result.toArray(new String[result.size()]);
}
private static String[] buildVanillaVariants() {
	return new String[] {""}; //$NON-NLS-1$
}
private String[] buildBasePaths(String pluginBase) {
	// Now build a list of all the bases to use
	ArrayList result = new ArrayList();
	result.add(pluginBase);
	PluginFragmentModel[] fragments = getFragments();
	int fragmentLength = (fragments == null) ? 0 : fragments.length;
	for (int i = 0; i < fragmentLength; i++) {
		FragmentDescriptor fragment = (FragmentDescriptor)fragments[i];
		result.add(fragment.getInstallURL().toString());
	}
	return (String[])result.toArray(new String[result.size()]);
}
/**
 * concatenates start and end.  If end has a '.' construct at the beginning
 * trim off any leading '.' constructs.  Since the libSpec was a path, we
 * know that it was canonicalized and will only have at most one set
 * of '.' constructs at the beginning.  Returns <code>null</code> if the 
 * end is null or starts with '..'.
 */
private String concat(String start, String end) {
	if (end == null)
		return null;
	if (end.startsWith("..")) //$NON-NLS-1$
		// ISSUE: should log an error here
		// error case.  Can't '..' out of the scope of a plugin.  Signal that this
		// should be ignored (return null).
		return null;
	if (end.startsWith("./")) //$NON-NLS-1$
		return start + (end.substring(2));
	if (end.startsWith(".")) //$NON-NLS-1$
		return start + end.substring(1);
	return start + end;
}
public Object createExecutableExtension(String className, Object initData, IConfigurationElement cfig, String propertyName) throws CoreException {
	// load the requested class from this plugin
	Class classInstance = null;
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
Object createExecutableExtension(String pluginName, String className, Object initData, IConfigurationElement cfig, String propertyName) throws CoreException {
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
 * convert a list of comma-separated tokens into an array
 */
private static String[] getArrayFromList(String prop) {
	if (prop == null || prop.trim().equals("")) //$NON-NLS-1$
		return new String[0];
	Vector list = new Vector();
	StringTokenizer tokens = new StringTokenizer(prop, ","); //$NON-NLS-1$
	while (tokens.hasMoreTokens()) {
		String token = tokens.nextToken().trim();
		if (!token.equals("")) //$NON-NLS-1$
			list.addElement(token);
	}
	return list.isEmpty() ? new String[0] : (String[]) list.toArray(new String[0]);
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
	List resolvedPrerequisites = new ArrayList(prereqs.length);
	for (int i = 0; i < prereqs.length; i++) {
		if (prereqs[i].getResolvedVersion() == null)
			continue;
		String prereqId = prereqs[i].getPlugin();
		// skip over the runtime and boot plugins if they were specified.  They are automatically included
		// as the platform and parent respectively.
//		if (prereqId.equalsIgnoreCase(Platform.PI_RUNTIME) || prereqId.equalsIgnoreCase(BootLoader.PI_BOOT))
//			continue;
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
	Class runtimeClass = null;
	try {
		if (pluginClassName == null || pluginClassName.equals("")) //$NON-NLS-1$
			runtimeClass = DefaultPlugin.class;
		else
			runtimeClass = getPluginClassLoader(true).loadClass(pluginClassName);
	} catch (ClassNotFoundException e) {
		errorMsg = Policy.bind("plugin.loadClassError", getId(), pluginClassName); //$NON-NLS-1$
		throwException(errorMsg, e);
	}

	// find the correct constructor
	Constructor construct = null;
	try {
		construct = runtimeClass.getConstructor(new Class[] { IPluginDescriptor.class });
	} catch (NoSuchMethodException eNoConstructor) {
		errorMsg = Policy.bind("plugin.instantiateClassError", getId(), pluginClassName ); //$NON-NLS-1$
		throwException(errorMsg, eNoConstructor);
	}

	long time = 0L;
//	if (InternalPlatform.DEBUG_STARTUP) {
//		time = System.currentTimeMillis();
//		System.out.println("Starting plugin: " + getId()); //$NON-NLS-1$
//	}
	// create a new instance
	try {
		pluginObject = (Plugin) construct.newInstance(new Object[] { this });
	} catch (ClassCastException e) {
		errorMsg = Policy.bind("plugin.notPluginClass", pluginClassName); //$NON-NLS-1$
		throwException(errorMsg, e);
	} catch (Exception e) {
		errorMsg = Policy.bind("plugin.instantiateClassError", getId(), pluginClassName); //$NON-NLS-1$
		throwException(errorMsg, e);
	} 

	// run startup()
	final String message = Policy.bind("plugin.startupProblems", getId()); //$NON-NLS-1$
//	final MultiStatus multiStatus = new MultiStatus(Platform.PI_RUNTIME, Platform.PLUGIN_ERROR, message, null);
//	ISafeRunnable code = new ISafeRunnable() {
//		public void run() throws Exception {
//			pluginObject.startup();
//		}
//		public void handleException(Throwable e) {
//			multiStatus.add(new Status(Status.WARNING, Platform.PI_RUNTIME, Platform.PLUGIN_ERROR, message, e));
//			try {
//				pluginObject.shutdown();
//			} catch (Exception ex) {
//				// Ignore exceptions during shutdown. Since startup failed we are probably
//				// in a weird state anyway.
//			}
//		}
//	};
//	InternalPlatform.run(code);
//	if (InternalPlatform.DEBUG_STARTUP) {
//		time = System.currentTimeMillis() - time;
//		System.out.println("Finished plugin startup for " + getId() + " time: " + time + "ms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//	}
//	if (!multiStatus.isOK())
//		throw new CoreException(multiStatus);
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
//private void logError(IStatus status) {
//	InternalPlatform.getRuntimePlugin().getLog().log(status);
//	if (InternalPlatform.DEBUG)
//		System.out.println(status.getMessage());
//}
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
//private String getFragmentLocation(PluginFragmentModel fragment) {
//	if (usePlatformURLs)
//		return FragmentDescriptor.FRAGMENT_URL + fragment.toString() + "/"; //$NON-NLS-1$
//	return fragment.getLocation();
//}
//public void setPluginClassLoader(DelegatingURLClassLoader value) {
//	loader = value;
//}
//public void setPluginClassLoader(PluginClassLoader value) {
//	loader = value;
//}
private void throwException(String message, Throwable exception) throws CoreException {
	IStatus status = new Status(IStatus.ERROR, Platform.PI_RUNTIME, Platform.PLUGIN_ERROR, message, exception);
//	logError(status);
	throw new CoreException(status);
}
/**
 * @see #getUniqueIdentifierFromString
 * @see #getVersionIdentifierFromString
 */
public String toString() {
	return getUniqueIdentifier()+VERSION_SEPARATOR + getVersionIdentifier().toString();
}
//public void activateDefaultPlugins(DelegatingURLClassLoader loader) {
//	Object[] result = getPluginClassLoaderPath(true);
//	loader.addURLs((URL[]) result[0], (URLContentFilter[]) result[1], (URL[]) result[2], (URLContentFilter[]) result[3]);
//}
/**
 * @see IPluginDescriptor
 */
public final URL find(IPath path) {
	return null; // find(path, null);
}
///**
// * @see IPluginDescriptor
// */
//public final URL find(IPath path, Map override) {
//	if (path == null)
//		return null;
//	
//	URL install = getInstallURLInternal();
//	URL result = null;	
//	
//	// Check for the empty or root case first
//	if (path.isEmpty() || path.isRoot()) {
//		// Watch for the root case.  It will produce a new
//		// URL which is only the root directory (and not the
//		// root of this plugin).	
//		result = findInPlugin(install, Path.EMPTY);
//		if (result == null)
//			result = findInFragments(Path.EMPTY);
//		return result;
//	}
//	
//	// Now check for paths without variable substitution
//	String first = path.segment(0);
//	if (first.charAt(0) != '$') {
//		result = findInPlugin(install, path);
//		if (result == null)
//			result = findInFragments(path);
//		return result;	
//	}
//		
//	// Worry about variable substitution
//	IPath rest = path.removeFirstSegments(1);
//	if (first.equalsIgnoreCase("$nl$")) //$NON-NLS-1$
//		return findNL(install, rest, override);
//	if (first.equalsIgnoreCase("$os$")) //$NON-NLS-1$
//		return findOS(install, rest, override);
//	if (first.equalsIgnoreCase("$ws$")) //$NON-NLS-1$
//		return findWS(install, rest, override);
//	if (first.equalsIgnoreCase("$files$")) //$NON-NLS-1$
//		return null;
//
//	return null;
//}

private URL findOS(URL install, IPath path, Map override) {
	String os = null;
	if (override != null)
		try {
			// check for override
			os = (String) override.get("$os$"); //$NON-NLS-1$
		} catch (ClassCastException e) {
			// just in case
		}
	if (os == null)
		// use default
		os = BootLoader.getOS();
	if (os.length() == 0)
		return null;
		
	// Now do the same for osarch
	String osArch = null;
	if (override != null)
		try {
			// check for override
			osArch = (String) override.get("$arch$"); //$NON-NLS-1$
		} catch (ClassCastException e) {
			// just in case
		}
	if (osArch == null)
		// use default
		osArch = BootLoader.getOSArch();
	if (osArch.length() == 0)
		return null;

	URL result = null;
	IPath base = new Path("os").append(os).append(osArch); //$NON-NLS-1$
	// Keep doing this until all you have left is "os" as a path
	while (base.segmentCount() != 1) {
		IPath filePath = base.append(path);	
		result = findInPlugin(install, filePath);
		if (result != null)
			return result;	
		result = findInFragments(filePath);
		if (result != null)
			return result;
		base = base.removeLastSegments(1);
	}
	// If we get to this point, we haven't found it yet.
	// Look in the plugin and fragment root directories
	result = findInPlugin(install, path);
	if (result != null)
		return result;
	return findInFragments(path);
}

private URL findWS(URL install, IPath path, Map override) {
	String ws = null;
	if (override != null)
		try {
			// check for override
			ws = (String) override.get("$ws$"); //$NON-NLS-1$
		} catch (ClassCastException e) {
			// just in case
		}
	if (ws == null)
		// use default
		ws = BootLoader.getWS();
	IPath filePath = new Path("ws").append(ws).append(path); //$NON-NLS-1$
	// We know that there is only one segment to the ws path
	// e.g. ws/win32	
	URL result = findInPlugin(install, filePath);
	if (result != null)
		return result;	
	result = findInFragments(filePath);
	if (result != null)
		return result;
	// If we get to this point, we haven't found it yet.
	// Look in the plugin and fragment root directories
	result = findInPlugin(install, path);
	if (result != null)
		return result;
	return findInFragments(path);
}

private URL findNL(URL install, IPath path, Map override) {
	String nl = null;
	String[] nlVariants = null;
	if (override != null)
		try {
			// check for override
			nl = (String) override.get("$nl$"); //$NON-NLS-1$
		} catch (ClassCastException e) {
			// just in case
		}
	nlVariants = nl == null ? NL_JAR_VARIANTS : buildNLVariants(nl);
	if (nl != null && nl.length() == 0)
		return null;

	URL result = null;
	for (int i=0; i<nlVariants.length; i++) {
		IPath filePath = new Path(nlVariants[i]).append(path);
		result = findInPlugin(install, filePath);
		if (result != null)
			return result;
		result = findInFragments(filePath);
		if (result != null)
			return result;
	}
	// If we get to this point, we haven't found it yet.
	// Look in the plugin and fragment root directories
	result = findInPlugin(install, path);
	if (result != null)
		return result;
	return findInFragments(path);
}

private URL findInPlugin(URL install, IPath filePath) {
	try {
		URL location = new URL(install, filePath.toString());
		String file = getFileFromURL(location);
		if (file != null && new File(file).exists()) {
			Path pluginRootPath = new Path(install.getFile());
			Path foundPath = new Path(file);
			if (pluginRootPath.isPrefixOf(foundPath))
				return location;
		}						
	} catch (IOException e) {
		// ignore bad URLs
	}
	return null;
}

private URL findInFragments(IPath filePath) {
	// This method will return a 'real' URL (as opposed to a platform
	// URL).
	PluginFragmentModel[] fragments = getFragments();
	if (fragments == null)
		return null;
		
	for (int i = 0; i < fragments.length; i++) {
		try {
			URL fragmentRootURL =  new URL(fragments[i].getLocation());
			URL location = new URL(fragmentRootURL, filePath.toString());
			String file = getFileFromURL(location);
			if (file != null && new File(file).exists()) {
				Path fragmentRootPath = new Path(fragmentRootURL.getFile());
				Path foundPath = new Path(file);
				if (fragmentRootPath.isPrefixOf(foundPath))
					return location;
			}
		} catch (IOException e) {
			// skip malformed url and urls that cannot be resolved
		}
	}
	return null;
}
}
