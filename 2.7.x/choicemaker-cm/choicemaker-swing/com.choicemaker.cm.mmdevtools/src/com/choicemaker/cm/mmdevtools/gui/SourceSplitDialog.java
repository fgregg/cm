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
package com.choicemaker.cm.mmdevtools.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.choicemaker.cm.core.base.Constants;
import com.choicemaker.cm.core.base.MarkedRecordPairSource;
import com.choicemaker.cm.core.base.Record;
import com.choicemaker.cm.core.base.RecordSink;
import com.choicemaker.cm.core.base.RecordSource;
import com.choicemaker.cm.core.base.Sink;
import com.choicemaker.cm.core.base.SinkFactory;
import com.choicemaker.cm.core.base.Source;
import com.choicemaker.cm.core.xmlconf.MarkedRecordPairSourceXmlConf;
import com.choicemaker.cm.core.xmlconf.RecordSourceXmlConf;
import com.choicemaker.cm.core.xmlconf.XmlConfException;
import com.choicemaker.cm.gui.utils.dialogs.FileChooserFactory;
import com.choicemaker.cm.io.composite.base.CompositeRecordSource;
import com.choicemaker.cm.io.flatfile.base.FlatFileRecordSinkFactory;
import com.choicemaker.cm.io.flatfile.base.FlatFileRecordSource;
import com.choicemaker.cm.io.xml.base.XmlRecordSinkFactory;
import com.choicemaker.cm.io.xml.base.XmlRecordSource;
import com.choicemaker.cm.mmdevtools.io.RoundRobinSink;
import com.choicemaker.cm.mmdevtools.util.AllFilter;
import com.choicemaker.cm.mmdevtools.util.Filter;
import com.choicemaker.cm.mmdevtools.util.RandomFilter;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.dialogs.SourceTypeSelectorDialog;

/**
 * @author ajwinkel
 *
 */
public class SourceSplitDialog extends JDialog {
	
	private static final long serialVersionUID = 1L;
	private static final int RS = 1;
	private static final int MRPS = 2;
	
	public static void showRsSplitDialog(ModelMaker mm) {
		new SourceSplitDialog(mm, RS).show();
	}
	
	private ModelMaker modelMaker;
	private int type;
	
	private MultiFileList inputList;
	
	private JRadioButton allButton, selectionButton, randomTargetButton, randomPercentButton;
	private JTextField selectionField, randomTargetField, randomPercentField;
	
	private JTextField sinkField;
	private JButton browseButton, newButton;
	private JRadioButton singleSinkButton, multiSinkButton;
	private JLabel roundRobinLabel, maxPairsLabel;
	private JTextField roundRobinField, maxPairsField;	
	
	private JButton okButton, cancelButton;
	
	private SourceSplitDialog(ModelMaker modelMaker, int type) {
		super(modelMaker, "", true);
		this.modelMaker = modelMaker;
		this.type = type;
		
		if (type == RS) {
			setTitle("Record Source Split/Merge");
		} else if (type == MRPS) {
			setTitle("Marked Record Pair Source Split/Merge");
		}
		
		createContent();
		createListeners();
		
		pack();
		setLocationRelativeTo(modelMaker);
	}
	
