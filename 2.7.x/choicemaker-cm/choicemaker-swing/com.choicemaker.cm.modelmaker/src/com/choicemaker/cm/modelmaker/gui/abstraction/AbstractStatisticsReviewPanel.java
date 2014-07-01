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

import com.choicemaker.cm.core.RepositoryChangeEvent;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.listeners.EvaluationEvent;
import com.choicemaker.cm.module.swing.AbstractTabbedPanel;

/**
 * @author rphall
 */
public abstract class AbstractStatisticsReviewPanel extends AbstractTabbedPanel {

	private static final long serialVersionUID = 1L;

	public abstract boolean isEvaluated();

	public abstract ModelMaker getModelMaker();

	public abstract void propertyChange(PropertyChangeEvent evt);

	public abstract void evaluated(EvaluationEvent evt);

	public abstract void setChanged(RepositoryChangeEvent evt);

	public abstract void recordDataChanged(RepositoryChangeEvent evt);

	public abstract void markupDataChanged(RepositoryChangeEvent evt);

	public abstract void dataChanged();

	public abstract void plotStatistics();
	
}

