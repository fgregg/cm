package com.choicemaker.e2it.std;

import java.io.IOException;
import java.net.URL;

/**
 * Proxy to the Eclipse 2 <code>org.eclipse.core.boot.BootLoader</code> class.
 * This proxy loads the BootLoader class dynamically, from a JAR file that is
 * specified when an instance is created, so that the BootLoader class does not
 * have to be statically linked to clients that use it. </p>
 * 
 * @see org.eclipse.core.boot.BootLoader
 */
public class Eclipse2BootLoader {

	public static final String PI_BOOT = "org.eclipse.core.boot";//$NON-NLS-1$
	public static final String FQCN_BOOTLOADER =
		"org.eclipse.core.boot.BootLoader";//$NON-NLS-1$

	private final URL bootJar;
	private final Class<?> bootLoaderClass;
	
	/**
	 * If the Eclipse 2 platform is started by a Launcher, this constructor
	 * <em>must</em> be used to create a BootLoader that can control it.
	 * (If the {@link #Eclipse2BootLoader(URL) URL-based constructor} is used,
	 * the boot loader will probably be created using a different class loader
	 * than the launcher and therefore the resulting boot loader will represent
	 * a different class than the one that controls the platform.)
	 * @param launcher
	 */
	public Eclipse2BootLoader(Eclipse2Launcher launcher) {
		if (launcher == null) {
			throw new IllegalArgumentException("null launcher");
		}
		this.bootJar = launcher.getBootJar();
		this.bootLoaderClass = launcher.getBootLoader();
	}

	/**
	 * Use this constructor only if a launcher will not be used with the Eclipse
	 * 2 platform.
	 * @param bootJar
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public Eclipse2BootLoader(URL bootJar) throws ClassNotFoundException,
			IOException {
		if (bootJar == null) {
			throw new IllegalArgumentException("null boot JAR");
		}
		this.bootJar = bootJar;
		this.bootLoaderClass =
			Eclipse2Utils.loadClassFromJar(FQCN_BOOTLOADER, bootJar);
	}

	public URL getBootJar() {
		return bootJar;
	}

	public Class<?> getBootLoaderClass() {
		return bootLoaderClass;
	}

	/**
	 * Returns whether the given location (typically a directory in the local
	 * file system) contains the saved data for a platform. The saved data for
	 * the platform is recognizable by the presence of a special platform
	 * metadata subdirectory; however, this metadata directory is not usually
	 * created unless there is some reason to do so (for example, so that an
	 * active plug-in can save its state).
	 * 
	 * @return <code>true</code> if the location contains the saved data for a
	 *         platform, and <code>false</code> otherwise
	 */
	public boolean containsSavedPlatform(String location) {
		throw new Error("not yet implemented");
	}

	/**
	 * Returns the command line args provided to the platform when it was first
	 * run. Note that individual platform runnables may be provided with
	 * different arguments if they are being run individually rather than with
	 * <code>Platform.run()</code>.
	 * 
	 * @return the command line used to start the platform
	 */
	public String[] getCommandLineArgs() {
		throw new Error("not yet implemented");
	}

	/**
	 * Returns the current platform configuration.
	 * 
	 * @return a non-null instance of
	 *         org.eclipse.core.boot.IPlatformConfiguration that is used in
	 *         current instance of platform
	 * @since 2.0
	 */
	public Object getCurrentPlatformConfiguration() {
		throw new Error("not yet implemented");
	}

	/**
	 * Returns URL at which the Platform runtime executables and libraries are
	 * installed. The returned value is distinct from the location of any given
	 * platform's data.
	 * 
	 * @return the URL indicating where the platform runtime is installed.
	 */
	public URL getInstallURL() {
		throw new Error("not yet implemented");
	}

	/**
	 * Returns the string name of the current locale for use in finding files
	 * whose path starts with <code>$nl$</code>.
	 * 
	 * @return the string name of the current locale
	 */
	public String getNL() {
		throw new Error("not yet implemented");
	}

	/**
	 * Returns the string name of the current operating system for use in
	 * finding files whose path starts with <code>$os$</code>.
	 * <code>OS_UNKNOWN</code> is returned if the operating system cannot be
	 * determined. The value may indicate one of the operating systems known to
	 * the platform (as specified in <code>knownOSValues</code>) or a
	 * user-defined string if the operating system name is specified on the
	 * command line.
	 * 
	 * @return the string name of the current operating system
	 * @see #knownOSValues
	 * 
	 */
	public String getOS() {
		throw new Error("not yet implemented");
	}

