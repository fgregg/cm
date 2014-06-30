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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.base.RepositoryChangeEvent;
import com.choicemaker.cm.core.base.RepositoryChangeListener;
import com.choicemaker.cm.core.train.Trainer;
import com.choicemaker.cm.modelmaker.ModelMakerEventNames;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.listeners.EvaluationEvent;
import com.choicemaker.cm.modelmaker.gui.listeners.EvaluationListener;
import com.choicemaker.cm.modelmaker.gui.tables.CluePerformanceTable;
import com.choicemaker.cm.modelmaker.gui.tables.ClueTableModel;
import com.choicemaker.cm.modelmaker.gui.tables.ClueTablePanel;

/**
 * The panel from which training is initiated.  Users may turn clues on and off, 
 * manually set weights, evaluate the clues on the source to get the counts 
 * statistics, and train the model.
 * 
 * @author S. Yoakum-Stover
 * @version $Revision: 1.2 $ $Date: 2010/03/29 13:04:44 $
 */
public class TrainingControlPanel extends JPanel implements RepositoryChangeListener, PropertyChangeListener, EvaluationListener {

    private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(TrainingControlPanel.class);
    private ModelMaker parent;
    private Trainer trainer;

    private JPanel controlsPanel;
    private JScrollPane cluePerformancePanel;
    private CluePerformanceTable performanceTable;

	private ClueTablePanel clueTablePanel;

    private boolean dirty;

    public TrainingControlPanel(ModelMaker g) {
        super();
        parent = g;
        setBorder(BorderFactory.createEmptyBorder(15, 5, 5, 5));
        buildPanel();
        addListeners();
        // addContentListeners();
        layoutPanel();
        parent.addPropertyChangeListener(this);
        parent.addEvaluationListener(this);
        parent.getProbabilityModelEventMultiplexer().addPropertyChangeListener(this);
        parent.addMarkedRecordPairDataChangeListener(this);
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b && dirty) {
            display();
        }
    }

    private void buildPanel() {
        controlsPanel = new JPanel();
        buildCluePerformancePanel();

        //ClueTable
        clueTablePanel = new ClueTablePanel(parent);
    }

    private void buildCluePerformancePanel() {
        performanceTable = new CluePerformanceTable();
        cluePerformancePanel = new JScrollPane();
        cluePerformancePanel.getViewport().add(performanceTable);
        Dimension tableSize = new Dimension(400, 112);
        cluePerformancePanel.setMinimumSize(tableSize);
        cluePerformancePanel.setPreferredSize(tableSize);
        cluePerformancePanel.setMaximumSize(tableSize);
        cluePerformancePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5), BorderFactory.createLoweredBevelBorder()));
    }
    
    public ClueTableModel getClueTableModel() {
    	ClueTableModel retVal = (ClueTableModel) this.clueTablePanel.getClueTable().getModel();
    	return retVal;
    }

    public void showCluePerformancePanel(boolean b) {
        if (b) {
            add(cluePerformancePanel, BorderLayout.SOUTH);
        } else {
            remove(cluePerformancePanel);
        }
    }

    private void addListeners() {
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        Object source = evt.getSource();
        if (source == parent) {
            if (propertyName == ModelMakerEventNames.MARKED_RECORD_PAIR_SOURCE || propertyName == ModelMakerEventNames.PROBABILITY_MODEL) {
                performanceTable.reset();
            }
        } else if (source == parent.getProbabilityModel()) {
            if (propertyName == null) {
                performanceTable.reset();
            }
        }
    }

    public void evaluated(EvaluationEvent evt) {
        if (evt.isEvaluated()) {
            setDirty();
        } else {
            performanceTable.reset();
        }
    }

    private void setDirty() {
        if (isVisible()) {
            display();
        } else {
            dirty = true;
        }
    }

    private void display() {
        dirty = false;
        if (parent.isEvaluated()) {
            if (parent.isEvaluated())
                performanceTable.refresh(parent.getProbabilityModel(), parent.getTrainer());
        }
    }

    public void setChanged(RepositoryChangeEvent evt) {
        performanceTable.reset();
        clueTablePanel.refreshStatistics();
    }

    public void recordDataChanged(RepositoryChangeEvent evt) {
    }

    public void markupDataChanged(RepositoryChangeEvent evt) {
    }

    private void layoutPanel() {
        //Place controls and clue table using BorderLayout***************************                      
        BorderLayout bl = new BorderLayout();
        setLayout(bl);
        add(clueTablePanel, BorderLayout.CENTER);
    }

}
