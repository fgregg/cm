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
package com.choicemaker.cm.modelmaker.gui.abstraction;

import java.beans.PropertyChangeListener;

import javax.swing.JFrame;
import javax.swing.JToolBar;

import com.choicemaker.cm.core.base.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.MarkedRecordPairSource;
import com.choicemaker.cm.core.base.Repository;
import com.choicemaker.cm.core.base.RepositoryChangeListener;
import com.choicemaker.cm.core.base.Thresholds;
import com.choicemaker.cm.core.train.Trainer;
import com.choicemaker.cm.core.util.IntArrayList;
import com.choicemaker.cm.core.util.OperationFailedException;
import com.choicemaker.cm.modelmaker.IRecordPairList;
import com.choicemaker.cm.modelmaker.filter.ListeningMarkedRecordPairFilter;
import com.choicemaker.cm.modelmaker.gui.listeners.EvaluationListener;
import com.choicemaker.cm.modelmaker.gui.listeners.EventMultiplexer;
import com.choicemaker.cm.modelmaker.stats.Statistics;
import com.choicemaker.cm.module.IUserMessages;
import com.choicemaker.cm.module.swing.AbstractTabbedPanel;
import com.choicemaker.cm.module.swing.DefaultManagedPanel;

/**
 * @author rphall
 */
