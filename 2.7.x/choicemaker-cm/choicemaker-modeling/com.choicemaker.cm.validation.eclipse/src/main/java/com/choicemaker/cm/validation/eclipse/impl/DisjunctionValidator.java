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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.choicemaker.cm.validation.AbstractAggregateValidator;
import com.choicemaker.cm.validation.IValidator;
import com.choicemaker.util.StringUtils;

/**
 * A validator that delegates to a collection of plugin validators.
 * The {@link IValidator.isValid(Object)}
 * method of at least one delegate must evalutate to <code>true</code>
 * in order for the method of this class to evaluate to <code>true</code>.
 * @author rphall
 * @version $Revision: 1.2 $ $Date: 2010/03/29 14:44:29 $
 */
public class DisjunctionValidator extends AbstractAggregateValidator {
	
//	private static Logger logger =
//		Logger.getLogger(SetBasedValidatorFactory.class);

	private Map validatorMap;
	
	/**
	 * Partially constructs an aggregate validator. The
	 * {@link #setValidators(Map)} or {@link #addValidator(String,IValidator)}
	 * methods must be called to finish construction.
	 */
	public DisjunctionValidator() {}

	/**
	 * Full constructs an aggregate validator.
	 * @param validatorMap a non-null map of plugin names to validator instances.
	 */
	public DisjunctionValidator(Map validatorMap) {
		setValidators(validatorMap);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.validation.eclipse.AbstractAggregateValidator#addValidator(java.lang.String, com.choicemaker.cm.validation.eclipse.IValidator)
	 */
	public void addValidator(String name, IValidator validator) {
		// Preconditions
		if (!StringUtils.nonEmptyString(name)) {
			throw new IllegalArgumentException("null or blank validator name");
		}
		if (validator == null) {
			throw new IllegalArgumentException("null validator");
		}

		if (this.validatorMap == null) {
			this.validatorMap = new HashMap();
		}
		this.validatorMap.put(name,validator);
	}

//	/* (non-Javadoc)
//	 * @see com.choicemaker.cm.validation.eclipse.IValidator#getValidationTypes()
//	 */
//	public Class[] getValidationTypes() {
//		invariant();
//		Set types = new HashSet();
//		for (Iterator i=validatorMap.keySet().iterator(); i.hasNext(); ) {
//			IValidator delegate = (IValidator) this.validatorMap.get(i.next());
//			Class[] delegateTypes = delegate.getValidationTypes();
//			for (int j=0; j<delegateTypes.length; j++) {
//				types.add(delegateTypes[j]);
//			}
//		}
//		Class[] retVal = (Class[]) types.toArray(new Class[types.size()]);
//		return retVal;
//	}

	/**
	 * This implementation is a bit unsafe, since it hands back
	 * the actual Map used by this instance, and not a clone.
	 */
	public Map getValidatorMap() {
		invariant();
		Map retVal = Collections.unmodifiableMap(this.validatorMap);
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.validation.eclipse.IAggregateValidator#getValidatorNames()
	 */
	public String[] getValidatorNames() {
		invariant();
		String[] retVal = (String[]) validatorMap.keySet().toArray(new String[0]);
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.validation.eclipse.IAggregateValidator#getValidators()
	 */
	public IValidator[] getValidators() {
		invariant();
		List list = new ArrayList();
		for (Iterator i=this.validatorMap.entrySet().iterator(); i.hasNext(); ) {
			IValidator v = (IValidator) ((Map.Entry) i.next()).getValue();
			list.add(v);
		}
		IValidator[] retVal = (IValidator[]) list.toArray(new IValidator[list.size()]);
		return retVal;
	}

	/** Checks that an instance has been fully constructed */
	private void invariant() {
		if (this.validatorMap == null) {
			throw new IllegalStateException("incompletely constructed");
		}
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.validation.eclipse.IValidator#isValid(java.lang.Object)
	 */
	public boolean isValid(Object value) {
		invariant();
		boolean retVal = false;
		for (Iterator i=this.validatorMap.entrySet().iterator(); !retVal && i.hasNext(); ) {
			Map.Entry entry = (Map.Entry) i.next();
			IValidator validator = (IValidator) entry.getValue();
			retVal = validator.isValid(value);
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.validation.eclipse.AbstractAggregateValidator#setValidators(java.util.Map)
	 */
	public void setValidators(Map validatorMap) {

		// Preconditions
		if (validatorMap == null) {
			throw new IllegalArgumentException("null validator map");
		}
		Set keySet = validatorMap.keySet();
		Set entrySet = validatorMap.entrySet();
		for (Iterator i=keySet.iterator(); i.hasNext(); ) {
			Object o = i.next();
			if (o == null) {
				throw new IllegalArgumentException("null validator name");
			} else if ( !(o instanceof String) ) {
				throw new IllegalArgumentException("non-String validator name");
			} else if (!StringUtils.nonEmptyString((String)o)) {
				throw new IllegalArgumentException("blank validator name");
			}
		}
		for (Iterator i=entrySet.iterator(); i.hasNext(); ) {
			Map.Entry e = (Map.Entry) i.next();
			if (e == null) {
				throw new IllegalArgumentException("null validator name");
			} else if ( !(e.getValue() instanceof IValidator) ) {
				throw new IllegalArgumentException("non-validator map entry");
			}
		}

		this.validatorMap = validatorMap;
	}

}

