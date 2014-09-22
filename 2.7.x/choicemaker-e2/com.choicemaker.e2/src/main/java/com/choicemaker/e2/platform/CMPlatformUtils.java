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

	private static final Logger log = Logger.getLogger(CMPlatformUtils.class.getName());
	private static final String SIMPLE_CLASS_NAME = CMPlatformUtils.class
			.getSimpleName();

	public static CMPluginRegistry getPluginRegistry() {
		// final String METHOD = "getPluginRegistry";
		// log.entering(SIMPLE_CLASS_NAME, METHOD);
		CMPlatform platform = InstallablePlatform.getInstance();
		CMPluginRegistry retVal = platform.getPluginRegistry();
		// log.exiting(SIMPLE_CLASS_NAME, METHOD, retVal);
		return retVal;
	}

	public static CMPluginDescriptor[] getPluginDescriptors() {
		final String METHOD = "getPluginDecriptors";
		log.entering(SIMPLE_CLASS_NAME, METHOD);
		CMPluginRegistry registry = getPluginRegistry();
		CMPluginDescriptor[] retVal = registry.getPluginDescriptors();
		log.exiting(SIMPLE_CLASS_NAME, METHOD, retVal);
		return retVal;
	}

	public static ClassLoader[] getPluginClassLoaders() {
		final String METHOD = "getPluginClassLoaders";
		log.entering(SIMPLE_CLASS_NAME, METHOD);
		List<ClassLoader> loaders = new LinkedList<>();
		CMPluginDescriptor[] plugins = getPluginDescriptors();
		for (CMPluginDescriptor plugin : plugins) {
			log.fine("Adding classloader for: " + plugin.getUniqueIdentifier());
			ClassLoader loader = plugin.getPluginClassLoader();
			loaders.add(loader);
		}
		ClassLoader[] retVal = new ClassLoader[loaders.size()];
		retVal = (ClassLoader[]) loaders.toArray(retVal);
		log.exiting(SIMPLE_CLASS_NAME, METHOD, retVal);
		return retVal;
	}

	public static URL[] getPluginClassPaths() {
		final String METHOD = "getPluginClassPaths";
		log.entering(SIMPLE_CLASS_NAME, METHOD);
		List<URL> urls = new LinkedList<>();
		ClassLoader[] loaders = getPluginClassLoaders();
		for (ClassLoader loader : loaders) {
			if (loader instanceof URLClassLoader) {
				URL[] ucp = ((URLClassLoader) loader).getURLs();
				for (URL url : ucp) {
					log.fine(url.toString());
					urls.add(url);
				}
			} else {
				log.warning("Skipping non-URLClassLoader: " + loader.toString());
			}
		}
		URL[] retVal = new URL[urls.size()];
		retVal = (URL[]) urls.toArray(retVal);
		log.exiting(SIMPLE_CLASS_NAME, METHOD, retVal);
		return retVal;
	}

	public static CMExtensionPoint getExtensionPoint(String extPt) {
		final String METHOD = "getExtensionPoint";
		log.entering(SIMPLE_CLASS_NAME, METHOD, extPt);
		CMExtensionPoint retVal = getPluginRegistry().getExtensionPoint(extPt);
		log.exiting(SIMPLE_CLASS_NAME, METHOD, retVal);
		return retVal;
	}

	public static CMExtension[] getExtensions(String extPt) {
		final String METHOD = "getExtensions";
		log.entering(SIMPLE_CLASS_NAME, METHOD, extPt);
		CMExtension[] retVal = getExtensionPoint(extPt).getExtensions();
		log.exiting(SIMPLE_CLASS_NAME, METHOD, retVal);
		return retVal;
	}

	public static CMExtension getExtension(String extPt, String extId) {
		final String METHOD = "getExtensions";
		log.entering(SIMPLE_CLASS_NAME, METHOD, extPt + "/" + extId );
		CMExtension retVal = getPluginRegistry().getExtension(extPt,extId);
		log.exiting(SIMPLE_CLASS_NAME, METHOD, retVal);
		return retVal;
	}

	private CMPlatformUtils() {
	}

}
