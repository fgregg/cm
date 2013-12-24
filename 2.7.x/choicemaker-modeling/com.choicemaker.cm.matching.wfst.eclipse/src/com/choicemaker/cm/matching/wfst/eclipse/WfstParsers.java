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
package com.choicemaker.cm.matching.wfst.eclipse;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

import com.choicemaker.cm.matching.wfst.AmbiguousParser;
import com.choicemaker.cm.matching.wfst.WfstParser;

/**
 * Collection of {@link AmbiguousParser ambiguous} parsers, implemented by
 * weighted finite state transducers. Each transducer is defined by a state table,
 * which in turn is defined by a grammar.
 * 
 * WfstParsers work similar to parsers, sets and relations. The module is loaded through
 * the class <code>com.choicemaker.cm.xmlconf.XmlWfstParsersInitializer</code>.
 * Based on this, we can then use an expression like 
 * <code>WfstParsers.parse("addressParser", q.full_address)</code> in a schema 
 * expression or in a clue/rule.
 * 
 * @author  	Rick Hall
 * @version   $Revision: 1.2 $ $Date: 2010/03/27 22:30:39 $
 */
public final class WfstParsers {

	private WfstParsers() {
	}

	private static Map parsers = new HashMap();

	static {
		initRegisteredWfstParsers();
	}

	/**
	 * Adds a parser to the collection of parsers.
	 *
	 * @param   name  The name of the collection.
	 * @param   parser  The parser to be added.
	 *
	 */
	public static void addWfstParser(String name, AmbiguousParser parser) {
		parsers.put(name, parser);
	}

	/**
	 * Parses a file, creates a parser and adds to the collection of parsers.
	 * @param name the parser name
	 * @param fUrl a URL pointing to the parser file
	 * @param lazy whether initialization of the parser should be deferred to first use
	 */
	static void addParser(String name, URL filterUrl, URL grammarUrl, boolean lazy)
		throws IOException {

		InputStream ifs = null;
		InputStream igs = null;
		try {
			AmbiguousParser p = null;
			if (lazy) {
				p = new LazyWfstParser(name, filterUrl, grammarUrl);
			} else {
				ifs = filterUrl.openStream();
				igs = grammarUrl.openStream();
				p = WfstParser.readWfstParser(ifs, igs);
			}
			addWfstParser(name, p);
		} finally {
			if (ifs != null) {
				try {
					ifs.close();
					ifs = null;
				} catch (IOException x) {
					x.printStackTrace();
				}
			}
			if (igs != null) {
						try {
							igs.close();
							igs = null;
						} catch (IOException x) {
							x.printStackTrace();
						}
					}
		} // finally

		return;
	} // addParser(String,String,boolean)

	/**
	 * Returns the parser named by <code>name</code>.
	 * 
	 * @param name the name of the collection
	 * @return the collection named by name
	 */
	public static AmbiguousParser getWfstParser(String name) {
		AmbiguousParser m = (AmbiguousParser) parsers.get(name);
		if (m instanceof LazyWfstParser) {
			((LazyWfstParser) m).init();
		}
		return (AmbiguousParser) parsers.get(name);
	}

	/**
	 * Returns a Collection containing the names of the WfstParser instances contained
	 * herein.
	 * @return a Collection of the names of all registered parsers
	 */
	public static Collection getWfstParserNames() {
		return parsers.keySet();
	}

	/**
	 * Retrieves the AmbiguousParser named by <code>name</code> and uses it to
	 * parse the specified text.
	 * 
	 * @param name the name of the AmbiguousParser with which to perform the parse
	 * @param text the text to parse
	 * @return a list of map instances. The keys of each map are terminal symbols in a
	 * grammar; the values are the parsed values from the input string.
	 * @see AmbiguousParser
	 * @throws IllegalArgumentException if no AmbiguousParser named <code>name</code> is registered.
	 */
	public static List parse(String name, String text) {
		AmbiguousParser parser = (AmbiguousParser) parsers.get(name);
		if (parser == null) {
			throw new IllegalArgumentException(
				"There is no parser named " + name + " registered");
		} else {
			return parser.parse(text);
		}
	}

	/**
	 * Loads  parsers registered by plugins.
	 */
	static void initRegisteredWfstParsers() {
		IExtensionPoint pt =
			Platform.getPluginRegistry().getExtensionPoint(
				"com.choicemaker.cm.matching.wfst.parser");
		IExtension[] extensions = pt.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			IExtension ext = extensions[i];
			URL pUrl = ext.getDeclaringPluginDescriptor().getInstallURL();
			IConfigurationElement[] els = ext.getConfigurationElements();
			for (int j = 0; j < els.length; j++) {
				IConfigurationElement el = els[j];

				String name = el.getAttribute("name");
				String filterFile = el.getAttribute("filterFile");
				String grammarFile = el.getAttribute("grammarFile");
				boolean lazy = true;
				if (el.getAttribute("lazy") != null) {
					lazy = Boolean.getBoolean(el.getAttribute("lazy"));
				}
				
				URL fUrl = null;
				URL gUrl = null;
				try {
					fUrl = new URL(pUrl, filterFile);
					gUrl = new URL(pUrl, grammarFile);
					addParser(name, fUrl, gUrl, lazy);
				} catch (MalformedURLException ex) {
					ex.printStackTrace();
				} catch (IOException ex) {
					ex.printStackTrace();
				}

			} // for j
		} // for i

		return;
	} // initRegisteredWfstParsers

} // WfstParsers

