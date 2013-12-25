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
package com.choicemaker.cm.validation;

import java.util.Set;

/**
 * A validator that uses a set of data values to validate
 * an object.
 *
 * @author rphall
 * @version $Revision: 1.2 $ $Date: 2010/03/29 14:45:31 $
 */
public interface ISetBasedValidator extends IValidator {

	/**
	 * Gets the name of the {@link com.choicemaker.cm.match.gen.Sets Sets instance}
	 * that holds the data used by this validator.
	 * FIXME: this operation is Eclipse-specific. Move it to an interface
	 * in com.choicemaker.cm.validation.eclipse
	 * FIXME: add a <code>Set getSet()</code> operation
	 * @return the plugin name of the set of tokens or Patterns used by this validator.
	 * @see com.choicemaker.cm.match.gen.Sets
	 */
	public String getNamedSet();

	/**
	 * Returns the set of data used by this validator.
	 * @return a non-null Set
	 */
	public Set getSetContents();

}

