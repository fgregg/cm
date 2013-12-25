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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.choicemaker.cm.compiler.impl.CompilerFactory;
import com.choicemaker.cm.core.ColumnDefinition;
import com.choicemaker.cm.core.Descriptor;
import com.choicemaker.cm.core.DescriptorCollection;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.RecordSink;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.compiler.ICompiler;
import com.choicemaker.cm.core.xmlconf.ProbabilityModelsXmlConf;
import com.choicemaker.cm.core.xmlconf.RecordSourceXmlConf;
import com.choicemaker.cm.gui.utils.dialogs.FileChooserFactory;
import com.choicemaker.cm.mmdevtools.util.DerivedFieldsComputer;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;

/**
 * @author Owner
 *
 */
public class DerivedFieldsComputerDialog extends JDialog {

	public static final int RS = -32;
	public static final int MRPS = -33;

	public static void showDerivedFieldsComputerDialog(ModelMaker modelMaker, int type) {
		new DerivedFieldsComputerDialog(modelMaker, type).show();
	}

	protected int type;
	protected ModelMaker modelMaker;
	
	protected FileSelector sourceSelector;
	protected FileSelector sourceModelSelector;
	protected FileSelector sinkSelector;
	protected FileSelector sinkModelSelector;

	protected JButton ok, cancel;

	public DerivedFieldsComputerDialog(ModelMaker modelMaker, int type) {
		super(modelMaker, "Derived Fields Computer", true);
		this.modelMaker = modelMaker;
		this.type = type;
		
		createContent();
		createListeners();
		
		pack();
		setLocationRelativeTo(modelMaker);
	}
	