	private void splitOrMerge() {
		try {
			
			RecordSource source = (RecordSource) createSource();
			Filter filter = createFilter();
			RecordSink sink = (RecordSink) createSink();
			
			source.open();
			sink.open();
			
			while (source.hasNext()) {
				Record r = source.getNext();
				if (filter.satisfy(r)) {
					sink.put(r);
				}
			}
			
			if (sink instanceof RoundRobinSink) {
				((RoundRobinSink)sink).saveSourceDescriptors();
			}
			
			sink.close();
			source.close();
			
		} catch (XmlConfException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}	

	private Source createSource() throws XmlConfException {
		CompositeRecordSource rs = new CompositeRecordSource();
		rs.setModel(modelMaker.getProbabilityModel());

		File[] sources = inputList.getFiles();
		for (int i = 0; i < sources.length; i++) {
			RecordSource rsi = RecordSourceXmlConf.getRecordSource(sources[i].getAbsolutePath());
			rs.add(rsi);
		}

		return rs;
	}
	
	private Sink createSink() throws XmlConfException {		
		if (type == RS) {
			RecordSource genericRs = RecordSourceXmlConf.getRecordSource( sinkField.getText().trim() );
			genericRs.setModel(modelMaker.getProbabilityModel());

			if (singleSinkButton.isSelected()) {
				return genericRs.getSink();
			} else {
				SinkFactory f = null;
				if (genericRs instanceof FlatFileRecordSource) {
					FlatFileRecordSource rs = (FlatFileRecordSource) genericRs;
					f = new FlatFileRecordSinkFactory(rs.getName(), 
													  rs.getFileNamePrefix(), 
													  rs.getFileNameSuffix(),
													  rs.isMultiFile(),
													  rs.isSingleLine(),
													  rs.isFixedLength(),
													  rs.getSeparator(),
													  rs.isTagged(),
													  true, // filter
													  rs.getModel());
				} else if (genericRs instanceof XmlRecordSource) {
					XmlRecordSource rs = (XmlRecordSource) genericRs;
					String fn = rs.getFileName();
					String descriptorBase = fn.substring(0, fn.length() - Constants.RS_EXTENSION.length() - 1);

					String rawXmlFn = rs.getXmlFileName();					
					String contentBase = rawXmlFn;
					String contentExtension = "";
					int dotIndex = rawXmlFn.lastIndexOf('.');
					if (dotIndex >= 0) {
						contentBase = rawXmlFn.substring(0, dotIndex);
						contentExtension = rawXmlFn.substring(dotIndex);
					}
					
					f = new XmlRecordSinkFactory(descriptorBase, contentBase, contentExtension, rs.getModel());
				} else {
					throw new IllegalStateException("Unknown RS type: " + genericRs.getClass().getName());
				}
				
				int distrib = Integer.parseInt(roundRobinField.getText().trim());
				if (distrib <= 0) {
					distrib = 1;
				}
				int maxPairs = Integer.parseInt(maxPairsField.getText().trim());
				if (maxPairs <= 0) {
					maxPairs = Integer.MAX_VALUE;
				}
				return new RoundRobinSink(f, distrib, maxPairs);
			}
		} else {
			throw new UnsupportedOperationException();
		}
		
	}
	
	private Filter createFilter() {

		if (allButton.isSelected()) {
			return new AllFilter();
		} else if (randomPercentButton.isSelected()) {
			float pct = Float.parseFloat(randomPercentField.getText().trim());
			return new RandomFilter(pct);
		} else {
			throw new UnsupportedOperationException();
		}
		
	}
	
	//
	// Creation and listening methods...
	//
			
	private void createContent() {
		GridBagLayout layout = new GridBagLayout();
		layout.columnWeights = new double[] {1};
		layout.rowWeights = new double[] {1, 0, 0, 0, 0};
		getContentPane().setLayout(layout);

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);
		c.fill = GridBagConstraints.BOTH;

		c.gridy = 0;
		JPanel sourcePanel = createSourcePanel();
		getContentPane().add(sourcePanel, c);

		c.gridy++;
		JPanel filterPanel = createFilterPanel();
		getContentPane().add(filterPanel, c);

		c.gridy++;		
		JPanel sinkPanel = createSinkPanel();
		getContentPane().add(sinkPanel, c);
		
		c.gridy++;
		JPanel buttonsPanel = createButtonsPanel();
		getContentPane().add(buttonsPanel, c);
	}
	
