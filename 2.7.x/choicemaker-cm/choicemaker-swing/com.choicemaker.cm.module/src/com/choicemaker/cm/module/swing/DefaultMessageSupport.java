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
package com.choicemaker.cm.module.swing;

import java.util.ResourceBundle;

import com.choicemaker.cm.core.util.ChoiceMakerCoreMessages;
import com.choicemaker.cm.core.util.MessageUtil;
import com.choicemaker.cm.module.IMessageSupport;

/**
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/27 19:27:57 $
 */
public class DefaultMessageSupport implements IMessageSupport {
	
	private MessageUtil choiceMakerCoreMessages;

	/**
	 * Retrieves messages from the ChoiceMaker resource bundle
	 */
	public DefaultMessageSupport() {
		this.choiceMakerCoreMessages = ChoiceMakerCoreMessages.m;
	}
	
	/**
	 * Retrieves messages from a specified resource bundle
	 */
	public DefaultMessageSupport(ResourceBundle resourceBundle) {
		this.choiceMakerCoreMessages = new ChoiceMakerCoreMessages(resourceBundle);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.module.IMessageSupport#formatMessage(java.lang.String)
	 */
	public String formatMessage(String messageKey) {
		return this.choiceMakerCoreMessages.formatMessage(messageKey);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.module.IMessageSupport#formatMessage(java.lang.String, java.lang.Object[])
	 */
	public String formatMessage(String messageKey, Object[] args) {
		return this.choiceMakerCoreMessages.formatMessage(messageKey,args);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.module.IMessageSupport#formatMessage(java.lang.String, java.lang.Object)
	 */
	public String formatMessage(String messageKey, Object arg0) {
		return this.choiceMakerCoreMessages.formatMessage(messageKey,arg0);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.module.IMessageSupport#formatMessage(java.lang.String, java.lang.Object, java.lang.Object)
	 */
	public String formatMessage(String messageKey, Object arg0, Object arg1) {
		return this.choiceMakerCoreMessages.formatMessage(messageKey,arg0,arg1);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.module.IMessageSupport#formatMessage(java.lang.String, java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	public String formatMessage(
		String messageKey,
		Object arg0,
		Object arg1,
		Object arg2) {
		return this.choiceMakerCoreMessages.formatMessage(messageKey,arg0,arg1,arg2);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.module.IMessageSupport#formatMessage(java.lang.String, java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	public String formatMessage(
		String messageKey,
		Object arg0,
		Object arg1,
		Object arg2,
		Object arg3) {
		return this.choiceMakerCoreMessages.formatMessage(messageKey,arg0,arg1,arg2,arg3);
	}

}

