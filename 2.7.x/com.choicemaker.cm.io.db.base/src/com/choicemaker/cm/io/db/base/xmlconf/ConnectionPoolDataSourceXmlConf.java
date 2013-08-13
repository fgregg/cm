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
package com.choicemaker.cm.io.db.base.xmlconf;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jdom.Element;

import com.choicemaker.cm.core.xmlconf.XmlConfigurator;
import com.choicemaker.cm.io.db.base.DataSources;

/**
 * XML configurator for the connection cache.
 */
public class ConnectionPoolDataSourceXmlConf {
	private static Logger logger = Logger.getLogger(ConnectionPoolDataSourceXmlConf.class);

	private static boolean alreadyInited = false;

	public static void init() {
		Element db = XmlConfigurator.getPlugin("db");
		if (db != null) {
			Iterator i = db.getChildren("ConnectionPool").iterator();
			while (i.hasNext()) {
				try {
					Element cp = (Element) i.next();
					String name = cp.getAttributeValue("name");

					// initialization params are kept in a Hashtable
					Hashtable args = new Hashtable();

					// the underlying driver
					args.put("jdbc.driver", cp.getChildText("driver"));

					args.put("jdbc.driver.classloader", XmlConfigurator.getReloadClassLoader());

					// the URL to connect the underlyng driver with the server
					args.put("jdbc.URL", cp.getChildText("url"));

					// these are properties that get passed
					// to DriverManager.getConnection(...)
					Properties jdbcProperties = new Properties();
					jdbcProperties.put("user", cp.getChildText("user"));
					jdbcProperties.put("password", cp.getChildText("password"));
					args.put("jdbc.properties", jdbcProperties);

					// a statement that is guaranteed to work
					// if the connection is working.
					args.put("jdbc.validityCheckStatement", cp.getChildText("validityCheckStatement"));

					// If this is specified, a low-priority thread will
					// sit in the background and refresh this pool every
					// N seconds.  In this case, it's refreshed every two minutes.
					Element e = cp.getChild("refreshThreadCheckInterval");
					if (e != null) {
						args.put("pool.refreshThreadCheckInterval", new Integer(e.getText()));
					}

					// the initial size of the pool.
					args.put("pool.initialSize", new Integer(cp.getChildText("initialSize")));

					// the maximum size the pool can grow to.
					args.put("pool.maxSize", new Integer(cp.getChildText("maxSize")));

					// each time the pool grows, it grows by this many connections
					args.put("pool.growBlock", new Integer(cp.getChildText("growBlock")));

					// between successive connections, wait this many milliseconds.
					args.put("pool.createWaitTime", new Integer(cp.getChildText("createWaitTime")));

					args.put("Syslog.classloader.warning", "off");

					// finally create the pool and we're ready to go!
					// JdbcConnectionPool pool = new JdbcConnectionPool(name, args);
					// DataSources.addDataSource(name, new JdbcConnectionPoolDataSource(name));
					throw new RuntimeException("not yet implemented");
						
				} catch (Exception ex) {
					logger.warn("Error creating connection pool.", ex);
				}
			}
		}
		
		alreadyInited = true;
	}

	/**
	 * Conditionally calls init() if it hasn't been called before over the life
	 * of the VM that this process is running in.
	 * 
	 * @return whether or not init() was actually called.
	 */
	public static boolean maybeInit() {
		if (!alreadyInited) {
			init();
			return true;
		} else {
			return false;
		}
	}

	public static void close() {
		// JdbcConnectionPoolDriver.shutdownAllConnections();
		throw new RuntimeException("not yet implemented");
	}
}
