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
package com.choicemaker.cm.core.report;

import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.Record;

/**
 * Comment
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.2 $ $Date: 2010/03/24 20:47:35 $
 */
public class Report {
	private static final ReporterPlugin[] NO_PLUGINS = new ReporterPlugin[0];
//	private static final String[] NO_ARGS = new String[0];
	private static final SortedSet NO_MATCHES = Collections.unmodifiableSortedSet(new TreeSet());
	
	private float differThreshold;
	private float matchThreshold;
	private int maxNumMatches;
	private IProbabilityModel probabilityModel;
	private long startTime;
	private long endTime;
	private String purpose;
	private Record queryRecord;
	private int numReturnedByBlocking;
	private SortedSet matches;
	private ReporterPlugin[] plugins;

	public Report(
		float differThreshold,
		float matchThreshold,
		int maxNumMatches,
		IProbabilityModel probabilityModel,
		long startTime,
		long endTime,
		String purpose,
		Record queryRecord,
		int numReturnedByBlocking,
		SortedSet matches,
		ReporterPlugin[] plugins) {
		this.differThreshold = differThreshold;
		this.matchThreshold = matchThreshold;
		this.maxNumMatches = maxNumMatches;
		this.probabilityModel = probabilityModel;
		this.startTime = startTime;
		this.endTime = endTime;
		this.purpose = purpose;
		this.queryRecord = queryRecord;
		this.numReturnedByBlocking = numReturnedByBlocking;
		this.matches = matches == null ? NO_MATCHES : matches ;
		this.plugins = plugins == null ? NO_PLUGINS : plugins ;
	}

	public float getDifferThreshold() {
		return differThreshold;
	}

	public long getEndTime() {
		return endTime;
	}

	public SortedSet getMatches() {
		return matches;
	}

	public float getMatchThreshold() {
		return matchThreshold;
	}

	public int getMaxNumMatches() {
		return maxNumMatches;
	}

	public ReporterPlugin[] getPlugins() {
		return plugins;
	}

	public IProbabilityModel getProbabilityModel() {
		return probabilityModel;
	}

	public Record getQueryRecord() {
		return queryRecord;
	}

	public long getStartTime() {
		return startTime;
	}
	
	public long getDuration() {
		return endTime - startTime;
	}

	public int getNumReturnedByBlocking() {
		return numReturnedByBlocking;
	}

	public String getPurpose() {
		return purpose;
	}
}
