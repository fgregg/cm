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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import com.choicemaker.cm.core.RepositoryChangeEvent;
import com.choicemaker.cm.core.RepositoryChangeListener;
import com.choicemaker.cm.core.util.ChoiceMakerCoreMessages;
import com.choicemaker.cm.modelmaker.ModelMakerEventNames;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.listeners.EvaluationEvent;
import com.choicemaker.cm.modelmaker.gui.listeners.EvaluationListener;
import com.choicemaker.cm.modelmaker.gui.tables.ConfusionTable;
import com.choicemaker.cm.modelmaker.gui.tables.ConfusionTableModel;
import com.choicemaker.cm.modelmaker.stats.StatPoint;

/**
 * Panel for evaluating the performance of the probability model on a 
 * MarkedRecordPair source.  Users may select a source, apply the model, and
 * display various statistics including the ConfusionMatrix and several kinds of
 * statistical plots.
 * 
 * @author S. Yoakum-Stover
 * @version $Revision: 1.2 $ $Date: 2010/03/29 13:03:48 $
 */
public class TestingControlPanel
	extends JPanel
	implements RepositoryChangeListener, PropertyChangeListener, EvaluationListener {
	private static final long serialVersionUID = 1L;
//	private static Logger logger = Logger.getLogger(TestingControlPanel.class);
	private ModelMaker parent;
//	private Trainer trainer;

	private JPanel confusionPanel;
	private JScrollPane confusionPane;
	private ConfusionTable confusionTable;
	private JLabel falsePositivesLabel;
	private JTextField falsePositives;
	private JLabel falseNegativesLabel;
	private JTextField falseNegatives;
	private JLabel differRecallLabel;
	private JTextField differRecall;
	private JLabel matchRecallLabel;
	private JTextField matchRecall;
	private JLabel humanReviewsLabel;
	private JTextField humanReviews;
//	private JLabel precisionLabel;
	private JTextField precision;
//	private JLabel recallLabel;
	private JTextField recall;
	private JLabel correlationLabel;
	private JTextField correlation;

	private JTabbedPane plotPanel;
	private JPanel percentagesPanel;
	private StatisticsHistogramPanel statisticsHistoPanel;

	private HoldVsAccuracyPlotPanel holdVsAccuracyPlotPanel;
	private AsymmetricThresholdVsAccuracyPlotPanel asymmetricHoldVsAccuracyPlotPanel;
	private Calculator calculator;
	private DecimalFormat df;
	private static final DecimalFormat DF4 = new DecimalFormat("0.0000");

	// FIXME UNUSED private JMenu testingMenu;
	private boolean dirty;

	private static final String HISTO = ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.panel.test.histogram");
	private static final String HOLDS = ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.panel.test.holdvsacc");
	private static final String ASYMM = ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.panel.test.asymm");
	private static final String CALC = ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.panel.test.calculator");

	public TestingControlPanel(ModelMaker g) {
		super();
		parent = g;
		df = new DecimalFormat("##0.00");
		setBorder(BorderFactory.createEmptyBorder(15, 5, 5, 5));
		buildPanel();
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

	public boolean isEvaluated() {
		return parent.isEvaluated();
	}

	public ModelMaker getModelMaker() {
		return parent;
	}

	private void buildPanel() {
		percentagesPanel = new JPanel();
		percentagesPanel.setBorder(
			BorderFactory.createTitledBorder(
				ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.panel.test.statistics.label")));

		falsePositivesLabel =
			new JLabel(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.panel.test.false.positives"), JLabel.RIGHT);
		falsePositives = new JTextField(5);
		falsePositives.setHorizontalAlignment(JTextField.RIGHT);
		falsePositives.setEditable(false);
		falseNegativesLabel =
			new JLabel(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.panel.test.false.negatives"), JLabel.RIGHT);
		falseNegatives = new JTextField(5);
		falseNegatives.setMinimumSize(falseNegatives.getPreferredSize());
		falseNegatives.setHorizontalAlignment(JTextField.RIGHT);
		falseNegatives.setEditable(false);
		differRecallLabel =
			new JLabel(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.panel.test.differ.recall"), JLabel.RIGHT);
		differRecall = new JTextField(5);
		differRecall.setMinimumSize(differRecall.getPreferredSize());
		differRecall.setHorizontalAlignment(JTextField.RIGHT);
		differRecall.setEditable(false);
		matchRecallLabel =
			new JLabel(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.panel.test.match.recall"), JLabel.RIGHT);
		matchRecall = new JTextField(5);
		matchRecall.setHorizontalAlignment(JTextField.RIGHT);
		matchRecall.setEditable(false);
		humanReviewsLabel =
			new JLabel(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.panel.test.humanreview"), JLabel.RIGHT);
		humanReviews = new JTextField(5);
		humanReviews.setHorizontalAlignment(JTextField.RIGHT);
		humanReviews.setEditable(false);
//		precisionLabel =
//			new JLabel(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.panel.test.precision"), JLabel.RIGHT);
		precision = new JTextField(5);
		precision.setHorizontalAlignment(JTextField.RIGHT);
		precision.setEditable(false);
//		recallLabel = new JLabel(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.panel.test.recall"), JLabel.RIGHT);
		recall = new JTextField(5);
		recall.setHorizontalAlignment(JTextField.RIGHT);
		recall.setEditable(false);
		correlationLabel =
			new JLabel(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.panel.test.correlation"), JLabel.RIGHT);
		correlation = new JTextField(5);
		correlation.setHorizontalAlignment(JTextField.RIGHT);
		correlation.setEditable(false);

		confusionTable = new ConfusionTable(this, new ConfusionTableModel());
		confusionTable.setEnabled(false);
		confusionPane = new JScrollPane();
		confusionPane.getViewport().add(confusionTable);
		confusionPane.setBorder(
			BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(5, 10, 5, 5),
				BorderFactory.createLoweredBevelBorder()));
		confusionPanel = new JPanel(new BorderLayout());
		confusionPanel.setBorder(
			BorderFactory.createTitledBorder(
				ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.panel.test.confusion.label")));
		confusionPanel.add(confusionPane, BorderLayout.CENTER);

		statisticsHistoPanel = new StatisticsHistogramPanel(this);
		holdVsAccuracyPlotPanel = new HoldVsAccuracyPlotPanel(this);
		asymmetricHoldVsAccuracyPlotPanel = new AsymmetricThresholdVsAccuracyPlotPanel(this);
		calculator = new Calculator(this);
		plotPanel = new JTabbedPane();
		plotPanel.addTab(HISTO, statisticsHistoPanel);
		plotPanel.addTab(HOLDS, holdVsAccuracyPlotPanel);
		plotPanel.addTab(ASYMM, asymmetricHoldVsAccuracyPlotPanel);
		plotPanel.addTab(CALC, calculator);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		String propertyName = evt.getPropertyName();
		Object source = evt.getSource();
		if (source == parent) {
			if (propertyName == ModelMakerEventNames.MARKED_RECORD_PAIR_SOURCE) {
				reset();
			} else if(propertyName == ModelMakerEventNames.PROBABILITY_MODEL || propertyName == null) {
				reset();
				setThresholdBasedEnabledness();
			} else if (propertyName == ModelMakerEventNames.THRESHOLDS) {
				if (parent.isEvaluated()) {
					refreshStatistics();
				}
			}
		} else if (source == parent.getProbabilityModel()) {
			if (propertyName == null) {
				reset();
			}
		}
	}
	
	private void setThresholdBasedEnabledness() {
		boolean b = parent.getProbabilityModel() != null && parent.getProbabilityModel().getMachineLearner().isRegression();
		calculator.setEnabled(b);
		asymmetricHoldVsAccuracyPlotPanel.setEnabled(b);
	}

	public void evaluated(EvaluationEvent evt) {
		if (evt.isEvaluated()) {
			setDirty();
		} else {
			reset();
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
			refreshStatistics();
			plotStatistics();
			confusionTable.setEnabled(true);
		}
	}

	public void setChanged(RepositoryChangeEvent evt) {
		reset();
	}

	public void recordDataChanged(RepositoryChangeEvent evt) {
		dataChanged();
	}

	public void markupDataChanged(RepositoryChangeEvent evt) {
		dataChanged();
	}

	public void dataChanged() {
		if (parent.haveSourceList()) {
			parent.getUserMessages().postInfo(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.panel.test.data.changed"));
		}
	}

	private void reset() {
		confusionTable.setEnabled(false);
		confusionTable.reset();
		blankStatistics();
		statisticsHistoPanel.reset();
		holdVsAccuracyPlotPanel.reset();
		asymmetricHoldVsAccuracyPlotPanel.reset();
		calculator.reset();
	}

	public void plotStatistics() {
		holdVsAccuracyPlotPanel.plot();
		asymmetricHoldVsAccuracyPlotPanel.plot();
		statisticsHistoPanel.plot();
		calculator.setEnabledness();
	}

	private String formatPercentage(float v) {
		if (Float.isNaN(v)) {
			return "--";
		} else {
			return df.format(v * 100) + " %";
		}
	}

	private void refreshStatistics() {
		StatPoint pt = parent.getStatistics().getCurrentStatPoint();
		falseNegatives.setText(formatPercentage(pt.falseNegatives));
		falsePositives.setText(formatPercentage(pt.falsePositives));
		differRecall.setText(formatPercentage(pt.differRecall));
		matchRecall.setText(formatPercentage(pt.matchRecall));
		humanReviews.setText(formatPercentage(pt.humanReview));
		precision.setText(formatPercentage(pt.precision));
		recall.setText(formatPercentage(pt.recall));
		correlation.setText(Float.isNaN(pt.correlation) ? "--" : DF4.format(pt.correlation));
		confusionTable.refresh();
	}

	private void blankStatistics() {
		falsePositives.setText("");
		falseNegatives.setText("");
		differRecall.setText("");
		matchRecall.setText("");
		humanReviews.setText("");
		precision.setText("");
		recall.setText("");
		correlation.setText("");
	}

	private void layoutPanel() {
		layoutPercentagesPanel();
		GridBagLayout lay = new GridBagLayout();
		JPanel topPanel = new JPanel(lay);

		GridBagConstraints c = new GridBagConstraints();

		Dimension tableSize = new Dimension(450, 97);
		confusionPane.setMinimumSize(new Dimension(300, 97));
		confusionPane.setPreferredSize(tableSize);
		confusionPane.setMaximumSize(tableSize);
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.WEST;
		c.gridheight = 2;
		c.weightx = 1;
		topPanel.add(confusionPanel, c);

		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.EAST;
		c.gridheight = 1;
		c.gridx = 2;
		c.weightx = 0;
		topPanel.add(percentagesPanel, c);

		//Here's the BorderLayout stuff.
		setLayout(new BorderLayout());
		add(topPanel, BorderLayout.NORTH);
		add(plotPanel, BorderLayout.CENTER);
	}

	private void layoutPercentagesPanel() {
		GridBagLayout lay = new GridBagLayout();
		lay.columnWidths = new int[] { 0, 0, 30, 0, 0 };
		GridBagConstraints c = new GridBagConstraints();
		percentagesPanel.setLayout(lay);
		c.insets = new Insets(2, 5, 2, 2);

		//Row 1..........................................................
		//falseNegativesLabel 
		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		lay.setConstraints(falseNegativesLabel, c);
		percentagesPanel.add(falseNegativesLabel);
		//falseNegatives
		c.gridx = 1;
		lay.setConstraints(falseNegatives, c);
		percentagesPanel.add(falseNegatives);

		c.gridx = 3;
		percentagesPanel.add(differRecallLabel, c);
		c.gridx = 4;
		percentagesPanel.add(differRecall, c);

		//Row 2..........................................................
		//falsePositivesLabel
		c.gridx = 0;
		c.gridy = 1;
		lay.setConstraints(falsePositivesLabel, c);
		percentagesPanel.add(falsePositivesLabel);
		//falsePositives
		c.gridx = 1;
		lay.setConstraints(falsePositives, c);
		percentagesPanel.add(falsePositives);

		c.gridx = 3;
		percentagesPanel.add(matchRecallLabel, c);
		c.gridx = 4;
		percentagesPanel.add(matchRecall, c);

		//Row 3..........................................................
		//correlationLabel
		c.gridy = 2;
		c.gridx = 0;
		percentagesPanel.add(humanReviewsLabel, c);
		c.gridx = 1;
		percentagesPanel.add(humanReviews, c);
		c.gridx = 3;
		lay.setConstraints(correlationLabel, c);
		percentagesPanel.add(correlationLabel);
		//correlation
		c.gridx = 4;
		lay.setConstraints(correlation, c);
		percentagesPanel.add(correlation);

		//Row 4..........................................................
		//recallLabel
		// 	c.gridy = 4;
		// 	c.gridx = 0;
		// 	percentagesPanel.add(precisionLabel, c);
		// 	c.gridx = 1;
		// 	percentagesPanel.add(precision, c);
		//         c.gridx = 3;
		//         lay.setConstraints(recallLabel, c);
		//         percentagesPanel.add(recallLabel);
		//         //recall
		//         c.gridx = 4;
		//         lay.setConstraints(recall, c);
		//         percentagesPanel.add(recall);
	}

}
