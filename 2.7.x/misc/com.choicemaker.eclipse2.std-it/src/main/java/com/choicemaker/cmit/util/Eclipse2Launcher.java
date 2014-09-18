package com.choicemaker.cmit.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

/**
 * Proxy to the Eclipse 2 <code>org.eclipse.core.launcher.Main</code> class.
 * This proxy loads the <code>Main</code> class dynamically, from a JAR file
 * that is specified when an instance is created, so that the <code>Main</code>
 * class does not have to be statically linked to clients that use it.
 * </p>
 *
 * @see org.eclipse.core.launcher.Main
 */
public class Eclipse2Launcher {

	public static final String PI_STARTUP = "org.eclipse.core.launcher"; //$NON-NLS-1$
	public static final String FQCN_STARTUP = "org.eclipse.core.launcher.Main"; //$NON-NLS-1$

	private final URL startupJar;
	private final Class<?> startupClass;
	private final Object startupInstance;
	private final URL bootJar;
	private final Class<?> bootLoaderClass;

	public Eclipse2Launcher(URL startupJar, URL bootJar) throws Exception {
		if (startupJar == null) {
			throw new IllegalArgumentException("null startup JAR");
		}
		if (bootJar == null) {
			throw new IllegalArgumentException("null boot JAR");
		}
		this.startupJar = startupJar;
		this.startupClass = Eclipse2Utils.loadClassFromJar(FQCN_STARTUP,startupJar);
		this.startupInstance = this.startupClass.newInstance();
		this.bootJar = bootJar;
		this.bootLoaderClass = initializeBootLoader();
	}

	public URL getStartupJar() {
		return startupJar;
	}

	public Class<?> getStartupClass() {
		return startupClass;
	}

	public Object getStartupInstance() {
		return startupInstance;
	}

	public URL getBootJar() {
		return bootJar;
	}

	public Class<?> getBootLoader() {
		return bootLoaderClass;
	}

	/**
	 * Creates and returns a platform <code>BootLoader</code> class which can be used
	 * to start up and run the platform.
	 *
	 * @return the new boot loader
	 * @param path
	 *            search path for the BootLoader
	 */
	protected Class<?> initializeBootLoader() throws Exception {
		Class<?>[] argTypes = new Class<?>[] { URL[].class };
		Method m = Eclipse2Utils.getMethod(this.getStartupClass(), "getBootLoader", argTypes);
		Class<?> retVal = null;
		try {
			URL[] urls = new URL[] { getBootJar() };
			Object[] params = new Object[]{ urls };
			retVal = (Class<?>) m.invoke(this.getStartupInstance(), params);
		} catch (IllegalAccessException | IllegalArgumentException | ClassCastException
				e) {
			throw new Error("Unexpected (i.e. design error: " + e.toString());
		} catch (InvocationTargetException e) {
			Throwable e2 = e.getCause();
			if (e2 instanceof Exception) {
				throw (Exception) e2;
			} else {
				throw e;
			}
		}
		return retVal;
	}

	/**
	 * Runs the application to be launched.
	 *
	 * @return the return value from the launched application
	 * @param args
	 *            the arguments to pass to the application
	 * @exception thrown
	 *                if a problem occurs during launching
	 */
	public Object run(String[] args) throws Exception {
		Class<?>[] argTypes = new Class<?>[] { String[].class };
		Method m = Eclipse2Utils.getMethod(this.getStartupClass(), "run", argTypes);
		Object retVal = null;
		try {
			Object[] params = new Object[]{ args };
			retVal = m.invoke(this.getStartupInstance(), params);
		} catch (IllegalAccessException | IllegalArgumentException
				e) {
			throw new Error("Unexpected (i.e. design error: " + e.toString());
		} catch (InvocationTargetException e) {
			Throwable e2 = e.getCause();
			if (e2 instanceof Exception) {
				throw (Exception) e2;
			} else {
				throw e;
			}
		}
		return retVal;
	}

	/**
	 * Runs the platform with the given arguments. The arguments must identify
	 * an application to run (e.g.,
	 * <code>-application com.example.application</code>). After running the
	 * application <code>System.exit(N)</code> is executed. The value of N is
	 * derived from the value returned from running the application. If the
	 * application's return value is an <code>Integer</code>, N is this value.
	 * In all other cases, N = 0.
	 * <p>
	 * Clients wishing to run the platform without a following
	 * <code>System.exit</code> call should use <code>run()</code>.
	 *
	 * @see #run
	 *
	 * @param args
	 *            the command line arguments
	 */
	public void main(String[] args) throws Exception {
		Class<?>[] argTypes = new Class<?>[] { String[].class };
		Method m = Eclipse2Utils.getMethod(this.getStartupClass(), "main", argTypes);
		try {
			Object[] params = new Object[] { args };
			m.invoke(null, params);
		} catch (IllegalAccessException | IllegalArgumentException
				e) {
			throw new Error("Unexpected (i.e. design error: " + e.toString());
		} catch (InvocationTargetException e) {
			Throwable e2 = e.getCause();
			if (e2 instanceof Exception) {
				throw (Exception) e2;
			} else {
				throw e;
			}
		}
	}

	/**
	 * Runs this launcher with the arguments specified in the given string.
	 *
	 * @param argString
	 *            the arguments string
	 * @exception Exception
	 *                thrown if a problem occurs during launching
	 */
	public void main(String argString) throws Exception {
		Class<?>[] argTypes = new Class<?>[] { String.class };
		Method m = Eclipse2Utils.getMethod(this.getStartupClass(), "main", argTypes);
		try {
			m.invoke(null, argString);
		} catch (IllegalAccessException | IllegalArgumentException
				e) {
			throw new Error("Unexpected (i.e. design error: " + e.toString());
		} catch (InvocationTargetException e) {
			Throwable e2 = e.getCause();
			if (e2 instanceof Exception) {
				throw (Exception) e2;
			} else {
				throw e;
			}
		}
	}

}
