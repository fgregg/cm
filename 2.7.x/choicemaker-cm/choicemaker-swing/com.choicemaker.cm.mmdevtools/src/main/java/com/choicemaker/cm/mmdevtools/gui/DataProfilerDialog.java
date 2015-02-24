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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.IntrospectionException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.choicemaker.cm.core.Descriptor;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.MarkedRecordPairSource;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.base.MarkedRecordPairBinder;
import com.choicemaker.cm.core.base.MutableMarkedRecordPair;
import com.choicemaker.cm.core.util.LogFrequencyPartitioner;
import com.choicemaker.cm.core.xmlconf.MarkedRecordPairSourceXmlConf;
import com.choicemaker.cm.core.xmlconf.RecordSourceXmlConf;
import com.choicemaker.cm.gui.utils.dialogs.ErrorDialog;
import com.choicemaker.cm.gui.utils.dialogs.FileChooserFactory;
import com.choicemaker.cm.mmdevtools.io.MrpsToRsAdapter;
import com.choicemaker.cm.mmdevtools.util.profiler.DefaultDateProfiler;
import com.choicemaker.cm.mmdevtools.util.profiler.DefaultIntProfiler;
import com.choicemaker.cm.mmdevtools.util.profiler.DefaultStringProfiler;
import com.choicemaker.cm.mmdevtools.util.profiler.FieldAccessor;
import com.choicemaker.cm.mmdevtools.util.profiler.FieldProfiler;
import com.choicemaker.cm.modelmaker.filter.ModelMakerCollectionMRPairFilter;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;

/**
 * @author ajwinkel
 *
 */
