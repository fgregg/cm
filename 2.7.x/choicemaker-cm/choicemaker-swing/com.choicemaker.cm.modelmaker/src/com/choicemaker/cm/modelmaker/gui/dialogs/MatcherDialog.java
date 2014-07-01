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
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.jdom.Element;

import com.choicemaker.cm.analyzer.matcher.SimpleRecordSink;
import com.choicemaker.cm.analyzer.sampler.DefaultPairSampler;
import com.choicemaker.cm.analyzer.sampler.PairSampler;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.MarkedRecordPairSink;
import com.choicemaker.cm.core.MarkedRecordPairSource;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.Source;
import com.choicemaker.cm.core.base.DescriptorCollection;
import com.choicemaker.cm.core.base.ImmutableThresholds;
import com.choicemaker.cm.core.base.Thresholds;
import com.choicemaker.cm.core.blocking.InMemoryBlocker;
import com.choicemaker.cm.core.util.LoggingObject;
import com.choicemaker.cm.core.util.MessageUtil;
import com.choicemaker.cm.core.xmlconf.MarkedRecordPairSourceXmlConf;
import com.choicemaker.cm.core.xmlconf.RecordSourceXmlConf;
import com.choicemaker.cm.core.xmlconf.XmlConfException;
import com.choicemaker.cm.gui.utils.ExtensionHolder;
import com.choicemaker.cm.gui.utils.JavaHelpUtils;
import com.choicemaker.cm.gui.utils.dialogs.FileChooserFactory;
import com.choicemaker.cm.gui.utils.viewer.CompositePaneModel;
import com.choicemaker.cm.gui.utils.viewer.xmlconf.RecordPairViewerXmlConf;
import com.choicemaker.cm.modelmaker.filter.ListeningMarkedRecordPairFilter;
import com.choicemaker.cm.modelmaker.filter.ModelMakerMRPFilter;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.matcher.BlockerToolkit;
import com.choicemaker.cm.modelmaker.gui.matcher.MatchDialogBlockerPlugin;
import com.choicemaker.cm.modelmaker.gui.matcher.Matcher;
import com.choicemaker.cm.modelmaker.gui.panels.FilterCluePanel;
import com.choicemaker.cm.modelmaker.gui.panels.RecordPairViewerPanel;
import com.choicemaker.cm.modelmaker.gui.utils.Enable;
import com.choicemaker.cm.modelmaker.gui.utils.EnablednessGuard;

/**
 * Description
 *
 * @author  Martin Buechi
 * @version $Revision: 1.2 $ $Date: 2010/03/29 12:44:29 $
 */
public class MatcherDialog extends JDialog implements Enable {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(MatcherDialog.class);
	private static int MRPS_OUTPUT = 0;

	private ModelMaker modelMaker;
	private JTabbedPane tabbedPane;
	private JButton matchButton;
	private JButton cancelButton;
	private JTextField small;
	private JButton smallBrowse;
	private JButton smallNew;
	private JButton smallPreview;
	private JCheckBox deduplicateSingleSource;
	private JLabel largeLabel;
	private JTextField large;
	private JButton largeBrowse;
	private JButton largeNew;
	private JButton largePreview;
	private JComboBox blocking;
	private JCheckBox excludeMatchesToSelf;
	private JTextField maxNumMatchesPerSourceRecord;
	private JComboBox sortOrder;
	private JCheckBox useSamplerBox;
	private JLabel sampleSizeLabel;
	private JTextField sampleSizeField;
	private JComboBox outputFormat;
	private JTextField sink;
	private JButton sinkBrowse;
	private JButton sinkNew;
	private MatchDialogBlockerPlugin blockerPlugin;
	private JPanel blockerPluginContainer;
	private FilterCluePanel filterCluePanel;

