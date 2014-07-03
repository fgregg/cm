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

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.naming.NamingException;

import com.choicemaker.cm.io.blocking.automated.offline.server.data.MatchWriterData;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.MatchDedupEach;

/**
 * This version of deddup single processor match file is for the Transitivity Engine.
 * 
 * @author pcheung
 *
 */
public class TransMatchDedupEach extends MatchDedupEach {

	private static final long serialVersionUID = 1L;
//	private static final Logger log = Logger.getLogger(TransMatchDedupEach.class);


	protected void sendToMatchDedupOABA2 (MatchWriterData d) throws NamingException, JMSException {
		Queue queue = configuration.getTransMatchDedupMessageQueue();
		configuration.sendMessage(queue, d);
	} 

}
