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
package com.choicemaker.cm.modelmaker.filter;

import java.beans.PropertyChangeEvent;

import com.choicemaker.cm.analyzer.filter.MarkedRecordPairFilter;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.modelmaker.ModelMakerEventNames;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;

public class ModelMakerMRPFilter extends MarkedRecordPairFilter implements
		ListeningMarkedRecordPairFilter {

	private static final long serialVersionUID = 1L;
	private final ModelMaker parent;

	public ModelMakerMRPFilter(ModelMaker parent) {
		this.parent = parent;
		parent.addPropertyChangeListener(this);
		parent.getProbabilityModelEventMultiplexer().addPropertyChangeListener(this);
		reset();
	}

	public void propertyChange(PropertyChangeEvent evt) {
		String propertyName = evt.getPropertyName();
		Object source = evt.getSource();
		if (source == parent) {
			if (propertyName == ModelMakerEventNames.PROBABILITY_MODEL) {
				setModel(parent.getProbabilityModel());
			}
		} else if (source == parent.getProbabilityModel() && propertyName == null) {
			setModel(parent.getProbabilityModel());
		}
	}

	public void setModel(IProbabilityModel model) {
		reset();
	}

}
