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
package com.choicemaker.cm.io.composite.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.MarkedRecordPairSource;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.util.LoggingObject;
import com.choicemaker.cm.core.util.ChoiceMakerCoreMessages;
import com.choicemaker.cm.core.xmlconf.MarkedRecordPairSourceXmlConf;
import com.choicemaker.cm.gui.utils.JavaHelpUtils;
import com.choicemaker.cm.gui.utils.dialogs.FileChooserFactory;
import com.choicemaker.cm.io.composite.base.CompositeMarkedRecordPairSource;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.dialogs.MarkedRecordPairSourceGui;
import com.choicemaker.cm.modelmaker.gui.utils.Enable;
import com.choicemaker.cm.modelmaker.gui.utils.EnablednessGuard;

/**
 * The MRPSGui associated the CompositeMarkedRecordPairSource.
 * An objects of this class would be created by the
 * CompositeMarkedRecordPairSourceFactory.  It is used
 * by the AbstractApplication so that users can easily configure
 * and build CompositeMarkedRecordPairSource.
 *
 * @author S. Yoakum-Stover
 * @version $Revision: 1.2 $ $Date: 2010/03/28 08:56:49 $
 */
public class CompositeMarkedRecordPairSourceGui extends MarkedRecordPairSourceGui implements Enable {
    private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(CompositeMarkedRecordPairSourceGui.class.getName());
	private static final String RELATIVE = ChoiceMakerCoreMessages.m.formatMessage("io.composite.gui.source.file.relative");
	private static final String ABSOLUTE = ChoiceMakerCoreMessages.m.formatMessage("io.composite.gui.source.file.absolute");
    private JLabel sourceNameLabel;
//    private JLabel fileNameLabel;
    private JButton addButton;
    private JButton removeButton;

    private JLabel sourcesTableLabel;
    private JTable sourcesTable;
    private JScrollPane sourcesTableScrollPane;

    public CompositeMarkedRecordPairSourceGui(ModelMaker parent, MarkedRecordPairSource s) {
        super(parent, ChoiceMakerCoreMessages.m.formatMessage("io.composite.gui.label"));
        init(s);
    }

    public void show() {
        setFields();
        setEnabledness();
        super.setVisible(true);
    }

    public void setFields() {
        if (source == null) {
            return;
        }
        CompositeMarkedRecordPairSource compSource = (CompositeMarkedRecordPairSource) source;
        sourceFileName.setText(compSource.getFileName());
        DefaultTableModel m = (DefaultTableModel) sourcesTable.getModel();
        int c = compSource.getNumSources();
        for (int i = 0; i < c; ++i) {
			Object[] row = new Object[2];
			row[0] = compSource.getSource(i).getFileName();
			row[1] = compSource.saveAsRelative(i) ? RELATIVE : ABSOLUTE;
			m.addRow(row);
        }
    }

    public void setEnabledness() {
        okayButton.setEnabled(sourceFileName.getText().length() > 0 && sourcesTable.getRowCount() > 0);
    }

    public void buildSource() {
        CompositeMarkedRecordPairSource compSource = (CompositeMarkedRecordPairSource) source;
        compSource.removeAll();
        compSource.setFileName(getSourceFileName());
        for (int i = 0; i < sourcesTable.getRowCount(); i++) {
        	String conFile = (String)sourcesTable.getValueAt(i, 0);
            boolean saveAsRel = sourcesTable.getValueAt(i, 1).equals(RELATIVE);
            try {
                MarkedRecordPairSource s = MarkedRecordPairSourceXmlConf.getMarkedRecordPairSource(conFile);
                logger.debug("Adding source: " + s.getFileName());
                compSource.add(s, saveAsRel);
            } catch (XmlConfException ex) {
				logger.error(new LoggingObject("CM-020001", conFile), ex);
            }
        }
    }

