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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.Constants;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;

/**
 * This object writes MatchRecord objects to file.
 * 
 * @author pcheung
 *
 */
public class MatchRecord2Sink extends BaseFileSink implements IMatchRecord2Sink {

	
	/** This creates a new match record sink.  By default it does not append to existing file, but
	 * overwrites it.
	 * 
	 * @param fileName
	 * @param type
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public MatchRecord2Sink (String fileName, int type) {
		init (fileName, type);
	}

	public void writeMatches (ArrayList matches) throws BlockingException {
		int size = matches.size();
		for (int i=0; i<size; i++) {
			MatchRecord2 match = (MatchRecord2) matches.get(i);
			writeMatch (match);
		}
	}
	
	
	public void writeMatches (Collection c) throws BlockingException {
		Iterator it = c.iterator();
		writeMatches (it);
	}


	public void writeMatches (Iterator it) throws BlockingException {
		while (it.hasNext()) {
			MatchRecord2 match = (MatchRecord2) it.next();
			writeMatch (match);
		}
	}
		
	
	public void writeMatch (MatchRecord2 match) throws BlockingException {
		try {
			if (type == Constants.STRING) {
				
				fw.write(getOutputString(match));
				
			} else if (type == Constants.BINARY) {
				int dataType = Constants.checkType(match.getRecordID1());
				dos.writeInt( dataType );
				writeID (match.getRecordID1(), dataType);

				dataType = Constants.checkType(match.getRecordID2());
				dos.writeInt( dataType );
				writeID (match.getRecordID2(), dataType);
				
				dos.writeFloat(match.getProbability());
				dos.writeChar(match.getMatchType());
				dos.writeChar(match.getRecord2Source());
				
				String str = match.getNotes();
				if (str == null || str.equals("")) {
					dos.writeInt(0);
				} else {
					dos.writeInt(str.length());
					dos.writeChars(str);
				}
			}
			count ++;
		} catch (IOException ex) {
			throw new BlockingException (ex.toString());
		}
	}
	
	
	/** This method returns a string representation of MatchRecord2 to get written to a FileWriter.
	 * 
	 * @param match
	 * @return
	 */
	public static String getOutputString (MatchRecord2 match) {
		StringBuffer sb = new StringBuffer ();
		int dataType = Constants.checkType(match.getRecordID1());
		sb.append(dataType);
		sb.append(' ');
		sb.append(match.getRecordID1().toString());
		sb.append(' ');

		dataType = Constants.checkType(match.getRecordID2());
		sb.append(dataType);
		sb.append(' ');
		sb.append(match.getRecordID2().toString());
		sb.append(' ');
				
		String str = match.getNotes();
		if (str == null) str =  "";
		else str = " " + str;

		sb.append(match.getProbability());
		sb.append(' ');
		sb.append(match.getMatchType());
		sb.append(' ');
		sb.append(match.getRecord2Source());
		sb.append(str);
		sb.append(Constants.LINE_SEPARATOR);
		
		return sb.toString();
	}
	

	private void writeID (Comparable c, int dataType) throws IOException {
		if (type == Constants.STRING) {
			if (dataType == Constants.TYPE_INTEGER) fw.write( c.toString() + " ");
			else if (dataType == Constants.TYPE_LONG) fw.write( c.toString() + " ");
			else if (dataType == Constants.TYPE_STRING) fw.write( c.toString() + " ");
		} else if (type == Constants.BINARY) {
			if (dataType == Constants.TYPE_INTEGER) dos.writeInt( ((Integer)c).intValue() ); 
			else if (dataType == Constants.TYPE_LONG) dos.writeLong( ((Long)c).longValue() ); 
			else if (dataType == Constants.TYPE_STRING) {
				String S = (String) c;
				dos.writeInt(S.length());
				dos.writeChars(S);
			} 
		}
	}

	
	/**
	 *  This is only for the text output file.  type == Constants.STRING is assumed.
	 * 
	 * @param c
	 * @param dataType
	 * @param sb
	 */
	private void writeID (Comparable c, int dataType, StringBuffer sb) {
		sb.append(c.toString());
		sb.append(' ');
	}


}
