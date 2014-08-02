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

import com.choicemaker.cm.core.ImmutableMarkedRecordPair;
import com.choicemaker.cm.core.RepositoryChangeEvent;
import com.choicemaker.cm.gui.utils.viewer.CompositePane;
import com.choicemaker.cm.gui.utils.viewer.CompositePaneModel;
import com.choicemaker.cm.modelmaker.gui.listeners.EvaluationEvent;
import com.choicemaker.cm.module.swing.AbstractTabbedPanel;

/**
 * @author rphall
 */
public abstract class AbstractPairReviewPanel extends AbstractTabbedPanel {
	private static final long serialVersionUID = 1L;
	public abstract void propertyChange(PropertyChangeEvent evt);
	public abstract void setChanged(RepositoryChangeEvent evt);
	public abstract void recordDataChanged(RepositoryChangeEvent evt);
	public abstract void markupDataChanged(RepositoryChangeEvent evt);
	public abstract void markedRecordPairSelected(int index);
	public abstract void markedRecordPairSelected(ImmutableMarkedRecordPair mrp);
	public abstract CompositePaneModel getRecordPairViewerModel();
	public abstract void setDefaultLayout();
	public abstract void setRecordPairViewerModel(CompositePaneModel rpvl);
	/**
	 * Returns the viewer.
	 * @return RecordPairViewer
	 */
	public abstract CompositePane getViewer();
//
  public abstract void showActiveCluesPanel(boolean b);

  public abstract void setSelectionSize(int ss);

  public abstract void saveData();

  public abstract void evaluated(EvaluationEvent evt);

  public abstract void setCurrentLayout(CompositePaneModel l);
  public abstract CompositePaneModel getCurrentLayout();
  public abstract void displayRecordPairFilterDialog();
//
}
