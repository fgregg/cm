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

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.ComparisonArray;
import com.choicemaker.cm.io.blocking.automated.offline.core.Constants;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySink;

/**
 * This is a file implementation of IComparisonArraySink.
 * 
 * @author pcheung
 *
 */
public class ComparisonArraySink extends BaseFileSink implements IComparisonArraySink {
	
	public ComparisonArraySink (String fileName, int type) {
		init (fileName, type);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonGroupSink#writeComparisonGroup(com.choicemaker.cm.io.blocking.automated.offline.core.ComparisonGroup)
	 */
	public void writeComparisonArray(ComparisonArray cg) throws BlockingException {
		try {
			if (cg.getStagingIDsType() == 0) throw new BlockingException ("stage id type not set");
			if ((cg.getMasterIDsType() == 0) && (cg.getMasterIDs().size() > 0)) throw new BlockingException ("master id type not set");
			
			
			if (type == Constants.BINARY) {
				//first write the id type of staging IDs
				dos.writeInt(cg.getStagingIDsType());
				
				//second write the stagingIDs list.
				writeArray (cg.getStagingIDs(), cg.getStagingIDsType());
				
				//third write the master id type
				dos.writeInt(cg.getMasterIDsType());
				
				//fourth write the master list
				writeArray (cg.getMasterIDs(), cg.getMasterIDsType());
				
			} else if (type == Constants.STRING) {
				//first write the id type of staging IDs
				fw.write (Integer.toString(cg.getStagingIDsType()) + Constants.LINE_SEPARATOR);

				//second write the stagingIDs list.
				writeArray (cg.getStagingIDs(), cg.getStagingIDsType());
				
				//third write the master id type
				fw.write (Integer.toString(cg.getMasterIDsType()) + Constants.LINE_SEPARATOR);
				
				//fourth write the master list
				writeArray (cg.getMasterIDs(), cg.getMasterIDsType());
				
			}
			
			count ++;
		
		} catch (IOException ex) {
			throw new BlockingException (ex.toString());
		}
	}
	
	
	private void writeArray (ArrayList list, int dataType) throws IOException {

		if (type == Constants.BINARY) {
			//first write the size
			dos.writeInt(list.size());
		
			//then write each element
			for (int i=0; i<list.size(); i++) {
				if (dataType == Constants.TYPE_INTEGER) {
					dos.writeInt( ((Integer) list.get(i)).intValue() );
				
				} else if (dataType == Constants.TYPE_LONG) {
					dos.writeLong( ((Long) list.get(i)).longValue() );

				} else if (dataType == Constants.TYPE_STRING) {
					String s = (String) list.get(i);
					dos.writeInt(s.length());
					dos.writeChars(s);
				}
			}
			
		} else if (type == Constants.STRING) {
			//first write the size
			fw.write (Integer.toString(list.size()) + Constants.LINE_SEPARATOR);

		
			//then write each element
			for (int i=0; i<list.size(); i++) {
				if (dataType == Constants.TYPE_INTEGER) {
					fw.write (((Integer) list.get(i)).toString() + Constants.LINE_SEPARATOR);
				
				} else if (dataType == Constants.TYPE_LONG) {
					fw.write ( ((Long) list.get(i)).toString() + Constants.LINE_SEPARATOR);

				} else if (dataType == Constants.TYPE_STRING) {
					String s = (String) list.get(i);
					fw.write ( s + Constants.LINE_SEPARATOR);
				}
			}

/*
			for (int i=0; i<list.size(); i++) {
				if (dataType == Constants.TYPE_INTEGER) {
					fw.write (((Integer) list.get(i)).toString() + " ");
				
				} else if (dataType == Constants.TYPE_LONG) {
					fw.write ( ((Long) list.get(i)).toString() + " ");

				} else if (dataType == Constants.TYPE_STRING) {
					String s = (String) list.get(i);
					fw.write ( s + " ");
				}
			}
			fw.write (Constants.LINE_SEPARATOR);
*/
			
		} //end if type
	}
	

}
