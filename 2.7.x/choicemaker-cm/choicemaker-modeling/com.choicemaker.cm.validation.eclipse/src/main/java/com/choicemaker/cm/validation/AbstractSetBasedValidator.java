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
 * Base class for set-based validators. This class extends the interface of
 * ISetBasedValidator by declaring a set operation
 * for the namedSet property, This class defines default methods for
 * the {@link #equals(IValidator)}, {@link #hashCode()} and {@link #toString()}
 * methods.
 *
 * @author rphall
 * @version $Revision: 1.2 $ $Date: 2010/03/29 14:45:31 $
 */
public abstract class AbstractSetBasedValidator implements ISetBasedValidator {

//	private static Logger logger =
//		Logger.getLogger(AbstractSetBasedValidator.class);

	/**
	 * Partially constructs a validator. The
	 * {@link #setNamedSet(String)} method must
	 * be called to finish construction.
	 */
	public AbstractSetBasedValidator() {
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.validation.eclipse.IValidator#equals(IValidator)
	 */
	public boolean equals(IValidator validator) {
		boolean retVal = false;
		if (validator != null) {
			Class thisClass = this.getClass();
			Class thatClass = validator.getClass();
			if (thisClass.equals(thatClass)) {
				AbstractSetBasedValidator that =
					(AbstractSetBasedValidator) validator;
				Set thisSet = this.getSetContents();
				Set thatSet = that.getSetContents();
				if (thisSet != null && thatSet != null) {
					retVal = thisSet.equals(thatSet);
				}
			}
		}
		return retVal;
	}
	
	public boolean equals(Object o) {
		boolean retVal = AbstractValidator.validatorEquals(this,o);
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.validation.eclipse.IValidator#hashCode()
	 */
	public int hashCode() {
		// FIXME: this implementation is Eclipse-specific. Move it to an abstract
		// class in com.choicemaker.cm.validation.eclipse or replace it
		// with an implementation based on java.util.Set
		int retVal = this.getClass().hashCode();
		String namedSet = this.getNamedSet();
		if (namedSet != null) {
			retVal += namedSet.hashCode();
		}
		return retVal;
	}

	/**
	 * Sets the name of the {@link com.choicemaker.cm.match.gen.Sets Sets instance}
	 * that holds the data used by this validator.
	 * @param setName the plugin name of the collection
	 * @see com.choicemaker.cm.match.gen.Sets
	 */
	public abstract void setNamedSet(String setName);

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.validation.eclipse.IValidator#toString()
	*/
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getClass().getName());
		sb.append("('");
		sb.append(this.getNamedSet());
		sb.append(")");
		String retVal = sb.toString();
		return retVal;
	}

}

