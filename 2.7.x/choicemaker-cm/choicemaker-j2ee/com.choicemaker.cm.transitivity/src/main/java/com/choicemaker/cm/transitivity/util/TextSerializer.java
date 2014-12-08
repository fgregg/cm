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
package com.choicemaker.cm.transitivity.util;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.choicemaker.cm.transitivity.core.CompositeEntity;
import com.choicemaker.cm.transitivity.core.Entity;
import com.choicemaker.cm.transitivity.core.INode;
import com.choicemaker.cm.transitivity.core.TransitivityResult;
import com.choicemaker.cm.transitivity.core.TransitivityResultSerializer;

/**
 * This object takes a TransitivityResult and a Writer and outputs the clusters
 * as RECORD_ID, MATCH_GROUP_ID, HOLD_GROUP_ID.
 *
 * @author pcheung
 *
 *         ChoiceMaker Technologies, Inc.
 */
@SuppressWarnings({ "rawtypes" })
public class TextSerializer implements TransitivityResultSerializer {

	private static final long serialVersionUID = 271L;

	protected static final char DELIMITER = '|';
	protected static final String NEW_LINE = System
			.getProperty("line.separator");

	// counter variables to assign hold and merge group ids.
	private int holdCount = 0;
	private int matchCount = 0;

	protected final Comparator<Record> recordComparator;

	public TextSerializer(Comparator<Record> rc) {
		if (rc == null) {
			throw new IllegalArgumentException("null record comparator");
		}
		this.recordComparator = rc;
	}

	@Override
	public void serialize(TransitivityResult result, Writer writer)
			throws IOException {
		if (result == null || writer == null) {
			throw new IllegalArgumentException("null argument");
		}

		// first get all the record IDs from the clusters.
		List<Record> records = new ArrayList<>();
		Iterator it = result.getNodes();
		while (it.hasNext()) {
			CompositeEntity ce = (CompositeEntity) it.next();
			getCompositeEntity(ce, records);
		}

		// second, sort them accordingly
		Object[] recs = handleSort(records);

		// free memory
		records.clear();
		records = null;

		// third, write them out.
		writeRecords(recs, writer);

		writer.flush();
		writer.close();

		// free memory
		recs = null;
	}

	/**
	 * This method gets the record ID and assign to it match group and hold
	 * group ids.
	 *
	 * @param ce
	 * @throws IOException
	 */
	protected void getCompositeEntity(CompositeEntity ce, List<Record> records) {
		holdCount++;

		List children = ce.getChildren();
		int s = children.size();
		for (int i = 0; i < s; i++) {
			INode node = (INode) children.get(i);
			if (node instanceof Entity) {
				TransitivityResultSerializer.Record r =
					new TransitivityResultSerializer.Record(node.getNodeId(),
							0, holdCount);
				records.add(r);
			} else if (node instanceof CompositeEntity) {
				matchCount++;
				List children2 = node.getChildren();
				int s2 = children2.size();
				for (int j = 0; j < s2; j++) {
					INode node2 = (INode) children2.get(j);
					if (node2 instanceof Entity) {
						TransitivityResultSerializer.Record r =
							new TransitivityResultSerializer.Record(
									node2.getNodeId(), matchCount, holdCount);
						records.add(r);
					} else {
						throw new IllegalArgumentException(
								"Does not support CompositeEntity within a CompositeEntity.");
					}
				}

			} else {
				throw new IllegalArgumentException("Unknown node: "
						+ node.toString());
			}
		}
	}

	protected Record[] handleSort(List<Record> records) {
		assert records != null;

		Comparator<Record> sort = this.recordComparator;
		Record[] recs = records.toArray(new Record[records.size()]);
		Arrays.sort(recs, sort);
		return recs;
	}

	private void writeRecords(Object[] recs, Writer writer) throws IOException {
		int s = recs.length;
		for (int i = 0; i < s; i++) {
			TransitivityResultSerializer.Record r = (TransitivityResultSerializer.Record) recs[i];
			writer.write(printRecord(r));
		}
	}

	protected String printRecord(TransitivityResultSerializer.Record r) {
		StringBuffer sb = new StringBuffer();
		sb.append(r.id.toString());
		sb.append(DELIMITER);
		sb.append(r.mergeGroupId);
		sb.append(DELIMITER);
		sb.append(r.holdGroupId);
		sb.append(NEW_LINE);
		return sb.toString();
	}

}
