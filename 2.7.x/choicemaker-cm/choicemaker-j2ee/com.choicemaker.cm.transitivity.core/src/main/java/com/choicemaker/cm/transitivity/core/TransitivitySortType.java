package com.choicemaker.cm.transitivity.core;

import java.util.Comparator;

import com.choicemaker.cm.transitivity.core.TransitivityResultSerializer.Record;
import com.choicemaker.cm.transitivity.core.TransitivityResultSerializer.SortByID;
import com.choicemaker.cm.transitivity.core.TransitivityResultSerializer.SortByHoldMergeID;

public enum TransitivitySortType {
	
	SORT_BY_ID("R3L", new SortByID()), SORT_BY_HOLD_MERGE_ID("H3L", new SortByHoldMergeID());

	private final String fileExtension;
	private final Comparator<Record> comparator;

	TransitivitySortType(String ext, Comparator<Record> trcs) {
		assert ext != null && ext.equals(ext.trim()) && !ext.isEmpty();
		assert trcs != null;
		fileExtension = ext;
		this.comparator = trcs;
	}
	public String getFileExtension() {
		return fileExtension;
	}
	public Comparator<Record> getComparator() {
		return comparator;
	}

}