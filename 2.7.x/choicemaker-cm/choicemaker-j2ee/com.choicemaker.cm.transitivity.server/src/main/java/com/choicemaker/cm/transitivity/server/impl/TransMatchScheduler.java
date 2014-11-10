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
package com.choicemaker.cm.transitivity.server.impl;

import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.naming.NamingException;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkDataSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySource;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonArrayGroupSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.StartData;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.UpdateData;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.MatchScheduler2;

/**
 * This is the match scheduler for the Transitivity Engine.
 * 
 * @author pcheung
 *
 */
@SuppressWarnings({"rawtypes"})
public class TransMatchScheduler extends MatchScheduler2 {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(TransMatchScheduler.class.getName());


	/** This method is called when all the chunks are done.
	 * 
	 */
	protected void nextSteps () 
		throws XmlConfException, BlockingException, NamingException, JMSException {
			
		cleanUp ();
						
		//update status
		sendToUpdateStatus (data.jobID, 90);

		//call matchdedup
		sendToMatchDebup (data);
	}


	/**
	 * This method cleans up the chunk files.
	 *
	 */
	protected void cleanUp () throws XmlConfException, BlockingException {
		log.fine("cleanUp");

		ImmutableProbabilityModel stageModel = PMManager.getModelInstance(data.modelConfigurationName);				
		//get the number of processors
		String temp = (String) stageModel.properties().get("numProcessors");
		int numProcessors = Integer.parseInt(temp);

		//remove the data
		IChunkDataSinkSourceFactory stageFactory = oabaConfig.getStageDataFactory();
		IChunkDataSinkSourceFactory masterFactory= oabaConfig.getMasterDataFactory();
		stageFactory.removeAllSinks(data.numChunks);
		masterFactory.removeAllSinks(data.numChunks);
		
		//oversized
		ComparisonArrayGroupSinkSourceFactory factoryOS =
			oabaConfig.getComparisonArrayGroupFactoryOS(numProcessors);
		
		//there is always 1 chunk to remove.
		int c = data.numChunks;
		if (c == 0) c = 1;
		
		for (int i=0; i<c; i++) {
			for (int j=1; j<=numProcessors; j++) {
				IComparisonArraySource sourceOS = factoryOS.getSource(i, j);
				sourceOS.remove();
				log.fine("removing " + sourceOS.getInfo());
			}
		}
	}


	
	protected void sendToMatchDebup (StartData data) throws NamingException, JMSException{
		Queue queue = configuration.getTransMatchDedupMessageQueue();
		configuration.sendMessage(queue, data);
	} 

	/** This method sends the message to the matcher bean.
	 * 
	 * @param data
	 * @throws NamingException
	 */
	protected void sendToMatcher (StartData sd) throws NamingException, JMSException{
		Queue queue = configuration.getTransMatcherMessageQueue();
		
		log.fine(" Sending chunkId " + sd.ind + " treeId " + sd.treeInd + " to " + queue.getQueueName());
		configuration.sendMessage(queue, sd);
	} 


	/** This method sends a message to the UpdateStatus message bean.
	 * 
	 * @param jobID
	 * @param percentComplete
	 * @throws NamingException
	 */
	protected void sendToUpdateStatus (long jobID, int percentComplete) throws NamingException, JMSException {
		Queue queue = configuration.getUpdateTransMessageQueue();
		UpdateData data = new UpdateData(jobID, percentComplete);
		log.info ("send to updateTransQueue " + jobID + " " + percentComplete);
		configuration.sendMessage(queue, data);
	} 

}
