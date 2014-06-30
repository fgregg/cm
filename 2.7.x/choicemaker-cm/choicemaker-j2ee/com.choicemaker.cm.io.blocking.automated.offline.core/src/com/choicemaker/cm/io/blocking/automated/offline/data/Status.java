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
package com.choicemaker.cm.io.blocking.automated.offline.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

import com.choicemaker.cm.core.base.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IStatus;

/**
 * This object stores the current status of the OABA and it is used for recovery.  This implementation
 * saves the info to a file.
 * 
 * @author pcheung
 *
 */
public class Status implements Serializable, IStatus{

	/* As of 2010-03-10 */
	static final long serialVersionUID = 7299535426344879222L;

	private int currentStatus;
	
	private String fileName;
	
	//This stores what extra info
	private String info;
	
	
	/** This constructor reads in the status from the file and then erases the old file.
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	public Status (String fileName) throws BlockingException {
		this.fileName = fileName;
		readStatus ();

		File file = new File (fileName);
		if (file.exists()) file.delete();
	}
	
	
	/** This sets the current status and writes it to file.
	 * 
	 * @param stat
	 * @throws IOException
	 */
	public void setStatus (int stat) throws BlockingException{
		currentStatus = stat;
		writeStatus ();
		
//		System.out.println ("Setting to " + stat);
	}


	public void setStatus (int stat, String info) throws BlockingException {
		currentStatus = stat;
		this.info = info;
		writeStatus (info);
	}

	
	/** This gets the current status.
	 * 
	 * @return int - returns the current status code.
	 */
	public int getStatus () throws BlockingException {
		return currentStatus;
	}
	
	public String toString () {
		return Integer.toString(currentStatus);
	}
	
	
	/** This method returns any additional info that is associated with this status.
	 * 
	 * @return String - This contains additional info associated with this status.
	 */
	public String getAdditionalInfo () throws BlockingException {
		return info;
	}


	/** This method reads the status from file.
	 * 
	 * @param stat - current status
	 * @throws IOException
	 */
	private void readStatus () throws BlockingException {
		try {
			File file = new File (fileName);
			boolean exists = file.exists();
			if (!exists) {
				currentStatus =  Status.INIT;
			} else {
				BufferedReader br = new BufferedReader (new FileReader(fileName));

				String line = br.readLine();
				parse(line);
			
				if (line == null) currentStatus = Status.INIT;
		
				while (line != null && !line.equals("")) {
					line = br.readLine();	
					parse (line);
				}
		
				br.close();
			
				if (currentStatus == Status.DONE_PROGRAM) currentStatus = Status.INIT;
			}
		} catch (IOException ex) {
			throw new BlockingException (ex.toString());
		}
	}
	
	
	private void parse (String line) {
		if (line != null && !line.equals("")) {
			int ind = line.indexOf(Status.DELIMIT);
			
			if (ind > -1) {
				currentStatus = Integer.parseInt( line.substring(0,ind) );
				info = line.substring(ind + 1);
			} else {
				currentStatus = Integer.parseInt( line );
			}
		} 
	}
	
	
	/** This method writes the status to file.
	 * 
	 * @param stat - current status
	 * @throws IOException
	 */
	private void writeStatus () throws BlockingException {
		try {
			FileWriter fw = new FileWriter (fileName, true);
			fw.write(this.toString() + "\n");
			fw.close();
		} catch (IOException ex) {
			throw new BlockingException (ex.toString());
		}
	}
	

	/** This method writes the status and additional info to file.
	 * 
	 * @param stat - current status
	 * @throws IOException
	 */
	private void writeStatus (String info) throws BlockingException {
		try {
			FileWriter fw = new FileWriter (fileName, true);
			fw.write(this.toString() + Status.DELIMIT + info + "\n");
			fw.close();
		} catch (IOException ex) {
			throw new BlockingException (ex.toString());
		}
	}
	
}
