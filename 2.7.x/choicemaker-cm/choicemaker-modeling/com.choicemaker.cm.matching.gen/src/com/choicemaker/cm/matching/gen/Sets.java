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

import org.eclipse.core.runtime.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Collection of collections.
 * A member collection may, for example, contain generic first names.
 *
 * Sets provide sets of objects.
 * The module is loaded through the class
 * <code>com.choicemaker.cm.xmlconf.XmlSetsInitializer</code>.
 * The actual sets are defined as child elements, as shown in the example:
 * <pre>
&LTmodule class="com.choicemaker.cm.xmlconf.XmlSetsInitializer"&GT
	&LTfileSet name="genericFirstNames" file="etc/data/genericFirstNames.txt"/&GT
	... &LT!-- more sets --&GT
&LT/module&GT
   </pre>
 *
 * This loads the contents of the specified file, one element per line, into
 * memory. E.g., the file genericFirstNames.txt may look like this:
 * <pre>
BABY
UNKNOWN
...
   </pre>
 *
 * Based on this, we can then use an expression like
 * <code>!Sets.includes("genericFirstNames", first_name)</code> in the validity
 * predicate of the <code>first_name</code> field or in a clue/rule.

 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/27 22:22:59 $
 */
public final class Sets {

	private static Map colls = new HashMap();

	private Sets() { }

	static {
		initRegisteredSets();
	}

	/**
	 * Answers whether a specified collection includes a specific value.
	 * Value rather than reference equality is used.
	 *
	 * @param   name  The name of the collection to be searched.
	 * @param   value  The value to be searched for.
	 * @return  whether the collection includes the specified value.
	 */
	public static boolean includes(String name, Object value) {
		boolean retVal = false;
		if (name != null && value!=null) {
			Collection c = (Collection) colls.get(name);
			if (c !=null) {
				retVal = c.contains(value);
			} else {
				String msg = "missing Set '" + name + "'";
				throw new NullPointerException(msg);
			}
		}
		return retVal;
	}

	/**
	 * Adds a collection to the collection of collections.
	 *
	 * @param   name  The name of the collection.
	 * @param   coll  The collection to be added.
	 *
	 */
	public static void addCollection(String name, Collection coll) {
		colls.put(name, coll);
	}

	/**
	 * Returns the collection named by <code>name</code>.
	 *
	 * @param name the name of the Collection to return
	 * @return the Collection named by <code>name</code>
	 */
	public static Collection getCollection(String name) {
		Set s = (Set) colls.get(name);
		if (s instanceof LazySet) {
			((LazySet)s).init();
		}
		return (Set) colls.get(name);
	}

	/**
	 * Returns a collection of all the Collections contained herein.
	 *
	 * @return a collections of all registered Collections
	 */
	public static Collection getCollectionNames() {
		return colls.keySet();
	}

	/**
	 * FOR INTERNAL CHOICEMAKER USER ONLY.
	 *
	 * Reads a Set from the specified file.  Each line
	 * is an entry in the returned set.  No whitespace is removed, except for
	 * newlines and/or carriage returns.  No lines, not even empty lines, are skipped.
	 * Such cases, cause a zero-length String to be added to the returned Set.
	 *
	 * <b>Note:</b> the returned set is <i>not</i> added to the registered Collections.
	 *
	 * @param fileName the name of the file
	 * @return the Set built from the specified file
	 * @throws IOException if there is a problem reading the set
	 */
	public static Set readFileSet(String fileName)throws IOException {
		InputStream fis =
			new FileInputStream(new File(fileName).getAbsoluteFile());
		Set s = readFileSet(fis);
		fis.close();
		return s;
	}

	/**
	 * FOR INTERNAL CHOICEMAKER USER ONLY.
	 *
	 * Reads a Set from the specified InputStream.  Each line
	 * is an entry in the returned set.  No whitespace is removed, except for
	 * newlines and/or carriage returns.  No lines, not even empty lines, are skipped.
	 * Such cases, cause a zero-length String to be added to the returned Set.
	 *
	 * <b>Note:</b> the returned set is <i>not</i> added to the registered Collections.
	 *
	 * @param stream the input stream
	 * @return the Set
	 * @throws IOException if there is a problem reading the set
	 */
	public static Set readFileSet(InputStream stream) throws IOException {
		Set s = new HashSet();
		InputStreamReader reader = new InputStreamReader(stream);
		BufferedReader in = new BufferedReader(reader);
		while (in.ready()) {
			String ln = in.readLine();
			if (ln != null) {
				s.add(ln);
			}
		}
		reader.close();
		in.close();
		return s;
	}

