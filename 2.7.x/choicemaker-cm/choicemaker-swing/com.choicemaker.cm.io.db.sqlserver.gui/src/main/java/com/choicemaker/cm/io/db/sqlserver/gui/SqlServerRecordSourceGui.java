/*
 * @(#)$RCSfile: SqlServerRecordSourceGui.java,v $        $Revision: 1.1.96.1 $ $Date: 2009/11/18 01:00:11 $
 *
 * Copyright (c) 2001 ChoiceMaker Technologies, Inc.
 * 41 East 11th Street, New York, NY 10003
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * ChoiceMaker Technologies Inc. ("Confidential Information").
 */
package com.choicemaker.cm.io.db.sqlserver.gui;

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

import javax.sql.DataSource;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.util.ChoiceMakerCoreMessages;
import com.choicemaker.cm.gui.utils.dialogs.FileChooserFactory;
import com.choicemaker.cm.io.db.base.DataSources;
import com.choicemaker.cm.io.db.base.DbAccessor;
import com.choicemaker.cm.io.db.base.util.DbMessageUtil;
import com.choicemaker.cm.io.db.sqlserver.SqlServerRecordSource;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.dialogs.RecordSourceGui;
import com.choicemaker.cm.modelmaker.gui.utils.Enable;
import com.choicemaker.cm.modelmaker.gui.utils.EnablednessGuard;

/**
 * @version $Revision: 1.1.96.1 $ $Date: 2009/11/18 01:00:11 $
 */
public class SqlServerRecordSourceGui extends RecordSourceGui implements Enable {

	private static final long serialVersionUID = 271L;

	private JLabel sourceFileLabel;
    // source file text field and browse button are defined in RecordSourceGui (uggghhh!!!)
	private JLabel dataSourceLabel;
	private JComboBox dataSource;
    private JLabel dbConfigurationLabel;
    private JComboBox dbConfiguration;
    private JLabel idsQueryLabel;
    private JTextArea idsQuery;

    public SqlServerRecordSourceGui(ModelMaker parent, RecordSource s) {
        super(parent, "SQL Server Record Source");
        init(s);
    }

	public void setVisible(boolean b) {
		if (b) {
			setFields();
			setEnabledness();
			super.setVisible(b);
		}
	}

	public void setEnabledness() {
		okayButton.setEnabled(
			sourceFileName.getText().length() > 0
				&& idsQuery.getText().length() > 0
				&& dataSource.getSelectedItem() != null);
	}

	public void buildSource() {
		SqlServerRecordSource dbSource = (SqlServerRecordSource) source;

		dbSource.setFileName(getSourceFileName());
		
		String dsName = (String) dataSource.getSelectedItem();
		DataSource ds = DataSources.getDataSource(dsName);
		dbSource.setDataSource(dsName, ds);
		
		dbSource.setDbConfiguration((String)dbConfiguration.getSelectedItem());
		dbSource.setIdsQuery(idsQuery.getText());
	}

    private String[] getDataSources() {
        Collection coll = DataSources.getDataSourceNames();
        String[] availableDataSources = (String[]) coll.toArray(new String[coll.size()]);
        for (int i = 0; i < availableDataSources.length; ++i) {
            availableDataSources[i] = availableDataSources[i].intern();
        }
        return availableDataSources;
    }
    
    private String[] getDbConfigurations() {
    	ImmutableProbabilityModel model = parent.getProbabilityModel();
    	if (model != null) {
    		DbAccessor acc = (DbAccessor) model.getAccessor();
    		return acc.getDbConfigurations();
    	} else {
    		return new String[0];
    	}
    }

    private void setFields() {
        if (source != null) {
	        sourceFileName.setText(source.getFileName());
	        SqlServerRecordSource s = (SqlServerRecordSource) source;

			String dsn = s.getDataSourceName();
			if (dsn != null) {
				dataSource.setSelectedItem(dsn);
			}

			String dbc = s.getDbConfiguration();
			if (dbc != null) {
				dbConfiguration.setSelectedItem(dbc);
			}

			idsQuery.setText(s.getIdsQuery());
        }
    }

	public void buildContent() {
		sourceFileLabel = new JLabel(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.dialog.source.name"));
		sourceFileName = new JTextField(35);
		sourceFileBrowseButton = new JButton(ChoiceMakerCoreMessages.m.formatMessage("browse.elipsis"));

		dataSourceLabel = new JLabel(DbMessageUtil.m.formatMessage("plugin.db.io.db.gui.data.source"));
		dataSource = new JComboBox(getDataSources());

		dbConfigurationLabel = new JLabel(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.dialog.source.conf"));
		dbConfiguration = new JComboBox(getDbConfigurations());

		idsQueryLabel = new JLabel(DbMessageUtil.m.formatMessage("plugin.db.io.db.gui.select"));
		idsQuery = new JTextArea(4, 1);
		idsQuery.setFont(new Font("Monospaced", 0, 12));
		idsQuery.setLineWrap(true);
		idsQuery.setWrapStyleWord(true);
		idsQuery.setBorder(BorderFactory.createLoweredBevelBorder());

		okayButton = new JButton(ChoiceMakerCoreMessages.m.formatMessage("ok"));
		cancelButton = new JButton(ChoiceMakerCoreMessages.m.formatMessage("cancel"));

		layoutContent();
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
        layout.setConstraints(sourceFileLabel, c);
        content.add(sourceFileLabel);
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

		// ...................................
		//dataSourceLabel
		c.gridy++;
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

        //Row 1...................................
        c.gridy++;
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        layout.setConstraints(dbConfigurationLabel, c);
        content.add(dbConfigurationLabel);

        c.gridx = 1;
        c.weightx = 1;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        layout.setConstraints(dbConfiguration, c);
        content.add(dbConfiguration);

        //Row 3...................................
        //whereFieldLabel
        c.gridy = 3;
        c.gridx = 0;
        c.weightx = 0;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        layout.setConstraints(idsQueryLabel, c);
        content.add(idsQueryLabel);
        //whereField
        c.gridx = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        layout.setConstraints(idsQuery, c);
        content.add(idsQuery);

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

	protected void addContentListeners() {
		super.addContentListeners();
        
		EnablednessGuard dl = new EnablednessGuard(this);
		sourceFileName.getDocument().addDocumentListener(dl);
		idsQuery.getDocument().addDocumentListener(dl);
		dataSource.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				setEnabledness();
			}
		});

		sourceFileBrowseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File file = FileChooserFactory.selectRsFile(getParent());
				if (file != null) {
					sourceFileName.setText(file.getAbsolutePath());
				}
			}
		});
	}

}
