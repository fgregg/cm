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
package com.choicemaker.cm.gui.utils.viewer.event;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Enumeration;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.choicemaker.cm.gui.utils.viewer.RecordTable;

public class ColumnDragListener extends MouseAdapter implements MouseMotionListener, TableColumnModelListener {
	RecordTable rtA;
	RecordTable rtB;
	public ColumnDragListener(RecordTable rtA, RecordTable rtB) {
		this.rtA = rtA;
		this.rtB = rtB;
	}

	// aweful, but no better way
	private int getColumnIndex(TableColumnModel m, TableColumn c) {
		if (c == null) {
			return -1;
		}
		Enumeration cols = m.getColumns();
		int i = 0;
		while (cols.nextElement() != c) {
			++i;
		}
		return i;
	}

	public void mouseDragged(MouseEvent e) {
		setDrag();
	}

	private void setDrag() {
		JTableHeader thA = rtA.getTableHeader();
		TableColumn draggedColumn = thA.getDraggedColumn();
		if (draggedColumn != null) {
			JTableHeader thB = rtB.getTableHeader();
			int column = getColumnIndex(rtA.getColumnModel(), draggedColumn);
			thB.setDraggedColumn(rtB.getColumnModel().getColumn(column));
			thB.setDraggedDistance(thA.getDraggedDistance());
			if (column != -1) {
				rtB.getColumnModel().moveColumn(column, column);
			}
		}
	}

	public void mousePressed(MouseEvent e) {
		resetDrag();
	}

	private void resetDrag() {
		JTableHeader thB = rtB.getTableHeader();
		TableColumn col = thB.getDraggedColumn();
		thB.setDraggedColumn(null);
		thB.setDraggedDistance(0);
		if (col != null) {
			int column = getColumnIndex(rtB.getColumnModel(), col);
			rtB.getColumnModel().moveColumn(column, column);
		}
	}

	public void mouseReleased(MouseEvent e) {
		resetDrag();
	}

	public void mouseMoved(MouseEvent e) {
	}

	/** Tells listeners that a column was added to the model. */
	public void columnAdded(TableColumnModelEvent e) {
	}

	/** Tells listeners that a column was removed from the model. */
	public void columnRemoved(TableColumnModelEvent e) {
	}

	/** Tells listeners that a column was repositioned. */
	public void columnMoved(TableColumnModelEvent e) {
//		rtB.getColumnModel().moveColumn(e.getFromIndex(), e.getToIndex());
//		setDrag();
	}

	/** Tells listeners that a column was moved due to a margin change. */
	public void columnMarginChanged(ChangeEvent e) {
	}

	/**
	 * Tells listeners that the selection model of the
	 * TableColumnModel changed.
	 */
	public void columnSelectionChanged(ListSelectionEvent e) {
	}
}
