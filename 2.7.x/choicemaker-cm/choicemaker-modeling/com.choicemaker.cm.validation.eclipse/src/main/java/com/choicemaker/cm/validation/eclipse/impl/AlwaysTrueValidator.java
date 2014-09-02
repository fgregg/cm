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

import java.util.Map;

import com.choicemaker.cm.validation.AbstractValidator;
import com.choicemaker.cm.validation.IValidator;

/**
 * A validator that always return true for the <code>isValid</code>
 * method. This can be useful in comparing the effect of other
 * validators on a matching model.
 * @author rphall
 * @version $Revision: 1.2 $ $Date: 2010/03/29 14:44:29 $
 */
public class AlwaysTrueValidator extends AbstractValidator {


	/**
	 * Partially constructs an aggregate validator. The
	 * {@link #setValidators(Map)} or {@link #addValidator(String,IValidator)}
	 * methods must be called to finish construction.
	 */
	public AlwaysTrueValidator() {
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.validation.eclipse.IValidator#getValidationTypes()
	 */
	public Class[] getValidationTypes() {
		Class[] retVal = new Class[] { Object.class };
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.validation.eclipse.IValidator#isValid(java.lang.Object)
	 */
	public boolean isValid(Object object) {
		return true;
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