public class DataProfilerDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	public static final String RECORDS_READ = "Num Records Read:";

	private static DataProfilerDialog dialog;

	public static synchronized void showDataProfilerDialog(ModelMaker mm) {
		//if (dialog == null) {
			dialog = new DataProfilerDialog(mm);
		//}
		dialog.setVisible(true);
	}

	protected ModelMaker modelMaker;

	private JComboBox fieldBox;
	private JRadioButton currentSelectionRadio, openSourceRadio, mrpsRadio, rsRadio;
	private JTextField mrpsField, rsField;

	private JTabbedPane tabs;
	private JTable scalarStatsTable;
	private JButton createFilesButton;

	private RecordSource rs;
	private FieldAccessor fieldAccessor;
	private FieldProfiler fieldProfiler;

	public DataProfilerDialog(ModelMaker modelMaker) {
		super(modelMaker, "Data Profiler", false);
		this.modelMaker = modelMaker;
		createContent();

		pack();
		setLocationRelativeTo(modelMaker);
	}

	private void createContent() {
		GridBagLayout layout = new GridBagLayout();
		layout.columnWeights = new double[] {0, 0, 1, 0, 0, 0};
		layout.rowWeights = new double[] {0, 0, 0, 0, 0, 0, 1, 0};
		getContentPane().setLayout(layout);

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(3, 5, 3, 5);
		c.fill = GridBagConstraints.HORIZONTAL;

		//

		c.gridy = 0;
		c.gridx = 0;
		getContentPane().add(new JLabel("Field Name:"), c);

		c.gridx = 1;
		c.gridwidth = 4;
		fieldBox = new JComboBox( getChoosableColumns(modelMaker.getProbabilityModel().getAccessor().getDescriptor()) );
		fieldBox.setEditable(false);
		getContentPane().add(fieldBox, c);
		c.gridwidth = 1;

		//

		c.gridy++;
		c.gridx = 0;
		getContentPane().add(new JLabel("Data Source:"), c);

		c.gridx = 1;
		c.gridwidth = 2;
		openSourceRadio = new JRadioButton("Current MRPS");
		openSourceRadio.setSelected(true);
		getContentPane().add(openSourceRadio, c);
		c.gridwidth = 1;

		//

		c.gridy++;
		c.gridx = 1;
		c.gridwidth = 2;
		currentSelectionRadio = new JRadioButton("Current MRPS Selection");
		getContentPane().add(currentSelectionRadio, c);
		c.gridwidth = 1;

		//

		c.gridy++;
		c.gridx = 1;
		mrpsRadio = new JRadioButton("MRPS:");
		getContentPane().add(mrpsRadio, c);

		c.gridx = 2;
		c.gridwidth = 3;
		mrpsField = new JTextField(40);
		getContentPane().add(mrpsField, c);
		c.gridwidth = 1;

		c.gridx = 5;
		getContentPane().add(new JButton(new MrpsBrowseAction()), c);

		//

		c.gridy++;
		c.gridx = 1;
		rsRadio = new JRadioButton("RS:");
		getContentPane().add(rsRadio, c);

		c.gridx = 2;
		c.gridwidth = 3;
		rsField = new JTextField(40);
		getContentPane().add(rsField, c);
		c.gridwidth = 1;

		c.gridx = 5;
		getContentPane().add(new JButton(new RsBrowseAction()), c);

		ButtonGroup bgSource = new ButtonGroup();
		bgSource.add(openSourceRadio);
		bgSource.add(currentSelectionRadio);
		bgSource.add(mrpsRadio);
		bgSource.add(rsRadio);

		//

		c.gridy++;
		c.gridx = 3;
		getContentPane().add(new JButton(new ComputeAction()), c);

		c.gridx = 4;
		getContentPane().add(new JButton(new CloseAction()), c);

		//

		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 6;
		c.fill = GridBagConstraints.BOTH;

		tabs = new JTabbedPane();
		scalarStatsTable = new JTable(new Object[0][2], new Object[] {"Property", "Value"});
		tabs.addTab("Scalar Properties", new JScrollPane(scalarStatsTable));

		getContentPane().add(tabs, c);
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;

		//

		c.gridy++;
		c.gridx = 4;
		createFilesButton = new JButton(new CreateFilesAction());
		getContentPane().add(createFilesButton, c);
	}

	/**
	 * Call with the top-level descriptor.
	 */
	private static Vector getChoosableColumns(Descriptor d) {
		Vector ret = new Vector();
		getChoosableColumns(d, ret);

		return ret;
	}

	private static void getChoosableColumns(Descriptor d, Vector v) {
		for (int i = 0, n = d.getColumnCount(); i < n; i++) {
			v.add(new FieldAccessor(d, i));
		}

		Descriptor[] kids = d.getChildren();
		for (int i = 0; i < kids.length; i++) {
			getChoosableColumns(kids[i], v);
		}
	}

	//
	// worker methods
	//

	private void dispatch() {
		final Dispatcher dispatcher = new Dispatcher();
		Thread t = new Thread(dispatcher);
		GenericProgressDialog progressDialog =
			new GenericProgressDialog(DataProfilerDialog.this,
									  "Progress",
									  new String[] {RECORDS_READ},
									  t);
		DataProfilerDialog.this.addPropertyChangeListener(progressDialog);
		t.start();
		progressDialog.setVisible(true);

		if (dispatcher.ex != null) {
			ErrorDialog.showErrorDialog(modelMaker, dispatcher.ex);
		}
	}

	private class Dispatcher implements Runnable {
		public Exception ex;
		public void run() {
			ImmutableProbabilityModel model = modelMaker.getProbabilityModel();

			fieldAccessor = (FieldAccessor)fieldBox.getSelectedItem();

			rs = null;
			try {
				if (openSourceRadio.isSelected()) {
					rs = getModelMakerSource();
				} else if (currentSelectionRadio.isSelected()) {
					rs = getModelMakerSelection();
				} else if (mrpsRadio.isSelected()) {
					String mrpsFile = mrpsField.getText().trim();
					rs = new MrpsToRsAdapter(MarkedRecordPairSourceXmlConf.getMarkedRecordPairSource(mrpsFile));
				} else {
					String rsFile = rsField.getText().trim();
					rs = RecordSourceXmlConf.getRecordSource(rsFile);
				}
				rs.setModel(model);

				processRecords(rs, fieldAccessor);
			} catch (Exception ex) {
				this.ex = ex;
				firePropertyChange(GenericProgressDialog.PN_ERROR, false, true);
			}
		}
	}

	private RecordSource getModelMakerSource() throws IllegalStateException {
		if (modelMaker.isEvaluated()) {
			List pairs = modelMaker.getSourceList();
			MarkedRecordPairSource mrps = MarkedRecordPairBinder.getMarkedRecordPairSource(pairs);
			return new MrpsToRsAdapter(mrps);
		} else if (!modelMaker.haveMarkedRecordPairSource()) {
			throw new IllegalStateException("There is no open MRPS!");
		} else {
			throw new IllegalStateException("Must evaluate MRPS in AbstractApplication!");
		}
	}

	private RecordSource getModelMakerSelection() throws IllegalStateException {
		if (!modelMaker.haveMarkedRecordPairSource()) {
			throw new IllegalStateException("There is no open MRPS!");
		}
		if (!modelMaker.isEvaluated()) {
			throw new IllegalStateException("Must evaluate MRPS in AbstractApplication!");
		}
		int[] selection = modelMaker.getSelection();
		if (selection == null || selection.length == 0) {
			throw new IllegalStateException("No pairs are selected!");
		}

		List sourceList = modelMaker.getSourceList();
		List sel = new ArrayList(selection.length);
		for (int i = 0; i < selection.length; i++) {
			sel.add(sourceList.get(selection[i]));
		}

		return new MrpsToRsAdapter(MarkedRecordPairBinder.getMarkedRecordPairSource(sel));
	}

	private FieldProfiler getProfiler(FieldAccessor fa) {
		Class propertyType = null;
		try {
			propertyType = FieldAccessor.getPropertyType(fa);
		} catch (IntrospectionException ex) {
			ex.printStackTrace();
		}

		if (propertyType == String.class ||
		    propertyType == char.class || propertyType == Character.class) {
			return new DefaultStringProfiler(fa);
		} else if (propertyType == Date.class) {
			return new DefaultDateProfiler(fa);
		} else if (propertyType == int.class || propertyType == Integer.class ||
				   propertyType == byte.class || propertyType == Byte.class ||
				   propertyType == short.class || propertyType == Short.class) {
			return new DefaultIntProfiler(fa);
		} else if (propertyType == long.class || propertyType == Long.class) {
			// TODO: combine this with int/Integer, etc.
		} else if (propertyType == float.class || propertyType == Float.class ||
				   propertyType == double.class || propertyType == Double.class) {
			// TODO: do a float/double profiler
		} else if (propertyType == boolean.class || propertyType == Boolean.class) {
			// TODO: do a very simple profiler which returns the pct valid
			// and the two-value histogram.
		} else {
			// NOTE: this is the default and will eventually only apply to Object
			// types that aren't String or Date.
			return new DefaultStringProfiler(fa);
		}

		return new DefaultStringProfiler(fa);
	}

	private void processRecords(RecordSource rs, FieldAccessor fa) throws IOException {
		fieldProfiler = getProfiler(fa);

		int num = 0;
		boolean interrupted = false;

		rs.open();
		while (!interrupted && rs.hasNext()) {
			fieldProfiler.processRecord(rs.getNext());

			num++;
			if (num % 100 == 0) {
				interrupted = Thread.interrupted();
			}
			if (num % 1000 == 0) {
				firePropertyChange(RECORDS_READ, -1, num);
			}
		}
		rs.close();

		firePropertyChange(RECORDS_READ, -1, num);

		updateScalarStatsTable(fieldProfiler);
		updateTabularStats(fieldProfiler);

		if (fieldProfiler instanceof DefaultStringProfiler) {
			createFilesButton.setEnabled(true);
		} else {
			createFilesButton.setEnabled(false);
		}

		firePropertyChange(GenericProgressDialog.PN_DONE, false, true);
	}

	private void updateScalarStatsTable(FieldProfiler profiler) {
		int numProps = profiler.getScalarStatCount();

		Object[][] data = new Object[numProps][2];
		for (int i = 0; i < numProps; i++) {
			data[i][0] = profiler.getScalarStatName(i);
			data[i][1] = profiler.getScalarStatValue(i);
		}

		Object[] headers = new Object[] {"Property", "Value"};

		scalarStatsTable.setModel(new SimpleSortableTableModel(data, headers));
		scalarStatsTable.setSelectionModel(new DefaultListSelectionModel());
		new ScalarTableHandler(scalarStatsTable, profiler);
	}

	private void updateTabularStats(FieldProfiler profiler) {
		while (tabs.getTabCount() > 1) {
			tabs.removeTabAt(tabs.getTabCount() - 1);
		}

		for (int i = 0; i < profiler.getTabularStatCount(); i++) {
			String title = profiler.getTabularStatName(i);
			Object[] headers = profiler.getTabularStatColumnHeaders(i);
			Object[][] data = profiler.getTabularStatTableData(i);
			SimpleSortableTableModel model = new SimpleSortableTableModel(data, headers);
			SimpleSortableTable table = new SimpleSortableTable(model);
			table.setDefaultRenderer(Float.class, DecimalToPctTableCellRenderer.INSTANCE);
			table.setDefaultRenderer(Double.class, DoubleTableCellRenderer.INSTANCE);
			new TableHandler(table, profiler, i);
			tabs.addTab(title, new JScrollPane(table));
		}
	}

	private class ComputeAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		public ComputeAction() {
			super("Compute");
		}
		public void actionPerformed(ActionEvent e) {
			dispatch();
		}
	}

	private class CloseAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		public CloseAction() {
			super("Close");
		}
		public void actionPerformed(ActionEvent e) {
			dispose();
		}
	}

	private class MrpsBrowseAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		public MrpsBrowseAction() {
			super("Browse");
		}
		public void actionPerformed(ActionEvent e) {
			File f = FileChooserFactory.selectMrpsFile(modelMaker);
			if (f != null) {
				mrpsField.setText(f.getAbsolutePath());
			}
		}
	}

	private class RsBrowseAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		public RsBrowseAction() {
			super("Browse");
		}
		public void actionPerformed(ActionEvent e) {
			File f = FileChooserFactory.selectRsFile(modelMaker);
			if (f != null) {
				rsField.setText(f.getAbsolutePath());
			}
		}
	}

	private class CreateFilesAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		public CreateFilesAction() {
			super("Create Files");
			setEnabled(false);
		}
		public void actionPerformed(ActionEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JDialog createFilesDialog = new CreateFilesDialog();
					createFilesDialog.setVisible(true);
				}
			});
		}
	}

	private class TextFileSelector extends FileSelector {
		public TextFileSelector(String label) {
			super(label);
		}
		protected File selectFile() {
			return FileChooserFactory.selectFlatFile(modelMaker);
		}
	}


	class ScalarTableHandler implements ListSelectionListener {

		private JTable table;
		private FieldProfiler profiler;

		public ScalarTableHandler(JTable table, FieldProfiler fp) {
			this.table = table;
			this.profiler = fp;

			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.getSelectionModel().addListSelectionListener(this);
		}

		public void valueChanged(ListSelectionEvent e) {
			if (!(rs instanceof MrpsToRsAdapter)) {
				return;
			}

			MrpsToRsAdapter adapter = (MrpsToRsAdapter) rs;
			MarkedRecordPairSource mrps = adapter.getMarkedRecordPairSource();
			if (!(mrps instanceof MarkedRecordPairBinder)) {
				return;
			}

			List filtered = new ArrayList();

			int statIndex = table.getSelectedRow();
			if (statIndex >= 0) {
				try {
					mrps.open();
					while (mrps.hasNext()) {
						MutableMarkedRecordPair mrp = mrps.getNextMarkedRecordPair();

						boolean seen = false;

						int rows = fieldAccessor.getRowCount(mrp.getQueryRecord());
						for (int row = 0; row < rows; row++) {
							if (profiler.filterRecordForScalarStat(statIndex, mrp.getQueryRecord())) {
								filtered.add(mrp);
								seen = true;
								break;
							}
						}

						if (seen) {
							continue;
						}

						rows = fieldAccessor.getRowCount(mrp.getMatchRecord());
						for (int row = 0; row < rows; row++) {
							if (profiler.filterRecordForScalarStat(statIndex, mrp.getMatchRecord())) {
								filtered.add(mrp);
								seen = true;
								break;
							}
						}
					}
					mrps.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}

			modelMaker.setFilter(new ModelMakerCollectionMRPairFilter(modelMaker, filtered));
		}
	}

	class TableHandler implements ListSelectionListener {

		private JTable table;
		private FieldProfiler profiler;
		private int statIndex;

		public TableHandler(JTable table, FieldProfiler fp, int statIndex) {
			this.table = table;
			this.profiler = fp;
			this.statIndex = statIndex;

			table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			table.getSelectionModel().addListSelectionListener(this);
		}

		public void valueChanged(ListSelectionEvent e) {
			if (!(rs instanceof MrpsToRsAdapter)) {
				return;
			}

			MrpsToRsAdapter adapter = (MrpsToRsAdapter) rs;
			MarkedRecordPairSource mrps = adapter.getMarkedRecordPairSource();
			if (!(mrps instanceof MarkedRecordPairBinder)) {
				return;
			}

			Set values = new HashSet();
			int[] selected = table.getSelectedRows();
			for (int i = 0; i < selected.length; i++) {
				int row = selected[i];
				Object val = table.getValueAt(row, 0);
				values.add(val);
			}

			List filtered = new ArrayList();
			try {
				mrps.open();
				while (mrps.hasNext()) {
					MutableMarkedRecordPair mrp = mrps.getNextMarkedRecordPair();

					boolean seen = false;

					int rows = fieldAccessor.getRowCount(mrp.getQueryRecord());
					for (int row = 0; row < rows; row++) {
						if (profiler.filterRecordForTableStat(statIndex, values, mrp.getQueryRecord())) {
							filtered.add(mrp);
							seen = true;
							break;
						}
					}

					if (seen) {
						continue;
					}

					rows = fieldAccessor.getRowCount(mrp.getMatchRecord());
					for (int row = 0; row < rows; row++) {
						if (profiler.filterRecordForTableStat(statIndex, values, mrp.getMatchRecord())) {
							filtered.add(mrp);
							seen = true;
							break;
						}
					}
				}
				mrps.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			modelMaker.setFilter(new ModelMakerCollectionMRPairFilter(modelMaker, filtered));
		}
	}

	class CreateFilesDialog extends JDialog {

		private static final long serialVersionUID = 1L;
		private JRadioButton
			fieldFreqBuckets,
			fieldCounts,
			fieldValues,
			patternFreqBuckets,
			patternCounts,
			patternValues,
			tokenCounts,
			tokenIdf;
		private TextFileSelector tfs;
		private JButton cancel, create;

		public CreateFilesDialog() {
			super(modelMaker, "Create Files", true);
			createContent();
			createListeners();

			pack();
			setLocationRelativeTo(modelMaker);
		}

		private void createContent() {
			GridBagLayout layout = new GridBagLayout();
			layout.rowWeights = new double[] {1, 0, 0};
			layout.columnWeights = new double[] {0, 1, 0, 0};
			getContentPane().setLayout(layout);

			GridBagConstraints c = new GridBagConstraints();
			c.insets = new Insets(3, 3, 3, 3);
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.EAST;

			//

			c.gridy = 0;
			c.gridx = 0;
			c.gridwidth = 4;
			Insets oldInsets = c.insets;
			c.insets = new Insets(1, 20, 1, 5);
			fieldFreqBuckets = new JRadioButton("Field Log-Frequency Buckets", true);
			getContentPane().add(fieldFreqBuckets, c);

			c.gridy++;
			fieldCounts = new JRadioButton("Field Counts");
			getContentPane().add(fieldCounts, c);

			c.gridy++;
			fieldValues = new JRadioButton("Field Values");
			getContentPane().add(fieldValues, c);

			c.gridy++;
			patternFreqBuckets = new JRadioButton("Pattern Log-Frequency Buckets");
			getContentPane().add(patternFreqBuckets, c);

			c.gridy++;
			patternCounts = new JRadioButton("Pattern Counts");
			getContentPane().add(patternCounts, c);

			c.gridy++;
			patternValues = new JRadioButton("Patterns");
			getContentPane().add(patternValues, c);

			c.gridy++;
			tokenCounts = new JRadioButton("Token Counts");
			getContentPane().add(tokenCounts, c);

			c.gridy++;
			tokenIdf = new JRadioButton("Token IDF");
			getContentPane().add(tokenIdf, c);

			c.gridwidth = 1;
			c.insets = oldInsets;

			ButtonGroup bg = new ButtonGroup();
			bg.add(fieldFreqBuckets);
			bg.add(fieldCounts);
			bg.add(fieldValues);
			bg.add(patternFreqBuckets);
			bg.add(patternCounts);
			bg.add(patternValues);
			bg.add(tokenCounts);
			bg.add(tokenIdf);

			//

			c.gridy++;
			getContentPane().add(Box.createVerticalStrut(10), c);

			//

			c.gridy++;

			tfs = new TextFileSelector("Output File: ");

			c.gridx = 0;
			getContentPane().add(tfs.getLabel(), c);

			c.gridx = 1;
			c.gridwidth = 2;
			getContentPane().add(tfs.getTextField(), c);
			c.gridwidth = 1;

			c.gridx = 3;
			getContentPane().add(tfs.getBrowseButton(), c);

			//

			c.gridy++;

			c.gridx = 2;
			create = new JButton("Create");
			create.setEnabled(false);
			getContentPane().add(create, c);

			c.gridx = 3;
			cancel = new JButton("Cancel");
			getContentPane().add(cancel, c);
		}

		private void createListeners() {
			tfs.addDocumentListener(new DocumentListener() {
				public void changedUpdate(DocumentEvent e) {
					create.setEnabled(tfs.hasFile());
				}
				public void insertUpdate(DocumentEvent e) { changedUpdate(e); }
				public void removeUpdate(DocumentEvent e) { changedUpdate(e); }
			});

			create.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					File outputFile = tfs.getFile();
					if (outputFile == null) {
						return;
					}

					try {
						// TODO: un-hard-code the minCounts and numBuckets arguments.
						if (fieldFreqBuckets.isSelected()) {
							Object[][] data = fieldProfiler.getTabularStatTableData(0);
							createLogFreqFile(data, 0, 1, 3, 5, outputFile, "\r\n", "\r\n");
						} else if (fieldCounts.isSelected()) {
							Object[][] data = fieldProfiler.getTabularStatTableData(0);
							createCountsFile(data, 0, 1, 3, outputFile, "\r\n", "\r\n");
						} else if (fieldValues.isSelected()) {
							Object[][] data = fieldProfiler.getTabularStatTableData(0);
							createValuesFile(data, 0, 1, 3, outputFile, "\r\n");
						} else if (patternFreqBuckets.isSelected()) {
							Object[][] data = fieldProfiler.getTabularStatTableData(1);
							createLogFreqFile(data, 0, 1, 3, 5, outputFile, "\r\n", "\r\n");
						} else if (patternCounts.isSelected()) {
							Object[][] data = fieldProfiler.getTabularStatTableData(1);
							createCountsFile(data, 0, 1, 3, outputFile, "\r\n", "\r\n");
						} else if (patternValues.isSelected()) {
							Object[][] data = fieldProfiler.getTabularStatTableData(1);
							createValuesFile(data, 0, 1, 3, outputFile, "\r\n");
						} else if (tokenCounts.isSelected()) {
							Object[][] data = fieldProfiler.getTabularStatTableData(3);
							createCountsFile(data, 0, 1, 3, outputFile, "\r\n", "\r\n");
						} else if (tokenIdf.isSelected()) {
							Object[][] data = fieldProfiler.getTabularStatTableData(3);
							createIdfFile(data, 0, 1, 2, 10, outputFile, "\r\n", "\r\n");
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});

			cancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});

			addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					dispose();
				}
			});
		}
	}

	private static void createIdfFile(Object[][] data, int valueCol, int countCol, int idfCol, int minCount,
										 File output, String elementSep, String pairSep) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(output));
		for (int i = 0, n = data.length; i < n; i++) {
			Object val = data[i][valueCol];
			int count = ((Integer)data[i][countCol]).intValue();
			String idf = data[i][idfCol].toString();
			if (val != null && count >= minCount) {
				String sVal = val.toString();
				if (sVal.length() > 0) {
					bw.write(sVal + elementSep + idf + pairSep);
				}
			}
		}
		bw.close();
	}

	private static void createValuesFile(Object[][] data, int valueCol, int countCol, int minCount,
										 File output, String rowSep) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(output));
		for (int i = 0, n = data.length; i < n; i++) {
			Object val = data[i][valueCol];
			int count = ((Integer)data[i][countCol]).intValue();
			if (val != null && count >= minCount) {
				String sVal = val.toString();
				if (sVal.length() > 0) {
					bw.write(sVal + rowSep);
				}
			}
		}
		bw.close();
	}

	private static void createCountsFile(Object[][] data, int valueCol, int countCol, int minCount,
										 File output, String elementSep, String pairSep) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(output));
		for (int i = 0, n = data.length; i < n; i++) {
			Object val = data[i][valueCol];
			int count = ((Integer)data[i][countCol]).intValue();
			if (val != null && count >= minCount) {
				String sVal = val.toString();
				if (sVal.length() > 0) {
					bw.write(sVal + elementSep + count + pairSep);
				}
			}
		}
		bw.close();
	}

	private static void createLogFreqFile(Object[][] data, int valueCol, int countCol, int minCount, int numBuckets,
										  File output, String elementSep, String pairSep) throws IOException {
		LogFrequencyPartitioner lfp = new LogFrequencyPartitioner();
		for (int i = 0, n = data.length; i < n; i++) {
			Object val = data[i][valueCol];
			int count = ((Integer)data[i][countCol]).intValue();
			if (val != null && count >= minCount) {
				String sVal = val.toString();
				if (sVal.length() > 0) {
					lfp.addPair(sVal, count);
				}
			}
		}
		lfp.computeBoundaries(numBuckets);
		lfp.writeFile(output.getAbsolutePath());
	}

}
