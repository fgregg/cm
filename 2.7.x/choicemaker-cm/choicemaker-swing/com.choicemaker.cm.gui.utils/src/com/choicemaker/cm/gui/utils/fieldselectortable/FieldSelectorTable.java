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
package com.choicemaker.cm.gui.utils.fieldselectortable;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;

import com.choicemaker.cm.gui.utils.viewer.RecordTableColumnModel;

/**
 * .  
 * 
 * @author Arturo Falck
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:46 $
 */
public class FieldSelectorTable extends JTable{

	private static Logger logger = Logger.getLogger(FieldSelectorTable.class);
	private static int VISIBLE_COLUMN = 2;

	private TableCellRenderer visibilityHeaderRenderer;
	private TableModelListener tableListener;

	public FieldSelectorTable() {
		super();
		init();
	}

	private void init() {
		setAutoCreateColumnsFromModel(true);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		JTextField textField = new JTextField();
		final TableCellEditor textEditor = new DefaultCellEditor(textField);
		setDefaultEditor(String.class, textEditor);
		
		textField.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				textEditor.stopCellEditing();
			}
		});
	
		getTableHeader().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				TableColumnModel colModel = getColumnModel();
				int columnModelIndex = colModel.getColumnIndexAtX(e.getX());
				int modelIndex = colModel.getColumn(columnModelIndex).getModelIndex();
				if (modelIndex == VISIBLE_COLUMN) {
					FieldSelectorTableModel model = (FieldSelectorTableModel)getModel();
					model.toggleAllColumnsVisible();
				}				
			}
		});
	}
	
	public void setModel(RecordTableColumnModel model){
		if (tableListener == null) {
			tableListener = new TableModelListener() {
				public void tableChanged(TableModelEvent e) {
					getTableHeader().repaint();
				}
			};
		}
		getModel().removeTableModelListener(tableListener);
		setModel(new FieldSelectorTableModel(model));
		getModel().addTableModelListener(tableListener);

		if (visibilityHeaderRenderer == null) {
			visibilityHeaderRenderer = new VisibilityHeaderRenderer();
		}		
		getColumnModel().getColumn(VISIBLE_COLUMN).setHeaderRenderer(visibilityHeaderRenderer);
	}
	
	private class VisibilityHeaderRenderer extends JCheckBox implements TableCellRenderer {
		public VisibilityHeaderRenderer() {
			setHorizontalAlignment(JCheckBox.CENTER);
			setMargin(new Insets(0, 0, 0, 0));
			setBorderPainted(true);
		}
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			FieldSelectorTableModel fstm = (FieldSelectorTableModel)table.getModel();
			setText(value.toString());
			setSelected(fstm.areAllColumnsVisible());
			
			JTableHeader header = table.getTableHeader();
			if (header != null) {
				setForeground(header.getForeground());
				setBackground(header.getBackground());
				setFont(header.getFont());
				setSize(new Dimension(getWidth(), table.getRowHeight()));
			}

			setBorder(UIManager.getBorder("TableHeader.cellBorder"));
						
			return this;
		}
	}

}
