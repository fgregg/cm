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
package com.choicemaker.cm.io.blocking.automated.offline.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.choicemaker.cm.core.base.BlockingException;
import com.choicemaker.cm.core.util.IntArrayList;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecValSource;

/**
 * @author pcheung
 *
 */
public class RecValDBSource implements IRecValSource {

	public static final String tableName = "CMT_REC_VAL_ID";
	public static final String groupName = "GROUP_ID";
	public static final String recName = "REC_ID";
	public static final String valName = "VAL_ID";
	
	private static final String CHECK_SQL = "select count(*) from " + tableName + 
		" where " + groupName + " = ?";
		
	private static final String SELECT_SQL = "select " + recName + "," + valName + " from " + tableName + 
		" where " + groupName + " = ? order by " + recName;
		
	private static final String REMOVE_SQL = "delete from " + tableName + " where " +
		groupName + " = ?";

	private DataSource ds;
	private Connection conn;
	private int groupID;
	private boolean exists = false;
	private PreparedStatement selectStmt;
	private ResultSet selectRS;
	private int count = 0;
	
	private long nextRecID = Long.MIN_VALUE;
	private long nextRecID2 = Long.MIN_VALUE;
	
	private IntArrayList nextValIDs;
	private IntArrayList nextValIDs2;


	/** This constructor takes these two parameters.
	 * 
	 * @param conn - DB connection
	 * @param groupID - unique identifier for this object.
	 */
	public RecValDBSource (DataSource ds, int groupID) throws BlockingException {
		this.ds = ds;
		this.groupID = groupID;
		
		try {
			conn = ds.getConnection();
			//check to see if there is any data on the table for this group.
			PreparedStatement stmt = conn.prepareStatement( CHECK_SQL );
			stmt.setInt(1, groupID);
			ResultSet rs = stmt.executeQuery();
			if (rs.next() && rs.getInt(1) > 0) exists = true;
			
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException ex) {
			throw new BlockingException ( ex.toString() );
		}
	}



	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IRecValSource#getNextRecID()
	 */
	public long getNextRecID() throws BlockingException {
		return nextRecID;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IRecValSource#getNextValues()
	 */
	public IntArrayList getNextValues() throws BlockingException {
		return nextValIDs;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#exists()
	 */
	public boolean exists() {
		return exists;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#open()
	 */
	public void open() throws BlockingException {
		try {
			conn = ds.getConnection();
			conn.setReadOnly(true);
			selectStmt = conn.prepareStatement( SELECT_SQL );
			selectStmt.setInt(1, groupID);
			selectRS = selectStmt.executeQuery();
		} catch (SQLException ex) {
			throw new BlockingException (ex.toString());
		}
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#hasNext()
	 */
	public boolean hasNext() throws BlockingException {
		boolean ret = false;
		boolean stop = false;
		
		try {
			if (nextRecID != Long.MIN_VALUE) {
				nextRecID = nextRecID2;
				nextValIDs = new IntArrayList (nextValIDs2);
			}
			
			while (!stop && selectRS.next()) {
				long tempRec = selectRS.getLong(1);
				int tempVal = selectRS.getInt(2);

				//only for the first record				
				if (nextRecID == Long.MIN_VALUE) {
					nextRecID = tempRec;
					nextValIDs = new IntArrayList (1);
				}
				
				if (tempRec == nextRecID) {
					nextValIDs.add( tempVal);
				} else {
					stop = true;
					ret = true;
					nextRecID2 = tempRec;
					nextValIDs2 = new IntArrayList ();
					nextValIDs2.add( tempVal);
				}
			}
						
			if (!stop) {
				if (nextRecID != Long.MIN_VALUE) ret = true;
				
				nextRecID2 = Long.MIN_VALUE;
			}
			
		} catch (SQLException ex) {
			throw new BlockingException (ex.toString());
		}
		
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#close()
	 */
	public void close() throws BlockingException {
		try {
			if (selectRS != null) selectRS.close();
			if (selectStmt != null) selectStmt.close();
			if (conn != null) conn.close();
		} catch (SQLException ex) {
			throw new BlockingException (ex.toString());
		}
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#getInfo()
	 */
	public String getInfo() {
		return Integer.toString(groupID);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#remove()
	 */
	public void remove() throws BlockingException {
		try {
			conn = ds.getConnection();
			//check to see if there is any data on the table for this group.
			PreparedStatement stmt = conn.prepareStatement( REMOVE_SQL );
			stmt.setInt(1, groupID);
			stmt.execute();
			conn.commit();
			
			stmt.close();
			conn.close();
		} catch (SQLException ex) {
			throw new BlockingException ( ex.toString() );
		}
	}

}
