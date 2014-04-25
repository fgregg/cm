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

/**
 * Base class for validators. This class implements the <code>equals</code>
 * methods in the manner recommendedc by the IValidator documentation.
 *
 * @author rphall
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:21 $
 */
public abstract class AbstractValidator implements IValidator {
	
	/**
	 * If <code>o</code> is an instance of IValidator, this method
	 * invokes <code>v.equals(IValidator)</code>; otherwise, it
	 * invokes <code>v.equals(Object)</code>.
	 */
	public static boolean validatorEquals(IValidator v, Object o) {
		boolean retVal = false;
		if (v != null && o instanceof IValidator) {
			retVal = v.equals((IValidator)o);
		} else {
			retVal = v.equals(o);
		}
		return retVal;
	}
	
 	/**
	 * Code to keep lint happy. Subclasses should override if they
	 * override {@link equals(IValidator)}.
	 */
	public int hashCode() {
		return super.hashCode();
	}

	/**
	 * If <code>o</code> is an instance of IValidator, this method
	 * invokes <code>equals(IValidator)</code>; otherwise, it
	 * invokes <code>super.equals(Object)</code>.
	 */
	public boolean equals(Object o) {
		boolean retVal;
		if (o instanceof IValidator) {
			retVal = this.equals((IValidator)o);
		} else {
			retVal = super.equals(o);
		}
		return retVal;
	}

}
