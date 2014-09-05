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
package com.choicemaker.cm.core;


/**
 * Base interface for information about an entity.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.1 $ $Date: 2010/01/20 15:05:04 $
 */
public interface Record extends BaseRecord {

	/**
	 * Returns a key that uniquely identifies an entity. If two records
	 * have different identifiers, then they represent different entities
	 * (in the absence of duplicates).
	 */
	Comparable getId();

	/**
	 * Computes non-persistent or cached fields and rows, and checks
	 * whether each field (derived and intrinsic) is valid. If a
	 * field is valid, it marked internally as valid and this
	 * information is available to implementations of this interface.
	 */
	void computeValidityAndDerived();

	/**
	 * Returns a flag indicating the origin of a record. This flag can be
	 * used in calculating derived values, which may depend on the source
	 * of a record.
	 */
	DerivedSource getDerivedSource();
}
