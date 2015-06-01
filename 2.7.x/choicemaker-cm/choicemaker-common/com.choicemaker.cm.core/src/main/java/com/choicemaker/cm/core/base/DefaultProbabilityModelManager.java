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
package com.choicemaker.cm.core.base;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.choicemaker.cm.core.Accessor;
import com.choicemaker.cm.core.ChoiceMakerExtensionPoint;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.IProbabilityModelManager;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.ModelConfigurationException;
import com.choicemaker.cm.core.compiler.DoNothingCompiler;
import com.choicemaker.cm.core.compiler.ICompiler;
import com.choicemaker.cm.core.report.Reporter;
import com.choicemaker.cm.core.xmlconf.ProbabilityModelsXmlConf;
import com.choicemaker.e2.CMConfigurationElement;
import com.choicemaker.e2.CMExtension;
import com.choicemaker.e2.platform.CMPlatformUtils;
import com.choicemaker.util.Precondition;

/**
 * Creates and manages a collection of IProbabilityModel instances. <br/>
 * <br/>
 * FIXME this class should be installable (or a Service Provider Implementation
 * or a plugin) so that a persistent manager could be specified as the active
 * manager.
 * 
 * @author Martin Buechi (initial implementation of ProbabilityModel)
 * @author S. Yoakum-Stover (initial implementation of ProbabilityModel)
 * @author rphall (Refactored from PMManager and previously ProbabilityModel)
 * 
 */
public class DefaultProbabilityModelManager implements IProbabilityModelManager {

	private static final Logger logger = Logger
			.getLogger(DefaultProbabilityModelManager.class.getName());

	private DefaultProbabilityModelManager() {
	}

	private static final Object _instanceSynch = new Object();

	private static DefaultProbabilityModelManager _instance = null;

	public static IProbabilityModelManager getInstance() {
		if (_instance == null) {
			synchronized (_instanceSynch) {
				if (_instance == null) {
					_instance = new DefaultProbabilityModelManager();
				}
			}
		}
		return _instance;
	}

	private final Map models = new HashMap();
	private Reporter[] reporters = new Reporter[0];

	/**
	 * Adds a probability model to the collection of configured models.
	 *
	 * @param model
	 *            The probability model.
	 */
	public void addModel(IProbabilityModel model) {
		models.put(model.getModelName(), model);
	}

	public Accessor createAccessor(String className, ClassLoader cl)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		Class accessorClass = Class.forName(className, true, cl);
		return (Accessor) accessorClass.newInstance();
	}

	/**
	 * Returns the specified probability model.
	 *
	 * @return The specified probability model.
	 */
	public IProbabilityModel getModelInstance(String name) {
		return (IProbabilityModel) models.get(name);
	}

	/**
	 * Returns the specified probability model.
	 *
	 * @return The specified probability model.
	 */
	public ImmutableProbabilityModel getImmutableModelInstance(String name) {
		return (ImmutableProbabilityModel) models.get(name);
	}

	// private Map models() {
	// Map retVal = Collections.unmodifiableMap(models);
	// return retVal;
	// }

	public IProbabilityModel[] getModels() {
		Set s = new HashSet(models.values());
		IProbabilityModel[] retVal = new IProbabilityModel[s.size()];
		retVal = (IProbabilityModel[]) s.toArray(retVal);
		return retVal;
	}

	public void setGlobalReporters(Reporter[] rs) {
		Precondition.assertNonNullArgument("null reporter array", rs);
		for (int i = 0; i < rs.length; i++) {
			Precondition.assertNonNullArgument("null reporter[" + i + "]",
					rs[i]);
		}
		Reporter[] copy = new Reporter[rs.length];
		System.arraycopy(rs, 0, copy, 0, rs.length);
		reporters = copy;
	}

	public Reporter[] getGlobalReporters() {
		Reporter[] retVal = new Reporter[this.reporters.length];
		System.arraycopy(this.reporters, 0, retVal, 0, this.reporters.length);
		return retVal;
	}

	public int loadModelPlugins() throws ModelConfigurationException,
			IOException {
		CMExtension[] extensions =
			CMPlatformUtils
					.getExtensions(ChoiceMakerExtensionPoint.CM_CORE_MODELCONFIGURATION);
		assert extensions != null;
		int retVal = 0;
		for (int i = 0; i < extensions.length; i++) {
			final CMExtension ext = extensions[i];
			URL pUrl = ext.getDeclaringPluginDescriptor().getInstallURL();
			CMConfigurationElement[] els = ext.getConfigurationElements();
			for (int j = 0; j < els.length; j++) {
				final CMConfigurationElement el = els[j];
				final String KEY_FILE = "model";
				String file = el.getAttribute(KEY_FILE);
				try {
					final String fileName = new File(file).getName();
					final URL rUrl = new URL(pUrl, file);
					final InputStream is = rUrl.openStream();
					final ICompiler compiler = new DoNothingCompiler();
					final StringWriter compilerMessages = new StringWriter();
					final ClassLoader cl =
						ext.getDeclaringPluginDescriptor()
								.getPluginClassLoader();
					final boolean allowCompile = false;
					IProbabilityModel model =
						ProbabilityModelsXmlConf.readModel(fileName, is,
								compiler, compilerMessages, cl, allowCompile);
					// HACK
					assert model instanceof MutableProbabilityModel;
					MutableProbabilityModel mpm =
						(MutableProbabilityModel) model;
					mpm.setModelName(ext.getUniqueIdentifier());
					// END HACK
					DefaultProbabilityModelManager.getInstance()
							.addModel(model);
					++retVal;

				} catch (ModelConfigurationException ex) {
					logger.severe(ex.toString());
					throw ex;
				} catch (IOException ex) {
					logger.severe(ex.toString());
					throw ex;
				}

			}
		}
		return retVal;
	}

}
