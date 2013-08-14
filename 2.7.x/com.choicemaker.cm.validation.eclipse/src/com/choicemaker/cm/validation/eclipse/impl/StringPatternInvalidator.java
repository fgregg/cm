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


/**
 * Validates a non-null name by checking that it does <em>NOT</em> match
 * any pattern in some set of patterns. Null names are considered invalid.
 * @see StringPatternValidator
 *
 * @author rphall
 * @version $Revision: 1.2 $ $Date: 2010/03/29 14:44:29 $
 */
public class StringPatternInvalidator extends StringPatternValidator {

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.validation.eclipse.IValidator#isValid(java.lang.String)
	 */
	public boolean isValid(Object object) {
		boolean retVal = true;
		if (object != null && object instanceof String) {
			retVal = !isMatch((String) object);
		}
		return retVal;
	}

}

