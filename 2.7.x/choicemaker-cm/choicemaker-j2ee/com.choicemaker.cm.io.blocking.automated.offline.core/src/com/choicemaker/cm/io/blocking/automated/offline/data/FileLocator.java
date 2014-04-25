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
package com.choicemaker.cm.io.blocking.automated.offline.data;

import java.io.FileWriter;
import java.io.IOException;

import com.choicemaker.cm.core.util.IntArrayList;

/**This object tells the system which data file contains the block data.
 *
 * For example:
 * min = 0, max = 30000, index 1
 * means that for blocks 0 to 30000, the data file is chunkdata1.txt
 *
 * @deprecated
 *
 * @author pcheung
 *
 */
public class FileLocator {
	int count = 0;
	IntArrayList min = new IntArrayList ();
	IntArrayList max = new IntArrayList ();
	IntArrayList index = new IntArrayList ();

	public FileLocator (){
	}


	/** This adds an entry to the FileLocator.
	 *
	 * @param small - smalled id
	 * @param large - largest id
	 * @param i - chunk number
	 */
	public void add (int small, int large, int i) {
		min.add(small);
		max.add(large);
		index.add(i);
		count ++;
	}

	public int getMin (int i) {
		return min.get(i);
	}

	public int getMax (int i) {
		return max.get(i);
	}

	public int getIndex (int i) {
		return index.get(i);
	}

	public int size () {
		return count;
	}


	/** This method checks to see if blockNum is in the ith chunk
	 *
	 * @param i
	 * @param blockNum
	 * @return boolean - true if this blockNum is in the ith chunk.
	 */
	public boolean isIn (int i, int blockNum) {
		return ((min.get(i) <= blockNum) && (blockNum <= max.get(i)));
	}


	/** This method writes the information out to a file.
	 *
	 */
	public void writeFileLocator (String fName) throws IOException{
		FileWriter fw;
		// 2014-04-24 rphall: Commented out unused local variable
//		String str;
//		int i1, i2, i3;
		fw = new FileWriter (fName, false);

		for (int i=0; i< this.count; i++) {
			fw.write(getMin(i) + " " + getMax(i) + " " + getIndex(i)+ "\r\n");
		}

		fw.close ();
	}

}
