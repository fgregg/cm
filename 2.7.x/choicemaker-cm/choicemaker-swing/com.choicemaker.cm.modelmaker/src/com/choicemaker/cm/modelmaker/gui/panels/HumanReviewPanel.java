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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.RepositoryChangeEvent;
import com.choicemaker.cm.core.RepositoryChangeListener;
import com.choicemaker.cm.core.base.Evaluator;
import com.choicemaker.cm.core.base.MutableMarkedRecordPair;
import com.choicemaker.cm.core.util.MessageUtil;
import com.choicemaker.cm.gui.utils.viewer.CompositePaneModel;
import com.choicemaker.cm.modelmaker.ModelMakerEventNames;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.listeners.EvaluationEvent;
import com.choicemaker.cm.modelmaker.gui.listeners.EvaluationListener;
import com.choicemaker.cm.modelmaker.gui.menus.FilterMenu;
import com.choicemaker.cm.modelmaker.gui.menus.LayoutMenu;
import com.choicemaker.cm.modelmaker.gui.tables.ActiveClueTablePanel;

/**
 * Panel for reviewing a chosen MarkedRecordPair from the current source.  Users may
 * step through MRPs, obsever the performance of clues that fired on a MRP, 
 * modify the layout of record display, modify the MRP data, save changes, 
 * and re-evaluate the probability model on this MRP.
 * 
 * @author  S. Yoakum-Stover
 * @author  Martin Buechi
 * @version $Revision: 1.2 $ $Date: 2010/03/29 13:02:25 $
 */
