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
package com.choicemaker.cm.modelmaker.gui.panels;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.log4j.Logger;
import org.jdom.Element;

import com.choicemaker.cm.core.RepositoryChangeEvent;
import com.choicemaker.cm.core.RepositoryChangeListener;
import com.choicemaker.cm.core.base.DescriptorCollection;
import com.choicemaker.cm.core.base.MutableMarkedRecordPair;
import com.choicemaker.cm.gui.utils.viewer.CompositePane;
import com.choicemaker.cm.gui.utils.viewer.CompositePaneModel;
import com.choicemaker.cm.gui.utils.viewer.xmlconf.RecordPairViewerXmlConf;
import com.choicemaker.cm.modelmaker.ModelMakerEventNames;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;

/**
 * The panel that displays the data contained in a MarkedRecordPair.  Users may
 * modify both the layout and the actual data in the pair.
 * 
 * @author Martin Buechi
 * @author S. Yoakum-Stover
 * @version $Revision: 1.2 $ $Date: 2010/03/29 13:06:20 $
 */
public class RecordPairViewerPanel implements RepositoryChangeListener, PropertyChangeListener {
	private static Logger logger = Logger.getLogger(RecordPairViewerPanel.class);
	public static RecordPairViewerPanel instance;

	private HumanReviewPanel parent;
	private ModelMaker modelMaker;

	private CompositePane viewer;

	public RecordPairViewerPanel(HumanReviewPanel g) {
		super();
		instance = this;
		parent = g;
		modelMaker = parent.getModelMaker();

		viewer = new CompositePane(true, true);

		modelMaker.addPropertyChangeListener(this);
		modelMaker.getProbabilityModelEventMultiplexer().addPropertyChangeListener(this);
		modelMaker.addMarkedRecordPairDataChangeListener(this);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		String propertyName = evt.getPropertyName();
		Object src = evt.getSource();
		if (src == modelMaker) {
			if (propertyName == ModelMakerEventNames.PROBABILITY_MODEL) {
				CompositePaneModel compositePaneModel =
					new CompositePaneModel(modelMaker.getProbabilityModel().getAccessor().getDescriptor());
				compositePaneModel.setEnableEditing(true);
				viewer.setCompositePaneModel(compositePaneModel);
			} else if (propertyName == ModelMakerEventNames.MARKED_RECORD_PAIR_SOURCE) {
				viewer.setRecordData(null);
			} else if (propertyName == ModelMakerEventNames.MARKED_RECORD_PAIR) {
				markedRecordPairSelected(((Integer) evt.getNewValue()).intValue());
			}
		} else if (src == modelMaker.getProbabilityModel() && propertyName == null) {
			Element e = RecordPairViewerXmlConf.modelToXml(viewer.getCompositePaneModel());
			String fileName = viewer.getCompositePaneModel().getFileName();
			CompositePaneModel compositePaneModel =
				RecordPairViewerXmlConf.compositePaneModelFromXml(
					e,
					new DescriptorCollection(modelMaker.getProbabilityModel().getAccessor().getDescriptor()));
			compositePaneModel.setEnableEditing(true);
			compositePaneModel.setFileName(fileName);
			viewer.setCompositePaneModel(compositePaneModel);
			viewer.validate();
		}
	}

	public void setChanged(RepositoryChangeEvent evt) {
		viewer.setRecordData(null);
	}

	public void recordDataChanged(RepositoryChangeEvent evt) {
	}

	public void markupDataChanged(RepositoryChangeEvent evt) {
	}

	public void markedRecordPairSelected(int index) {
		viewer.setRecordData((MutableMarkedRecordPair) modelMaker.getSourceList().get(index));
	}

	public void markedRecordPairSelected(MutableMarkedRecordPair mrp) {
		viewer.setRecordData(mrp);
	}

	public CompositePaneModel getRecordPairViewerModel() {
		return viewer.getCompositePaneModel();
	}

	public void setDefaultLayout() {
		CompositePaneModel layout =
			new CompositePaneModel(modelMaker.getProbabilityModel().getAccessor().getDescriptor());
		layout.setEnableEditing(true);
		viewer.setCompositePaneModel(layout);
	}

	public void setRecordPairViewerModel(CompositePaneModel rpvl) {
		viewer.setCompositePaneModel(rpvl);
	}
	/**
	 * Returns the viewer.
	 * @return RecordPairViewer
	 */
	public CompositePane getViewer() {
		return viewer;
	}
}
