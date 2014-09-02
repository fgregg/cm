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

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.jdom.Element;

import com.choicemaker.cm.core.xmlconf.XmlConfigurator;
import com.choicemaker.cm.io.db.base.DataSources;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * XML configurator for the connection cache.
 */
public class ConnectionPoolDataSourceXmlConf {
	private static Logger logger = Logger.getLogger(ConnectionPoolDataSourceXmlConf.class.getName());

	private static boolean alreadyInited = false;

	public static void init() {
		Element db = XmlConfigurator.getInstance().getPlugin("db");
		if (db != null) {
			Iterator i = db.getChildren("ConnectionPool").iterator();
			while (i.hasNext()) {
				try {
					Element cp = (Element) i.next();
					ComboPooledDataSource cpds = new ComboPooledDataSource();

					// the underlying driver
					final String jdbcDriver = cp.getChildText("driver");
					cpds.setDriverClass(jdbcDriver); // loads the jdbc driver

					// the URL to connect the underlyng driver with the server
					final String jdbcURL = cp.getChildText("url");
					cpds.setJdbcUrl(jdbcURL);

					// security credentials
					final String user = cp.getChildText("user");
					cpds.setUser(user);
					final String password = cp.getChildText("password");
					cpds.setPassword(password);

					// the initial size of the pool.
					final Integer poolInitialSize =
						new Integer(cp.getChildText("initialSize"));
					cpds.setMinPoolSize(poolInitialSize.intValue());

					// the maximum size the pool can grow to.
					final Integer poolMaxSize =
						new Integer(cp.getChildText("maxSize"));
					cpds.setMaxPoolSize(poolMaxSize.intValue());

					// each time the pool grows, it grows by this many
					// connections
					final Integer poolGrowBack =
						new Integer(cp.getChildText("growBlock"));
					cpds.setAcquireIncrement(poolGrowBack.intValue());

					// Currently unused by C3P0
					// final ClassLoader jdbcDriverClassLoader =
					// XmlConfigurator.getReloadClassLoader();
					// final String jdbcValidityCheckStatement =
					// cp.getChildText("validityCheckStatement");
					// Integer poolRefreshInterval = null;
					// Element e = cp.getChild("refreshThreadCheckInterval");
					// if (e != null) {
					// poolRefreshInterval = new Integer(e.getText());
					// }
					// final Integer poolCreateWaitTime = new
					// Integer(cp.getChildText("createWaitTime"));
					// final String syslogClassLoaderWarning = "off";

					final String name = cp.getAttributeValue("name");
					DataSources.addDataSource(name, cpds);

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
