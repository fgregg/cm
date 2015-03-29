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
import java.util.logging.Logger;

import org.jdom.Document;
import org.jdom.Element;

import com.choicemaker.cm.core.xmlconf.XmlConfigurator;
import com.choicemaker.cm.io.db.base.DataSources;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * XML configuration for the connection cache. Uses C3P0 to implement the cache.
 * 
 * @see http://www.mchange.com/projects/c3p0/
 */
public class ConnectionPoolDataSourceXmlConf {

	/** Default limit to retry attempts */
	public static final int DEFAULT_ACQUIRE_RETRY_ATTEMPTS = 10;

	/** Default delay before retrying a connection (msec) */
	public static final int DEFAULT_AQUIRE_RETRY_DELAY = 1500;

	/** Default overall limit to connection checkout (msec) */
	public static final int DEFAULT_CHECKOUT_TIME =
		DEFAULT_ACQUIRE_RETRY_ATTEMPTS * DEFAULT_AQUIRE_RETRY_DELAY;

	private static Logger logger = Logger
			.getLogger(ConnectionPoolDataSourceXmlConf.class.getName());

	private static boolean alreadyInited = false;
	
	public static void init() {
		Document d = XmlConfigurator.getInstance().getDocument();
		init(d);
	}

	public static void init(Document d) {
		Element db = XmlConfigurator.getPlugin(d, "db");
		init(db);
	}

	public static void init(Element db) {
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

					cpds.setAcquireRetryAttempts(DEFAULT_ACQUIRE_RETRY_ATTEMPTS);
					cpds.setAcquireRetryDelay(DEFAULT_AQUIRE_RETRY_DELAY);
					cpds.setCheckoutTimeout(DEFAULT_CHECKOUT_TIME);

					final String name = cp.getAttributeValue("name");
					DataSources.addDataSource(name, cpds);

				} catch (Exception ex) {
					logger.warning("Error creating connection pool: " + ex);
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
