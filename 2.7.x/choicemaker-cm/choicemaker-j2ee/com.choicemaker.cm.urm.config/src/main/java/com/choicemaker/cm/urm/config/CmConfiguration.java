/*
 * Created on Nov 28, 2006
 *
 */
package com.choicemaker.cm.urm.config;

import java.util.Hashtable;

/**
 * @author emoussikaev
 *
 */
public class CmConfiguration implements UrmSettings, UrmSettings2 {

	static final long serialVersionUID = 271L;

	protected static final String PN_MAX_SINGLE = "MaxSingle";
	protected static final String PN_MUST_INCLUDE_QUERY = "MustIncludeQuery";
	protected static final String PN_DIFFER_THRESHOLD = "DifferThreshold";
	protected static final String PN_MATCH_THRESHOLD = "MatchThreshold";
	protected static final String PN_MODEL_NAME = "ModelName";
	protected static final String PN_GRAPH_PROP_TYPE = "GraphPropType";
	protected static final String PN_RECORD_TYPE = "RecordType";
	protected static final String PN_SCORE_TYPE = "ScoreType";
	protected static final String PN_ANALYSIS_RESULT_FORMAT =
		"AnalysisResultFormat";
	protected static final String PN_MAX_NUM_MATCHES = "MaxNumMatches";
	protected static final String PN_LIMIT_PER_BLOCKING_SET =
		"LimitPerBlockingSet";
	protected static final String PN_MAX_BLOCK_SIZE = "MaxBlockSize";
	protected static final String PN_MAX_OVERSIZED = "MaxOversized";
	protected static final String PN_MIN_FIELDS = "MinFields";

	protected static final String PN_IS_STAGING_TO_MASTER = "isStagingToMaster";

	@SuppressWarnings("rawtypes")
	protected Hashtable props = new Hashtable();

	public CmConfiguration() {
	}

	@SuppressWarnings("rawtypes")
	public CmConfiguration(CmConfiguration cmConf1) {
		if (cmConf1 != null) {
			this.props = (Hashtable) cmConf1.props.clone();
		}
	}

	public Integer getMaxSingle() {
		return (Integer) props.get(PN_MAX_SINGLE);
	}

	public String getModelName() {
		return (String) props.get(PN_MODEL_NAME);
	}

	public Boolean getMustIncludeQuery() {
		return (Boolean) props.get(PN_MUST_INCLUDE_QUERY);
	}

	public RecordType getRecordType() {
		return (RecordType) props.get(PN_RECORD_TYPE);
	}

	public ScoreType getScoreType() {
		return (ScoreType) props.get(PN_SCORE_TYPE);
	}

	public Float getDifferThreshold() {
		return (Float) props.get(PN_DIFFER_THRESHOLD);
	}

	public IGraphProperty getGraphPropType() {
		return (IGraphProperty) props.get(PN_GRAPH_PROP_TYPE);
	}

	public Float getMatchThreshold() {
		return (Float) props.get(PN_MATCH_THRESHOLD);
	}

	@SuppressWarnings("unchecked")
	public void setMaxSingle(Integer integer) {
		props.put(PN_MAX_SINGLE, integer);
	}

	@SuppressWarnings("unchecked")
	public void setModelName(String string) {
		props.put(PN_MODEL_NAME, string);
	}

	@SuppressWarnings("unchecked")
	public void setMustIncludeQuery(Boolean boolean1) {
		props.put(PN_MUST_INCLUDE_QUERY, boolean1);
	}

	@SuppressWarnings("unchecked")
	public void setRecordType(RecordType type) {
		props.put(PN_RECORD_TYPE, type);
	}

	@SuppressWarnings("unchecked")
	public void setScoreType(ScoreType type) {
		props.put(PN_SCORE_TYPE, type);
	}

	@SuppressWarnings("unchecked")
	public void setDifferThreshold(Float float1) {
		props.put(PN_DIFFER_THRESHOLD, float1);
	}

