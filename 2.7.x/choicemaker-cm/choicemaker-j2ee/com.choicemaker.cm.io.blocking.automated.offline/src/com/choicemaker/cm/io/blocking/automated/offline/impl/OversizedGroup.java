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
package com.choicemaker.cm.io.blocking.automated.offline.impl;

import java.util.ArrayList;

import com.choicemaker.cm.core.base.BlockingException;
import com.choicemaker.cm.core.util.IntArrayList;
import com.choicemaker.cm.io.blocking.automated.offline.core.BlockSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IOversizedGroup;

/**
 * @author pcheung
 *
 * This object hides the detail list of oversized sinks from rest of the program and treats them as one.
 * It breaks the oversized blocks into files with the same max column number.
 */
public class OversizedGroup implements IOversizedGroup {

	int numColumn;
	IBlockSinkSourceFactory bFactory;
	ArrayList sinks;


	public OversizedGroup (int numColumn, IBlockSinkSourceFactory bFactory) throws BlockingException {
		this.numColumn = numColumn;
		this.bFactory = bFactory;

		sinks = new ArrayList (numColumn);

		for (int i=0; i<numColumn; i++) {
			IBlockSink sink = bFactory.getNextSink();
			sinks.add(sink);
		}
	}


	/** This method finds the maximum column id of the block set.
	 *
	 * @param bs
	 * @return
	 */
	private int findMax (BlockSet bs) {
		IntArrayList columns = bs.getColumns();
		// 2014-04-24 rphall: Commented out unused local variable.
//		int s = columns.size();

		//columns are sorted so just return the last one.
		return columns.get(columns.size() - 1);
	}


	public void writeBlock(BlockSet bs) throws BlockingException {
		IBlockSink sink = (IBlockSink) sinks.get(findMax(bs));
		sink.writeBlock(bs);

	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IOversizedGroup#IBlockSource(int)
	 */
	public IBlockSource getSource (int maxColumn) throws BlockingException {
		return bFactory.getSource((IBlockSink) sinks.get(maxColumn));
	}


	/* Initializes and opens all the sinks
	 */
	public void openAllSinks() throws BlockingException {
		for (int i=0; i<numColumn; i++) {
			IBlockSink sink = (IBlockSink) sinks.get(i);
			sink.open();
//			System.out.println ("Open " + sink.getInfo());
		}

	}


	/* Initializes and opens all the sinks for append
	 */
	public void appendAllSinks() throws BlockingException {
		for (int i=0; i<numColumn; i++) {
			IBlockSink sink = (IBlockSink) sinks.get(i);
			sink.append();
//			System.out.println ("Append " + sink.getInfo());
		}

	}


	/* closes all the sinks
	 */
	public void closeAllSinks() throws BlockingException {
		for (int i=0; i<numColumn; i++) {
			IBlockSink sink = (IBlockSink) sinks.get(i);
			sink.close();
		}
	}



	public void cleanUp () throws BlockingException {
		for (int i=0; i<numColumn; i++) {
			IBlockSink sink = (IBlockSink) sinks.get(i);
			bFactory.removeSink(sink);

//			System.out.println ("Remove " + sink.getInfo());
		}

	}

}