	private JPanel createSourcePanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Source: "));
		panel.setLayout(new BorderLayout());
		inputList = new MultiFileList(MultiFileList.RS);
		panel.add(inputList, BorderLayout.CENTER);
		return panel;
	}
	
	private JPanel createFilterPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Filter: "));

		GridBagLayout layout = new GridBagLayout();
		layout.columnWeights = new double[] {0, 1};
		layout.rowWeights = new double[] {0, 0, 0, 0};
		panel.setLayout(layout);

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);
		c.anchor = GridBagConstraints.WEST;

		//
		
		c.gridy = 0;
		c.gridx = 0;
		allButton = new JRadioButton("All Records");
		allButton.setSelected(true);
		panel.add(allButton, c);

		//
		
		c.gridy++;
		c.gridx = 0;
		selectionButton = new JRadioButton("Selection (e.g. \"200-300, 405\")");
		panel.add(selectionButton, c);
		
		c.gridx++;
		selectionField = new JTextField(15);
		selectionField.setMinimumSize(selectionField.getPreferredSize());
		panel.add(selectionField, c);

		//

		c.gridy++;
		c.gridx = 0;
		randomTargetButton = new JRadioButton("Random Records (how many)");
		panel.add(randomTargetButton, c);
		
		c.gridx++;
		randomTargetField = new JTextField(8);
		randomTargetField.setMinimumSize(randomTargetField.getPreferredSize());
		panel.add(randomTargetField, c);
		
		//
	
		c.gridy++;
		c.gridx = 0;
		randomPercentButton = new JRadioButton("Random Records (percent)");
		panel.add(randomPercentButton, c);

		c.gridx++;
		randomPercentField = new JTextField(4);
		randomPercentField.setMinimumSize(randomPercentField.getPreferredSize());
		panel.add(randomPercentField, c);

		return panel;
	}
	
	private JPanel createSinkPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Sink: "));

		GridBagLayout layout = new GridBagLayout();
		layout.columnWeights = new double[] {0, 0, 0, 1, 0, 0};
		layout.rowWeights = new double[] {0, 0, 0, 1};
		panel.setLayout(layout);
		
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);
		c.fill = GridBagConstraints.HORIZONTAL;
		
		//
		
		c.gridy = 0;
		c.gridx = 0;
		c.anchor = GridBagConstraints.WEST;
		panel.add(new JLabel("  Sink:   "), c);
		
		c.gridx++;
		c.gridwidth = 3;
		sinkField = new JTextField(40);
		panel.add(sinkField, c);
		c.gridwidth = 1;
		
		c.gridx += 3;
		browseButton = new JButton("Browse");
		panel.add(browseButton, c);
		
		c.gridx++;
		newButton = new JButton("New");
		newButton.setPreferredSize(browseButton.getPreferredSize());
		panel.add(newButton, c);
		
		//

		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		singleSinkButton = new JRadioButton("Single Sink");
		singleSinkButton.setSelected(true);
		panel.add(singleSinkButton, c);
		c.gridwidth = 1;
		
		//
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 5;
		multiSinkButton = new JRadioButton("Multiple Sinks (SinkName0.rs, SinkName1.rs, SinkName2.rs, ...)");
		panel.add(multiSinkButton, c);
		c.gridwidth = 1;

		//
		
		c.gridy++;
		c.gridx = 1;
		c.gridwidth = 2;
		roundRobinLabel = new JLabel("Round Robin over # files: ");
		roundRobinLabel.setEnabled(false);
		panel.add(roundRobinLabel, c);
		c.gridwidth = 1;
		
		c.gridx += 2;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.NONE;
		roundRobinField = new JTextField(5);
		roundRobinField.setText("1");
		roundRobinField.setEnabled(false);
		roundRobinField.setMinimumSize(roundRobinField.getPreferredSize());
		panel.add(roundRobinField, c);
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		
		//
		
		c.gridy++;
		c.gridx = 1;
		c.gridwidth = 2;
		maxPairsLabel = new JLabel("Max Pairs/Records per file: ");
		maxPairsLabel.setEnabled(false);
		panel.add(maxPairsLabel, c);
		c.gridwidth = 1;
		
		c.gridx += 2;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.NONE;
		maxPairsField = new JTextField(5);
		maxPairsField.setText("0");
		maxPairsField.setEnabled(false);
		maxPairsField.setMinimumSize(maxPairsField.getPreferredSize());
		panel.add(maxPairsField, c);
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;

		// dummy last row
		
		c.gridy++;
		panel.add(new JLabel());

		return panel;
	}
	
	private JPanel createButtonsPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		okButton = new JButton("OK");
		cancelButton = new JButton("Cancel");

		okButton.setPreferredSize(cancelButton.getPreferredSize());
		okButton.setEnabled(false);
		
		panel.add(Box.createHorizontalGlue());
		panel.add(okButton);
		panel.add(Box.createHorizontalStrut(5));
		panel.add(cancelButton);
		
		return panel;
	}

	private void createListeners() {
		
		// source panel
		
		inputList.addListener(new ListDataListener() {
			public void intervalAdded(ListDataEvent e) {
				updateOkButton();
			}
			public void intervalRemoved(ListDataEvent e) {
				updateOkButton();
			}
			public void contentsChanged(ListDataEvent e) {
				updateOkButton();
			}			
		});
		
		// filter panel
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(allButton);
		bg.add(selectionButton);
		bg.add(randomTargetButton);
		bg.add(randomPercentButton);

		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateOkButton();
			}
		};
		
		allButton.addActionListener(al);
		selectionButton.addActionListener(al);
		randomTargetButton.addActionListener(al);
		randomPercentButton.addActionListener(al);
		
		selectionField.getDocument().addDocumentListener(new DocumentEventRedirector() {
			public void redirectedEvent(DocumentEvent e) {
				selectionButton.setSelected(true);
				updateOkButton();
			}
		});

		randomTargetField.getDocument().addDocumentListener(new DocumentEventRedirector() {
			public void redirectedEvent(DocumentEvent e) {
				randomTargetButton.setSelected(true);
				updateOkButton();
			}
		});

		randomPercentField.getDocument().addDocumentListener(new DocumentEventRedirector() {
			public void redirectedEvent(DocumentEvent e) {
				randomPercentButton.setSelected(true);
				updateOkButton();
			}
		});
				
		// sink panel
		
		ButtonGroup bg2 = new ButtonGroup();
		bg2.add(singleSinkButton);
		bg2.add(multiSinkButton);
		
		sinkField.getDocument().addDocumentListener(new DocumentEventRedirector() {
			public void redirectedEvent(DocumentEvent e) {
				updateOkButton();
			}
		});
		
		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File file = null;
				if (type == RS) {
					file = FileChooserFactory.selectRsFile(modelMaker);
				} else if (type == MRPS) {
					file = FileChooserFactory.selectMrpsFile(modelMaker);
				} else {
					throw new IllegalStateException();
				}
				if (file != null) {
					sinkField.setText(file.getAbsolutePath());
				}
			}
		});
		
		newButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Source src = null;
				if (type == RS) {
					src = new SourceTypeSelectorDialog(modelMaker, SourceTypeSelectorDialog.RS, true).define();
				} else if (type == MRPS) {
					src = new SourceTypeSelectorDialog(modelMaker, SourceTypeSelectorDialog.MRPS, true).define();					
				} else {
					throw new IllegalStateException();
				}
				if (src != null) {
					sinkField.setText(src.getFileName());
					try {
						if (type == RS) {
							RecordSourceXmlConf.add((RecordSource)src);
						} else if (type == MRPS) {
							MarkedRecordPairSourceXmlConf.add((MarkedRecordPairSource)src);
						}
					} catch (XmlConfException ex) {
						ex.printStackTrace();
					}
				}
			}			
		});
		
		multiSinkButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				boolean enabled = e.getStateChange() == ItemEvent.SELECTED;
				roundRobinLabel.setEnabled(enabled);
				roundRobinField.setEnabled(enabled);
				maxPairsLabel.setEnabled(enabled);
				maxPairsField.setEnabled(enabled);
				
				updateOkButton();
			}
		});
		
		roundRobinField.getDocument().addDocumentListener(new DocumentEventRedirector() {
			public void redirectedEvent(DocumentEvent e) {
				updateOkButton();
			}
		});

		maxPairsField.getDocument().addDocumentListener(new DocumentEventRedirector() {
			public void redirectedEvent(DocumentEvent e) {
				updateOkButton();
			}
		});

		// buttons panel
		
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				splitOrMerge();
			}
		});
		
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
	}
	
	private void updateOkButton() {
		boolean enabled = true;
		enabled &= inputList.getNumFiles() > 0;
		enabled &= checkFilterPanel();
		enabled &= checkSinkPanel();
		
		okButton.setEnabled(enabled);
	}
	
	private boolean checkFilterPanel() {
		if (allButton.isSelected()) {
			return true;
		} else if (selectionButton.isSelected()) {
			return hasText(selectionField);
		} else if (randomTargetButton.isSelected()) {
			return hasText(randomTargetField);
		} else {
			return hasText(randomPercentField);
		}
	}
	
	private boolean checkSinkPanel() {
		if (!hasText(sinkField)) {
			return false;
		}

		if (singleSinkButton.isSelected()) {
			return true;
		} else {
			return hasText(roundRobinField) && hasText(maxPairsField);
		}
	}
	
	public boolean hasText(JTextField field) {
		return field.getText().trim().length() > 0;
	}
	
	public static abstract class DocumentEventRedirector implements DocumentListener {
		public void insertUpdate(DocumentEvent e) {
			redirectedEvent(e);
		}
		public void removeUpdate(DocumentEvent e) {
			redirectedEvent(e);
		}
		public void changedUpdate(DocumentEvent e) {
			redirectedEvent(e);
		}
		public abstract void redirectedEvent(DocumentEvent e);
	}
	
}
