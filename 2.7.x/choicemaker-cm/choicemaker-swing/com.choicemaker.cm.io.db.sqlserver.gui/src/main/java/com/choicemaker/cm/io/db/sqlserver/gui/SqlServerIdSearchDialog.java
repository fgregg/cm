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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.logging.Logger;

import javax.sql.DataSource;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.choicemaker.cm.args.AbaSettings;
import com.choicemaker.cm.core.DatabaseException;
import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.MarkedRecordPairSource;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.base.MarkedRecordPairBinder;
import com.choicemaker.cm.core.base.Match;
import com.choicemaker.cm.core.base.MutableMarkedRecordPair;
import com.choicemaker.cm.core.base.RecordDecisionMaker;
import com.choicemaker.cm.core.base.Thresholds;
import com.choicemaker.cm.gui.utils.dialogs.ErrorDialog;
import com.choicemaker.cm.io.blocking.automated.AbaStatistics;
import com.choicemaker.cm.io.blocking.automated.AbaStatisticsCache;
import com.choicemaker.cm.io.blocking.automated.AutomatedBlocker;
import com.choicemaker.cm.io.blocking.automated.DatabaseAccessor;
import com.choicemaker.cm.io.blocking.automated.base.Blocker2;
import com.choicemaker.cm.io.db.base.DataSources;
import com.choicemaker.cm.io.db.sqlserver.SqlServerXmlUtils;
import com.choicemaker.cm.io.db.sqlserver.dbom.SqlDbObjectMaker;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;

/**
 * @author ajwinkel
 *
 */
public class SqlServerIdSearchDialog extends JDialog {

	private static final long serialVersionUID = 271L;
	
	private static final Logger logger = Logger
			.getLogger(SqlServerIdSearchDialog.class.getName());

	private static final int MIN_LPBS = 10;
	private static final int MIN_STBSGL = MIN_LPBS;
	private static final int MIN_LSBS = MIN_STBSGL * 2;

	private static final int DEFAULT_LPBS = 50;
	private static final int DEFAULT_STBSGL = 100;
	private static final int DEFAULT_LSBS = 200;
	
	private ModelMaker modelMaker;
	private ImmutableProbabilityModel model;

	private JComboBox dataSource;
	private JRadioButton predefined, manual;
	// This configuration option no longer makes sense since the Analyzer
	// configuration no longer defines production models. For now, the
	// combo box will just be disabled, rather than removed.
	private JComboBox productionConfiguration;
	private BlockingParametersPanel blockingParametersPanel;
	private JTextField qId;
	private JButton ok, cancel;

