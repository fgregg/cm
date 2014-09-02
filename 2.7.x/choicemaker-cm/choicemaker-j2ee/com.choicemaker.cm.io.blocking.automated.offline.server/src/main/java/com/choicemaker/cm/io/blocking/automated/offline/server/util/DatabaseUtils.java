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
package com.choicemaker.cm.io.blocking.automated.offline.server.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import com.choicemaker.cm.core.util.ConnectionUtils;

/**
 * @author pcheung
 *
 */
public class DatabaseUtils {
	
	private static final Logger logger = Logger.getLogger(DatabaseUtils.class.getName());
	
	
	/** This method gets the next number in the job id sequence.
	 * 
	 * @param conn - database connection
	 * @return next job id
	 */
	public static int getNextID (Connection conn) {
		int ret = 0;
		Statement  stmt = null;

		try {
			conn.setAutoCommit(false);

			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select cmt_seq_job_id.nextval from dual");
			rs.next();
			ret = rs.getInt(1);
			
			ConnectionUtils.tryToCommitConnection(conn);
			ConnectionUtils.tryToCloseResultSet(rs);
			ConnectionUtils.tryToCloseStatement(stmt);
			
		} catch (Exception ex) {
			logger.severe(ex.toString());
		}
		
		return ret;
	}
	

	/** This method gets the next number in the job id sequence.
	 * 
	 * @param conn - database connection
	 * @return next job id
	 */
	public static int getNextIdSqlServer (Connection conn) {
		int ret = 0;
		Statement  stmt = null;

		try {
			conn.setAutoCommit(false);

			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select id from cmt_sequence");
			rs.next();
			ret = rs.getInt(1);
			
			int i = stmt.executeUpdate("update cmt_sequence set id = id + 1");
			
			if (i != 1) throw new SQLException ("Could not update cmt_sequence");
			
			ConnectionUtils.tryToCommitConnection(conn);
			ConnectionUtils.tryToCloseResultSet(rs);
			ConnectionUtils.tryToCloseStatement(stmt);
			
		} catch (Exception ex) {
			logger.severe(ex.toString());
		}
		
		return ret;
	}



}
