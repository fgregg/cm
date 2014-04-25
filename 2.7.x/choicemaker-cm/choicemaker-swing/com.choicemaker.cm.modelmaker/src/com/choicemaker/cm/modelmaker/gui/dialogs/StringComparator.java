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
package com.choicemaker.cm.modelmaker.gui.dialogs;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import com.choicemaker.cm.core.util.UpperCaseTextField;
import com.choicemaker.cm.gui.utils.ExtensionHolder;
import com.choicemaker.cm.gui.utils.JavaHelpUtils;
import com.choicemaker.cm.matching.en.DoubleMetaphone;
import com.choicemaker.cm.matching.en.Jaro;
import com.choicemaker.cm.matching.en.Metaphone;
import com.choicemaker.cm.matching.en.Nysiis;
import com.choicemaker.cm.matching.en.Soundex;
import com.choicemaker.cm.matching.gen.EditDistance;
import com.choicemaker.cm.matching.gen.LongestCommonSubsequence;
import com.choicemaker.cm.matching.gen.LongestCommonSubstring;
import com.choicemaker.cm.modelmaker.gui.utils.Enable;
import com.choicemaker.cm.modelmaker.gui.utils.EnablednessGuard;
import com.wcohen.ss.eclipse.StringDistances;

/**
 * @author mbuechi
 */
public class StringComparator extends JDialog implements Enable {
	private static final long serialVersionUID = 1L;

	private static DecimalFormat df = new DecimalFormat("##0.00");

	private JTextField a;
	private Dimension preferredSizeA;
	private JTextField b;
	private Dimension preferredSizeB;
	private JTabbedPane tabbedPane;
	private JLabel soundexA;
	private JLabel soundexB;
	private JLabel soundexEquals;
	private JComboBox soundexNumDigits;
	private JLabel nysiisA;
	private JLabel nysiisB;
	private JLabel nysiisEquals;
	private JLabel metaphoneA;
	private JLabel metaphoneB;
	private JLabel metaphoneEquals;
	private JLabel dMetaphoneA1;
	private JLabel dMetaphoneB1;
	private JLabel dMetaphoneEquals;
	private JLabel dmA;
	private JLabel dmB;
	private JLabel dmEquals;
	private JLabel jaro;
	private JCheckBox jaroHigherScoreForLongerStrings;
	private JCheckBox jaroCheckForNumbers;
	private JLabel ed;
	private JComboBox edMaxDistance;
	private JLabel stringDistance;
	private JComboBox stringDistanceNames;
	private JLabel lcs;
	private JLabel lcsSimilarity;
	private JLabel longestCommonSubsequence;
	private JComboBox lcsSimilarityDenominator;
	private JComboBox lcsSimilarityMinLength;
	private JComboBox lcsSimilarityMaxRepetition;
	private JLabel lcsAbbrev;
	private JComboBox lcsAbbrevLen;
	private JLabel lcsAbbrevAny;
	private JComboBox lcsAbbrevAnyLen;
	private JButton closeButton;
	private JButton setDefaultParametersButton;