	private final String idx_small = "small";
	private final String idx_deduplicateSingleSource =
		"deduplicateSingleSource";
	private final String idx_large = "large";
	private final String idx_blocking = "blocking";
	private final String idx_excludeMatchesToSelf = "excludeMatchesToSelf";
	private final String idx_maxNumMatchesPerSourceRecord =
		"maxNumMatchesPerSourceRecord";
	private final String idx_sortOrder = "sortOrder";
	private final String idx_useSamplerBox = "useSamplerBox";
	private final String idx_sampleSizeField = "sampleSizeField";
	private final String idx_outputFormat = "outputFormat";
	private final String idx_sink = "sink";
	private final String idx_filters_prefix = "filter_";
	private final String idx_probablityLowerBound = "probablityLowerBound";
	private final String idx_probablityUpperBound = "probablityUpperBound";

	private final String default_small = "";
	private final boolean default_deduplicateSingleSource = true;
	private final String default_large = "";
	private final int default_blocking = 0;
	private final boolean default_excludeMatchesToSelf = true;
	private final String default_maxNumMatchesPerSourceRecord = "0";
	private final int default_sortOrder = 0;
	private final boolean default_useSamplerBox = false;
	private final String default_sampleSizeField = "1000";
	private final int default_outputFormat = 0;
	private final String default_sink = "";
	private final boolean default_isFiltered = true;
	private final boolean[] default_filters =
		new boolean[] { default_isFiltered, default_isFiltered, default_isFiltered };
	private final float default_probablityLowerBound = ImmutableThresholds.MIN_VALUE;
	private final float default_probablityUpperBound = ImmutableThresholds.MAX_VALUE;

	private void loadItems() {
		Preferences prefs = Preferences.userNodeForPackage(MatcherDialog.class);
		this.small.setText(prefs.get(idx_small, default_small));
		this.deduplicateSingleSource.setSelected(
			prefs.getBoolean(
				idx_deduplicateSingleSource,
				default_deduplicateSingleSource));
		this.large.setText(prefs.get(idx_large, default_large));
		try {
			this.blocking.setSelectedIndex(
				prefs.getInt(idx_blocking, default_blocking));
		} catch (Exception x) {
			this.blocking.setSelectedIndex(default_blocking);
		}
		this.excludeMatchesToSelf.setSelected(
			prefs.getBoolean(
				idx_excludeMatchesToSelf,
				default_excludeMatchesToSelf));
		this.maxNumMatchesPerSourceRecord.setText(
			prefs.get(
				idx_maxNumMatchesPerSourceRecord,
				default_maxNumMatchesPerSourceRecord));
		try {
			this.sortOrder.setSelectedIndex(
				prefs.getInt(idx_sortOrder, default_sortOrder));
		} catch (Exception x) {
			this.sortOrder.setSelectedIndex(default_sortOrder);
		}
		this.useSamplerBox.setSelected(
			prefs.getBoolean(idx_useSamplerBox, default_useSamplerBox));
		this.sampleSizeField.setText(
			prefs.get(idx_sampleSizeField, default_sampleSizeField));
		try {
			this.outputFormat.setSelectedIndex(
				prefs.getInt(idx_outputFormat, default_outputFormat));
		} catch (Exception x) {
			this.outputFormat.setSelectedIndex(default_outputFormat);
		}
		this.sink.setText(prefs.get(idx_sink, default_sink));

		final int EXPECTED =
			this.filterCluePanel.getChoiceMakerDecisionFilters().length;
		boolean[] prefValues = new boolean[EXPECTED];
		for (int i = 0; i < EXPECTED; i++) {
			String idx = makeFilterIndex(i);
			prefValues[i] = prefs.getBoolean(idx, this.default_isFiltered);
		}
		this.filterCluePanel.setChoiceMakerDecisionFilters(prefValues);
		this.filterCluePanel.setProbabilityLowerBound(
			prefs.getFloat(
				this.idx_probablityLowerBound,
				this.default_probablityLowerBound));
		this.filterCluePanel.setProbabilityUpperBound(
			prefs.getFloat(
				this.idx_probablityUpperBound,
				this.default_probablityUpperBound));

	}

	private String makeFilterIndex(int i) {
		return this.idx_filters_prefix + i;
	}