	protected void computeDerivedFields() {
		Runnable doer = new Runnable() {
			public void run() {
				try {
					actuallyDoIt();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};
		Thread t = new Thread(doer);
		
		String[] names = {"No counter yet..."};
		String[] values = {"Please come back later"};
		GenericProgressDialog d = new GenericProgressDialog(modelMaker, "Computing Derived Fields", names, values, t);
		addPropertyChangeListener(d);
		t.start();
		d.show();
	}
	
	protected void actuallyDoIt() throws Exception {

		CompilerFactory factory = CompilerFactory.getInstance ();
		ICompiler compiler = factory.getDefaultCompiler();
		
		Writer statusOutput = new StringWriter();
		File f = sourceModelSelector.getFile();
		String modelName = f.getName();
		InputStream is = new FileInputStream(f);
		IProbabilityModel sourceModel = 
			ProbabilityModelsXmlConf.readModel(modelName,is,compiler,statusOutput);

		statusOutput = new StringWriter();
		f = sinkModelSelector.getFile();
		modelName = f.getName();
		is = new FileInputStream(f);
		IProbabilityModel sinkModel = 
		ProbabilityModelsXmlConf.readModel(modelName,is,compiler,statusOutput);

		RecordSource source = RecordSourceXmlConf.getRecordSource(sourceSelector.getFile().getAbsolutePath());
		RecordSource sinkSource = RecordSourceXmlConf.getRecordSource(sinkSelector.getFile().getAbsolutePath());
		
		source.setModel(sourceModel);
		sinkSource.setModel(sinkModel);
		
		String[][] fieldNames = getFieldNames(sinkModel);
			
		DerivedFieldsComputer fc = new DerivedFieldsComputer(source, sourceModel, (RecordSink)sinkSource.getSink(), sinkModel, fieldNames);
		fc.compute();		
	}
	
	private String[][] getFieldNames(ImmutableProbabilityModel model) {
		Descriptor d = model.getAccessor().getDescriptor();
		Descriptor[] ds = new DescriptorCollection(d).getDescriptors();
		
		String[][] fn = new String[ds.length][];
		for (int i = 0; i < fn.length; i++) {
			ColumnDefinition[] cd = ds[i].getColumnDefinitions();
			fn[i] = new String[cd.length];
			for (int j = 0; j < fn[i].length; j++) {
				fn[i][j] = cd[j].getFieldName();
			}
		}
		
		return fn;
	}

	protected void createContent() {
		if (type == RS) {
			sourceSelector = new RsFileSelector("Source:");
			sinkSelector = new RsFileSelector("Sink: ");
		} else if (type == MRPS) {
			sourceSelector = new MrpsFileSelector("Source:");
			sinkSelector = new MrpsFileSelector("Sink: ");
		}
		
		sourceModelSelector = new ModelFileSelector("Source Model:");
		sinkModelSelector = new ModelFileSelector("Sink Model: ");
		
		ok = new JButton("OK");
		cancel = new JButton("Cancel");
		
		GridBagLayout layout = new GridBagLayout();
		layout.columnWeights = new double[] {0, 0, 1, 0, 0};
		getContentPane().setLayout(layout);
		
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(3, 5, 3, 5);
		c.fill = GridBagConstraints.HORIZONTAL;
		
		//
		
		c.gridy = 0;
		c.gridx = 0;
		getContentPane().add(sourceSelector.getLabel(), c);
		
		c.gridx = 1;
		c.gridwidth = 3;
		getContentPane().add(sourceSelector.getTextField(), c);
		c.gridwidth = 1;
		
		c.gridx = 4;
		getContentPane().add(sourceSelector.getBrowseButton(), c);

		//
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		getContentPane().add(sourceModelSelector.getLabel(), c);
		
		c.gridx = 2;
		getContentPane().add(sourceModelSelector.getTextField(), c);
		
		c.gridx = 4;
		c.gridwidth = 1;
		getContentPane().add(sourceModelSelector.getBrowseButton(), c);

		//
		
		c.gridy++;
		c.gridx = 0;
		getContentPane().add(sinkSelector.getLabel(), c);
		
		c.gridx = 1;
		c.gridwidth = 3;
		getContentPane().add(sinkSelector.getTextField(), c);
		c.gridwidth = 1;
		
		c.gridx = 4;
		getContentPane().add(sinkSelector.getBrowseButton(), c);

		//
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		getContentPane().add(sinkModelSelector.getLabel(), c);
		
		c.gridx = 2;
		getContentPane().add(sinkModelSelector.getTextField(), c);
		
		c.gridx = 4;
		c.gridwidth = 1;
		getContentPane().add(sinkModelSelector.getBrowseButton(), c);
		
		//
		
		c.gridy++;
		c.gridx = 3;
		getContentPane().add(ok, c);
		
		c.gridx = 4;
		getContentPane().add(cancel, c);

	}
	
	protected void createListeners() {
		DocumentListener dl = new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				updateEnabledness();
			}
			public void insertUpdate(DocumentEvent e) {
				updateEnabledness();
			}
			public void removeUpdate(DocumentEvent e) {
				updateEnabledness();
			}
		};
		
		sourceSelector.addDocumentListener(dl);		
		sourceModelSelector.addDocumentListener(dl);		
		sinkSelector.addDocumentListener(dl);		
		sinkModelSelector.addDocumentListener(dl);
		
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				computeDerivedFields();
			}
		});
		
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
	}
	
	protected void updateEnabledness() {
		ok.setEnabled(sourceSelector.hasFile() &&
					  sourceModelSelector.hasFile() &&
					  sinkSelector.hasFile() &&
					  sinkModelSelector.hasFile());
	}
	
	class RsFileSelector extends FileSelector {
		public RsFileSelector(String label) {
			super(label);
		}
		public File selectFile() {
			return FileChooserFactory.selectRsFile(modelMaker);
		}
	}

	class MrpsFileSelector extends FileSelector {
		public MrpsFileSelector(String label) {
			super(label);
		}
		public File selectFile() {
			return FileChooserFactory.selectMrpsFile(modelMaker);
		}
	}
	
	class ModelFileSelector extends FileSelector {
		public ModelFileSelector(String label) {
			super(label);
		}
		public File selectFile() {
			return FileChooserFactory.selectModelFile(modelMaker);
		}
	}

}
