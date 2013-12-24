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

import java.awt.Dimension;
import java.beans.*;

import javax.swing.*;
import javax.swing.table.JTableHeader;

import com.choicemaker.cm.core.*;
import com.choicemaker.cm.gui.utils.viewer.event.*;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:46 $
 */
public class RecordPairFrame extends InternalFrame {
	private static final Dimension ZERO_DIMENSION = new Dimension(1,1);

	private RecordTable topTable;
	private RecordTable bottomTable;
	private JSplitPane splitPane;
	private JScrollPane topPane;
	private JScrollPane bottomPane;
	private boolean pair;
	private ColumnDragListener drA;

	private RecordPairFramePopupManager bottomPaneFrameManager;
	private RecordPairFramePopupManager topPaneFrameManager;

	public RecordPairFrame(boolean pair, boolean contentEditable) {
		super("", true, true, false, false);
		this.pair = pair;
		setFrameIcon(null);
		setMinimumSize(new Dimension(10, 10));
		topTable = new RecordTable(contentEditable, true);
		topPane = new JScrollPane(topTable);
		topPane.setMinimumSize(ZERO_DIMENSION);
		bottomTable = new RecordTable(contentEditable, false);
		bottomPane = new JScrollPane(bottomTable);
		bottomPane.setMinimumSize(ZERO_DIMENSION);
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, topPane, bottomPane);
		splitPane.setDividerSize(2);
		splitPane.setResizeWeight(0.5);
		if (pair) {
			getContentPane().add(splitPane);
		} else {
			getContentPane().add(topPane);
		}
	}
	
	public void destroy() {
		super.destroy();
		((RecordPairFrameModel) getInternalFrameModel()).getRecordTableColumnModel().removeColumnModelListener(drA);
		topTable.destroy();
		bottomTable.destroy();
	}

	/**
	 * Sets the recordPairFrameModel.
	 * @param recordPairFrameModel The recordPairFrameModel to set
	 */
	public void initInternalFrameModel() {
		RecordPairFrameModel recordPairFrameModel = (RecordPairFrameModel) getInternalFrameModel();

		bottomTable.setColumnModel(recordPairFrameModel.getRecordTableColumnModel());
		topTable.setColumnModel(recordPairFrameModel.getRecordTableColumnModel());
		Descriptor descriptor = recordPairFrameModel.getDescriptor();
		topTable.setDescriptor(descriptor);
		bottomTable.setDescriptor(descriptor);

		JTableHeader thA = topTable.getTableHeader();
		drA = new ColumnDragListener(topTable, bottomTable);
		thA.addMouseMotionListener(drA);
		thA.addMouseListener(drA);
		thA.getColumnModel().addColumnModelListener(drA);

		boolean stackable = descriptor.isStackable();
		topPane.setVerticalScrollBarPolicy(
			stackable ? JScrollPane.VERTICAL_SCROLLBAR_ALWAYS : JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		bottomPane.setVerticalScrollBarPolicy(
			stackable ? JScrollPane.VERTICAL_SCROLLBAR_ALWAYS : JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		topPaneFrameManager = new RecordPairFramePopupManager(topTable, recordPairFrameModel);
		topPane.addMouseListener(topPaneFrameManager);
		bottomPaneFrameManager = new RecordPairFramePopupManager(bottomTable, recordPairFrameModel);
		bottomPane.addMouseListener(bottomPaneFrameManager);

		splitPane.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (JSplitPane.DIVIDER_LOCATION_PROPERTY == evt.getPropertyName()) {
					if (!isIgnoreUpdateFromView())
						((RecordPairFrameModel) getInternalFrameModel()).setDividerLocation(
							splitPane.getDividerLocation());
				}
			}
		});
	}

	/**
	 * Overridden to update the splitPane position and to enable and disable the Layout changes.
	 */
	public void updateFromModel() {
		super.updateFromModel();

		// AJW: added to avoid exception on set layout editable
		if (getInternalFrameModel() == null) {
			return;
		}
		
		splitPane.setDividerLocation(((RecordPairFrameModel) getInternalFrameModel()).getDividerLocation());

		boolean enableEditing = getInternalFrameModel().isEnableEditing();
		topPaneFrameManager.setEnableEditing(enableEditing);
		bottomPaneFrameManager.setEnableEditing(enableEditing);
		splitPane.setEnabled(enableEditing);
	}

	public void setRecordData(RecordData recordData) {
		topTable.setRecordData(recordData);
		if (pair) {
			bottomTable.setRecordData(recordData);
		}
	}

}
