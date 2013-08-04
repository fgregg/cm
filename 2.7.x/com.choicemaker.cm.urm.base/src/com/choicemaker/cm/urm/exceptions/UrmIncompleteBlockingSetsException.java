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
 * an IncompleteBlockingSetsException is thrown during
 * a method that invokes the Online Automated Blocking Algorithm.
 * Incomplete blocking sets are user-correctable problems. As an
 * example, they can be caused by user asking ChoceMaker to a
 * person with an uncommon last name but a common birthday.</p>
 * As a simplistic example, suppose a database contains records with known
 * misspellings in the last name (and no soundex as blocking fields).
 * By submitting both last name and a date of birth, a user might expect
 * to find records with valid and misspelled last names:
 * <pre>
 * DB:
 * BORTHWICK 12/31/1969
 * BORTHWICH 12/31/1969
 * BORTWICK 12/31/1969
 *</pre>
 * ChoiceMaker will be able to form a blocking set composed of just
 * the last name, because it is uncommon, but it won't be able to form
 * a blocking set with the birthday. Using just the birthday would create
 * too large of a blocking set, but using the birthday with the last name
 * creates a blocking set which is a subset of the last name blocking set.
 * Therefore the birthday won't be used, and only one record would be
 * found (if an IncompleteBlockingSetsException weren't thrown instead):
 * <pre>
 * BLOCKING CANDIDATES:
 * BORTHWICK 12/31/1969
 * </pre>
 * In other words, if an IncompleteBlockingSetsException weren't thrown,
 * blocking would succeed erroneously. While this example is extremely
 * simplistic, the condition can arise in real-world databases. The problem
 * can be corrected by adding more blocking criteria or by removing unused
 * blocking criteria.
 * <p>
 * This exception helps decouple the URM from the classes used
 * to implement the URM.
 * @see com.choicemaker.cm.io.blocking.automated.base.IncompleteBlockingSetsException
 */
public class UrmIncompleteBlockingSetsException extends Exception {

	static final long serialVersionUID = -8595118222357053047L;

	public UrmIncompleteBlockingSetsException() {
	}

	public UrmIncompleteBlockingSetsException(String message) {
		super(message);
	}

}
