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
package com.choicemaker.cm.io.blocking.automated.offline.utils;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecValSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecValSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecValSource;
import com.choicemaker.cm.io.blocking.automated.offlinelong.RecordValue2;
import com.choicemaker.util.IntArrayList;
import com.choicemaker.util.LongArrayList;

/**
 * This object trims the rec_id, val_id pair file by only keeping rec_id's that show up in
 * oversized blocks.
 * 
 * This version uses RecordValue2.
 * 
 * @author pcheung
 *
 */
public class RecordValueTrimmer2 {

	private LongArrayList osIDs;
	private IRecValSinkSourceFactory rvFactory;
	private IRecValSource rvSource;
	
	public RecordValueTrimmer2 (LongArrayList osIDs, IRecValSinkSourceFactory rvFactory, IRecValSource rvSource) {
		this.osIDs = osIDs;
		this.rvFactory = rvFactory;
		this.rvSource = rvSource;
	}
	
	
	public void trim () throws BlockingException {
		removeDups ();
		
		RecordValue2 recVal = new RecordValue2 (rvSource);

		IRecValSink sink = rvFactory.getSink(rvSource);
		sink.open();
		
		for (int i=0; i < osIDs.size(); i++) {
			IntArrayList list = (IntArrayList) recVal.get( osIDs.get(i));
			sink.writeRecordValue(osIDs.get(i), list);
		}
		
		sink.close();
	}
	
	
	//This method removes dups in osIDs after it is sorted.
	private void removeDups () {
		osIDs.sort();
		
		LongArrayList newList = new LongArrayList (100);
		
		long last = -1;
		for	(int i=0;i <osIDs.size(); i++) {
			if (osIDs.get(i) != last) {
				last = osIDs.get(i);
				newList.add(last);
			}
		}
		osIDs = newList;
	}

}
