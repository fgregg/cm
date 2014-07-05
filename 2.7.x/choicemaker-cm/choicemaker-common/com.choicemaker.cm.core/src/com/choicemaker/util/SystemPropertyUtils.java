package com.choicemaker.util;

import java.util.Properties;

public class SystemPropertyUtils {

	private SystemPropertyUtils() {
	}

	/**
	 * Sets a System property only if it hasn't already been
	 * set. Same as invoking
	 * <pre>
	 * setSystemProperty(false, key, value)
	 * </pre>
	 */
	public static void setProperty(String key, String value) {
		setProperty(false, key, value);
	}

	/**
	 * Sets a System property.
	 * @param if true, forces the property to be set, even if the
	 * property has already been set to another value. If false,
	 * the property is set only if it hasn't already been set.
	 */
	public static void setProperty(boolean force, String key, String value) {
		boolean doSet = force || System.getProperty(key) == null;
		if (doSet) {
			System.setProperty(key, value);
		}
//		for (Iterator i=System.getProperties().keySet().iterator(); i.hasNext(); ) {
//			Object k = i.next();
//			Object v = System.getProperty((String) k);
//			System.out.println(k + ": " + v);
//		}
	}

	public static void unsetProperty(String key) {
		Properties p = System.getProperties();
		p.remove(key);
	}

}
