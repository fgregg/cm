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
package com.choicemaker.cm.modelmaker.gui.tables;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.modelmaker.ModelMakerEventNames;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.listeners.EvaluationEvent;
import com.choicemaker.cm.modelmaker.gui.listeners.EvaluationListener;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.2 $ $Date: 2010/03/29 13:43:03 $
 */
public class ClueTablePanel extends JScrollPane implements PropertyChangeListener, EvaluationListener {
	private static final long serialVersionUID = 1L;

	private ModelMaker modelMaker;
	
	// Use get/set methods
	private ClueTable clueTable;

	public ClueTablePanel(ModelMaker modelMaker) {
		this.modelMaker = modelMaker;
		setBorder(
			BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(5, 10, 5, 5),
				BorderFactory.createLoweredBevelBorder()));
		modelMaker.addPropertyChangeListener(this);
		modelMaker.getProbabilityModelEventMultiplexer().addPropertyChangeListener(this);
		modelMaker.addEvaluationListener(this);
	}

	/**
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		String propertyName = evt.getPropertyName();
		Object source = evt.getSource();
		if (source == modelMaker) {
			if (propertyName == ModelMakerEventNames.PROBABILITY_MODEL) {
				replaceClueTable();
			} else if (propertyName == ModelMakerEventNames.MARKED_RECORD_PAIR_SOURCE) {
				refreshStatistics();
			}
		} else if (source == modelMaker.getProbabilityModel()) {
			if (propertyName == null || propertyName == ImmutableProbabilityModel.MACHINE_LEARNER) {
				replaceClueTable();
			} else if (
				propertyName == ImmutableProbabilityModel.MACHINE_LEARNER_PROPERTY
					|| propertyName == ImmutableProbabilityModel.CLUES_TO_EVALUATE) {
				refreshStatistics();
			}
		}
	}

	public void evaluated(EvaluationEvent evt) {
		refreshStatistics();
	}

	public void refreshStatistics() {
		replaceClueTable();
	}

	private void replaceClueTable() {
		if(getClueTable() != null) {
			getViewport().remove(getClueTable());
		}
		ImmutableProbabilityModel model = modelMaker.getProbabilityModel();
		if(model != null) {
			setClueTable(new ClueTable(modelMaker));
			getViewport().add(getClueTable());
		}
	}

	private void setClueTable(ClueTable clueTable) {
		this.clueTable = clueTable;
	}

	public ClueTable getClueTable() {
		return clueTable;
	}
}