	/**
	 * Returns the string name of the current system architecture. The value is
	 * a user-defined string if the architecture is specified on the command
	 * line, otherwise it is the value returned by
	 * <code>java.lang.System.getProperty("os.arch")</code>.
	 * 
	 * @return the string name of the current system architecture
	 * @since 2.0
	 */
	public String getOSArch() {
		throw new Error("not yet implemented");
	}

	/**
	 * Returns a platform configuration object, optionally initialized with
	 * previously saved configuration information.
	 * 
	 * @param url
	 *            location of previously save configuration information. If
	 *            <code>null</code> is specified, an empty configuration object
	 *            is returned
	 * @return a non-null instance of
	 *         org.eclipse.core.boot.IPlatformConfiguration that is used in
	 *         current instance of platform
	 * @since 2.0
	 */
	public Object getPlatformConfiguration(URL url) throws IOException {
		throw new Error("not yet implemented");
	}

	/**
	 * Returns the complete plugin path defined by the file at the given
	 * location. If the given location is <code>null</code> or does not indicate
	 * a valid plug-in path definition file, <code>null</code> is returned.
	 * 
	 * @return the complete set of URLs which locate plug-ins
	 */
	public URL[] getPluginPath(URL pluginPathLocation) {
		throw new Error("not yet implemented");
	}

	/**
	 * Instantiates and returns an instance of the named application's runnable
	 * entry point. <code>null</code> is returned if the runnable cannot be
	 * found.
	 * 
	 * @param applicationName
	 *            the fully qualified name of an extension installed in the
	 *            platform's <code>applications</code> extension point (i.e.,
	 *            <code>org.eclipse.core.applications</code>).
	 * @return a non-null instance of org.eclipse.core.boot.IPlatformRunnable
	 * @exception Exception
	 *                if there is a problem instantiating the specified runnable
	 */
	public Object getRunnable(String applicationName) throws Exception {
		throw new Error("not yet implemented");
	}

	/**
	 * Instantiates and returns an instance of the named class. The class must
	 * implement <code>IPlatformRunnable</code>. If the class implements
	 * <code>IExecutableExtension</code>, the created instance is initialized
	 * with the given arguments. <code>null</code> is returned if the runnable
	 * cannot be found.
	 * 
	 * @param pluginId
	 *            the unique identifier of the plug-in containing the given
	 *            class
	 * @param className
	 *            the fully qualified name of the class to instantiate
	 * @param args
	 *            the initialization arguments passed to the new instance
	 * @return a non-null instance of org.eclipse.core.boot.IPlatformRunnable,
	 *         or <code>null</code> if the runnable cannot be found
	 * @exception Exception
	 *                if there is a problem instantiating the specified runnable
	 */
	public Object getRunnable(String pluginId, String className, Object args)
			throws Exception {
		throw new Error("not yet implemented");
	}

	/**
	 * Returns the string name of the current window system for use in finding
	 * files whose path starts with <code>$ws$</code>. <code>null</code> is
	 * returned if the window system cannot be determined.
	 * 
	 * @return the string name of the current window system or <code>null</code>
	 */
	public String getWS() {
		throw new Error("not yet implemented");
	}

	/**
	 * Returns a list of known system architectures.
	 * <p>
	 * Note that this list is not authoritative; there may be legal values not
	 * included in this list. Indeed, the value returned by
	 * <code>getOSArch</code> may not be in this list. Also, this list may
	 * change over time as Eclipse comes to run on more operating environments.
	 * </p>
	 * 
	 * @return the list of system architectures known to the system
	 * @see #getOSArch
	 * @since 2.0
	 */
	public String[] knownOSArchValues() {
		throw new Error("not yet implemented");
	}

	/**
	 * Returns a list of known operating system names.
	 * <p>
	 * Note that this list is not authoritative; there may be legal values not
	 * included in this list. Indeed, the value returned by <code>getOS</code>
	 * may not be in this list. Also, this list may change over time as Eclipse
	 * comes to run on more operating environments.
	 * </p>
	 * 
	 * @return the list of operating systems known to the system
	 * @see #getOS
	 * @since 2.0
	 */
	public String[] knownOSValues() {
		throw new Error("not yet implemented");
	}

