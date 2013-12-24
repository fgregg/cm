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

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.Thresholds;
import com.choicemaker.cm.core.util.MessageUtil;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.stats.StatPoint;

public class Calculator extends JPanel {
	private static Logger logger = Logger.getLogger(Calculator.class);
	private TestingControlPanel parent;
	private JComboBox firstTpe;
	private JComboBox sndTpe;
	private JTextField firstFld;
	private JTextField sndFld;
	private JButton calculate;
	private JTextField differThreshold;
	private JTextField matchThreshold;
	private JTextField falseNegatives;
	private JTextField falsePositives;
	private JTextField differRecall;
	private JTextField matchRecall;
	private JTextField humanReview;
	private JButton setDifferThreshold;
	private JButton setMatchThreshold;
	private JButton setBothThresholds;
	private StatPoint pt;
	private static final DecimalFormat DF2 = new DecimalFormat("##0.00");
	private static final int THRESHOLD = 0;
	private static final int ERROR = 1;
	private static final int HR = 2;
	private static final int NONE = 3;

	public Calculator(TestingControlPanel g) {
		parent = g;
		buildPanel();
		layoutPanel();
		addListeners();
		reset();
	}

	public void reset() {
		calculate.setEnabled(false);
		setDifferThreshold.setEnabled(false);
		setMatchThreshold.setEnabled(false);
		setBothThresholds.setEnabled(false);
		differThreshold.setText("");
		matchThreshold.setText("");
		differRecall.setText("");
		matchRecall.setText("");
		falseNegatives.setText("");
		falsePositives.setText("");
		humanReview.setText("");
	}

	private void buildPanel() {
		firstTpe =
			new JComboBox(
				new String[] {
					MessageUtil.m.formatMessage("train.gui.modelmaker.panel.calc.differ.threshold"),
					MessageUtil.m.formatMessage("train.gui.modelmaker.panel.calc.false.negatives"),
					MessageUtil.m.formatMessage("train.gui.modelmaker.panel.calc.human.review")});
		sndTpe =
			new JComboBox(
				new String[] {
					MessageUtil.m.formatMessage("train.gui.modelmaker.panel.calc.match.threshold"),
					MessageUtil.m.formatMessage("train.gui.modelmaker.panel.calc.false.positives"),
					MessageUtil.m.formatMessage("train.gui.modelmaker.panel.calc.human.review"),
					MessageUtil.m.formatMessage("train.gui.modelmaker.panel.calc.none")});
		firstFld = new JTextField(5);
		sndFld = new JTextField(5);
		calculate = new JButton(MessageUtil.m.formatMessage("train.gui.modelmaker.panel.calc.calculate"));
		differThreshold = new JTextField(5);
		differThreshold.setEditable(false);
		matchThreshold = new JTextField(5);
		matchThreshold.setEditable(false);
		falseNegatives = new JTextField(5);
		falseNegatives.setEditable(false);
		falsePositives = new JTextField(5);
		falsePositives.setEditable(false);
		differRecall = new JTextField(5);
		differRecall.setEditable(false);
		matchRecall = new JTextField(5);
		matchRecall.setEditable(false);
		humanReview = new JTextField(5);
		humanReview.setEditable(false);
		setDifferThreshold = new JButton(MessageUtil.m.formatMessage("train.gui.modelmaker.panel.calc.set.differ"));
		setMatchThreshold = new JButton(MessageUtil.m.formatMessage("train.gui.modelmaker.panel.calc.set.match"));
		setBothThresholds = new JButton(MessageUtil.m.formatMessage("train.gui.modelmaker.panel.calc.set.both"));
	}

	private String formatPercentage(float v) {
		if (Float.isNaN(v)) {
			return "--";
		} else {
			return DF2.format(v * 100) + " %";
		}
	}

