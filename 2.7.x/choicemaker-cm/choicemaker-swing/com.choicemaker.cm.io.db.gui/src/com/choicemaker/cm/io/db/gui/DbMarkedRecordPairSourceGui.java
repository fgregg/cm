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
package com.choicemaker.cm.io.db.gui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.choicemaker.cm.core.MarkedRecordPairSource;
import com.choicemaker.cm.core.util.ChoiceMakerCoreMessages;
import com.choicemaker.cm.gui.utils.JavaHelpUtils;
import com.choicemaker.cm.gui.utils.dialogs.FileChooserFactory;
import com.choicemaker.cm.io.db.base.DataSources;
import com.choicemaker.cm.io.db.base.DbMarkedRecordPairSource2;
import com.choicemaker.cm.io.db.base.util.DbMessageUtil;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.dialogs.MarkedRecordPairSourceGui;
import com.choicemaker.cm.modelmaker.gui.utils.Enable;
import com.choicemaker.cm.modelmaker.gui.utils.EnablednessGuard;
//import db.jdbc.pool.*;

/**
 * The MRPSGui associated the DbMarkedRecordPairSource2.
 * An objects of this class would be created by the
 * DbMarkedRecordPairSourceGuiFactory.  It is used
 * by the AbstractApplication so that users can easily configure
 * and build DbMarkedRecordPairSources.
 *
 * @author S. Yoakum-Stover
 * @version $Revision: 1.2 $ $Date: 2010/03/28 09:04:19 $
 */
public class DbMarkedRecordPairSourceGui extends MarkedRecordPairSourceGui implements Enable {
    private static final long serialVersionUID = 1L;
//	private static Logger logger = Logger.getLogger(DbMarkedRecordPairSourceGui.class);
//    private String name;
    private JLabel sourceFileNameLabel;
    private JLabel confLabel;
    private JTextField conf;
    private JLabel whereFieldLabel;
    private JTextArea whereField;
//    private String where;
    private JLabel dataSourceLabel;
    private JComboBox dataSource;

    public DbMarkedRecordPairSourceGui(ModelMaker parent, MarkedRecordPairSource s) {
        super(parent, DbMessageUtil.m.formatMessage("plugin.db.io.db.gui.label"));
        init(s);
    }

    /**
     * Executed by the superclass constructor to build the panel.
     */
    public void buildContent() {
        sourceFileNameLabel = new JLabel(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.dialog.source.name"));
        sourceFileName = new JTextField(35);
        sourceFileBrowseButton = new JButton(ChoiceMakerCoreMessages.m.formatMessage("browse.elipsis"));
        confLabel = new JLabel(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.dialog.source.conf"));
        conf = new JTextField(10);
        whereFieldLabel = new JLabel(DbMessageUtil.m.formatMessage("plugin.db.io.db.gui.select"));
        whereField = new JTextArea(4, 1);
        whereField.setFont(new Font("Monospaced", 0, 12));
        whereField.setLineWrap(true);
        whereField.setWrapStyleWord(true);
        whereField.setBorder(BorderFactory.createLoweredBevelBorder());
        dataSourceLabel = new JLabel(DbMessageUtil.m.formatMessage("plugin.db.io.db.gui.data.source"));
        dataSource = new JComboBox(getDataSources());
        okayButton = new JButton(ChoiceMakerCoreMessages.m.formatMessage("ok"));
        cancelButton = new JButton(ChoiceMakerCoreMessages.m.formatMessage("cancel"));

        layoutContent();
    }

    /**
     * Get the Db data sources from the config file.
     */
    private String[] getDataSources() {
        Collection coll = DataSources.getDataSourceNames();
        String[] availableDataSources = (String[]) coll.toArray(new String[coll.size()]);
        for (int i = 0; i < availableDataSources.length; ++i) {
            availableDataSources[i] = availableDataSources[i].intern();
        }
        return availableDataSources;
    }

    public void show() {
        setFields();
        setEnabledness();
        super.setVisible(true);
    }

    private void setFields() {
        if (source == null) {
            return;
        }
        sourceFileName.setText(source.getFileName());
        DbMarkedRecordPairSource2 s = (DbMarkedRecordPairSource2) source;

        conf.setText(s.getConf());
        whereField.setText(s.getSelection());
        String dsn = s.getDataSourceName();
        dataSource.setSelectedItem(dsn == null ? null : dsn.intern());
    }

    public void setEnabledness() {
        okayButton.setEnabled(
            sourceFileName.getText().length() > 0
                && whereField.getText().length() > 0
                && dataSource.getSelectedItem() != null);
    }

    public void addContentListeners() {
        super.addContentListeners();

        EnablednessGuard dl = new EnablednessGuard(this);
        sourceFileName.getDocument().addDocumentListener(dl);
        whereField.getDocument().addDocumentListener(dl);
        dataSource.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                setEnabledness();
            }
        });

		sourceFileBrowseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File file = FileChooserFactory.selectMrpsFile(getParent());
				if (file != null) {
					sourceFileName.setText(file.getAbsolutePath());
				}
			}
		});

		JavaHelpUtils.enableHelpKey(this, "io.gui.oracle.mrps");
    }

    private void layoutContent() {
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        content.setLayout(layout);
        c.insets = new Insets(2, 2, 5, 10);

        //Row 0...................................
        //sourceFileNameLabel
        c.gridy = 0;
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        layout.setConstraints(sourceFileNameLabel, c);
        content.add(sourceFileNameLabel);
        //sourceFileName
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        layout.setConstraints(sourceFileName, c);
        content.add(sourceFileName);
        c.gridx = 2;
        c.weightx = 0;
        layout.setConstraints(sourceFileBrowseButton, c);
        content.add(sourceFileBrowseButton);

        //Row 1...................................
        //sourceFileNameLabel
        c.gridy = 1;
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        layout.setConstraints(confLabel, c);
        content.add(confLabel);
        //sourceFileName
        c.gridx = 1;
        c.weightx = 1;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        layout.setConstraints(conf, c);
        content.add(conf);

        //Row 2...................................
        //dataSourceLabel
        c.gridy = 2;
        c.gridx = 0;
        c.weightx = 0;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        layout.setConstraints(dataSourceLabel, c);
        content.add(dataSourceLabel);
        //dataSource
        c.gridx = 1;
        c.weightx = 1;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        layout.setConstraints(dataSource, c);
        content.add(dataSource);

        //Row 3...................................
        //whereFieldLabel
        c.gridy = 3;
        c.gridx = 0;
        c.weightx = 0;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        layout.setConstraints(whereFieldLabel, c);
        content.add(whereFieldLabel);
        //whereField
        c.gridx = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        layout.setConstraints(whereField, c);
        content.add(whereField);

        JPanel buttons = new JPanel(new GridLayout(1, 4, 10, 10));
        buttons.add(okayButton);
        buttons.add(cancelButton);
        c.gridy = 4;
        c.gridx = 0;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.EAST;
        content.add(buttons, c);
    }

    /**
     * Builds an DbMarkedRecordPairSource2.
     */
    public void buildSource() {
        DbMarkedRecordPairSource2 dbSource = (DbMarkedRecordPairSource2) source;

        dbSource.setDataSourceName((String) dataSource.getSelectedItem());
        dbSource.setFileName(getSourceFileName());
        dbSource.setConf(conf.getText());
        dbSource.setSelection(whereField.getText());
    }
}
