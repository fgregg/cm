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

import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.event.SwingPropertyChangeSupport;

import com.choicemaker.cm.core.Accessor;
import com.choicemaker.cm.core.ChoiceMakerExtensionPoint;
import com.choicemaker.cm.core.ClueDesc;
import com.choicemaker.cm.core.ClueSet;
import com.choicemaker.cm.core.Constants;
import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.Descriptor;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.MachineLearner;
import com.choicemaker.cm.core.ModelConfigurationException;
import com.choicemaker.cm.core.ProbabilityModelSpecification;
import com.choicemaker.cm.core.report.Report;
import com.choicemaker.cm.core.report.Reporter;
import com.choicemaker.cm.core.util.NameUtils;
import com.choicemaker.cm.core.util.Signature;
import com.choicemaker.e2.CMConfigurationElement;
import com.choicemaker.e2.CMExtension;
import com.choicemaker.e2.CMPluginDescriptor;
import com.choicemaker.e2.platform.CMPlatformUtils;
import com.choicemaker.util.ArrayHelper;
import com.choicemaker.util.FileUtilities;

/**
 * A probability model consisting of holder classes, translators, a clue set, weights, and
 * a list of clues to be evaluated.
 *
 * Class invariant:
 * clueSet != null <=>
 *   cluesToEval != null AND cluesToEval.length == clueSet.size() AND
 *   weights != null AND weights.length == clueSet.size
 *
 * @author Martin Buechi
 * @author S. Yoakum-Stover
 * @author rphall (Split ProbabilityModel into separate instance and manager types)
 * @version $Revision: 1.1 $ $Date: 2010/03/24 18:02:30 $
 * @see PMManager
 */
public class MutableProbabilityModel implements IProbabilityModel {
	
	private static final Logger logger = Logger
			.getLogger(MutableProbabilityModel.class.getName());

	private Accessor acc;
	private String accessorClassName;
	private String blockingConfigurationName;
	private String clueFilePath;
	private File clueFile;
	private boolean[] cluesToEvaluate;
//	private String databaseAbstractionName;
	private String databaseAccessorName;
	private String databaseConfigurationName;
	private int decisionDomainSize;
	private boolean enableAllCluesBeforeTraining;
	private boolean enableAllRulesBeforeTraining;
	private int firingThreshold = 3;
	private Date lastTrainingDate;
	private MachineLearner ml;
	private String modelName;
	private String modelFilePath;
	private boolean multiPropertyChange;
	private boolean trainedWithHolds;
	private String trainingSource;
	private String userName;

	/** @deprecated */
	private Hashtable properties;

	/** @deprecated */
	private String antCommand;

	/** @deprecated */
	private boolean useAnt;

	// listeners
	private SwingPropertyChangeSupport propertyChangeListeners = new SwingPropertyChangeSupport(this);

	/**
	 * Constructor.
	 */
	MutableProbabilityModel() {
		this.properties = new Hashtable();
	}

	MutableProbabilityModel(String modelFilePath, String clueFilePath) {
		this();
		setModelFilePath(modelFilePath);
		setClueFilePath(clueFilePath);
		setMachineLearner(new DoNothingMachineLearning());
	}

	public MutableProbabilityModel(ProbabilityModelSpecification spec,
			Accessor acc) throws ModelConfigurationException {
		this();
		setModelFilePath(spec.getWeightFilePath());
		setClueFilePath(spec.getClueFilePath());
		setMachineLearner(spec.getMachineLearner());
		this.setAccessor(acc);
	}

	MutableProbabilityModel(
			String modelFilePath,
			String clueFilePath,
			Accessor acc,
			MachineLearner ml,
			boolean[] cluesToEvaluate,
			String trainingSource,
			boolean trainedWithHolds,
			Date lastTrainingDate)
			throws IllegalArgumentException, ModelConfigurationException {
		this (
				modelFilePath,
				clueFilePath,
				acc,
				ml,
				cluesToEvaluate,
				trainingSource,
				trainedWithHolds,
				lastTrainingDate,
				/* useAnt */ false,
				/* String */ null);
		}

