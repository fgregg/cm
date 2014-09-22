/*
 * Copyright (c) 2001, 2009 ChoiceMaker Technologies, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License
 * v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ChoiceMaker Technologies, Inc. - initial API and implementation
 */
package com.choicemaker.cm.matching.gen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.choicemaker.cm.core.ChoiceMakerExtensionPoint;
import com.choicemaker.cm.core.util.ConvUtils;
import com.choicemaker.e2.CMConfigurationElement;
import com.choicemaker.e2.CMExtension;
import com.choicemaker.e2.platform.CMPlatformUtils;

/**
 * Collection of collections.
 * A member collection may, for example, contain generic first names.
 *
 * Maps work similar to sets. The module is loaded through the class
 * <code>com.choicemaker.cm.xmlconf.XmlMapsInitializer</code>. The actual sets
 * are defined as child elements, as shown in the example:
 * <pre>
 &LTmodule class="com.choicemaker.cm.xmlconf.XmlMapsInitializer"&GT
	&LTfileMap name="firstNameFrequency10" file="etc/data/firstNameFrequency.txt"
		keyType="String" valueType="int"/&GT
	&LT!-- more maps --&GT
 &LT/module&GT
   </pre>
 * This loads the contents of the specified file, one token per line, into memory.
 * E.g., the file firstNameFrequency.txt may look like this:
<pre>
JIM
10
JACQUES
1
...
</pre>
 *
 * Based on this, we can then use an expression like
 * <code>Maps.lookupInt("firstNameFrequency", q.first_name)</code> in a schema
 * expression or in a clue/rule.
 *
 * All primitive Java types and String are supported as key and value types.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:04 $
 */
public final class Maps {

	private static Map maps = new HashMap();

	private Maps() { }

	static {
		initRegisteredMaps();
	}

	/**
	 * Adds a map to the collection of maps.
	 *
	 * @param   name  The name of the collection.
	 * @param   map  The map to be added.
	 *
	 */
	public static void addMap(String name, Map map) {
		maps.put(name, map);
	}

	/**
	 * Returns the map named by <code>name</code>.
	 *
	 * @param name the name of the collection
	 * @return the collection named by name
	 */
	public static Map getMap(String name) {
		Map m = (Map) maps.get(name);
		if (m instanceof LazyMap) {
			((LazyMap)m).init();
		}
		return (Map) maps.get(name);
	}

	/**
	 * Returns a Collection containing the names of the Maps contained herein.
	 * @return a Collection of the names of all registered maps
	 */
	public static Collection getMapNames() {
		return maps.keySet();
	}

	/**
	 * Retrieves the Map named by <code>name</code> and returns the
	 * key's mapped entry in the Map.
	 *
	 * @param name the name of the Map in which to perform the lookup
	 * @param key the key to lookup
	 * @return the value keyed by <code>key</code>
	 * @throws IllegalArgumentException if no Map named <code>name</code> is registered.
	 */
	public static Object lookup(String name, Object key) {
		Map map = (Map) maps.get(name);
		if (map == null) {
			throw new IllegalArgumentException("There is no map named " + name + " registered");
		} else {
			return map.get(key);
		}
	}

	/**
	 * Convenience method that performs a lookup as in <code>lookup(name, key)</code>
	 * and casts the return value to a String.
	 *
	 * @param name the name of the Map in which to perform the lookup
	 * @param key the key to lookup in the Map
	 * @return the value keyed by <code>key</code>
	 * @throws ClassCastException if the keyed value is not a String
	 * @throws IllegalArgumentException if no Map named <code>name</code> is registered.
	 */
	public static String lookupString(String name, Object key) {
		return (String)lookup(name, key);
	}

	/**
	 * Convenience method that performs a lookup as in <code>lookup(name, key)</code>
	 * and converts the returned value to an int.  This method should only be called
	 * on Maps whose value types are defined as "int".  If the specified map does not
	 * have a value for the specified key, this method returns <code>Integer.MIN_VALUE</code>.
	 *
	 * @param name the name of the Map in which to perform the lookup
	 * @param key the key to lookup in the Map
	 * @return the value keyed by <code>key</code>
	 * @throws ClassCastException if the keyed value is not an Integer object
	 * @throws IllegalArgumentException if no Map named <code>name</code> is registered.
	 */
	public static int lookupInt(String name, Object key) {
		Object o = lookup(name, key);
		if (o != null) {
			return ((Integer) o).intValue();
		} else {
			return Integer.MIN_VALUE;
		}
	}

	/**
	 * FOR CHOICEMAKER INTERNAL USE ONLY.
	 */
	public static Map readFileMap(String fileName, String keyType, String valueType) throws IOException {
		InputStream fis = new FileInputStream(new File(fileName).getAbsoluteFile());
		Map m = readFileMap(fis, keyType, valueType);
		fis.close();
		return m;
	}

