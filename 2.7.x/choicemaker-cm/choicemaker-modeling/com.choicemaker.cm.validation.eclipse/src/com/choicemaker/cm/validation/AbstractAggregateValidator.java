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

import java.util.Arrays;
import java.util.Map;

/**
 * Base class for aggregate validators. This class extends the interface of
 * IAggregateValidator by declaring a <code>set</code> operation
 * for the validators property and a new <code>addValidator</code>
 * operation, This class defines default methods for
 * the {@link #equals(IValidator)}, {@link #hashCode()} and {@link #toString()}
 * methods.
 *
 * @author rphall
 * @version $Revision: 1.2 $ $Date: 2010/03/29 14:45:31 $
 */
public abstract class AbstractAggregateValidator
	implements IAggregateValidator {

//	private static Logger logger =
//		Logger.getLogger(AbstractAggregateValidator.class);
		
	private int cachedHashCode = 0;

	/**
	 * The hashCode should be reset as a part of subclass method for
	 * {@link #setValidators(Map)} or {@link #addValidator(String,IValidator)}
	 */
	protected void resetCachedHashCode() {
		this.cachedHashCode = 0;
	}

	protected int getCachedHashCode() {
		return cachedHashCode;
	}

	/**
	 * Partially constructs a validator. The
	 * {@link #setValidators(IValidator[])} method must
	 * be called to finish construction.
	 */
	public AbstractAggregateValidator() {
	}

	/**
	 * Sets the validators used by this validator.
	 * @param validatorMap a map of validator names to validator instances
	 */
	public abstract void setValidators(Map validatorMap);

	/**
	 * Adds a validator to the validators used by this validator.
	 * @param name the plugin name of the validator
	 * @param validator the validator
	 */
	public abstract void addValidator(String name, IValidator validator);

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.validation.eclipse.IValidator#equals(IValidator)
	 */
	public boolean equals(IValidator validator) {
		boolean retVal = false;
		if (validator != null) {
			Class thisClass = this.getClass();
			Class thatClass = validator.getClass();
			if (thisClass.equals(thatClass)) {
				AbstractAggregateValidator that =
					(AbstractAggregateValidator) validator;
				String[] theseNames = this.getValidatorNames();
				String[] thoseNames = that.getValidatorNames();
				 ;
				if (theseNames != null
					&& thoseNames != null
					&& Arrays.equals(theseNames,thoseNames)) {

					retVal = true;
					Map thisMap = this.getValidatorMap();
					Map thatMap = that.getValidatorMap();
					for (int i = 0; retVal && i < theseNames.length; i++) {
						String thisName = theseNames[i];
						IValidator thisValidator = (IValidator) thisMap.get(thisName);
						IValidator thatValidator = (IValidator) thatMap.get(thisName);
						retVal =
							retVal
								&& thisValidator != null
								&& thisValidator.equals(thatValidator);
					}
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
		if (this.getCachedHashCode() == 0) {
			this.cachedHashCode = this.getClass().hashCode();
			String[] names = this.getValidatorNames();
			for (int i=0; i<names.length; i++) {
				this.cachedHashCode += names[i].hashCode();
			}
		}
		return this.getCachedHashCode();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.validation.eclipse.IValidator#toString()
	*/
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getClass().getName());
		sb.append("('");
		sb.append(this.getValidatorNames().length);
		sb.append(" ");
		sb.append(hashCode());
		sb.append(")");
		String retVal = sb.toString();
		return retVal;
	}

}