	@SuppressWarnings("unchecked")
	public void setGraphPropType(IGraphProperty property) {
		props.put(PN_GRAPH_PROP_TYPE, property);
	}

	@SuppressWarnings("unchecked")
	public void setMatchThreshold(Float float1) {
		props.put(PN_MATCH_THRESHOLD, float1);
	}

	public AnalysisResultFormat getAnalysisResultFormat() {
		return (AnalysisResultFormat) props.get(PN_ANALYSIS_RESULT_FORMAT);
	}

	@SuppressWarnings("unchecked")
	public void setAnalysisResultFormat(AnalysisResultFormat format) {
		props.put(PN_ANALYSIS_RESULT_FORMAT, format);
	}

	public Integer getMaxNumMatches() {
		return (Integer) props.get(PN_MAX_NUM_MATCHES);
	}

	@SuppressWarnings("unchecked")
	public void setMaxNumMatches(Integer integer) {
		props.put(PN_MAX_NUM_MATCHES, integer);
	}

	public Integer getLimitPerBlockingSet() {
		return (Integer) props.get(PN_LIMIT_PER_BLOCKING_SET);
	}

	@SuppressWarnings("unchecked")
	public void setLimitPerBlockingSet(Integer integer) {
		props.put(PN_LIMIT_PER_BLOCKING_SET, integer);
	}

	public Integer getMaxBlockSize() {
		return (Integer) props.get(PN_MAX_BLOCK_SIZE);
	}

	@SuppressWarnings("unchecked")
	public void setMaxBlockSize(Integer integer) {
		props.put(PN_MAX_BLOCK_SIZE, integer);
	}

	public Integer getMaxOversized() {
		return (Integer) props.get(PN_MAX_OVERSIZED);
	}

	@SuppressWarnings("unchecked")
	public void setMaxOversized(Integer integer) {
		props.put(PN_MAX_OVERSIZED, integer);
	}

	public Integer getMinFields() {
		return (Integer) props.get(PN_MIN_FIELDS);
	}

	@SuppressWarnings("unchecked")
	public void setMinFields(Integer integer) {
		props.put(PN_MIN_FIELDS, integer);
	}

	/**
	 * Returns true or false (the default), never null.
	 */
	public Boolean isMasterToMasterLinkage() {
		Boolean b = isStagingToMasterMatching();
		// assert b != null
		Boolean retVal;
		if (b.booleanValue()) {
			retVal = Boolean.FALSE;
		} else {
			retVal = Boolean.TRUE;
		}
		return retVal;
	}

	/**
	 * Returns true (the default) or false, never null.
	 */
	public Boolean isStagingToMasterMatching() {
		Boolean retVal = (Boolean) props.get(PN_IS_STAGING_TO_MASTER);
		if (retVal == null) {
			retVal = Boolean.TRUE;
			setStagingToMasterMatching(retVal);
		}
		return retVal;
	}

	/**
	 * Null arguments to this method are treated as <code>false</code>.
	 */
	public void setMasterToMasterLinkage(Boolean boolean1) {
		if (boolean1 == null) {
			boolean1 = Boolean.TRUE;
		} else if (boolean1 == Boolean.TRUE) {
			boolean1 = Boolean.FALSE;
		} else {
			boolean1 = Boolean.TRUE;
		}
		setStagingToMasterMatching(boolean1);
	}

	/**
	 * This operation should be equivalent to invoking
	 * <code>setMasterToMaster(!boolean1)</code> Null arguments to this method
	 * should be treated as <code>true</code>.
	 * 
	 * @param boolean1
	 *            true to set staging-to-master matching.
	 */
	@SuppressWarnings("unchecked")
	public void setStagingToMasterMatching(Boolean boolean1) {
		if (boolean1 == null) {
			boolean1 = Boolean.TRUE;
		}
		props.put(PN_IS_STAGING_TO_MASTER, boolean1);
	}

}
