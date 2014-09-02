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
package com.choicemaker.cm.transitivity.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import com.choicemaker.cm.transitivity.core.CompositeEntity;
import com.choicemaker.cm.transitivity.core.TransitivityResult;

/**
 * This object takes a TransitivityResult and a Writer and outputs the clusters as
 * RECORD_ID, MATCH_GROUP_ID, HOLD_GROUP_ID.  I can split the result into many files
 * in order to get around the Windows file size limit.
 * 
 * @author pcheung
 *
 */
public class CompositeTextSerializer extends TextSerializer {

	private static final Logger log = Logger.getLogger(CompositeTextSerializer.class.getName());
	
	//This counts the number of files.
	private int currentFile;
	
	//This is the maximum size a file can have.
	private int maxFileSize;
	
	//This defines how often to check the file size.
	private static final int INTERVAL = 50000;
	
	private String fileBase;
	private String fileExt;
	
	

	public CompositeTextSerializer (TransitivityResult tr, String fileBase, 
		String fileExt, int maxFileSize, int sortType) {
			
		currentFile = 1;
		
		this.result = tr;
		this.records = new ArrayList ();
		this.sortType = sortType;
		this.maxFileSize = maxFileSize;
		this.fileExt = fileExt;
		this.fileBase = fileBase;

		try {
			writer = new FileWriter (
				FileUtils.getFileName(fileBase, fileExt, currentFile), false);
		} catch (IOException e) {
			log.severe(e.toString());
		}
	}
	
	
	/** This method serializes the result to the writer.
	 * 
	 *
	 */
	public void serialize () throws IOException {
		//first get all the record IDs from the clusters.
		Iterator it = result.getNodes();
		while (it.hasNext()) {
			CompositeEntity ce = (CompositeEntity) it.next();
			
			getCompositeEntity (ce);
		}
		
		//second, sort them accordingly
		Object [] recs = handleSort ();
		
		//free memory
		records = null;
		
		//third, write them out.
		writeRecords (recs);
		
		writer.flush();
		writer.close();
		
		//free up memory
		recs = null;
	}



	private void writeRecords (Object [] recs) throws IOException {
		int s = recs.length;
		for (int i=0; i<s; i++) {
			Record r = (Record) recs[i];
			
			writer.write(printRecord(r));
			
			if (i % INTERVAL == 0) {
				writer.flush();
				if (FileUtils.isFull(fileBase, fileExt, currentFile, maxFileSize)) {
					writer.close();
					
					currentFile ++;
					writer = new FileWriter (
						FileUtils.getFileName(fileBase, fileExt, currentFile), false);
				}
			}
		} //end for
	
	}
	
	

}