public abstract class AbstractApplication extends JFrame
/* implements IModelMaker2 */ {

	private static final long serialVersionUID = 1L;
	protected abstract void buildComponents();

	public abstract JToolBar getToolBar();
	public abstract DefaultManagedPanel getClusterPanel();
	public abstract IRecordPairList getRecordPairList();

	public abstract void showPairIndices(boolean b);
	public abstract void showStatusMessages(boolean b);
	public abstract void showToolbar(boolean b);

	public abstract String[] getTabbedPanelNames();
	public abstract AbstractTabbedPanel getTabbedPanel(String tab);
	public abstract void showTabPanel(String tabName);
	//		public abstract IHumanReviewController getHumanReviewController();
	//		public abstract AbstractMessagePanel getMessagePanel();
	//		public abstract ITestingController getTestingController();
	//		public abstract IModelReviewController getTrainingController();
	//		public abstract void showClusterPanel();
	//		public abstract void showHumanReviewPanel();
	//		public abstract void showTestingPanel();
	//		public abstract void showTrainingControlPanel();

	public abstract Thresholds getThresholds();
	public abstract void setDifferThreshold(float d);
	public abstract void setMatchThreshold(float m);
	public abstract void setThresholds(Thresholds t);
	public abstract void addThresholdChangeListener(PropertyChangeListener l);
	public abstract void removeThresholdChangeListener(PropertyChangeListener l);

	//************************************************************************************************
	public abstract boolean haveProbabilityModel();
	public abstract void reloadProbabilityModel();
	/**
	 * Gets the model using the model name, then sets the model.
	 * If a model by the passed name can not be retrieved, an
	 * error is posted and the previously set model is kept as
	 * the active model.
	 *
	 * @param modelName
	 */
	public abstract void setProbabilityModel(String modelName, boolean reload)
		throws OperationFailedException;
	/**
	 * Sets the probability model.  Nulls out the source list and calls
	 * resetEvaluationStatistics on the trainingPanel so that the proper clue
	 * set is displayed.  Sends a modelChanged message to any listeners.
	 *
	 * @param pm     A reference to a PMManager.
	 */
	public abstract void setProbabilityModel(ImmutableProbabilityModel pm);
	/**
	 *
	 * @return A reference to the active PMManager.
	 */
	public abstract ImmutableProbabilityModel getProbabilityModel();
	public abstract void postProbabilityModelInfo();
	public abstract ImmutableProbabilityModel getProbabilityModel(String modelName)
		throws OperationFailedException;
	/**
	 * Saves the probability model to disk.
	 *
	 * @param pm
	 */
	public abstract void saveProbabilityModel(ImmutableProbabilityModel pm)
		throws OperationFailedException;
	/**
	 * Saves the active probability model to disk.
	 */
	public abstract void saveActiveModel();
	public abstract boolean buildProbabilityModel(ImmutableProbabilityModel pm);
	public abstract EventMultiplexer getProbabilityModelEventMultiplexer();

	//************************************************************************************************
	public abstract boolean haveMarkedRecordPairSource();
	public abstract boolean haveMarkedRecordPairSource(int i);
	public abstract void setMultiIncludeHolds(int i, boolean b);
	public abstract boolean getMultiIncludeHolds(int i);
	public abstract boolean isIncludeHolds();
	public abstract MarkedRecordPairSource getMultiSource(int i);
	public abstract void setMultiSource(int i, MarkedRecordPairSource s);
	/**
	 * Returns the keepBothSourcesInMemory.
	 * @return boolean
	 */
	public abstract boolean isKeepAllSourcesInMemory();
	/**
	 * Sets the keepBothSourcesInMemory.
	 * @param keepBothSourcesInMemory The keepBothSourcesInMemory to set
	 */
	public abstract void setKeepAllSourcesInMemory(boolean keepAllSourcesInMemory);
	/**
	 * Sets the trainSourceUsed.
	 * @param trainSourceUsed The trainSourceUsed to set
	 */
	public abstract void setUsedMultiSource(int i);
	public abstract int getUsedMultiSource();
	public abstract void swapSources();
	public abstract MarkedRecordPairSource getMarkedRecordPairSource();
	public abstract java.util.List getSourceList();
	/**
	 * Returns true if we have a non-null source list.
	 *
	 * @return
	 */
	public abstract boolean haveSourceList();
	public abstract boolean isSourceDataModified();
	public abstract boolean isChecked(int mrpIndex);
	public abstract void setChecked(int mrpIndex, boolean checked);
	public abstract void uncheckAll();
	public abstract void checkAll();
	public abstract int[] getCheckedIndices();
	public abstract void sortChecked();
	public abstract IntArrayList getChecked();
	/**
	 * Saves the active MRPSource to disk.  This method is called
	 * by the DefaultPairReviewPanel to save a source that has been
	 * modified.
	 */
	public abstract void saveMarkedRecordPairSource();
	public abstract void addMarkedRecordPairDataChangeListener(RepositoryChangeListener l);
	public abstract void removeMarkedRecordPairDataChangeListener(RepositoryChangeListener l);
	//public abstract void fireMarkedRecordPairDataChange(RepositoryChangeEvent evt);
	//************************************************************************************************
	public abstract void setAllCluesOrRules(int what, boolean value);
	/**
	 * Resets the weights in the probability model all to 1.
	 */
	public abstract void resetWeights();
	/**
	 * Evaluates the clues on the source to get and display
	 * the counts.
	 */
	public abstract void evaluateClues();
	public abstract boolean isEvaluated();
	public abstract void addEvaluationListener(EvaluationListener l);
	public abstract void removeEvaluationListener(EvaluationListener l);
	public abstract void postClue(String clueText);
	public abstract void postClueText(int clueId);
	/**
	 * Starts the training.  When training is done, updates
	 * the ProbabilityModelChangeListeners and TrainerChangeListeners
	 * so that they can update their data.
	 */
	public abstract boolean train(
		boolean recompile,
		boolean enableAllClues,
		boolean enableAllRules,
		int firingThreshold,
		boolean andTest);
	public abstract Trainer getTrainer();
	public abstract Statistics getStatistics();
	public abstract Repository getRepository();
	//************************************************************************************************
	public abstract void setMarkedRecordPair(int p);
	public abstract int getMarkedRecordPair();
	/**
	 * This method is called by the ClueTableCellListener which is
	 * attached to the clue table in the TestingPanel.  It allows
	 * one to step through the MRPs associated with a given
	 * clue.
	 *
	 * @param clueID
	 * @param fireType
	 */
	public abstract void updateRecordPairList(int clueID, int fireType);
	public abstract void displayRecordPairFilterDialog();
	public abstract void filterMarkedRecordPairList();
	public abstract int[] getSelection();
	public abstract ListeningMarkedRecordPairFilter getFilter();
	public abstract void setFilter(ListeningMarkedRecordPairFilter filter);
	//************************ Data Methods
	public abstract void dataModified();
	//*************************
	public abstract void reviewNextMarkedRecordPair();
	/**
	 * Since the DefaultPairReviewPanel and the DefaultTestingControlPanel do
	 * not communicate directly, this method allows one to click
	 * a button on the review Panel to select the previous MRP to
	 * be displayed from the list shown on the testing panel.
	 */
	public abstract void reviewPreviousMarkedRecordPair();

	// Delegates
	public abstract IUserMessages getUserMessages();
	protected abstract void setUserMessages(IUserMessages userMessages);

}

