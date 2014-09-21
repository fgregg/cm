/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     ChoiceMaker Technologies, Inc. - alternate minimal runtime engine
 *******************************************************************************/
package com.choicemaker.e2.mbd.runtime;

import java.util.logging.Logger;

import com.choicemaker.e2.mbd.plugin.impl.DefaultPlugin;
import com.choicemaker.e2.mbd.plugin.impl.PluginDescriptor;
import com.choicemaker.e2.mbd.runtime.impl.Assert;
import com.choicemaker.e2.mbd.runtime.impl.Policy;

/**
 * The abstract superclass of all plug-in runtime class
 * implementations. A plug-in subclasses this class and overrides
 * the <code>startup</code> and <code>shutdown</code> methods 
 * in order to react to life cycle requests automatically issued
 * by the platform.
 * <p>
 * Conceptually, the plug-in runtime class represents the entire plug-in
 * rather than an implementation of any one particular extension the
 * plug-in declares. A plug-in is not required to explicitly
 * specify a plug-in runtime class; if none is specified, the plug-in
 * will be given a default plug-in runtime object that ignores all life 
 * cycle requests (it still provides access to the corresponding
 * plug-in descriptor).
 * </p>
 * <p>
 * In the case of more complex plug-ins, it may be desireable
 * to define a concrete subclass of <code>Plugin</code>.
 * However, just subclassing <code>Plugin</code> is not
 * sufficient. The name of the class must be explicitly configured
 * in the plug-in's manifest (<code>plugin.xml</code>) file
 * with the class attribute of the <code>&ltplugin&gt</code> element markup.
 * </p>
 * <p>
 * Instances of plug-in runtime classes are automatically created 
 * by the platform in the course of plug-in activation.
 * <b>Clients must never explicitly instantiate a plug-in runtime class</b>.
 * </p>
 * <p>
 * A typical implementation pattern for plug-in runtime classes is to
 * provide a static convenience method to gain access to a plug-in's
 * runtime object. This way, code in other parts of the plug-in
 * implementation without direct access to the plug-in runtime object
 * can easily obtain a reference to it, and thence to any plug-in-wide
 * resources recorded on it. An example follows:
 * <pre>
 *     package myplugin;
 *     public class MyPluginClass extends Plugin {
 *         private static MyPluginClass instance;
 *
 *         public static MyPluginClass getInstance() { return instance; }
 *
 *         public void MyPluginClass(IPluginDescriptor descriptor) {
 *             super(descriptor);
 *             instance = this;
 *             // ... other initialization
 *         }
 *         // ... other methods
 *     }
 * </pre>
 * In the above example, a call to <code>MyPluginClass.getInstance()</code>
 * will always return an initialized instance of <code>MyPluginClass</code>.
 * </p>
 * <p>
 * The static method <code>Platform.getPlugin()</code>
 * can be used to locate a plug-in's runtime object by name.
 * The extension initialization would contain the following code:
 * <pre>
 *     Plugin myPlugin = Platform.getPlugin("com.example.myplugin");
 * </pre>
 * 
 * Another typical implementation pattern for plug-in classes
 * is handling of any initialization files required by the plug-in.
 * Typically, each plug-in will ship one or more default files
 * as part of the plug-in install. The executing plug-in will
 * use the defaults on initial startup (or when explicitly requested
 * by the user), but will subsequently rewrite any modifications
 * to the default settings into one of the designated plug-in
 * working directory locations. An example of such an implementation
 * pattern is illustrated below:
 * <pre>
 * package myplugin;
 * public class MyPlugin extends Plugin {
 *
 *     private static final String INI = "myplugin.ini"; 
 *     private Properties myProperties = null;
 *
 *     public void startup() throws CoreException {
 *         try {
 *             InputStream input = null;
 *             // look for working properties.  If none, use shipped defaults 
 *             File file = getStateLocation().append(INI).toFile();
 *             if (!file.exists()) {			
 *                 URL base = getDescriptor().getInstallURL();
 *                 input = (PlatformURLFactory.createURL(base,INI)).openStream();
 *             } else 
 *                 input = new FileInputStream(file);
 * 
 *             // load properties 
 *             try {
 *                 myProperties = new Properties();
 *                 myProperties.load(input);
 *             } finally {
 *                 try {
 *                     input.close();
 *                 } catch (IOException e) {
 *                     // ignore failure on close
 *                 }
 *             }
 *         } catch (Exception e) {
 *             throw new CoreException(
 *                 new Status(IStatus.ERROR, getDescriptor().getUniqueIdentifier(),
 *                     0, "Problems starting plug-in myplugin", e));
 *         }
 *     }
 *
 *     public void shutdown() throws CoreException { 
 *         // save properties in plugin state location (r/w)
 *         try {
 *             FileOutputStream output = null; 
 *             try {
 *                 output = new FileOutputStream(getStateLocation().append(INI)); 
 *                 myProperties.store(output, null);
 *             } finally {
 *                 try {
 *                     output.close();
 *                 } catch (IOException e) {
 *                     // ignore failure on close
 *                 }
 *             }
 *         } catch (Exception e) {
 *             throw new CoreException(
 *                 new Status(IStatus.ERROR, getDescriptor().getUniqueIdentifier(),
 *                     0, "Problems shutting down plug-in myplugin", e));
 *         }
 *     }
 *
 *     public Properties getProperties() {	
 *         return myProperties; 
 *     }
 * }
 * </pre>
 * </p>
 */
