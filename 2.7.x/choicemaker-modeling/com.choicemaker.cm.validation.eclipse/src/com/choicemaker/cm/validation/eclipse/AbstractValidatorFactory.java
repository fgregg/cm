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
package com.choicemaker.cm.validation.eclipse;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.Platform;

import com.choicemaker.cm.core.util.StringUtils;
import com.choicemaker.cm.validation.IValidator;
import com.choicemaker.cm.validation.ValidatorCreationException;

/**
 * A base class for validator factories.
 *
 * @author rphall
 * @version $Revision: 1.2 $ $Date: 2010/03/29 14:44:54 $
 */
public abstract class AbstractValidatorFactory implements IValidatorFactory {

	/**
	 * An immutable tuple consisting of a validator configuation name, the validator,
	 * and optionally the name of the validator extension point.
	 */
	public static class NamedValidator {
		public final String configurationName;
		public final String extensionPointName;
		public final IValidator validator;
		/**
		 * Create a NamedValidator without specifying
		 * the validator extension point
		 * @param n the validator configuration name
		 * @param v the validator
		 */
		public NamedValidator(String n, IValidator v) {
			this(n,v,null);
		}
		/**
		 * Create a NamedValidator
		 * @param n the validator configuration name
		 * @param v the validator
		 * @param e the name of the validator extension point
		 */
		public NamedValidator(String n, IValidator v, String e) {
			this.configurationName = n;
			this.validator = v;
			this.extensionPointName = e;
			if (!StringUtils.nonEmptyString(n)) {
				throw new IllegalArgumentException("null or blank validator name");
			}
			if (validator == null) {
				throw new IllegalArgumentException("null validator");
			}
		}
	}

	/**
	 * Cached array of registered validator names.<ul>
	 * <li>key - factory class name</li>
	 * <li>value - factory-specific array of validator names</li>
	 * </ul>
	 */
	// private static String[] cachedValidatorNames;
	protected static Map cachedValidatorNamesMap = new HashMap();

	/**
	 * Cached maps of validators.<ul>
	 * <li>key - factory class name</li>
	 * <li>value - factory-specific map of validators</li>
	 * </ul>
	 */
	// private static Map cachedValidators;
	protected static Map cachedValidatorsMap = new HashMap();

	/**
	 * One extension point per factory class.<ul>
	 * <li>key - factory class name</li>
	 * <li>value - factory-specific extension point name</li>
	 * </ul>
	 */
	// private static String handledValidatorExtensionPoint;
	protected static Map handledValidatorExtensionPointsMap = new HashMap();

	private static Logger logger =
		Logger.getLogger(AbstractValidatorFactory.class);

	/** Cached map of extension points to ValidatorFactories */
	private static Map validatorFactories = null;
	
	/** Synchronization object for creating the validatorFactories singleton */
	private static final Object validatorFactoriesInit = new Object();

	public static IValidator createValidator(String name, String extensionPoint)
		throws ValidatorCreationException {
	
		// Preconditions
		if (!StringUtils.nonEmptyString(name)) {
			throw new IllegalArgumentException("null or blank validator configuration name");
		}
		if (!StringUtils.nonEmptyString(extensionPoint)) {
			throw new IllegalArgumentException("null or blank name for validator extension point");
		}
	
		// Get factory for the extension point
		Map factories  = AbstractValidatorFactory.getValidatorFactories();
		IValidatorFactory factory = (IValidatorFactory) factories.get(extensionPoint);
		if (factory == null) {
				String msg = "no validator factory for '" + extensionPoint + "'";
				logger.error(msg);
				throw new ValidatorCreationException(msg);
		}
		
		// Ask the factory to create the extension point
		IValidator retVal = factory.createValidator(name);		
	
		return retVal;
	}

	/**
	 * Creates a map of validator extension points to validator factories.
	 * @return a non-null, but possibly empty map of validator
	 * extension points to factories.
	 */
	public static Map createValidatorFactoryMap()
		throws ValidatorCreationException {

		Map retVal = new HashMap();
		IPluginRegistry registry = Platform.getPluginRegistry();
		IExtensionPoint pt =
			registry.getExtensionPoint(IValidatorFactory.VALIDATOR_FACTORY_EXTENSION_POINT);
		IExtension[] extensions = pt.getExtensions();
	
		for (int i = 0; i < extensions.length; i++) {
			IExtension ext = extensions[i];
			IConfigurationElement[] els = ext.getConfigurationElements();
			for (int j = 0; j < els.length; j++) {
				IConfigurationElement el = els[j];
	
				String handledExtensionPoint =
					el.getAttribute("handledValidatorExtensionPoint");
				IValidatorFactory factory = (IValidatorFactory) retVal.get(handledExtensionPoint);
				if (factory == null) {
					try {
						Object o = el.createExecutableExtension("class");
						factory = (IValidatorFactory) o;
					} catch (CoreException x) {
						String className = el.getAttribute("class");
						String msg = registryExceptionMessage(className,handledExtensionPoint,x);
						logger.error(msg,x);
						throw new ValidatorCreationException(msg,x);
					}
					factory.setHandledValidatorExtensionPoint(
						handledExtensionPoint);
					retVal.put(handledExtensionPoint,factory);
				} // if null factory
			} // for j validator extension configurations
		} // for i validator extensions
		return retVal;
	}
	
