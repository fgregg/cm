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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.Constants;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecordSink;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord;

/**
 * This object writes MatchRecord objects to file.
 * 
 * @author pcheung
 *
 */
public class MatchRecordSink extends BaseFileSink implements IMatchRecordSink {

	
	/** This creates a new match record sink.  By default it does not append to existing file, but
	 * overwrites it.
	 * 
	 * @param fileName
	 * @param type
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public MatchRecordSink (String fileName, int type) {
		init (fileName, type);
	}

	public void writeMatches (ArrayList matches) throws BlockingException {
		for (int i=0; i<matches.size(); i++) {
			MatchRecord match = (MatchRecord) matches.get(i);
			writeMatch (match);
		}
	}
	
	
	public void writeMatches (Collection c) throws BlockingException {
		Iterator it = c.iterator();
		writeMatches (it);
	}


	public void writeMatches (Iterator it) throws BlockingException {
		while (it.hasNext()) {
			MatchRecord match = (MatchRecord) it.next();
			writeMatch (match);
		}
	}
		
	
	public void writeMatch (MatchRecord match) throws BlockingException {
		try {
			if (type == Constants.STRING) {
				fw.write( match.getRecordID1() + " " + match.getRecordID2() + " " + 
				match.getProbability() + " " + match.getMatchType() + " " + 
				match.getRecord2Source () + 
				Constants.LINE_SEPARATOR);
			} else if (type == Constants.BINARY) {
				dos.writeLong(match.getRecordID1());
				dos.writeLong(match.getRecordID2());
				dos.writeFloat(match.getProbability());
				dos.writeChar(match.getMatchType());
				dos.writeChar(match.getRecord2Source());
			}
			count ++;
		} catch (IOException ex) {
			throw new BlockingException (ex.toString());
		}
	}
	
	/**
	 * NOP for now
	 * @see com.choicemaker.cm.core.Sink#flush()
	 */
	public void flush() {
	}

}