	private MutableProbabilityModel(
		String modelFilePath,
		String clueFilePath,
		Accessor acc,
		MachineLearner ml,
		boolean[] cluesToEvaluate,
		String trainingSource,
		boolean trainedWithHolds,
		Date lastTrainingDate,
		boolean useAnt,
		String antCommand)
		throws IllegalArgumentException, ModelConfigurationException {
		this(modelFilePath, clueFilePath);
		setAccessorInternal(acc);
		this.trainingSource = trainingSource;
		this.trainedWithHolds = trainedWithHolds;
		this.lastTrainingDate = lastTrainingDate;
		this.useAnt = useAnt;
		this.antCommand = antCommand;
		acc.getClueSet(); // Make sure the clue set gets loaded
		setMachineLearner(ml);
		setCluesToEvaluate(cluesToEvaluate);
		computeDecisionDomainSize();
	}

	/**
	 * Returns the number of active clues in this <code>ClueSet</code>.
	 *
	 * @return  The number of active clues in this <code>ProbabilityModel</code>.
	 */
	public int activeSize() {
		int r = 0;
		for (int i = 0; i < cluesToEvaluate.length; ++i) {
			if (cluesToEvaluate[i]) {
				++r;
			}
		}
		return r;
	}

	/**
	 * Returns the number of clues predicting <code>Decision<code>
	 * <code>d</code> in this <code>ProbabilityModel</code>.
	 *
	 * @return  The number of clues predicting <code>Decision</code>
	 *            <code>d</code> in this <code>ProbabilityModel</code>.
	 */
	public int activeSize(Decision d) {
		ClueDesc[] cd = acc.getClueSet().getClueDesc();
		int r = 0;
		for (int i = 0; i < cluesToEvaluate.length; ++i) {
			if (cluesToEvaluate[i] && cd[i].getDecision() == d) {
				++r;
			}
		}
		return r;
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public void beginMultiPropertyChange() {
		multiPropertyChange = true;
	}

	public boolean canEvaluate() {
		return ml.canEvaluate();
	}

	public void changedCluesToEvaluate() {
		if (!multiPropertyChange) {
			propertyChangeListeners.firePropertyChange(CLUES_TO_EVALUATE, null, cluesToEvaluate);
		}
	}

	private void computeDecisionDomainSize() {
		decisionDomainSize = acc.getClueSet().size(Decision.HOLD) == 0 ? 2 : 3;
	}

	public void endMultiPropertyChange() {
		multiPropertyChange = false;
		propertyChangeListeners.firePropertyChange(null, new Object(), new Object());
	}

	/**
	 * Returns the translator accessors.
	 *
	 * @return  The translator accessors.
	 */
	public Accessor getAccessor() {
		return acc;
	}

	/**
	 * Returns the name of the Accessor class.
	 *
	 * Note: this is not the same as getAccessor().getClass().getName()
	 * because getAccessor() returns a dynamic proxy, so the class name
	 * is something like $Proxy0.
	 *
	 * @return The name of the accessor class.
	 */
	public String getAccessorClassName() {
		return accessorClassName;
	}

	/**
	 * Returns an instance of the clue set.
	 *
	 * @return   An instance of the clue set.
	 */
	public ClueSet getClueSet() {
		return acc.getClueSet();
	}

//	public String getClueSetPath() {
//		return clueSetPath;
//	}

	public String getClueFilePath() {
		return clueFilePath;
	}

	public File getClueFile() {
		return this.clueFile;
	}

	/**
	 * Returns the list of clues to evaluate.
	 *
	 * @return  The list of clues to evaluate.
	 */
	public boolean[] getCluesToEvaluate() {
		return cluesToEvaluate;
	}

	public String getClueText(int clueNum) throws IOException {
		ClueDesc cd = acc.getClueSet().getClueDesc()[clueNum];
		int start = cd.getStartLineNumber();
		int end = cd.getEndLineNumber();
		int len = end - start;
		BufferedReader in = new BufferedReader(new FileReader(getClueFilePath()));
		for (int i = 1; i < start && in.ready(); ++i) {
			in.readLine();
		}
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i <= len && in.ready(); ++i) {
			buf.append(in.readLine()).append(Constants.LINE_SEPARATOR);
		}
		in.close();
		return buf.toString();
	}

	public int getDecisionDomainSize() {
		return decisionDomainSize;
	}

	public Evaluator getEvaluator() {
		return ml.getEvaluator();
	}

	/**
	 * Returns the file name of the probability model.
	 *
	 * @return   The file name of the probability model.
	 */
	public String getModelFilePath() {
		return modelFilePath;
	}