	private void saveItems() {
		Preferences prefs = Preferences.userNodeForPackage(MatcherDialog.class);
		prefs.put(idx_small, this.small.getText());
		prefs.putBoolean(
			idx_deduplicateSingleSource,
			this.deduplicateSingleSource.isSelected());
		prefs.put(idx_large, this.large.getText());
		prefs.putInt(idx_blocking, this.blocking.getSelectedIndex());
		prefs.putBoolean(
			idx_excludeMatchesToSelf,
			this.excludeMatchesToSelf.isSelected());
		prefs.put(
			idx_maxNumMatchesPerSourceRecord,
			this.maxNumMatchesPerSourceRecord.getText());
		prefs.putInt(idx_sortOrder, this.sortOrder.getSelectedIndex());
		prefs.putBoolean(idx_useSamplerBox, this.useSamplerBox.isSelected());
		prefs.put(idx_sampleSizeField, this.sampleSizeField.getText());
		prefs.putInt(idx_outputFormat, this.outputFormat.getSelectedIndex());
		prefs.put(idx_sink, this.sink.getText());

		boolean[] filterValues =
			this.filterCluePanel.getChoiceMakerDecisionFilters();
		for (int i = 0; i < filterValues.length; i++) {
			String idx = makeFilterIndex(i);
			prefs.putBoolean(idx, filterValues[i]);
		}
		prefs.putFloat(
			this.idx_probablityLowerBound,
			this.filterCluePanel.getProbabilityLowerBound());
		prefs.putFloat(
			this.idx_probablityUpperBound,
			this.filterCluePanel.getProbabilityUpperBound());

	}

	public MatcherDialog(ModelMaker modelMaker) {
		super(
			modelMaker,
			MessageUtil.m.formatMessage(
				"train.gui.modelmaker.dialog.matcher.label"),
			true);
		this.modelMaker = modelMaker;
		buildContent();
		addContentListners();
		pack();
		setLocationRelativeTo(modelMaker);
		setBlockingPlugin();
		setEnabledness();
	}

	private void setBlockingPlugin() {
		blockerPluginContainer.removeAll();
		try {
			blockerPlugin =
				(
					(BlockerToolkit) ((ExtensionHolder) blocking
						.getSelectedItem())
						.getInstance())
						.getDialogPlugin(
					modelMaker.getProbabilityModel());
		} catch (CoreException e) {
			e.printStackTrace();
		}
		blockerPluginContainer.add(blockerPlugin, BorderLayout.CENTER);
		pack();
	}

	private PairSampler getSampler(IProbabilityModel model) {
		if (useSamplerBox.isSelected()) {
			int sampleSize = Integer.parseInt(sampleSizeField.getText());
			return new DefaultPairSampler(model, sampleSize);
		} else {
			return null;
		}
	}