	public SqlServerIdSearchDialog(ModelMaker modelMaker) {
		super(modelMaker, "SQL Server Record Search", false);
		this.modelMaker = modelMaker;
		this.model = modelMaker.getProbabilityModel();

		SqlServerUtils.maybeInitConnectionPools();
//		SqlServerUtils.maybeInitProductionModels();
		
		createContent();
		createListeners();
		updateEnabledness();

		setResizable(false);
				
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
		getContentPane().add(Box.createVerticalStrut(5), c);
		
		//
		
		c.gridy++;
		
		c.gridx = 0;
		c.gridwidth = 1;
		getContentPane().add(new JLabel("Data Source: "), c);
		
		c.gridx = 1;
		c.gridwidth = 3;
		dataSource = SqlServerUtils.createDataSource();
		getContentPane().add(dataSource, c);
				
		//
		
		c.gridy++;
		getContentPane().add(Box.createVerticalStrut(10), c);
				
		//
				
		c.gridy++;
		
		c.gridx = 0;
		c.gridwidth = 2;
		predefined = new JRadioButton("Predefined Configuration");
		getContentPane().add(predefined, c);
		
		c.gridx = 2;
		productionConfiguration = SqlServerUtils.createProductionConfigurationsComboBox();
		getContentPane().add(productionConfiguration, c);

		//
		
		c.gridy++;
		
		c.gridx = 0;
		c.gridwidth = 2;
		manual = new JRadioButton("Manual Configuration");
		getContentPane().add(manual, c);

		//
		
		c.gridy++;
		getContentPane().add(Box.createVerticalStrut(5), c);

		//
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 4;
		blockingParametersPanel = new BlockingParametersPanel(
			SqlServerUtils.getDbConfigurations(model),
			SqlServerUtils.getBlockingConfigurations(model),
			DEFAULT_LPBS,
			DEFAULT_STBSGL,
			DEFAULT_LSBS);
		blockingParametersPanel.setBorder(BorderFactory.createTitledBorder("Blocking Parameters"));
		getContentPane().add(blockingParametersPanel, c);
		
		//
		
		c.gridy++;
		getContentPane().add(Box.createVerticalStrut(5), c);

		//
		
		c.gridy++;
		
		c.gridx = 0;
		c.gridwidth = 1;
		getContentPane().add(new JLabel("Record id:"), c);
		
		c.gridx = 1;
		c.gridwidth = 3;
		qId = new JTextField(10);
		getContentPane().add(qId, c);

		//
		
		c.gridy++;
		getContentPane().add(Box.createVerticalStrut(5), c);
						
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
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(predefined);
		bg.add(manual);
		predefined.setSelected(true);
		
		productionConfiguration.setEnabled(false);
		blockingParametersPanel.setEnabled(true);
		
//		predefined.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				blockingParametersPanel.setEnabled(false);
////				productionConfiguration.setEnabled(true);
//				// This method is very problematic. It **changes** the name
//				// of model -- it requires this entire  module to work with mutable
//				// models, unlike every other DB GUI plugin -- just to pretty up
//				// a user interface.
////				ImmutableProbabilityModel prodModel = SqlServerUtils.getProductionConfiguration(getPredefinedConfigurationName());
//				// END discussion
////				if (prodModel != null) {
////					blockingParametersPanel.setFromProperties(prodModel);
////				}
//			}
//		});
		
//		manual.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				productionConfiguration.setEnabled(false);
//				blockingParametersPanel.setEnabled(true);
//			}
//		});
				
		dataSource.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				updateEnabledness();
			}
		});
		
		blockingParametersPanel.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (BlockingParametersPanel.BLOCKING_PARAMETERS_PROPERTY.equals(evt.getPropertyName())) {
					updateEnabledness();
				}
			}
		});
		
		qId.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) { updateEnabledness(); }
			public void removeUpdate(DocumentEvent e) { updateEnabledness(); }
			public void changedUpdate(DocumentEvent e) { updateEnabledness(); }
		});
		
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				findMatches();
			}
		});
	}
	
	private void findMatches() {
		SqlServerUtils.setWaitCursor(modelMaker, this);

		DataSource ds = getDataSource();
		DatabaseAccessor dbAccessor = SqlServerUtils.createDatabaseAccessor(ds);
		
		String dbConfiguration = blockingParametersPanel.getSelectedDbConfiguration();
		String blockingConfiguration = blockingParametersPanel.getSelectedBlockingConfiguration();
		int lpbs = blockingParametersPanel.getLPBS();
		int stbsgl = blockingParametersPanel.getSTBSGL();
		int lsbs = blockingParametersPanel.getLSBS();
		
		model.properties().put(SqlServerXmlUtils.PN_DB_CONFIGURATION, dbConfiguration);
		model.properties().put(SqlServerXmlUtils.PN_BLOCKING_CONFIGURATION, blockingConfiguration);
		model.properties().put(SqlServerXmlUtils.PN_LIMITPERBLOCKINGSET, String.valueOf(lpbs));
		model.properties().put(SqlServerXmlUtils.PN_SINGLETABLEBLOCKINGSETGRACELIMIT, String.valueOf(stbsgl));
		model.properties().put(SqlServerXmlUtils.PN_LIMITSINGLEBLOCKINGSET, String.valueOf(lsbs));
		model.properties().put(SqlDbObjectMaker.getMultiKey(model, dbConfiguration),
							 SqlDbObjectMaker.getMultiQuery(model, dbConfiguration));		
		
		// FIXME temporary HACK
		AbaStatisticsCache statsCache = null;
		String msg =
			"SqlServerIdSearchDialog.findMatches: "
					+ "HACK: Null cache for ABA statistics";
		logger.severe(msg);
		// END FIXME
		try {
			SqlServerUtils.maybeUpdateCounts(ds, model, statsCache);
		} catch (SQLException ex) {
			SqlServerUtils.setDefaultCursor(modelMaker, this);
			ErrorDialog.showErrorDialog(this, "Error updating counts: " + ex, ex);
			return;
		} catch (DatabaseException e) {
			SqlServerUtils.setDefaultCursor(modelMaker, this);
			ErrorDialog.showErrorDialog(this, "Error updating counts: " + e, e);
			return;
		}
								
		String qId = getQId();
		Record q = null;
		try {
			q = SqlServerUtils.readRecord(model, ds, qId);
		} catch (IOException ex) {
			SqlServerUtils.setDefaultCursor(modelMaker, this);
			ErrorDialog.showErrorDialog(this, "Error reading record with ID " + qId, ex);
			return;
		} catch (NoSuchElementException ex) {
			SqlServerUtils.setDefaultCursor(modelMaker, this);
			ErrorDialog.showErrorDialog(this, "No record with ID " + qId, ex);
			return;
		}

		// FIXME temporary HACK
		AbaSettings FIXME = null;
		final String databaseConfig = null;
		final String _blockingConfig = null;
		// END FIXME
		final int limitPBS = FIXME.getLimitPerBlockingSet();
		final int stbgl = FIXME.getSingleTableBlockingSetGraceLimit();
		final int limitSBS = FIXME.getLimitSingleBlockingSet();
		final AbaStatistics stats = statsCache.getStatistics(model);
		if (blockingConfiguration == null
				|| !blockingConfiguration.equals(_blockingConfig)) {
			msg =
				"Blocking configuration from GUI ('" + blockingConfiguration
						+ "' does not equal value from application ('"
						+ _blockingConfig + "').";
			logger.warning(msg);
		}
		AutomatedBlocker blocker =
			new Blocker2(dbAccessor, model, q, limitPBS, stbgl, limitSBS,
					stats, databaseConfig, blockingConfiguration);
		Thresholds t = modelMaker.getThresholds();
		SortedSet matches = null;
		try {
			matches = RecordDecisionMaker.getPairs(q, blocker, model, t.getDifferThreshold(), t.getMatchThreshold());
		} catch (IOException ex) {
			SqlServerUtils.setDefaultCursor(modelMaker, this);
			ErrorDialog.showErrorDialog(this, "Internal Error", ex);
			return;
		}
						
		Object actualId = q.getId();
		ArrayList pairs = new ArrayList(matches.size());				
		Iterator it = matches.iterator();
		while (it.hasNext()) {
			Match match = (Match)it.next();
			Record m = match.m;
			if (!actualId.equals(m.getId())) {
				pairs.add(new MutableMarkedRecordPair(q, m, Decision.HOLD, new Date(), "", "", ""));
			}
		}
		
		// check to make sure we actually have something...
		if (pairs.size() == 0) {
			SqlServerUtils.setDefaultCursor(modelMaker, this);
			modelMaker.setMultiSource(1, null);
			ErrorDialog.showErrorDialog(this, "No records returned from blocking for ID " + qId);
			return;
		}
				
		// open and evaluate the pairs
		MarkedRecordPairSource mrps = MarkedRecordPairBinder.getMarkedRecordPairSource(pairs);
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
					  blockingParametersPanel.getSelectedDbConfiguration() != null && 
					  blockingParametersPanel.getSelectedBlockingConfiguration() != null &&
					  areIntParamsValid() &&
					  getQId() != null);
	}
	
	private DataSource getDataSource() {
		String dsName = (String)dataSource.getSelectedItem();
		return DataSources.getDataSource(dsName);
	}

//	private String getPredefinedConfigurationName() {
//		return (String)productionConfiguration.getSelectedItem();
//	}
		
	private boolean areIntParamsValid() {
		try {
			int lpbs = blockingParametersPanel.getLPBS();
			int stbsgl = blockingParametersPanel.getSTBSGL();
			int lsbs = blockingParametersPanel.getLSBS();
			
			return lpbs >= MIN_LPBS &&
				stbsgl >= lpbs && stbsgl >= MIN_STBSGL &&
				lsbs >= stbsgl && lsbs >= MIN_LSBS;
		} catch (NumberFormatException ex) {
			return false;
		}
	}
		
	private String getQId() {
		return SqlServerUtils.getText(qId);
	}
		
}
