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
package com.choicemaker.cm.gui.utils.viewer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import com.choicemaker.cm.core.ColumnDefinition;
import com.choicemaker.cm.core.DerivedSource;
import com.choicemaker.cm.core.Descriptor;

/**
 * This class acts as the TableColumnModel and as a Relay of PropertyChangeEvents
 * from the Columns.  This second function is necessary to update the column headers
 * when they change (I don't know why the Table doesn't listen to those messages by default)
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:46 $
 */
public class RecordTableColumnModel extends DefaultTableColumnModel {
	
	//****************** Constants
	
	private static final long serialVersionUID = 1L;

	public static final String ENABLE_EDITING = "ENABLE_EDITING";
	
	//****************** Fields

	private boolean enableEditing;
	private RecordTableColumn[] visibleAndInvisibleColumns;
	
	//****************** Constructors
	
	public RecordTableColumnModel(Descriptor descriptor, boolean add) {
		
		bugFixingInit();
		
		HeaderRenderer rawFieldHeaderRenderer = new HeaderRenderer(false);
		HeaderRenderer derivedFieldHeaderRenderer = new HeaderRenderer(true);
		ColumnDefinition[] columnDefinitions = descriptor.getColumnDefinitions();
		boolean[] derived = descriptor.getEditable(DerivedSource.ALL);
		visibleAndInvisibleColumns = new RecordTableColumn[columnDefinitions.length];
		for (int i = 0; i < columnDefinitions.length; i++) {
			ColumnDefinition columnDefinition = columnDefinitions[i];
			RecordTableColumn recordTableColumn =
				new RecordTableColumn(
					i,
					columnDefinition.getWidth(),
					columnDefinition.getFieldName(),
					columnDefinition.getName());
			if(!derived[i]) {
				recordTableColumn.setHeaderRenderer(derivedFieldHeaderRenderer);
			}
			else{
				recordTableColumn.setHeaderRenderer(rawFieldHeaderRenderer);
			}
			visibleAndInvisibleColumns[i] = recordTableColumn;
			if(add) {
				addColumn(recordTableColumn);
			}
		}
	}

	/**
	 * @see javax.swing.table.TableColumnModel#addColumn(javax.swing.table.TableColumn)
	 */
	public void addColumn(TableColumn column) {
		((RecordTableColumn) column).setVisible(true);
		super.addColumn(column);
		setDisplayIndexes();
	}

	/**
	 * @see javax.swing.table.TableColumnModel#removeColumn(javax.swing.table.TableColumn)
	 */
	public void removeColumn(TableColumn column) {
		((RecordTableColumn) column).setVisible(false);
		super.removeColumn(column);
		setDisplayIndexes();
	}
	
	public boolean areAllColumnsVisible() {
		return this.getColumnCount() == visibleAndInvisibleColumns.length;
	}
	
	public void addAllColumns() {
		for (int i = 0; i < visibleAndInvisibleColumns.length; i++) {
			RecordTableColumn col = visibleAndInvisibleColumns[i];
			if (!col.isVisible()) {
				col.setVisible(true);
				addColumn(col);
			}
		}
		setDisplayIndexes();
	}
	
	public void removeAllColumns() {
		for (int i = 0; i < visibleAndInvisibleColumns.length; i++) {
			RecordTableColumn col = visibleAndInvisibleColumns[i];
			if (col.isVisible()) {
				col.setVisible(false);
				removeColumn(col);
			}
		}
		setDisplayIndexes();
	}
	
	public void toggleAllColumnsVisible() {
		if (areAllColumnsVisible()) {
			removeAllColumns();
		} else {
			addAllColumns();
		}
	}
	
	public void setAlias(TableColumn column, String newValue){
		column.setHeaderValue(newValue);
	}

	public void moveColumn(int columnIndex, int newIndex) {
		super.moveColumn(columnIndex, newIndex);
		if(columnIndex != newIndex) {
			setDisplayIndexes();
		}
	}

	/**
	 * Returns the visibleAndInvisibleColumns.
	 * @return RecordTableColumn[]
	 */
	public RecordTableColumn[] getVisibleAndInvisibleColumns() {
		return visibleAndInvisibleColumns;
	}

	/**
	 * Sets the display index of each column.
	 */
	private void setDisplayIndexes() {
		int columnCount = getColumnCount();
		for (int i = 0; i < columnCount; i++) {
			RecordTableColumn recordTableColumn = (RecordTableColumn)getColumn(i);
			recordTableColumn.setDisplayIndex(i);
		}
	}

	public boolean isEnableEditing() {
		return enableEditing;
	}

	public void setEnableEditing(boolean newValue) {
		Boolean oldValue = new Boolean(enableEditing);
		enableEditing = newValue;
		support.firePropertyChange(ENABLE_EDITING, oldValue, new Boolean(newValue));
	}
	
	// *********************** Bug Fixing Fields and Methods
	
	PropertyChangeSupport support;
	
	protected void bugFixingInit(){
		support = new PropertyChangeSupport(this);
	}
	
	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		support.firePropertyChange(evt);
		super.propertyChange(evt);
	}

	/** 
	 * delegated to the PropertyChangeSupport
	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	/** 
	 * delegated to the PropertyChangeSupport
	 * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}

}
