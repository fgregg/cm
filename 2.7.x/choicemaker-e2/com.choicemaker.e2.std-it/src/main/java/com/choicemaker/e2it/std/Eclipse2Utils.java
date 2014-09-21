package com.choicemaker.e2it.std;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class Eclipse2Utils {

	private Eclipse2Utils() {
	}

	/** Gets the System class loader */
	public static ClassLoader getSystemClassLoader() {
		ClassLoader retVal = ClassLoader.getSystemClassLoader();
		ClassLoader parent = retVal.getParent();
		while (parent != null) {
			retVal = parent;
				System.out.println("getSystemBootLoader() " + ": " + retVal.toString());
			parent = retVal.getParent();
		}

		System.out.println("getSystemBootLoader() returning "
				+ ": " + retVal.toString());
		return retVal;
	}

	/**
	 * Loads a specified class from a specified JAR file using a class loader
	 * whose parent is the System boot loader.
	 *
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	public static Class<?> loadClassFromJar(String fqcn, URL bootJar)
			throws ClassNotFoundException, IOException {
		if (fqcn == null || fqcn.trim().isEmpty()) {
			throw new IllegalArgumentException("null or blank class name");
		}
		if (bootJar == null) {
			throw new IllegalArgumentException("null URL");
		}

		Class<?> retVal = null;
		ClassLoader parent = getSystemClassLoader();
		URL[] jars = new URL[] { bootJar };
		URLClassLoader cl = null;
		try {
			cl = new URLClassLoader(jars, parent);
			retVal = cl.loadClass(fqcn);
		} finally {
			if (cl != null) {
				// Don't close the loader here, because it may be
				// needed later; for example, it is often used to find
				// methods of the specified class.
				// cl.close();
				cl = null;
			}
		}
		assert cl == null;

		if (retVal == null) {
			String msg =
				"org.eclipse.core.boot.BootLoader not found in " + bootJar;
			throw new ClassNotFoundException(msg);
		}
		return retVal;
	}

	/**
	 * Provide a detailed message about a method
	 */
	public static String methodDetails(Class<?> targetClass, String methodName,
			Class<?>[] argTypes) {

		String targetClassName = "null";
		String clName = "null";
		if (targetClass != null) {
			targetClassName = targetClass.getName();
			ClassLoader cl = targetClass.getClassLoader();
			if (cl != null) {
				clName = cl.toString();
			}
		}

		String strArgTypes = "null";
		if (argTypes != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("[ ");
			for (Class<?> c : argTypes) {
				String strArgType = "null";
				if (c != null) {
					strArgType = c.getName();
				}
				sb.append(strArgType).append(" ");
			}
			sb.append("]");
		}

		String msg =
			"Method '" + methodName + "' with argument types '" + strArgTypes
					+ "' in the class '" + targetClassName
					+ "' which was loaded by '" + clName + "'";
		return msg;
	}

	public static Method getMethod(Class<?> c, String name, Class<?>[] argTypes) {
		if (c == null) {
			throw new IllegalArgumentException("null class");
		}
		Method retVal = null;
		try {
			retVal = c.getDeclaredMethod(name, argTypes);
		} catch (Exception x) {
			String msg =
				"Exception while finding method: "
						+ methodDetails(c, name, argTypes) + ": "
						+ x.toString();
			throw new Error(msg);
		}
		if (retVal == null) {
			String msg =
				"Unable to find method: " + methodDetails(c, name, argTypes);
			throw new Error(msg);
		}
		return retVal;
	}

	public static boolean isEclipseRunning(Class<?> bootLoader)
			throws Exception {
		if (bootLoader == null) {
			throw new IllegalArgumentException("null launcher");
		}
		Boolean retVal = null;
		try {
			Method m = getMethod(bootLoader, "isRunning", null);
			retVal = (Boolean) m.invoke(null, (Object[]) null);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | ClassCastException e) {
			throw new Error("Unexpected (i.e. a design error): " + e.toString());
		}
		assert retVal != null;
		return retVal.booleanValue();
	}

	public static void shutdownEclipse(Class<?> bootLoader)
			throws Exception {
		if (bootLoader == null) {
			throw new IllegalArgumentException("null boot loader");
		}
		try {
			Method m = getMethod(bootLoader, "shutdown", null);
			m.invoke(null, (Object[]) null);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | ClassCastException e) {
			throw new Error("Unexpected (i.e. a design error): " + e.toString());
		}
	}

}
