/*
 * Created on Dec 6, 2006
 *
 */
package com.choicemaker.cm.urm.config;

import java.io.Serializable;

/**
 * @author emoussikaev
 */
public interface UrmSettings extends Serializable {

	AnalysisResultFormat getAnalysisResultFormat();

	Float getDifferThreshold();

	IGraphProperty getGraphPropType();

	Integer getLimitPerBlockingSet();

	Float getMatchThreshold();

	public Integer getMaxBlockSize();

	Integer getMaxNumMatches();

	public Integer getMaxOversized();

	Integer getMaxSingle();

	public Integer getMinFields();

	String getModelName();

	Boolean getMustIncludeQuery();

	RecordType getRecordType();

	ScoreType getScoreType();

	void setAnalysisResultFormat(AnalysisResultFormat format);

	void setDifferThreshold(Float float1);

	void setGraphPropType(IGraphProperty property);

	void setLimitPerBlockingSet(Integer integer);

	void setMatchThreshold(Float float1);

	public void setMaxBlockSize(Integer integer);

	void setMaxNumMatches(Integer integer);

	public void setMaxOversized(Integer integer);

	void setMaxSingle(Integer integer);

	public void setMinFields(Integer integer);

	void setModelName(String string);

	void setMustIncludeQuery(Boolean boolean1);

	void setRecordType(RecordType type);

	void setScoreType(ScoreType type);

}