	/**
	 * Get the value of firingThreshold.
	 * @return value of firingThreshold.
	 */
	public int getFiringThreshold() {
		return firingThreshold;
	}

	/**
	 * Get the value of lastTrainingDate.
	 * @return value of lastTrainingDate.
	 */
	public Date getLastTrainingDate() {
		return lastTrainingDate;
	}

	public MachineLearner getMachineLearner() {
		return ml;
	}

	/**
	 * Returns the model name of the probability model.
	 *
	 * @return   The model name of the probability model.
	 */
	public String getModelName() {
		return modelName;
	}

	public boolean[] getTrainCluesToEvaluate() {
		boolean[] res = new boolean[cluesToEvaluate.length];
		ClueDesc[] desc = getClueSet().getClueDesc();
		for (int i = 0; i < cluesToEvaluate.length; ++i) {
			res[i] = cluesToEvaluate[i] && !desc[i].rule;
		}
		return res;
	}

	/**
	 * Get the value of trainingSource.
	 * @return value of trainingSource.
	 */
	public String getTrainingSource() {
		return trainingSource;
	}

	/**
	 * Get the value of userName.
	 * @return value of userName.
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Get the value of enableAllCluesBeforeTraining.
	 * @return value of enableAllCluesBeforeTraining.
	 */
	public boolean isEnableAllCluesBeforeTraining() {
		return enableAllCluesBeforeTraining;
	}

	/**
	 * Get the value of enableAllRulesBeforeTraining.
	 * @return value of enableAllRulesBeforeTraining.
	 */
	public boolean isEnableAllRulesBeforeTraining() {
		return enableAllRulesBeforeTraining;
	}

	public boolean isTrainedWithHolds() {
		return trainedWithHolds;
	}

	public void machineLearnerChanged(Object oldValue, Object newValue) {
		propertyChangeListeners.firePropertyChange(MACHINE_LEARNER_PROPERTY, oldValue, newValue);
	}

	public boolean needsRecompilation() {
		if (acc == null) {
			return true;
		} else {
			long cd = acc.getCreationDate();
			return cd < getClueFile().lastModified() || cd < new File(acc.getSchemaFileName()).getAbsoluteFile().lastModified();
		}
	}

	public int numTrainCluesToEvaluate() {
		int res = 0;
		ClueDesc[] desc = getClueSet().getClueDesc();
		for (int i = 0; i < cluesToEvaluate.length; ++i) {
			if (cluesToEvaluate[i] && !desc[i].rule) {
				++res;
			}
		}
		return res;
	}

