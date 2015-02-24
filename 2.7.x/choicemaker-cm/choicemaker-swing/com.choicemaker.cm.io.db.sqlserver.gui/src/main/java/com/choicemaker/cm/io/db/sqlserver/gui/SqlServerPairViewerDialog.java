/*
 * Created on Jan 9, 2004
 *
 */
package com.choicemaker.cm.io.db.sqlserver.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.NoSuchElementException;

import javax.sql.DataSource;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.MarkedRecordPairSource;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.base.MarkedRecordPairBinder;
import com.choicemaker.cm.core.base.MutableMarkedRecordPair;
import com.choicemaker.cm.gui.utils.dialogs.ErrorDialog;
import com.choicemaker.cm.io.db.base.DataSources;
import com.choicemaker.cm.io.db.sqlserver.SqlServerXmlUtils;
import com.choicemaker.cm.io.db.sqlserver.dbom.SqlDbObjectMaker;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;

/**
 * @author ajwinkel
 *
 */
public class SqlServerPairViewerDialog extends JDialog {

	private static final long serialVersionUID = 271L;

	private static SqlServerPairViewerDialog dialog;

	public static void showDialog(ModelMaker modelMaker) {
		if (dialog == null) {
			dialog = new SqlServerPairViewerDialog(modelMaker);
		}
		
		dialog.show();
	}
	
	private ModelMaker modelMaker;
	private ImmutableProbabilityModel model;

	private JComboBox dataSource;
	private JComboBox dbConfiguration;
	private JTextField qId, mId;
	private JButton ok, cancel;

	private SqlServerPairViewerDialog(ModelMaker modelMaker) {
		super(modelMaker, "SQL Server Record Pair Selector", false);
		this.modelMaker = modelMaker;
		this.model = modelMaker.getProbabilityModel();
		
		SqlServerUtils.maybeInitConnectionPools();
		
		createContent();
		createListeners();
		updateEnabledness();
		
		pack();
		setLocationRelativeTo(modelMaker);
	}
	
	private void createContent() {
		GridBagLayout layout = new GridBagLayout();
		layout.columnWeights = new double[] {0, 1, 0, 0};
		getContentPane().setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 4, 2, 4);
		c.fill = GridBagConstraints.HORIZONTAL;
		
		//
		
		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 1;
		getContentPane().add(new JLabel("Data Source: "), c);
		
		c.gridx = 1;
		c.gridwidth = 3;
		dataSource = SqlServerUtils.createDataSource();
		getContentPane().add(dataSource, c);
		
		//
		
		c.gridy++;
		
		c.gridx = 0;
		c.gridwidth = 1;
		getContentPane().add(new JLabel("DB Configuration: "), c);
		
		c.gridx = 1;
		c.gridwidth = 3;
		dbConfiguration = SqlServerUtils.createDbConfigurationsBox(model);
		getContentPane().add(dbConfiguration, c);
		
		//
		
		c.gridy++;
		
		c.gridx = 0;
		c.gridwidth = 1;
		getContentPane().add(new JLabel("Q id:"), c);
		
		c.gridx = 1;
		c.gridwidth = 3;
		qId = new JTextField(10);
		getContentPane().add(qId, c);
		
		//
		
		c.gridy++;
		
		c.gridx = 0;
		c.gridwidth = 1;
		getContentPane().add(new JLabel("M id:"), c);
		
		c.gridx = 1;
		c.gridwidth = 3;
		mId = new JTextField(10);
		getContentPane().add(mId, c);
		
		//
		
		c.gridy++;
		
		c.gridx = 2;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		ok = new JButton("OK");
		getContentPane().add(ok, c);
		
		c.gridx = 3;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		cancel = new JButton("Cancel");
		getContentPane().add(cancel, c);
	}
		
	private void createListeners() {
		ItemListener il = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				updateEnabledness();
			}
		};
		
		dataSource.addItemListener(il);
		dbConfiguration.addItemListener(il);
		
		DocumentListener dl = new DocumentListener() {
			public void insertUpdate(DocumentEvent e) { updateEnabledness(); }
			public void removeUpdate(DocumentEvent e) { updateEnabledness(); }
			public void changedUpdate(DocumentEvent e) { updateEnabledness(); }
		};
		
		qId.getDocument().addDocumentListener(dl);
		mId.getDocument().addDocumentListener(dl);
		
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getPair();
			}
		});
	}

	private void getPair() {		
		SqlServerUtils.setWaitCursor(modelMaker, this);

		DataSource ds = getDataSource();
		String dbConfiguration = getDbConfiguration();
		model.properties().put(SqlServerXmlUtils.PN_DB_CONFIGURATION, dbConfiguration);
		model.properties().put(SqlDbObjectMaker.getMultiKey(model, dbConfiguration),
							 SqlDbObjectMaker.getMultiQuery(model, dbConfiguration));
				
		String qId = getQId(), mId = getMId();
		Record q = null, m = null;

		String errorMsg = null;
		Exception ex = null;
		try {
			q = SqlServerUtils.readRecord(model, ds, qId);
			m = SqlServerUtils.readRecord(model, ds, mId);
		} catch (IOException ex2) {
			errorMsg = "Error reading record " + (q == null ? qId : mId);
			ex = ex2;
		} catch (NoSuchElementException ex2) {
			errorMsg = "No record with id " + (q == null ? qId : mId);
			ex = ex2;
		}

		if (errorMsg != null) {
			SqlServerUtils.setDefaultCursor(modelMaker, this);
			ErrorDialog.showErrorDialog(this, errorMsg, ex);
			return;
		}

		MutableMarkedRecordPair mrp = new MutableMarkedRecordPair(q, m, Decision.HOLD, new Date(), "", "", "");
		Collection c = new ArrayList(1);
		c.add(mrp);
		MarkedRecordPairSource mrps = new MarkedRecordPairBinder(c);
		modelMaker.setMultiSource(1, mrps);
		modelMaker.setMultiIncludeHolds(1, true);

		SqlServerUtils.setDefaultCursor(modelMaker, this);

		modelMaker.evaluateClues();

		// select all
		modelMaker.getFilter().reset();
		modelMaker.filterMarkedRecordPairList();
	}

	private void updateEnabledness() {
		ok.setEnabled(getDataSource() != null && 
					  getDbConfiguration() != null && 
					  getQId() != null && getMId() != null);
	}
	
	private DataSource getDataSource() {
		String dsName = (String)dataSource.getSelectedItem();
		return DataSources.getDataSource(dsName);
	}
	
	private String getDbConfiguration() {
		return (String)dbConfiguration.getSelectedItem();
	}
			
	private String getQId() {
		return SqlServerUtils.getText(qId);
	}
	
	private String getMId() {
		return SqlServerUtils.getText(mId);
	}
	
}
