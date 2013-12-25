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

import com.choicemaker.cm.core.util.StringUtils;
import com.choicemaker.cm.validation.AbstractValidator;
import com.choicemaker.cm.validation.IValidator;

/**
 * A simple validator for testing aggregation. This class extends
 * AbstractValidator, rather than implementing IValidator directly,
 * in order to test the AbstractValidator.equals(Object) method.
 * @author rphall
 * @version $Revision: 1.2 $ $Date: 2010/03/29 14:44:29 $
 */
public class NonEmptyStringValidator extends AbstractValidator {

	private static Logger logger = Logger.getLogger(NonEmptyStringValidator.class);

	/**
	 * Partially constructs an aggregate validator. The
	 * {@link #setValidators(Map)} or {@link #addValidator(String,IValidator)}
	 * methods must be called to finish construction.
	 */
	public NonEmptyStringValidator() {
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.validation.eclipse.IValidator#getValidationTypes()
	 */
	public Class[] getValidationTypes() {
		Class[] retVal = new Class[] { String.class };
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.validation.eclipse.IValidator#isValid(java.lang.Object)
	 */
	public boolean isValid(Object object) {
		boolean retVal = false;
		if (object != null && (object instanceof String)) {
			String value = (String) object;
			retVal = StringUtils.nonEmptyString(value);
		}
		return retVal;
	}
	
	public boolean equals(IValidator validator) {
		boolean retVal = false;
		if (validator != null
			&& validator.getClass().equals(this.getClass())) {
			retVal = true;
		}
		return retVal;
	}

	public int hashCode() {
		return this.getClass().hashCode();
	}

}

