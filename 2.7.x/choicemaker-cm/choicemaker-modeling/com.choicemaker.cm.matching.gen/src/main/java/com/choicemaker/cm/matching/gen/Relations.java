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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

import com.choicemaker.cm.core.util.ConvUtils;

/**
 * Relations provide support for generic relations.
 * <pre>
&LTmodule class="com.choicemaker.cm.xmlconf.XmlRelationsInitializer"&GT
	&LTfileRelation name="nicknames" file="etc/data/nicknames.txt"
		keyType="String" valueType="String" reflexive="true"/&GT
	&LT!-- more relations --&GT
&LT/module&GT
   </pre>
 *
 * This loads the contents of the specified file, one token per line, into memory.
 * E.g., the file nicknames.txt may look like this:
 * <pre>
JIM
JAMES
JIMMY
JAMES
JOE
JOSEPH
...
   </pre>
 *
 * Based on this, we can then use an expression like
 * <code>Relations.emptyIntersection("nicknames", q.first_name, m.first_name) </code>
 * in a schema expression or in a clue/rule.
 *
 * All primitive Java types and String are supported as key and value types.
 * The attribute reflexive governs whether the relation should be extended by
 * (x, x) for all x. It only has an effect if the key and value types are the
 * same.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:04 $
 */
public final class Relations {

	private static final Map relations = new HashMap();

	private Relations() { }

	static {
		initRegisteredRelations();
	}

	/**
	 * Add a relation named <code>name</code> to the list of
	 * registered relations.
	 *
	 * @param name the name of the relation
	 * @param r the relation itself
	 */
	public static void add(String name, Relation r) {
		relations.put(name, r);
	}

	/**
	 * Retrieves the relation named by <code>name</code>
	 * and then returns the mapped set keyed by <code>x</code>.
	 *
	 * @param name the name of the relation
	 * @param x the key
	 * @return x's mapped set in relation named by <code>name</code>
	 */
	public static Set get(String name, Object x) {
		return getRelation(name).get(x);
	}

	/**
	 * Returns the registered relation named by name.
	 *
	 * @param name the name of the relation
	 * @return the relation named by <code>name</code>
	 */
	public static Relation getRelation(String name) {
		Relation r = (Relation) relations.get(name);
		if (r instanceof LazyRelation) {
			((LazyRelation)r).init();
		}
		return (Relation) relations.get(name);
	}

	/**
	 * Returns a Collection of the names of the registered relations.
	 *
	 * @return a Collection of the names of the registered relations
	 */
	public static Collection getRelationNames() {
		return relations.keySet();
	}

	/**
	 * Returns true iff the mapped sets of x1 and x2 in the Relation named by <code>name</code>
	 * share no elements.
	 *
	 * @param name the name of the Relation
	 * @param x1 the first key
	 * @param x2 the second key
	 * @return true iff the x1's and x2's mapped sets share no elements
	 */
	public static boolean emptyIntersection(String name, Object x1, Object x2) {
		Relation r = (Relation) relations.get(name);
		if (r.isReflexive()) {
			// optimized implementation, avoid unnecessary object allocation
			Set s1 = r.get(x1, false);
			if (s1.contains(x2)) {
				return false;
			}
			Set s2 = r.get(x2, false);
			if (s2.contains(x1)) {
				return false;
			}
			return emptyIntersection(s1, s2);
		} else {
			return emptyIntersection(r.get(x1), r.get(x2));
		}

	}

	/**
	 * Returns true iff c1 and c2 share no elements.
	 */
	private static boolean emptyIntersection(Collection c1, Collection c2) {
		Iterator i1 = c1.iterator();
		while (i1.hasNext()) {
			if (c2.contains(i1.next())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * FOR CHOICEMAKER INTERNAL USE ONLY.
	 */
	public static Relation readFileRelation(String fileName, String keyType, String valueType, boolean reflexive) throws IOException {
		InputStream fis = new FileInputStream(new File(fileName).getAbsoluteFile());
		Relation r = readFileRelation(fis, keyType, valueType, reflexive);
		fis.close();
		return r;
	}

	/**
	 * FOR CHOICEMAKER INTERNAL USE ONLY.
	 */
	public static Relation readFileRelation(InputStream stream, String keyType, String valueType, boolean reflexive) throws IOException {
		Relation r = new Relation(reflexive);
		InputStreamReader reader = new InputStreamReader(stream);
		BufferedReader in = new BufferedReader(reader);
		while (in.ready()) {
			String key = in.readLine();
			String value = in.readLine();
			if (key != null && value != null) {
				r.add(
					ConvUtils.convertString2Object(key, keyType),
					ConvUtils.convertString2Object(value, valueType));
			}
		}
		in.close();
		reader.close();
		return r;
	}

	/**
	 * Called by GenPlugin to init the registered relations.
	 */
	static void initRegisteredRelations() {
		IExtensionPoint pt = Platform.getPluginRegistry().getExtensionPoint("com.choicemaker.cm.matching.gen.relation");
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
//				boolean reload = true;
//				if (el.getAttribute("reload") != null) {
//					reload = Boolean.getBoolean(el.getAttribute("reload"));
//				}
				String keyType = "String";
				if (el.getAttribute("keyType") != null) {
					keyType = el.getAttribute("keyType");
				}
				String valueType = "String";
				if (el.getAttribute("valueType") != null) {
					valueType = el.getAttribute("valueType");
				}
				boolean reflexive = true;
				if (el.getAttribute("reflexive") != null) {
					reflexive = Boolean.getBoolean(el.getAttribute("reflexive"));
				}

				try {
					URL rUrl = new URL(pUrl, file);
					Relation r = new LazyRelation(name, rUrl, keyType, valueType, reflexive);
					add(name, r);
				} catch (MalformedURLException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

}

class LazyRelation extends Relation {

	private String name;
	private URL url;
	private String keyType;
	private String valueType;
	private boolean reflexive;

	private Relation store;

	public LazyRelation(String name, URL url, String keyType, String valueType, boolean reflexive) {
		this.name = name;
		this.url = url;
		this.keyType = keyType;
		this.valueType = valueType;
		this.reflexive = reflexive;
	}

	protected void init() {
		if (store == null) {
			try {
				store = Relations.readFileRelation(url.openStream(), keyType, valueType, reflexive);
				Relations.add(name, store);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public boolean isReflexive() {
		init();
		return store.isReflexive();
	}

	public void add(Object x, Object y) {
		init();
		store.add(x, y);
	}

	public Set get(Object x) {
		init();
		return store.get(x);
	}

	public Set get(Object x, boolean considerReflexive) {
		init();
		return store.get(x, considerReflexive);
	}

	public Relation getInverse() {
		init();
		return store.getInverse();
	}

}
