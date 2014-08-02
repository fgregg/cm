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
package com.choicemaker.cm.module;

/**
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/27 19:27:56 $
 */
public interface IMessageSupport {
	public abstract String formatMessage(String messageKey);
	public abstract String formatMessage(String messageKey, Object[] args);
	public abstract String formatMessage(String messageKey, Object arg0);
	public abstract String formatMessage(
		String messageKey,
		Object arg0,
		Object arg1);
	public abstract String formatMessage(
		String messageKey,
		Object arg0,
		Object arg1,
		Object arg2);
	public abstract String formatMessage(
		String messageKey,
		Object arg0,
		Object arg1,
		Object arg2,
		Object arg3);
}
