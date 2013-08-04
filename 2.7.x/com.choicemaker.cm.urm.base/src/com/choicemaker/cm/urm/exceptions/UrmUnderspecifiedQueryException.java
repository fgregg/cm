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
package com.choicemaker.cm.urm.exceptions;

/**
 * This exception is thrown by the URM API if
 * an UnderspecifiedQueryException is thrown during
 * a method that invokes the Online Automated Blocking Algorithm.
 * Underspecified queries are user-correctable problems. As an
 * example, they can be caused by user asking ChoiceMaker to a
 * person with the last name of Smith if the database has many
 * records with this last name. The problem can be corrected by
 * refining the query; for example, by asking for records matching
 * Raymond Smith in the 10021 zip code.
 * <p>
 * This exception helps decouple the URM from the classes used
 * to implement the URM.
 * @see com.choicemaker.cm.io.blocking.automated.base.UnderspecifiedQueryException
 */
public class UrmUnderspecifiedQueryException extends Exception {

	static final long serialVersionUID = 8033703828763496399L;

	public UrmUnderspecifiedQueryException() {
		super();
	}

	public UrmUnderspecifiedQueryException(String message) {
		super(message);
	}

}
