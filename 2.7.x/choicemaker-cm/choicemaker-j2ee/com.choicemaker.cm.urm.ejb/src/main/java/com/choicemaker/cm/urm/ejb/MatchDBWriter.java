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
package com.choicemaker.cm.urm.ejb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;

import javax.sql.DataSource;

import java.util.logging.Logger;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.util.ConnectionUtils;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.urm.exceptions.CmRuntimeException;
import com.choicemaker.cm.urm.exceptions.RecordCollectionException;

/**
 * @author pcheung
 *
 * This object writes the de-dup match result file to the database.
 */
public class MatchDBWriter {
	
	private static final Logger log = Logger.getLogger(MatchDBWriter.class.getName());
	private static final int BATCH_SIZE = 500;

	private String name;
	private IMatchRecord2Source mSource;
	private DataSource ds;
	private String jobID;
	
	private long totalTime = 0; //total time in DB stored procedure
	
	public MatchDBWriter (IMatchRecord2Source mSource, DataSource ds, String name, long jobID) {
		
		this.mSource = mSource;
		this.ds = ds;
		this.jobID = String.valueOf(jobID);
		this.name =  name;
	}


	public long getTotalDBTime () {
		return totalTime;
	}


	/** This method writes the matches to DB.  
	 * 
	 * @return 0 if ok, -1 if error.
	 */
	public void writeToDB () throws RecordCollectionException, CmRuntimeException {

		Connection connWrite = null;		
		try {
			connWrite = ds.getConnection();
			connWrite.setAutoCommit(false);
			
			truncatePairsTable(connWrite);
			
			mSource.open();
				
			ArrayList matches = new ArrayList ();
				
			int count = 0;
				
			while (mSource.hasNext()) {
				MatchRecord2 mr = (MatchRecord2) mSource.getNext();
				matches.add(mr);
					
				count ++;
				if (count == BATCH_SIZE) {
					insertMatchesArray (connWrite, matches);
					matches = new ArrayList ();
					count = 0;
				}
			}
				
			mSource.close();
				
			//one last insert
			insertMatchesArray (connWrite, matches);
		} catch (BlockingException ex){
			throw new CmRuntimeException(ex.toString(),ex); 
		} catch (SQLException ex){
			throw new CmRuntimeException(ex.toString(),ex); 
		}
		finally{
			ConnectionUtils.tryToCloseConnection(connWrite);
		}
	}

	/** This method writes the match information to the database table.  It uses an array of variables.
	 * 
	 * @param matches
	 * @param ds
	 * @return 0 is OK, else returns -1 or some Oracle error id.
	 */
	private void insertMatchesArray (Connection connWrite,ArrayList matches) throws RecordCollectionException {
		Calendar cal = Calendar.getInstance ();
		long start = cal.getTimeInMillis();

		String insStmt = "insert into "+name+
					  " (job_id, record_1, record_2, record_2_source, probability, match_ind)"+
					  " values (?, ?, ?, ?, ?, ?)";
					  //"" values 	in_job_id, in_rec1, in_rec2, in_source, in_prob, in_type);;
		PreparedStatement pstmt = null;
		
		try {
			pstmt = connWrite.prepareStatement(insStmt);			  
			String decision;
			String secondRecordLocation;
			String r1s;
			String r2s;
			float prob;

			for (int j=0; j< matches.size(); j++) {
				MatchRecord2 mr = (MatchRecord2) matches.get(j);
				r1s = mr.getRecordID1().toString();
				r2s = mr.getRecordID2().toString();
				secondRecordLocation = Character.toString(mr.getRecord2Source());
				prob = mr.getProbability();
				decision = Character.toString(mr.getMatchType());
					
				pstmt.setString(1, jobID);
				pstmt.setString(2, r1s);
				pstmt.setString(3, r2s);
				pstmt.setString(4, secondRecordLocation);
				pstmt.setFloat(5, prob);
				pstmt.setString(6, decision);
				pstmt.execute();
			}
			connWrite.commit();
	
		} catch (SQLException ex) {
			log.severe(ex.toString());
			ConnectionUtils.tryToCloseStatement(pstmt);
			ConnectionUtils.tryToCloseConnection(connWrite);
			throw new RecordCollectionException(ex.toString());	
		}
		ConnectionUtils.tryToCloseStatement(pstmt);
		
		cal = Calendar.getInstance();
		start = cal.getTimeInMillis() - start;
	}


	/** Truncates  and removes all existing data.
	 * 
	 * @throws SQLException
	 */
	public void truncatePairsTable (Connection connWrite) throws RecordCollectionException {
		
		PreparedStatement pstmt = null;
		try {
			String trStmt = "truncate table "+this.name;
			pstmt = connWrite.prepareStatement(trStmt);
			log.fine("prepared stmt: "+pstmt);
			pstmt.execute();
			connWrite.commit();
			
		} catch (SQLException ex) {
			log.severe(ex.toString());
			ConnectionUtils.tryToCloseStatement(pstmt);
			ConnectionUtils.tryToCloseConnection(connWrite);
			connWrite = null;
			throw new RecordCollectionException(ex.toString());
		} 
		ConnectionUtils.tryToCloseStatement(pstmt);
	}

}