	public static Map getValidatorFactories() throws ValidatorCreationException {
		if (validatorFactories == null) {
			synchronized(validatorFactoriesInit) {
				if (validatorFactories == null) {
					validatorFactories = createValidatorFactoryMap();
				}
			} // synchronized
		}
		return Collections.unmodifiableMap(validatorFactories);
	}

	protected static String registryExceptionMessage(
		String registryName,
		String extension,
		Exception x) {
		String retVal =
			"registry name (validator or factory) '"
				+ registryName
				+ "' (extension '"
				+ extension
				+ "'): "
				+ x.getClass().getName()
				+ ": "
				+ x.getMessage();
		return retVal;
	}

	/**
	 * The {@link setHandledValidatorExtensionPoint(String)} method
	 * must be called after construction and before other methods are
	 * used.
	 */
	public AbstractValidatorFactory() {
	}

	/**
	 * Sets the extension point handled by this factory.
	 * @param id	validator extension point handled by this factory.
	 */
	public AbstractValidatorFactory(String id) {
		setHandledValidatorExtensionPoint(id);
	}

	private String[] cloneStringArray(String[] array) {
		String[] retVal = new String[array.length];
		System.arraycopy(array, 0, retVal, 0, array.length);
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.validation.eclipse.IValidatorFactory#createValidator(java.lang.String)
	 */
	public IValidator createValidator(String name)
		throws ValidatorCreationException {

		// Precondition
		if (!StringUtils.nonEmptyString(name)) {
			throw new IllegalArgumentException("null or blank validator name");
		}
		invariant();
		name = name.trim();

		IValidator retVal = null;
		String key = this.getClass().getName();
		Map cachedValidators = (Map) cachedValidatorsMap.get(key);
		if (cachedValidators == null) {
			retVal = createValidatorFromRegistry(name);
		} else {
			retVal = (IValidator) cachedValidators.get(name);
			if (retVal == null) {
				String msg = "no validator cached as '" + name + "')";
				logger.error(msg);
				throw new NoSuchElementException(msg);
			}
		} // else

		return retVal;
	} // createValidator(String)

	private IValidator createValidatorFromRegistry(String name)
		throws ValidatorCreationException {

		IPluginRegistry registry = Platform.getPluginRegistry();
		String extensionId = this.getHandledValidatorExtensionPoint();
		IExtensionPoint pt = registry.getExtensionPoint(extensionId);
		IExtension[] extensions = pt.getExtensions();

		IValidator retVal = null;
		for (int i = 0; retVal == null && i < extensions.length; i++) {
			IExtension ext = extensions[i];
			IConfigurationElement[] els = ext.getConfigurationElements();
			for (int j = 0; retVal == null && j < els.length; j++) {
				IConfigurationElement el = els[j];
				try {
					String validatorName =
						getValidatorNameFromRegistryConfigurationElement(el);
					if (name.equals(validatorName)) {
						NamedValidator nv =
							createValidatorFromRegistryConfigurationElement(el);
						retVal = nv.validator;
					} // if validatorName
				} catch (Exception x) {
					String msg =
						registryExceptionMessage(name, extensionId, x);
					logger.error(msg, x);
					throw new ValidatorCreationException(msg, x);
				}
			} // for j ConfigurationElements
		} // for i Extensions

		if (retVal == null) {
			String msg = "no validator registered as '" + name + "')";
			logger.error(msg);
			throw new NoSuchElementException(msg);
		}

		return retVal;
	} // createValidatorFromRegistry

	/**
	 * Abstract, factory-dependent method that must be implemented
	 * by subclasses.
	 * @param els ConfigurationElements associated with a validator extension.
	 * @return the validator specified by the extension.
	 * @throws Exception if the validator can not be created.
	 */
	protected abstract NamedValidator createValidatorFromRegistryConfigurationElement(IConfigurationElement el)
		throws Exception;

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.validation.eclipse.IValidatorFactory#createValidators()
	 */
	public Map createValidators() throws ValidatorCreationException {
		invariant();
		String key = this.getClass().getName();
		Map cachedValidators = (Map) cachedValidatorsMap.get(key);
		if (cachedValidators == null) {
			cachedValidators = this.createValidatorsFromRegistry();
			cachedValidatorsMap.put(key, cachedValidators);
		}
		Map retVal = Collections.unmodifiableMap(cachedValidators);
		return retVal;
	} // createValidators()

	public Map createValidatorsFromRegistry()
		throws ValidatorCreationException {
		invariant();
		Map retVal = new HashMap();

		// Error messages
		boolean isError = false;
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);

		IPluginRegistry registry = Platform.getPluginRegistry();
		String extensionId = this.getHandledValidatorExtensionPoint();
		IExtensionPoint pt = registry.getExtensionPoint(extensionId);
		IExtension[] extensions = pt.getExtensions();

		for (int i = 0; i < extensions.length; i++) {
			IExtension ext = extensions[i];
			IConfigurationElement[] els = ext.getConfigurationElements();
			NamedValidator nv = null;
			for (int j = 0; j < els.length; j++) {
				IConfigurationElement el = els[j];
				try {
					nv = createValidatorFromRegistryConfigurationElement(el);
					retVal.put(nv.configurationName, nv.validator);
				} catch (Exception x) {
					isError = true;
					String msg =
						registryExceptionMessage(nv.configurationName, extensionId, x);
					logger.error(msg, x);
					pw.println(msg);
				} // catch
			} // for j ConfigurationElements
		} // for i Extensions

		if (isError) {
			String msg = sw.toString();
			throw new ValidatorCreationException(msg);
		}

		return retVal;
	} // createValidatorsFromRegistry()

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.validation.eclipse.IValidatorFactory#getHandledValidatorExtensionPoint()
	 */
	public String getHandledValidatorExtensionPoint() {
		invariant();
		String key = this.getClass().getName();
		String retVal = (String) handledValidatorExtensionPointsMap.get(key);
		if (!StringUtils.nonEmptyString(retVal)) {
			throw new IllegalStateException(
				"null or blank handled extension point for " + key);
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.validation.eclipse.IValidatorFactory#getRegisteredValidatorNames()
	 */
	public String[] getRegisteredValidatorNames()
		throws ValidatorCreationException {
		invariant();
		String key = this.getClass().getName();
		String[] cachedValidatorNames =
			(String[]) cachedValidatorNamesMap.get(key);
		if (cachedValidatorNames == null) {
			Map validators = this.createValidators();
			Set validatorNames = validators.keySet();
			cachedValidatorNames =
				(String[]) validatorNames.toArray(new String[0]);
			cachedValidatorNamesMap.put(key, cachedValidatorNames);
		}
		String[] retVal = cloneStringArray(cachedValidatorNames);
		return retVal;
	}

	/**
	 * Abstract, factory-dependent method that must be implemented
	 * by subclasses.
	 * @param els ConfigurationElements associated with a validator extension.
	 * @return the plugin name of the validator
	 * @throws Exception if a non-null, non-empty name could
	 * not be determined for the validator
	 */
	protected String getValidatorNameFromRegistryConfigurationElement(IConfigurationElement el)
		throws Exception {
		String retVal = el.getAttribute("name");
		return retVal;
	}

	/**
	 * Checks that a factory has been properly initialized.
	 */
	private void invariant() {
		/* Not a useful invariant
		String key = this.getClass().getName();
		String handledValidatorExtensionPoint = handledValidatorExtensionPointsMap.get(key);
		if (handledValidatorExtensionPoint == null) {
			throw new IllegalStateException("not initialized: null handledValidatorExtensionPointsMap");
		}
		*/
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.validation.eclipse.IValidatorFactory#setHandledValidatorExtensionPoint(String)
	 */
	public void setHandledValidatorExtensionPoint(String id) {
		if (!StringUtils.nonEmptyString(id)) {
			throw new IllegalArgumentException("null handled extension point id");
		}
		String trimmedId = id.trim();
		String key = this.getClass().getName();
		String handledValidatorExtensionPoint =
			(String) handledValidatorExtensionPointsMap.get(key);
		if (handledValidatorExtensionPoint == null) {
			handledValidatorExtensionPointsMap.put(key, trimmedId);
		} else if (
			handledValidatorExtensionPoint != null
				&& !handledValidatorExtensionPoint.equals(trimmedId)) {
			throw new IllegalArgumentException(
				"new value '"
					+ id
					+ "' of handled extension point id differs from current value '"
					+ handledValidatorExtensionPoint
					+ "'");
		} else {
			logger.debug(
				"redundant value '"
					+ trimmedId
					+ "' for handled extension point id");
		}
	}

}

