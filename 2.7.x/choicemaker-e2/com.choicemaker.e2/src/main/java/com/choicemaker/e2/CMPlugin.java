package com.choicemaker.e2;

public interface CMPlugin {

//	/**
//	 * The name of the file (value <code>"preferences.ini"</code>) in a
//	 * plug-in's (read-only) directory that, when present, contains values that
//	 * override the normal default values for this plug-in's preferences.
//	 * <p>
//	 * The format of the file is as per <code>java.io.Properties</code> where
//	 * the keys are property names and values are strings.
//	 * </p>
//	 * 
//	 * @since 2.0
//	 */
//	public static final String PREFERENCES_DEFAULT_OVERRIDE_BASE_NAME =
//		"preferences"; //$NON-NLS-1$
//	public static final String PREFERENCES_DEFAULT_OVERRIDE_FILE_NAME =
//		PREFERENCES_DEFAULT_OVERRIDE_BASE_NAME + ".ini"; //$NON-NLS-1$

	//	/**
	//	 * Returns a URL for the given path.  Returns <code>null</code> if the URL
	//	 * could not be computed or created.
	//	 * 
	//	 * @param file path relative to plug-in installation location 
	//	 * @return a URL for the given path or <code>null</code>
	//	 */
	//	public final URL find(CMPath path) {
	//		return getDescriptor().find(path);
	//	}
	//	/**
	//	 * Returns a URL for the given path.  Returns <code>null</code> if the URL
	//	 * could not be computed or created.
	//	 * 
	//	 * @param path file path relative to plug-in installation location
	//	 * @param override map of override substitution arguments to be used for
	//	 * any $arg$ path elements. The map keys correspond to the substitution
	//	 * arguments (eg. "$nl$" or "$os$"). The resulting
	//	 * values must be of type java.lang.String. If the map is <code>null</code>,
	//	 * or does not contain the required substitution argument, the default
	//	 * is used.
	//	 * @return a URL for the given path or <code>null</code>
	//	 */
	//	public final URL find(CMPath path, Map override) {
	//		return getDescriptor().find(path, override);
	//	}
	//	private String getFileFromURL(URL target) {
	//		String protocol = target.getProtocol();
	//		if (protocol.equals(PlatformURLHandler.FILE))
	//			return target.getFile();
	//		if (protocol.equals(PlatformURLHandler.JAR)) {
	//			// strip off the jar separator at the end of the url then do a recursive call
	//			// to interpret the sub URL.
	//			String file = target.getFile();
	//			file = file.substring(0, file.length() - PlatformURLHandler.JAR_SEPARATOR.length());
	//			try {
	//				return getFileFromURL(PlatformURLFactory.createURL(file));
	//			} catch (MalformedURLException e) {
	//			}
	//		}
	//		return null;
	//	}
	/**
	 * Returns the plug-in descriptor for this plug-in runtime object.
	 *
	 * @return the plug-in descriptor for this plug-in runtime object
	 */
	CMPluginDescriptor getDescriptor();

	//	/**
	//	 * Returns the log for this plug-in.  If no such log exists, one is created.
	//	 *
	//	 * @return the log for this plug-in
	//	 */
	//	public final ILog getLog() {
	//		return InternalPlatform.getLog(this);
	//	}
	//	/**
	//	 * Returns the location in the local file system of the 
	//	 * plug-in state area for this plug-in.
	//	 * If the plug-in state area did not exist prior to this call,
	//	 * it is created.
	//	 * <p>
	//	 * The plug-in state area is a file directory within the
	//	 * platform's metadata area where a plug-in is free to create files.
	//	 * The content and structure of this area is defined by the plug-in,
	//	 * and the particular plug-in is solely responsible for any files
	//	 * it puts there. It is recommended for plug-in preference settings and 
	//	 * other configuration parameters.
	//	 * </p>
	//	 *
	//	 * @return a local file system path
	//	 */
	//	public final CMPath getStateLocation() {
	//		return InternalPlatform.getPluginStateLocation(descriptor, true);
	//	}

	/**
	 * Returns whether this plug-in is in debug mode.
	 * By default plug-ins are not in debug mode.  A plug-in can put itself
	 * into debug mode or the user can set an execution option to do so.
	 *
	 * @return whether this plug-in is in debug mode
	 */
	boolean isDebugging();

