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

import java.io.IOException;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.ComparisonTreeNode;
import com.choicemaker.cm.io.blocking.automated.offline.core.Constants;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSink;

/**
 * This is a file implementation of a sink to write ComparisonTreeNode.
 * 
 * @author pcheung
 *
 */
@SuppressWarnings({"rawtypes"})
public class ComparisonTreeSink extends BaseFileSink implements IComparisonTreeSink {

	/** This constructor creates a String ComparisonTreeSink with the given file name.
	 * 
	 * @param fileName
	 */
	public ComparisonTreeSink (String fileName) {
		init (fileName, Constants.STRING);
	}
	

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSink#writeComparisonTree(com.choicemaker.cm.io.blocking.automated.offline.core.ComparisonTreeNode)
	 */
	public void writeComparisonTree(ComparisonTreeNode tree) throws BlockingException {
		try {
			if (type == Constants.BINARY) {
				throw new BlockingException ("BINARY format of SuffixTreeSink is not yet supported");
			} else if (type == Constants.STRING) {
				StringBuffer sb = new StringBuffer ();
				tree.writeTree2(sb);
				sb.append(Constants.LINE_SEPARATOR);
				fw.write(sb.toString());
			}
			
			count ++;
						
		} catch (IOException ex) {
			throw new BlockingException (ex.toString());
		}

	}

}