	private void addListeners() {
		calculate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					reset();
					calculate.setEnabled(true);
					pt = new StatPoint();
					float firstVal = Float.parseFloat(firstFld.getText()) / 100f;
					switch (firstTpe.getSelectedIndex()) {
						case THRESHOLD :
							pt.differThreshold = firstVal;
							break;
						case ERROR :
							pt.falseNegatives = firstVal;
							break;
						case HR :
							pt.humanReview = firstVal;
							break;
					}
					int idx = sndTpe.getSelectedIndex();
					if (idx != NONE) {
						float sndVal = Float.parseFloat(sndFld.getText()) / 100f;
						switch (idx) {
							case THRESHOLD :
								pt.matchThreshold = sndVal;
								break;
							case ERROR :
								pt.falsePositives = sndVal;
								break;
							case HR :
								pt.humanReview = sndVal;
								break;
						}
					}
					parent.getModelMaker().getStatistics().computeStatPoint(pt);
					differThreshold.setText(formatPercentage(pt.differThreshold));
					falseNegatives.setText(formatPercentage(pt.falseNegatives));
					differRecall.setText(formatPercentage(pt.differRecall));
					matchThreshold.setText(formatPercentage(pt.matchThreshold));
					falsePositives.setText(formatPercentage(pt.falsePositives));
					matchRecall.setText(formatPercentage(pt.matchRecall));
					humanReview.setText(formatPercentage(pt.humanReview));
					boolean vd = !Float.isNaN(pt.differThreshold);
					boolean vm = !Float.isNaN(pt.matchThreshold);
					setDifferThreshold.setEnabled(vd);
					setMatchThreshold.setEnabled(vm);
					setBothThresholds.setEnabled(vd && vm);
				} catch (NumberFormatException ex) {
					// ignore
				}

			}
		});
		ActionListener l = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledness();
			}
		};
		firstTpe.addActionListener(l);
		sndTpe.addActionListener(l);
		setDifferThreshold.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ModelMaker mm = parent.getModelMaker();
				Thresholds t = mm.getThresholds();
				mm.setThresholds(new Thresholds(pt.differThreshold, t.getMatchThreshold()));
			}
		});
		setMatchThreshold.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ModelMaker mm = parent.getModelMaker();
				Thresholds t = mm.getThresholds();
				mm.setThresholds(new Thresholds(t.getDifferThreshold(), pt.matchThreshold));
			}
		});
		setBothThresholds.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ModelMaker mm = parent.getModelMaker();
				mm.setThresholds(new Thresholds(pt.differThreshold, pt.matchThreshold));
			}
		});
	}

	public void setEnabledness() {
		int fi = firstTpe.getSelectedIndex();
		int si = sndTpe.getSelectedIndex();
		calculate.setEnabled(
			(si != NONE || fi == HR) && !(fi == HR && si == HR) && parent.getModelMaker().isEvaluated());
		sndFld.setEnabled(si != NONE);
	}

	private JPanel layoutInputPanel() {
		JPanel inputPanel = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		inputPanel.setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(2, 2, 5, 10);
		c.gridx = 0;
		c.gridy = 0;
		inputPanel.add(firstTpe, c);
		c.gridx = 1;
		inputPanel.add(firstFld, c);
		c.gridx = 0;
		c.gridy = 1;
		inputPanel.add(sndTpe, c);
		c.gridx = 1;
		inputPanel.add(sndFld, c);
		c.gridx = 0;
		c.gridy = 2;
		inputPanel.add(calculate, c);
		inputPanel.setBorder(
			BorderFactory.createTitledBorder(MessageUtil.m.formatMessage("train.gui.modelmaker.panel.calc.input")));
		return inputPanel;
	}

	private JPanel layoutOutputPanel() {
		JPanel outputPanel = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		outputPanel.setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(2, 2, 5, 10);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		outputPanel.add(new JLabel(MessageUtil.m.formatMessage("train.gui.modelmaker.panel.calc.differ.threshold")), c);
		c.gridx = 1;
		outputPanel.add(differThreshold, c);
		c.gridx = 2;
		outputPanel.add(new JLabel(MessageUtil.m.formatMessage("train.gui.modelmaker.panel.calc.false.negatives")), c);
		c.gridx = 3;
		outputPanel.add(falseNegatives, c);
		c.gridx = 4;
		outputPanel.add(new JLabel(MessageUtil.m.formatMessage("train.gui.modelmaker.panel.calc.differ.recall")), c);
		c.gridx = 5;
		outputPanel.add(differRecall, c);
		c.gridx = 6;
		outputPanel.add(setDifferThreshold, c);
		c.gridx = 7;
		c.gridheight = 2;
		outputPanel.add(setBothThresholds, c);
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = 1;
		outputPanel.add(new JLabel(MessageUtil.m.formatMessage("train.gui.modelmaker.panel.calc.match.threshold")), c);
		c.gridx = 1;
		outputPanel.add(matchThreshold, c);
		c.gridx = 2;
		outputPanel.add(new JLabel(MessageUtil.m.formatMessage("train.gui.modelmaker.panel.calc.false.positives")), c);
		c.gridx = 3;
		outputPanel.add(falsePositives, c);
		c.gridx = 4;
		outputPanel.add(new JLabel(MessageUtil.m.formatMessage("train.gui.modelmaker.panel.calc.match.recall")), c);
		c.gridx = 5;
		outputPanel.add(matchRecall, c);
		c.gridx = 6;
		outputPanel.add(setMatchThreshold, c);
		c.gridx = 0;
		c.gridy = 2;
		outputPanel.add(new JLabel(MessageUtil.m.formatMessage("train.gui.modelmaker.panel.calc.human.review")), c);
		c.gridx = 1;
		outputPanel.add(humanReview, c);
		outputPanel.setBorder(
			BorderFactory.createTitledBorder(MessageUtil.m.formatMessage("train.gui.modelmaker.panel.calc.output")));
		return outputPanel;
	}

	private void layoutPanel() {
		setLayout(new FlowLayout(FlowLayout.LEFT));
		JPanel p = new JPanel();
		add(p);
		GridBagLayout layout = new GridBagLayout();
		p.setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(2, 2, 5, 10);
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridy = 0;
		p.add(layoutInputPanel(), c);
		c.gridy = 1;
		p.add(layoutOutputPanel(), c);
	}
}
