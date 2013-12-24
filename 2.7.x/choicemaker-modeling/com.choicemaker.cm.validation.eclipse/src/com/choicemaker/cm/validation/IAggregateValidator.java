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

import java.util.Map;

/**
 * A validator that delegates to a collection of other validators
 * to validate an object.
 *
 * @author rphall
 * @version $Revision: 1.2 $ $Date: 2010/03/29 14:45:31 $
 */
public interface IAggregateValidator extends IValidator {

	/**
	 * Gets a map of names to instances of the validators used by this validator.
	 * FIXME: this operation is Eclipse-specific. Move it to an interface
	 * in com.choicemaker.cm.validation.eclipse
	 * @return a non-null, but possibly empty map of name to delegate validators.
	 */
	public Map getValidatorMap();

	/**
	 * Gets the plugin names of the validators used by this validator.
	 * FIXME: this operation is Eclipse-specific. Move it to an interface
	 * in com.choicemaker.cm.validation.eclipse
	 * @return a non-null, but possibly empty array of validator names.
	 */
	public String[] getValidatorNames();

	/**
	 * Gets the validators used by this validator.
	 * @return a non-null, but possibly empty array of delegate validators.
	 */
	public IValidator[] getValidators();

}

