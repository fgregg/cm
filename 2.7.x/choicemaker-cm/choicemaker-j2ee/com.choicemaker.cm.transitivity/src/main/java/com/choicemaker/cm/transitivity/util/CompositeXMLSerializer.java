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
import java.util.Iterator;
import java.util.logging.Logger;

import com.choicemaker.cm.transitivity.core.CompositeEntity;
import com.choicemaker.cm.transitivity.core.TransitivityResult;

/**
 * This is an enhanced version of XMLSerializer.  It splits the output into several 
 * files each smaller than the given parameter.
 * 
 * @author pcheung
 *
 */
public class CompositeXMLSerializer extends XMLSerializer {

	private static final Logger log = Logger.getLogger(CompositeXMLSerializer.class.getName());

	//This defines how often to check the file size.
	private static final int INTERVAL = 2000;
	

	//This counts the number of files.
	private int currentFile;
	
	//This is the maximum size a file can have.
	private int maxFileSize;
	
	private String fileBase;
	private String fileExt;


	/** This constructor takes these parameters:
	 * 
	 * @param tr - Transitivity Result that has the graphs
	 * @param fileBase - base name for the output files
	 * @param fileExt - extension for the output files
	 * @param maxFileSize - the maximum size of each file.
	 */
	public CompositeXMLSerializer (TransitivityResult tr, String fileBase, 
		String fileExt, int maxFileSize) {
			
		currentFile = 1;
		
		this.result = tr;
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
		writeHeader ();
		
		int count = 0;
		
		Iterator it = result.getNodes();
		while (it.hasNext()) {
			StringBuffer sb = new StringBuffer ();
			
			CompositeEntity ce = (CompositeEntity) it.next();

			sb.append(writeCompositeEntity (ce));
			sb.append(NEW_LINE);
			writer.write(sb.toString());
			
			count ++;
			if (count % INTERVAL == 0) {
				writer.flush();
				if (FileUtils.isFull(fileBase, fileExt, currentFile, maxFileSize)) {
					writeFooter();
					writer.close();
					
					currentFile ++;
					writer = new FileWriter (
						FileUtils.getFileName(fileBase, fileExt, currentFile), false);
					writeHeader ();
				}
			}
		}
		
		writeFooter ();

		writer.flush();
		writer.close();
	}


}
