package com.choicemaker.cm.modelmaker.filter;

import java.beans.PropertyChangeListener;

import com.choicemaker.cm.analyzer.filter.Filter;
import com.choicemaker.cm.analyzer.filter.IMarkedRecordPairFilter;

public interface ListeningMarkedRecordPairFilter extends Filter,
		IMarkedRecordPairFilter, PropertyChangeListener {

}