	/**
	 * Returns a list of known windowing system names.
	 * <p>
	 * Note that this list is not authoritative; there may be legal values not
	 * included in this list. Indeed, the value returned by <code>getWS</code>
	 * may not be in this list. Also, this list may change over time as Eclipse
	 * comes to run on more operating environments.
	 * </p>
	 * 
	 * @return the list of window systems known to the system
	 * @see #getWS
	 * @since 2.0
	 */
	public String[] knownWSValues() {
		throw new Error("not yet implemented");
	}

	/**
	 * Returns <code>true</code> if the platform is currently running in debug
	 * mode. The platform is run in debug mode using the "-debug" command line
	 * argument.
	 * 
	 * @return whether or not the platform is running in debug mode
	 */
	public boolean inDebugMode() {
		throw new Error("not yet implemented");
	}

	/**
	 * Returns <code>true</code> if the platform is currently running in
	 * development mode. That is, if special procedures are to be taken when
	 * defining plug-in class paths. The platform is run in development mode
	 * using the "-dev" command line argument.
	 * 
	 * @return whether or not the platform is running in development mode
	 */
	public boolean inDevelopmentMode() {
		throw new Error("not yet implemented");
	}

	/**
	 * Returns whether the platform is running. The <code>startup</code> method
	 * starts the platform running; the <code>shutdown</code> method stops it.
	 * 
	 * @return <code>true</code> if the platform is running, and
	 *         <code>false</code> otherwise
	 * @see #startup
	 * @see #shutdown
	 */
	public boolean isRunning() {
		throw new Error("not yet implemented");
	}

	/**
	 * Launches the Platform to run a single application. This convenince method
	 * starts up the Platform, runs the indicated application, and then shuts
	 * down the Platform. The Platform must not be running already.
	 * 
	 * @param applicationName
	 *            The fully qualified name of an extension installed in the
	 *            Platform plug-in's <code>applications</code> extension-point
	 *            (i.e., <code>org.eclipse.core.runtime.applications</code>).
	 * @param pluginPathLocation
	 *            the URL of the plug-in path; this is where the Platform is to
	 *            find the code for plug-ins
	 * @param location
	 *            the location (usually a string path in the local file file
	 *            system) for the saved platform state
	 * @param args
	 *            the array of command-line style argments which are passed to
	 *            the Platform on initialization. The arguments which are
	 *            consumed by the Platform's initialization are removed from the
	 *            arg list. This modified arg list is the return value of this
	 *            method.
	 * @return the list of <code>args</code> which were supplied but not
	 *         consumed by this method.
	 * @return the result, or <code>null</code> if none
	 * @exception Exception
	 *                if anything goes wrong
	 * @see #startup
	 */
	public Object run(String applicationName, URL pluginPathLocation,
			String location, String[] args) throws Exception {
		throw new Error("not yet implemented");
	}

	/**
	 * Launches the Platform to run a single application. This convenince method
	 * starts up the Platform, runs the indicated application, and then shuts
	 * down the Platform. The Platform must not be running already.
	 * 
	 * @param applicationName
	 *            The fully qualified name of an extension installed in the
	 *            Platform plug-in's <code>applications</code> extension-point
	 *            (i.e., <code>org.eclipse.core.runtime.applications</code>).
	 * @param pluginPathLocation
	 *            the URL of the plug-in path; this is where the Platform is to
	 *            find the code for plug-ins
	 * @param location
	 *            the location (usually a string path in the local file file
	 *            system) for the saved platform state
	 * @param args
	 *            the array of command-line style argments which are passed to
	 *            the Platform on initialization. The arguments which are
	 *            consumed by the Platform's initialization are removed from the
	 *            arg list. This modified arg list is the return value of this
	 *            method.
	 * @param handler
	 *            an optional handler invoked by the launched application at the
	 *            point the application considers itself initialized. A typical
	 *            use for the handler would be to take down any splash screen
	 *            that was displayed by the caller of this method.
	 * @return the list of <code>args</code> which were supplied but not
	 *         consumed by this method.
	 * @return the result, or <code>null</code> if none
	 * @exception Exception
	 *                if anything goes wrong
	 * @see #startup
	 */
	public Object run(String applicationName, URL pluginPathLocation,
			String location, String[] args, Runnable handler) throws Exception {
		throw new Error("not yet implemented");
	}