	public StringComparator(JFrame parent) {
		super(parent, "String Comparison Function Utility", false);
		buildPanel();
		setDefaultParameters();
		addListeners();
		setEnabledness();
		pack();
		setLocationRelativeTo(parent);
	}
	/**
	 * Method addListeners.
	 */
	private void addListeners() {
		EnablednessGuard dl = new EnablednessGuard(this);
		a.getDocument().addDocumentListener(dl);
		b.getDocument().addDocumentListener(dl);
		
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledness();
			}
		};
		soundexNumDigits.addActionListener(al);
		jaroCheckForNumbers.addActionListener(al);
		jaroHigherScoreForLongerStrings.addActionListener(al);
		edMaxDistance.addActionListener(al);
		stringDistanceNames.addActionListener(al);
		lcsSimilarityDenominator.addActionListener(al);
		lcsSimilarityMaxRepetition.addActionListener(al);
		lcsSimilarityMinLength.addActionListener(al);
		lcsAbbrevLen.addActionListener(al);
		lcsAbbrevAnyLen.addActionListener(al);

		setDefaultParametersButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setDefaultParameters();
			}
		});

		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				dispose();
			}
		});

		JavaHelpUtils.enableHelpKey(this, "train.gui.dialog.stringcomparator");
	}

	private void buildPanel() {
		JPanel content = new JPanel();
		getContentPane().setLayout(new FlowLayout(FlowLayout.LEFT));
		getContentPane().add(content);

		GridBagLayout layout = new GridBagLayout();
		content.setLayout(layout);
		layout.columnWidths = new int[]{20};
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 5, 5);

		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 2;
		JLabel l = new JLabel("Fields");
		l.setMinimumSize(l.getPreferredSize());
		content.add(l, c);
		c.gridwidth = 1;
		c.gridx = 3;
		content.add(new JLabel("A"), c);
		c.gridx = 5;
		content.add(new JLabel("B"), c);

		c.gridy = 1;
		c.gridx = 0;
		c.gridwidth = 6;
		c.fill = GridBagConstraints.HORIZONTAL;
		content.add(new JSeparator(), c);
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;

		c.gridy = 2;
		c.gridx = 0;
		c.gridwidth = 2;
		content.add(new JLabel("Inputs"), c);
		c.gridwidth = 1;
		c.gridx = 3;
		a = new UpperCaseTextField(15);
		preferredSizeA = a.getPreferredSize();
		a.setMinimumSize(preferredSizeA);
		content.add(a, c);
		c.gridx = 5;
		b = new UpperCaseTextField(15);
		preferredSizeB = b.getPreferredSize();
		b.setMinimumSize(preferredSizeB);
		content.add(b, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 6;
		c.gridx = 0;
		c.gridy = 3;

		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Scalar", getScalarPanel());
		tabbedPane.addTab("Distance", getDistancePanel());
		tabbedPane.addTab("Substring", getSubstringPanel());
		tabbedPane.addTab("Subsequence", getSubsequencePanel());
		content.add(tabbedPane,c);

		c.gridy = 35;
		c.gridx = 3;
		c.gridwidth = 3;
		c.anchor = GridBagConstraints.EAST;
		JPanel p = new JPanel();
		content.add(p, c);
		setDefaultParametersButton = new JButton("Set default parameters");
		p.add(setDefaultParametersButton);
		closeButton = new JButton("Close");
		p.add(closeButton);
	}

	private JPanel getScalarPanel() {
		JPanel content = new JPanel();
		content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[]{20};
		content.setLayout(layout);

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(2, 2, 5, 5);

		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 2;
		content.add(new JLabel("Soundex"), c);
		c.gridwidth = 1;
		c.gridx = 3;
		soundexA = new JLabel();
		soundexA.setPreferredSize(this.preferredSizeA);
		content.add(soundexA, c);
		c.gridx = 4;
		soundexEquals = new JLabel();
		content.add(soundexEquals, c);
		c.gridx = 5;
		soundexB = new JLabel();
		soundexB.setPreferredSize(this.preferredSizeB);
		content.add(soundexB, c);

		c.gridy = 1;
		c.gridx = 1;
		content.add(new JLabel("Number of digits"), c);
		c.gridx = 2;
		soundexNumDigits = new JComboBox(getIntegerArray(10));
		c.fill = GridBagConstraints.HORIZONTAL;
		content.add(soundexNumDigits, c);
		c.fill = GridBagConstraints.NONE;

		c.gridy = 2;
		c.gridx = 0;
		c.gridwidth = 6;
		c.fill = GridBagConstraints.HORIZONTAL;
		content.add(new JSeparator(), c);
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;

		c.gridy = 3;
		c.gridx = 0;
		c.gridwidth = 2;
		content.add(new JLabel("NYSIIS"), c);
		c.gridwidth = 1;
		c.gridx = 3;
		nysiisA = new JLabel();
		content.add(nysiisA, c);
		c.gridx = 4;
		nysiisEquals = new JLabel();
		content.add(nysiisEquals, c);
		c.gridx = 5;
		nysiisB = new JLabel();
		content.add(nysiisB, c);

		c.gridy = 4;
		c.gridx = 0;
		c.gridwidth = 6;
		c.fill = GridBagConstraints.HORIZONTAL;
		content.add(new JSeparator(), c);
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;

		c.gridy = 5;
		c.gridx = 0;
		c.gridwidth = 2;
		content.add(new JLabel("Metaphone"), c);
		c.gridwidth = 1;
		c.gridx = 3;
		metaphoneA = new JLabel();
		content.add(metaphoneA, c);
		c.gridx = 4;
		metaphoneEquals = new JLabel();
		content.add(metaphoneEquals, c);
		c.gridx = 5;
		metaphoneB = new JLabel();
		content.add(metaphoneB, c);

		c.gridy = 6;
		c.gridx = 0;
		c.gridwidth = 6;
		c.fill = GridBagConstraints.HORIZONTAL;
		content.add(new JSeparator(), c);
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;

		c.gridy = 7;
		c.gridx = 0;
		c.gridwidth = 2;
		JLabel dll = new JLabel("Double Metaphone");
		content.add(dll, c);
		c.gridwidth = 1;
		c.gridx = 3;
		dMetaphoneA1 = new JLabel();
		content.add(dMetaphoneA1, c);
		c.gridx = 4;
		dMetaphoneEquals = new JLabel(" ");
		content.add(dMetaphoneEquals, c);
		c.gridx = 5;
		dMetaphoneB1 = new JLabel();
		content.add(dMetaphoneB1, c);

		return content;
	} // getScalarPanel

	private JPanel getDistancePanel() {
		JPanel content = new JPanel();
		content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[]{20};
		content.setLayout(layout);

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(2, 2, 5, 5);

		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 2;
		content.add(new JLabel("Jaro-Winkler"), c);
		c.gridwidth = 1;
		c.gridx = 4;
		jaro = new JLabel();
		content.add(jaro, c);

		c.gridy = 1;
		c.gridx = 1;
		content.add(new JLabel("Higher score for long strings"), c);
		c.gridx = 2;
		jaroHigherScoreForLongerStrings = new JCheckBox();
		content.add(jaroHigherScoreForLongerStrings, c);

		c.gridy = 2;
		c.gridx = 1;
		content.add(new JLabel("Check for numbers"), c);
		c.gridx = 2;
		jaroCheckForNumbers = new JCheckBox();
		content.add(jaroCheckForNumbers, c);

		c.gridy = 3;
		c.gridx = 0;
		c.gridwidth = 6;
		c.fill = GridBagConstraints.HORIZONTAL;
		content.add(new JSeparator(), c);
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;

		c.gridy = 4;
		c.gridx = 0;
		c.gridwidth = 2;
		content.add(new JLabel("Edit distance"), c);
		c.gridwidth = 1;
		c.gridx = 4;
		ed = new JLabel();
		content.add(ed, c);

		c.gridy = 5;
		c.gridx = 1;
		content.add(new JLabel("Max distance"), c);
		c.gridx = 2;
		Integer[] labels = getIntegerArray(10);
		labels[9] = new Integer(Integer.MAX_VALUE - 1);
		edMaxDistance = new JComboBox(labels);
		c.fill = GridBagConstraints.HORIZONTAL;
		content.add(edMaxDistance, c);		
		c.fill = GridBagConstraints.NONE;

		c.gridy = 6;
		c.gridx = 0;
		c.gridwidth = 6;
		c.fill = GridBagConstraints.HORIZONTAL;
		content.add(new JSeparator(), c);
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;

		c.gridy = 7;
		c.gridx = 0;
		c.gridwidth = 2;
		content.add(new JLabel("String distance"), c);
		c.gridwidth = 1;
		c.gridx = 4;
		stringDistance = new JLabel();
		content.add(stringDistance, c);

		c.gridy = 8;
		c.gridx = 1;
		content.add(new JLabel("available"), c);
		c.gridx = 2;
		ExtensionHolder[] extensions = ExtensionHolder.getExtensionHolders(Platform.getPluginRegistry().getExtensionPoint("com.wcohen.ss.stringdistance"));
		c.fill = GridBagConstraints.HORIZONTAL;
		// BUG: combo box doesn't show all instances, just instance 0
		// stringDistanceNames = new JComboBox(extensions);
		// BUGFIX: iterate over all configuration elements (a.k.a. instances)
		List nameList = new ArrayList();
		for (int _i=0; _i<extensions.length; _i++) {
			IConfigurationElement[] instances = extensions[_i].getExtension().getConfigurationElements();
			for (int _j=0; _j<instances.length; _j++) {
				String _name = instances[_j].getAttribute("name");
				nameList.add(_name);
			}
		}
		String[] names = (String[]) nameList.toArray(new String[nameList.size()]);
		stringDistanceNames = new JComboBox(names);
		// ENDBUGFIX
		if (extensions.length == 0) {
			JLabel none = new JLabel("<none>");
			content.add(none,c);
		} else {
			content.add(stringDistanceNames,c);
		}
		c.fill = GridBagConstraints.NONE;

		return content;
	} // getDistancePanel

	private JPanel getSubstringPanel() {
		JPanel content = new JPanel();
		content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[]{20};
		content.setLayout(layout);

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(2, 2, 5, 5);

		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 2;
		content.add(new JLabel("Longest common substring length"), c);
		c.gridwidth = 1;
		c.gridx = 4;
		lcs = new JLabel();
		content.add(lcs, c);
		
		c.gridy = 1;
		c.gridx = 0;
		c.gridwidth = 6;
		c.fill = GridBagConstraints.HORIZONTAL;
		content.add(new JSeparator(), c);
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;

		c.gridy = 2;
		c.gridx = 0;
		c.gridwidth = 2;
		content.add(new JLabel("Longest common substring similarity"), c);
		c.gridwidth = 1;
		c.gridx = 4;
		lcsSimilarity = new JLabel();
		content.add(lcsSimilarity, c);

		c.gridy = 3;
		c.gridx = 1;
		content.add(new JLabel("Denominator"), c);
		c.gridx = 2;
		lcsSimilarityDenominator = new JComboBox(new String[] { "AVERAGE", "SHORTEST", "LONGEST" });
		c.fill = GridBagConstraints.HORIZONTAL;
		content.add(lcsSimilarityDenominator, c);
		c.fill = GridBagConstraints.NONE;

		c.gridy = 4;
		c.gridx = 1;
		content.add(new JLabel("Minimum length"), c);
		c.gridx = 2;
		lcsSimilarityMinLength = new JComboBox(getIntegerArray(10));
		c.fill = GridBagConstraints.HORIZONTAL;
		content.add(lcsSimilarityMinLength, c);
		c.fill = GridBagConstraints.NONE;

		c.gridy = 5;
		c.gridx = 1;
		content.add(new JLabel("Max repetitions"), c);
		c.gridx = 2;
		lcsSimilarityMaxRepetition = new JComboBox(getIntegerArray(10));
		c.fill = GridBagConstraints.HORIZONTAL;
		content.add(lcsSimilarityMaxRepetition, c);
		c.fill = GridBagConstraints.NONE;

		return content;
	} // getSubstringPanel
		
	private JPanel getSubsequencePanel() {
		JPanel content = new JPanel();
		content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[]{20};
		content.setLayout(layout);

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(2, 2, 5, 5);

		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 2;
		content.add(new JLabel("Longest common subsequence length"), c);
		c.gridwidth = 1;
		c.gridx = 4;
		longestCommonSubsequence = new JLabel();
		content.add(longestCommonSubsequence, c);

		c.gridy = 1;
		c.gridx = 0;
		c.gridwidth = 6;
		c.fill = GridBagConstraints.HORIZONTAL;
		content.add(new JSeparator(), c);
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;

		c.gridy = 2;
		c.gridx = 0;
		c.gridwidth = 2;
		content.add(new JLabel("Longest common subsequence abbreviation"), c);
		c.gridwidth = 1;
		c.gridx = 4;
		lcsAbbrev = new JLabel();
		content.add(lcsAbbrev, c);

		c.gridy = 3;
		c.gridx = 1;
		content.add(new JLabel("Minimum length"), c);
		c.gridx = 2;
		lcsAbbrevLen = new JComboBox(getIntegerArray(10));
		c.fill = GridBagConstraints.HORIZONTAL;
		content.add(lcsAbbrevLen, c);
		c.fill = GridBagConstraints.NONE;

		c.gridy = 4;
		c.gridx = 0;
		c.gridwidth = 6;
		c.fill = GridBagConstraints.HORIZONTAL;
		content.add(new JSeparator(), c);
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;

		c.gridy = 5;
		c.gridx = 0;
		c.gridwidth = 2;
		content.add(new JLabel("Longest common subsequence abbreviation any start"), c);
		c.gridwidth = 1;
		c.gridx = 4;
		lcsAbbrevAny = new JLabel();
		content.add(lcsAbbrevAny, c);

		c.gridy = 6;
		c.gridx = 1;
		content.add(new JLabel("Minimum length"), c);
		c.gridx = 2;
		lcsAbbrevAnyLen = new JComboBox(getIntegerArray(10));
		c.fill = GridBagConstraints.HORIZONTAL;
		content.add(lcsAbbrevAnyLen, c);
		c.fill = GridBagConstraints.NONE;

		return content;		
	} // getSubsequencePanel

	/**
	 * @see com.choicemaker.cm.train.gui.utils.Enable#setEnabledness()
	 */
	public void setEnabledness() {
		String at = a.getText().toUpperCase();
		String bt = b.getText().toUpperCase();
		int sndxDigits = soundexNumDigits.getSelectedIndex();
		set(Soundex.soundex(at, sndxDigits), Soundex.soundex(bt, sndxDigits), soundexA, soundexEquals, soundexB);
		set(Nysiis.nysiis(at), Nysiis.nysiis(bt), nysiisA, nysiisEquals, nysiisB);
		set(Metaphone.metaphone(at), Metaphone.metaphone(bt), metaphoneA, metaphoneEquals, metaphoneB);

		DoubleMetaphone da = DoubleMetaphone.doubleMetaphone(at);
		DoubleMetaphone db = DoubleMetaphone.doubleMetaphone(bt);
		dMetaphoneA1.setText(da.getPrimary() + ", " + da.getAlternate());
		dMetaphoneB1.setText(db.getPrimary() + ", " + db.getAlternate());
		dMetaphoneEquals.setText(da.equals(db) ? "=" : "!=");

		jaro.setText(
			df.format(
				Jaro.jaro(
					at,
					bt,
					jaroHigherScoreForLongerStrings.isSelected(),
					true,
					jaroCheckForNumbers.isSelected())));
		ed.setText(String.valueOf(EditDistance.editDistance(at, bt, ((Integer)edMaxDistance.getSelectedItem()).intValue())));
		if (stringDistanceNames.getModel().getSize() > 0) {
			// ExtensionHolder _eh = (ExtensionHolder) stringDistanceNames.getSelectedItem();
			String name = (String) stringDistanceNames.getSelectedItem();
			double score = StringDistances.score(name,at,bt);
			stringDistance.setText(String.valueOf(score));
		}
		lcs.setText(String.valueOf(LongestCommonSubstring.longestCommonSubstring(at, bt)));
		int denominator;
		switch (lcsSimilarityDenominator.getSelectedIndex()) {
			case 0 :
				denominator = LongestCommonSubstring.AVERAGE;
				break;
			case 1 :
				denominator = LongestCommonSubstring.SHORTEST;
				break;
			default :
				denominator = LongestCommonSubstring.LONGEST;
				break;
		}
		lcsSimilarity.setText(
			df.format(
				LongestCommonSubstring.similarity(
					at,
					bt,
					denominator,
					lcsSimilarityMinLength.getSelectedIndex(),
					lcsSimilarityMaxRepetition.getSelectedIndex())));
		longestCommonSubsequence.setText(String.valueOf(LongestCommonSubsequence.lcsLength(at, bt)));
		lcsAbbrev.setText(LongestCommonSubsequence.isLcsAbbrev(at, bt, ((Integer)lcsAbbrevLen.getSelectedItem()).intValue()) ? "=" : "!=");
		lcsAbbrevAny.setText(LongestCommonSubsequence.isLcsAbbrevAnyStart(at, bt, ((Integer)lcsAbbrevAnyLen.getSelectedItem()).intValue()) ? "=" : "!=");
	}

	private void set(String x, String y, JLabel xl, JLabel e, JLabel yl) {
		xl.setText(x);
		yl.setText(y);
		if (x.equals(y)) {
			e.setText("=");
		} else {
			e.setText("!=");
		}
	}
	
	private void setDefaultParameters() {
		soundexNumDigits.setSelectedIndex(3);
		jaroHigherScoreForLongerStrings.setSelected(true);
		jaroCheckForNumbers.setSelected(false);
		edMaxDistance.setSelectedIndex(9);
		if (stringDistanceNames.getModel().getSize() > 0) {
			stringDistanceNames.setSelectedIndex(0);
		}
		lcsSimilarityDenominator.setSelectedIndex(1);
		lcsSimilarityMinLength.setSelectedIndex(3);
		lcsSimilarityMaxRepetition.setSelectedIndex(3);
		lcsAbbrevLen.setSelectedIndex(2);
		lcsAbbrevAnyLen.setSelectedIndex(2);
	}

	private Integer[] getIntegerArray(int max) {
		Integer[] res = new Integer[max];
		for (int i = 0; i < max; i++) {
			res[i] = new Integer(i);
		}
		return res;
	}
}
