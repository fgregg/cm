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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.choicemaker.cm.core.RepositoryChangeEvent;
import com.choicemaker.cm.core.RepositoryChangeListener;
import com.choicemaker.cm.core.train.Trainer;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.listeners.EvaluationEvent;
import com.choicemaker.cm.modelmaker.gui.listeners.EvaluationListener;
import com.choicemaker.cm.modelmaker.gui.tables.CluePerformanceTable;
import com.choicemaker.cm.modelmaker.gui.tables.ClueTablePanel;
import com.choicemaker.cm.module.swing.AbstractTabbedPanel;

/**
 * The panel from which training is initiated.  Users may turn clues on and off, 
 * manually set weights, evaluate the clues on the source to get the counts 
 * statistics, and train the model.
 * 
 * @author S. Yoakum-Stover
 * @version $Revision: 1.1 $ $Date: 2010/03/28 17:13:15 $
 */
public abstract class AbstractModelReviewPanel
	extends AbstractTabbedPanel
	implements
		RepositoryChangeListener,
		PropertyChangeListener,
		EvaluationListener {

	private ModelMaker parent;
	private Trainer trainer;

	private JPanel controlsPanel;
	private JScrollPane cluePerformancePanel;
	private CluePerformanceTable performanceTable;

	private ClueTablePanel clueTablePanel;

	private boolean dirty;

	public AbstractModelReviewPanel(ModelMaker g) {
		super();
		parent = g;
		setBorder(BorderFactory.createEmptyBorder(15, 5, 5, 5));
		buildPanel();
		parent.addPropertyChangeListener(this);
		parent.addEvaluationListener(this);
		parent.getProbabilityModelEventMultiplexer().addPropertyChangeListener(
			this);
		parent.addMarkedRecordPairDataChangeListener(this);
	}

	/**
	 * Invoked when the panel is displayed (true)
	 * or hidden (false) from the user
	 */
	public abstract void setVisible(boolean b);

	protected abstract void buildPanel();

	/**
	 * Invoked to display (true) or hide (false) a clue
	 * performance table from the user
	 */
	public abstract void showCluePerformancePanel(boolean b);

	/**
	 * Handles PropertyChangeEvent notices from the application or the
	 * model that require the clue performance table to be reset:<ul>
	 * <li/>if the source is the application and<ul>
	 * <li/>the event name is
	 * {@link com.choicemaker.cm.modelmaker.ModelMakerEventNames#MARKED_RECORD_PAIR_SOURCE MARKED_RECORD_PAIR_SOURCE}
	 * <li/>the event name is
	 * {@link com.choicemaker.cm.modelmaker.ModelMakerEventNames#PROBABILITY_MODEL PROBABILITY_MODEL}
	 * </ul><li/>or the source is the current model</ul>
	 */
	public abstract void propertyChange(PropertyChangeEvent evt);

	/** Handle evaluation events from the current model trainer */
	public abstract void evaluated(EvaluationEvent evt);

	/** Handle data source changes */
	public abstract void setChanged(RepositoryChangeEvent evt);

	/** Handle changes to data */
	public abstract void recordDataChanged(RepositoryChangeEvent evt);

	/** Handle markup changes to data */
	public abstract void markupDataChanged(RepositoryChangeEvent evt);

}
