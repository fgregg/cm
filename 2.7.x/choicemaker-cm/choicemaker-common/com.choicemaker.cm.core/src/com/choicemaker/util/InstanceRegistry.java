package com.choicemaker.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * A global registry for any kind of Java instances. This registry was
 * originally written as a helper class for testing Eclipse executable
 * extensions, for which there may be no other way of finding executables
 * instantiations, but it may also be used to register instances of any
 * plain-old Java class.
 * 
 * @author rphall
 * 
 */
public class InstanceRegistry {

	private static Logger logger = Logger.getLogger(InstanceRegistry.class
			.getName());

	private static InstanceRegistry instance = new InstanceRegistry();

	public static InstanceRegistry getInstance() {
//		System.out.println("InstanceRegistry class: "
//				+ InstanceRegistry.class.toString());
//		System.out.println("InstanceRegistry classLoader: "
//				+ InstanceRegistry.class.getClassLoader().toString());
		return instance;
	}

	/**
	 * A synchronized map of keys to instances. In a typical usage, a key
	 * consists of a base tag, such as a plugin id or class name, and a suffix
	 * that distinguishes Java instances that are registered with same base tag.
	 */
	private Hashtable runnables = new Hashtable();

	private InstanceRegistry() {
	}

	public synchronized String registerInstance(String baseTag, Object o) {
		if (baseTag == null || baseTag.isEmpty()) {
			throw new IllegalArgumentException("null or blank baseTag");
		}
		if (o == null) {
			throw new IllegalArgumentException("null instance");
		}
		String key = createKey(baseTag);
		Object existing = runnables.put(key, o);
		assert existing == null;
		return key;
	}

	public synchronized Object findRegisteredInstance(String key) {
		return runnables.get(key);
	}

	public synchronized Map findRegisteredInstances(String baseTag) {
		Map retVal = new HashMap();
		for (Iterator it = runnables.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			if (((String) entry.getKey()).startsWith(baseTag)) {
				retVal.put(entry.getKey(), entry.getValue());
			}
		}
		return Collections.unmodifiableMap(retVal);
	}

	/**
	 * Returns the remainder of a tag after the initial String starting with
	 * <code>baseTag</code>.
	 * 
	 * @param tag
	 *            may be null
	 * @param baseTag
	 *            must not be null or empty.
	 * @return may be null if the tag is null or the tag does not start with the
	 *         baseTag or the tag does not have a suffix after the baseTag.
	 */
	public String getSuffix(String tag, String baseTag) {
		if (baseTag == null || baseTag.isEmpty()) {
			throw new IllegalArgumentException("null or blank baseTag");
		}
		String retVal = null;
		if (tag != null && tag.startsWith(baseTag)) {
			int index = baseTag.length();
			if (index < tag.length()) {
				retVal = tag.substring(index);
			}
		}
		return retVal;
	}

	public String createKey(String baseTag) {
		if (baseTag == null || baseTag.isEmpty()) {
			throw new IllegalArgumentException("null or blank baseTag");
		}
		Integer suffix = findUnusedSuffix(baseTag);
		return baseTag + suffix;
	}

	public Integer findUnusedSuffix(String baseTag) {
		assert baseTag != null && !baseTag.isEmpty();
		int minSuffix = Integer.MAX_VALUE;
		int maxSuffix = Integer.MIN_VALUE;
		Set keys = runnables.keySet();
		Set existingSuffices = new HashSet();
		for (Iterator it = keys.iterator(); it.hasNext();) {
			String key = (String) it.next();
			String s = getSuffix(key, baseTag);
			if (s != null) {
				try {
					int i = Integer.parseInt(s);
					existingSuffices.add(Integer.valueOf(i));
					if (i > maxSuffix) {
						i = Math.max(maxSuffix + 1, maxSuffix);
					}
					if (i < minSuffix) {
						i = Math.min(minSuffix - 1, minSuffix);
					}
				} catch (NumberFormatException x) {
					logger.debug("Not an integer suffix: " + s);
				}
			}
		}
		Integer retVal =
			findUnusedSuffix(existingSuffices, minSuffix, maxSuffix);
		return retVal;
	}

	/**
	 * Choose a suffix greater than any seen; or if that is not possible, choose
	 * a suffix less than any seen; or if that is not possible, choose a hole
	 * between the max and min that were seen. It is wildly implausible, but if
	 * no suffix fits these three possibilities, throw an IllegalState exception
	 * indicating that all suffices have been exhausted.
	 */
	private Integer findUnusedSuffix(Set existingSuffices, int minSuffix,
			int maxSuffix) {
		assert existingSuffices != null;

		Integer suffix = null;
		if (maxSuffix < Integer.MAX_VALUE) {
			suffix = Integer.valueOf(maxSuffix);
		} else if (minSuffix > Integer.MIN_VALUE) {
			suffix = Integer.valueOf(minSuffix);
		} else {
			assert existingSuffices
					.contains(Integer.valueOf(Integer.MAX_VALUE));
			assert existingSuffices
					.contains(Integer.valueOf(Integer.MIN_VALUE));
			for (int i = Integer.MIN_VALUE; i < Integer.MAX_VALUE; i++) {
				Integer candidate = Integer.valueOf(i);
				if (!existingSuffices.contains(candidate)) {
					suffix = candidate;
					break;
				}
			}
		}
		if (suffix == null) {
			throw new IllegalStateException("all suffices exhausted");
		}

		return suffix;
	}

}
