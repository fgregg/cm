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
package com.choicemaker.cm.core.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.logging.Logger;


/**
 * This object contains standard connection util methods.
 * 
 * @author pcheung
 *
 */
public class ConnectionUtils {

	private static Logger logger = Logger.getLogger(ConnectionUtils.class.getName());

	public static void tryToCloseResultSet(ResultSet stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (Exception ex) {
				logger.warning(ex.toString());
			}
		}
	}

	public static void tryToCloseStatement(Statement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (Exception ex) {
				logger.warning(ex.toString());
			}
		}
	}

	public static void tryToCloseConnection(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception ex) {
				logger.warning(ex.toString());
			}
		}
	}

	public static void tryToRollbackConnection(Connection conn) {
		if (conn != null) {
			try {
				conn.rollback();
			} catch (Exception ex) {
				logger.warning(ex.toString());
			}
		}
	}
	
	public static void tryToCommitConnection(Connection conn) {
		if (conn != null) {
			try {
				conn.commit();
			} catch (Exception ex) {
				logger.warning(ex.toString());
			}
		}
	}
	

}