	public Map properties() {
		return this.properties;
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public void report(Report report) throws IOException {
		IOException ex = null;
		Reporter[] reporters = PMManager.getGlobalReporters();
		for (int i = 0; i < reporters.length; i++) {
			try {
				reporters[i].append(report);
			} catch (IOException e) {
				if (ex == null) {
					ex = e;
				}
			}
		}
		if (ex != null) {
			throw ex;
		}
	}

	/**
	 * Sets the translator accessors.
	 *
	 * @param   newAcc  The translator accessors.
	 * @throws ModelConfigurationException 
	 */
	public void setAccessor(Accessor newAcc) throws ModelConfigurationException {
		Accessor oldAccessor = acc;
		ClueSet newClueSet = newAcc.getClueSet();
		int newSize = newClueSet.size();
		boolean[] newCluesToEvaluate = ArrayHelper.getTrueArray(newSize);
		int[] oldClueNums = new int[newSize];
		if (acc != null) {
			ClueDesc[] oldDesc = acc.getClueSet().getClueDesc();
			HashMap m = new HashMap();
			for (int i = 0; i < oldDesc.length; ++i) {
				m.put(oldDesc[i].getName(), oldDesc[i]);
			}
			ClueDesc[] newDesc = newClueSet.getClueDesc();
			for (int i = 0; i < newDesc.length; ++i) {
				ClueDesc k = newDesc[i];
				ClueDesc o = (ClueDesc) m.get(k.getName());
				if (o != null) {
					int number = o.getNumber();
					newCluesToEvaluate[i] = cluesToEvaluate[number];
					oldClueNums[i] = number;
				} else {
					oldClueNums[i] = -1;
				}
			}
		} else {
			for (int i = 0; i < oldClueNums.length; ++i) {
				oldClueNums[i] = -1;
			}
		}
		cluesToEvaluate = newCluesToEvaluate;
		if (ml.canUse(newAcc.getClueSet())) {
			ml.changedAccessor(acc, newAcc, oldClueNums);
			setAccessorInternal(newAcc);
			computeDecisionDomainSize();
		} else {
			setAccessorInternal(newAcc);
			computeDecisionDomainSize();
			setMachineLearner(new DoNothingMachineLearning());
		}
		if (!multiPropertyChange) {
			propertyChangeListeners.firePropertyChange(null, oldAccessor, acc);
		}
	}

	private void setAccessorInternal(Accessor accessor)
			throws ModelConfigurationException {
		CMExtension[] exts =
			CMPlatformUtils.getExtensions(ChoiceMakerExtensionPoint.CM_CORE_ACCESSOR);
		Class[] interfaces = new Class[exts.length];
		for (int i = 0; i < interfaces.length; i++) {
			CMExtension ext = exts[i];
			try {
				CMConfigurationElement[] configs =
					ext.getConfigurationElements();
				assert configs != null;
				if (configs.length < 1) {
					String msg =
						"No accessors configured for " + this.getModelName();
					throw new ModelConfigurationException(msg);
				}
				if (configs.length > 1) {
					String msg =
						"Multiple accessors configured for "
								+ this.getModelName();
					throw new ModelConfigurationException(msg);
				}
				CMConfigurationElement ce = configs[0];
				String clsName =
					ce.getAttribute(ChoiceMakerExtensionPoint.CM_CORE_ACCESSOR_ATTR_CLASS);
				CMPluginDescriptor descriptor =
					ext.getDeclaringPluginDescriptor();
				ClassLoader cl1 = descriptor.getPluginClassLoader();
				interfaces[i] = Class.forName(clsName, false, cl1);
			} catch (ClassNotFoundException e) {
				throw new ModelConfigurationException(e.toString(), e);
			}
		}
		Class accessorClass = accessor.getClass();
		this.accessorClassName = accessor.getClass().getName();

		ClassLoader cl2 = accessorClass.getClassLoader();
		this.acc =
			(Accessor) Proxy.newProxyInstance(cl2, interfaces,
					new AccessorInvocationHandler(accessor));
	}

	/**
	 * Sets the clues to evaluate.
	 *
	 * @param   cluesToEvaluate  The clues to evaluate.
	 */
	public void setCluesToEvaluate(boolean[] cluesToEvaluate) throws IllegalArgumentException {
		ClueSet clueSet = getClueSet();
		if (clueSet != null) {
			if (cluesToEvaluate == null || cluesToEvaluate.length != clueSet.size()) {
				throw new IllegalArgumentException("Illegal cluesToEvaluate.");
			}
		} else if (cluesToEvaluate != null) {
			throw new IllegalArgumentException("Illegal cluesToEvaluate.");
		}
		this.cluesToEvaluate = cluesToEvaluate;
		if (!multiPropertyChange) {
			propertyChangeListeners.firePropertyChange(CLUES_TO_EVALUATE, null, cluesToEvaluate);
		}
	}

	/**
	 * Set the value of enableAllCluesBeforeTraining.
	 * @param v  Value to assign to enableAllCluesBeforeTraining.
	 */
	public void setEnableAllCluesBeforeTraining(boolean v) {
		this.enableAllCluesBeforeTraining = v;
	}

	/**
	 * Set the value of enableAllRulesBeforeTraining.
	 * @param v  Value to assign to enableAllRulesBeforeTraining.
	 */
	public void setEnableAllRulesBeforeTraining(boolean v) {
		this.enableAllRulesBeforeTraining = v;
	}

	/**
	 * Sets the path to the probability model weights file (*.model)
	 * 
	 * If this model is in the collection of probability models, the
	 * {@link #getModelName() model configuration name} that is
	 * associated with in the collection is not changed.
	 * 
	 * @param modelFilePath
	 *            The new file path.
	 */
	public void setModelFilePath(String filePath) {
		this.modelFilePath = filePath;
		setModelName(NameUtils.getNameFromFilePath(filePath));
	}

	/**
	 * Set the value of firingThreshold.
	 * @param v  Value to assign to firingThreshold.
	 */
	public void setFiringThreshold(int v) {
		this.firingThreshold = v;
	}

	/**
	 * Set the value of lastTrainingDate.
	 * @param v  Value to assign to lastTrainingDate.
	 */
	public void setLastTrainingDate(Date v) {
		this.lastTrainingDate = v;
	}

	public void setMachineLearner(MachineLearner ml) {
		MachineLearner old = this.ml;
		this.ml = ml;
		ml.setProbabilityModel(this);
		if (!multiPropertyChange) {
			propertyChangeListeners.firePropertyChange(MACHINE_LEARNER, old, ml);
		}
	}

	public void setModelName(String name) {
		String oldName = this.modelName;
		this.modelName = name;
		if (!multiPropertyChange) {
			propertyChangeListeners.firePropertyChange(NAME, oldName, name);
		}
	}

	public void setClueFilePath(String fn) {
		this.clueFilePath = fn;
		if (fn != null && modelFilePath != null) {
			this.clueFile = FileUtilities.getAbsoluteFile(new File(modelFilePath).getParentFile(), fn);
		} else if (fn != null) {
			this.clueFile = new File(fn).getAbsoluteFile();
		} else {
			this.clueFile = null;
		}
	}

	public void setTrainedWithHolds(boolean b) {
		trainedWithHolds = b;
	}

	/**
	 * Set the value of trainingSource.
	 * @param v  Value to assign to trainingSource.
	 */
	public void setTrainingSource(String v) {
		this.trainingSource = v;
	}

	/**
	 * Set the value of userName.
	 * @param v  Value to assign to userName.
	 */
	public void setUserName(String v) {
		this.userName = v;
	}
	
	/** @deprecated */
	public boolean isUseAnt() {
		assert useAnt == false;
		return useAnt;
	}

	/** @deprecated */
	public void setUseAnt(boolean ignored) {
		assert this.useAnt == false;
	}

	/**
	 * Get the value of antCommand.
	 * @return value of antCommand.
	 */
	public String getAntCommand() {
		assert antCommand == null;
		return antCommand;
	}

	/**
	 * Set the value of antCommand.
	 * @param v  Value to assign to antCommand.
	 */
	public void setAntCommand(String ignored) {
		assert antCommand == null;
	}

	public String getClueSetName() {
		return this.getAccessor().getClueSetName();
	}

	// NOT YET IMPLEMENTED (as of version 2.7.1)
	// public String getEntityInterfaceName() {
	// return null;
	// }

	public String getSchemaName() {
		return this.getAccessor().getSchemaFileName();
	}

	public String getClueSetSignature() {
		return Signature.calculateClueSetSignature(this.getClueSet());
	}

	public String getModelSignature() {
		return Signature.calculateModelSignature(this);
	}

	public String getSchemaSignature() {
		Descriptor d = this.getAccessor().getDescriptor();
		return Signature.calculateRecordLayoutSignature(d);
	}

	public String getEvaluatorSignature() {
		Evaluator e = getEvaluator();
		return e == null ? "" : e.getSignature();
	}

	public String getBlockingConfigurationName() {
		String retVal = blockingConfigurationName;
		if (retVal == null) {
			logger.warning("Blocking configuration has not been set.");
		}
		return retVal;
	}

//	public String getDatabaseAbstractionName() {
//		return databaseAbstractionName;
//	}

	public String getDatabaseAccessorName() {
		return databaseAccessorName;
	}

	public String getDatabaseConfigurationName() {
		String retVal = databaseConfigurationName;
		if (retVal == null) {
			logger.warning("Database configuration has not been set.");
		}
		return retVal;
	}

	public void setBlockingConfigurationName(String bc) {
		/*
		if (bc == null || bc.trim().isEmpty()) {
			throw new IllegalArgumentException("null or blank blocking configuration");
		}
		*/
		this.blockingConfigurationName = bc;
	}

//	public void setDatabaseAbstractionName(String databaseAbstractionName) {
//		this.databaseAbstractionName = databaseAbstractionName;
//	}

	public void setDatabaseAccessorName(String databaseAccessorName) {
		this.databaseAccessorName = databaseAccessorName;
	}

	public void setDatabaseConfigurationName(String dbc) {
		/*
		if (dbc == null || dbc.trim().isEmpty()) {
			logger.warning("null or blank database configuration");
		}
		*/
		this.databaseConfigurationName = dbc;
	}

//	@Override
	public String toString() {
		return "ProbabilityModel [modelName=" + modelName
				+ ", clueFile=" + clueFilePath + "]";
	}

}

