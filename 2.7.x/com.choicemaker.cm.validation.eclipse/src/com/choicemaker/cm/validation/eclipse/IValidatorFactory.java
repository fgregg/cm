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
package com.choicemaker.cm.validation.eclipse;

import java.util.Map;
import java.util.NoSuchElementException;

import com.choicemaker.cm.validation.IValidator;
import com.choicemaker.cm.validation.ValidatorCreationException;

/**
 * Factory interface for creating validators.
 *
 * @author rphall
 * @version $Revision: 1.2 $ $Date: 2010/03/29 14:44:54 $
 */
public interface IValidatorFactory {

	public final String VALIDATOR_FACTORY_EXTENSION_POINT =
		"com.choicemaker.cm.validation.eclipse.validatorFactory";

	/**
	 * Creates a validator identified by the specified name.
	 * @param name the name of a validator registered by
	 * some plugin.
	 * @throws NoSuchElementException if no validator is registered
	 * as a plugin under the specified name
	 * @throws ValidatorCreationException if validator name is valid
	 * but the validator can not be created.
	 */
	public IValidator createValidator(String name)
		throws NoSuchElementException, ValidatorCreationException;

	/**
	 * Returns a Map of all the registered validators that this factory
	 * can create, keyed by the validator names.
	 * @return a non-null, but possibly empty map of validators keyed
	 * by validator name.
	 */
	public Map createValidators() throws ValidatorCreationException;

	/**
	 * Returns the identifier of the validator extension point
	 * handled by this factory.
	 * @return
	 */
	public String getHandledValidatorExtensionPoint();

	/**
	 * Returns an array of the names of registered validators that may be
	 * created by this factory.
	 * @return a non-null, but possibly empty array of non-null,
	 * non-empty names.
	 */
	public String[] getRegisteredValidatorNames()
		throws ValidatorCreationException;

	/**
	 * Sets the identifier of the validator extension point
	 * handled by this factory.
	 */
	public void setHandledValidatorExtensionPoint(String id);

}