public class HumanReviewPanel
	extends JPanel
	implements RepositoryChangeListener, PropertyChangeListener, EvaluationListener {
	private static final long serialVersionUID = 1L;
	private JLabel markedDecisionLabel;
	private JLabel choiceMakerDecisionLabel;
	private JLabel choiceMakerProbabilityLabel;
	private JLabel globalSelectLabel;
	private JLabel currentRecordLabel;
	private JLabel nextInLoggerLabel;
	private JLabel selectionSizeLabel;
	private static Logger logger = Logger.getLogger(HumanReviewPanel.class);
	private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
	private ModelMaker parent;
	private RecordPairViewerPanel viewer;
	private JScrollPane viewerScrollPane;
	private JComboBox markedDecisionComboBox;
	private JTextField choiceMakerDecisionField;
	private JTextField choiceMakerProbabilityField;
	private JTextField user;
	private JTextField date;
	private JTextArea comment;
	private JButton fullButton;
	private JPanel checkedPanel;
	private JCheckBox checked;
	private MutableMarkedRecordPair markedRP;
	private int markedRecordPairIndex;
	private Icon forwardIcon = new ImageIcon(ModelMaker.class.getResource("images/StepForward16.gif"));
	private Icon backIcon = new ImageIcon(ModelMaker.class.getResource("images/StepBack16.gif"));
	private Icon warnIcon = new ImageIcon(ModelMaker.class.getResource("images/wand1.gif"));
	private JPanel navigationPanel;
	private JTextField selectionSize;
	private JButton stepForwardButton;
	private JButton stepBackButton;
	private JSlider recordPairSlider;
	private JTextField currentRecord;
	private Hashtable labelTable;
	private Integer zero = new Integer(0);
	private JPanel cmDecisionPanel;
	private ActiveClueTablePanel activeClueTablePanel;
	private LayoutMenu layoutMenu;
	private FilterMenu filterMenu;
	private boolean reset;
	private boolean dirty;
	private HumanDataDialog humanDataDialog;

	private DecimalFormat df = new DecimalFormat("##0.0");

	/**
	 * Creates the human review panel.
	 *
	 * @param   g  The parent.
	 */
	public HumanReviewPanel(ModelMaker g) {
		super();
		parent = g;
		buildPanel();
		buildMenus();
		addListeners();
		layoutPanelWithActiveClues();
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		parent.addPropertyChangeListener(this);
		parent.addEvaluationListener(this);
		parent.getProbabilityModelEventMultiplexer().addPropertyChangeListener(this);
		parent.addMarkedRecordPairDataChangeListener(this);
		parent.getRepository().addRepositoryChangeListener(this);
	}

	public void showActiveCluesPanel(boolean b) {
		if (b) {
			layoutPanelWithActiveClues();
		} else {
			layoutPanelWithoutActiveClues();
		}
	}
	
	private void buildPanel() {		
		viewer = new RecordPairViewerPanel(this);
		viewerScrollPane = new JScrollPane(viewer.getViewer());
		activeClueTablePanel = new ActiveClueTablePanel(parent);

		buildCmDecisionPanel();
		buildCheckedPanel();

		user = new JTextField(20);
		user.setEditable(false);
		date = new JTextField(20);
		date.setEditable(false);
		comment = new JTextArea();
		comment.setEnabled(false);

		buildNavigationPanel();
		humanDataDialog = new HumanDataDialog();
	}

	private void buildCheckedPanel() {
		checkedPanel = new JPanel();
		checkedPanel.setBorder(BorderFactory.createTitledBorder("Checked"));

		GridBagLayout layout = new GridBagLayout();
		checkedPanel.setLayout(layout);

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 6, 2, 6);
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.CENTER;
		checked = new JCheckBox("Checked");
		checkedPanel.add(checked, c);
	}

	private void buildCmDecisionPanel() {
		cmDecisionPanel = new JPanel();
		cmDecisionPanel.setBorder(
			BorderFactory.createTitledBorder(
				MessageUtil.m.formatMessage("train.gui.modelmaker.panel.humanreview.decisions")));

		choiceMakerProbabilityLabel =
			new JLabel(MessageUtil.m.formatMessage("train.gui.modelmaker.panel.humanreview.probability"));
		choiceMakerProbabilityField = new JTextField(20);
		choiceMakerProbabilityField.setEditable(false);

		choiceMakerDecisionLabel =
			new JLabel(MessageUtil.m.formatMessage("train.gui.modelmaker.panel.humanreview.cm"));
		choiceMakerDecisionField = new JTextField(20);
		choiceMakerDecisionField.setEditable(false);

		markedDecisionLabel =
			new JLabel(MessageUtil.m.formatMessage("train.gui.modelmaker.panel.humanreview.human"));
		String[] decisionValues = new String[Decision.NUM_DECISIONS];
		for (int i = 0; i < Decision.NUM_DECISIONS; ++i) {
			decisionValues[i] = Decision.valueOf(i).toString();
		}
		markedDecisionComboBox = new JComboBox(decisionValues);
		if (!parent.isIncludeHolds()) {
			markedDecisionComboBox.removeItem(Decision.HOLD.toString());
		}
		markedDecisionComboBox.setEnabled(false);
		markedDecisionComboBox.setMinimumSize(markedDecisionComboBox.getPreferredSize());

		fullButton = new JButton(MessageUtil.m.formatMessage("train.gui.modelmaker.panel.humanreview.more"));
		fullButton.setEnabled(false);
		fullButton.setMinimumSize(fullButton.getPreferredSize());
	}

	private void buildNavigationPanel() {
		navigationPanel = new JPanel();
		//navigationPanel.setPreferredSize(new Dimension(200, 120));
		navigationPanel.setBorder(
			BorderFactory.createTitledBorder(
				MessageUtil.m.formatMessage("train.gui.modelmaker.panel.humanreview.navigation")));

		selectionSizeLabel = new JLabel(MessageUtil.m.formatMessage("train.gui.modelmaker.panel.humanreview.navigation.selection.size"));
		selectionSize = new JTextField(10);
		selectionSize.setEditable(false);

		nextInLoggerLabel =
			new JLabel(MessageUtil.m.formatMessage("train.gui.modelmaker.panel.humanreview.navigation.selection.next"));
		stepForwardButton = new JButton(forwardIcon);
		stepForwardButton.setEnabled(false);
		stepForwardButton.setMnemonic(KeyEvent.VK_N);
		stepBackButton = new JButton(backIcon);
		stepBackButton.setEnabled(false);
		stepBackButton.setMnemonic(KeyEvent.VK_P);

		currentRecordLabel =
			new JLabel(MessageUtil.m.formatMessage("train.gui.modelmaker.panel.humanreview.navigation.current"));
		currentRecord = new JTextField(Integer.toString(markedRecordPairIndex), 8);
		currentRecord.setEnabled(false);

		globalSelectLabel =
			new JLabel(MessageUtil.m.formatMessage("train.gui.modelmaker.panel.humanreview.navigation.all"));
		recordPairSlider = new JSlider(JSlider.HORIZONTAL);
		recordPairSlider.setPaintTicks(true);
		recordPairSlider.setPaintLabels(true);
		recordPairSlider.setEnabled(false);
		recordPairSlider.setMinimumSize(new Dimension(100, 40));
	}

	public void setSelectionSize(int ss) {
		selectionSize.setText(String.valueOf(ss));
	}

	private void buildMenus() {
		layoutMenu = new LayoutMenu(this);
		parent.getJMenuBar().add(layoutMenu);
		filterMenu = new FilterMenu(this);
		parent.getJMenuBar().add(filterMenu);
	}

	private void addListeners() {
		//markedDecisionComboBox
		markedDecisionComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				JComboBox cb = (JComboBox) ev.getSource();
				Decision d = Decision.valueOf((String) cb.getSelectedItem());
				if (markedRP != null) {
					Decision oldDecision = markedRP.getMarkedDecision();
					if (d != oldDecision) {
						markedRP.setMarkedDecision(d);
						markedRP.setDateMarked(new Date());
						markedRP.setUser(System.getProperty("user.name"));
						markedRecordPairSelected(markedRecordPairIndex);
						parent.fireMarkedRecordPairDataChange(new RepositoryChangeEvent(this, null, oldDecision, d));
					}
				}
			}
		});

		comment.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				set();
			}
			public void removeUpdate(DocumentEvent e) {
				set();
			}
			public void changedUpdate(DocumentEvent e) {
				set();
			}
			private void set() {
				if (markedRP != null) {
					markedRP.setComment(comment.getText());
				}
			}
		});

		fullButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent A) {
				humanDataDialog.show();
			}
		});

		checked.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (markedRecordPairIndex >= 0) {
					parent.setChecked(markedRecordPairIndex, checked.isSelected());	
				}
			}
		});

		//stepForwardButton
		stepForwardButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent A) {
				//logger.debug("Request step forward.");
				parent.reviewNextMarkedRecordPair();
			}
		});

		//stepBackButton
		stepBackButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent A) {
				//logger.debug("Request step back.");
				parent.reviewPreviousMarkedRecordPair();
			}
		});

		//recordPairSlider
		recordPairSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!reset && !source.getValueIsAdjusting()) {
					int selection = source.getValue();
					parent.setMarkedRecordPair(selection);
				}
			}

		});

		currentRecord.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JTextField source = (JTextField) e.getSource();
				int selection = markedRecordPairIndex;
				try {
					selection = Integer.parseInt(source.getText());
				} catch (NumberFormatException ex) {
					// ignore
				}
				if (selection != markedRecordPairIndex) {
					int ss = parent.getSourceList().size();
					if (selection < 0) {
						currentRecord.setText("0");
						selection = 0;
					} else if (selection >= ss) {
						currentRecord.setText(String.valueOf(ss - 1));
						selection = ss - 1;
					}
					parent.setMarkedRecordPair(selection);
				}
			}
		});

	}

	public void saveData() {
		parent.saveMarkedRecordPairSource();
	}

	public void propertyChange(PropertyChangeEvent evt) {
		String propertyName = evt.getPropertyName();
		Object source = evt.getSource();
		if (source == parent) {
			if (propertyName == ModelMakerEventNames.MARKED_RECORD_PAIR_SOURCE || propertyName == ModelMakerEventNames.PROBABILITY_MODEL) {
				reset();
			} else if (propertyName == ModelMakerEventNames.MARKED_RECORD_PAIR) {
				markedRecordPairSelected(((Integer) evt.getNewValue()).intValue());
			} else if (propertyName == ModelMakerEventNames.THRESHOLDS) {
				if (markedRP != null) {
					setChoiceMakerDecisionField();
				}
			} else if(propertyName == ModelMakerEventNames.CHECKED_INDICES) {
				checked.setSelected(parent.isChecked(markedRecordPairIndex));
			}
		} else if (source == parent.getProbabilityModel()) {
			if (propertyName == null) {
				reset();
			}
		}
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
			if (parent.isEvaluated() && parent.getSourceList().size() > 0) {
				recordPairSlider.setEnabled(true);
				currentRecord.setEnabled(true);
				int numRecordPairs = parent.getSourceList().size();
				recordPairSlider.setMaximum(numRecordPairs - 1);
			}
		}
	}

	public void setChanged(RepositoryChangeEvent evt) {
		reset();
	}

	public void recordDataChanged(RepositoryChangeEvent evt) {
		updateDisplay();
	}

	public void markupDataChanged(RepositoryChangeEvent evt) {
	}

	private void reset() {
		if(markedRP != null) {
			markedRP.removeRepositoryChangeListener(this);
		}
		reset = true;
		markedRecordPairIndex = -1;
		markedRP = null;
		markedDecisionComboBox.setEnabled(false);
		markedDecisionComboBox.removeItem(Decision.HOLD.toString());
		if (parent.isIncludeHolds()) {
			markedDecisionComboBox.addItem(Decision.HOLD.toString());
		}
		stepForwardButton.setEnabled(false);
		stepBackButton.setEnabled(false);
		recordPairSlider.setEnabled(false);
		currentRecord.setEnabled(false);
		choiceMakerDecisionField.setText("");
		choiceMakerProbabilityField.setText("");
		user.setText("");
		date.setText("");
		comment.setText("");
		comment.setEnabled(false);
		comment.setEnabled(false);
		fullButton.setEnabled(false);
		checked.setSelected(false);
		currentRecord.setText("");
		recordPairSlider.setValue(0);
		selectionSize.setText("");
		activeClueTablePanel.resetData();
		repaint();
		reset = false;
	}

	public void setVisible(boolean b) {
		super.setVisible(b);
		layoutMenu.setEnabled(b);
		if (b && dirty) {
			display();
		}
	}

	/**
	 * Returns the parent.
	 *
	 * @return  The parent.
	 */
	public ModelMaker getModelMaker() {
		return parent;
	}

	public void markedRecordPairSelected(int index) {
		if(markedRP != null) {
			markedRP.removeRepositoryChangeListener(this);
		}
		markedRecordPairIndex = index;
		markedRP = (MutableMarkedRecordPair) parent.getSourceList().get(index);
		markedRP.addRepositoryChangeListener(this);
		updateDisplay();
	}
		
	private void updateDisplay() {	
		markedDecisionComboBox.setEnabled(true);
		RecordPairList rpl = parent.getRecordPairList();
		int s = rpl.modelSize();
		stepForwardButton.setEnabled(s > 0 && markedRecordPairIndex < rpl.getLastIndex());
		stepBackButton.setEnabled(s > 0 && markedRecordPairIndex > rpl.getFirstIndex());
		markedDecisionComboBox.setSelectedItem(markedRP.getMarkedDecision().toString());
		user.setText(markedRP.getUser());
		if (markedRP.getDateMarked() != null) {
			date.setText(DATE_FORMAT.format(markedRP.getDateMarked()));
		}
		comment.setText(markedRP.getComment());
		comment.setEnabled(true);
		comment.setEnabled(true);
		fullButton.setEnabled(true);
		setChoiceMakerDecisionField();
		choiceMakerProbabilityField.setText(df.format(markedRP.getProbability() * 100));

		checked.setSelected(parent.isChecked(markedRecordPairIndex));

		currentRecord.setText(Integer.toString(markedRecordPairIndex));

		int numRecordPairs = parent.getSourceList().size();

		Integer max = new Integer(numRecordPairs - 1);
		labelTable = new Hashtable(2);
		labelTable.clear();
		labelTable.put(zero, new JLabel(zero.toString()));
		labelTable.put(max, new JLabel(max.toString()));

		recordPairSlider.setLabelTable(labelTable);
		recordPairSlider.repaint();
		recordPairSlider.setValue(markedRecordPairIndex);
		repaint();
	}

	private void setChoiceMakerDecisionField() {
		choiceMakerDecisionField.setText(
			Evaluator.isBasedOnRule(markedRP, parent.getThresholds())
				? MessageUtil.m.formatMessage(
					"train.gui.modelmaker.panel.humanreview.rulebased",
					markedRP.getCmDecision().toString())
				: MessageUtil.m.formatMessage(
					"train.gui.modelmaker.panel.humanreview.not.rulebased",
					markedRP.getCmDecision().toString()));
		choiceMakerDecisionField.setForeground(markedRP.getMarkedDecision() == markedRP.getCmDecision() ? Color.black : Color.red);
	}

	public void setCurrentLayout(CompositePaneModel l) {
		viewer.setRecordPairViewerModel(l);
		viewer.markedRecordPairSelected(markedRP);
	}

	public CompositePaneModel getCurrentLayout() {
		return viewer.getRecordPairViewerModel();
	}

	public void setDefaultLayout() {
		viewer.setDefaultLayout();
		viewer.markedRecordPairSelected(markedRP);
	}

	public void displayRecordPairFilterDialog() {
		parent.displayRecordPairFilterDialog();
	}

	private void layoutPanelWithActiveClues() {
		removeAll();
		layoutCmDecisionPanelWithActiveClues();
		layoutNavigationPanelWithActiveClues();
		
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(3, 3, 3, 3);

		//row 0...............................................................
		//decisionPanel
		c.gridy = 0;
		c.gridx = 0;
		c.ipady = 10;
		//c.anchor = GridBagConstraints.NORTH;
		c.fill = GridBagConstraints.BOTH;
		c.gridheight = 2;
		c.weightx = 1;
		add(activeClueTablePanel, c);
		c.gridheight = 1;
		c.weightx = 0;

		// clueTableScrollPane
		c.gridx = 1;
		//clueTableScrollPane.setMinimumSize(new Dimension(800, 140));
		add(cmDecisionPanel, c);

		//navigationPanel
		c.gridx = 2;
		//        c.fill = GridBagConstraints.NONE;
		c.gridheight = 2;
		add(navigationPanel, c);
		c.gridheight = 1;

		//row 1..............................................................
		c.gridy = 1;
		c.gridx = 1;
		add(checkedPanel, c);


		//row 2...............................................................
		//viewerScrollPane
		c.gridy = 2;
		c.gridx = 0;
		c.gridwidth = 5;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		layout.setConstraints(viewerScrollPane, c);
		add(viewerScrollPane);
	}

	private void layoutNavigationPanelWithActiveClues() {
		navigationPanel.removeAll();
		
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 5, 10);
		navigationPanel.setLayout(layout);

		//row 0.......................................
		//selectionSizeLabel
		c.gridy = 0;
		c.gridx = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		layout.setConstraints(selectionSizeLabel, c);
		navigationPanel.add(selectionSizeLabel);
		//selectionSize
		c.gridx = 1;
		c.gridwidth = 3;
		c.anchor = GridBagConstraints.CENTER;
		layout.setConstraints(selectionSize, c);
		navigationPanel.add(selectionSize);

		//row 1.......................................
		//nextInLoggerLabel
		c.gridy = 1;
		c.gridx = 0;
		c.anchor = GridBagConstraints.WEST;
		layout.setConstraints(nextInLoggerLabel, c);
		navigationPanel.add(nextInLoggerLabel);
		//stepBackButton & stepForwardButton
		JPanel stepPanel = new JPanel();
		stepPanel.add(stepBackButton);
		stepPanel.add(stepForwardButton);
		c.gridx = 1;
		c.gridwidth = 3;
		c.anchor = GridBagConstraints.CENTER;
		layout.setConstraints(stepPanel, c);
		navigationPanel.add(stepPanel);

		//row 2.......................................
		//currentRecordLabel
		c.gridy = 2;
		c.gridx = 0;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		layout.setConstraints(currentRecordLabel, c);
		navigationPanel.add(currentRecordLabel);
		//currentRecord
		c.gridx = 1;
		c.gridwidth = 3;
		c.anchor = GridBagConstraints.CENTER;
		layout.setConstraints(currentRecord, c);
		navigationPanel.add(currentRecord);

		//row 3.......................................
		//globalSelectLabel
		c.gridy = 3;
		c.gridx = 0;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		layout.setConstraints(globalSelectLabel, c);
		navigationPanel.add(globalSelectLabel);
		//recordPairSlider
		c.gridx = 1;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		layout.setConstraints(recordPairSlider, c);
		navigationPanel.add(recordPairSlider);		
	}

	private void layoutCmDecisionPanelWithActiveClues() {
		cmDecisionPanel.removeAll();
		
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 5, 10);
		cmDecisionPanel.setLayout(layout);

		//row 0.......................................
		c.gridy = 0;
		c.gridx = 0;
		c.anchor = GridBagConstraints.WEST;
		c.ipadx = 0;
		cmDecisionPanel.add(choiceMakerProbabilityLabel, c);
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		cmDecisionPanel.add(choiceMakerProbabilityField, c);

		//row2.........................................
		//choiceMakerProbabilityLabel
		c.gridy = 1;
		c.gridx = 0;
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		cmDecisionPanel.add(choiceMakerDecisionLabel, c);
		//choiceMakerProbabilityField
		c.gridx = 1;
		//c.ipadx = 50;
		c.fill = GridBagConstraints.HORIZONTAL;
		cmDecisionPanel.add(choiceMakerDecisionField, c);

		c.gridy = 2;
		c.gridx = 0;
		cmDecisionPanel.add(markedDecisionLabel, c);
		c.gridx = 1;
		cmDecisionPanel.add(markedDecisionComboBox, c);

		c.gridy = 3;
		c.gridx = 1;
		cmDecisionPanel.add(fullButton, c);
		
	}

	private void layoutPanelWithoutActiveClues() {
		removeAll();
		layoutCmDecisionPanelWithoutActiveClues();
		layoutNavigationPanelWithoutActiveClues();
		
		GridBagLayout layout = new GridBagLayout();
		layout.rowWeights = new double[] {0, 1};
		layout.columnWeights = new double[] {0, 1, 1};
		setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(3, 3, 3, 3);

		//row ...............................................................
		c.gridy = 0;

		c.gridx = 0;
		add(checkedPanel, c);

		// decision panel
		c.gridx = 1;
		add(cmDecisionPanel, c);

		//navigationPanel
		c.gridx = 2;
		add(navigationPanel, c);

		// row ..............................................................
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 3;
		add(viewerScrollPane, c);		
	}

	private void layoutNavigationPanelWithoutActiveClues() {
		navigationPanel.removeAll();
		
		GridBagLayout layout = new GridBagLayout();
		layout.columnWeights = new double[] {0, 0, 0, 1, 0, 0};
		navigationPanel.setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 6, 2, 6);

		//row 0.......................................
		c.gridy = 0;

		//selectionSizeLabel
		c.gridx = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		navigationPanel.add(selectionSizeLabel, c);

		//selectionSize
		c.gridx = 1;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.CENTER;
		navigationPanel.add(selectionSize, c);
		c.gridwidth = 1;

		//row 1.......................................
		c.gridy++;

		//nextInLoggerLabel
		c.gridx = 0;
		c.anchor = GridBagConstraints.WEST;
		navigationPanel.add(nextInLoggerLabel, c);

		//stepBackButton & stepForwardButton
		c.gridx = 1;
		navigationPanel.add(stepBackButton, c);
		
		c.gridx = 2;
		navigationPanel.add(stepForwardButton, c);

		//row 2.......................................
		c.gridy = 0;

		//currentRecordLabel
		c.gridx = 4;
		c.anchor = GridBagConstraints.WEST;
		navigationPanel.add(currentRecordLabel, c);

		//currentRecord
		c.gridx = 5;
		c.anchor = GridBagConstraints.CENTER;
		navigationPanel.add(currentRecord, c);

		//row 3.......................................
		c.gridy++;

		c.gridx = 4;
		c.anchor = GridBagConstraints.WEST;
		navigationPanel.add(globalSelectLabel, c);

		c.gridx = 5;
		c.fill = GridBagConstraints.HORIZONTAL;
		navigationPanel.add(recordPairSlider, c);
		
		// middle "glue"
		c.gridy = 0;
		c.gridx = 3;
		JPanel p = new JPanel();
		p.setMinimumSize(new Dimension(10, 10));
		navigationPanel.add(p, c);
		
	}

	private void layoutCmDecisionPanelWithoutActiveClues() {
		cmDecisionPanel.removeAll();
		
		GridBagLayout layout = new GridBagLayout();
		layout.columnWeights = new double[] {0, 0, 1, 0, 0};
		cmDecisionPanel.setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 6, 2, 6);

		//row.......................................
		c.gridy = 0;

		c.gridx = 0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		cmDecisionPanel.add(choiceMakerDecisionLabel, c);

		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		cmDecisionPanel.add(choiceMakerDecisionField, c);
		
		//row.........................................
		c.gridy++;

		c.gridx = 0;
		cmDecisionPanel.add(markedDecisionLabel, c);
		
		c.gridx = 1;
		cmDecisionPanel.add(markedDecisionComboBox, c);

		// row..........................................
		c.gridy = 0;

		c.gridx = 3;
		c.anchor = GridBagConstraints.WEST;
		cmDecisionPanel.add(choiceMakerProbabilityLabel, c);

		c.gridx = 4;
		c.fill = GridBagConstraints.HORIZONTAL;
		cmDecisionPanel.add(choiceMakerProbabilityField, c);

		
		// row.......................................
		c.gridy = 1;
		c.gridx = 4;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		cmDecisionPanel.add(fullButton, c);
		
		// glue .....................................
		c.gridy = 0;
		c.gridx = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		JPanel p = new JPanel();
		p.setMinimumSize(new Dimension(10, 10));
		cmDecisionPanel.add(p, c);
		
	}

	private class HumanDataDialog extends JDialog {
		private static final long serialVersionUID = 1L;

		HumanDataDialog() {
			super(parent, MessageUtil.m.formatMessage("train.gui.modelmaker.panel.humanreview.info"));
			JPanel content = new JPanel();
			GridBagLayout layout = new GridBagLayout();
			content.setLayout(layout);
			GridBagConstraints c = new GridBagConstraints();
			c.insets = new Insets(2, 2, 5, 10);
			c.gridy = 0;
			c.gridx = 0;
			c.anchor = GridBagConstraints.NORTHWEST;
			content.add(new JLabel(MessageUtil.m.formatMessage("train.gui.modelmaker.panel.humanreview.user")), c);
			c.gridx = 1;
			c.fill = GridBagConstraints.HORIZONTAL;
			content.add(user, c);

			c.gridy = 1;
			c.gridx = 0;
			content.add(new JLabel(MessageUtil.m.formatMessage("train.gui.modelmaker.panel.humanreview.date")), c);
			c.gridx = 1;
			content.add(date, c);

			c.gridy = 2;
			c.gridx = 0;
			content.add(new JLabel(MessageUtil.m.formatMessage("train.gui.modelmaker.panel.humanreview.comment")), c);
			c.gridx = 1;
			c.weightx = 1;
			c.weighty = 1;
			c.fill = GridBagConstraints.BOTH;
			content.add(comment, c);
			setContentPane(content);
			setSize(250, 250);
		}
	}
}
