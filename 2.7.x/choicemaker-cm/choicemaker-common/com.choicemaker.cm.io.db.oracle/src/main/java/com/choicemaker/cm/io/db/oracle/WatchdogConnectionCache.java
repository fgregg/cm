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
package com.choicemaker.cm.io.db.oracle;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;

import javax.sql.DataSource;

import oracle.jdbc.pool.OracleConnectionCacheImpl;

import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.io.db.oracle.xmlconf.OraConnectionCacheXmlConf;

/**
 * Description
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.1 $ $Date: 2010/01/28 02:02:10 $
 */
public class WatchdogConnectionCache extends Thread implements DataSource {
	private static Logger logger = Logger
			.getLogger(WatchdogConnectionCache.class.getName());
	private String name;
	private OracleConnectionCacheImpl cc;
	private LinkedList connections = new LinkedList();
	private boolean stop = false;
	private int numTimeouts;

	public WatchdogConnectionCache(String name) throws SQLException, XmlConfException {
		logger.info("Connection cache: " + name);
		this.name = name;
		cc = (OracleConnectionCacheImpl) OraConnectionCacheXmlConf.getConnectionCache(name);
		start();
	}

	public Connection getConnection() throws SQLException {
		return cc.getConnection();
	}

	public Connection getConnection(String username, String password) throws SQLException {
		return cc.getConnection(username, password);
	}

	public Connection getConnection(long timeout) throws SQLException {
		synchronized (this) {
			++numTimeouts;
		}
		Connection c = cc.getConnection();
		synchronized (this) {
			--numTimeouts;
			connections.add(new TimeoutConnection(c, timeout));
		}
		return c;
	}

	public int getLoginTimeout() throws SQLException {
		return cc.getLoginTimeout();
	}

	public PrintWriter getLogWriter() throws SQLException {
		return cc.getLogWriter();
	}

	public void setLoginTimeout(int seconds) throws SQLException {
		cc.setLoginTimeout(seconds);
	}

	public void setLogWriter(PrintWriter out) throws SQLException {
		cc.setLogWriter(out);
	}

	public int getCacheSize() {
		return cc.getCacheSize();
	}

	public int getActiveSize() {
		return cc.getActiveSize();
	}

	public synchronized void close() {
		stop = true;
		try {
			cc.close();
		} catch (SQLException ex) {
			logger.severe("Closing connection cache: " + ex.toString());
		}
	}

	public void run() {
		while (true) { // contains break
			try {
				Thread.sleep(5000);
				synchronized (this) {
					if (stop) {
						break;
					}
					long curTime = System.currentTimeMillis();
					Iterator i = connections.iterator();
					while (i.hasNext()) {
						TimeoutConnection b = (TimeoutConnection) i.next();
						if (curTime > b.endTime) {
							++numTimeouts;
							logger.warning("Forced closing of connection.");
							new ConnectionCloser(b.connection).start();
							i.remove();
						}
					}
					if (numTimeouts > 5) {
						logger.warning("Replacing connection cache.");
						i = connections.iterator();
						while (i.hasNext()) {
							new ConnectionCloser(((TimeoutConnection) i.next()).connection).start();
						}
						Thread.yield();
						numTimeouts = 0;
						new ConnectionCacheReplacer().start();
					}
				}
			} catch (InterruptedException ex) {
				logger.warning("Watchdog error: " + ex.toString());
			}
		}
	}

	synchronized void remove(Connection c) {
		Iterator i = connections.iterator();
		while (i.hasNext()) {
			if (c == ((TimeoutConnection) i.next()).connection) {
				i.remove();
				break;
			}
		}
	}

	private static class TimeoutConnection {
		Connection connection;
		long endTime;

		TimeoutConnection(Connection connection, long timeout) {
			this.connection = connection;
			if (timeout == 0) {
				endTime = Long.MAX_VALUE;
			} else {
				endTime = System.currentTimeMillis() + timeout;
			}
		}
	}

	private static class ConnectionCloser extends Thread {
		private Connection b;

		ConnectionCloser(Connection b) {
			this.b = b;
		}

		public void run() {
			try {
				b.close();
			} catch (SQLException ex) {
				logger.severe("Error closing: " + ex.toString());
			}
			logger.info("Completed closing.");
		}
	}

	private class ConnectionCacheReplacer extends Thread {
		public void run() {
			try {
				OraConnectionCacheXmlConf.remove(name);
				OracleConnectionCacheImpl tmp = cc;
				cc = (OracleConnectionCacheImpl) OraConnectionCacheXmlConf.getConnectionCache(name);
				tmp.close();
			} catch (Exception ex) {
				logger.severe("Replacing connection cache: " + ex.toString());
			}
		}
	}

	public java.util.logging.Logger getParentLogger()
			throws SQLFeatureNotSupportedException {
				throw new SQLFeatureNotSupportedException("not supported");
	}

	public Object unwrap(Class iface) throws SQLException {
		throw new SQLFeatureNotSupportedException("not supported");
	}

	public boolean isWrapperFor(Class iface) throws SQLException {
		throw new SQLFeatureNotSupportedException("not supported");
	}

}
