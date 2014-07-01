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

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IConfigurationElement;

import com.choicemaker.cm.validation.AbstractAggregateValidator;
import com.choicemaker.cm.validation.IValidator;
import com.choicemaker.cm.validation.ValidatorCreationException;
import com.choicemaker.cm.validation.eclipse.AbstractValidatorFactory;
import com.choicemaker.util.StringUtils;

/**
 * Factory class for aggregate validators.
 *
 * @author rphall
 * @version $Revision: 1.2 $ $Date: 2010/03/29 14:44:29 $
 */
public class AggregateValidatorFactory extends AbstractValidatorFactory {

	private static Logger logger =
		Logger.getLogger(AggregateValidatorFactory.class);

	/**
	 * The {@link setHandledValidatorExtensionPoint(String)} method
	 * must be called after construction and before other methods are
	 * used.
	 */
	public AggregateValidatorFactory() {
	}

	/**
	 * Sets the extension point handled by this factory.
	 * @param id	validator extension point handled by this factory.
	 */
	public AggregateValidatorFactory(String id) {
		super(id);
	}

	private NamedValidator createNamedDelegate(IConfigurationElement el)
		throws Exception {
		NamedValidator retVal = null;
		String delegateName = el.getAttribute("name");
		if (!StringUtils.nonEmptyString(delegateName)) {
			String msg =
				"Null, blank or missing name attribute for validatorRef";
			throw new ValidatorCreationException(msg);
		}
		String delegateExtensionPoint = el.getAttribute("extensionPoint");
		if (!StringUtils.nonEmptyString(delegateExtensionPoint)) {
			String msg =
				"Null, blank or missing extensionPoint attribute for validatorRef";
			throw new ValidatorCreationException(msg);
		}
		IValidator validator =
			AbstractValidatorFactory.createValidator(
				delegateName,
				delegateExtensionPoint);
		retVal =
			new NamedValidator(delegateName, validator, delegateExtensionPoint);
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.validation.eclipse.AbstractValidatorFactory#createValidatorFromRegistryConfigurationElements(org.eclipse.core.runtime.IConfigurationElement[])
	 */
	protected NamedValidator createValidatorFromRegistryConfigurationElement(IConfigurationElement el)
		throws Exception {

		String validatorName = el.getAttribute("name");
		Object o = el.createExecutableExtension("class");
		AbstractAggregateValidator validator = (AbstractAggregateValidator) o;
		IConfigurationElement[] delegates = el.getChildren("validatorRef");

		for (int i = 0; i < delegates.length; i++) {
			IConfigurationElement delegateInfo = delegates[i];
			NamedValidator nv = createNamedDelegate(delegateInfo);
			validator.addValidator(nv.configurationName, nv.validator);
		}

		NamedValidator retVal = new NamedValidator(validatorName, validator);
		return retVal;
	}

}