	/**
	 * Shuts down the Platform. The Platform must be running. In the process,
	 * each active plug-in is told to shutdown via <code>Plugin.shutdown</code>.
	 * <p>
	 * Note that the state of the Platform is not automatically saved before
	 * shutting down.
	 * </p>
	 * <p>
	 * On return, the Platform will no longer be running (but could be
	 * re-launched with another call to <code>startup</code>). Any objects
	 * handed out by running Platform, including Platform runnables obtained via
	 * <code>getRunnable</code>, will be permanently invalid. The effects of
	 * attempting to invoke methods on invalid objects is undefined.
	 * </p>
	 * 
	 * @exception Exception
	 *                if there were problems shutting down
	 */
	public void shutdown() throws Exception {
		throw new Error("not yet implemented");
	}

	/**
	 * Launches the Eclipse Platform. The Platform must not be running.
	 * <p>
	 * The location of the started Platform is defined as follows:
	 * <ul>
	 * <li>If the <code>location</code> argument is specified, that value is
	 * used.
	 * <li>If <code>location</code> is <code>null</code> but <code>args</code>
	 * contains a <code>-data&ltlocation&gt</code> pair, then the given value is
	 * used.
	 * <li>If neither is specified, <code>System.getProperty("user.dir")</code>
	 * is used.
	 * </ul>
	 * The plug-in path of the started Platform is defined as follows:
	 * <ul>
	 * <li>If the <code>pluginPathLocation</code> argument is specified, that
	 * value is tried.
	 * <li>If <code>pluginPathLocation</code> is <code>null</code> but
	 * <code>args</code> contains a <code>-plugins &ltlocation&gt</code> pair,
	 * then the given value is tried.
	 * <li>If neither value is specified or a given location does not exist, the
	 * Platform's location is searched.
	 * <li>Finally, the default plug-in path is used. This value identifies the
	 * plug-ins in the Platform's install location.
	 * </ul>
	 * 
	 * @param pluginPathLocation
	 *            the URL of the plug-in path; this is where the Platform is to
	 *            find the code for plug-ins
	 * @param location
	 *            the location (usually a string path in the local file file
	 *            system) for the saved Platform state
	 * @param args
	 *            the array of command-line style argments which are passed to
	 *            the platform on initialization. The arguments which are
	 *            consumed by the Platform's initialization are removed from the
	 *            arg list. This modified arg list is the return value of this
	 *            method.
	 * @return the list of <code>args</code> which were supplied but not
	 *         consumed by this method.
	 * @exception Exception
	 *                if there are problems starting the platform
	 */
	public String[] startup(URL pluginPathLocation, String location,
			String[] args) throws Exception {
		throw new Error("not yet implemented");
	}

	/**
	 * Launches the Eclipse Platform. The Platform must not be running.
	 * <p>
	 * The location of the started Platform is defined as follows:
	 * <ul>
	 * <li>If the <code>location</code> argument is specified, that value is
	 * used.
	 * <li>If <code>location</code> is <code>null</code> but <code>args</code>
	 * contains a <code>-data &ltlocation&gt</code> pair, then the given value
	 * is used.
	 * <li>If neither is specified, <code>System.getProperty("user.dir")</code>
	 * is used.
	 * </ul>
	 * The plug-in path of the started Platform is defined as follows:
	 * <ul>
	 * <li>If the <code>pluginPathLocation</code> argument is specified, that
	 * value is tried.
	 * <li>If <code>pluginPathLocation</code> is <code>null</code> but
	 * <code>args</code> contains a <code>-plugins &ltlocation&gt</code> pair,
	 * then the given value is tried.
	 * <li>If neither value is specified or a given location does not exist, the
	 * Platform's location is searched.
	 * <li>Finally, the default plug-in path is used. This value identifies the
	 * plug-ins in the Platform's install location.
	 * </ul>
	 * 
	 * @param pluginPathLocation
	 *            the URL of the plug-in path; this is where the Platform is to
	 *            find the code for plug-ins
	 * @param location
	 *            the location (usually a string path in the local file file
	 *            system) for the saved Platform state
	 * @param args
	 *            the array of command-line style argments which are passed to
	 *            the platform on initialization. The arguments which are
	 *            consumed by the Platform's initialization are removed from the
	 *            arg list. This modified arg list is the return value of this
	 *            method.
	 * @param handler
	 *            an optional handler invoked by the launched application at the
	 *            point the application considers itself initialized. A typical
	 *            use for the handler would be to take down any splash screen
	 *            that was displayed by the caller of this method.
	 * @return the list of <code>args</code> which were supplied but not
	 *         consumed by this method.
	 * @exception Exception
	 *                if there are problems starting the platform
	 */
	public String[] startup(URL pluginPathLocation, String location,
			String[] args, Runnable handler) throws Exception {
		throw new Error("not yet implemented");
	}

}
