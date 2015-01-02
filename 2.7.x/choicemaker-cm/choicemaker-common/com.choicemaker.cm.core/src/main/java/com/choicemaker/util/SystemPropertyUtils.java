package com.choicemaker.util;

import java.util.Properties;

/**
 * Some missing manifest constants and set/unset methods.
 * 
 * @author rphall
 */
public class SystemPropertyUtils {

	/** Java Runtime Environment version */
	public static final String JAVA_VERSION = "java.version";

	/** Java Runtime Environment vendor */
	public static final String JAVA_VENDOR = "java.vendor";

	/** Java vendor URL */
	public static final String JAVA_VENDOR_URL = "java.vendor.url";

	/** Java installation directory */
	public static final String JAVA_HOME = "java.home";

	/** Java Virtual Machine specification version */
	public static final String JAVA_VM_SPECIFICATION_VERSION =
		"java.vm.specification.version";

	/** Java Virtual Machine specification vendor */
	public static final String JAVA_VM_SPECIFICATION_VENDOR =
		"java.vm.specification.vendor";

	/** Java Virtual Machine specification name */
	public static final String JAVA_VM_SPECIFICATION_NAME =
		"java.vm.specification.name";

	/** Java Virtual Machine implementation version */
	public static final String JAVA_VM_VERSION = "java.vm.version";

	/** Java Virtual Machine implementation vendor */
	public static final String JAVA_VM_VENDOR = "java.vm.vendor";

	/** Java Virtual Machine implementation name */
	public static final String JAVA_VM_NAME = "java.vm.name";

	/** Java Runtime Environment specification version */
	public static final String JAVA_SPECIFICATION_VERSION =
		"java.specification.version";

	/** Java Runtime Environment specification vendor */
	public static final String JAVA_SPECIFICATION_VENDOR =
		"java.specification.vendor";

	/** Java Runtime Environment specification name */
	public static final String JAVA_SPECIFICATION_NAME =
		"java.specification.name";

	/** Java class format version number */
	public static final String JAVA_CLASS_VERSION = "java.class.version";

	/** Java class path */
	public static final String JAVA_CLASS_PATH = "java.class.path";

	/** List of paths to search when loading libraries */
	public static final String JAVA_LIBRARY_PATH = "java.library.path";

	/** Default temp file path */
	public static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

	/** Name of JIT compiler to use */
	public static final String JAVA_COMPILER = "java.compiler";

	/** Path of extension directory or directories */
	public static final String JAVA_EXT_DIRS = "java.ext.dirs";

	/** Operating system name */
	public static final String OS_NAME = "os.name";

	/** Operating system architecture */
	public static final String OS_ARCH = "os.arch";

	/** Operating system version */
	public static final String OS_VERSION = "os.version";

	/** File separator ("/" on UNIX) */
	public static final String FILE_SEPARATOR = "file.separator";

	/** Path separator (":" on UNIX) */
	public static final String PATH_SEPARATOR = "path.separator";

	/** Line separator ("n" on UNIX) */
	public static final String LINE_SEPARATOR = "line.separator";

	/** User's account name */
	public static final String USER_NAME = "user.name";

	/** User's home directory */
	public static final String USER_HOME = "user.home";

	/** User's current working directory */
	public static final String USER_DIR = "user.dir";

	private SystemPropertyUtils() {
	}

	/**
	 * Conditionally sets a System property only if it hasn't already been set.
	 * Same as invoking
	 * 
	 * <pre>
	 * setSystemProperty(false, key, value)
	 * </pre>
	 */
	public static void setPropertyIfMissing(String key, String value) {
		setProperty(false, key, value);
	}

	/**
	 * Sets a System property.
	 * 
	 * @param force if true, forces the property to be set, even if the property has
	 *        already been set to another value. If false, the property is set
	 *        only if it hasn't already been set.
	 */
	public static void setProperty(boolean force, String key, String value) {
		boolean doSet = force || System.getProperty(key) == null;
		if (doSet) {
			System.setProperty(key, value);
		}
	}

	public static void unsetProperty(String key) {
		Properties p = System.getProperties();
		p.remove(key);
	}

}
