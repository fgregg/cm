/*
 * Created on Jan 9, 2004
 *
 */
package com.choicemaker.cm.io.db.sqlserver.gui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.choicemaker.cm.args.RecordAccess;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.menus.ToolsMenu.ToolAction;

/**
 * @author ajwinkel
 *
 */
public class SqlServerUtilsAction extends ToolAction implements PropertyChangeListener {

	private static final long serialVersionUID = 271L;
	
	public SqlServerUtilsAction() {
		super("SQL Server Utils");
		setEnabled(false);
	}

	public void setModelMaker(ModelMaker m) {
		super.setModelMaker(m);
		m.addPropertyChangeListener(this);
	}

	public void actionPerformed(ActionEvent e) { }

	public void propertyChange(PropertyChangeEvent evt) {
		setEnabled(modelMaker.haveProbabilityModel() && modelMaker.getProbabilityModel().canEvaluate());		
	}

	public static class SqlServerIdSearchAction extends ToolAction implements PropertyChangeListener {

		private static final long serialVersionUID = 271L;

		private RecordAccess dbParams;

		public void setDatabaseParameters(RecordAccess dbp) {
			this.dbParams = dbp;
		}

		public RecordAccess getDatabaseParameters() {
			return this.dbParams;
		}

		public SqlServerIdSearchAction() {
			this(null);
		}
	
		public SqlServerIdSearchAction(RecordAccess dbp) {
			super("SQL Server Record Search...");
			this.dbParams = dbp;
			setEnabled(false);
		}
	
		public void setModelMaker(ModelMaker m) {
			super.setModelMaker(m);
			m.addPropertyChangeListener(this);
		}

		public void actionPerformed(ActionEvent e) {
			RecordAccess dbp = getDatabaseParameters();
			new SqlServerIdSearchDialog(modelMaker,dbp).show();
		}

		public void propertyChange(PropertyChangeEvent evt) {
			setEnabled(modelMaker.haveProbabilityModel() && modelMaker.getProbabilityModel().canEvaluate());		
		}

	}
	
	public static class SqlServerPairViewerAction extends ToolAction implements PropertyChangeListener {

		private static final long serialVersionUID = 271L;

		public SqlServerPairViewerAction() {
			super("SQL Server Pair Viewer...");
			setEnabled(false);
		}
	
		public void setModelMaker(ModelMaker m) {
			super.setModelMaker(m);
			m.addPropertyChangeListener(this);
		}

		public void actionPerformed(ActionEvent e) {
			SqlServerPairViewerDialog.showDialog(modelMaker);
		}

		public void propertyChange(PropertyChangeEvent evt) {
			setEnabled(modelMaker.haveProbabilityModel() && modelMaker.getProbabilityModel().canEvaluate());		
		}
	}
	
}
