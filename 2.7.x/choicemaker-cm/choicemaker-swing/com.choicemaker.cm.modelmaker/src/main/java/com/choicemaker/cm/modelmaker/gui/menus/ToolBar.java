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
package com.choicemaker.cm.modelmaker.gui.menus;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.Thresholds;
import com.choicemaker.cm.core.util.ChoiceMakerCoreMessages;
import com.choicemaker.cm.modelmaker.ModelMakerEventNames;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.utils.Enable;
import com.choicemaker.cm.modelmaker.gui.utils.EnablednessGuard;

/**
 *
 * @author    
 * @version   $Revision: 1.2 $ $Date: 2010/03/29 13:15:55 $
 */
public class ToolBar extends JToolBar implements PropertyChangeListener, Enable {
	private static final long serialVersionUID = 1L;
	private ModelMaker parent;
	private JTextField differThreshold;
	private JTextField matchThreshold;
	private JButton setButton;
	private DecimalFormat df;
	private JComponent thresholdComponents[];

	public ToolBar(ModelMaker parent, String name) {
		super(name);
		this.parent = parent;
		df = new DecimalFormat("##0.00");
	}

	public void addThresholds() {
		thresholdComponents = new JComponent[5];
		ImageIcon divider = new ImageIcon(ModelMaker.class.getResource("images/divider2.gif"));
		add(new JLabel(divider));
		JLabel l = new JLabel(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.toolbar.differ.threshold"));
		thresholdComponents[0] = l;
		l.setBorder(new EmptyBorder(0, 5, 0, 3));
		add(l);
		differThreshold = new JTextField(5);
		thresholdComponents[1] = differThreshold;
		differThreshold.setHorizontalAlignment(JTextField.RIGHT);
		Dimension size = new Dimension(50, 20);
		differThreshold.setPreferredSize(size);
		differThreshold.setMaximumSize(size);
		add(differThreshold);
		l = new JLabel(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.toolbar.match.threshold"));
		thresholdComponents[2] = l; 
		l.setBorder(new EmptyBorder(0, 10, 0, 3));
		add(l);
		matchThreshold = new JTextField(5);
		thresholdComponents[3] = matchThreshold; 
		matchThreshold.setHorizontalAlignment(JTextField.RIGHT);
		matchThreshold.setPreferredSize(size);
		matchThreshold.setMaximumSize(size);
		add(matchThreshold);
		displayThresholds(parent.getThresholds());
		setButton = new JButton(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.toolbar.set.thresholds"));
		thresholdComponents[4] = setButton; 
		setButton.setBorder(new CompoundBorder(new EmptyBorder(0, 5, 0, 5), setButton.getBorder()));
		add(setButton);
		add(new JLabel(divider));
		setButton.setEnabled(false);
		addListeners();
		parent.addPropertyChangeListener(this);
		parent.getProbabilityModelEventMultiplexer().addPropertyChangeListener(this);
		setThresholdsEnabled();
	}

	private void addListeners() {
		//mergePointsListener
		ActionListener mergePointsListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean success = getMatchPoints();
				setButton.setEnabled(!success);
			}
		};

		//matchThreshold
		matchThreshold.addActionListener(mergePointsListener);

		//differThreshold
		differThreshold.addActionListener(mergePointsListener);
		setButton.addActionListener(mergePointsListener);

		EnablednessGuard dl = new EnablednessGuard(this);
		differThreshold.getDocument().addDocumentListener(dl);
		matchThreshold.getDocument().addDocumentListener(dl);
	}

	public void setEnabledness() {
		setButton.setEnabled(true);
	}

	private void displayThresholds(Thresholds t) {
		matchThreshold.setText(df.format(t.getMatchThreshold() * 100f));
		differThreshold.setText(df.format(t.getDifferThreshold() * 100f));
	}

	private boolean getMatchPoints() {
		try {
			final float differPoint = Float.parseFloat(differThreshold.getText());
			final float differPoint2 = differPoint / 100f;
			final float matchPoint = Float.parseFloat(matchThreshold.getText());
			final float matchPoint2 = matchPoint / 100f;
			// If the user entered thresholds in reverse order
			// (i.e. matchThreshold < differThreshold), then the Thresholds
			// constructor will automatically "un-reverse" them, which
			// is all good, except that the UI needs to be updated. In principle,
			// the test requires checking that the actual values match the
			// specified values to within float precision; in practice, one can
			// ignore the issue of finite precision here.
			Thresholds possiblyTweaked = new Thresholds(differPoint2, matchPoint2);
			boolean isNoTweak = differPoint2 == possiblyTweaked.getDifferThreshold()
				&& matchPoint2 == possiblyTweaked.getMatchThreshold();
			if ( !isNoTweak ) {
				// Definitely tweaked
				displayThresholds(possiblyTweaked);
			}
			parent.setThresholds(possiblyTweaked);
		} catch (NumberFormatException ex) {
			return false;
		}
		return true;
	}

	public void propertyChange(PropertyChangeEvent evt) {
		String propertyName = evt.getPropertyName();
		Object source = evt.getSource();
		if (source == parent) {
			if (propertyName == ModelMakerEventNames.THRESHOLDS) {
				Thresholds t = (Thresholds) evt.getNewValue();
				displayThresholds(t);
				setButton.setEnabled(false);
			} else if (propertyName == ModelMakerEventNames.PROBABILITY_MODEL || propertyName == null) {
				setThresholdsEnabled();
			}
		} else if (source == parent.getProbabilityModel()
				&& (ImmutableProbabilityModel.MACHINE_LEARNER == propertyName
					|| ImmutableProbabilityModel.MACHINE_LEARNER_PROPERTY == propertyName || propertyName == null)) {
			setThresholdsEnabled();
		}
	}

	private void setThresholdsEnabled() {
		boolean b = parent.getProbabilityModel() != null && parent.getProbabilityModel().getMachineLearner().isRegression();
		for (int i = 0; i < thresholdComponents.length; i++) {
			thresholdComponents[i].setEnabled(b);	
		}
	}
}
