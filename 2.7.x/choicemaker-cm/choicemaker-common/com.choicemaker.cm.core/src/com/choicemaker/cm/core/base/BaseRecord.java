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
package com.choicemaker.cm.core.base;

import java.io.Serializable;

/**
 * Base interface for all generated record holder class.
 * This interface is implemented by <em>both</em> the root and
 * all nested record holder classes.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.1 $ $Date: 2010/01/20 15:05:04 $
 */
public interface BaseRecord extends Serializable {
	/**
	 * Computes the validity of fields and the values of derived fields.
	 *
	 * @param   src  The <code>DerivedSource</code> from which this record was read in. Used to
	 *            determine which fields must be computed.
	 * @see     DerivedSource
	 */
	void computeValidityAndDerived(DerivedSource src);

	/**
	 * Set the validity of all fields to false and the value of all derived fields
	 * to null/0.
	 * @param   src  The <code>DerivedSource</code> from which this record was read in. Used to
	 *            determine which fields must be computed.
	 * @see     DerivedSource
	 */
	void resetValidityAndDerived(DerivedSource src);
}
