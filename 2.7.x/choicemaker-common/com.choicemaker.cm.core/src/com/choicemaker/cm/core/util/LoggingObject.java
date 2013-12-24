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

/**
 * A LoggingObject is a logging message for log4j that
 * can take parameters.
 * 
 * @author   Adam Winkel
 * @version  $Revision: 1.2 $ $Date: 2010/03/27 21:20:20 $
 */
public class LoggingObject {

	private String message;
	private Object[] params;

	public LoggingObject(String msg) {
		this(msg, new Object[0]);	
	}

	public LoggingObject(String msg, Object[] params) {
		this.message = msg;
		if (params != null) {
			this.params = params;	
		} else {
			this.params = new Object[0];	
		}
	}
	
	public LoggingObject(String msg, Object p1) {
		this(msg, new Object[] {p1});
	}
	
	public LoggingObject(String msg, Object p1, Object p2) {
		this(msg, new Object[] {p1, p2});	
	}
	
	public LoggingObject(String msg, Object p1, Object p2, Object p3) {
		this(msg, new Object[] {p1, p2, p3});	
	}
	
	// other constructors here
	
	public String toString() {
		return message;	
	}

	public String getMessage() {
		return message;	
	}
	
	public Object[] getParams() {
		return params;	
	}
	
	public String getFormattedMessage() {
		//return MessageUtil.m.formatMessage(message, params);
		return getFormattedMessage(MessageUtil.m);
	}

	public String getFormattedMessage(MessageUtil m) {
		return m.formatMessage(message, params);	
	}

}
