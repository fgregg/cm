package com.choicemaker.e2.platform;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.choicemaker.e2.CMExtension;
import com.choicemaker.e2.CMExtensionPoint;
import com.choicemaker.e2.CMPlatform;
import com.choicemaker.e2.CMPluginDescriptor;
import com.choicemaker.e2.CMPluginRegistry;

public class CMPlatformUtils {

	private static final Logger logger = Logger.getLogger(CMPlatformUtils.class
			.getName());
	private static final String SIMPLE_CLASS_NAME = CMPlatformUtils.class
			.getSimpleName();

	public static CMPluginRegistry getPluginRegistry() {
		// final String METHOD = "getPluginRegistry";
		// logger.entering(SIMPLE_CLASS_NAME, METHOD);
		CMPlatform platform = InstallablePlatform.getInstance();
		CMPluginRegistry retVal = platform.getPluginRegistry();
		// logger.exiting(SIMPLE_CLASS_NAME, METHOD, retVal);
		return retVal;
	}

	public static CMPluginDescriptor[] getPluginDescriptors() {
		final String METHOD = "getPluginDecriptors";
		logger.entering(SIMPLE_CLASS_NAME, METHOD);
		CMPluginRegistry registry = getPluginRegistry();
		CMPluginDescriptor[] retVal = registry.getPluginDescriptors();
		logger.exiting(SIMPLE_CLASS_NAME, METHOD, retVal);
		return retVal;
	}

	public static CMPluginDescriptor getPluginDescriptor(String pluginId) {
		final String METHOD = "getPluginDescriptor";
		logger.entering(SIMPLE_CLASS_NAME, METHOD, pluginId);
		CMPluginRegistry registry = getPluginRegistry();
		CMPluginDescriptor retVal = registry.getPluginDescriptor(pluginId);
		logger.exiting(SIMPLE_CLASS_NAME, METHOD, retVal);
		return retVal;
	}

	/** Returns the extensions implemented by the specified plugin */
	public static CMExtension[] getPluginExtensions(String pluginId) {
		final String METHOD = "getPluginDescriptor";
		logger.entering(SIMPLE_CLASS_NAME, METHOD, pluginId);
		CMPluginDescriptor plugin = getPluginDescriptor(pluginId);
		CMExtension[] retVal;
		if (plugin == null) {
			logger.severe("no plugin found for: " + pluginId);
			retVal = new CMExtension[0];
		} else {
			retVal = plugin.getExtensions();
		}
		logger.exiting(SIMPLE_CLASS_NAME, METHOD, retVal);
		return retVal;
	}

	/** Returns the extension points defined by the specified plugin */
	public static CMExtensionPoint[] getPluginExtensionPoints(String pluginId) {
		final String METHOD = "getPluginDescriptor";
		logger.entering(SIMPLE_CLASS_NAME, METHOD, pluginId);
		CMPluginDescriptor plugin = getPluginDescriptor(pluginId);
		CMExtensionPoint[] retVal = plugin.getExtensionPoints();
		logger.exiting(SIMPLE_CLASS_NAME, METHOD, retVal);
		return retVal;
	}

	public static ClassLoader[] getPluginClassLoaders() {
		final String METHOD = "getPluginClassLoaders";
		logger.entering(SIMPLE_CLASS_NAME, METHOD);
		List<ClassLoader> loaders = new LinkedList<>();
		CMPluginDescriptor[] plugins = getPluginDescriptors();
		for (CMPluginDescriptor plugin : plugins) {
			logger.finer("Adding classloader for: "
					+ plugin.getUniqueIdentifier());
			ClassLoader loader = plugin.getPluginClassLoader();
			loaders.add(loader);
		}
		ClassLoader[] retVal = new ClassLoader[loaders.size()];
		retVal = (ClassLoader[]) loaders.toArray(retVal);
		logger.exiting(SIMPLE_CLASS_NAME, METHOD, retVal);
		return retVal;
	}

	public static URL[] getPluginClassPaths() {
		final String METHOD = "getPluginClassPaths";
		logger.entering(SIMPLE_CLASS_NAME, METHOD);
		List<URL> urls = new LinkedList<>();
		ClassLoader[] loaders = getPluginClassLoaders();
		for (ClassLoader loader : loaders) {
			if (loader instanceof URLClassLoader) {
				URL[] ucp = ((URLClassLoader) loader).getURLs();
				for (URL url : ucp) {
					logger.finer(url.toString());
					urls.add(url);
				}
			} else {
				logger.warning("Skipping non-URLClassLoader: "
						+ loader.toString());
			}
		}
		URL[] retVal = new URL[urls.size()];
		retVal = (URL[]) urls.toArray(retVal);
		logger.exiting(SIMPLE_CLASS_NAME, METHOD, retVal);
		return retVal;
	}

	public static CMExtensionPoint getExtensionPoint(String extPt) {
		final String METHOD = "getExtensionPoint";
		logger.entering(SIMPLE_CLASS_NAME, METHOD, extPt);
		CMExtensionPoint retVal = getPluginRegistry().getExtensionPoint(extPt);
		logger.exiting(SIMPLE_CLASS_NAME, METHOD, retVal);
		return retVal;
	}

	public static CMExtension[] getExtensions(String extPt) {
		final String METHOD = "getExtensions";
		logger.entering(SIMPLE_CLASS_NAME, METHOD, extPt);
		CMExtension[] retVal = getExtensionPoint(extPt).getExtensions();
		logger.exiting(SIMPLE_CLASS_NAME, METHOD, retVal);
		return retVal;
	}

	public static CMExtension getExtension(String extPt, String extId) {
		final String METHOD = "getExtensions";
		logger.entering(SIMPLE_CLASS_NAME, METHOD, extPt + "/" + extId);
		CMExtension retVal = getPluginRegistry().getExtension(extPt, extId);
		logger.exiting(SIMPLE_CLASS_NAME, METHOD, retVal);
		return retVal;
	}

	private CMPlatformUtils() {
	}

}
