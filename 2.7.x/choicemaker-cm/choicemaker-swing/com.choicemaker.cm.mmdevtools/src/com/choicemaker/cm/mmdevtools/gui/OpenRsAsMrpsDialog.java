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

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.Source;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.xmlconf.RecordSourceXmlConf;
import com.choicemaker.cm.gui.utils.dialogs.ErrorDialog;
import com.choicemaker.cm.gui.utils.dialogs.FileChooserFactory;
import com.choicemaker.cm.mmdevtools.io.RsToMrpsAdapter;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.dialogs.SourceTypeSelectorDialog;

public class OpenRsAsMrpsDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private ModelMaker modelMaker;
	private FileSelector rsSelector;
	private JButton newButton;
	private JButton ok, cancel;

	public OpenRsAsMrpsDialog(ModelMaker modelMaker) {
		super(modelMaker, "Open RS as MRPS", true);
		this.modelMaker = modelMaker;

		createContent();
		createListeners();

		pack();
		setLocationRelativeTo(modelMaker);
	}

	private void createContent() {
		rsSelector = new RsSelector("Record Source");

		ok = new JButton("OK");
		cancel = new JButton("Cancel");
		ok.setMinimumSize(cancel.getPreferredSize());
		ok.setEnabled(false);

		GridBagLayout layout = new GridBagLayout();
		layout.columnWeights = new double[] {0, 1, 0, 0, 0};
		getContentPane().setLayout(layout);

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(3, 5, 3, 5);
		c.fill = GridBagConstraints.HORIZONTAL;

		//

		c.gridy = 0;

		c.gridx = 0;
		c.gridwidth = 1;
		getContentPane().add(rsSelector.getLabel(), c);

		c.gridx = 1;
		c.gridwidth = 2;
		getContentPane().add(rsSelector.getTextField(), c);

		c.gridx = 3;
		c.gridwidth = 1;
		getContentPane().add(rsSelector.getBrowseButton(), c);

		c.gridx = 4;
		newButton = new JButton("New");
		getContentPane().add(newButton, c);

		//

		c.gridy++;

		c.gridx = 3;
		getContentPane().add(ok, c);

		c.gridx = 4;
		getContentPane().add(cancel, c);

	}

	private void createListeners() {
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
		rsSelector.addDocumentListener(dl);

		newButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Source s = new SourceTypeSelectorDialog(modelMaker, SourceTypeSelectorDialog.RS, false).define();
				if (s != null) {
					try {
						RecordSourceXmlConf.add((RecordSource) s);
						rsSelector.setFile(new File(s.getFileName()));
					} catch (XmlConfException ex) {
						showError(ex);
					}
				}
			}
		});

		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					String rsFileName = rsSelector.getFile().getAbsolutePath();
					RecordSource rs = RecordSourceXmlConf.getRecordSource(rsFileName);
					RsToMrpsAdapter adapter = new RsToMrpsAdapter(rs);
					modelMaker.setMultiSource(1, adapter);
					dispose();
				} catch (XmlConfException ex) {
					showError(ex);
				}
			}
		});

		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
	}

	private void showError(final Throwable ex) {
		Runnable runner = new Runnable() {
			public void run() {
				ErrorDialog.showErrorDialog(modelMaker, ex);
			}
		};
		SwingUtilities.invokeLater(runner);
	}

	private void updateEnabledness() {
		ok.setEnabled(rsSelector.hasFile());
	}

	class RsSelector extends FileSelector {
		public RsSelector(String label) {
			super(label);
		}
		protected File selectFile() {
			return FileChooserFactory.selectRsFile(modelMaker);
		}
	}

}
