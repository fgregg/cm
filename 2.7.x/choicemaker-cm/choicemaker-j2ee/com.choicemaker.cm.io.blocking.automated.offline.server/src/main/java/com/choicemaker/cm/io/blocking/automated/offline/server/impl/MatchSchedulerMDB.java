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
package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * This MDB delegates message handling to a singleton EJB, which tracks
 * certain OABA-related data between invocations of <code>onMessage</code>.
 * 
 * @author rphall
 */
@MessageDriven(
		activationConfig = {
				@ActivationConfigProperty(propertyName = "maxSession",
						propertyValue = "1"), // Singleton (JBoss only)
				@ActivationConfigProperty(
						propertyName = "destinationLookup",
						propertyValue = "java:/choicemaker/urm/jms/matchSchedulerQueue"),
				@ActivationConfigProperty(propertyName = "destinationType",
						propertyValue = "javax.jms.Queue") })
public class MatchSchedulerMDB implements MessageListener {

	@EJB
	private MatchSchedulerSingleton singleton;

	@Override
	public void onMessage(Message message) {
		singleton.onMessage(message);
	}

}
