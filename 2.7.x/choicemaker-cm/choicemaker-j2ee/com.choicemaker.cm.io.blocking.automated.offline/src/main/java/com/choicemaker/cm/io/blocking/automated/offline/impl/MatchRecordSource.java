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

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.EXTERNAL_DATA_FORMAT;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecordSource;
import com.choicemaker.cm.io.blocking.automated.offline.data.IMatchRecord;


/**
 * This object handles reading MatchRecord objects from a file.
 * 
 * @author pcheung
 *
 */
public class MatchRecordSource extends BaseFileSource<IMatchRecord> implements IMatchRecordSource {

	private IMatchRecord next;

	@Deprecated
	public MatchRecordSource (String fileName, int type) {
		super(fileName, EXTERNAL_DATA_FORMAT.fromSymbol(type));
	}
	
	public MatchRecordSource(String fileName, EXTERNAL_DATA_FORMAT type) {
		super(fileName, type);
	}

	
	/** Always call hasNext before calling getNext ().
	 * 
	 * @return boolean - true if there are more elements in the source.
	 */ 
	public boolean hasNext () throws BlockingException {
		boolean ret = false;
		String str = "";
		
		try {
			if (type == EXTERNAL_DATA_FORMAT.STRING) {
				str = br.readLine();
				
				if ((str != null) && (str.length() > 0)) {
					int i = str.indexOf(' ');
					String temp = str.substring(0, i);
					long i1 = Long.parseLong(temp);
				
					int j = str.indexOf(' ',i+1);
					temp = str.substring(i+1,j);
					long i2 = Long.parseLong(temp);
				
					i  = str.indexOf(' ',j+1);
					temp = str.substring(j+1,i);
					float f = Float.parseFloat(temp);
				
					j = str.indexOf(' ',i+1);
					temp = str.substring(i+1,j);
					char tt = temp.charAt(0);

					temp = str.substring(j+1);
					char source = temp.charAt(0);

					next = new MatchRecord (i1, i2, source, f, tt);
					count ++;
					ret = true;
				}

			} else if (type == EXTERNAL_DATA_FORMAT.BINARY) {

				long i1 = dis.readLong();
				long i2 = dis.readLong();
				float f = dis.readFloat();
				char type = dis.readChar();
				char source = dis.readChar();
		
				next = new MatchRecord (i1, i2, source, f, type);
				count ++;
				ret = true;
			}

		} catch (EOFException ex) {
			//
		} catch (IOException ex1) {
			throw new BlockingException (ex1.toString());
		}
		
		return ret;
	}
	
	public IMatchRecord next() {
		return next;
	}
	
	
	public int getCount () {
		return count;
	}


	
}
