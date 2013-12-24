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
package com.choicemaker.cm.io.db.base.gen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.choicemaker.cm.io.db.base.Index;

/**
 * Comment
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.2 $ $Date: 2010/03/28 09:05:59 $
 */
public class IndexCombinations {
	public static class Combination {
		String fields;
		Index[] indices;

		Combination(String fields, Index[] indices) {
			this.fields = fields;
			this.indices = indices;
		}
		public String getFields() {
			return fields;
		}
		public Index[] getIndices() {
			return indices;
		}

	}

	//	static class TableCombination {
	//		String table;
	//		Combination[] combinations;
	//		TableCombination(String table, Combination[] combinations) {
	//			this.table = table;
	//			this.combinations = combinations;
	//		}
	//	}
	//
	//	public TableCombination[] getTableCombinations(Index[] indices) {
	//		Set tables = new HashSet();
	//		List tableCombinations = new ArrayList();
	//		for (int i = 0; i < indices.length; i++) {
	//			String table = indices[i].getTable();
	//			if (tables.add(table)) {
	//				List tableIndices = new ArrayList();
	//				for (int j = i; j < indices.length; j++) {
	//					Index idx = indices[j];
	//					if (table.equals(idx.getTable())) {
	//						tableIndices.add(idx);
	//					}
	//				}
	//				tableCombinations.add(
	//					new TableCombination(
	//						table,
	//						getCombinations((Index[]) tableIndices.toArray(new Index[tableIndices.size()]))));
	//			}
	//		}
	//		return (TableCombination[]) tableCombinations.toArray(new TableCombination[tableCombinations.size()]);
	//	}

	public Combination[] getCombinations(Index[] tableIndices) {
		Set fieldsSet = new HashSet();
		for (int i = 0; i < tableIndices.length; i++) {
			String[] indexFields = tableIndices[i].getFields();
			for (int j = 0; j < indexFields.length; j++) {
				fieldsSet.add(indexFields[j]);
			}
		}
		String[] fields = (String[]) fieldsSet.toArray(new String[fieldsSet.size()]);
		List combinations = new ArrayList();
		List curFields = new ArrayList();
		addCombinations(combinations, fields, tableIndices, curFields, 0);
		return (Combination[]) combinations.toArray(new Combination[combinations.size()]);
	}

	private void addCombinations(List combinations, String[] fields, Index[] tableIndices, List curFields, int i) {
		if (i == fields.length) {
			if (curFields.size() > 0) {
				Index[] solution =
					getBestCombination((String[]) curFields.toArray(new String[curFields.size()]), tableIndices);
				if (solution != null) {
					combinations.add(new Combination(getName(curFields), solution));
				}
			}
		} else {
			addCombinations(combinations, fields, tableIndices, curFields, i + 1);
			curFields.add(fields[i]);
			addCombinations(combinations, fields, tableIndices, curFields, i + 1);
			curFields.remove(curFields.size() - 1);
		}
	}

	private String getName(List fields) {
		fields = new ArrayList(fields);
		Collections.sort(fields);
		StringBuffer res = new StringBuffer();
		for (Iterator iFields = fields.iterator(); iFields.hasNext();) {
			res.append(iFields.next());
			res.append('|');
		}
		return res.toString();
	}

	private Index[] getBestCombination(String[] fields, Index[] tableIndices) {
		for (int i = 1; i < tableIndices.length; ++i) {
			Index[] used = new Index[i];
			Index[] solution = getBestCombination(fields, tableIndices, 0, used, 0);
			if (solution != null) {
				return solution;
			}
		}
		return null;
	}

	private Index[] getBestCombination(String[] fields, Index[] tableIndices, int numUsed, Index[] used, int cur) {
		Index[] solution = null;
		if (cur < tableIndices.length) {
			solution = getBestCombination(fields, tableIndices, numUsed, used, cur + 1);
			if (solution == null) {
				used[numUsed++] = tableIndices[cur];
				if (numUsed == used.length) {
					if (isSolution(fields, used)) {
						solution = used;
					}
				} else {
					solution = getBestCombination(fields, tableIndices, numUsed, used, cur + 1);
				}
			}
		}
		return solution;
	}

	private boolean isSolution(String[] fields, Index[] tableIndices) {
		boolean[] covered = new boolean[fields.length];
		int numCovered = 0;
		for (int i = 0; i < tableIndices.length; ++i) {
			String[] indexFields = tableIndices[i].getFields();
			for (int j = 0; j < indexFields.length; j++) {
				int idx = find(indexFields[j], fields);
				if (idx == -1) {
					break;
				} else if (!covered[idx]) {
					covered[idx] = true;
					++numCovered;
				}
			}
		}
		return numCovered == fields.length;
	}

	private int find(String key, String[] s) {
		for (int i = 0; i < s.length; i++) {
			if (key.equals(s[i])) {
				return i;
			}
		}
		return -1;
	}
}
