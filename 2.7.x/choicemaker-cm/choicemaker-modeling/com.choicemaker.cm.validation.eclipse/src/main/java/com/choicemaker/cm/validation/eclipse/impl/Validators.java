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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.choicemaker.cm.validation.IValidator;
import com.choicemaker.cm.validation.ValidatorCreationException;
import com.choicemaker.cm.validation.eclipse.AbstractValidatorFactory;
import com.choicemaker.cm.validation.eclipse.IValidatorFactory;
import com.choicemaker.util.StringUtils;

/**
 * Collection of validators. The semantics of the class
 * is similar to the com.choicemaker.cm.matching.gen.Sets
 * or com.choicemaker.cm.matching.wfst.eclipse.WfstParsers
 * collections.
 *
 * @author    Rick Hall
 * @version   $Revision: 1.2 $ $Date: 2010/03/29 14:44:29 $
 */
public final class Validators {

	private static Logger logger = Logger.getLogger(Validators.class.getName());

	/**
	 * Cached map of validator configuration names to validators.
	 * Do not use directly (except in {@link #addValidator(String,IValidator)};
	 * use {@link #getValidators()} instead.
	 * @see #getValidators()
	*/
	private static Map validators = null;

	/** Synchronization object for creating the validatorFactories singleton */
	private static final Object validatorsInit = new Object();

	/**
	 * Returns an unmodifiable copy of the validator collection managed by
	 * this class. The map keys are validator configuration names and the
	 * values are validator configuration instances.
	 * @return a non-null, but possibly empty map
	 * @throws ValidatorCreationException if the validator collection
	 * managed by this class can not be created.
	 * @see #createValidatorMap()
	 */
	public static Map getValidators() throws ValidatorCreationException {
		if (validators == null) {
			synchronized (validatorsInit) {
				if (validators == null) {
					validators = createValidatorMapFromRegistry();
				}
			} // synchronized
		}
		return Collections.unmodifiableMap(validators);
	}

	/**
	 * Adds a validator to the collection of validators. If two validators are
	 * added under the same name, the second addition replaces the first.
	 * @param name the name of the validator configuration to be added.
	 * As a general practice, names should be trimmed and case should be
	 * standardized, but these recommendations aren't enforced.
	 * @param   validator  The validator to be added.
	 * @throws ValidatorCreationException if the validator collection
	 * managed by this class can not be created.
	 */
	public static void addValidator(String name, IValidator validator)
		throws ValidatorCreationException {
		// Preconditions
		if (!StringUtils.nonEmptyString(name)) {
			throw new IllegalArgumentException("null or blank validator name");
		}
		if (validator == null) {
			throw new IllegalArgumentException("null validator");
		}

		synchronized (Validators.validatorsInit) {
			Map newValidatorMap = cloneMap(getValidators());
			Object alreadyPresent = newValidatorMap.put(name, validator);
			if (alreadyPresent != null) {
				logger.warn(
					"An instance of "
						+ alreadyPresent.getClass().getName()
						+ "was replaced as a validator by an instance of "
						+ validator.getClass().getName()
						+ " with the name '"
						+ name
						+ "'");
			}
			Validators.validators = newValidatorMap;
		}

	}

	private static Map cloneMap(Map map) {
		Map retVal = new HashMap();
		retVal.putAll(map);
		return retVal;
	}

	/**
	 * Returns the validator identified by <code>name</code>.
	 * 
	 * @param name the validator configuration for which a
	 * validator instance should be returned
	 * @return a validator corresponding to the configuration
	 * named by <code>name</code>, or null.
	 * @throws ValidatorCreationException if the validator collection
	 * managed by this class can not be created.
	 */
	public static IValidator getValidator(String name)
		throws ValidatorCreationException {
		IValidator retVal = (IValidator) getValidators().get(name);
		if (retVal == null) {
			String msg = "unknown validator '" + name + "'";
			logger.error(msg);
		}
		return retVal;
	}

	/**
	 * Returns all the names of validator configurations managed by
	 * this class.
	 * @return all the names of validators managed by
	 * this class
	 * @throws ValidatorCreationException if the validator collection
	 * managed by this class can not be created.
	 */
	public static Collection getValidatorNames()
		throws ValidatorCreationException {
		return getValidators().keySet();
	}

	/**
	 * Creates a map of registered validators, where keys are validator
	 * configuration names and values are validator configuration instances.
	 * The map is created directly from the plugin registry; it is not simply
	 * a copy of the validator collection managed by this class. However,
	 * since plugin configurations of the Eclipse 2.x plugin registry are not
	 * dynamic, the map returned by this method should be functionally
	 * equivalent to the map managed by this class.
	 * @return a non-null, but possibly empty map
	 * @throws ValidatorCreationException if the validator map
	 * can not be created.
	 * @see #getValidators()
	 */
	public static Map createValidatorMapFromRegistry()
		throws ValidatorCreationException {

		Map retVal = new HashMap();
		Map factories = AbstractValidatorFactory.createValidatorFactoryMap();
		Set handledExtensionPoints = factories.keySet();
		for (Iterator i = handledExtensionPoints.iterator(); i.hasNext();) {
			String handledExtensionPoint = (String) i.next();
			IValidatorFactory factory =
				(IValidatorFactory) factories.get(handledExtensionPoint);
			Map v = factory.createValidators();
			retVal.putAll(v);
		}
		return retVal;
	} // createValidatorMap()

	/**
	 * Answers whether a specified value is valid according
	 * to the specified validator.
	 *
	 * @param   name  The validationTag of the validator to be used.
	 * @param   value  The value to be checked.
	 * @return  whether the specified value is valid according
	 * to the specified validator.
	 * @throws ValidatorCreationException if the validator collection
	 * managed by this class can not be created.
	 */
	public static boolean isValid(String name, Object value)
		throws ValidatorCreationException {
		boolean retVal = false;
		if (name != null && value != null) {
			IValidator validator = (IValidator) getValidators().get(name);
			if (validator != null) {
				retVal = validator.isValid(value);
			} else {
				String msg = "missing IValidator '" + name + "'";
				throw new IllegalArgumentException(msg);
			}
		}
		return retVal;
	}

	private Validators() {
	}

}

