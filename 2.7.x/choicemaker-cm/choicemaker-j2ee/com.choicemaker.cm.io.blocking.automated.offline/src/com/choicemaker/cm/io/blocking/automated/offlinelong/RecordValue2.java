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
package com.choicemaker.cm.io.blocking.automated.offlinelong;

import java.util.ArrayList;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.util.IntArrayList;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecValSource;

/**This object takes a IRecValSource and creates a ArrayList storing the record ids and
 * IntArrayList of value ids.
 *
 * The critical change to this code is that it uses IntArrayList instead of Integer
 * as map value and ArrayList value to handle stacking.
 *
 * This version uses the input record id to internal translator, therefor we could use an
 * array instead of hashMap;
 *
 * @author pcheung
 *
 */
public class RecordValue2 {

//	private static final Logger log = Logger.getLogger(RecordValue2.class);

	private ArrayList recordList = null;

	public RecordValue2 (IRecValSource rvSource) throws BlockingException{
		recordList = readColumnList(rvSource);
	}



	public Object get (int ind) {
		Object o = null;
		if (ind >= 0 && ind < recordList.size()) o = recordList.get(ind);
		return o;
	}

	public Object get (long ind) {
		return get ((int) ind);
	}

	public ArrayList getList () {
		return recordList;
	}


	public int size () {
		int s = 0;
		s = recordList.size();
		return s;
	}


	/** This method reads the ArrayList from a binary file.
	 * This assumes that the record ids coming in from the file is sorted.
	 *
	 * @param fileName
	 * @return ArrayList - array containing rec_id, val_id pairs.
	 */
	public static ArrayList readColumnList (IRecValSource rvSource) throws BlockingException{
		ArrayList list = new ArrayList (1000);

		// 2014-04-24 rphall: Commented out unused local variables.
//		int count = 0;
//		int padCount = 0;
		int current = 0;

		rvSource.open();

		while (rvSource.hasNext()) {
//			count ++;

			int row = (int) rvSource.getNextRecID();
			IntArrayList values = rvSource.getNextValues();

			//pad the array list with null;
			if (row > current) {
				for (int i=current; i<row; i++) {
//					padCount ++;
					list.add(null);
					current ++;
				}
			}

			list.add(values);

			current ++;
		}

		rvSource.close();

//		System.out.println ("reading count " + count + " padCount " + padCount);

		return list;
	}




}