public abstract class Plugin {
	
	private static final Logger logger = Logger.getLogger(Plugin.class.getName());

	/**
	 * The debug flag for this plug-in.  The flag is false by default.
	 * It can be set to true either by the plug-in itself or in the platform 
	 * debug options.
	 */
	private boolean debug = false;

	/** The plug-in descriptor.
	 */
	private IPluginDescriptor descriptor;

	/**
	 * The name of the file (value <code>"preferences.ini"</code>) in a
	 * plug-in's (read-only) directory that, when present, contains values that
	 * override the normal default values for this plug-in's preferences.
	 * <p>
	 * The format of the file is as per <code>java.io.Properties</code> where
	 * the keys are property names and values are strings.
	 * </p>
	 * 
	 * @since 2.0
	 */
	public static final String PREFERENCES_DEFAULT_OVERRIDE_BASE_NAME = "preferences"; //$NON-NLS-1$
	public static final String PREFERENCES_DEFAULT_OVERRIDE_FILE_NAME = PREFERENCES_DEFAULT_OVERRIDE_BASE_NAME + ".ini"; //$NON-NLS-1$

	/**
	 * Creates a new plug-in runtime object for the given plug-in descriptor.
	 * <p>
	 * Instances of plug-in runtime classes are automatically created 
	 * by the platform in the course of plug-in activation.
	 * <b>Clients must never explicitly instantiate a plug-in runtime class.</b>
	 * </p>
	 * <p>
	 * Note: The class loader typically has monitors acquired during invocation of this method.  It is 
	 * strongly recommended that this method avoid synchronized blocks or other thread locking mechanisms,
	 * as this would lead to deadlock vulnerability.
	 * </p>
	 *
	 * @param descriptor the plug-in descriptor
	 * @see #getDescriptor
	 */
	public Plugin(IPluginDescriptor descriptor) {
		Assert.isNotNull(descriptor);
		Assert.isTrue(!descriptor.isPluginActivated(), Policy.bind("plugin.deactivatedLoad", this.getClass().getName(), descriptor.getUniqueIdentifier() + " is not activated")); //$NON-NLS-1$ //$NON-NLS-2$
		String className = ((PluginDescriptor) descriptor).getPluginClass();
		if (this.getClass() == DefaultPlugin.class) {
//			Assert.isTrue(className == null || className.equals(""), Policy.bind("plugin.mismatchRuntime", descriptor.getUniqueIdentifier())); //$NON-NLS-1$ //$NON-NLS-2$
			if (className != null && !className.equals("")) {
				String msg = Policy.bind("plugin.mismatchRuntime", descriptor.getUniqueIdentifier());
				logger.warning(msg);
			}
		}
		else {
//			Assert.isTrue(this.getClass().getName().equals(className), ); //$NON-NLS-1$
			if (!this.getClass().getName().equals(className)) {
				String msg = Policy.bind("plugin.mismatchRuntime", descriptor.getUniqueIdentifier());
				logger.warning(msg);
			}
		}
		this.descriptor = descriptor;
		this.debug = false;
	}

	/**
	 * Returns the plug-in descriptor for this plug-in runtime object.
	 *
	 * @return the plug-in descriptor for this plug-in runtime object
	 */
	public final IPluginDescriptor getDescriptor() {
		return descriptor;
	}

	/**
	 * Initializes the default preferences settings for this plug-in.
	 * <p>
	 * This method is called sometime after the preference store for this
	 * plug-in is created. Default values are never stored in preference
	 * stores; they must be filled in each time. This method provides the
	 * opportunity to initialize the default values.
	 * </p>
	 * <p>
	 * The default implementation of this method does nothing. A subclass that needs
	 * to set default values for its preferences must reimplement this method.
	 * Default values set at a later point will override any default override
	 * settings supplied from outside the plug-in (product configuration or
	 * platform start up).
	 * </p>
	 * 
	 * @since 2.0
	 */
	protected void initializeDefaultPluginPreferences() {
		// default implementation of this method - spec'd to do nothing
	}

	/**
	 * Returns whether this plug-in is in debug mode.
	 * By default plug-ins are not in debug mode.  A plug-in can put itself
	 * into debug mode or the user can set an execution option to do so.
	 *
	 * @return whether this plug-in is in debug mode
	 */
	public boolean isDebugging() {
		return debug;
	}

	/**
	 * Sets whether this plug-in is in debug mode.
	 * By default plug-ins are not in debug mode.  A plug-in can put itself
	 * into debug mode or the user can set a debug option to do so.
	 *
	 * @param value whether or not this plugi-in is in debug mode
	 */
	public void setDebugging(boolean value) {
		debug = value;
	}

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
	 * @exception CoreException if this method fails to shut down
	 *   this plug-in
	 */
	public void shutdown() throws CoreException {
	}

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
	 * @exception CoreException if this plug-in did not start up properly
	 */
	public void startup() throws CoreException {
	}

	/**
	 * Returns a string representation of the plug-in, suitable 
	 * for debugging purposes only.
	 */
	public String toString() {
		return descriptor.toString();
	}

}
