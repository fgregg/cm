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
import com.choicemaker.cm.io.blocking.automated.offline.core.BlockSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.Constants;
import com.choicemaker.cm.io.blocking.automated.offline.core.EXTERNAL_DATA_FORMAT;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSink;
import com.choicemaker.util.IntArrayList;
import com.choicemaker.util.LongArrayList;

/**
 * @author pcheung
 *
 */
public class BlockSink extends BaseFileSink implements IBlockSink {

	@Deprecated
	public BlockSink (String fileName, int type) {
		super(fileName, EXTERNAL_DATA_FORMAT.fromSymbol(type));
	}

	public BlockSink(String fileName, EXTERNAL_DATA_FORMAT type) {
		super(fileName, type);
	}

	public void writeBlock (BlockSet bs) throws BlockingException {
		try {
			if (type == EXTERNAL_DATA_FORMAT.BINARY) {

				IntArrayList columns = bs.getColumns();
				int s = columns.size();
				//first write size of block values
				dos.writeInt(s);

				//second write the blocking field			
				for (int i=0; i< s; i++) {
					dos.writeInt(columns.get(i) );
				}
			
				LongArrayList list = bs.getRecordIDs();
				//third write the size of record ids
				dos.writeInt(list.size());

				//fourth write the ids			
				for (int i=0; i< list.size(); i++) {
					dos.writeLong( list.get(i));
				}

			} else if (type == EXTERNAL_DATA_FORMAT.STRING) {
				StringBuffer sb = new StringBuffer ();
				IntArrayList columns = bs.getColumns();
				int s = columns.size();
				for (int i=0; i< s; i++) {
					sb.append(columns.get(i));
					sb.append(' ');
				}
				sb.append(Constants.LINE_SEPARATOR);
			
				LongArrayList list = bs.getRecordIDs();
			
				for (int i=0; i< list.size(); i++) {
					sb.append( list.get(i) );
					sb.append(' ');
				}
				sb.append(Constants.LINE_SEPARATOR);

				fw.write(sb.toString ());

/*
				LinkedList al = bs.getBlockValues();
				for (int i=0; i< al.size(); i++) {
					BlockValue bv = (BlockValue) al.get(i);
					fw.write( Integer.toString(bv.getColumnID()) );
					fw.write(" ");
				}
				fw.write(Constants.LINE_SEPARATOR);
			
				LongArrayList list = bs.getRecordIDs();
			
				for (int i=0; i< list.size(); i++) {
					fw.write( Long.toString(list.get(i)) );
					fw.write(" ");
				}
				fw.write(Constants.LINE_SEPARATOR);
*/
			}
			
			count ++;
		
		} catch (IOException ex) {
			throw new BlockingException (ex.toString());
		}
		
	}


	@Override
	public String toString() {
		return "BlockSink [count=" + count + ", type=" + type + ", fileName="
				+ fileName + "]";
	}
	
	

}
