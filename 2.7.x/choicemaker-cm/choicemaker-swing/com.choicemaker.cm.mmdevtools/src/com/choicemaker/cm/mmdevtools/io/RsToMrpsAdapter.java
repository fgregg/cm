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
package com.choicemaker.cm.mmdevtools.io;

import java.io.IOException;
import java.util.Date;

import com.choicemaker.cm.core.base.Decision;
import com.choicemaker.cm.core.base.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.ImmutableRecordPair;
import com.choicemaker.cm.core.base.MarkedRecordPairSource;
import com.choicemaker.cm.core.base.MutableMarkedRecordPair;
import com.choicemaker.cm.core.base.Record;
import com.choicemaker.cm.core.base.RecordSource;
import com.choicemaker.cm.core.base.Sink;

/**
 * Comment
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.2 $ $Date: 2010/03/29 14:27:37 $
 */
public class RsToMrpsAdapter implements MarkedRecordPairSource {

	protected RecordSource rs;
	protected Decision decision;
	protected Date date;
	protected String user;
	protected String src;
	protected String comment;

	public RsToMrpsAdapter(RecordSource rs) {
		this(rs, Decision.MATCH, new Date(), "", "", "");
	}

	public RsToMrpsAdapter(RecordSource rs,
						   Decision decision,
						   Date date,
						   String user,
						   String src,
						   String comment) {
		this.rs = rs;
		this.decision = decision;
		this.date = date;
		this.user = user;
		this.src = src;
		this.comment = comment;
	}

	public RecordSource getRecordSource() {
		return rs;
	}

	public MutableMarkedRecordPair getNextMarkedRecordPair() throws IOException {
		Record r = rs.getNext();
		return new MutableMarkedRecordPair(r, r, decision, date, user, src, comment);
	}

	public ImmutableRecordPair getNext() throws IOException {
		return getNextMarkedRecordPair();
	}

	public void setDecision(Decision d) {
		this.decision = d;
	}
	
	public void setDate(Date d) {
		this.date = d;
	}
	
	public void setUser(String usr) {
		this.user = usr;
	}
	
	public void setSrc(String src) {
		this.src = src;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}

	public void open() throws IOException {
		rs.open();
	}

	public void close() throws IOException {
		rs.close();
	}

	public boolean hasNext() throws IOException {
		return rs.hasNext();
	}

	public String getName() {
		return "RS to MRPS Adapter";
	}

	public void setName(String name) {
		// do nothing...
	}

	public ImmutableProbabilityModel getModel() {
		return rs.getModel();
	}

	public void setModel(ImmutableProbabilityModel m) {
		rs.setModel(m);
	}

	public boolean hasSink() {
		return false;
	}

	public Sink getSink() {
		return null;
	}

	public String getFileName() {
		return null;
	}

}