    /**
     * Executed by the superclass constructor to build the panel.
     */
    public void buildContent() {
        sourceNameLabel = new JLabel(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.dialog.source.name"));
        sourceFileName = new JTextField(35);
        sourceFileBrowseButton = new JButton(ChoiceMakerCoreMessages.m.formatMessage("browse.elipsis"));

        sourcesTableLabel = new JLabel(ChoiceMakerCoreMessages.m.formatMessage("io.composite.gui.constituting"));

        Object[] colNames = { ChoiceMakerCoreMessages.m.formatMessage("io.composite.gui.column0name"),
        					  ChoiceMakerCoreMessages.m.formatMessage("io.composite.gui.column1name") };
        DefaultTableModel model = new DefaultTableModel() {
        	private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int row, int column) {
        		return column == 1;
        	}
        };
        model.setDataVector(new Object[0][], colNames);
        sourcesTable = new JTable(model);
        sourcesTable.getColumnModel().getColumn(0).setMinWidth(280);
		sourcesTable.getColumnModel().getColumn(1).setMinWidth(100);
        sourcesTable.setColumnSelectionAllowed(false);
        sourcesTable.setShowGrid(false);
        sourcesTable.setRowHeight(20);
		JComboBox relBox = new JComboBox();
		relBox.addItem(RELATIVE);
		relBox.addItem(ABSOLUTE);
		sourcesTable.setDefaultEditor(Object.class, new DefaultCellEditor(relBox));

        sourcesTableScrollPane = new JScrollPane(sourcesTable);
        sourcesTableScrollPane.setMinimumSize(new Dimension(400, 150));
        sourcesTableScrollPane.setPreferredSize(new Dimension(400, 150));

        addButton = new JButton("Add...");
        removeButton = new JButton("Remove");

        okayButton = new JButton(ChoiceMakerCoreMessages.m.formatMessage("ok"));
        cancelButton = new JButton(ChoiceMakerCoreMessages.m.formatMessage("cancel"));

        layoutContent();
    }

    public void addContentListeners() {
        super.addContentListeners();

		//sourceFileBrowseButton
		sourceFileBrowseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				File f = FileChooserFactory.selectMrpsFile(parent);
				if (f != null) {
					sourceFileName.setText(f.getAbsolutePath());
				}
			}
		});

        // removeButton
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                DefaultTableModel m = (DefaultTableModel) sourcesTable.getModel();
                int[] si = sourcesTable.getSelectedRows();
                for (int i = si.length - 1; i >= 0; i--) {
                    m.removeRow(si[i]);
                }
                setEnabledness();
            }
        });

        //addButton
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
				DefaultTableModel m = (DefaultTableModel) sourcesTable.getModel();
				File[] fs = FileChooserFactory.selectMrpsFiles(parent);
                for (int i = 0; i < fs.length; i++) {
                	Object[] row = new Object[] { fs[i].getAbsolutePath(), RELATIVE };
                    m.addRow(row);
                }
                setEnabledness();
            }
        });

        EnablednessGuard guard = new EnablednessGuard(this);
        sourceFileName.getDocument().addDocumentListener(guard);

		JavaHelpUtils.enableHelpKey(this, "io.gui.composite.mrps");
    }

    private void layoutContent() {
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 5, 5, 10);
        content.setLayout(layout);

        //row 0........................................
        c.gridy = 0;
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        content.add(sourceNameLabel, c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        content.add(sourceFileName, c);
        c.weightx = 0;
        c.gridx = 2;
        content.add(sourceFileBrowseButton, c);

        //row 1........................................
        c.gridy = 1;
        c.gridx = 0;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        content.add(sourcesTableLabel, c);

        //row 2........................................
        c.gridy = 2;
        c.gridx = 0;
        c.gridwidth = 3;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        content.add(sourcesTableScrollPane, c);

        //row 3........................................
        JPanel buttons = new JPanel(new GridLayout(2, 2, 10, 10));
        buttons.add(addButton);
        buttons.add(removeButton);
        buttons.add(okayButton);
        buttons.add(cancelButton);

        c.gridy = 3;
        c.gridx = 0;
        c.gridheight = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.EAST;
        content.add(buttons, c);
    }
}