	/**
	 * FOR CHOICEMAKER INTERNAL USE ONLY.
	 */
	public static Map readFileMap(InputStream stream, String keyType, String valueType) throws IOException {
		Map m = new HashMap();
		InputStreamReader reader = new InputStreamReader(stream);
		BufferedReader in = new BufferedReader(reader);
		while (in.ready()) {
			String key = in.readLine();
			String value = in.readLine();
			if (key != null && value != null) {
				m.put(ConvUtils.convertString2Object(key, keyType),
					ConvUtils.convertString2Object(value, valueType));
			}
		}
		reader.close();
		in.close();
		return m;
	}

	/**
	 * FOR CHOICEMAKER INTERNAL USE ONLY.
	 */
	public static Map readSingleLineMap(String fileName, String keyType, String valueType) throws IOException {
		InputStream fis = new FileInputStream(new File(fileName).getAbsoluteFile());
		Map m = readSingleLineMap(fis, keyType, valueType);
		fis.close();
		return m;
	}

	/**
	 * FOR CHOICEMAKER INTERNAL USE ONLY.
	 */
	public static Map readSingleLineMap(InputStream stream, String keyType, String valueType) throws IOException {
		Map m = new HashMap();
		InputStreamReader reader = new InputStreamReader(stream);
		BufferedReader in = new BufferedReader(reader);
		while (in.ready()) {
			String line = in.readLine().trim();
			int index = line.indexOf("//");
			if (index >= 0) {
				line = line.substring(0, index).trim();
			}

			if (line.length() == 0) {
				continue;
			}

			index = line.indexOf(':');
			if (index < 0) {
				throw new IOException("Problem parsing line:\n\t" + line);
			}

			String value = line.substring(0, index).trim();
			Object v = ConvUtils.convertString2Object(value, valueType);
			String keys = line.substring(index + 1);
			StringTokenizer toks = new StringTokenizer(keys, ",");
			while (toks.hasMoreTokens()) {
				Object k = ConvUtils.convertString2Object(toks.nextToken().trim(), keyType);
				m.put(k, v);
			}
		}
		reader.close();
		in.close();
		return m;
	}

	/**
	 * Called by GenPlugin to load the registered sets.
	 */
	static void initRegisteredMaps() {
		CMExtension[] extensions =
			CMPlatformUtils
					.getExtensions(ChoiceMakerExtensionPoint.CM_MATCHING_GEN_MAP);
		for (int i = 0; i < extensions.length; i++) {
			CMExtension ext = extensions[i];
			URL pUrl = ext.getDeclaringPluginDescriptor().getInstallURL();
			CMConfigurationElement[] els = ext.getConfigurationElements();
			for (int j = 0; j < els.length; j++) {
				CMConfigurationElement el = els[j];

				String name = el.getAttribute("name");
				String file = el.getAttribute("file");
				String keyType = "String";
				String valueType = "String";

				// 2014-04-24 rphall: Commented out unused local variables.
//				boolean lazy = true;
//				if (el.getAttribute("lazy") != null) {
//					lazy = Boolean.getBoolean(el.getAttribute("lazy"));
//				}
//
//				boolean reload = true;
//				if (el.getAttribute("reload") != null) {
//					reload = Boolean.getBoolean(el.getAttribute("reload"));
//				}

				if (el.getAttribute("keyType") != null) {
					keyType = el.getAttribute("keyType");
				}

				if (el.getAttribute("valueType") != null) {
					valueType = el.getAttribute("valueType");
				}

				boolean singleLine = false;
				if (el.getAttribute("singleLine") != null) {
					singleLine = "true".equals(el.getAttribute("singleLine"));
				}

				try {
					URL rUrl = new URL(pUrl, file);

					LazyMap m = new LazyMap(name, rUrl, keyType, valueType, singleLine);
					addMap(name, m);
				} catch (MalformedURLException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

}

class LazyMap implements Map {

	private String name;
	private URL url;
	private String keyType;
	private String valueType;
	private boolean singleLine;

	private Map store;

	public LazyMap(String name, URL url, String keyType, String valueType, boolean singleLine) {
		this.name = name;
		this.url = url;
		this.keyType = keyType;
		this.valueType = valueType;
		this.store = null;
		this.singleLine = singleLine;
	}

	protected synchronized void init() {
		if (store == null) {
			try {
				if (singleLine) {
					store = Maps.readSingleLineMap(url.openStream(), keyType, valueType);
				} else {
					store = Maps.readFileMap(url.openStream(), keyType, valueType);
				}
				Maps.addMap(name, store);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public int size() {
		init();
		return store.size();
	}

	public boolean isEmpty() {
		init();
		return store.isEmpty();
	}

	public boolean containsKey(Object key) {
		init();
		return store.containsKey(key);
	}

	public boolean containsValue(Object value) {
		init();
		return store.containsValue(value);
	}

	public Object get(Object key) {
		init();
		return store.get(key);
	}

	public Object put(Object key, Object value) {
		init();
		return store.put(key, value);
	}

	public Object remove(Object key) {
		init();
		return store.remove(key);
	}

	public void putAll(Map t) {
		init();
		store.putAll(t);
	}

	public void clear() {
		init();
		store.clear();
	}

	public Set keySet() {
		init();
		return store.keySet();
	}

	public Collection values() {
		init();
		return store.values();
	}

	public Set entrySet() {
		init();
		return store.entrySet();
	}

}