	private void addContentListners() {
		matchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					RecordSource smallSource =
						RecordSourceXmlConf.getRecordSource(small.getText());
					RecordSource largeSource;
					if (deduplicateSingleSource.isSelected()) {
						largeSource = null;
					} else {
						largeSource =
							RecordSourceXmlConf.getRecordSource(
								large.getText());
					}
					InMemoryBlocker blocker = blockerPlugin.getBlocker();
					MarkedRecordPairSink outputSink = null;
					if (outputFormat.getSelectedIndex() == MRPS_OUTPUT) {
						MarkedRecordPairSource outputSource =
							MarkedRecordPairSourceXmlConf
								.getMarkedRecordPairSource(
								sink.getText());
						if (outputSource.hasSink()) {
							outputSink =
								(MarkedRecordPairSink) outputSource.getSink();
						}
					} else {
						outputSink =
							new SimpleRecordSink(new File(sink.getText()));
					}
					if (outputSink != null) {
						IProbabilityModel probabilityModel =
							modelMaker.getProbabilityModel();
						filterCluePanel.set();
						ListeningMarkedRecordPairFilter filter = filterCluePanel.getFilter();
						PairSampler sampler = getSampler(probabilityModel);
						Thresholds thresholds = modelMaker.getThresholds();
						float lowerThreshold = thresholds.getDifferThreshold();
						float upperThreshold = thresholds.getMatchThreshold();
						String user = System.getProperty("user.name");
						String src = "matcher";
						String comment = "";
						int mnm =
							Integer.parseInt(
								maxNumMatchesPerSourceRecord.getText());
						final Matcher matcher =
							new Matcher(
								smallSource,
								largeSource,
								outputSink,
								blocker,
								filter,
								sampler,
								probabilityModel,
								lowerThreshold,
								upperThreshold,
								user,
								src,
								comment,
								excludeMatchesToSelf.isSelected(),
								mnm,
								sortOrder.getSelectedIndex());

						// Good to go, so save preferences now
						saveItems();

						final Thread t = new Thread("Matcher Thread") {
							public void run() {
								try {
									matcher.match();
								} catch (IOException e) {
									logger.error(
										new LoggingObject("CM-100401"),
										e);
								}
							}
						};
						MatcherProgressDialog matcherProgressDialog =
							new MatcherProgressDialog(modelMaker, matcher, t);
						t.start();
						matcherProgressDialog.show();
					}
				} catch (XmlConfException ex) {
					logger.error(new LoggingObject("CM-100401"), ex);
				}
				dispose();
			}
		});

		//cancelButton
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				dispose();
			}
		});

		ActionListener browseActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (evt.getSource() == sinkBrowse) {
					if (outputFormat.getSelectedIndex() == MRPS_OUTPUT) {
						File file =
							FileChooserFactory.selectMrpsFile(modelMaker);
						if (file != null) {
							sink.setText(file.getAbsolutePath());
						}
					} else {
						File file =
							FileChooserFactory.selectFlatFile(modelMaker);
						if (file != null) {
							sink.setText(file.getAbsolutePath());
						}
					}
				} else {
					File file = FileChooserFactory.selectRsFile(modelMaker);
					if (file != null) {
						if (evt.getSource() == largeBrowse) {
							large.setText(file.getAbsolutePath());
						} else if (evt.getSource() == smallBrowse) {
							small.setText(file.getAbsolutePath());
						}
					}
				}
			}
		};
		smallBrowse.addActionListener(browseActionListener);
		largeBrowse.addActionListener(browseActionListener);
		sinkBrowse.addActionListener(browseActionListener);

		sinkNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				Source s =
					new SourceTypeSelectorDialog(modelMaker, true).define();
				if (s != null) {
					try {
						MarkedRecordPairSourceXmlConf.add(
							(MarkedRecordPairSource) s);
					} catch (XmlConfException e) {
						logger.error(
							new LoggingObject("CM-100402", s.getFileName()),
							e);
					}
					sink.setText(((MarkedRecordPairSource) s).getFileName());
				}
			}
		});

		ActionListener sourceNew = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				Source s =
					new SourceTypeSelectorDialog(
						modelMaker,
						SourceTypeSelectorDialog.RS,
						false)
						.define();
				if (s != null) {
					try {
						RecordSourceXmlConf.add((RecordSource) s);
					} catch (XmlConfException e) {
						logger.error(
							new LoggingObject("CM-100403", s.getFileName()),
							e);
					}
					String path = ((RecordSource) s).getFileName();
					if (evt.getSource() == smallNew) {
						small.setText(path);
					} else {
						large.setText(path);
					}
				}
			}
		};
		smallNew.addActionListener(sourceNew);
		largeNew.addActionListener(sourceNew);

		deduplicateSingleSource.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				setEnabledness();
			}
		});

		smallPreview.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					Element e =
						RecordPairViewerXmlConf.modelToXml(
							RecordPairViewerPanel
								.instance
								.getViewer()
								.getCompositePaneModel());
					CompositePaneModel compositePaneModel =
						RecordPairViewerXmlConf.compositePaneModelFromXml(
							e,
							new DescriptorCollection(
								modelMaker
									.getProbabilityModel()
									.getAccessor()
									.getDescriptor()));
					compositePaneModel.setEnableEditing(true);
					new RecordSourceViewerDialog(
						MatcherDialog.this.modelMaker,
						RecordSourceXmlConf.getRecordSource(small.getText()),
						modelMaker.getProbabilityModel(),
						compositePaneModel)
						.show();
				} catch (XmlConfException ex) {
					logger.error(
						new LoggingObject("CM-100601", small.getText()),
						ex);
				}
			}
		});
		largePreview.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					Element e =
						RecordPairViewerXmlConf.modelToXml(
							RecordPairViewerPanel
								.instance
								.getViewer()
								.getCompositePaneModel());
					CompositePaneModel compositePaneModel =
						RecordPairViewerXmlConf.compositePaneModelFromXml(
							e,
							new DescriptorCollection(
								modelMaker
									.getProbabilityModel()
									.getAccessor()
									.getDescriptor()));
					compositePaneModel.setEnableEditing(true);
					new RecordSourceViewerDialog(
						MatcherDialog.this.modelMaker,
						RecordSourceXmlConf.getRecordSource(large.getText()),
						modelMaker.getProbabilityModel(),
						compositePaneModel)
						.show();
				} catch (XmlConfException ex) {
					logger.error(
						new LoggingObject("CM-100601", large.getText()),
						ex);
				}
			}
		});

		useSamplerBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				setEnabledness();
			}
		});

		EnablednessGuard dl = new EnablednessGuard(this);
		small.getDocument().addDocumentListener(dl);
		large.getDocument().addDocumentListener(dl);
		sink.getDocument().addDocumentListener(dl);
		maxNumMatchesPerSourceRecord.getDocument().addDocumentListener(dl);

		sampleSizeField.getDocument().addDocumentListener(dl);

		outputFormat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledness();
			}
		});

		blocking.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				setBlockingPlugin();
			}
		});

		JavaHelpUtils.enableHelpKey(this, "train.gui.dialog.matcher");
	}

	public void setEnabledness() {
		smallPreview.setEnabled(small.getText().length() > 0);
		largePreview.setEnabled(
			large.getText().length() > 0
				&& !deduplicateSingleSource.isSelected());

		boolean b = !deduplicateSingleSource.isSelected();
		largeLabel.setEnabled(b);
		large.setEnabled(b);
		largeBrowse.setEnabled(b);
		largeNew.setEnabled(b);

		boolean mnm = false;
		try {
			mnm = Integer.parseInt(maxNumMatchesPerSourceRecord.getText()) >= 0;
		} catch (NumberFormatException ex) {
			// ignore
		}

		int sampleSize = 0;
		try {
			sampleSize = Integer.parseInt(sampleSizeField.getText());
		} catch (NumberFormatException ex) {
			// ignore
		}
		sampleSizeLabel.setEnabled(useSamplerBox.isSelected());
		sampleSizeField.setEnabled(useSamplerBox.isSelected());

		sinkNew.setEnabled(outputFormat.getSelectedIndex() == MRPS_OUTPUT);

		boolean o =
			mnm
				&& small.getText().length() > 0
				&& (deduplicateSingleSource.isSelected()
					|| large.getText().length() > 0)
				&& (!useSamplerBox.isSelected() || sampleSize > 0)
				&& sink.getText().length() > 0;
		matchButton.setEnabled(o);
	}

	private void buildContent() {
		JPanel content = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		content.setLayout(layout);
		layout.columnWeights = new double[] { 1 };
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 5, 5);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 5;
		c.gridx = 0;
		c.gridy = 0;

		tabbedPane = new JTabbedPane();
		tabbedPane.addTab(
			MessageUtil.m.formatMessage(
				"train.gui.modelmaker.dialog.matcher.sources.label"),
			getSourcePanel());
		tabbedPane.addTab(
			MessageUtil.m.formatMessage(
				"train.gui.modelmaker.dialog.matcher.blocking.label"),
			getBlockingPanel());
		tabbedPane.addTab(
			MessageUtil.m.formatMessage(
				"train.gui.modelmaker.dialog.matcher.filter.label"),
			getFilterPanel());
		tabbedPane.addTab("Sampler", getSamplerPanel());
		content.add(tabbedPane, c);

		c.gridy = 1;
		content.add(getOutputPanel(), c);

		c.gridy = 2;
		c.gridx = 3;
		c.gridwidth = 1;
		matchButton =
			new JButton(
				MessageUtil.m.formatMessage(
					"train.gui.modelmaker.dialog.matcher.match"));
		content.add(matchButton, c);
		cancelButton = new JButton(MessageUtil.m.formatMessage("cancel"));
		c.gridx = 4;
		content.add(cancelButton, c);

		loadItems();

		setContentPane(content);
	}

	private JPanel getSourcePanel() {
		JPanel content = new JPanel();
		content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		GridBagLayout layout = new GridBagLayout();
		layout.rowWeights = new double[] { 0, 0, 0, 1 };
		layout.columnWeights = new double[] { 0, 1, 0, 0, 0 };
		content.setLayout(layout);

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 5, 5);

		c.gridy = 0;
		c.gridx = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		JLabel smallLabel =
			new JLabel(
				MessageUtil.m.formatMessage(
					"train.gui.modelmaker.dialog.matcher.sources.small"));
		content.add(smallLabel, c);
		c.gridx = 1;
		small = new JTextField(20);
		content.add(small, c);
		c.gridx = 2;
		smallBrowse =
			new JButton(MessageUtil.m.formatMessage("browse.elipsis"));
		content.add(smallBrowse, c);
		c.gridx = 3;
		smallNew = new JButton(MessageUtil.m.formatMessage("new.elipsis"));
		content.add(smallNew, c);
		c.gridx = 4;
		smallPreview = new JButton("Preview");
		content.add(smallPreview);

		c.gridy = 1;
		c.gridx = 0;
		JLabel deduplicateSingleLabel = new JLabel("Deduplicate single source");
		content.add(deduplicateSingleLabel, c);
		c.gridx = 1;
		deduplicateSingleSource = new JCheckBox();
		deduplicateSingleSource.setSelected(true);
		content.add(deduplicateSingleSource, c);

		c.gridy = 2;
		c.gridx = 0;
		largeLabel =
			new JLabel(
				MessageUtil.m.formatMessage(
					"train.gui.modelmaker.dialog.matcher.sources.large"));
		content.add(largeLabel, c);
		c.gridx = 1;
		large = new JTextField(20);
		content.add(large, c);
		c.gridx = 2;
		largeBrowse =
			new JButton(MessageUtil.m.formatMessage("browse.elipsis"));
		content.add(largeBrowse, c);
		c.gridx = 3;
		largeNew = new JButton(MessageUtil.m.formatMessage("new.elipsis"));
		content.add(largeNew, c);
		c.gridx = 4;
		largePreview = new JButton("Preview");
		content.add(largePreview, c);

		// vertical pad
		c.gridy++;
		content.add(new JLabel(), c);

		return content;
	}

	private JPanel getBlockingPanel() {
		JPanel content = new JPanel();
		content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		GridBagLayout layout = new GridBagLayout();
		layout.columnWeights = new double[] { 0, 0, 1 };
		layout.rowWeights = new double[] { 0, 0, 1 };
		content.setLayout(layout);

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 5, 5);

		c.gridy = 0;
		c.gridx = 0;
		JLabel algorithm =
			new JLabel(
				MessageUtil.m.formatMessage(
					"train.gui.modelmaker.dialog.matcher.blocking.algorithm"));
		content.add(algorithm, c);
		c.gridx = 1;
		blocking =
			new JComboBox(
				ExtensionHolder.getExtensionHolders(
					Platform.getPluginRegistry().getExtensionPoint(
						"com.choicemaker.cm.modelmaker.matcherBlockingToolkit")));
		content.add(blocking);

		c.gridy = 1;
		c.gridx = 0;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.BOTH;
		blockerPluginContainer = new JPanel(new BorderLayout());
		content.add(blockerPluginContainer, c);

		// vertical pad
		c.gridy++;
		content.add(new JLabel(), c);

		return content;
	}

	private JPanel getFilterPanel() {
		JPanel content = new JPanel();
		content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		GridBagLayout layout = new GridBagLayout();
		layout.columnWeights = new double[] { 0, 0, 1 };
		layout.rowWeights = new double[] { 0, 1 };
		content.setLayout(layout);

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 5, 5);

		c.gridy = 0;
		c.gridx = 0;
		c.fill = GridBagConstraints.BOTH;
		content.add(new JLabel("Exclude matches to same ID"), c);
		c.gridx = 1;
		excludeMatchesToSelf = new JCheckBox();
		excludeMatchesToSelf.setSelected(true);
		content.add(excludeMatchesToSelf, c);

		c.gridy = 1;
		c.gridx = 0;
		c.gridwidth = 3;
		ListeningMarkedRecordPairFilter mrpf = new ModelMakerMRPFilter(modelMaker);
		mrpf.setChoiceMakerDecision(new boolean[] { false, true, true });
		filterCluePanel = new FilterCluePanel(modelMaker, mrpf, false);
		content.add(filterCluePanel, c);
		c.gridwidth = 1;

		c.gridy = 2;
		c.gridx = 0;
		content.add(new JLabel("Sort"), c);
		c.gridx = 1;
		sortOrder =
			new JComboBox(
				new String[] {
					"No sorting",
					"Lexicographical: decision, probability decreasing",
					"Probability decreasing" });
		content.add(sortOrder, c);

		c.gridy = 3;
		c.gridx = 0;
		content.add(new JLabel("Max num matches per source record"), c);
		c.gridx = 1;
		maxNumMatchesPerSourceRecord = new JTextField();
		maxNumMatchesPerSourceRecord.setText("0");
		content.add(maxNumMatchesPerSourceRecord, c);

		return content;
	}

	private JPanel getSamplerPanel() {
		JPanel content = new JPanel();
		content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		GridBagLayout layout = new GridBagLayout();
		layout.columnWeights = new double[] { 0, 0, 1 };
		layout.rowWeights = new double[] { 0, 0, 1 };
		content.setLayout(layout);

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 5, 10);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;

		//

		c.gridy = 0;

		c.gridx = 0;
		content.add(new JLabel("Use Output Pair Sampler"), c);

		c.gridx++;
		useSamplerBox = new JCheckBox();
		content.add(useSamplerBox, c);

		//

		c.gridy++;

		c.gridx = 0;
		sampleSizeLabel = new JLabel("Sample Size");
		content.add(sampleSizeLabel, c);

		c.gridx++;
		sampleSizeField = new JTextField(10);
		content.add(sampleSizeField, c);

		// padding

		c.gridy++;
		c.gridx = 3;
		content.add(new JLabel(), c);

		return content;
	}

	private JPanel getOutputPanel() {
		JPanel content = new JPanel();
		content.setBorder(
			BorderFactory.createTitledBorder(
				MessageUtil.m.formatMessage(
					"train.gui.modelmaker.dialog.matcher.output.label")));

		GridBagLayout layout = new GridBagLayout();
		layout.columnWeights = new double[] { 0, 1, 0, 0 };
		content.setLayout(layout);

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 5, 5);

		c.gridx = 0;
		c.gridy = 0;
		content.add(new JLabel("Format"), c);
		c.gridx = 1;
		c.anchor = GridBagConstraints.WEST;
		outputFormat =
			new JComboBox(
				new String[] {
					"Marked Record Pair Source",
					"Id, Id, decision, probability CSV file" });
		content.add(outputFormat, c);

		c.gridy = 1;
		c.gridx = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		JLabel sinkLabel =
			new JLabel(
				MessageUtil.m.formatMessage(
					"train.gui.modelmaker.dialog.matcher.output.sink"));
		content.add(sinkLabel, c);
		c.gridx = 1;
		sink = new JTextField(20);
		content.add(sink, c);
		c.gridx = 2;
		sinkBrowse = new JButton(MessageUtil.m.formatMessage("browse.elipsis"));
		content.add(sinkBrowse, c);
		c.gridx = 3;
		sinkNew = new JButton(MessageUtil.m.formatMessage("new.elipsis"));
		content.add(sinkNew, c);

		return content;
	}
}
