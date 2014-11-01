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

import java.io.IOException;
import java.io.Serializable;

import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;

/**
 * This class used to store the current processing stage of the OABA in a file,
 * which was used if a job need to be recovered.
 * @deprecated Store status in the database
 */
@Deprecated
public class Status implements Serializable, OabaProcessing {

	static final long serialVersionUID = 271;

	private int currentEvent;

	private String info;

	/** This sets the current event.
	 * 
	 * @param stat
	 * @throws IOException
	 */
	public void setCurrentProcessingEvent (int event) {
		setCurrentProcessingEvent(event, null);
	}

	public void setCurrentProcessingEvent (int event, String info) {
		currentEvent = event;
		this.info = info;
		writeStatus (info);
	}

	/** This gets the current status.
	 * 
	 * @return int - returns the current status code.
	 */
	public int getCurrentProcessingEvent () {
		return currentEvent;
	}
	
	public String toString () {
		return Integer.toString(currentEvent);
	}

	/** This method returns any additional info that is associated with this status.
	 * 
	 * @return String - This contains additional info associated with this status.
	 */
	public String getAdditionalInfo () {
		return info;
	}

	private void writeStatus (String info) {
		throw new Error("no longer implemented -- store status in the database");
	}
	
}
