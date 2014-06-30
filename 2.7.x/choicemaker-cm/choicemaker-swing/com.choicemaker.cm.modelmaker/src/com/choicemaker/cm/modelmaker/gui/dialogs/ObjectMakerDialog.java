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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

import com.choicemaker.cm.compiler.util.ProductionModelsJarBuilder;
import com.choicemaker.cm.core.base.Constants;
import com.choicemaker.cm.core.util.ObjectMaker;
import com.choicemaker.cm.gui.utils.dialogs.FileChooserFactory;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.utils.Enable;
import com.choicemaker.cm.modelmaker.gui.utils.EnablednessGuard;
import com.choicemaker.cm.modelmaker.gui.utils.ThreadWatcher;

/**
 *
 * @author    Adam Winkel
 * @version   
 */
public class ObjectMakerDialog extends JDialog implements Enable {
	
	private static final long serialVersionUID = 1L;

	private ModelMaker modelMaker;
	
	private JTextField dirField;
	private JButton dirBrowse;
	
	private ObjectMaker[] objectMakers;
	private String[] descriptions;
	private Boolean[] defaults;

	private JCheckBox[] boxes;
	private JButton ok, cancel;
	
	public ObjectMakerDialog(ModelMaker modelMaker) {
		super(modelMaker, "Holder Classes Jar and DB Objects Dialog", true);
		this.modelMaker = modelMaker;
	
		getPlugins();
		
		createContent();
		createListeners();

		setEnabledness();

		pack();
		setLocationRelativeTo(modelMaker);
	}
	
	public File getOutDir() {
		if (dirField.getText().trim().length() == 0) {
			return null;
		} else {
			return new File(dirField.getText().trim()).getAbsoluteFile();
		}
	}
	
	public void setEnabledness() {
		File outDir = getOutDir();
		if (outDir == null || outDir.isFile()) {
			ok.setEnabled(false);
			return;
		}
		
		for (int i = 0; i < boxes.length; i++) {
			if (boxes[i].isSelected()) {
				ok.setEnabled(true);
				return;
			}
		}
		
		ok.setEnabled(false);
	}
	
	private void generateObjects() {		
		final Exception[] thrown = new Exception[1];
		final Thread t = new Thread() {
			public void run() {
				try {
					ProductionModelsJarBuilder.refreshProductionProbabilityModels();
				} catch (Exception ex) {
					thrown[0] = ex;
					return;
				}

				final File outDir = getOutDir();
				if (!outDir.isDirectory()) {
					outDir.mkdirs();
				}

				for (int i = 0; i < boxes.length; i++) {
					if (currentThread().isInterrupted()) {
						return;
					}
					if (boxes[i].isSelected()) {
						try {
							objectMakers[i].generateObjects(outDir);	
						} catch (Exception ex) {
							thrown[0] = ex;
							return;
						}
					}
				}
			}
		};

		hide();
		
		boolean interrupted =
			ThreadWatcher.watchThread(
				t,
				modelMaker,
				"Please Wait",
				"Generating Objects");
		
		if (thrown[0] != null) {
			Logger.getLogger(ObjectMakerDialog.class).error("Problem creating Holder classes and DB objects", thrown[0]);
			dispose();
		} else if (interrupted) {
			show();
		} else {
			final JDialog d = new JDialog(modelMaker, "Status", true);
			d.getContentPane().setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.insets = new Insets(10, 10, 10, 10);
			c.gridx = 0;
			c.gridy = 0;
			d.getContentPane().add(new JLabel("Object Generation Complete!"), c);
			JButton dOk = new JButton("OK");
			c.gridy = 1;
			d.getContentPane().add(dOk, c);
			dOk.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					d.dispose();
				}
			});
			d.pack();
			d.setLocationRelativeTo(modelMaker);
			d.show();
			dispose();
		}
	}
	
	private void getPlugins() {
		ArrayList makers = new ArrayList();
		ArrayList descs = new ArrayList();
		ArrayList defs = new ArrayList();
		
		IExtensionPoint pt = Platform.getPluginRegistry().getExtensionPoint("com.choicemaker.cm.core.base.objectGenerator");
		IExtension[] extensions = pt.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			IExtension extension = extensions[i];
			IConfigurationElement[] els = extension.getConfigurationElements();
			for (int j = 0; j < els.length; j++) {
				IConfigurationElement element = els[j];
				try {
					makers.add(element.createExecutableExtension("class"));
					descs.add(element.getAttribute("description"));
					defs.add(new Boolean("true".equals(element.getAttribute("default"))));
				} catch (CoreException ex) {
					ex.printStackTrace();
				}
			}
		}
		
		objectMakers = (ObjectMaker[])makers.toArray(new ObjectMaker[makers.size()]);
		descriptions = (String[])descs.toArray(new String[descs.size()]);
		defaults = (Boolean[])defs.toArray(new Boolean[defs.size()]);
	}
	
	private void createContent() {
		GridBagLayout layout = new GridBagLayout();
		layout.columnWeights = new double[]{0, 1, 0, 0};
		getContentPane().setLayout(layout);
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(3, 3, 3, 3);
		c.weighty = 0;
		
		//
		
		c.gridy = 0;
		
		c.gridx = 0;
		getContentPane().add(new JLabel("Output Directory: "), c);
		
		c.gridx = 1;
		c.gridwidth = 2;
		dirField = new JTextField(35);
		dirField.setText(new File(Constants.MODELS_DIRECTORY, "gen/out").getAbsolutePath());
		getContentPane().add(dirField, c);
		c.gridwidth = 1;
		
		c.gridx = 3;
		dirBrowse = new JButton("Browse");
		getContentPane().add(dirBrowse, c);
		
		//
		
		c.gridx = 1;
		c.gridwidth = 3;
		c.anchor = GridBagConstraints.WEST;
		
		boxes = new JCheckBox[descriptions.length];
		for (int i = 0; i < boxes.length; i++) {
			c.gridy++;

			boxes[i] = new JCheckBox(descriptions[i]);
			if (defaults[i].booleanValue()) {
				boxes[i].setSelected(true);
			}
			
			getContentPane().add(boxes[i], c);
		}
		
		//
		
		c.gridy++;
		
		c.gridx = 2;
		c.gridwidth = 1;
		ok = new JButton("OK");
		ok.setEnabled(false);
		getContentPane().add(ok, c);
		
		c.gridx = 3;
		cancel = new JButton("Cancel");
		getContentPane().add(cancel, c);
		
	}
	
	private void createListeners() {
		EnablednessGuard dl = new EnablednessGuard(this);
		dirField.getDocument().addDocumentListener(dl);
		
		dirBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File dir = FileChooserFactory.selectDirectory(ObjectMakerDialog.this, new File(dirField.getText()));
				if (dir != null) {
					dirField.setText(dir.getAbsolutePath());
				}
			}
		});
		
		ChangeListener boxListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setEnabledness();
			}
		};
		for (int i = 0; i < boxes.length; i++) {
			boxes[i].addChangeListener(boxListener);
		}
		
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				generateObjects();
			}
		});

		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
	}
	
}
