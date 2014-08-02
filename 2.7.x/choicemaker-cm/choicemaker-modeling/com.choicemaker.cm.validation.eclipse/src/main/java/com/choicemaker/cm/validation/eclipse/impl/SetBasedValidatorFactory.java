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
package com.choicemaker.cm.validation.eclipse.impl;

import org.eclipse.core.runtime.IConfigurationElement;

import com.choicemaker.cm.validation.AbstractSetBasedValidator;
import com.choicemaker.cm.validation.eclipse.AbstractValidatorFactory;

/**
 * Factory class for set-based validators.
 *
 * @author rphall
 * @version $Revision: 1.2 $ $Date: 2010/03/29 14:44:29 $
 */
public class SetBasedValidatorFactory extends AbstractValidatorFactory {

//	private static Logger logger =
//		Logger.getLogger(SetBasedValidatorFactory.class);

	/**
	 * The {@link setHandledValidatorExtensionPoint(String)} method
	 * must be called after construction and before other methods are
	 * used.
	 */
	public SetBasedValidatorFactory() {
	}

	/**
	 * Sets the extension point handled by this factory.
	 * @param id	validator extension point handled by this factory.
	 */
	public SetBasedValidatorFactory(String id) {
		super(id);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.validation.eclipse.AbstractValidatorFactory#createValidatorFromRegistryConfigurationElements(org.eclipse.core.runtime.IConfigurationElement[])
	 */
	protected NamedValidator createValidatorFromRegistryConfigurationElement(IConfigurationElement el)
		throws Exception {

		String validatorName = el.getAttribute("name");
		Object o = el.createExecutableExtension("class");
		AbstractSetBasedValidator validator = (AbstractSetBasedValidator) o;
		String setName = el.getAttribute("set");
		validator.setNamedSet(setName);

		NamedValidator retVal = new NamedValidator(validatorName, validator);
		return retVal;
	}

}

