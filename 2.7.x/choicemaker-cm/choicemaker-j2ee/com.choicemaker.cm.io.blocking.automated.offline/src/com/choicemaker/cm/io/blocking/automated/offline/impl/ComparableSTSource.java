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
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.ISuffixTreeSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.PairID;
import com.choicemaker.cm.io.blocking.automated.offline.core.SuffixTreeNode;
import com.choicemaker.cm.io.blocking.automated.offline.utils.SuffixTreeUtils;

/**
 * This is a wrapper object on SuffixTreeSource to make it look like IComparableSource.
 * It reads a suffix tree and build an array of PairID.
 * 
 * @author pcheung
 *
 */
public class ComparableSTSource implements IComparableSource {

	private ISuffixTreeSource source;
	private int count = 0;
	private ArrayList pairs = null;
	private int ind = 0;
	
	
	public ComparableSTSource (ISuffixTreeSource source) {
		this.source = source;
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSource#getNext()
	 */
	public Comparable getNext() throws BlockingException {
		PairID p = (PairID) pairs.get(ind);
		ind++;
		return p;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSource#getCount()
	 */
	public int getCount() {
		return count;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#exists()
	 */
	public boolean exists() {
		return source.exists();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#open()
	 */
	public void open() throws BlockingException {
		source.open();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#hasNext()
	 */
	public boolean hasNext() throws BlockingException {
		boolean ret = false;
		
		if (pairs == null) {
			if (source.hasNext()) {
				SuffixTreeNode root = source.getNext();
				pairs = SuffixTreeUtils.getPairs (root);
				ret = true;
				ind = 0;
			}
		} else {
			if (ind < pairs.size()) ret = true;
			else {
				//get the next tree
				if (source.hasNext()) {
					SuffixTreeNode root = source.getNext();
					pairs = SuffixTreeUtils.getPairs (root);
					ret = true;
					ind = 0;
				}
			}
		}
		
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#close()
	 */
	public void close() throws BlockingException {
		source.close();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#getInfo()
	 */
	public String getInfo() {
		return source.getInfo();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#remove()
	 */
	public void remove() throws BlockingException {
		source.remove();
	}

}
