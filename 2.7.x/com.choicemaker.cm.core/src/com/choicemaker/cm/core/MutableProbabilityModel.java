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
package com.choicemaker.cm.core;

import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;

import javax.swing.event.SwingPropertyChangeSupport;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Platform;

import com.choicemaker.cm.core.ml.MachineLearner;
import com.choicemaker.cm.core.ml.none.None;
import com.choicemaker.cm.core.report.Report;
import com.choicemaker.cm.core.report.Reporter;
import com.choicemaker.cm.core.util.ArrayHelper;
import com.choicemaker.cm.core.util.FileUtilities;
import com.choicemaker.cm.core.util.NameUtils;

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
class MutableProbabilityModel implements IProbabilityModel, ImmutableProbabilityModel {

	private Accessor acc;
	private String accessorClassName;
	private String antCommand;
	private String clueFileName;
	private boolean[] cluesToEvaluate;
	private int decisionDomainSize;
	private boolean enableAllCluesBeforeTraining;
	private boolean enableAllRulesBeforeTraining;
	private String fileName;
	private int firingThreshold = 3;
	private Date lastTrainingDate;
	private MachineLearner ml;
	private boolean multiPropertyChange;

	private String name;
	private Hashtable properties;

	// listeners
	private SwingPropertyChangeSupport propertyChangeListeners = new SwingPropertyChangeSupport(this);
	private String rawClueFileName;
	private boolean trainedWithHolds;
	private String trainingSource;
	private boolean useAnt;
	private String userName;

	/**
	 * Constructor.
	 */
	MutableProbabilityModel() {
		this.properties = new Hashtable();
	}

	MutableProbabilityModel(String fileName, String rawClueFileName) {
		this();
		setFileName(fileName);
		setRawClueFileName(rawClueFileName);
		setMachineLearner(new None());
	}

	MutableProbabilityModel(
		String fileName,
		String rawClueFileName,
		Accessor acc,
		MachineLearner ml,
		boolean[] cluesToEvaluate,
		String trainingSource,
		boolean trainedWithHolds,
		Date lastTrainingDate,
		boolean useAnt,
		String antCommand)
		throws IllegalArgumentException {
		this(fileName, rawClueFileName);
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
	 * Get the value of antCommand.
	 * @return value of antCommand.
	 */
	public String getAntCommand() {
		return antCommand;
	}

	public String getClueFileName() {
		return clueFileName;
	}

	/**
	 * Returns an instance of the clue set.
	 *
	 * @return   An instance of the clue set.
	 */
	public ClueSet getClueSet() {
		return acc.getClueSet();
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
		BufferedReader in = new BufferedReader(new FileReader(getClueFileName()));
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
	public String getFileName() {
		return fileName;
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
	 * Returns the name of the probability model.
	 *
	 * @return   The name of the probability model.
	 */
	public String getName() {
		return name;
	}

	public String getRawClueFileName() {
		return rawClueFileName;
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

	/**
	 * Get the value of useAnt.
	 * @return value of useAnt.
	 */
	public boolean isUseAnt() {
		return useAnt;
	}

	public void machineLearnerChanged(Object oldValue, Object newValue) {
		propertyChangeListeners.firePropertyChange(MACHINE_LEARNER_PROPERTY, oldValue, newValue);
	}

	public boolean needsRecompilation() {
		if (acc == null) {
			return true;
		} else {
			long cd = acc.getCreationDate();
			return cd < new File(getClueFileName()).lastModified() || cd < new File(acc.getSchemaFileName()).getAbsoluteFile().lastModified();
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
	
	public Hashtable properties() {
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
	 */
	public void setAccessor(Accessor newAcc) {
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
			setMachineLearner(new None());
		}
		if (!multiPropertyChange) {
			propertyChangeListeners.firePropertyChange(null, oldAccessor, acc);
		}
	}

	private void setAccessorInternal(Accessor accessor) {
		IExtension[] accessorElems = Platform.getPluginRegistry().getExtensionPoint("com.choicemaker.cm.core.accessor").getExtensions();
		Class[] interfaces = new Class[accessorElems.length];
		for (int i = 0; i < interfaces.length; i++) {
			IExtension accessorElem = accessorElems[i];
			try {
				IConfigurationElement ce = accessorElem.getConfigurationElements()[0];
				String clsName = ce.getAttribute("class");

				IPluginDescriptor descriptor = accessorElem.getDeclaringPluginDescriptor();
				ClassLoader cl1= descriptor.getPluginClassLoader();

				interfaces[i] = Class.forName( clsName,  false, cl1);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		Class accessorClass = accessor.getClass();
		this.accessorClassName = accessor.getClass().getName();

		ClassLoader cl2 = accessorClass.getClassLoader();
		this.acc = (Accessor) Proxy.newProxyInstance(cl2, interfaces, new AccessorInvocationHandler(accessor));
	}

	/**
	 * Set the value of antCommand.
	 * @param v  Value to assign to antCommand.
	 */
	public void setAntCommand(String v) {
		this.antCommand = v;
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
	 * Sets the name of the probability model.
	 *
	 * If this model is in the collection of probability models, the
	 * name that it is associated with in the collection does not get
	 * changed.
	 *
	 * @param   fileName  The new name.
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
		setName(NameUtils.getNameFromFileName(fileName));
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

	public void setName(String name) {
		String oldName = this.name;
		this.name = name;
		if (!multiPropertyChange) {
			propertyChangeListeners.firePropertyChange(NAME, oldName, name);
		}
	}

	public void setRawClueFileName(String fn) {
		this.rawClueFileName = fn;
		if (fn != null && fileName != null) {
			this.clueFileName = FileUtilities.getAbsoluteFile(new File(fileName).getParentFile(), fn).toString();
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
	 * Set the value of useAnt.
	 * @param v  Value to assign to useAnt.
	 */
	public void setUseAnt(boolean v) {
		this.useAnt = v;
	}

	/**
	 * Set the value of userName.
	 * @param v  Value to assign to userName.
	 */
	public void setUserName(String v) {
		this.userName = v;
	}
}

