/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.choicemaker.fake;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Singleton that implements PluginDiscovery
 * 
 * @author rphall
 */
public class EmbeddedPluginDiscovery implements PluginDiscovery {

	public static final String PREFIX = "META-INF/plugins/";
	public static final String PLUGINS_LISTING = "plugins.xml";
	public static final String PLUGIN_DESCRIPTOR_FILE = "plugin.xml";
	public static final String FRAGMENT_DESCRIPTOR_FILE = "fragment.xml";

	private static void fail(String msg, Throwable cause)
			throws PluginDiscoveryException {
		throw new PluginDiscoveryException(msg, cause);
	}

	// The class loader used to locate, load, and instantiate providers
	private ClassLoader loader;

	// Cached plugin IDs, in instantiation order
	// private LinkedHashMap<String,?> plugins = new LinkedHashMap<>();
	private List<String> pluginIds = new LinkedList<>();

	// The current lazy-lookup iterator
	private LazyIterator lookupIterator;

	public EmbeddedPluginDiscovery() {
		this(Thread.currentThread().getContextClassLoader());
	}

	public EmbeddedPluginDiscovery(ClassLoader cl) {
		if (cl == null) {
			throw new IllegalArgumentException("null class loader");
		}
		loader = cl;
		reload();
	}

	public void reload() {
		pluginIds.clear();
		lookupIterator = new LazyIterator(loader);
		while (lookupIterator.hasNext()) {
			URL u = lookupIterator.next();
			pluginIds.add(u.toString());
		}
	}

	@Override
	public List<String> listPluginIds() {
		return Collections.unmodifiableList(pluginIds);
	}

	// Parse the content of the given URL as a plugins-listing file.
	//
	// @param u
	// The URL naming the configuration file to be parsed
	//
	// @return A (possibly empty) iterator that will yield the provider-class
	// names in the given configuration file that are not yet members
	// of the returned set
	//
	// @throws PluginDiscoveryException
	// If an I/O error occurs while reading from the given URL, or
	// if a configuration-file format error is detected
	//
	private Iterator<URL> parse(URL u) throws PluginDiscoveryException {
		InputStream in = null;
		List<URL> urls = Collections.emptyList();
		try {
			in = u.openStream();
			urls = getPluginPaths(u, in);
		} catch (IOException x) {
			fail("Error reading configuration file", x);
		}
		return urls.iterator();
	}

	private static List<URL> getPluginPaths(URL base, InputStream in) {
		List<URL> retVal = Collections.emptyList();
		try {
			PluginsParser pluginsParser = new PluginsParser(base);
			XMLReader xmlReader =
					SAXParserFactory.newInstance().newSAXParser().getXMLReader();
			xmlReader.setContentHandler(pluginsParser);
			InputSource is = new InputSource(in);
			xmlReader.parse(is);
			retVal = pluginsParser.getDescriptorUrls();
		} catch (Exception ex) {
			throw new Error("Reading plugins", ex);
		}
		return retVal;
	}

	// Private inner class that parses a PLUGINS_LISTING file
	//
	private static class PluginsParser extends DefaultHandler {
		private List<URL> urls = new ArrayList<>();
		private final URL base;

		public PluginsParser(URL u) {
			if (u == null) {
				throw new IllegalArgumentException("null base URL");
			}
			base = u;
		}

		public List<URL> getDescriptorUrls() {
			return urls;
		}

		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			qName = qName.intern();
			if (qName == IModel.PLUGIN) {
				addElement(attributes, IModel.PLUGIN_ID, PLUGIN_DESCRIPTOR_FILE);
			} else if (qName == IModel.FRAGMENT) {
				addElement(attributes, IModel.FRAGMENT_ID,
						FRAGMENT_DESCRIPTOR_FILE);
			}
		}

		private void addElement(Attributes attributes, String idAttribute,
				String descriptorFile) throws SAXException {
			String idWithVersion =
				attributes.getValue(idAttribute) + "_"
						+ attributes.getValue(IModel.PLUGIN_VERSION);
			idWithVersion = idWithVersion.replace('.', '_');
			String resource = idWithVersion + "/" + descriptorFile;
			URL url;
			try {
				url = new URL(base, resource);
			} catch (MalformedURLException e) {
				String msg =
					"Malformed URL: '" + base.toString() + "' + '" + resource
							+ "'";
				throw new SAXException(msg);
			}
			urls.add(url);
		}

	}

	// Private inner class implementing fully-lazy plugin lookup
	//
	private class LazyIterator implements Iterator<URL> {

		ClassLoader loader;
		Enumeration<URL> configs = null;
		Iterator<URL> pending = null;
		URL nextURL = null;

		private LazyIterator(ClassLoader loader) {
			this.loader = loader;
		}

		public boolean hasNext() {
			if (nextURL != null) {
				return true;
			}
			if (configs == null) {
				try {
					String fullName = PREFIX + PLUGINS_LISTING;
					if (loader == null)
						configs = ClassLoader.getSystemResources(fullName);
					else
						configs = loader.getResources(fullName);
				} catch (IOException x) {
					fail("Error locating configuration files", x);
				}
			}
			while ((pending == null) || !pending.hasNext()) {
				if (!configs.hasMoreElements()) {
					return false;
				}
				pending = parse(configs.nextElement());
			}
			nextURL = pending.next();
			return true;
		}

		public URL next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			URL cn = nextURL;
			nextURL = null;
			return cn;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

}