	/**
	 * FOR INTERNAL CHOICEMAKER USE ONLY.
	 *
	 * Reads a Set from the specified file.  Each line may represent multiple entries in the Set.
	 * An example line follows:
	 * <p>
	 * WILLIAM : BILL, WILL, WILLY, WM
	 * </p>
	 *
	 * This format was introduced to serve as a common format for Sets, Maps, and Relations.
	 * Currently, the returned Set will include all entries on the line, separated by colons or commas.
	 * Entries will have the whitespace removed from the beginning and end.
	 * If an entry contains a comma or colon, or the user desired to have a zero-length
	 * String in the set, then he must use the standard readFileSet() methods.
	 *
	 * This is the same as having a normal file set as follows
	 * <br>
	 * WILLIAM<br>
	 * BILL<br>
	 * WILL<br>
	 * WILLY<br>
	 * WM<br>
	 *
	 * @param fileName the name of the file containing the set.
	 * @return a Set with the appropriate entries.
	 * @throws IOException if there is a problem reading the set
	 */
	public static Set readSingleLineSet(String fileName) throws IOException {
		InputStream fis = new FileInputStream(new File(fileName).getAbsoluteFile());
		Set s = readSingleLineSet(fis);
		fis.close();
		return s;
	}

	/**
	 * FOR INTERNAL CHOICEMAKER USE ONLY.
	 *
	 * Reads a Set from the specified file.  Each line may represent multiple entries in the Set.
	 * An example line follows:
	 * <p>
	 * WILLIAM : BILL, WILL, WILLY, WM
	 * </p>
	 *
	 * This format was introduced to serve as a common format for Sets, Maps, and Relations.
	 * Currently, the returned Set will include all entries on the line, separated by colons or commas.
	 * Entries will have the whitespace removed from the beginning and end.
	 * If an entry contains a comma or colon, or the user desired to have a zero-length
	 * String in the set, then he must use the standard readFileSet() methods.
	 *
	 * This is the same as having a normal file set as follows
	 * <br>
	 * WILLIAM<br>
	 * BILL<br>
	 * WILL<br>
	 * WILLY<br>
	 * WM<br>
	 *
	 * @param stream the stream from which to read the set
	 * @return a Set
	 * @throws IOException if there is a problem reading the set
	 */
	public static Set readSingleLineSet(InputStream stream) throws IOException {
		Set s = new HashSet();
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
			s.add(value);

			String keys = line.substring(index + 1);
			StringTokenizer toks = new StringTokenizer(keys, ",");
			while (toks.hasMoreTokens()) {
				s.add(toks.nextToken().trim());
			}
		}
		reader.close();
		in.close();
		return s;
	}


	/**
	 * FOR CHOICEMAKER INTERNAL USE ONLY.
	 *
	 * Called by GenPlugin to load the registered sets.
	 */
	static void initRegisteredSets() {
		IExtensionPoint pt = Platform.getPluginRegistry().getExtensionPoint("com.choicemaker.cm.matching.gen.set");
		IExtension[] extensions = pt.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			IExtension ext = extensions[i];
			URL pUrl = ext.getDeclaringPluginDescriptor().getInstallURL();
			IConfigurationElement[] els = ext.getConfigurationElements();
			for (int j = 0; j < els.length; j++) {
				IConfigurationElement el = els[j];

				String name = el.getAttribute("name");
				String file = el.getAttribute("file");
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

				boolean singleLine = false;
				if (el.getAttribute("singleLine") != null) {
					singleLine = "true".equals(el.getAttribute("singleLine"));
				}

				try {
					URL rUrl = new URL(pUrl, file);
					LazySet s = new LazySet(name, rUrl, singleLine);
					addCollection(name, s);
				} catch (MalformedURLException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

}

class LazySet implements Set {

	private String name;
	private URL url;
	private boolean singleLine;

	private Set store;

	public LazySet(String name, URL url) {
		this(name, url, false);
	}

	public LazySet(String name, URL url, boolean singleLine) {
		this.name = name;
		this.url = url;
		this.singleLine = singleLine;
		this.store = null;
	}

	protected synchronized void init() {
		if (store == null) {
			try {
				if (singleLine) {
					store = Sets.readSingleLineSet(url.openStream());
				} else {
					store = Sets.readFileSet(url.openStream());
				}
				Sets.addCollection(name, store);
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

	public boolean contains(Object o) {
		init();
		return store.contains(o);
	}

	public Iterator iterator() {
		init();
		return store.iterator();
	}

	public Object[] toArray() {
		init();
		return store.toArray();
	}

	public Object[] toArray(Object[] a) {
		init();
		return store.toArray(a);
	}

	public boolean add(Object o) {
		init();
		return store.add(o);
	}

	public boolean remove(Object o) {
		init();
		return store.remove(o);
	}

	public boolean containsAll(Collection c) {
		init();
		return store.containsAll(c);
	}

	public boolean addAll(Collection c) {
		init();
		return store.addAll(c);
	}

	public boolean retainAll(Collection c) {
		init();
		return store.retainAll(c);
	}

	public boolean removeAll(Collection c) {
		init();
		return store.removeAll(c);
	}

	public void clear() {
		init();
		store.clear();
	}

}

