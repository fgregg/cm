/*
 * Created on Feb 21, 2004
 *
 */
package com.choicemaker.cm.io.db.sqlserver;

import java.io.IOException;
import java.sql.Connection;
import java.util.NoSuchElementException;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.Sink;

/**
 * This flavor of record source assumes that the connection 
 * 
 * @author ajwinkel
 *
 */
public class PiecewiseSqlServerRecordSource implements RecordSource {

	protected ImmutableProbabilityModel model;
	protected Connection conn;
	protected String dbConfiguration;
	protected String tableName;
	protected int rsId;
	protected int firstPiece;
	protected int lastPiece;
	
	protected boolean open;
	
	protected SqlServerRecordSource pieceRs;
	protected int currentPiece;
	protected int recordsRead;

	/**
	 * Assumes that 
	 */
	public PiecewiseSqlServerRecordSource(
		ImmutableProbabilityModel model,
		Connection conn,
		String dbConfiguration,
		String tableName,
		int rsId,
		int firstPiece,
		int lastPiece) {
		
		this.model = model;
		this.conn = conn;
		this.dbConfiguration = dbConfiguration;
		this.tableName = tableName;
		this.rsId = rsId;
		this.firstPiece = firstPiece;
		this.lastPiece = lastPiece;
		
		if (model == null) {
			throw new IllegalArgumentException("Model cannot be null");
		} else if (conn == null) {
			throw new IllegalArgumentException("Connection cannot be null");
		} else if (dbConfiguration == null) {
			throw new IllegalArgumentException("dbConfiguration is empty");
		} else if (tableName == null) {
			throw new IllegalArgumentException("tableName is empty");
		} else if (firstPiece < 1 || lastPiece < firstPiece) {
			throw new IllegalArgumentException("Illegal piece range: [" + firstPiece + ", " + lastPiece + "]");
		}
		
		reset();
	}

	public void open() throws IOException {
		if (!open) {
			advancePiece();
			open = true;
		} else {
			throw new IllegalStateException("Attempting to open an already-opened RecordSource.");
		}
	}

	private void reset() {
		this.currentPiece = this.firstPiece - 1;
		this.recordsRead = 0;
		this.open = false;
	}

	private void advancePiece() throws IOException {
		if (pieceRs != null) {
			pieceRs.close();
			pieceRs = null;
		}
		
		currentPiece++;
		if (currentPiece <= lastPiece) {
			pieceRs = new SqlServerRecordSource();
			pieceRs.setModel(model);
			pieceRs.setConnection(conn);
			pieceRs.setDbConfiguration(dbConfiguration);
			
			String idsQuery = "select record_id from " + tableName + " where rs_id = " + rsId + " and piece_id = " + currentPiece;
			pieceRs.setIdsQuery(idsQuery);
			
			pieceRs.open();
		}
	}

	public boolean hasNext() throws IOException {
		if (!open) {
			throw new IllegalStateException("RecordSource is not open.");
		}
		return currentPiece <= lastPiece && pieceRs.hasNext();
	}

	public Record getNext() throws IOException {
		if (!open) {
			throw new IllegalStateException("RecordSource is not open.");
		}

		if (!hasNext()) {
			throw new NoSuchElementException();
		} else {
			Record r = pieceRs.getNext();
			if (!pieceRs.hasNext()) {
				advancePiece();
			}
			return r;
		}
	}

	public void close() throws IOException {
		if (pieceRs != null) {
			pieceRs.close();
			pieceRs = null;
		}
		
		reset();
	}

	public ImmutableProbabilityModel getModel() {
		return model;
	}

	public void setModel(ImmutableProbabilityModel m) {
		this.model = m;
	}

	public String getName() {
		throw new UnsupportedOperationException("No reason for you to be calling this...");
	}

	public void setName(String name) {
		throw new UnsupportedOperationException("Use setId(int) instead.");
	}

	public boolean hasSink() {
		throw new UnsupportedOperationException("No reason for you to be calling this...");
	}

	public Sink getSink() {
		throw new UnsupportedOperationException("No sink.");
	}

	public String getFileName() {
		throw new UnsupportedOperationException("No reason for you to be calling this...");
	}

}
