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

import java.io.EOFException;
import java.io.IOException;
import java.util.StringTokenizer;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.Constants;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMergeGroup;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMergeGroupSource;
import com.choicemaker.cm.io.blocking.automated.offline.data.MergeGroup;
import com.choicemaker.util.LongArrayList;

/**
 * @author pcheung
 * @deprecated
 */
public class MergeGroupSource extends BaseFileSource<IMergeGroup> implements
		IMergeGroupSource {
	
	private MergeGroup mg;

	public MergeGroupSource (String fileName, int type) {
		super.init(fileName, type);
	}

	public IMergeGroup next() throws BlockingException {
		return getNext();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IMergeGroupSource#getNext()
	 */
	public IMergeGroup getNext() throws BlockingException {
		return mg;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#hasNext()
	 */
	public boolean hasNext() throws BlockingException {
		boolean ret = false;

		try {
			String str = "";

			if (type == Constants.STRING) {
				str = br.readLine();
				if (str == null || str.equals("")) throw new EOFException ();
				long[] stage = parseArray(str);
				
				str = br.readLine();
				long[] master = parseArray(str);
				
				str = br.readLine();
				long[] stageHold = parseArray(str);
				
				str = br.readLine();
				long[] masterHold = parseArray(str);
				
				str = br.readLine();
				boolean isHold = false;
				if (str.indexOf('T') >= 0) isHold = true;
				
				mg = new MergeGroup (stage, master, stageHold, masterHold, isHold);

			} else if (type == Constants.BINARY) {
				int size = dis.readInt();
				long[] stage = parseArray (size);
				
				size = dis.readInt();
				long[] master = parseArray (size);
				
				size = dis.readInt();
				long[] stageHold = parseArray (size);
				
				size = dis.readInt();
				long[] masterHold = parseArray (size);
				
				boolean isHold = dis.readBoolean();
				
				mg = new MergeGroup (stage, master, stageHold, masterHold, isHold);
			}
			
			ret = true;
			count ++;

		} catch (EOFException ex) {
			//do nothing
		} catch (IOException ex1) {
			throw new BlockingException (ex1.toString());
		}
		
		return ret;
	}
	
	
	private long [] parseArray (int size) throws IOException {
		LongArrayList list = new LongArrayList ();
		for (int i=0; i<size; i++) {
			list.add( dis.readLong());
		}
		return list.toArray();
	}
	
	
	private long [] parseArray (String str) {
		StringTokenizer st = new StringTokenizer (str);
		LongArrayList list = new LongArrayList ();
		
		while (st.hasMoreTokens()) {
			list.add( Long.parseLong(st.nextToken()) );
		}
		
		return list.toArray();
	}

}
