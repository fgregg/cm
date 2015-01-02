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
package com.choicemaker.cm.io.blocking.automated.offline.services;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEvent;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEventLog;
import com.choicemaker.cm.io.blocking.automated.offline.utils.RecordIDTranslator;

/**
 * @author pcheung
 *
 * This service reverse translates the internal system ID back to the original Record ID.
 */
public class ReverseTranslateService {

	private RecordIDTranslator translator;
	
	private IBlockSource bSource;
	private IBlockSink bSink;
	
	private IBlockSource osSource;
	private IBlockSink osSink;
	
	private OabaEventLog status;
	
	private long time; //this keeps track of time

	
	/** This constructor takes these parameters:
	 * 
	 * @param translator - this object contains the id mapping
	 * @param bSource - input block source with internal ids
	 * @param bSink - output block sink with record ids
	 * @param osSource - input oversized block source with internal ids
	 * @param osSink - output oversized block sink with record ids
	 */
	public ReverseTranslateService (RecordIDTranslator translator, 
		IBlockSource bSource, IBlockSink bSink,
		IBlockSource osSource, IBlockSink osSink, OabaEventLog status) {
			
		this.translator = translator;
		this.bSink = bSink;
		this.bSource = bSource;
		this.osSink = osSink;
		this.osSource = osSource;
		this.status = status;
		
	}
	
	
	/** This method returns the time it takes to run the runService method.
	 * 
	 * @return long - returns the time (in milliseconds) it took to run this service.
	 */
	public long getTimeElapsed () { return time; }


	/** This method runs the service.
	 * 
	 *
	 */
	public void runService () throws BlockingException {
		time = System.currentTimeMillis();

//		System.out.println ("runService: " + status.getStatus());

		if (status.getCurrentOabaEventId() >= OabaProcessing.EVT_DONE_REVERSE_TRANSLATE_OVERSIZED) {
			//do nothing
		} else if (status.getCurrentOabaEventId() == OabaProcessing.EVT_DONE_DEDUP_OVERSIZED) {
			//reverse translate block and oversized
			reverseTranslate (bSource, bSink);
			status.setCurrentOabaEvent( OabaEvent.DONE_REVERSE_TRANSLATE_BLOCK);

			reverseTranslate (osSource, osSink);
			status.setCurrentOabaEvent( OabaEvent.DONE_REVERSE_TRANSLATE_OVERSIZED);
			
		} else if (status.getCurrentOabaEventId() == OabaProcessing.EVT_DONE_REVERSE_TRANSLATE_BLOCK) {
			//reverse translate oversized
			reverseTranslate (osSource, osSink);
			status.setCurrentOabaEvent( OabaEvent.DONE_REVERSE_TRANSLATE_OVERSIZED);

		}
		time = System.currentTimeMillis() - time;
	}
	
	
	private void reverseTranslate (IBlockSource source, IBlockSink sink) throws BlockingException {
//		System.out.println ("translating from " + source.getInfo() + " to " + sink.getInfo());
		
		translator.reverseTranslate(source, sink);
		source.delete();
	}
	
	
}

