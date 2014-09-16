package com.choicemaker.eclipse2.core.runtime;

import java.net.URL;

import com.choicemaker.eclipse2.core.boot.CMPlatformRunnable;

public interface CMPlatform {

//	/**
//	 * The unique identifier constant (value "<code>org.eclipse.core.runtime</code>")
//	 * of the Core Runtime (pseudo-) plug-in.
//	 */
//	public static final String PI_RUNTIME = "org.eclipse.core.runtime"; //$NON-NLS-1$
//	public static final String PLUGIN_BASE_DIR = "META-INF/plugins";
//	public static final String PLUGINS_FILE = PLUGIN_BASE_DIR + "/plugins.xml";
//	public static final String PLUGIN_DESCRIPTOR_FILE = "plugin.xml";
//	public static final String FRAGMENT_DESCRIPTOR_FILE = "fragment.xml";
//
//	/** 
//	 * The simple identifier constant (value "<code>applications</code>") of
//	 * the extension point of the Core Runtime plug-in where plug-ins declare
//	 * the existence of runnable applications. A plug-in may define any
//	 * number of applications; however, the platform is only capable
//	 * of running one application at a time.
//	 * 
//	 * @see org.eclipse.core.boot.BootLoader#run
//	 */
//	public static final String PT_APPLICATIONS = "applications"; //$NON-NLS-1$
//	/** 
//	 * Status code constant (value 1) indicating a problem in a plug-in
//	 * manifest (<code>plugin.xml</code>) file.
//	 */
//	public static final int PARSE_PROBLEM = 1;
//	/**
//	 * Status code constant (value 2) indicating an error occurred while running a plug-in.
//	 */
//	public static final int PLUGIN_ERROR = 2;

	CMPluginRegistry getPluginRegistry();

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
	CMPlatformRunnable loaderGetRunnable(String applicationName);

	String getPluginDirectory(String id, String version);

	URL getPluginDescriptorUrl(String id, String version, String descriptorFile);

}