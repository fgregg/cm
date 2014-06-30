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
package com.choicemaker.cm.core.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

/**
 * Description
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/27 21:21:01 $
 */

public class MessageUtil {

	private static Logger logger = Logger.getLogger(MessageUtil.class);
	private static Object[] ZERO_LENGTH_ARRAY = new Object[0];

	public static MessageUtil m = new MessageUtil("com.choicemaker.cm.core.util.res.ChoiceMaker");

	private ResourceBundle myResources;

	public MessageUtil(String name) {
		myResources = ResourceBundle.getBundle(name);
	}

	public MessageUtil(ResourceBundle myResources) {
		this.myResources = myResources;
	}

	private String getMessageString(String messageKey) {
		try {
			return myResources.getString(messageKey);
		} catch (MissingResourceException ex) {
			logger.error("missing resource in locale: " + Locale.getDefault(), ex);
			return "Missing resource: " + messageKey;
		}
	}

	public String formatMessage(String messageKey) {
		MessageFormat mf = new MessageFormat(getMessageString(messageKey));
		return mf.format(ZERO_LENGTH_ARRAY);
	}

	public String formatMessage(String messageKey, Object[] args) {
		MessageFormat mf = new MessageFormat(getMessageString(messageKey));
		return mf.format(args);
	}

	public String formatMessage(String messageKey, Object arg0) {
		MessageFormat mf = new MessageFormat(getMessageString(messageKey));
		Object[] args = new Object[1];
		args[0] = arg0;
		return mf.format(args);
	}

	public String formatMessage(String messageKey, Object arg0, Object arg1) {
		MessageFormat mf = new MessageFormat(getMessageString(messageKey));
		Object[] args = new Object[2];
		args[0] = arg0;
		args[1] = arg1;
		return mf.format(args);
	}

	public String formatMessage(String messageKey, Object arg0, Object arg1, Object arg2) {
		MessageFormat mf = new MessageFormat(getMessageString(messageKey));
		Object[] args = new Object[3];
		args[0] = arg0;
		args[1] = arg1;
		args[2] = arg2;
		return mf.format(args);
	}

	public String formatMessage(String messageKey, Object arg0, Object arg1, Object arg2, Object arg3) {
		MessageFormat mf = new MessageFormat(getMessageString(messageKey));
		Object[] args = new Object[4];
		args[0] = arg0;
		args[1] = arg1;
		args[2] = arg2;
		args[3] = arg3;
		return mf.format(args);
	}
}
