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
import java.awt.GridLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.base.ImmutableThresholds;
import com.choicemaker.cm.core.util.ChoiceMakerCoreMessages;
import com.choicemaker.cm.core.util.LoggingObject;
import com.choicemaker.cm.modelmaker.filter.ListeningMarkedRecordPairFilter;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.dialogs.RecordPairFilterDialog;
import com.choicemaker.cm.modelmaker.gui.tables.filtercluetable.FilterClueTable;

/**
 * .
 *
 * @author   Arturo Falck
 * @version  $Revision: 1.2 $ $Date: 2010/03/29 12:57:39 $
 */
public class FilterCluePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static Logger logger =
		Logger.getLogger(RecordPairFilterDialog.class.getName());
	private static final DecimalFormat DF = new DecimalFormat("##0.00");

	private ModelMaker parent;
	private ListeningMarkedRecordPairFilter filter;
	private FilterClueTable clueTable;
	private JScrollPane clueTableScrollPane;
	private JLabel differLabel;
	private JLabel holdLabel;
	private JLabel matchLabel;
	private JLabel humanLabel;
	private JLabel choiceMakerLabel;
//	private JLabel probRangeLabel;
	private JLabel limiterLabel;
	private JLabel lowLabel;
	private JLabel hiLabel;
	private JCheckBox[] humanDecision;
	private JCheckBox[] choiceMakerDecision;
	private JTextField pLow;
	private JTextField pHi;
	private JTextField limiters;
	private boolean includeHumanDecision;
	private JCheckBox checked;

	private final float default_probabilityLowerBound = (float) ImmutableThresholds.MIN_VALUE;
	private final float default_probabilityUpperBound = (float) ImmutableThresholds.MAX_VALUE;

	public FilterCluePanel(ModelMaker parent, ListeningMarkedRecordPairFilter filter) {
		this(parent, filter, true);
	}

	public FilterCluePanel(
		ModelMaker parent,
		ListeningMarkedRecordPairFilter filter,
		boolean includeHumanDecision) {
		super();
		if (parent == null) {
			throw new IllegalArgumentException("null ModelMaker");
		}
		if (filter == null) {
			throw new IllegalArgumentException("null filter");
		}
		this.includeHumanDecision = includeHumanDecision;
		this.parent = parent;
		this.filter = filter;
		buildPanel();
		layoutPanel();

		setClues();
		updateDisplay();
	}

	protected void setClues() {
		clueTable.setClues(parent.getProbabilityModel());
	}

	public void reset() {
		filter.reset();
		clueTable.deselectAll();
		updateDisplay();
	}

	private void updateDisplay() {
		if (includeHumanDecision) {
			boolean[] hd = filter.getHumanDecision();
			for (int i = 0; i < Decision.NUM_DECISIONS; ++i) {
				humanDecision[i].setSelected(hd[i]);
			}
		}
		boolean[] md = filter.getChoiceMakerDecision();
		for (int i = 0; i < Decision.NUM_DECISIONS; ++i) {
			choiceMakerDecision[i].setSelected(md[i]);
		}
		setThresholdText(pLow, filter.getFromPercentage());
		setThresholdText(pHi, filter.getFromPercentage());
		clueTable.select(filter.getConditions());
		limiters.setText(filter.getLimitersAsString());
		checked.setSelected(filter.getCollection() != null);
	}

	private void setThresholdText(JTextField jtf, float f) {
		jtf.setText(DF.format(f * 100f));
	}

	public void set() {
		boolean[] hd = new boolean[Decision.NUM_DECISIONS];
		boolean[] md = new boolean[Decision.NUM_DECISIONS];
		for (int i = 0; i < Decision.NUM_DECISIONS; ++i) {
			hd[i] = humanDecision[i].isSelected();
			md[i] = choiceMakerDecision[i].isSelected();
		}
		if (includeHumanDecision) {
			filter.setHumanDecision(hd);
		}
		filter.setChoiceMakerDecision(md);
		try {
			filter.setFromPercentage(
				getThresholdValue(pLow, this.default_probabilityLowerBound));
			filter.setToPercentage(
				getThresholdValue(pHi, this.default_probabilityUpperBound));
			filter.setLimiters(limiters.getText());
		} catch (NumberFormatException ex) {
			// ignore
		} catch (IllegalArgumentException ex) {
			logger.severe(new LoggingObject("CM-100101").getFormattedMessage() + ": " + ex);
		}
		filter.setConditions(clueTable.getFilterConditions());
		filter.setCollection(checked.isSelected() ? parent.getChecked() : null);
	}

	private float getThresholdValue(JTextField jtf, float defaultValue) {
		String s = jtf.getText();
		float retVal;
		try {
			retVal = Float.parseFloat(s);
			retVal = retVal / 100f;
		} catch (Exception x) {
			retVal = defaultValue;
		}
		return retVal;
	}

	public void buildPanel() {
		// 2014-04-24 rphall: Commented out unused local variable.
//		JPanel content = this;

		clueTable = createClueTable();
		clueTableScrollPane = new JScrollPane();
		clueTableScrollPane.getViewport().add(clueTable);
		clueTableScrollPane.setPreferredSize(new Dimension(400, 100));
		clueTableScrollPane.setMinimumSize(new Dimension(400, 100));

		differLabel = new JLabel(ChoiceMakerCoreMessages.m.formatMessage("differ"));
		holdLabel = new JLabel(ChoiceMakerCoreMessages.m.formatMessage("hold"));
		matchLabel = new JLabel(ChoiceMakerCoreMessages.m.formatMessage("match"));

		humanLabel =
			new JLabel(
				ChoiceMakerCoreMessages.m.formatMessage(
					"train.gui.modelmaker.dialog.recordpairfilter.marked.human"),
				JLabel.LEFT);
		choiceMakerLabel =
			new JLabel(
				ChoiceMakerCoreMessages.m.formatMessage(
					"train.gui.modelmaker.dialog.recordpairfilter.marked.choicemaker"),
				JLabel.LEFT);

		humanDecision = new JCheckBox[Decision.NUM_DECISIONS];
		choiceMakerDecision = new JCheckBox[Decision.NUM_DECISIONS];
		for (int i = 0; i < Decision.NUM_DECISIONS; ++i) {
			humanDecision[i] = new JCheckBox();
			choiceMakerDecision[i] = new JCheckBox();
		}

//		probRangeLabel =
//			new JLabel(
//				ChoiceMakerCoreMessages.m.formatMessage(
//					"train.gui.modelmaker.dialog.recordpairfilter.probabilityrange"),
//				JLabel.LEFT);
		lowLabel =
			new JLabel(
				ChoiceMakerCoreMessages.m.formatMessage(
					"train.gui.modelmaker.dialog.recordpairfilter.probabilityrange.low"),
				JLabel.LEFT);
		pLow = new JTextField("", 5);
		hiLabel =
			new JLabel(
				ChoiceMakerCoreMessages.m.formatMessage(
					"train.gui.modelmaker.dialog.recordpairfilter.probabilityrange.high"),
				JLabel.LEFT);
		pHi = new JTextField("", 5);

		limiterLabel = new JLabel("Limited by:");
		limiters = new JTextField("", 15);
		checked = new JCheckBox();
	}

	protected FilterClueTable createClueTable() {
		return new FilterClueTable(parent);
	}

	private void layoutPanel() {

		JPanel decisionPanel = createDecisionPanel();
		JPanel limiterPanel = createLimiterPanel();

		this.setLayout(new BorderLayout(10, 10));
		this.add(decisionPanel, BorderLayout.NORTH);
		this.add(clueTableScrollPane, BorderLayout.CENTER);
		this.add(limiterPanel, BorderLayout.SOUTH);
	}

	private JPanel createDecisionPanel() {
		JPanel decisionsPanel = new JPanel();

		if (includeHumanDecision) {
			decisionsPanel.setLayout(new GridLayout(4, 4, 10, 2));
		} else {
			decisionsPanel.setLayout(new GridLayout(3, 4, 10, 2));
		}

		decisionsPanel.add(Box.createHorizontalStrut(5));
		decisionsPanel.add(differLabel);
		decisionsPanel.add(matchLabel);
		decisionsPanel.add(holdLabel);

		if (includeHumanDecision) {
			decisionsPanel.add(humanLabel);
			for (int i = 0; i < Decision.NUM_DECISIONS; ++i) {
				decisionsPanel.add(humanDecision[i]);
			}
		}

		decisionsPanel.add(choiceMakerLabel);
		for (int i = 0; i < Decision.NUM_DECISIONS; ++i) {
			decisionsPanel.add(choiceMakerDecision[i]);
		}

		decisionsPanel.add(lowLabel);
		decisionsPanel.add(pLow);
		decisionsPanel.add(hiLabel);
		decisionsPanel.add(pHi);

		JPanel returnValue = new JPanel();
		returnValue.setBorder(BorderFactory.createTitledBorder("Decisions"));
		returnValue.setLayout(new BorderLayout());
		returnValue.add(decisionsPanel, BorderLayout.CENTER);
		returnValue.add(Box.createHorizontalStrut(5), BorderLayout.WEST);
		returnValue.add(Box.createHorizontalStrut(5), BorderLayout.EAST);
		return returnValue;
	}

	private JPanel createLimiterPanel() {
		GridBagLayout layout = new GridBagLayout();
		layout.columnWeights = new double[] { 0, 1 };
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 5, 10);

		JPanel limiterPanel = new JPanel(layout);
		limiterPanel.setBorder(BorderFactory.createTitledBorder("Others"));

		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		limiterPanel.add(limiterLabel, c);
		c.gridx = 1;
		limiterPanel.add(limiters, c);

		if (includeHumanDecision) {
			c.gridy = 1;
			c.gridx = 0;
			limiterPanel.add(new JLabel("Checked only"), c);
			c.gridx = 1;
			limiterPanel.add(checked, c);
		}
		return limiterPanel;
	}

	/**
	 * Returns the filter.
	 * @return MarkedRecordPairFilter
	 */
	public ListeningMarkedRecordPairFilter getFilter() {
		return filter;
	}

	public void setChoiceMakerDecisionFilters(boolean[] b) {
		if (b.length == this.choiceMakerDecision.length) {
			for (int i = 0; i < this.choiceMakerDecision.length; i++) {
				this.choiceMakerDecision[i].setSelected(b[i]);
			}
		}
	}

	public boolean[] getChoiceMakerDecisionFilters() {
		boolean retVal[] = new boolean[this.choiceMakerDecision.length];
		for (int i = 0; i < this.choiceMakerDecision.length; i++) {
			retVal[i] = this.choiceMakerDecision[i].isSelected();
		}
		return retVal;
	}

	public void setProbabilityLowerBound(float probabilityLowerBound) {
		setThresholdText(this.pLow, probabilityLowerBound);
	}

	public float getProbabilityLowerBound() {
		return getThresholdValue(this.pLow, this.default_probabilityLowerBound);
	}

	public void setProbabilityUpperBound(float probabilityUpperBound) {
		setThresholdText(this.pHi, probabilityUpperBound);
	}

	public float getProbabilityUpperBound() {
		return this.getThresholdValue(
			this.pHi,
			this.default_probabilityUpperBound);
	}

}