	//	/**
	//	 * Returns an input stream for the specified file. The file path
	//	 * must be specified relative this the plug-in's installation location.
	//	 *
	//	 * @param file path relative to plug-in installation location
	//	 * @return an input stream
	//	 * @see #openStream(CMPath,boolean)
	//	 */
	//	public final InputStream openStream(CMPath file) throws IOException {
	//		return openStream(file, false);
	//	}
	//	/**
	//	 * Returns an input stream for the specified file. The file path
	//	 * must be specified relative to this plug-in's installation location.
	//	 * Optionally, the platform searches for the correct localized version
	//	 * of the specified file using the users current locale, and Java
	//	 * naming convention for localized resource files (locale suffix appended 
	//	 * to the specified file extension).
	//	 * <p>
	//	 * The caller must close the returned stream when done.
	//	 * </p>
	//	 *
	//	 * @param file path relative to plug-in installation location
	//	 * @param localized <code>true</code> for the localized version
	//	 *   of the file, and <code>false</code> for the file exactly
	//	 *   as specified
	//	 * @return an input stream
	//	 */
	//	public final InputStream openStream(CMPath file, boolean localized) throws IOException {
	//		URL target = PlatformURLFactory.createURL(getDescriptor().getInstallURL() + file.toString());
	//		return target.openStream();
	//	}
	/**
	 * Sets whether this plug-in is in debug mode.
	 * By default plug-ins are not in debug mode.  A plug-in can put itself
	 * into debug mode or the user can set a debug option to do so.
	 *
	 * @param value whether or not this plugi-in is in debug mode
	 */
	void setDebugging(boolean value);

	/**
	 * Shuts down this plug-in and discards all plug-in state.
	 * <p>
	 * This method should be re-implemented in subclasses that need to do something
	 * when the plug-in is shut down.  Implementors should call the inherited method
	 * to ensure that any system requirements can be met.
	 * </p>
	 * <p>
	 * Plug-in shutdown code should be robust. In particular, this method
	 * should always make an effort to shut down the plug-in. Furthermore,
	 * the code should not assume that the plug-in was started successfully,
	 * as this method will be invoked in the event of a failure during startup.
	 * </p>
	 * <p>
	 * Note 1: If a plug-in has been started, this method will be automatically
	 * invoked by the platform when the platform is shut down.
	 * </p>
	 * <p>
	 * Note 2: This method is intended to perform simple termination
	 * of the plug-in environment. The platform may terminate invocations
	 * that do not complete in a timely fashion.
	 * </p>
	 * <b>Clients must never explicitly call this method.</b>
	 *
	 * @exception E2Exception if this method fails to shut down
	 *   this plug-in
	 */
	void shutdown() throws E2Exception;

	/**
	 * Starts up this plug-in.
	 * <p>
	 * This method should be overridden in subclasses that need to do something
	 * when this plug-in is started.  Implementors should call the inherited method
	 * to ensure that any system requirements can be met.
	 * </p>
	 * <p>
	 * If this method throws an exception, it is taken as an indication that
	 * plug-in initialization has failed; as a result, the plug-in will not
	 * be activated; moreover, the plug-in will be marked as disabled and 
	 * ineligible for activation for the duration.
	 * </p>
	 * <p>
	 * Plug-in startup code should be robust. In the event of a startup failure,
	 * the plug-in's <code>shutdown</code> method will be invoked automatically,
	 * in an attempt to close open files, etc.
	 * </p>
	 * <p>
	 * Note 1: This method is automatically invoked by the platform 
	 * the first time any code in the plug-in is executed.
	 * </p>
	 * <p>
	 * Note 2: This method is intended to perform simple initialization 
	 * of the plug-in environment. The platform may terminate initializers 
	 * that do not complete in a timely fashion.
	 * </p>
	 * <p>
	 * Note 3: The class loader typically has monitors acquired during invocation of this method.  It is 
	 * strongly recommended that this method avoid synchronized blocks or other thread locking mechanisms,
	 * as this would lead to deadlock vulnerability.
	 * </p>
	 * <b>Clients must never explicitly call this method.</b>
	 *
	 * @exception E2Exception if this plug-in did not start up properly
	 */
	void startup() throws E2Exception;

}